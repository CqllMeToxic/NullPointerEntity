package lol.cqllmetoxic.nullpointerentity.network;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.network.payload.ClientCapabilitiesPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.ClientLocalTimePayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.ClientUsernamePayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.EffectTriggerPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.PassiveEffectsPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.PrivacyConsentPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.RunEventPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.SessionPolicyPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.SessionPrivacyPayload;
import lol.cqllmetoxic.nullpointerentity.network.payload.WriteFilePayload;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * server-side networking handlers and packet helpers.
 */
public final class ServerNetworking {
    public static final String EFFECT_FAKE_DEATH = "fake_death";
    /** crashes the receiving player's own client (used for the story's fake game crash) */
    public static final String EFFECT_CRASH_GAME = "crash_game";
    /** flashes the receiving player's own screen (jumpscare flash before the entity crash) */
    public static final String EFFECT_JUMPSCARE_FLASH = "jumpscare_flash";
    /** toggles the red-rain overlay on the receiving player's own client (durationMs > 0 = on) */
    public static final String EFFECT_RED_RAIN = "red_rain";

    private ServerNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(SessionPolicyPayload.ID, SessionPolicyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(EffectTriggerPayload.ID, EffectTriggerPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SessionPrivacyPayload.ID, SessionPrivacyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RunEventPayload.ID, RunEventPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WriteFilePayload.ID, WriteFilePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PassiveEffectsPayload.ID, PassiveEffectsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClientCapabilitiesPayload.ID, ClientCapabilitiesPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PrivacyConsentPayload.ID, PrivacyConsentPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClientLocalTimePayload.ID, ClientLocalTimePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClientUsernamePayload.ID, ClientUsernamePayload.CODEC);
    }

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ClientCapabilitiesPayload.ID, (payload, context) -> {
            context.server().execute(() -> NullPointerEntity.LOGGER.debug(
                "Client capabilities from {}: supportsEffects={}",
                context.player().getName().getString(),
                payload.supportsEffects()
            ));
        });

        ServerPlayNetworking.registerGlobalReceiver(PrivacyConsentPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ConsentState.setConsent(context.player().getUuid(), payload.consentGranted());
                NullPointerEntity.LOGGER.info(
                    "Updated privacy consent for {}: {}",
                    context.player().getName().getString(),
                    payload.consentGranted()
                );
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ClientLocalTimePayload.ID, (payload, context) ->
            context.server().execute(() ->
                ClientLocalTimeState.set(context.player().getUuid(), payload.hour(), payload.minute())));

        ServerPlayNetworking.registerGlobalReceiver(ClientUsernamePayload.ID, (payload, context) ->
            context.server().execute(() ->
                ClientUsernameState.set(context.player().getUuid(), payload.displayName())));
    }

    public static void sendSessionPolicy(ServerPlayerEntity player) {
        if (player == null || !ServerPlayNetworking.canSend(player, SessionPolicyPayload.ID)) {
            return;
        }
        MultiplayerPolicy policy = resolvePolicy(player);
        ServerPlayNetworking.send(player, new SessionPolicyPayload(policy.name()));
    }

    public static void sendFakeDeathEffect(ServerPlayerEntity player, long durationMs) {
        ServerPlayNetworking.send(player, new EffectTriggerPayload(EFFECT_FAKE_DEATH, durationMs));
    }

    /**
     * crashes the given player's OWN client. on an integrated server (singleplayer / LAN host) the
     * client and server share a JVM, so this also ends the host's session; on a dedicated server it
     * only ends that one client and never kills the shared server process for everyone.
     */
    public static void sendCrashGame(ServerPlayerEntity player) {
        if (player == null || !ServerPlayNetworking.canSend(player, EffectTriggerPayload.ID)) {
            return;
        }
        ServerPlayNetworking.send(player, new EffectTriggerPayload(EFFECT_CRASH_GAME, 0L));
    }

    /** flashes the given player's OWN screen (the jumpscare flash), instead of only the host's. */
    public static void sendJumpscareFlash(ServerPlayerEntity player) {
        if (player == null || !ServerPlayNetworking.canSend(player, EffectTriggerPayload.ID)) {
            return;
        }
        ServerPlayNetworking.send(player, new EffectTriggerPayload(EFFECT_JUMPSCARE_FLASH, 0L));
    }

    /** toggles the red-rain overlay on the given player's OWN client (was host-only before). */
    public static void sendRedRain(ServerPlayerEntity player, boolean active) {
        if (player == null || !ServerPlayNetworking.canSend(player, EffectTriggerPayload.ID)) {
            return;
        }
        ServerPlayNetworking.send(player, new EffectTriggerPayload(EFFECT_RED_RAIN, active ? 1L : 0L));
    }

    /** pushes the given player's active passive "loss of control" effects to their own client. */
    public static void sendPassiveEffects(ServerPlayerEntity player, int flags, int tintPacked, float mouseSensitivityMultiplier) {
        if (player == null || !ServerPlayNetworking.canSend(player, PassiveEffectsPayload.ID)) {
            return;
        }
        ServerPlayNetworking.send(player, new PassiveEffectsPayload(flags, tintPacked, mouseSensitivityMultiplier));
    }

    /** sends the host's Privacy Mode (the session setting) to a single player. */
    public static void sendSessionPrivacy(ServerPlayerEntity player) {
        if (player == null || !ServerPlayNetworking.canSend(player, SessionPrivacyPayload.ID)) {
            return;
        }
        ServerPlayNetworking.send(player, new SessionPrivacyPayload(PrivacyManager.isPrivacyEnabledRaw()));
    }

    /** broadcasts the host's Privacy Mode to every online player (used when the host toggles it). */
    public static void broadcastSessionPrivacy(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendSessionPrivacy(player);
        }
    }

    /**
     * asks a client to run a story event locally against its own machine. the client reads its own
     * data and shows its own messages/effects; nothing comes back to the server.
     */
    public static void sendRunEvent(ServerPlayerEntity player, int eventId) {
        if (player == null || !ServerPlayNetworking.canSend(player, RunEventPayload.ID)) {
            return;
        }
        ServerPlayNetworking.send(player, new RunEventPayload(eventId));
    }

    /**
     * asks a client to write a server-computed file on its own machine. used for events whose content
     * depends on server-authoritative data (the phase-1 analysis files built from Minecraft stats).
     * the client-side write is privacy-gated, so nothing is written when Privacy Mode is on.
     */
    public static void sendWriteFile(ServerPlayerEntity player, String fileName, String content, String location) {
        if (player == null || !ServerPlayNetworking.canSend(player, WriteFilePayload.ID)) {
            return;
        }
        ServerPlayNetworking.send(player, new WriteFilePayload(fileName, content, location));
    }

    public static MultiplayerPolicy resolvePolicy(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) {
            return MultiplayerPolicy.SAFE_ONLY;
        }

        if (player.getServer().isDedicated()) {
            return MultiplayerPolicy.SAFE_ONLY;
        }

        int playerCount = player.getServer().getPlayerManager().getPlayerList().size();
        if (playerCount <= 1) {
            return MultiplayerPolicy.IMMERSIVE_LOCAL_ALLOWED;
        }

        return MultiplayerPolicy.SAFE_ONLY;
    }

    public static boolean isSensitiveActionAllowed(ServerPlayerEntity player) {
        // governed by the player's own consent (= their Privacy Mode being off), in single player
        // and multiplayer alike.
        return ConsentState.hasConsent(player.getUuid());
    }
}
