package lol.cqllmetoxic.nullpointerentity.network;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.NullPointerEntityClient;
import lol.cqllmetoxic.nullpointerentity.client.ClientPassiveEffects;
import lol.cqllmetoxic.nullpointerentity.network.payload.ClientCapabilitiesPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.EffectTriggerPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.PassiveEffectsPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.PrivacyConsentPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.RunEventPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.SessionPolicyPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.SessionPrivacyPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.WriteFilePayload;
import lol.cqllmetoxic.nullpointerentity.client.event.ClientEventExecutor;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/**
 * client-side packet handlers and initial capability/consent messages.
 */
public final class ClientNetworking {
    private ClientNetworking() {
    }

    public static void initialize() {
        // resync consent + this client's display name with the server whenever privacy mode changes
        // (incl. when the host's session override arrives), so the reported name reflects the current mode
        PrivacyManager.setChangeListener(ClientNetworking::onPrivacyChanged);

        ClientPlayNetworking.registerGlobalReceiver(SessionPolicyPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    MultiplayerPolicy policy = MultiplayerPolicy.valueOf(payload.policy());
                    ClientSessionState.setCurrentPolicy(policy);
                    NullPointerEntity.LOGGER.info("Received multiplayer session policy: {}", policy);
                } catch (IllegalArgumentException ex) {
                    NullPointerEntity.LOGGER.warn("Unknown multiplayer policy '{}' from server", payload.policy());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(EffectTriggerPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (ServerNetworking.EFFECT_FAKE_DEATH.equals(payload.effectType())) {
                    NullPointerEntityClient.triggerFakeDeathScreen(payload.durationMs());
                } else if (ServerNetworking.EFFECT_CRASH_GAME.equals(payload.effectType())) {
                    // crash THIS client only - never the shared server (the server never System.exits)
                    NullPointerEntity.LOGGER.info("NullPointerEntity is crashing this client...");
                    System.exit(-1);
                } else if (ServerNetworking.EFFECT_JUMPSCARE_FLASH.equals(payload.effectType())) {
                    // flash THIS client's own screen (was host-only before)
                    NullPointerEntityClient.triggerJumpscareFlash();
                } else if (ServerNetworking.EFFECT_RED_RAIN.equals(payload.effectType())) {
                    // toggle the red-rain overlay on THIS client (was host-only before)
                    lol.cqllmetoxic.nullpointerentity.client.RedRainRenderer.setRedRainActive(payload.durationMs() > 0);
                }
            });
        });

        // the host's Privacy Mode for this session, which governs this client's own invasive execution
        ClientPlayNetworking.registerGlobalReceiver(SessionPrivacyPayload.ID, (payload, context) -> {
            context.client().execute(() -> PrivacyManager.setSessionOverride(payload.privacyEnabled()));
        });

        // server asks this client to run a story event locally, against its own machine
        ClientPlayNetworking.registerGlobalReceiver(RunEventPayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientEventExecutor.run(payload.eventId()));
        });

        // server pushes this client's active passive "loss of control" effects, applied locally
        ClientPlayNetworking.registerGlobalReceiver(PassiveEffectsPayload.ID, (payload, context) -> {
            context.client().execute(() -> ClientPassiveEffects.apply(payload));
        });

        // server asks this client to write a (server-computed) file on its own machine (privacy-gated in the sink)
        ClientPlayNetworking.registerGlobalReceiver(WriteFilePayload.ID, (payload, context) -> {
            context.client().execute(() ->
                lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.createSystemFileInCommonLocation(
                    payload.fileName(), payload.content(), payload.location()));
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // figure out if the server has the mod by whether it can receive one of our c2s payloads.
            // SessionPolicyPayload is s2c-only, so canSend(SessionPolicyPayload.ID) is always false (the
            // server never registers a receiver for it) - which made every remote server look mod-less and
            // hid the mic/ptt controls on the privacy screen. ClientCapabilitiesPayload is c2s and the
            // server does register a receiver for it, so that's the right thing to probe.
            boolean serverHasMod = ClientPlayNetworking.canSend(ClientCapabilitiesPayload.ID);
            ClientSessionState.setServerModPresent(serverHasMod);

            if (serverHasMod) {
                ClientPlayNetworking.send(new ClientCapabilitiesPayload(true));
                // report this client's own local time so return messages use the player's clock, not the host's
                java.time.LocalTime localTime = java.time.LocalTime.now();
                ClientPlayNetworking.send(new lol.cqllmetoxic.nullpointerentity.network.payload.ClientLocalTimePayload(
                    localTime.getHour(), localTime.getMinute()));
                // report this client's own display name so server-side event chat/files use it, not the host's
                sendClientUsername();
            }
            sendPrivacyConsent();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientSessionState.setServerModPresent(false);
            ClientSessionState.setCurrentPolicy(MultiplayerPolicy.SAFE_ONLY);
            // drop the host's session override so the local privacy toggle applies again
            PrivacyManager.setSessionOverride(null);
            // clear any active passive effects so they don't carry into the next session
            ClientPassiveEffects.clear();
        });
    }

    /**
     * pushes the client's current consent state (the inverse of privacy mode) to the server.
     * runs on the client thread and only sends when connected to a server that can receive it,
     * so it's safe to call from arbitrary threads (privacy screen, commands) and modless servers.
     */
    public static void sendPrivacyConsent() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        client.execute(() -> {
            if (client.getNetworkHandler() != null && ClientPlayNetworking.canSend(PrivacyConsentPayload.ID)) {
                ClientPlayNetworking.send(new PrivacyConsentPayload(!PrivacyManager.isPrivacyEnabled()));
            }
        });
    }

    /** runs when privacy mode changes (incl. the host's session override arriving): resync both signals. */
    private static void onPrivacyChanged() {
        sendPrivacyConsent();
        sendClientUsername();
    }

    /**
     * reports this client's own display username to the server. the name is privacy-processed here
     * ({@link PrivacyManager#getSystemUsername}), so under Privacy Mode only an anonymized "User####"
     * leaves the client; the real OS username is only sent when privacy is off.
     */
    public static void sendClientUsername() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        client.execute(() -> {
            if (client.getNetworkHandler() != null
                && ClientPlayNetworking.canSend(lol.cqllmetoxic.nullpointerentity.network.payload.ClientUsernamePayload.ID)) {
                String displayName = PrivacyManager.getSystemUsername(System.getProperty("user.name"));
                ClientPlayNetworking.send(new lol.cqllmetoxic.nullpointerentity.network.payload.ClientUsernamePayload(displayName));
            }
        });
    }
}
