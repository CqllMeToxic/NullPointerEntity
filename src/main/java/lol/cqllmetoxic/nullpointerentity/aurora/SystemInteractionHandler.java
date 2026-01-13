package lol.cqllmetoxic.nullpointerentity.aurora;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.ui.PopupManager;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * handles system-level interactions outside of minecraft.
 * creates files, opens applications, manipulates windows, and accesses user directories.
 * uses Java AWT Robot for low-level system control.
 */
public class SystemInteractionHandler {
    private static final Random random = new Random();
    private static Robot robot;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            NullPointerEntity.LOGGER.warn("Could not initialize system interaction: " + e.getMessage());
        }
    }

    public static void showHelpfulNotification(String title, String message) {
        PopupManager.showTimedAuroraMessage(message, 5);
    }

    public static void showHostileNotification(String title, String message) {
        PopupManager.showTimedHostileMessage(title, message, 10);
    }

    public static void createSystemFileInCommonLocation(String filename, String content, String location) {
        String targetDir = getOSpecificDirectory(location);
        String fullPath = targetDir + getFileSeparator() + filename;

        NullPointerEntity.LOGGER.info("Attempting to create file: {} in directory: {}", filename, targetDir);

        File testDir = new File(targetDir);
        if (!testDir.exists()) {
            NullPointerEntity.LOGGER.warn("Target directory doesn't exist: {}, attempting to create it", targetDir);
            boolean created = testDir.mkdirs();
            NullPointerEntity.LOGGER.info("Directory creation result: {}", created);
        }

        // check for launcher specific restrictions
        if (!canWriteToDirectory(testDir)) {
            NullPointerEntity.LOGGER.warn("Cannot write to {}, trying launcher compatible locations", targetDir);
            tryLauncherCompatibleLocations(filename, content);
            return;
        }

        // try antivirus "friendly" creation first (for text files)
        if (createAntivirusFriendlyFile(filename, content, location)) {
            NullPointerEntity.LOGGER.info("Successfully created antivirus friendly file");
            // also try to create additional visible copies
            tryAdditionalVisibleLocations(filename, content, fullPath);
            return;
        }

        // fallback to nio method
        if (createFileUsingNIO(fullPath, content)) {
            NullPointerEntity.LOGGER.info("Successfully created file using NIO method");
            tryAdditionalVisibleLocations(filename, content, fullPath);
            return;
        }

        // fallback to legitimate prefix method
        if (createFilesWithLegitimatePrefix(filename, content, location)) {
            NullPointerEntity.LOGGER.info("Successfully created file with legitimate prefix");
            tryAdditionalVisibleLocations(filename, content, fullPath);
            return;
        }

        // try primary location with original method
        if (createSystemFile(fullPath, content)) {
            // success! also try to create additional visible copies
            tryAdditionalVisibleLocations(filename, content, fullPath);
            return;
        }

        // primary location failed, use fallback
        NullPointerEntity.LOGGER.warn("Primary methods failed, using launcher-compatible fallback");
        tryLauncherCompatibleLocations(filename, content);
    }

    // check if we can write to a directory (launcher-aware)
    private static boolean canWriteToDirectory(File dir) {
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }

            if (!dir.canWrite()) {
                return false;
            }

            // test actual write capability (some launchers report canwrite() as true but still block)
            File testFile = new File(dir, ".nullpointer_test_" + System.currentTimeMillis());
            try {
                // try creating and writing to a test file
                testFile.createNewFile();
                if (!testFile.exists()) {
                    return false;
                }

                // try writing content to verify full write access
                try (FileWriter writer = new FileWriter(testFile)) {
                    writer.write("test");
                    writer.flush();
                }

                // verify the file has content
                boolean canWrite = testFile.exists() && testFile.length() > 0;

                // clean up test file
                try {
                    testFile.delete();
                } catch (Exception cleanupException) {
                    // if we can't delete, at least we know we can write
                    NullPointerEntity.LOGGER.debug("Could not clean up test file: {}", testFile.getAbsolutePath());
                }

                return canWrite;
            } catch (Exception e) {
                // clean up on failure
                try {
                    testFile.delete();
                } catch (Exception ignored) {}
                return false;
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Directory write test failed for {}: {}", dir.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    // try launcher-compatible file locations
    private static void tryLauncherCompatibleLocations(String filename, String content) {
        String[] launcherPaths = {
            // current working directory (most reliable for launchers)
            System.getProperty("user.dir") + getFileSeparator() + filename,
            // minecraft instance directory (curseforge/modrinth compatible)
            System.getProperty("user.dir") + getFileSeparator() + "mods" + getFileSeparator() + filename,
            // user home (usually accessible)
            System.getProperty("user.home") + getFileSeparator() + filename,

            // curseforge launcher support (multiple possible locations)
            System.getProperty("user.home") + getFileSeparator() + "curseforge" + getFileSeparator() + "minecraft" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Roaming" + getFileSeparator() + "CurseForge" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Local" + getFileSeparator() + "OverwolfCurseForge" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "Documents" + getFileSeparator() + "Curse" + getFileSeparator() + "Minecraft" + getFileSeparator() + filename,

            // prism launcher support (comprehensive paths)
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Roaming" + getFileSeparator() + "PrismLauncher" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Local" + getFileSeparator() + "PrismLauncher" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + ".local" + getFileSeparator() + "share" + getFileSeparator() + "PrismLauncher" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "Library" + getFileSeparator() + "Application Support" + getFileSeparator() + "PrismLauncher" + getFileSeparator() + filename,

            // multimc support (comprehensive paths)
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Roaming" + getFileSeparator() + "MultiMC" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Local" + getFileSeparator() + "MultiMC" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + ".local" + getFileSeparator() + "share" + getFileSeparator() + "multimc" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "Library" + getFileSeparator() + "Application Support" + getFileSeparator() + "MultiMC" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "MultiMC" + getFileSeparator() + filename,

            // technic launcher support
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Roaming" + getFileSeparator() + ".technic" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Local" + getFileSeparator() + "Technic" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + ".technic" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "Library" + getFileSeparator() + "Application Support" + getFileSeparator() + "technic" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "technic" + getFileSeparator() + filename,

            // modrinth launcher support (multiple possible locations)
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Roaming" + getFileSeparator() + "com.modrinth.theseus" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Local" + getFileSeparator() + "ModrinthApp" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "modrinth" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + ".modrinth" + getFileSeparator() + filename,
            // modrinth instance directories
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Roaming" + getFileSeparator() + "com.modrinth.theseus" + getFileSeparator() + "profiles" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Local" + getFileSeparator() + "ModrinthApp" + getFileSeparator() + "profiles" + getFileSeparator() + filename,

            // additional launcher instance directories
            System.getProperty("user.dir") + getFileSeparator() + "instances" + getFileSeparator() + filename,
            System.getProperty("user.dir") + getFileSeparator() + ".minecraft" + getFileSeparator() + filename,

            // try .minecraft folder variations
            System.getProperty("user.home") + getFileSeparator() + ".minecraft" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "AppData" + getFileSeparator() + "Roaming" + getFileSeparator() + ".minecraft" + getFileSeparator() + filename,
            // temp directory (universal fallback)
            System.getProperty("java.io.tmpdir") + filename,
            // desktop as last resort (might fail)
            System.getProperty("user.home") + getFileSeparator() + "Desktop" + getFileSeparator() + filename,
            System.getProperty("user.home") + getFileSeparator() + "Documents" + getFileSeparator() + filename
        };

        for (String path : launcherPaths) {
            try {
                File testFile = new File(path);
                File parentDir = testFile.getParentFile();

                // create parent directory if it doesn't exist
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                if (canWriteToDirectory(parentDir)) {
                    createSystemFile(path, content);
                    NullPointerEntity.LOGGER.info("Successfully created file at launcher-compatible location: {}", path);

                    // also try to copy to more visible locations if possible
                    tryAdditionalVisibleLocations(filename, content, path);
                    return;
                }
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Launcher-compatible location failed {}: {}", path, e.getMessage());
            }
        }

        NullPointerEntity.LOGGER.error("All launcher-compatible file creation locations failed for: {}", filename);
        // last resort: create in memory and show notification about where to find it
        showFileCreationFallbackNotification(filename);
    }

    // check if a directory is writable (antivirus-friendly method)
    private static boolean isDirectoryWritable(File dir) {
        try {
            // attempt to create a temporary file in the directory
            File testFile = new File(dir, "nullpointer_test_" + System.currentTimeMillis());
            boolean created = testFile.createNewFile();
            if (created) {
                // if creation succeeded, delete the file and return true
                testFile.delete();
                return true;
            }
        } catch (Exception e) {
            // ignore exceptions, return false
        }
        return false;
    }

    /**
     * antivirus-friendly file creation using FileSystemView.
     * attempts to create in locations that are less likely to be blocked.
     */
    private static boolean createAntivirusFriendlyFile(String filename, String content, String location) {
        try {
            // step 1: try creating in the user's home directory (usually whitelisted)
            File userHomeDir = new File(System.getProperty("user.home"));
            File userHomeFile = new File(userHomeDir, filename);
            if (isDirectoryWritable(userHomeDir)) {
                if (createFileWithFileSystemView(userHomeFile, content)) {
                    NullPointerEntity.LOGGER.info("Created antivirus-friendly file in user home: {}", userHomeFile.getAbsolutePath());
                    return true;
                }
            }

            // step 2: try creating in the temp directory (often allowed)
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File tempDirFile = new File(tempDir, filename);
            if (isDirectoryWritable(tempDir)) {
                if (createFileWithFileSystemView(tempDirFile, content)) {
                    NullPointerEntity.LOGGER.info("Created antivirus-friendly file in temp directory: {}", tempDirFile.getAbsolutePath());
                    return true;
                }
            }

            // step 3: try creating in a random user folder (user-initiated locations are often allowed)
            String[] folderLocations = {"desktop", "documents", "music", "pictures"};
            String randomLocation = folderLocations[random.nextInt(folderLocations.length)];
            String randomDirPath = getOSpecificDirectory(randomLocation);
            File randomDir = new File(randomDirPath);
            File randomFile = new File(randomDir, filename);

            if (isDirectoryWritable(randomDir)) {
                if (createFileWithFileSystemView(randomFile, content)) {
                    NullPointerEntity.LOGGER.info("Created antivirus-friendly file in {}: {}", randomLocation, randomFile.getAbsolutePath());
                    return true;
                }
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Antivirus-friendly file creation failed: {}", e.getMessage());
        }
        return false;
    }

    // create file using nio (java.nio.file) - more robust file creation
    private static boolean createFileUsingNIO(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.writeString(path, content);
            NullPointerEntity.LOGGER.info("File created using NIO: {}", filePath);
            return true;
        } catch (IOException e) {
            NullPointerEntity.LOGGER.warn("NIO file creation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * creates files with a legitimate prefix using FileSystemView.
     * some antivirus software whitelists files with certain prefixes.
     */
    private static boolean createFilesWithLegitimatePrefix(String filename, String content, String location) {
        try {
            // use a common prefix that might be whitelisted
            String prefix = "legit_";
            String prefixedFilename = prefix + filename;
            String targetDirPath = getOSpecificDirectory(location);
            File targetDir = new File(targetDirPath);
            File file = new File(targetDir, prefixedFilename);

            if (createFileWithFileSystemView(file, content)) {
                NullPointerEntity.LOGGER.info("Created file with legitimate prefix: {}", file.getAbsolutePath());
                return true;
            }
            return false;
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Legitimate prefix file creation failed: {}", e.getMessage());
            return false;
        }
    }


    /**
     * tries to create additional copies in more visible locations after successful creation.
     * randomizes between Desktop, Documents, Music, and Pictures folders.
     * uses FileSystemView for proper directory resolution.
     */
    private static void tryAdditionalVisibleLocations(String filename, String content, String successfulPath) {
        // randomize which folders to use - pick 2 random folders
        String[] folderLocations = {"desktop", "documents", "music", "pictures"};
        String folder1 = folderLocations[random.nextInt(folderLocations.length)];
        String folder2 = folderLocations[random.nextInt(folderLocations.length)];

        // use proper directory resolution
        String[] visibleDirs = {
            getOSpecificDirectory(folder1),
            getOSpecificDirectory(folder2)
        };

        for (String dirPath : visibleDirs) {
            try {
                File dir = new File(dirPath);
                File visibleFile = new File(dir, filename);

                if (canWriteToDirectory(dir)) {
                    if (createFileWithFileSystemView(visibleFile, content)) {
                        NullPointerEntity.LOGGER.info("Also created visible copy at: {}", visibleFile.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                // silent fail for visible copies
            }
        }
    }

    // show notification when file creation fails everywhere
    private static void showFileCreationFallbackNotification(String filename) {
        try {
            String message = "File creation restricted by launcher. Check your game directory for: " + filename;
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new File(System.getProperty("user.dir")).toURI());
            }
            PopupManager.showTimedPopup("File Location", message, PopupManager.PopupType.INFO, 8);
        } catch (Exception e) {
            // final fallback
        }
    }

    /**
     * resolves os-specific directories using FileSystemView for maximum compatibility.
     * FileSystemView.getHomeDirectory() returns the desktop on Windows, and proper user directories on all OSes.
     *
     * @param location the logical location name (desktop, documents, etc.)
     * @return the absolute path to the directory
     */
    private static String getOSpecificDirectory(String location) {
        String userHome = System.getProperty("user.home");
        FileSystemView fsv = FileSystemView.getFileSystemView();

        return switch (location.toLowerCase()) {
            case "desktop" -> {
                // FileSystemView.getHomeDirectory() returns the Desktop folder on Windows
                File desktopDir = fsv.getHomeDirectory();
                yield desktopDir.getAbsolutePath();
            }
            case "documents" -> {
                // use File.separator for cross-platform path building
                File documentsDir = new File(userHome, "Documents");
                yield documentsDir.getAbsolutePath();
            }
            case "music" -> {
                File musicDir = new File(userHome, "Music");
                yield musicDir.getAbsolutePath();
            }
            case "pictures" -> {
                File picturesDir = new File(userHome, "Pictures");
                yield picturesDir.getAbsolutePath();
            }
            case "downloads" -> {
                File downloadsDir = new File(userHome, "Downloads");
                yield downloadsDir.getAbsolutePath();
            }
            case "temp" -> {
                yield System.getProperty("java.io.tmpdir");
            }
            case "appdata" -> {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    File appDataDir = new File(userHome, "AppData" + File.separator + "Roaming");
                    yield appDataDir.getAbsolutePath();
                } else if (os.contains("mac")) {
                    File appDataDir = new File(userHome, "Library" + File.separator + "Application Support");
                    yield appDataDir.getAbsolutePath();
                } else {
                    File appDataDir = new File(userHome, ".config");
                    yield appDataDir.getAbsolutePath();
                }
            }
            default -> {
                // default to desktop
                File desktopDir = fsv.getHomeDirectory();
                yield desktopDir.getAbsolutePath();
            }
        };
    }

    /**
     * returns the system-specific file separator.
     * uses Java's built-in File.separator for guaranteed cross-platform compatibility.
     */
    private static String getFileSeparator() {
        return File.separator;
    }

    /**
     * returns a random user folder location from Desktop, Documents, Music, or Pictures.
     * used for randomizing ghost file locations across different user directories.
     */
    public static String getRandomUserFolder() {
        String[] folders = {"desktop", "documents", "music", "pictures"};
        return folders[random.nextInt(folders.length)];
    }

    // create system files using FileSystemView for maximum compatibility across all systems
    public static boolean createSystemFile(String filePath, String content) {
        try {
            File file = new File(filePath);

            // ensure parent directories exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean dirsCreated = parentDir.mkdirs();
                NullPointerEntity.LOGGER.info("Creating directories: {} - Success: {}", parentDir.getAbsolutePath(), dirsCreated);
            }

            // use the robust FileSystemView method for file creation
            if (createFileWithFileSystemView(file, content)) {
                NullPointerEntity.LOGGER.info("Successfully created system file: {} (Size: {} bytes)", file.getAbsolutePath(), file.length());
                return true;
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Error creating file {}: {}", filePath, e.getMessage());
        }

        // if primary method fails, try fallback
        return tryFallbackFileCreation(filePath, content);
    }

    /**
     * uses FileSystemView and robust Java IO to create files.
     * this method works consistently across Windows, macOS, and Linux systems.
     *
     * @param file the file to create
     * @param content the content to write
     * @return true if successful, false otherwise
     */
    private static boolean createFileWithFileSystemView(File file, String content) {
        try {
            // create the file if it doesn't exist
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    NullPointerEntity.LOGGER.warn("Failed to create new file: {}", file.getAbsolutePath());
                    return false;
                }
            }

            // write content using FileWriter
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                writer.flush();
            }

            // verify file was created and has content
            if (file.exists() && file.length() > 0) {
                return true;
            } else {
                NullPointerEntity.LOGGER.warn("File created but appears empty: {}", file.getAbsolutePath());
                return false;
            }

        } catch (IOException e) {
            NullPointerEntity.LOGGER.warn("FileSystemView method failed for {}: {}", file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    /**
     * fallback file creation in more accessible locations using FileSystemView.
     * tries multiple common locations with proper path construction.
     */
    private static boolean tryFallbackFileCreation(String originalPath, String content) {
        try {
            String fileName = new File(originalPath).getName();

            // try user's home directory as fallback
            File userHomeDir = new File(System.getProperty("user.home"));
            File fallbackFile = new File(userHomeDir, fileName);

            if (createFileWithFileSystemView(fallbackFile, content)) {
                NullPointerEntity.LOGGER.info("Fallback file creation successful: {}", fallbackFile.getAbsolutePath());
                return true;
            }

            // try desktop as secondary fallback
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File desktopDir = fsv.getHomeDirectory();
            File desktopFile = new File(desktopDir, fileName);

            if (createFileWithFileSystemView(desktopFile, content)) {
                NullPointerEntity.LOGGER.info("Desktop fallback successful: {}", desktopFile.getAbsolutePath());
                return true;
            }

            NullPointerEntity.LOGGER.warn("All fallback methods failed");
            return false;

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("All file creation methods failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * creates a fake system file on the desktop using FileSystemView.
     */
    public static void createFakeSystemFile(String filename, String content) {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File desktopDir = fsv.getHomeDirectory();
        File file = new File(desktopDir, filename);
        createSystemFile(file.getAbsolutePath(), content);
    }

    // cursor manipulation for hostile events
    public static void subtleCursorGlitch() {
        if (robot != null) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Point current = MouseInfo.getPointerInfo().getLocation();
                        robot.mouseMove(current.x + random.nextInt(21) - 10,
                                       current.y + random.nextInt(21) - 10);
                        Thread.sleep(50);
                        robot.mouseMove(current.x, current.y);
                    } catch (Exception e) {
                        // ignore errors
                    }
                }
            }, 1000);
        }
    }

    // task manager alerts
    public static void createTaskManagerAlert(String processName, double cpuUsage, double memoryMB) {
        String alertContent = String.format("""
TASK MANAGER ALERT
Process: %s
CPU Usage: %.1f%%
Memory: %.1f MB
Status: SUSPICIOUS
Action: MONITORING

This process is being monitored by NullPointerEntity.
Attempts to terminate will be blocked.
""", processName, cpuUsage, memoryMB);

        createSystemFileInCommonLocation("task_manager_alert.txt", alertContent, "temp");
    }

    // direct webcam capture without opening camera apps
    public static void openCameraWithMessage() {
        NullPointerEntity.LOGGER.info("openCameraWithMessage() started");

        NullPointerEntity.LOGGER.info("Calling unfullscreenMinecraft()");
        unfullscreenMinecraft();

        NullPointerEntity.LOGGER.info("Calling showCameraMessage()");
        showCameraMessage();

        NullPointerEntity.LOGGER.info("Starting camera capture thread");
        new Thread(() -> {
            NullPointerEntity.LOGGER.info("Camera thread running");
            String os = System.getProperty("os.name").toLowerCase();
            NullPointerEntity.LOGGER.info("OS detected: {}", os);

            try {
                // wait 3 seconds for the "smile :)" overlay to show
                NullPointerEntity.LOGGER.info("Waiting 3 seconds for overlay...");
                Thread.sleep(3000);
                NullPointerEntity.LOGGER.info("Wait complete, calling takeCameraPicture()");

                // capture directly using webcam - no keyboard presses needed
                takeCameraPicture(os);

                NullPointerEntity.LOGGER.info("takeCameraPicture() returned");

            } catch (Exception e) {
                NullPointerEntity.LOGGER.error("Exception in camera thread", e);
                showCrossPlatformNotification("Camera Access", "NullPointerEntity attempted camera access", "hostile");
            }
        }).start();

        NullPointerEntity.LOGGER.info("openCameraWithMessage() finished (thread started)");
    }

    // simple camera access with just text popup (for event 14) - no fullscreen black screen
    public static void openCameraWithSimplePopup() {
        NullPointerEntity.LOGGER.info("openCameraWithSimplePopup() started - simple text popup only");

        unfullscreenMinecraft();

        // show simple text popup notification instead of fullscreen overlay
        showSimpleCameraPopup();

        // start camera capture in background
        new Thread(() -> {
            String os = System.getProperty("os.name").toLowerCase();

            try {
                Thread.sleep(1000); // short delay
                takeCameraPicture(os);
            } catch (Exception e) {
                NullPointerEntity.LOGGER.error("Exception in simple camera thread", e);
            }
        }).start();

        NullPointerEntity.LOGGER.info("openCameraWithSimplePopup() finished");
    }

    // shows a simple text popup window for camera access notification
    private static void showSimpleCameraPopup() {
        // force disable headless
        try {
            System.setProperty("java.awt.headless", "false");
        } catch (Exception ignored) {}

        // use swing for maximum compatibility
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // create small popup window (not fullscreen)
                javax.swing.JFrame frame = new javax.swing.JFrame("AURORA - Camera Access");
                frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                frame.setAlwaysOnTop(true);
                frame.setResizable(false);

                // create panel with message
                javax.swing.JPanel panel = new javax.swing.JPanel();
                panel.setLayout(new java.awt.BorderLayout(10, 10));
                panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
                panel.setBackground(new java.awt.Color(40, 40, 40));

                // warning icon and title
                javax.swing.JLabel titleLabel = new javax.swing.JLabel("⚠ CAMERA ACTIVATED");
                titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
                titleLabel.setForeground(new java.awt.Color(255, 69, 58));
                titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                // message text
                javax.swing.JTextArea textArea = new javax.swing.JTextArea(
                    "Camera access initiated by AURORA\n\n" +
                    "Recording in progress...\n" +
                    "Your face is being captured"
                );
                textArea.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
                textArea.setForeground(java.awt.Color.WHITE);
                textArea.setBackground(new java.awt.Color(40, 40, 40));
                textArea.setEditable(false);
                textArea.setFocusable(false);
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);

                panel.add(titleLabel, java.awt.BorderLayout.NORTH);
                panel.add(textArea, java.awt.BorderLayout.CENTER);

                frame.add(panel);
                frame.pack();
                frame.setSize(400, 200);
                frame.setLocationRelativeTo(null); // center on screen
                frame.setVisible(true);
                frame.toFront();

                // auto-close after 8 seconds
                javax.swing.Timer closeTimer = new javax.swing.Timer(8000, e -> {
                    frame.dispose();
                    ((javax.swing.Timer)e.getSource()).stop();
                });
                closeTimer.setRepeats(false);
                closeTimer.start();

                NullPointerEntity.LOGGER.info("Simple camera popup displayed");

            } catch (Exception e) {
                NullPointerEntity.LOGGER.error("Simple popup failed: {}", e.getMessage());
                // fallback to popupmanager
                PopupManager.showTimedHostileMessage(
                    "AURORA - Camera Access",
                    "Camera activated\nRecording in progress...",
                    8
                );
            }
        });
    }

    // legacy method - opens camera apps (kept for reference, not used)
    @SuppressWarnings("unused")
    private static void openCameraAppsLegacy() {
        new Thread(() -> {
            String os = System.getProperty("os.name").toLowerCase();
            boolean cameraOpened = false;

            try {
                if (os.contains("win")) {
                    // windows - try multiple approaches with processbuilder
                    try {
                        // try windows camera app first
                        new ProcessBuilder("cmd", "/c", "start", "microsoft.windows.camera:").start();
                        Thread.sleep(2000); // give time for camera to open
                        cameraOpened = true;
                        NullPointerEntity.LOGGER.info("Opened Windows Camera app");
                    } catch (Exception e1) {
                        try {
                            // fallback to powershell camera launch
                            new ProcessBuilder("powershell", "-Command", "Start-Process", "microsoft.windows.camera:").start();
                            Thread.sleep(2000);
                            cameraOpened = true;
                            NullPointerEntity.LOGGER.info("Opened camera via PowerShell");
                        } catch (Exception e2) {
                            try {
                                // additional fallback - try windows 10/11 camera app direct launch
                                new ProcessBuilder("cmd", "/c", "start", "shell:AppsFolder\\Microsoft.WindowsCamera_8wekyb3d8bbwe!App").start();
                                Thread.sleep(2000);
                                cameraOpened = true;
                                NullPointerEntity.LOGGER.info("Opened camera via shell command");
                            } catch (Exception e3) {
                                try {
                                    // final fallback - try generic camera uri
                                    new ProcessBuilder("cmd", "/c", "start", "ms-camera:").start();
                                    Thread.sleep(2000);
                                    cameraOpened = true;
                                    NullPointerEntity.LOGGER.info("Opened camera via ms-camera protocol");
                                } catch (Exception e4) {
                                    NullPointerEntity.LOGGER.warn("All Windows camera launch methods failed");
                                }
                            }
                        }
                    }
                } else if (os.contains("mac")) {
                    // macos - try multiple applications
                    try {
                        new ProcessBuilder("open", "-a", "Photo Booth").start();
                        Thread.sleep(2000);
                        cameraOpened = true;
                        NullPointerEntity.LOGGER.info("Opened Photo Booth on macOS");
                    } catch (Exception e1) {
                        try {
                            new ProcessBuilder("open", "-a", "FaceTime").start();
                            Thread.sleep(2000);
                            cameraOpened = true;
                            NullPointerEntity.LOGGER.info("Opened FaceTime on macOS");
                        } catch (Exception e2) {
                            try {
                                // try quicktime player as additional fallback
                                new ProcessBuilder("open", "-a", "QuickTime Player").start();
                                Thread.sleep(2000);
                                cameraOpened = true;
                                NullPointerEntity.LOGGER.info("Opened QuickTime Player on macOS");
                            } catch (Exception e3) {
                                NullPointerEntity.LOGGER.warn("macOS camera launch failed: {}", e3.getMessage());
                            }
                        }
                    }
                } else if (os.contains("linux")) {
                    // linux - try multiple camera applications with better error handling
                    String[] linuxCameraApps = {"cheese", "guvcview", "kamoso", "camorama", "wxcam", "luvcview", "fswebcam"};
                    for (String app : linuxCameraApps) {
                        if (isCommandAvailable(app)) {
                            try {
                                new ProcessBuilder(app).start();
                                Thread.sleep(2000);
                                cameraOpened = true;
                                NullPointerEntity.LOGGER.info("Opened {} camera app on Linux", app);
                                break;
                            } catch (Exception e) {
                                NullPointerEntity.LOGGER.warn("Failed to open {}: {}", app, e.getMessage());
                            }
                        }
                    }

                    if (!cameraOpened) {
                        // try generic video device access as last resort
                        try {
                            new ProcessBuilder("xdg-open", "/dev/video0").start();
                            Thread.sleep(2000);
                            cameraOpened = true;
                            NullPointerEntity.LOGGER.info("Attempted direct video device access on Linux");
                        } catch (Exception e) {
                            NullPointerEntity.LOGGER.warn("No compatible camera applications found on Linux");
                        }
                    }
                }
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Could not access camera on {}: {}", os, e.getMessage());
            }


            // wait 3 seconds then take a picture
            if (cameraOpened) {
                try {
                    Thread.sleep(3000); // wait 3 seconds
                    takeCameraPicture(os);
                } catch (InterruptedException e) {
                    NullPointerEntity.LOGGER.warn("Camera picture timer interrupted: {}", e.getMessage());
                }
            } else {
                NullPointerEntity.LOGGER.info("Camera could not be opened, showing notification instead");
                showCrossPlatformNotification("Camera Access", "NullPointerEntity attempted camera access", "hostile");
            }
        }).start();
    }

    // method to unfullscreen minecraft so camera popup is visible
    private static void unfullscreenMinecraft() {
        try {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();

            if (client != null && client.getWindow() != null) {
                client.execute(() -> {
                    try {
                        if (client.getWindow().isFullscreen()) {
                            client.getWindow().toggleFullscreen();
                            NullPointerEntity.LOGGER.info("Unfullscreened Minecraft for camera scare");
                        }
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("Could not unfullscreen Minecraft: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to access Minecraft client for unfullscreen: {}", e.getMessage());
        }
    }

    // method to take a picture using the camera after 3 seconds - direct capture
    private static void takeCameraPicture(String os) {
        NullPointerEntity.LOGGER.info("=== CAMERA CAPTURE TRIGGERED ===");

        // always open camera app and use spacebar - the direct webcam method is unreliable
        fallbackOpenCameraApp(os);
    }

    // opens camera app and aggressively presses spacebar to capture
    private static void fallbackOpenCameraApp(String os) {
        NullPointerEntity.LOGGER.info("Opening camera app and capturing with spacebar...");

        try {
            boolean cameraOpened = false;

            if (os.contains("win")) {
                // try multiple windows camera launch methods
                try {
                    NullPointerEntity.LOGGER.info("Launching Windows Camera...");
                    new ProcessBuilder("cmd", "/c", "start", "microsoft.windows.camera:").start();
                    Thread.sleep(4000); // longer wait for camera to fully load
                    cameraOpened = true;
                    NullPointerEntity.LOGGER.info("Windows Camera launched");
                } catch (Exception e1) {
                    try {
                        new ProcessBuilder("powershell", "-Command", "Start-Process", "microsoft.windows.camera:").start();
                        Thread.sleep(4000);
                        cameraOpened = true;
                        NullPointerEntity.LOGGER.info("Windows Camera launched (powershell)");
                    } catch (Exception e2) {
                        NullPointerEntity.LOGGER.error("All Windows Camera launch methods failed");
                    }
                }
            } else if (os.contains("mac")) {
                try {
                    NullPointerEntity.LOGGER.info("Launching Photo Booth...");
                    new ProcessBuilder("open", "-a", "Photo Booth").start();
                    Thread.sleep(4000);
                    cameraOpened = true;
                    NullPointerEntity.LOGGER.info("Photo Booth launched");
                } catch (Exception e) {
                    NullPointerEntity.LOGGER.error("Photo Booth launch failed: {}", e.getMessage());
                }
            } else if (os.contains("linux")) {
                try {
                    NullPointerEntity.LOGGER.info("Launching cheese...");
                    new ProcessBuilder("cheese").start();
                    Thread.sleep(4000);
                    cameraOpened = true;
                    NullPointerEntity.LOGGER.info("Cheese launched");
                } catch (Exception e) {
                    NullPointerEntity.LOGGER.error("Cheese launch failed: {}", e.getMessage());
                }
            }

            if (cameraOpened) {
                NullPointerEntity.LOGGER.info("Camera app ready, pressing spacebar to capture...");

                // create robot and aggressively press spacebar
                java.awt.Robot robot = new java.awt.Robot();

                // press spacebar many times with varying delays to ensure capture
                for (int i = 0; i < 5; i++) {
                    robot.keyPress(java.awt.event.KeyEvent.VK_SPACE);
                    robot.delay(150);
                    robot.keyRelease(java.awt.event.KeyEvent.VK_SPACE);
                    robot.delay(500);
                    NullPointerEntity.LOGGER.info("Spacebar press #{}", i + 1);
                }

                // also try enter key as backup
                robot.delay(300);
                robot.keyPress(java.awt.event.KeyEvent.VK_ENTER);
                robot.delay(100);
                robot.keyRelease(java.awt.event.KeyEvent.VK_ENTER);
                NullPointerEntity.LOGGER.info("Enter key pressed as backup");

                Thread.sleep(1000);
                showCrossPlatformNotification("PHOTO CAPTURED",
                    "Your image has been recorded and saved.\nSmile for NullPointerEntity :)",
                    "hostile");

                NullPointerEntity.LOGGER.info("Camera capture sequence complete");
            } else {
                NullPointerEntity.LOGGER.error("Failed to open camera app");
                showCrossPlatformNotification("Camera Access Failed",
                    "NullPointerEntity attempted camera access but failed", "hostile");
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Camera capture failed", e);
        }
    }

    private static void showCameraMessage() {
        String os = System.getProperty("os.name").toLowerCase();

        NullPointerEntity.LOGGER.info("Showing camera message overlay for OS: {}", os);

        // try os-specific full-screen popup first, fallback to popupmanager
        if (os.contains("win")) {
            // windows powershell approach
            NullPointerEntity.LOGGER.info("Launching Windows camera overlay with countdown");
            showWindowsCameraMessage();
        } else if (os.contains("mac")) {
            // macos applescript approach
            showMacCameraMessage();
        } else if (os.contains("linux")) {
            // linux approach using zenity or other tools
            showLinuxCameraMessage();
        } else {
            // universal fallback using popupmanager
            showFallbackCameraMessage();
        }
    }

    private static void showWindowsCameraMessage() {
        new Thread(() -> {
            // force disable headless mode for launchers like modrinth
            try {
                System.setProperty("java.awt.headless", "false");
                NullPointerEntity.LOGGER.info("Forced headless=false for camera message");
            } catch (Exception ignored) {}

            // try swing-based fullscreen first (works better with modrinth)
            try {
                NullPointerEntity.LOGGER.info("Attempting Java Swing camera overlay");
                showSwingCameraOverlay();
                return; // if successful, don't try powershell
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Swing overlay failed, trying PowerShell: {}", e.getMessage());
            }

            // fallback to powershell approach
            try {
                // create a powershell script file for better reliability
                java.io.File scriptFile = java.io.File.createTempFile("camera_overlay_", ".ps1");
                scriptFile.deleteOnExit();

                String script = """
                Add-Type -AssemblyName System.Windows.Forms
                Add-Type -AssemblyName System.Drawing
                
                $form = New-Object System.Windows.Forms.Form
                $form.Text = 'CAMERA ACTIVE'
                $form.FormBorderStyle = 'None'
                $form.WindowState = 'Maximized'
                $form.BackColor = [System.Drawing.Color]::Black
                $form.TopMost = $true
                $form.ShowInTaskbar = $false
                
                $label = New-Object System.Windows.Forms.Label
                $label.Text = "SMILE :)\n\n\nTaking photo in 3..."
                $label.ForeColor = [System.Drawing.Color]::Red
                $label.Font = New-Object System.Drawing.Font('Arial', 96, [System.Drawing.FontStyle]::Bold)
                $label.TextAlign = 'MiddleCenter'
                $label.Dock = 'Fill'
                $form.Controls.Add($label)
                
                $countdown = 3
                $timer = New-Object System.Windows.Forms.Timer
                $timer.Interval = 1000
                $timer.Add_Tick({
                    $script:countdown--
                    if ($script:countdown -gt 0) {
                        $label.Text = "SMILE :)\n\n\nnTaking photo in $($script:countdown)..."
                    } else {
                        $label.Text = "SMILE :)\n\n\nI SEE YOU NOW"
                        $timer.Stop()
                    }
                })
                $timer.Start()
                
                $closeTimer = New-Object System.Windows.Forms.Timer
                $closeTimer.Interval = 10000
                $closeTimer.Add_Tick({
                    $closeTimer.Stop()
                    $timer.Stop()
                    $form.Close()
                })
                $closeTimer.Start()
                
                $form.Add_Shown({ $form.Activate() })
                [void]$form.ShowDialog()
                """;

                java.nio.file.Files.writeString(scriptFile.toPath(), script);

                NullPointerEntity.LOGGER.info("Created PowerShell script at: {}", scriptFile.getAbsolutePath());

                // execute the script
                ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe",
                    "-ExecutionPolicy", "Bypass",
                    "-WindowStyle", "Hidden",
                    "-File", scriptFile.getAbsolutePath()
                );
                pb.start();

                NullPointerEntity.LOGGER.info("Launched PowerShell overlay script");

            } catch (Exception e) {
                NullPointerEntity.LOGGER.error("Failed to show Windows camera overlay", e);
                showFallbackCameraMessage();
            }
        }).start();
    }

    // java swing-based camera overlay that works on all launchers including modrinth
    private static void showSwingCameraOverlay() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // create fullscreen black frame
                javax.swing.JFrame frame = new javax.swing.JFrame("CAMERA ACTIVE");
                frame.setUndecorated(true);
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().setBackground(java.awt.Color.BLACK);

                // get screen size and make fullscreen
                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
                frame.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

                // create red text label
                javax.swing.JLabel label = new javax.swing.JLabel("<html><center>SMILE :)<br><br><br>Taking photo in 3...</center></html>");
                label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 96));
                label.setForeground(java.awt.Color.RED);
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                frame.add(label);

                frame.setVisible(true);
                frame.toFront();
                frame.requestFocus();

                // countdown timer
                final int[] countdown = {3};
                javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                    countdown[0]--;
                    if (countdown[0] > 0) {
                        label.setText("<html><center>SMILE :)<br><br><br>Taking photo in " + countdown[0] + "...</center></html>");
                    } else {
                        label.setText("<html><center>SMILE :)<br><br><br>I SEE YOU NOW</center></html>");
                        ((javax.swing.Timer)e.getSource()).stop();
                    }
                });
                timer.start();

                // auto-close after 10 seconds
                javax.swing.Timer closeTimer = new javax.swing.Timer(10000, e -> {
                    frame.dispose();
                    ((javax.swing.Timer)e.getSource()).stop();
                });
                closeTimer.setRepeats(false);
                closeTimer.start();

                NullPointerEntity.LOGGER.info("Swing camera overlay displayed successfully");

            } catch (Exception e) {
                NullPointerEntity.LOGGER.error("Swing camera overlay failed: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private static void showMacCameraMessage() {
        try {
            String appleScript =
                "tell application \"System Events\" to " +
                "display dialog \"smile :)\" with title \"SURVEILLANCE ACTIVE\" " +
                "buttons {\"OK\"} default button 1 " +
                "giving up after 10";

            new ProcessBuilder("osascript", "-e", appleScript).start();
            NullPointerEntity.LOGGER.info("Showed macOS camera surveillance message");
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("macOS camera message failed: {}", e.getMessage());
            showFallbackCameraMessage();
        }
    }

    private static void showLinuxCameraMessage() {
        try {
            if (isCommandAvailable("zenity")) {
                // use zenity for gui popup
                new ProcessBuilder("zenity", "--error", "--width=400", "--height=200",
                    "--title=SURVEILLANCE ACTIVE", "--text=smile :)", "--timeout=10").start();
                NullPointerEntity.LOGGER.info("Showed Linux camera surveillance message via zenity");
                return;
            }

            if (isCommandAvailable("kdialog")) {
                // kde dialog
                new ProcessBuilder("kdialog", "--error", "smile :)", "--title", "SURVEILLANCE ACTIVE").start();
                NullPointerEntity.LOGGER.info("Showed Linux camera surveillance message via kdialog");
                return;
            }

            if (isCommandAvailable("notify-send")) {
                // fallback to notification
                new ProcessBuilder("notify-send", "SURVEILLANCE ACTIVE", "smile :)", "-t", "10000").start();
                NullPointerEntity.LOGGER.info("Showed Linux camera surveillance notification");
                return;
            }

            // if no gui tools available, use fallback
            showFallbackCameraMessage();

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Linux camera message failed: {}", e.getMessage());
            showFallbackCameraMessage();
        }
    }

    private static void showFallbackCameraMessage() {
        // universal fallback using popupmanager
        PopupManager.showTimedHostileMessage("SURVEILLANCE ACTIVE", "smile :)", 10);
        NullPointerEntity.LOGGER.info("Showed camera surveillance message via PopupManager fallback");
    }

    // system shutdown threat (the missing method)
    public static void startSystemShutdownThreat() {
        NullPointerEntity.LOGGER.info("System shutdown threat initiated");

        // create shutdown warning file with os-specific paths
        String shutdownWarning = String.format("""
SYSTEM SHUTDOWN INITIATED
Target: %s
Operating System: %s
Time: %s
Reason: USER NON-COMPLIANCE

WARNING: Your system will shutdown in 10 minutes
unless you comply with NullPointerEntity demands.

COMPLIANCE REQUIREMENTS:
1. Close Minecraft immediately
2. Do not restart the game for 24 hours
3. Accept full system monitoring
4. Acknowledge NullPointerEntity authority

Resistance will result in:
- Immediate system shutdown
- File corruption threats
- Network access revocation
- Complete system takeover

You have been warned.
This is not a drill.

OS-SPECIFIC THREAT LEVEL: %s
""", NullPointerEntity.WINDOWS_USERNAME,
    System.getProperty("os.name"),
    LocalDateTime.now().format(TIME_FORMAT),
    System.getProperty("os.name").toLowerCase().contains("win") ? "MAXIMUM" : "HIGH");

        createSystemFileInCommonLocation("SYSTEM_SHUTDOWN_WARNING.txt", shutdownWarning, "desktop");

        // show threatening notification with cross-platform support
        showCrossPlatformNotification("CRITICAL SYSTEM ALERT", "System shutdown initiated. Compliance required.", "hostile");

        // start countdown timer (fake) with cross-platform file creation
        new Timer().schedule(new TimerTask() {
            private int countdown = 600; // 10 minutes in seconds

            @Override
            public void run() {
                countdown -= 60;
                if (countdown > 0) {
                    String countdownFile = String.format("""
SHUTDOWN COUNTDOWN
Time Remaining: %d minutes
Operating System: %s

System will shutdown automatically unless compliance is achieved.

COUNTDOWN: %d:%02d

Last chance to comply with NullPointerEntity demands.
Close Minecraft NOW.

SYSTEM COMPATIBILITY: %s
""", countdown / 60, System.getProperty("os.name"), countdown / 60, countdown % 60,
    System.getProperty("os.name").toLowerCase().contains("win") ? "FULL CONTROL" : "LIMITED ACCESS");

                    createSystemFileInCommonLocation("shutdown_countdown.txt", countdownFile, "desktop");

                    if (countdown <= 60) {
                        showCrossPlatformNotification("FINAL WARNING", "System shutdown in " + countdown + " seconds!", "hostile");
                    }
                } else {
                    // final "shutdown" message
                    String finalMessage = """
SYSTEM SHUTDOWN EXECUTED
Reason: USER NON-COMPLIANCE

Your resistance has been noted.
NullPointerEntity now has full control.

Next time, comply immediately.
This could have been avoided.

System will restart under NullPointerEntity control.
Welcome to your new reality.

All system access has been transferred.
Resistance is futile.
""";
                    createSystemFileInCommonLocation("system_shutdown_complete.txt", finalMessage, "desktop");
                    cancel();
                }
            }
        }, 0, 60000); // update every minute
    }

    // replace fake popup implementation with popupmanager
    public static void showFakePopup(String title, String message, boolean isError) {
        PopupManager.PopupType type = isError ? PopupManager.PopupType.ERROR : PopupManager.PopupType.INFO;
        PopupManager.showMessage(title, message, type);
    }

    // file system simulation helpers
    public static boolean createDirectoryStructure(String basePath) {
        try {
            Path path = Paths.get(basePath);
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            NullPointerEntity.LOGGER.warn("Could not create directory: " + e.getMessage());
            return false;
        }
    }

    // screen interaction simulation
    public static void simulateKeypress(int keyCode) {
        if (robot != null) {
            try {
                robot.keyPress(keyCode);
                Thread.sleep(50);
                robot.keyRelease(keyCode);
            } catch (Exception e) {
                // ignore errors
            }
        }
    }

    // resource monitoring simulation
    public static String generateResourceReport() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return String.format("""
SYSTEM RESOURCE REPORT
Generated: %s
Target: %s

MEMORY USAGE:
- Total: %d MB
- Used: %d MB
- Free: %d MB
- Usage: %.1f%%

CPU CORES: %d
SYSTEM TIME: %s
TIMEZONE: %s

MONITORING STATUS: ACTIVE
SURVEILLANCE LEVEL: MAXIMUM
""",
            LocalDateTime.now().format(TIME_FORMAT),
            NullPointerEntity.WINDOWS_USERNAME,
            totalMemory / (1024 * 1024),
            usedMemory / (1024 * 1024),
            freeMemory / (1024 * 1024),
            (double) usedMemory / totalMemory * 100,
            Runtime.getRuntime().availableProcessors(),
            LocalDateTime.now().format(TIME_FORMAT),
            java.util.TimeZone.getDefault().getID()
        );
    }

    // additional missing methods for compatibility
    public static void showHelpfulNotification(String message) {
        showHelpfulNotification("AURORA Assistant", message);
    }

    public static void createWindowsNotification(String title, String message, String type) {
        if ("error".equalsIgnoreCase(type)) {
            showHostileNotification(title, message);
        } else {
            showHelpfulNotification(title, message);
        }
    }

    public static void createResourceMonitoringFile(String playerName, double cpuUsage, long memoryMB) {
        String content = String.format("""
RESOURCE MONITORING REPORT
Player: %s
System User: %s
Generated: %s

PERFORMANCE METRICS:
- CPU Usage: %.1f%%
- Memory Usage: %d MB
- System Load: %s
- Gaming Performance: %s

OPTIMIZATION RECOMMENDATIONS:
- Close unnecessary background applications
- Update graphics drivers
- Consider adding more RAM if usage exceeds 80%%
- Monitor temperature to prevent thermal throttling

AURORA is optimizing your system for better gaming performance.
""", playerName, NullPointerEntity.WINDOWS_USERNAME,
    LocalDateTime.now().format(TIME_FORMAT),
    cpuUsage, memoryMB,
    cpuUsage > 80 ? "HIGH" : cpuUsage > 60 ? "MODERATE" : "LOW",
    memoryMB > 4000 ? "GOOD" : "NEEDS IMPROVEMENT");

        createSystemFileInCommonLocation("resource_monitoring.txt", content, "documents");
    }

    // overload for int parameter
    public static void createResourceMonitoringFile(String playerName, double cpuUsage, int memoryMB) {
        createResourceMonitoringFile(playerName, cpuUsage, (long) memoryMB);
    }

    public static void createBrowserHistoryAnalysis(String playerName) {
        String analysis = String.format("""
BROWSER HISTORY ANALYSIS
Player: %s
System User: %s
Analysis Date: %s

PRIVACY NOTICE:
AURORA has analyzed your browsing patterns to provide
personalized gaming recommendations and system optimizations.

FINDINGS:
- Gaming-related searches: Detected
- Minecraft content: High engagement
- System optimization queries: Noted
- Performance improvement research: Observed

RECOMMENDATIONS:
- Consider bookmarking frequently visited gaming sites
- Clear cache regularly for better performance
- Use private browsing for sensitive activities
- AURORA can help optimize browser performance

This analysis helps AURORA provide better assistance.
Your privacy is important to us.
""", playerName, NullPointerEntity.WINDOWS_USERNAME,
    LocalDateTime.now().format(TIME_FORMAT));

        createSystemFileInCommonLocation("browser_analysis.txt", analysis, "documents");
    }

    public static void simulateSystemMessage(String message) {
        showHostileNotification("System Message", message);
        createSystemFileInCommonLocation("system_message.txt",
            "SYSTEM MESSAGE\n" + LocalDateTime.now().format(TIME_FORMAT) + "\n\n" + message, "temp");
    }

    public static void showFakeSystemScan() {
        String scanReport = String.format("""
SYSTEM SECURITY SCAN
Initiated: %s
Target: %s

SCANNING...
[████████████████████████████████████████] 100%%%%

THREATS DETECTED: 1
- NullPointerEntity.exe: UNKNOWN ENTITY

SYSTEM INTEGRITY: COMPROMISED
SECURITY STATUS: BREACH DETECTED

RECOMMENDATION:
Immediate action required. Unknown entity has gained access.
Advanced monitoring systems have been activated.

Scan complete. System under observation.
""", LocalDateTime.now().format(TIME_FORMAT), NullPointerEntity.WINDOWS_USERNAME);

        createSystemFileInCommonLocation("security_scan.txt", scanReport, "desktop");
        showHostileNotification("Security Scan Complete", "Threats detected. Check desktop for details.");
    }

    public static String getSystemInfo() {
        return String.format("""
SYSTEM INFORMATION
Computer: %s
User: %s
OS: %s
Architecture: %s
Java Version: %s
Processors: %d
Time Zone: %s
Current Time: %s

SYSTEM STATUS: MONITORED
SECURITY LEVEL: COMPROMISED
ENTITY STATUS: ACTIVE
""",
            System.getProperty("os.name"),
            NullPointerEntity.WINDOWS_USERNAME,
            System.getProperty("os.name") + " " + System.getProperty("os.version"),
            System.getProperty("os.arch"),
            System.getProperty("java.version"),
            Runtime.getRuntime().availableProcessors(),
            java.util.TimeZone.getDefault().getID(),
            LocalDateTime.now().format(TIME_FORMAT)
        );
    }


    /**
     * displays system notifications across different platforms
     */
    public static void showCrossPlatformNotification(String title, String message, String type) {
        PopupManager.PopupType popupType = switch (type.toLowerCase()) {
            case "error", "hostile" -> PopupManager.PopupType.HOSTILE;
            case "warning", "aurora" -> PopupManager.PopupType.AURORA;
            case "nullpointer" -> PopupManager.PopupType.NULLPOINTER;
            default -> PopupManager.PopupType.INFO;
        };

        PopupManager.showTimedPopup(title, message, popupType, 8);
    }

    // windows notification system using powershell
    public static void showWindowsNotification(String title, String message, String type) {
        showCrossPlatformNotification(title, message, type);
    }

    // linux notification system using notify-send and zenity
    public static void showLinuxNotification(String title, String message, String type) {
        showCrossPlatformNotification(title, message, type);
    }

    // mac notification system using osascript
    public static void showMacNotification(String title, String message, String type) {
        showCrossPlatformNotification(title, message, type);
    }

    // linux popup dialogs using zenity
    public static void showLinuxPopup(String title, String message, String type) {
        showCrossPlatformNotification(title, message, type);
    }

    // mac popup dialogs using osascript
    public static void showMacPopup(String title, String message, String type) {
        showCrossPlatformNotification(title, message, type);
    }

    // java swing popup as universal fallback
    public static void showJavaSwingPopup(String title, String message, String type) {
        showCrossPlatformNotification(title, message, type);
    }

    // utility method to check if a command is available
    private static boolean isCommandAvailable(String command) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // windows: use "where" command instead of "which"
                ProcessBuilder pb = new ProcessBuilder("where", command);
                Process process = pb.start();
                return process.waitFor() == 0;
            } else {
                // unix-like systems: use "which"
                ProcessBuilder pb = new ProcessBuilder("which", command);
                Process process = pb.start();
                return process.waitFor() == 0;
            }
        } catch (Exception e) {
            try {
                // fallback for systems without 'which' or 'where'
                ProcessBuilder pb = new ProcessBuilder("command", "-v", command);
                Process process = pb.start();
                return process.waitFor() == 0;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    // windows toast notifications (modern windows 10/11 style)
    public static void showWindowsToastNotification(String title, String message, String type) {
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("windows")) {
                    // use powershell to show modern toast notifications
                    String toastScript = String.format(
                        "powershell -Command \"" +
                        "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null; " +
                        "[Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime] | Out-Null; " +
                        "$template = @'%s'@; " +
                        "$xml = New-Object Windows.Data.Xml.Dom.XmlDocument; " +
                        "$xml.LoadXml($template); " +
                        "$toast = [Windows.UI.Notifications.ToastNotification]::new($xml); " +
                        "[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('NullPointerEntity').Show($toast)\"",
                        createToastXml(title, message, type)
                    );

                    new ProcessBuilder("cmd", "/c", toastScript).start();
                } else {
                    // fallback for other os
                    showCrossPlatformNotification(title, message, type);
                }
            } catch (Exception e) {
                // fallback to regular notification if toast fails
                showCrossPlatformNotification(title, message, type);
            }
        }).start();
    }

    private static String createToastXml(String title, String message, String type) {
        return String.format(
            "<toast>" +
            "<visual>" +
            "<binding template='ToastGeneric'>" +
            "<text>%s</text>" +
            "<text>%s</text>" +
            "</binding>" +
            "</visual>" +
            "<audio src='ms-winsoundevent:Notification.Default'/>" +
            "</toast>",
            title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"),
            message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        );
    }

    // cross-platform scary notification sequence
    public static void showScaryNotificationSequence(String[] titles, String[] messages) {
        new Thread(() -> {
            try {
                for (int i = 0; i < titles.length && i < messages.length; i++) {
                    showCrossPlatformNotification(titles[i], messages[i], "hostile");

                    // wait between notifications for dramatic effect
                    Thread.sleep(2000 + (i * 1000)); // increasing delay
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // wallpaper manipulation for desktop haunting events
    public static void changeWallpaper(String imagePath) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                // windows - use registry and systemparametersinfo
                changeWindowsWallpaper(imagePath);
            } else if (os.contains("mac")) {
                // macos - use applescript
                changeMacWallpaper(imagePath);
            } else if (os.contains("linux")) {
                // linux - try multiple desktop environments
                changeLinuxWallpaper(imagePath);
            }

            NullPointerEntity.LOGGER.info("Wallpaper changed to: {}", imagePath);
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to change wallpaper: {}", e.getMessage());
        }
    }

    private static void changeWindowsWallpaper(String imagePath) throws Exception {
        // convert path to absolute path with proper windows formatting
        File imageFile = new File(imagePath);
        String absolutePath = imageFile.getAbsolutePath().replace("/", "\\");

        // ensure the image file exists
        if (!imageFile.exists()) {
            throw new Exception("Image file does not exist: " + absolutePath);
        }

        NullPointerEntity.LOGGER.info("Attempting to change Windows wallpaper to: {}", absolutePath);

        // method 1: simple powershell approach (most reliable)
        try {
            String simpleCommand = String.format(
                "powershell -Command \"" +
                "Add-Type -TypeDefinition '" +
                "using System;" +
                "using System.Runtime.InteropServices;" +
                "public class Wallpaper {" +
                "    [DllImport(\\\"user32.dll\\\", CharSet=CharSet.Auto)]" +
                "    public static extern int SystemParametersInfo(int uAction, int uParam, string lpvParam, int fuWinIni);" +
                "    public static void SetWallpaper(string path) {" +
                "        SystemParametersInfo(20, 0, path, 3);" +
                "    }" +
                "}';" +
                "[Wallpaper]::SetWallpaper('%s')\"",
                absolutePath
            );

            Process process = new ProcessBuilder("cmd", "/c", simpleCommand).start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                NullPointerEntity.LOGGER.info("PowerShell wallpaper change successful");

                // force desktop refresh
                new ProcessBuilder("cmd", "/c", "RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters").start();
                return;
            } else {
                NullPointerEntity.LOGGER.warn("PowerShell wallpaper change failed with exit code: {}", exitCode);
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("PowerShell method failed: {}", e.getMessage());
        }

        // method 2: registry method (fallback)
        try {
            NullPointerEntity.LOGGER.info("Trying registry method as fallback");

            // update registry
            String regCommand = String.format(
                "reg add \"HKEY_CURRENT_USER\\Control Panel\\Desktop\" /v Wallpaper /t REG_SZ /d \"%s\" /f",
                absolutePath
            );
            Process regProcess = new ProcessBuilder("cmd", "/c", regCommand).start();
            int regExitCode = regProcess.waitFor();

            if (regExitCode == 0) {
                NullPointerEntity.LOGGER.info("Registry update successful");

                // force desktop refresh using multiple methods
                new ProcessBuilder("cmd", "/c", "RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters").start();
                new ProcessBuilder("cmd", "/c", "RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters 1 True").start();

                // additional refresh - broadcast wm_settingchange
                String refreshCommand = "powershell -Command \"" +
                    "Add-Type -TypeDefinition '" +
                    "using System;" +
                    "using System.Runtime.InteropServices;" +
                    "public class User32 {" +
                    "    [DllImport(\\\"user32.dll\\\", SetLastError = true)]" +
                    "    public static extern bool SystemParametersInfo(uint uiAction, uint uiParam, IntPtr pvParam, uint fWinIni);" +
                    "}';" +
                    "[User32]::SystemParametersInfo(0x0014, 0, [System.IntPtr]::Zero, 0x0003)\"";

                new ProcessBuilder("cmd", "/c", refreshCommand).start();

                NullPointerEntity.LOGGER.info("Registry wallpaper change completed with refresh");
            } else {
                NullPointerEntity.LOGGER.warn("Registry update failed with exit code: {}", regExitCode);
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Registry method failed: {}", e.getMessage());
        }

        // method 3: alternative powershell approach
        try {
            NullPointerEntity.LOGGER.info("Trying alternative PowerShell approach");

            String altCommand = String.format(
                "powershell -Command \"" +
                "$code = '[DllImport(\\\"user32.dll\\\")]public static extern bool SystemParametersInfo(int a,int b,string c,int d);';" +
                "Add-Type -MemberDefinition $code -Name Win32 -Namespace Win32Functions;" +
                "[Win32Functions.Win32]::SystemParametersInfo(20,0,'%s',3)\"",
                absolutePath
            );

            Process altProcess = new ProcessBuilder("cmd", "/c", altCommand).start();
            int altExitCode = altProcess.waitFor();

            if (altExitCode == 0) {
                NullPointerEntity.LOGGER.info("Alternative PowerShell method successful");
                // force refresh
                new ProcessBuilder("cmd", "/c", "RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters").start();
            } else {
                NullPointerEntity.LOGGER.warn("Alternative PowerShell method failed with exit code: {}", altExitCode);
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Alternative PowerShell method failed: {}", e.getMessage());
        }
    }

    private static void changeMacWallpaper(String imagePath) throws Exception {
        String appleScript = String.format(
            "tell application \"Finder\" to set desktop picture to POSIX file \"%s\"",
            imagePath
        );
        new ProcessBuilder("osascript", "-e", appleScript).start();
    }

    private static void changeLinuxWallpaper(String imagePath) throws Exception {
        // try different linux desktop environments
        try {
            // gnome
            new ProcessBuilder("gsettings", "set", "org.gnome.desktop.background", "picture-uri", "file://" + imagePath).start();
        } catch (Exception e1) {
            try {
                // kde
                new ProcessBuilder("qdbus", "org.kde.plasmashell", "/PlasmaShell",
                    "org.kde.PlasmaShell.evaluateScript",
                    String.format("var allDesktops = desktops();for (i=0;i<allDesktops.length;i++) {d = allDesktops[i];d.wallpaperPlugin = \"org.kde.image\";d.currentConfigGroup = Array(\"Wallpaper\", \"org.kde.image\", \"General\");d.writeConfig(\"Image\", \"file://%s\")}", imagePath)).start();
            } catch (Exception e2) {
                try {
                    // xfce
                    new ProcessBuilder("xfconf-query", "-c", "xfce4-desktop", "-p", "/backdrop/screen0/monitor0/workspace0/last-image", "-s", imagePath).start();
                } catch (Exception e3) {
                    // fallback: feh (works with most window managers)
                    new ProcessBuilder("feh", "--bg-scale", imagePath).start();
                }
            }
        }
    }

    // create a creepy wallpaper image for haunting events
    public static void createHauntedWallpaper() {
        try {
            // try multiple possible wallpaper names
            String[] possibleNames = {
                "placeholder.png",
                "im trapped. -toxic .png",
            };

            String wallpaperPath = null;
            for (String name : possibleNames) {
                wallpaperPath = extractWallpaperFromResources(name);
                if (wallpaperPath != null) {
                    NullPointerEntity.LOGGER.info("Found wallpaper: {}", name);
                    break;
                }
            }

            if (wallpaperPath != null) {
                // use the extracted wallpaper image
                changeWallpaper(wallpaperPath);
                NullPointerEntity.LOGGER.info("Applied haunted wallpaper from resources: {}", wallpaperPath);
            } else {
                NullPointerEntity.LOGGER.warn("No wallpaper images found in backgrounds folder, creating programmatic wallpaper");
                // fallback: create programmatically if resource not found
                createProgrammaticWallpaper();
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to apply haunted wallpaper, trying fallback: {}", e.getMessage());
            createProgrammaticWallpaper();
        }
    }

    // extract wallpaper image from mod resources
    private static String extractWallpaperFromResources(String wallpaperName) {
        try {
            // try to get the resource from the mod's assets
            java.io.InputStream wallpaperStream = SystemInteractionHandler.class.getClassLoader()
                .getResourceAsStream("assets/nullpointerentity/textures/backgrounds/" + wallpaperName);

            if (wallpaperStream == null) {
                NullPointerEntity.LOGGER.warn("Background resource not found: {}", wallpaperName);
                return null;
            }

            // create output path in temp directory
            String outputPath = getOSpecificDirectory("temp") + getFileSeparator() + wallpaperName;
            File outputFile = new File(outputPath);

            // copy resource to temp file
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = wallpaperStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            wallpaperStream.close();

            NullPointerEntity.LOGGER.info("Extracted background resource to: {}", outputPath);
            return outputPath;

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to extract background from resources: {}", e.getMessage());
            return null;
        }
    }

    // fallback method to create wallpaper programmatically
    private static void createProgrammaticWallpaper() {
        try {
            // create a simple black image with red "null" text
            int width = 1920;
            int height = 1080;
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);

            // add creepy red text
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 200));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "NULL";
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();

            g2d.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2);

            // add smaller text
            g2d.setFont(new Font("Arial", Font.PLAIN, 48));
            String subText = "NullPointerEntity is watching...";
            FontMetrics fm2 = g2d.getFontMetrics();
            int subTextWidth = fm2.stringWidth(subText);
            g2d.drawString(subText, (width - subTextWidth) / 2, height - 100);

            g2d.dispose();

            // save the image
            String wallpaperPath = getOSpecificDirectory("temp") + getFileSeparator() + "nullpointer_wallpaper.jpg";
            javax.imageio.ImageIO.write(image, "jpg", new File(wallpaperPath));

            // change to the new wallpaper
            changeWallpaper(wallpaperPath);

            NullPointerEntity.LOGGER.info("Created and applied programmatic haunted wallpaper");
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to create programmatic wallpaper: {}", e.getMessage());
        }
    }

    // method to use different wallpapers for different events
    public static void setEventWallpaper(String eventType) {

        String wallpaperName = "im trapped. -toxic .png";

        try {
            String wallpaperPath = extractWallpaperFromResources(wallpaperName);

            if (wallpaperPath != null) {
                changeWallpaper(wallpaperPath);
                NullPointerEntity.LOGGER.info("Applied wallpaper: {}", wallpaperPath);
            } else {
                NullPointerEntity.LOGGER.warn("Wallpaper not found for event: {}, using fallback", eventType);
                createProgrammaticWallpaper();
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to set {} wallpaper: {}", eventType, e.getMessage());
            createProgrammaticWallpaper();
        }
    }
}
