package lol.cqllmetoxic.nullpointerentity;

import lol.cqllmetoxic.nullpointerentity.entity.ModEntities;
import lol.cqllmetoxic.nullpointerentity.entity.client.FakePlayerEntityRenderer;
import lol.cqllmetoxic.nullpointerentity.client.JumpscareFlash;
import lol.cqllmetoxic.nullpointerentity.client.FakeDeathScreen;
import lol.cqllmetoxic.nullpointerentity.network.ClientNetworking;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyEventHandler;
import lol.cqllmetoxic.nullpointerentity.client.ClientWakeDetection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import lol.cqllmetoxic.nullpointerentity.config.VoiceChatConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * client-side initialization for the mod.
 * handles rendering, UI screens, and client-only features.
 * runs separately from server-side code. (server side being your world)
 */
public class NullPointerEntityClient implements ClientModInitializer {
    // biggest/most accurate english model vosk has - also the fallback when a language's own model won't load
    private static final String DEFAULT_VOSK_MODEL = "vosk-model-en-us-0.42-gigaspeech";
    private static boolean shouldShowPrivacyScreen = false;
    private static boolean shouldTriggerJumpscareFlash = false;
    private static boolean shouldShowFakeDeathScreen = false;
    private static long fakeDeathDuration = 5000;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.FAKE_PLAYER_ENTITY, FakePlayerEntityRenderer::new);

        PrivacyEventHandler.initialize();
        ClientNetworking.initialize();
        initializeShriekClient();
        lol.cqllmetoxic.nullpointerentity.client.VoicePushToTalk.initialize();
        JumpscareFlash.initialize();
        ClientWakeDetection.initialize();
        lol.cqllmetoxic.nullpointerentity.client.SleepPauseDetector.initialize();
        lol.cqllmetoxic.nullpointerentity.client.RedRainRenderer.initialize();

        // tick handler for client-side screen triggers
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) return;

            if (shouldShowPrivacyScreen) {
                shouldShowPrivacyScreen = false;
                client.execute(() -> {
                    client.setScreen(new lol.cqllmetoxic.nullpointerentity.privacy.PrivacyScreen(client.currentScreen));
                });
            }

            if (shouldTriggerJumpscareFlash) {
                shouldTriggerJumpscareFlash = false;
                JumpscareFlash.triggerJumpscareFlash();
            }

            if (shouldShowFakeDeathScreen) {
                shouldShowFakeDeathScreen = false;
                client.execute(() -> {
                    client.setScreen(new FakeDeathScreen(fakeDeathDuration));
                });
            }
        });
    }

    private void initializeShriekClient() {
        if (!VoiceChatConfig.isVoiceChatEnabled()) {
            NullPointerEntity.LOGGER.info("Client: voice chat disabled in config");
            return;
        }

        if (FabricLoader.getInstance().isModLoaded("shriek") && FabricLoader.getInstance().isModLoaded("architectury")) {
            tryLoadVoskModel();
            try {
                com.pryzmm.api.ShriekApi.setPrintToConsole(false);
                com.pryzmm.api.ShriekApi.registerClientSpeechListener(event -> {
                    try {
                        String text = ((com.pryzmm.api.events.ClientTalkEvent) event).getText();
                        if (text != null && !text.isBlank()) {
                            NullPointerEntity.LOGGER.info("[Shriek Client] {}", text);
                        }
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("Error processing Shriek client event: {}", e.getMessage());
                    }
                });
                NullPointerEntity.LOGGER.info("Shriek client initialized");
            } catch (Throwable t) {
                NullPointerEntity.LOGGER.info("Shriek client API not available: {}", t.getMessage());
            }
            applyPushToTalkPreference();
        } else {
            NullPointerEntity.LOGGER.info("Client: Shriek/Architectury not installed - voice chat disabled");
        }
    }

    /**
     * switches Shriek into push-to-talk mode so AURORA only "hears" the mic while the player
     * holds Shriek's voice keybind (rebindable in Options -> Controls). when push-to-talk is
     * disabled in config, Shriek keeps its default always-listening behaviour (the key mutes).
     *
     * <p>Shriek's recording loop is gated by {@code ShriekClient.alwaysOnRecording}:
     * {@code true} = always recording / key mutes (Shriek default), {@code false} = records only
     * while the key is held (push-to-talk). it's a plain public static, so flipping it is enough.
     * reading the field also forces Shriek's class init to run first, so our value always wins.
     *
     * <p>safe to call any time (e.g. from the privacy screen toggle): it no-ops when Shriek isn't
     * installed, and the Throwable guard covers a missing or changed Shriek class.
     */
    public static void applyPushToTalkPreference() {
        if (!FabricLoader.getInstance().isModLoaded("shriek")) {
            return;
        }
        boolean pushToTalk = VoiceChatConfig.isPushToTalkEnabled();
        try {
            com.pryzmm.ShriekClient.alwaysOnRecording = !pushToTalk;
            relabelVoiceKeybind(pushToTalk);
            NullPointerEntity.LOGGER.info("Shriek voice input mode: {}", pushToTalk ? "push-to-talk" : "always-on");
        } catch (Throwable t) {
            NullPointerEntity.LOGGER.info("Could not set Shriek push-to-talk mode: {}", t.getMessage());
        }
    }

    /**
     * relabels Shriek's voice key in Options -> Controls. in push-to-talk mode it reads as
     * "Push to Talk (AURORA)"; otherwise it keeps Shriek's own "Push To Mute" text (which is
     * accurate when the mic is always listening). only the display name changes.
     */
    private static void relabelVoiceKeybind(boolean pushToTalk) {
        net.minecraft.client.option.KeyBinding voiceKey = com.pryzmm.ShriekClient.vKeyMapping;
        if (voiceKey == null) {
            return;
        }
        String label = pushToTalk ? "key.nullpointerentity.talk_to_aurora" : "key.shriek.push_to_mute";
        ((lol.cqllmetoxic.nullpointerentity.mixin.client.KeyBindingAccessor) (Object) voiceKey)
            .nullpointerentity$setTranslationKey(label);
    }

    private void tryLoadVoskModel() {
        if (!FabricLoader.getInstance().isModLoaded("shriek") || !FabricLoader.getInstance().isModLoaded("architectury")) {
            return;
        }

        String[] preferredModels = resolvePreferredModelNames();
        for (String modelName : preferredModels) {
            try {
                com.pryzmm.client.event.EventHandler.loadVoskModel(modelName);
                NullPointerEntity.LOGGER.info("Shriek Vosk model requested: {}", modelName);
                return;
            } catch (Throwable t) {
                // ignore and try next
            }
        }
        try {
            com.pryzmm.client.event.EventHandler.loadVoskModel();
            NullPointerEntity.LOGGER.info("Shriek Vosk model requested (no-arg)");
        } catch (Throwable t) {
            NullPointerEntity.LOGGER.debug("Failed to load generic vosk model", t);
        }
    }

    private String[] resolvePreferredModelNames() {
        String languageCode = resolveLanguageCode();
        String primary = languageCode.split("[_-]", 2)[0];

        // biggest model vosk has per language, falling back to the big english one. pl/tr/ko only ship a
        // small model so they stay small. sizes are roughly what shriek downloads the first time you talk.
        switch (primary) {
            case "de":
                // 1.9G (a 4.4G "vosk-model-de-tuda-0.6-900k" exists too if you want to go all-in)
                return new String[]{"vosk-model-de-0.21", DEFAULT_VOSK_MODEL};
            case "fr":
                return new String[]{"vosk-model-fr-0.22", DEFAULT_VOSK_MODEL}; // 1.4G
            case "es":
                return new String[]{"vosk-model-es-0.42", DEFAULT_VOSK_MODEL}; // 1.4G
            case "pt":
                return new String[]{"vosk-model-pt-fb-v0.1.1-20220516_2113", DEFAULT_VOSK_MODEL}; // 1.6G
            case "ru":
                return new String[]{"vosk-model-ru-0.42", DEFAULT_VOSK_MODEL}; // 1.8G
            case "uk":
                return new String[]{"vosk-model-uk-v3", DEFAULT_VOSK_MODEL}; // 343M, biggest ukrainian there is
            case "pl":
                // no big polish model exists
                return new String[]{"vosk-model-small-pl-0.22", DEFAULT_VOSK_MODEL};
            case "it":
                return new String[]{"vosk-model-it-0.22", DEFAULT_VOSK_MODEL}; // 1.2G
            case "nl":
                return new String[]{"vosk-model-nl-spraakherkenning-0.6", DEFAULT_VOSK_MODEL}; // 860M
            case "tr":
                // no big turkish model exists
                return new String[]{"vosk-model-small-tr-0.3", DEFAULT_VOSK_MODEL};
            case "ja":
                return new String[]{"vosk-model-ja-0.22", DEFAULT_VOSK_MODEL}; // 1G
            case "zh":
                return new String[]{"vosk-model-cn-0.22", DEFAULT_VOSK_MODEL}; // 1.3G
            case "ko":
                // no big korean model exists
                return new String[]{"vosk-model-small-ko-0.22", DEFAULT_VOSK_MODEL};
            case "en":
            default:
                return new String[]{DEFAULT_VOSK_MODEL}; // gigaspeech, 2.3G
        }
    }

    private String resolveLanguageCode() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getLanguageManager() != null) {
                String code = client.getLanguageManager().getLanguage();
                if (code != null && !code.isBlank()) {
                    return code.toLowerCase(Locale.ROOT);
                }
            }
        } catch (Throwable ignored) {
            // fall through to system locale.
        }

        String fallback = Locale.getDefault().toLanguageTag().toLowerCase(Locale.ROOT);
        return fallback.replace('-', '_');
    }


    /**
     * queues the privacy screen to show on the next client tick.
     * thread-safe - can be called from server-side code.
     */
    public static void triggerPrivacyScreenTest() {
        shouldShowPrivacyScreen = true;
    }

    /**
     * queues a jumpscare flash effect for the next tick.
     */
    public static void triggerJumpscareFlash() {
        shouldTriggerJumpscareFlash = true;
    }

    /**
     * queues the fake death screen to display.
     *
     * @param duration how long to show the fake death screen (milliseconds)
     */
    public static void triggerFakeDeathScreen(long duration) {
        fakeDeathDuration = duration;
        shouldShowFakeDeathScreen = true;
    }
}
