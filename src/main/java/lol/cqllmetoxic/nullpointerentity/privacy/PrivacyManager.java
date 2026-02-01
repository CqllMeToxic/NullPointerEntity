package lol.cqllmetoxic.nullpointerentity.privacy;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

/**
 * handles privacy configuration and data obfuscation throughout the mod.
 * stores user preferences for showing real vs randomized system information.
 * tracks which worlds have been processed to avoid showing privacy screen multiple times.
 */
public class PrivacyManager {
    private static final String CONFIG_FILE = "nullpointerentity_privacy.properties";
    /** when true, shows randomized data instead of real system info */
    private static boolean privacyEnabled = true;
    private static boolean firstTimeUser = true;
    private static final Random random = new Random();
    private static boolean configLoaded = false;
    private static Set<String> processedWorlds = new HashSet<>();
    /** stores the user's selected microphone name for audio recording */
    private static String selectedMicrophoneName = null;

    /**
     * reads the config file from disk and loads all privacy settings.
     * also loads the list of worlds that have already been processed.
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
                    privacyEnabled = Boolean.parseBoolean(props.getProperty("privacy_enabled", "true"));
                    firstTimeUser = Boolean.parseBoolean(props.getProperty("first_time_user", "true"));
                    selectedMicrophoneName = props.getProperty("selected_microphone", null);

                    /** grab the comma-separated list of world names */
                    String worldsString = props.getProperty("processed_worlds", "");
                    if (!worldsString.isEmpty()) {
                        String[] worlds = worldsString.split(",");
                        for (String world : worlds) {
                            processedWorlds.add(world.trim());
                        }
                    }
                }
            }
            configLoaded = true;
        } catch (IOException e) {
            System.err.println("Failed to load privacy config: " + e.getMessage());
            /** mark as loaded anyway so we don't spam errors */
            configLoaded = true;
        }
    }

    /**
     * writes current privacy settings to disk.
     * creates the config directory if it doesn't exist yet.
     * server-side compatible, doesn't rely on client-only APIs.
     */
    public static void saveConfig() {
        try {
            Path configDir = Paths.get("config");
            configDir.toFile().mkdirs();
            File configFile = configDir.resolve(CONFIG_FILE).toFile();
            
            Properties props = new Properties();
            props.setProperty("privacy_enabled", String.valueOf(privacyEnabled));
            props.setProperty("first_time_user", String.valueOf(firstTimeUser));
            if (selectedMicrophoneName != null) {
                props.setProperty("selected_microphone", selectedMicrophoneName);
            }

            /** build comma-separated string of all processed world names */
            StringBuilder worldsBuilder = new StringBuilder();
            for (String world : processedWorlds) {
                if (worldsBuilder.length() > 0) {
                    worldsBuilder.append(",");
                }
                worldsBuilder.append(world);
            }
            props.setProperty("processed_worlds", worldsBuilder.toString());

            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "NullPointerEntity Privacy Configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to save privacy config: " + e.getMessage());
        }
    }

    /**
     * checks current privacy setting.
     * @return true if privacy mode is on (randomized data), false for real data
     */
    public static boolean isPrivacyEnabled() {
        loadConfig();
        return privacyEnabled;
    }

    /**
     * updates the privacy setting and saves to disk.
     * @param enabled true to enable privacy mode, false to show real data
     */
    public static void setPrivacyEnabled(boolean enabled) {
        loadConfig();
        privacyEnabled = enabled;
        saveConfig();
    }

    /**
     * checks if this is the player's first time using the mod.
     * used to determine whether to show the initial privacy configuration screen.
     * @return true if first time, false if they've seen the privacy screen before
     */
    public static boolean isFirstTimeUser() {
        loadConfig();
        return firstTimeUser;
    }

    /**
     * marks whether this is the player's first time using the mod.
     * @param firstTime true if first time, false otherwise
     */
    public static void setFirstTimeUser(boolean firstTime) {
        loadConfig();
        firstTimeUser = firstTime;
        saveConfig();
    }

    /**
     * returns the username to display in messages and files.
     * if privacy mode is on, generates a consistent hash-based anonymized name.
     * if privacy is off, returns the actual system username.
     *
     * @param realUsername the actual windows username from System.getProperty
     * @return anonymized or real username depending on privacy settings
     */
    public static String getSystemUsername(String realUsername) {
        loadConfig();
        if (privacyEnabled) {
            /** use hashcode for consistent anonymization - same input always gives same output */
            return "User" + Math.abs(realUsername.hashCode() % 10000);
        }
        return realUsername;
    }

    /**
     * processes usernames for display in various events and messages.
     * when privacy is on, picks from a pool of generic usernames randomly.
     * when privacy is off, just returns the original username unchanged.
     *
     * @param originalUsername the real username to potentially obfuscate
     * @return processed username string
     */
    public static String processUsername(String originalUsername) {
        if (!isPrivacyEnabled()) {
            return originalUsername;
        }
        
        /** pick a random generic username from the pool */
        String[] randomUsernames = {
            "User" + random.nextInt(1000),
            "Player" + random.nextInt(999),
            "Guest" + random.nextInt(9999),
            "Anonymous" + random.nextInt(99),
            "Unknown" + random.nextInt(999),
            "Subject" + random.nextInt(999)
        };
        return randomUsernames[random.nextInt(randomUsernames.length)];
    }

    /**
     * obfuscates system information like computer name or hardware specs.
     * returns generic placeholders when privacy mode is enabled.
     *
     * @param originalInfo the real system info string
     * @return either real or randomized system info
     */
    public static String processSystemInfo(String originalInfo) {
        if (!isPrivacyEnabled()) {
            return originalInfo;
        }
        
        /** generate a random generic system name */
        String[] randomSystems = {
            "GenericPC-" + random.nextInt(999),
            "Computer-" + random.nextInt(99999),
            "System-" + random.nextInt(9999),
            "Device-" + random.nextInt(99999),
            "Terminal-" + random.nextInt(999)
        };
        return randomSystems[random.nextInt(randomSystems.length)];
    }

    /**
     * masks location data like city, region, or GPS coordinates.
     * replaces with generic placeholders if privacy is enabled.
     *
     * @param originalLocation the real location string
     * @return real or obfuscated location data
     */
    public static String processLocation(String originalLocation) {
        if (!isPrivacyEnabled()) {
            return originalLocation;
        }
        
        /** swap in a generic location string */
        String[] randomLocations = {
            "Unknown Location",
            "Randomized Coordinates",
            "Protected Location",
            "Anonymous Region",
            "Privacy Zone " + random.nextInt(99),
            "Secure Location " + random.nextInt(999)
        };
        return randomLocations[random.nextInt(randomLocations.length)];
    }

    /**
     * hides browser type and version information when privacy mode is on.
     * useful for masking what browser the player is actually using.
     *
     * @param originalData the real browser info
     * @return real or generic browser identifier
     */
    public static String processBrowserData(String originalData) {
        if (!isPrivacyEnabled()) {
            return originalData;
        }
        
        /** pick from pool of generic browser names */
        String[] randomBrowsers = {
            "Generic Browser",
            "Unknown Browser v" + (random.nextInt(10) + 1) + ".0",
            "Private Browser",
            "Anonymous Client",
            "Secure Browser " + random.nextInt(999)
        };
        return randomBrowsers[random.nextInt(randomBrowsers.length)];
    }

    /**
     * anonymizes IP addresses when privacy mode is active.
     * replaces with localhost range IPs to prevent leaking real network info.
     *
     * @param originalIP the player's actual IP address
     * @return real or fake IP depending on settings
     */
    public static String processIPAddress(String originalIP) {
        if (!isPrivacyEnabled()) {
            return originalIP;
        }

        /** just use localhost range with randomized last numbers idk if it has a name lol */
        return "127.0.0." + random.nextInt(255);
    }

    /**
     * scans event messages and strips out personal info when privacy is on.
     * replaces usernames, file paths, and URLs with generic placeholders.
     * helps prevent accidentally leaking data through chat or UI messages.
     *
     * @param message the original message text
     * @param playerName the minecraft player name (currently unused)
     * @param windowsUsername the windows user to potentially strip out
     * @return sanitized message string
     */
    public static String processEventMessage(String message, String playerName, String windowsUsername) {
        if (isPrivacyEnabled()) {
            return message;
        }
        
        /** find and replace username if present */
        String processedMessage = message;
        
        if (windowsUsername != null && !windowsUsername.isEmpty()) {
            String processedUsername = processUsername(windowsUsername);
            processedMessage = processedMessage.replace(windowsUsername, processedUsername);
        }
        
        /** regex patterns to catch file paths and URLs */
        processedMessage = processedMessage.replaceAll("\\b[A-Za-z]:\\\\[^\\s]+", "C:\\\\ProtectedPath\\\\");
        processedMessage = processedMessage.replaceAll("\\bhttps?://[^\\s]+", "https://protected-site.example");
        
        return processedMessage;
    }

    /**
     * marks a world as having shown the privacy screen already.
     * prevents the screen from appearing again when loading that world.
     *
     * @param worldName the name/ID of the minecraft world
     */
    public static void markWorldAsProcessed(String worldName) {
        loadConfig();
        processedWorlds.add(worldName);
        saveConfig();
    }

    /**
     * checks if a world has already had the privacy screen shown.
     * used to determine whether to show the screen again.
     *
     * @param worldName the world name/ID to check
     * @return true if this world has been processed before
     */
    public static boolean hasWorldBeenProcessed(String worldName) {
        loadConfig();
        return processedWorlds.contains(worldName);
    }

    /**
     * alias method for hasWorldBeenProcessed.
     * exists because privacyeventhandler was calling this name.
     *
     * @param worldName the world to check
     * @return whether the world was processed
     */
    public static boolean isWorldProcessed(String worldName) {
        return hasWorldBeenProcessed(worldName);
    }

    /**
     * determines if the privacy screen should pop up for a given world.
     * first-time users always see it. returning users only see it for new worlds.
     * this prevents spam but still allows configuration per-world.
     *
     * @param worldName the world that's being loaded
     * @return true if we should show the privacy config screen
     */
    public static boolean shouldShowPrivacyScreen(String worldName) {
        loadConfig();

        /** first launch ever - definitely show the screen */
        if (isFirstTimeUser()) {
            return true;
        }

        /** returning user - only show for worlds they haven't configured yet */
        return !hasWorldBeenProcessed(worldName);
    }

    /**
     * gets the user's selected microphone name.
     *
     * @return microphone name or null if not set
     */
    public static String getSelectedMicrophoneName() {
        loadConfig();
        return selectedMicrophoneName;
    }

    /**
     * sets the user's selected microphone and saves to config.
     *
     * @param microphoneName the name of the selected microphone
     */
    public static void setSelectedMicrophone(String microphoneName) {
        selectedMicrophoneName = microphoneName;
        saveConfig();
    }

    /**
     * gets the selected microphone's Mixer.Info object for recording.
     *
     * @return Mixer.Info or null if not found/set
     */
    public static javax.sound.sampled.Mixer.Info getSelectedMicrophoneInfo() {
        if (selectedMicrophoneName == null) {
            return null;
        }

        try {
            javax.sound.sampled.Mixer.Info[] available = lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.getAvailableMicrophones();
            for (javax.sound.sampled.Mixer.Info info : available) {
                if (info.getName().equals(selectedMicrophoneName)) {
                    return info;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get selected microphone: " + e.getMessage());
        }

        return null;
    }
}
