package lol.cqllmetoxic.nullpointerentity.privacy;

import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Random;

/**
 * manages privacy configuration persistence.
 * loads and saves privacy settings to a properties file in the config directory.
 */
public class PrivacyConfig {
    private static final String CONFIG_FILE = "nullpointerentity_privacy.properties";
    private static boolean privacyEnabled = false;
    private static boolean firstTimeUser = true;
    private static final Random random = new Random();

    /**
     * loads privacy configuration from disk.
     */
    public static void load() {
        try {
            Path configDir = MinecraftClient.getInstance().runDirectory.toPath().resolve("config");
            File configFile = configDir.resolve(CONFIG_FILE).toFile();
            
            if (configFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                    privacyEnabled = Boolean.parseBoolean(props.getProperty("privacy_enabled", "false"));
                    firstTimeUser = Boolean.parseBoolean(props.getProperty("first_time_user", "true"));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load privacy config: " + e.getMessage());
        }
    }

    // save configuration to file
    public static void save() {
        try {
            Path configDir = MinecraftClient.getInstance().runDirectory.toPath().resolve("config");
            configDir.toFile().mkdirs();
            File configFile = configDir.resolve(CONFIG_FILE).toFile();
            
            Properties props = new Properties();
            props.setProperty("privacy_enabled", String.valueOf(privacyEnabled));
            props.setProperty("first_time_user", String.valueOf(firstTimeUser));
            
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "NullPointerEntity Privacy Configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to save privacy config: " + e.getMessage());
        }
    }

    public static boolean isPrivacyEnabled() {
        return privacyEnabled;
    }

    public static void setPrivacyEnabled(boolean enabled) {
        privacyEnabled = enabled;
        save();
    }

    public static boolean isFirstTimeUser() {
        return firstTimeUser;
    }

    public static void setFirstTimeUser(boolean firstTime) {
        firstTimeUser = firstTime;
        save();
    }

    // randomize personally identifiable information if privacy is disabled
    public static String randomizeUsername(String originalUsername) {
        if (privacyEnabled) {
            return originalUsername;
        }
        
        String[] randomUsernames = {
            "User" + random.nextInt(1000),
            "Player" + random.nextInt(999),
            "Guest" + random.nextInt(9999),
            "Anonymous" + random.nextInt(99),
            "Unknown" + random.nextInt(999),
            "Entity" + random.nextInt(9999),
            "Subject" + random.nextInt(999)
        };
        return randomUsernames[random.nextInt(randomUsernames.length)];
    }

    public static String randomizeSystemInfo(String originalInfo) {
        if (privacyEnabled) {
            return originalInfo;
        }
        
        // replace system information with randomized equivalents
        String[] randomSystems = {
            "GenericPC-" + random.nextInt(999),
            "Computer-" + random.nextInt(99999),
            "System-" + random.nextInt(9999),
            "Device-" + random.nextInt(99999),
            "Terminal-" + random.nextInt(999)
        };
        return randomSystems[random.nextInt(randomSystems.length)];
    }

    public static String randomizeLocation(String originalLocation) {
        if (privacyEnabled) {
            return originalLocation;
        }
        
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

    public static String randomizeBrowserData(String originalData) {
        if (privacyEnabled) {
            return originalData;
        }
        
        String[] randomBrowserData = {
            "Generic Browser Activity",
            "Randomized Web Data",
            "Protected Browsing History",
            "Anonymous Web Activity",
            "Privacy-Protected Data",
            "Scrambled Browser Information"
        };
        return randomBrowserData[random.nextInt(randomBrowserData.length)];
    }
}
