package lol.cqllmetoxic.nullpointerentity.util;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * detects which minecraft launcher the player is using.
 * looks for specific config files and directory structures that each launcher creates.
 * useful for tailoring messages and understanding the player's setup.
 */
public class LauncherDetection {

    /**
     * enum of all supported launchers.
     * each launcher has a display name that gets shown in logs and potentially in-game.
     */
    public enum Launcher {
        ATLAUNCHER("ATLauncher"),
        CURSEFORGE("CurseForge/Overwolf"),
        GDLAUNCHER("GDLauncher"),
        MCUPDATER("MCUpdater"),
        MODRINTH("Modrinth"),
        MULTIMC("MultiMC/PolyMC/PrismLauncher"),
        TECHNIC("Technic"),
        FEATHER_LUNAR("Feather/Lunar Client"),
        VANILLA("Vanilla/Unknown");

        private final String displayName;

        Launcher(String displayName) {
            this.displayName = displayName;
        }

        /**
         * gets the human-readable name of this launcher.
         * @return display name like "Prism Launcher" or "CurseForge"
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /** cached result so we don't re-scan filesystem every time */
    private static Launcher detectedLauncher = null;
    /** tracks whether we've already run detection */
    private static boolean hasDetected = false;

    /**
     * main detection method. scans the game directory for launcher-specific files.
     * caches the result so subsequent calls are instant.
     * checks launchers in priority order (feather/lunar first, then others).
     *
     * @return the detected launcher enum value
     */
    public static Launcher detectLauncher() {
        if (hasDetected) {
            return detectedLauncher;
        }

        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            Path parentDir = gameDir.getParent();

            NullPointerEntity.LOGGER.info("Detecting launcher... Game directory: {}", gameDir);
            if (parentDir != null) {
                NullPointerEntity.LOGGER.info("Parent directory: {}", parentDir);
            }

            // check for feather client first
            if (isFeatherClient(gameDir)) {
                detectedLauncher = Launcher.FEATHER_LUNAR;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: Feather Client");
                return detectedLauncher;
            }

            // check for atlauncher (instance.json)
            if (fileExists(parentDir, "instance.json") && isATLauncherInstance(parentDir)) {
                detectedLauncher = Launcher.ATLAUNCHER;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: ATLauncher");
                return detectedLauncher;
            }

            // check for curseforge/overwolf (manifest.json or minecraftinstance.json)
            if (fileExists(gameDir, "manifest.json") || fileExists(gameDir, "minecraftinstance.json")) {
                detectedLauncher = Launcher.CURSEFORGE;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: CurseForge/Overwolf");
                return detectedLauncher;
            }

            // check for gdlauncher (manifest.json with gdlauncher signature)
            if (fileExists(gameDir, "manifest.json") && isGDLauncherInstance(gameDir)) {
                detectedLauncher = Launcher.GDLAUNCHER;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: GDLauncher");
                return detectedLauncher;
            }

            // check for mcupdater (instance.json in parent directory)
            if (fileExists(parentDir, "instance.json") && isMCUpdaterInstance(parentDir)) {
                detectedLauncher = Launcher.MCUPDATER;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: MCUpdater");
                return detectedLauncher;
            }

            // check for modrinth (profile.json, modrinth.index.json, or .mrpack markers)
            if (isModrinthInstance(gameDir, parentDir)) {
                detectedLauncher = Launcher.MODRINTH;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: Modrinth");
                return detectedLauncher;
            }

            // check for multimc/polymc/prismlauncher (instance.cfg)
            if (fileExists(parentDir, "instance.cfg")) {
                detectedLauncher = Launcher.MULTIMC;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: MultiMC/PolyMC/PrismLauncher");
                return detectedLauncher;
            }

            // check for technic (installedpacks)
            if (fileExists(parentDir, "installedPacks") || fileExists(parentDir.getParent(), "installedPacks")) {
                detectedLauncher = Launcher.TECHNIC;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: Technic");
                return detectedLauncher;
            }

            // check for lunar client (lunarclient)
            if (fileExists(gameDir, "lunarclient") || fileExists(gameDir.getParent(), "lunarclient")) {
                detectedLauncher = Launcher.FEATHER_LUNAR;
                hasDetected = true;
                NullPointerEntity.LOGGER.info("Detected launcher: Lunar Client");
                return detectedLauncher;
            }

            // default to vanilla/unknown
            detectedLauncher = Launcher.VANILLA;
            hasDetected = true;
            NullPointerEntity.LOGGER.info("Could not detect specific launcher, defaulting to Vanilla/Unknown");
            return detectedLauncher;

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Error detecting launcher", e);
            detectedLauncher = Launcher.VANILLA;
            hasDetected = true;
            return detectedLauncher;
        }
    }

    /**
     * returns the cached launcher detection result.
     * runs detection first time it's called.
     *
     * @return the launcher enum
     */
    public static Launcher getLauncher() {
        if (!hasDetected) {
            return detectLauncher();
        }
        return detectedLauncher;
    }

    /**
     * gets the human-friendly name of whatever launcher you're using.
     *
     * @return display name like "Prism Launcher" or "Modrinth"
     */
    public static String getLauncherName() {
        return getLauncher().getDisplayName();
    }

    /**
     * tests if you're using a specific launcher.
     *
     * @param launcher the launcher to check for
     * @return true if currently using that launcher
     */
    public static boolean isLauncher(Launcher launcher) {
        return getLauncher() == launcher;
    }

    /**
     * clears the detection cache. mainly for testing purposes.
     * forces a fresh detection run next time getLauncher() is called.
     */
    public static void reset() {
        hasDetected = false;
        detectedLauncher = null;
    }

    /**
     * checks if a file exists in the given directory.
     * logs the file path if found for debugging.
     *
     * @param directory the directory to check in
     * @param fileName name of the file to look for
     * @return true if file exists, false otherwise
     */
    private static boolean fileExists(Path directory, String fileName) {
        if (directory == null) {
            return false;
        }
        Path filePath = directory.resolve(fileName);
        boolean exists = Files.exists(filePath);
        if (exists) {
            NullPointerEntity.LOGGER.debug("Found file: {}", filePath);
        }
        return exists;
    }

    /**
     * looks for feather client specific markers.
     * checks for feather directory in game dir or parent dir, plus system properties.
     *
     * @param gameDir the minecraft directory
     * @return true if feather client detected
     */
    private static boolean isFeatherClient(Path gameDir) {
        return fileExists(gameDir, "feather") ||
               fileExists(gameDir.getParent(), "feather") ||
               System.getProperty("feather.version") != null;
    }

    /**
     * reads ATLauncher's instance.json and checks for their specific format.
     * ATLauncher includes a launcher field with their name in it.
     *
     * @param directory directory to check
     * @return true if instance.json has ATLauncher signature
     */
    private static boolean isATLauncherInstance(Path directory) {
        try {
            Path instanceFile = directory.resolve("instance.json");
            if (Files.exists(instanceFile)) {
                String content = Files.readString(instanceFile);
                return content.contains("\"launcher\":") && content.contains("\"ATLauncher\"");
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Error checking ATLauncher instance", e);
        }
        return false;
    }

    /**
     * scans manifest.json for GDLauncher identifiers.
     * GDLauncher puts their name in the manifest file.
     *
     * @param directory directory to check
     * @return true if manifest contains gdlauncher markers
     */
    private static boolean isGDLauncherInstance(Path directory) {
        try {
            Path manifestFile = directory.resolve("manifest.json");
            if (Files.exists(manifestFile)) {
                String content = Files.readString(manifestFile);
                return content.contains("gdlauncher") || content.contains("GDLauncher");
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Error checking GDLauncher instance", e);
        }
        return false;
    }

    /**
     * checks instance.json for MCUpdater specific structure.
     * MCUpdater includes their name in the json config.
     *
     * @param directory directory to check
     * @return true if instance.json matches MCUpdater format
     */
    private static boolean isMCUpdaterInstance(Path directory) {
        try {
            Path instanceFile = directory.resolve("instance.json");
            if (Files.exists(instanceFile)) {
                String content = Files.readString(instanceFile);
                return content.contains("MCUpdater") || content.contains("mcupdater");
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Error checking MCUpdater instance", e);
        }
        return false;
    }

    /**
     * looks for modrinth launcher markers in various locations.
     * modrinth uses profile.json, modrinth.index.json, or .mrpack files.
     * checks game dir, parent dir, and several subdirectories.
     *
     * @param gameDir the minecraft directory
     * @param parentDir parent of the minecraft directory
     * @return true if modrinth markers found
     */
    private static boolean isModrinthInstance(Path gameDir, Path parentDir) {
        try {
            // scan game directory first
            if (fileExists(gameDir, "profile.json") ||
                fileExists(gameDir, "modrinth.index.json") ||
                fileExists(gameDir, ".mrpack")) {
                return true;
            }

            // check in parent directory and its subdirectories
            if (parentDir != null) {
                if (fileExists(parentDir, "profile.json") ||
                    fileExists(parentDir, "modrinth.index.json") ||
                    fileExists(parentDir, ".mrpack")) {
                    return true;
                }

                // check in the ".minecraft" directory in the parent directory
                Path minecraftDir = parentDir.resolve(".minecraft");
                if (Files.exists(minecraftDir) && Files.isDirectory(minecraftDir)) {
                    if (fileExists(minecraftDir, "profile.json") ||
                        fileExists(minecraftDir, "modrinth.index.json") ||
                        fileExists(minecraftDir, ".mrpack")) {
                        return true;
                    }
                }

                // check for modrinth app specific paths - profiles directory
                Path profilesDir = parentDir.resolve("profiles");
                if (Files.exists(profilesDir) && Files.isDirectory(profilesDir)) {
                    return true;
                }

                // check grandparent for modrinth app structure
                Path grandParentDir = parentDir.getParent();
                if (grandParentDir != null) {
                    if (fileExists(grandParentDir, "profile.json")) {
                        return true;
                    }
                    Path modrinthProfiles = grandParentDir.resolve("profiles");
                    if (Files.exists(modrinthProfiles) && Files.isDirectory(modrinthProfiles)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Error checking Modrinth instance", e);
        }
        return false;
    }
}
