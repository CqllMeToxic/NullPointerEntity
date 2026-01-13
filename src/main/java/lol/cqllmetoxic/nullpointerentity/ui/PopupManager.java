package lol.cqllmetoxic.nullpointerentity.ui;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

/**
 * creates native OS popups outside of minecraft.
 * uses PowerShell on windows to display system-style message boxes.
 * supports different popup types with appropriate icons and sounds.
 */
public class PopupManager {

    /**
     * defines different types of popups with associated styling and sounds.
     */
    public enum PopupType {
        INFO("Information", "Asterisk"),
        WARNING("Warning", "Exclamation"),
        ERROR("Error", "Hand"),
        AURORA("AURORA", "Asterisk"),
        NULLPOINTER("NullPointerEntity", "Hand"),
        HOSTILE("System Alert", "Critical");

        private final String defaultTitle;
        private final String soundType;

        PopupType(String defaultTitle, String soundType) {
            this.defaultTitle = defaultTitle;
            this.soundType = soundType;
        }

        public String getDefaultTitle() { return defaultTitle; }
        public String getSoundType() { return soundType; }
    }

    /**
     * displays a native OS popup message.
     *
     * @param title window title (null uses default for type)
     * @param message popup message content
     * @param type popup style and icon
     */
    public static void showMessage(String title, String message, PopupType type) {
        String actualTitle = title != null ? title : type.getDefaultTitle();
        showPowerShellPopup(actualTitle, message, type);
    }

    public static void showTimedPopup(String title, String message, PopupType type, int seconds) {
        String actualTitle = title != null ? title : type.getDefaultTitle();
        showPowerShellTimedPopup(actualTitle, message, type, seconds);
    }

    public static void showPersistentPopup(String title, String message, PopupType type) {
        showMessage(title, message, type);
    }

    public static boolean showConfirmation(String title, String message, PopupType type) {
        String actualTitle = title != null ? title : type.getDefaultTitle();
        return showPowerShellConfirmation(actualTitle, message, type);
    }

    public static String showInput(String title, String message, PopupType type) {
        String actualTitle = title != null ? title : type.getDefaultTitle();
        return showPowerShellInput(actualTitle, message);
    }

    private static void showPowerShellPopup(String title, String message, PopupType type) {
        new Thread(() -> {
            try {
                String formattedMessage = formatMessage(message);

                String iconType = switch (type) {
                    case ERROR, HOSTILE, NULLPOINTER -> "Stop";
                    case WARNING -> "Exclamation";
                    default -> "Information";
                };

                String command = String.format(
                    "powershell -Command \"Add-Type -AssemblyName System.Windows.Forms; " +
                    "[System.Windows.Forms.MessageBox]::Show('%s', '%s', 'OK', '%s')\"",
                    escapeForPowerShell(formattedMessage),
                    escapeForPowerShell(title),
                    iconType
                );

                new ProcessBuilder("cmd", "/c", command).start();
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Failed to show PowerShell popup: " + e.getMessage());
                // fallback: create a txt file instead
                createPopupFallbackFile(title, message, type);
            }
        }).start();
    }

    private static void showPowerShellTimedPopup(String title, String message, PopupType type, int seconds) {
        new Thread(() -> {
            try {
                String formattedMessage = formatMessage(message);

                // create a timed popup using powershell with windows forms
                String command = String.format(
                    "powershell -Command \"" +
                    "Add-Type -AssemblyName System.Windows.Forms; " +
                    "Add-Type -AssemblyName System.Drawing; " +
                    "$form = New-Object System.Windows.Forms.Form; " +
                    "$form.Text = '%s'; " +
                    "$form.Size = New-Object System.Drawing.Size(400,200); " +
                    "$form.StartPosition = 'CenterScreen'; " +
                    "$form.TopMost = $true; " +
                    "$form.FormBorderStyle = 'FixedDialog'; " +
                    "$form.MaximizeBox = $false; " +
                    "$form.MinimizeBox = $false; " +
                    "$label = New-Object System.Windows.Forms.Label; " +
                    "$label.Text = '%s'; " +
                    "$label.AutoSize = $false; " +
                    "$label.Size = New-Object System.Drawing.Size(350,100); " +
                    "$label.Location = New-Object System.Drawing.Point(25,25); " +
                    "$form.Controls.Add($label); " +
                    "$timer = New-Object System.Windows.Forms.Timer; " +
                    "$timer.Interval = %d; " +
                    "$timer.Add_Tick({ $form.Close(); $timer.Stop(); }); " +
                    "$timer.Start(); " +
                    "$form.ShowDialog()\"",
                    escapeForPowerShell(title),
                    escapeForPowerShell(formattedMessage),
                    seconds * 1000
                );

                new ProcessBuilder("cmd", "/c", command).start();
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Failed to show timed PowerShell popup: " + e.getMessage());
                // fallback: create a txt file instead
                createPopupFallbackFile(title, message, type);
            }
        }).start();
    }

    private static boolean showPowerShellConfirmation(String title, String message, PopupType type) {
        try {
            String formattedMessage = formatMessage(message);

            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName System.Windows.Forms; " +
                "$result = [System.Windows.Forms.MessageBox]::Show('%s', '%s', 'YesNo', 'Question'); " +
                "if ($result -eq 'Yes') { exit 0 } else { exit 1 }\"",
                escapeForPowerShell(formattedMessage),
                escapeForPowerShell(title)
            );

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to show PowerShell confirmation: " + e.getMessage());
            return false;
        }
    }

    private static String showPowerShellInput(String title, String message) {
        try {
            String formattedMessage = formatMessage(message);

            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName Microsoft.VisualBasic; " +
                "[Microsoft.VisualBasic.Interaction]::InputBox('%s', '%s')\"",
                escapeForPowerShell(formattedMessage),
                escapeForPowerShell(title)
            );

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );

            String result = reader.readLine();
            process.waitFor();
            reader.close();

            return result != null ? result.trim() : "";
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to show PowerShell input dialog: " + e.getMessage());
            return "";
        }
    }

    private static String formatMessage(String message) {
        if (message == null) return "";

        return message.replace("\\n", "\n")
                     .replace("\\r\\n", "\n")
                     .replace("\\r", "\n");
    }

    private static String escapeForPowerShell(String text) {
        if (text == null) return "";

        return text.replace("'", "''")
                  .replace("\"", "`\"");
    }

    // convenience methods for specific mod entities
    public static void showAuroraMessage(String message) {
        showMessage("AURORA - System Assistant", message, PopupType.AURORA);
    }

    public static void showNullPointerMessage(String message) {
        showMessage("NullPointerEntity", message, PopupType.NULLPOINTER);
    }

    public static void showHostileMessage(String title, String message) {
        showMessage(title, message, PopupType.HOSTILE);
    }

    public static void showTimedAuroraMessage(String message, int seconds) {
        showTimedPopup("AURORA - System Assistant", message, PopupType.AURORA, seconds);
    }

    public static void showTimedNullPointerMessage(String message, int seconds) {
        showTimedPopup("NullPointerEntity", message, PopupType.NULLPOINTER, seconds);
    }

    public static void showTimedHostileMessage(String title, String message, int seconds) {
        showTimedPopup(title, message, PopupType.HOSTILE, seconds);
    }

    // special surveillance popup using powershell
    public static void showSurveillancePopup() {
        new Thread(() -> {
            try {
                String command =
                    "powershell -Command \"" +
                    "Add-Type -AssemblyName System.Windows.Forms; " +
                    "Add-Type -AssemblyName System.Drawing; " +
                    "$form = New-Object System.Windows.Forms.Form; " +
                    "$form.Text = 'SURVEILLANCE ACTIVE'; " +
                    "$form.Size = New-Object System.Drawing.Size(400,300); " +
                    "$form.StartPosition = 'CenterScreen'; " +
                    "$form.TopMost = $true; " +
                    "$form.FormBorderStyle = 'None'; " +
                    "$form.BackColor = 'Black'; " +
                    "$label = New-Object System.Windows.Forms.Label; " +
                    "$label.Text = 'SMILE :)'; " +
                    "$label.ForeColor = 'Red'; " +
                    "$label.Font = New-Object System.Drawing.Font('Consolas',24,[System.Drawing.FontStyle]::Bold); " +
                    "$label.TextAlign = 'MiddleCenter'; " +
                    "$label.Dock = 'Fill'; " +
                    "$form.Controls.Add($label); " +
                    "$timer = New-Object System.Windows.Forms.Timer; " +
                    "$timer.Interval = 3000; " +
                    "$timer.Add_Tick({ $form.Close(); $timer.Stop(); }); " +
                    "$timer.Start(); " +
                    "$form.Add_Click({ $form.Close(); $timer.Stop(); }); " +
                    "$form.ShowDialog()\"";

                new ProcessBuilder("cmd", "/c", command).start();
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Failed to show surveillance popup: " + e.getMessage());
                // fallback to simple message
                showHostileMessage("SURVEILLANCE ACTIVE", "smile :)");
            }
        }).start();
    }

    // fallback method to create txt files when powershell fails
    private static void createPopupFallbackFile(String title, String message, PopupType type) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            );

            String filename = String.format("popup_%s_%s.txt",
                type.name().toLowerCase(),
                timestamp
            );

            String content = String.format("""
=== POPUP MESSAGE ===
Title: %s
Type: %s
Time: %s

Message:
%s

===================================
This popup was created by NullPointerEntity because 
system popups are not available on this system.
""", title, type.name(),
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ),
                formatMessage(message)
            );

            // use the same cross-platform file creation logic as systeminteractionhandler
            createCrossPlatformFile(filename, content);

            NullPointerEntity.LOGGER.info("Created popup fallback file: {}", filename);

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to create popup fallback file: {}", e.getMessage());
        }
    }

    // cross-platform file creation method
    private static void createCrossPlatformFile(String filename, String content) {
        String[] attemptPaths = {
            // desktop (most visible)
            System.getProperty("user.home") + getFileSeparator() + "Desktop" + getFileSeparator() + filename,
            // user home directory
            System.getProperty("user.home") + getFileSeparator() + filename,
            // temp directory
            System.getProperty("java.io.tmpdir") + filename,
            // current directory
            System.getProperty("user.dir") + getFileSeparator() + filename
        };

        for (String path : attemptPaths) {
            try {
                java.io.File file = new java.io.File(path);
                java.io.File parentDir = file.getParentFile();

                // ensure parent directory exists and is writable
                if (parentDir != null && (!parentDir.exists() || !parentDir.canWrite())) {
                    if (!parentDir.mkdirs()) {
                        continue; // try next path
                    }
                }

                // test write capability
                if (!canWriteToDirectory(parentDir)) {
                    continue; // try next path
                }

                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write(content);
                    writer.flush();
                }

                if (file.exists() && file.length() > 0) {
                    NullPointerEntity.LOGGER.info("Successfully created file at: {}", file.getAbsolutePath());
                    return; // success - exit method
                }

            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Failed to create file at {}: {}", path, e.getMessage());
                // continue to next path
            }
        }

        NullPointerEntity.LOGGER.error("Failed to create file at any location for: {}", filename);
    }

    // check if we can write to a directory
    private static boolean canWriteToDirectory(java.io.File dir) {
        try {
            if (dir == null || !dir.exists() || !dir.canWrite()) {
                return false;
            }

            // test actual write capability
            java.io.File testFile = new java.io.File(dir, ".popup_test");
            try {
                testFile.createNewFile();
                boolean canWrite = testFile.exists();
                testFile.delete();
                return canWrite;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    // cross-platform file separator
    private static String getFileSeparator() {
        return System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/";
    }
}
