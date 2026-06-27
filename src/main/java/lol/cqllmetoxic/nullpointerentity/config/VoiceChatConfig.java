package lol.cqllmetoxic.nullpointerentity.config;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * handles voice chat and Shriek integration configuration.
 * stores user preferences for enabling/disabling speech recognition.
 */
public class VoiceChatConfig {
    private static final String CONFIG_FILE = "nullpointerentity_voicechat.properties";
    private static boolean voiceChatEnabled = true;
    // when true, AURORA only "hears" the mic while the player holds Shriek's voice keybind
    // (Options -> Controls). when false, the mic is always listening (Shriek's default).
    private static boolean pushToTalkEnabled = true;
    private static boolean configLoaded = false;

    /**
     * reads the config file from disk and loads all voice chat settings.
     * won't reload if config has already been loaded once.
     */
    public static void loadConfig() {
        if (configLoaded) return;

        try {
            Path configDir = Paths.get("config");
            File configFile = configDir.resolve(CONFIG_FILE).toFile();

            if (configFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                    voiceChatEnabled = Boolean.parseBoolean(props.getProperty("voice_chat_enabled", "true"));
                    pushToTalkEnabled = Boolean.parseBoolean(props.getProperty("push_to_talk_enabled", "true"));
                }
            }
            configLoaded = true;
        } catch (IOException e) {
            NullPointerEntity.LOGGER.warn("Failed to load voice chat config: {}", e.getMessage());
            configLoaded = true;
        }
    }

    /**
     * writes current voice chat settings to disk.
     * creates the config directory if it doesn't exist yet.
     */
    public static void saveConfig() {
        try {
            Path configDir = Paths.get("config");
            configDir.toFile().mkdirs();
            File configFile = configDir.resolve(CONFIG_FILE).toFile();

            Properties props = new Properties();
            props.setProperty("voice_chat_enabled", String.valueOf(voiceChatEnabled));
            props.setProperty("push_to_talk_enabled", String.valueOf(pushToTalkEnabled));

            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "NullPointerEntity Voice Chat Configuration");
            }
        } catch (IOException e) {
            NullPointerEntity.LOGGER.warn("Failed to save voice chat config: {}", e.getMessage());
        }
    }


    /**
     * checks if voice chat integration is enabled.
     * @return true if voice chat is enabled, false otherwise
     */
    public static boolean isVoiceChatEnabled() {
        loadConfig();
        return voiceChatEnabled;
    }

    /**
     * updates the voice chat setting and saves to disk.
     * @param enabled true to enable voice chat, false to disable
     */
    public static void setVoiceChatEnabled(boolean enabled) {
        loadConfig();
        voiceChatEnabled = enabled;
        saveConfig();
    }

    /**
     * checks if push-to-talk is enabled. when on, AURORA only hears the mic while
     * the player holds Shriek's voice keybind (rebindable in Options -> Controls).
     * @return true if push-to-talk is enabled, false for always-listening
     */
    public static boolean isPushToTalkEnabled() {
        loadConfig();
        return pushToTalkEnabled;
    }

    /**
     * updates the push-to-talk setting and saves to disk.
     * @param enabled true to require holding the voice key, false to always listen
     */
    public static void setPushToTalkEnabled(boolean enabled) {
        loadConfig();
        pushToTalkEnabled = enabled;
        saveConfig();
    }
}

