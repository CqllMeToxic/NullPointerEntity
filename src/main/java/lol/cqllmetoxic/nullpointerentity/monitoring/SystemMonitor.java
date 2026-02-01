package lol.cqllmetoxic.nullpointerentity.monitoring;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * collects system information like CPU usage, running processes, and system details.
 * cross-platform implementation for windows, mac, and linux.
 * provides data for aurora's system awareness messages.
 */
public class SystemMonitor {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    private static final boolean IS_MAC = OS_NAME.contains("mac") || OS_NAME.contains("darwin");
    private static final boolean IS_LINUX = OS_NAME.contains("linux") || OS_NAME.contains("nix") || OS_NAME.contains("aix");

    /**
     * attempts to get current CPU usage as a percentage.
     * tries multiple methods with fallbacks if the OS doesn't support it.
     * 
     * @return CPU usage percentage (0-100)
     */
    public static double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;

                double processCpuUsage = sunOsBean.getProcessCpuLoad() * 100;

                if (processCpuUsage >= 0) {
                    return processCpuUsage;
                }
            }

            double loadAverage = osBean.getSystemLoadAverage();
            if (loadAverage >= 0) {
                int availableProcessors = osBean.getAvailableProcessors();
                double cpuPercentage = (loadAverage / availableProcessors) * 100;
                return Math.min(cpuPercentage, 100.0);
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("could not get cpu usage: {}", e.getMessage());
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
            return Math.min(memoryUsagePercent * 0.8, 100.0);
        } catch (Exception e) {
            return 15.0;
        }
    }

    /**
     * retrieves a list of currently running GUI applications (taskbar apps).
     *
     * @return list of taskbar application names
     */

    // worst event of all time this was such a pain to get working without lagging like crazy

    public static List<String> getRunningProcesses() {
        List<String> processes = new ArrayList<>();
        try {
            ProcessBuilder processBuilder;

            if (IS_WINDOWS) {
                processBuilder = new ProcessBuilder("tasklist", "/fo", "csv", "/nh");
            } else if (IS_MAC) {
                // mac: use ps command to get GUI applications
                processBuilder = new ProcessBuilder("ps", "-eo", "comm", "-c");
            } else if (IS_LINUX) {
                // linux: use ps command to get processes
                processBuilder = new ProcessBuilder("ps", "-eo", "comm", "--no-headers");
            } else {
                // fallback for unknown unix-like systems
                processBuilder = new ProcessBuilder("ps", "-e");
            }

            Process process = processBuilder.start();

            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String processName;
                    if (IS_WINDOWS) {
                        // parse simple csv format: "name","pid","session","mem"
                        line = line.trim();
                        if (line.startsWith("\"") && line.contains("\",")) {
                            String[] parts = line.split("\",\"");
                            if (parts.length >= 1) {
                                processName = parts[0].substring(1); // remove leading quote

                                // only include GUI applications
                                if (isTaskbarApplication(processName)) {
                                    processes.add(processName);
                                }
                            }
                        }
                    } else {
                        // unix systems: only include known GUI applications
                        processName = line.trim();
                        if (!processName.isEmpty() && !processName.equals("COMMAND")) {
                            // clean up the process name (remove path, keep only executable name)
                            if (processName.contains("/")) {
                                processName = processName.substring(processName.lastIndexOf("/") + 1);
                            }
                            if (isTaskbarApplication(processName)) {
                                processes.add(processName);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not get taskbar applications: {}", e.getMessage());
            // fallback to common GUI applications if we can't get real ones
            processes.addAll(getFallbackProcesses());
        }

        if (processes.isEmpty()) {
            processes.addAll(getFallbackProcesses());
        }

        return processes;
    }

    /**
     * determines if a process is a GUI application that would appear in the taskbar.
     * includes browsers, media players, communication apps, games, etc.
     */
    private static boolean isTaskbarApplication(String processName) {
        if (processName == null) return false;

        String lower = processName.toLowerCase();

        // common GUI applications that appear in taskbar
        return lower.contains("chrome") ||
               lower.contains("firefox") ||
               lower.contains("msedge") || lower.contains("edge") ||
               lower.contains("opera") ||
               lower.contains("brave") ||
               lower.contains("safari") ||
               lower.contains("discord") ||
               lower.contains("spotify") ||
               lower.contains("steam") ||
               lower.contains("epicgames") ||
               lower.contains("minecraft") ||
               lower.contains("java") && !lower.contains("javaw") || // include java GUI apps
               lower.contains("code") || // vscode
               lower.contains("notepad++") ||
               lower.contains("sublime") ||
               lower.contains("atom") ||
               lower.contains("explorer") && !lower.contains("iexplore") || // file explorer
               lower.contains("vlc") ||
               lower.contains("obs") ||
               lower.contains("streamlabs") ||
               lower.contains("photoshop") ||
               lower.contains("illustrator") ||
               lower.contains("premiere") ||
               lower.contains("davinci") ||
               lower.contains("gimp") ||
               lower.contains("blender") ||
               lower.contains("word") ||
               lower.contains("excel") ||
               lower.contains("powerpoint") ||
               lower.contains("outlook") ||
               lower.contains("teams") ||
               lower.contains("zoom") ||
               lower.contains("slack") ||
               lower.contains("notion") ||
               lower.contains("terminal") ||
               lower.contains("iterm") ||
               lower.contains("cmd") ||
               lower.contains("powershell") ||
               lower.contains("thunderbird") ||
               lower.contains("skype") ||
               lower.contains("telegram") ||
               lower.contains("signal") ||
               lower.contains("whatsapp") ||
               lower.contains("itunes") ||
               lower.contains("musicbee") ||
               lower.contains("foobar") ||
               lower.contains("winamp") ||
               lower.contains("audacity") ||
               lower.contains("premier") ||
               lower.contains("twitch") ||
               lower.contains("nvidia") && (lower.contains("geforce") || lower.contains("share")) ||
               lower.contains("radeon") ||
               lower.contains("afterburner") ||
               lower.contains("hwmonitor") ||
               lower.contains("cpuz") ||
               lower.contains("gpuz") ||
               lower.contains("battle.net") ||
               lower.contains("origin") ||
               lower.contains("uplay") ||
               lower.contains("gog") ||
               lower.contains("roblox") ||
               lower.contains("fortnite") ||
               lower.contains("leagueoflegends") ||
               lower.contains("valorant") ||
               lower.contains("overwolf") ||
               lower.contains("parsec") ||
               lower.contains("anydesk") ||
               lower.contains("teamviewer") ||
               lower.contains("rainmeter") ||
               lower.contains("wallpaper") ||
               lower.contains("7zip") ||
               lower.contains("winrar");
    }

    /**
     * gathers comprehensive system information including OS, user, and platform details.
     *
     * @return map of system property names to values
     */
    public static Map<String, String> getSystemInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.version", System.getProperty("os.version"));
        info.put("os.arch", System.getProperty("os.arch"));
        info.put("java.version", System.getProperty("java.version"));
        info.put("user.name", System.getProperty("user.name"));
        info.put("user.home", System.getProperty("user.home"));

        // get additional platform-specific information
        if (IS_WINDOWS) {
            getWindowsSystemInfo(info);
        } else if (IS_MAC) {
            getMacSystemInfo(info);
        } else if (IS_LINUX) {
            getLinuxSystemInfo(info);
        }

        return info;
    }

    /**
     * async version of getSystemInfo for non-blocking calls.
     *
     * @return future containing system info map
     */
    public static CompletableFuture<Map<String, String>> getSystemInfoAsync() {
        return CompletableFuture.supplyAsync(() -> getSystemInfo());
    }

    /**
     * retrieves current memory usage statistics in megabytes.
     *
     * @return map with total, free, used, and max memory values
     */
    public static Map<String, Double> getMemoryInfo() {
        Map<String, Double> memoryInfo = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        // convert bytes to megabytes
        memoryInfo.put("total", totalMemory / (1024.0 * 1024.0));
        memoryInfo.put("free", freeMemory / (1024.0 * 1024.0));
        memoryInfo.put("used", usedMemory / (1024.0 * 1024.0));
        memoryInfo.put("max", maxMemory / (1024.0 * 1024.0));

        return memoryInfo;
    }

    /**
     * performs a complete system analysis including all available metrics.
     * runs asynchronously to avoid blocking the main thread.
     *
     * @return future containing full analysis results
     */
    public static CompletableFuture<Map<String, Object>> getFullSystemAnalysisAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> analysis = new HashMap<>();

            analysis.put("system_info", getSystemInfo());
            analysis.put("memory_info", getMemoryInfo());
            analysis.put("cpu_usage", getCpuUsage());
            analysis.put("running_processes", getRunningProcesses());
            analysis.put("timestamp", System.currentTimeMillis());

            return analysis;
        });
    }

    /**
     * collects windows-specific system information using environment variables and wmic.
     */
    private static void getWindowsSystemInfo(Map<String, String> info) {
        try {
            // get windows-specific system information
            info.put("processor", System.getenv("PROCESSOR_IDENTIFIER"));
            info.put("computer_name", System.getenv("COMPUTERNAME"));
            info.put("username", System.getenv("USERNAME"));
            info.put("user_domain", System.getenv("USERDOMAIN"));
            info.put("temp_dir", System.getenv("TEMP"));

            // try to get more detailed windows info using wmic
            ProcessBuilder pb = new ProcessBuilder("wmic", "os", "get", "Caption,Version,BuildNumber", "/format:csv");
            Process process = pb.start();

        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("could not get detailed windows info: {}", e.getMessage());
        }
    }

    /**
     * collects mac-specific system information using environment variables and system commands.
     */
    private static void getMacSystemInfo(Map<String, String> info) {
        try {
            info.put("computer_name", System.getenv("HOSTNAME"));
            info.put("username", System.getenv("USER"));
            info.put("shell", System.getenv("SHELL"));
            info.put("temp_dir", System.getenv("TMPDIR"));

            // try to get more detailed mac info using system_profiler
            ProcessBuilder pb = new ProcessBuilder("sw_vers", "-productVersion");
            Process process = pb.start();
        } catch (Exception e) {
        }
    }

    /**
     * collects linux-specific system information using environment variables and system commands.
     */
    private static void getLinuxSystemInfo(Map<String, String> info) {
        try {
            info.put("computer_name", System.getenv("HOSTNAME"));
            info.put("username", System.getenv("USER"));
            info.put("shell", System.getenv("SHELL"));
            info.put("desktop_session", System.getenv("DESKTOP_SESSION"));
            info.put("temp_dir", System.getenv("TMPDIR"));

            // try to get more detailed linux info
            ProcessBuilder pb = new ProcessBuilder("uname", "-a");
            Process process = pb.start();

        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("could not get detailed linux info: {}", e.getMessage());
        }
    }

    private static List<String> getFallbackProcesses() {
        List<String> fallback = new ArrayList<>();

        if (IS_WINDOWS) {
            fallback.add("java.exe");
            fallback.add("javaw.exe");
            fallback.add("minecraft.exe");
            fallback.add("explorer.exe");
            fallback.add("chrome.exe");
            fallback.add("discord.exe");
            fallback.add("steam.exe");
            fallback.add("spotify.exe");
            fallback.add("notepad.exe");
            fallback.add("taskmgr.exe");
            // launcher-specific windows processes
            fallback.add("CurseForge.exe");
            fallback.add("Overwolf.exe");
            fallback.add("PrismLauncher.exe");
            fallback.add("MultiMC.exe");
            fallback.add("TechnicLauncher.exe");
            fallback.add("ModrinthApp.exe");
        } else if (IS_MAC) {
            fallback.add("java");
            fallback.add("Minecraft");
            fallback.add("Google Chrome");
            fallback.add("Discord");
            fallback.add("Steam");
            fallback.add("Spotify");
            fallback.add("TextEdit");
            fallback.add("Activity Monitor");
            // launcher-specific macos processes
            fallback.add("CurseForge");
            fallback.add("Prism Launcher");
            fallback.add("MultiMC");
            fallback.add("Technic Launcher");
            fallback.add("Modrinth App");
        } else if (IS_LINUX) {
            fallback.add("java");
            fallback.add("minecraft-launcher");
            fallback.add("modrinth-app");
            fallback.add("chromium-browser");
            fallback.add("firefox");
            fallback.add("discord");
            fallback.add("steam");
            fallback.add("spotify");
            fallback.add("gedit");
            fallback.add("htop");
            // launcher-specific linux processes
            fallback.add("curseforge");
            fallback.add("prismlauncher");
            fallback.add("multimc");
            fallback.add("technic-launcher");
            fallback.add("technic");
        }

        // cross-platform launcher process names
        fallback.add("modrinth");
        fallback.add("theseus");
        fallback.add("modrinth-app");
        fallback.add("ModrinthApp.exe");
        fallback.add("Modrinth App");
        fallback.add("curseforge");
        fallback.add("CurseForge");
        fallback.add("overwolf");
        fallback.add("Overwolf");
        fallback.add("prismlauncher");
        fallback.add("PrismLauncher");
        fallback.add("multimc");
        fallback.add("MultiMC");
        fallback.add("technic");
        fallback.add("TechnicLauncher");
        fallback.add("Technic Launcher");

        return fallback;
    }
}
