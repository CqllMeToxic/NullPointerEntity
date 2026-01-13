package lol.cqllmetoxic.nullpointerentity.monitoring;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * reads browser history from chrome, firefox, edge, and brave.
 * accesses sqlite database files to retrieve recent browsing data.
 * respects privacy settings and returns fake data when privacy mode is enabled.
 */
public class BrowserHistoryReader {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            // sqlite driver not available
        }
    }

    /**
     * represents a single browser history entry.
     * stores URL, page title, visit time, and browser name.
     */
    public static class HistoryEntry {
        public String url, title, browser;
        public long visitTime;
        public int visitCount;

        public HistoryEntry(String url, String title, long visitTime, int visitCount, String browser) {
            this.url = url;
            this.title = formatTitle(title);
            this.visitTime = visitTime;
            this.visitCount = visitCount;
            this.browser = browser;
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", title, browser);
        }

        /**
         * cleans up page titles for better display.
         * handles common title formats from youtube, github, etc.
         */
        private static String formatTitle(String rawTitle) {
            if (rawTitle == null || rawTitle.trim().isEmpty()) {
                return "Untitled Page";
            }

            // keep the exact title as it appears in the browser
            // most sites already format their titles like "page name | site name"
            String title = rawTitle.trim();

            // handle some common cases where titles might need cleaning
            if (title.equals("New Tab") || title.equals("Blank Page") || title.equals("Start Page")) {
                return "Browser Start Page";
            }

            // clean up some common title formats to make them more readable
            if (title.contains(" - YouTube")) {
                return title.replace(" - YouTube", " | YouTube");
            }
            if (title.contains(" | GitHub") && title.length() > 50) {
                // shorten very long github titles
                String[] parts = title.split(" \\| GitHub");
                return parts[0] + " | GitHub";
            }

            return title;
        }
    }

    /**
     * asynchronously retrieves recent browser history entries.
     * respects privacy settings - returns fake data if privacy mode is on.
     *
     * @param limit maximum number of history entries to retrieve
     * @return future containing list of history entries
     */
    public static CompletableFuture<List<HistoryEntry>> getRecentHistoryAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                return getFakeHistory();
            }

            try {
                return new BrowserHistoryReader().getHistory(limit, false);
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Failed to read browser history: {}", e.getMessage());
                return getFakeHistory();
            }
        });
    }

    public static CompletableFuture<List<HistoryEntry>> getMostVisitedAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            // check privacy mode - return fake history if enabled
            if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                return getFakeHistory();
            }

            try {
                return new BrowserHistoryReader().getHistory(limit, true);
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Failed to read most visited history: {}", e.getMessage());
                return getFakeHistory();
            }
        });
    }

    private List<HistoryEntry> getHistory(int limit, boolean mostVisited) {
        List<HistoryEntry> history = new ArrayList<>();
        String sortBy = mostVisited ? "visit_count" : "last_visit_time";

        // only use the default browser - no multiple browsers
        String defaultBrowser = getDefaultBrowser();

        if (defaultBrowser != null) {
            // read only from the default browser
            history.addAll(readBrowserHistory(defaultBrowser, sortBy, limit));
        } else {
            // if we can't detect default browser, try chrome as fallback
            history.addAll(readBrowserHistory("chrome", sortBy, limit));
        }

        history.sort(mostVisited ?
                (a, b) -> Integer.compare(b.visitCount, a.visitCount) :
                (a, b) -> Long.compare(b.visitTime, a.visitTime));

        return history.subList(0, Math.min(limit, history.size()));
    }

    public static String getDefaultBrowser() {
        // try to get the actual default browser from windows registry first
        String actualDefault = getWindowsDefaultBrowser();
        if (actualDefault != null) {
            NullPointerEntity.LOGGER.debug("detected windows default browser: {}", actualDefault);
            return actualDefault;
        }

        // fallback: detect by checking which has the most recent activity
        long latestTime = 0;
        String defaultBrowser = "chrome"; // fallback to chrome

        String[] browsers = {"chrome", "firefox", "edge", "opera", "brave"};
        for (String browser : browsers) {
            String path = getBrowserPath(browser);
            if (path != null) {
                File historyFile = new File(path);
                if (historyFile.exists()) {
                    long lastModified = historyFile.lastModified();
                    if (lastModified > latestTime) {
                        latestTime = lastModified;
                        defaultBrowser = browser;
                    }
                }
            }
        }

        NullPointerEntity.LOGGER.debug("fallback detected browser: {}", defaultBrowser);
        return defaultBrowser;
    }

    private static String getWindowsDefaultBrowser() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return null; // only works on windows
        }

        try {
            // use processbuilder instead of deprecated runtime.exec(string)
            ProcessBuilder processBuilder = new ProcessBuilder(
                "reg", "query",
                "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\Shell\\Associations\\UrlAssociations\\http\\UserChoice",
                "/v", "ProgId"
            );
            Process process = processBuilder.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("ProgId") && line.contains("REG_SZ")) {
                    String progId = line.substring(line.lastIndexOf("REG_SZ") + 6).trim();

                    // map windows progid to our browser names
                    if (progId.contains("ChromeHTML")) return "chrome";
                    if (progId.contains("FirefoxURL")) return "firefox";
                    if (progId.contains("MSEdgeHTM")) return "edge";
                    if (progId.contains("OperaStable")) return "opera";
                }
            }

            reader.close();
            process.waitFor();

        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("failed to detect windows default browser: {}", e.getMessage());
        }

        return null; // couldn't detect, will use fallback method
    }

    private List<HistoryEntry> readBrowserHistory(String browser, String sortBy, int limit) {
        List<HistoryEntry> entries = new ArrayList<>();
        String historyPath = getBrowserPath(browser);
        if (historyPath == null) return entries;

        File historyFile = new File(historyPath);
        if (!historyFile.exists()) {
            NullPointerEntity.LOGGER.debug("{} history file not found: {}", browser, historyPath);
            return entries;
        }

        String tempPath = copyToTemp(historyPath, browser);
        String connectionUrl = "jdbc:sqlite:" + (tempPath != null ? tempPath : historyPath);

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {
            String query;
            String timeColumn;

            // handle firefox's different schema
            if (browser.equals("firefox")) {
                // try different possible column names for firefox
                timeColumn = "last_visit_date";
                query = "SELECT url, title, visit_count, " + timeColumn + " FROM moz_places " +
                       "WHERE " + timeColumn + " IS NOT NULL AND visit_count > 0 " +
                       "ORDER BY " + (sortBy.equals("last_visit_time") ? timeColumn : sortBy) + " DESC LIMIT ?";

                // if that fails, try alternative column name
                try (PreparedStatement testStmt = conn.prepareStatement("SELECT last_visit_time FROM moz_places LIMIT 1")) {
                    testStmt.executeQuery();
                    timeColumn = "last_visit_time";
                    query = "SELECT url, title, visit_count, " + timeColumn + " FROM moz_places " +
                           "WHERE " + timeColumn + " IS NOT NULL AND visit_count > 0 " +
                           "ORDER BY " + sortBy + " DESC LIMIT ?";
                } catch (SQLException ignored) {
                    // stick with original query
                }
            } else {
                // chrome, edge, opera use standard schema
                timeColumn = "last_visit_time";
                query = "SELECT url, title, visit_count, " + timeColumn + " FROM urls " +
                       "WHERE " + timeColumn + " > 0 AND visit_count > 0 " +
                       "ORDER BY " + sortBy + " DESC LIMIT ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String title = rs.getString("title");
                    if (title == null || title.trim().isEmpty() || isBoringPage(title)) continue;

                    long visitTime = rs.getLong(timeColumn);

                    // convert firefox time to compatible format if needed
                    if (browser.equals("firefox") && timeColumn.equals("last_visit_date")) {
                        visitTime = visitTime / 1000; // firefox uses microseconds, convert to milliseconds
                    }

                    entries.add(new HistoryEntry(
                        rs.getString("url"),
                        title,
                        visitTime,
                        rs.getInt("visit_count"),
                        browser.substring(0, 1).toUpperCase() + browser.substring(1) // capitalize browser name
                    ));
                }
            }

            NullPointerEntity.LOGGER.debug("Successfully read {} {} history entries", entries.size(), browser);

        } catch (SQLException e) {
            NullPointerEntity.LOGGER.warn("Error reading {} history: {}", browser, e.getMessage());
        } finally {
            if (tempPath != null) {
                try {
                    new File(tempPath).delete();
                } catch (Exception e) {
                    NullPointerEntity.LOGGER.debug("Failed to delete temp file: {}", tempPath);
                }
            }
        }

        return entries;
    }

    private static String getBrowserPath(String browser) {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        return switch(browser) {
            case "chrome" -> os.contains("win") ? userHome + "/AppData/Local/Google/Chrome/User Data/Default/History" :
                    os.contains("mac") ? userHome + "/Library/Application Support/Google/Chrome/Default/History" :
                            userHome + "/.config/google-chrome/Default/History";
            case "edge" -> os.contains("win") ? userHome + "/AppData/Local/Microsoft/Edge/User Data/Default/History" :
                    os.contains("mac") ? userHome + "/Library/Application Support/Microsoft Edge/Default/History" :
                            userHome + "/.config/microsoft-edge/Default/History";
            case "opera" -> os.contains("win") ? userHome + "/AppData/Roaming/Opera Software/Opera GX Stable/History" :
                    os.contains("mac") ? userHome + "/Library/Application Support/com.operasoftware.OperaGX/History" :
                            userHome + "/.config/opera-gx/History";
            case "firefox" -> getFirefoxPath();
            default -> null;
        };
    }

    private static String getFirefoxPath() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        String profilesPath = os.contains("win") ? userHome + "/AppData/Roaming/Mozilla/Firefox/Profiles" :
                os.contains("mac") ? userHome + "/Library/Application Support/Firefox/Profiles" :
                        userHome + "/.mozilla/firefox";

        File profilesDir = new File(profilesPath);
        if (profilesDir.exists() && profilesDir.isDirectory()) {
            File[] profiles = profilesDir.listFiles();
            if (profiles != null) {
                for (File profile : profiles) {
                    if (profile.isDirectory() && (profile.getName().contains("default") || profile.getName().contains("release"))) {
                        File placesFile = new File(profile, "places.sqlite");
                        if (placesFile.exists()) return placesFile.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private String copyToTemp(String originalPath, String browserName) {
        try {
            File originalFile = new File(originalPath);
            if (!originalFile.exists()) return null;
            File tempFile = File.createTempFile(browserName.toLowerCase() + "_history_", ".sqlite");
            tempFile.deleteOnExit();
            java.nio.file.Files.copy(originalFile.toPath(), tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to copy {} history to temp: {}", browserName, e.getMessage());
            return null;
        }
    }

    private boolean isBoringPage(String title) {
        if (title == null) return true;
        String lower = title.toLowerCase();
        return lower.contains("new tab") ||
               lower.contains("blank page") ||
               lower.contains("start page") ||
               lower.contains("speed dial") ||
               lower.trim().isEmpty() ||
               lower.equals("untitled");
    }

    private static List<HistoryEntry> getFakeHistory() {
        return List.of(
                new HistoryEntry("https://github.com/user/repo", "Your Repositories | GitHub", System.currentTimeMillis(), 89, "Chrome"),
                new HistoryEntry("https://www.paypal.com/checkout", "Pay with PayPal | PayPal", System.currentTimeMillis() - 1000000, 45, "Chrome"),
                new HistoryEntry("https://www.youtube.com/watch?v=example", "Minecraft Tutorial - Building Guide | YouTube", System.currentTimeMillis() - 2000000, 156, "Chrome"),
                new HistoryEntry("https://stackoverflow.com/questions/12345", "How to fix NullPointerException | Stack Overflow", System.currentTimeMillis() - 3000000, 34, "Firefox"),
                new HistoryEntry("https://discord.com/channels/123/456", "Gaming Server - General | Discord", System.currentTimeMillis() - 4000000, 78, "Chrome")
        );
    }

    public static List<HistoryEntry> getHistory() {
        // check privacy mode - return fake history if enabled
        if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
            return getFakeHistory();
        }

        try {
            return new BrowserHistoryReader().getHistory(10, false);
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to get browser history: {}", e.getMessage());
            return getFakeHistory();
        }
    }

    public static List<HistoryEntry> getMostVisited() {
        // check privacy mode - return fake history if enabled
        if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
            return getFakeHistory();
        }

        try {
            return new BrowserHistoryReader().getHistory(10, true);
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to get most visited history: {}", e.getMessage());
            return getFakeHistory();
        }
    }
}
