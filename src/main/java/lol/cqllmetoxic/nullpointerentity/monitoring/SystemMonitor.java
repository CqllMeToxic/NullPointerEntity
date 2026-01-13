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
     * retrieves a list of currently running processes on the system.
     * filters out most basic system processes but keeps some for atmosphere.
     *
     * @return list of process names
     */
    public static List<String> getRunningProcesses() {
        List<String> processes = new ArrayList<>();
        try {
            ProcessBuilder processBuilder;

            if (IS_WINDOWS) {
                // windows: use tasklist command
                processBuilder = new ProcessBuilder("tasklist", "/fo", "csv", "/nh");
            } else if (IS_MAC) {
                // mac: use ps command with mac-specific flags
                processBuilder = new ProcessBuilder("ps", "-eo", "comm", "-c");
            } else if (IS_LINUX) {
                // linux: use ps command with linux-specific flags
                processBuilder = new ProcessBuilder("ps", "-eo", "comm", "--no-headers");
            } else {
                // fallback for unknown unix-like systems
                processBuilder = new ProcessBuilder("ps", "-e");
            }

            Process process = processBuilder.start();

            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null && processes.size() < 100) {
                    String processName;
                    if (IS_WINDOWS) {
                        // parse csv format without header
                        line = line.trim();
                        if (line.startsWith("\"") && line.contains("\",")) {
                            int endQuote = line.indexOf("\",");
                            processName = line.substring(1, endQuote);

                            // filter out system processes no one don't care about
                            if (!isSystemProcess(processName)) {
                                processes.add(processName);
                            }
                        }
                    } else {
                        // unix systems: process name is usually the whole line or first part
                        processName = line.trim();
                        if (!processName.isEmpty() && !processName.equals("COMMAND")) {
                            // clean up the process name (remove path, keep only executable name)
                            if (processName.contains("/")) {
                                processName = processName.substring(processName.lastIndexOf("/") + 1);
                            }
                            if (!isSystemProcess(processName)) {
                                processes.add(processName);
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            // fallback to common processes if we can't get real ones
            processes.addAll(getFallbackProcesses());
        }

        if (processes.isEmpty()) {
            processes.addAll(getFallbackProcesses());
        }

        return processes;
    }

    /**
     * determines if a process should be filtered from the list.
     * keeps interesting processes but removes basic system utilities.
     */
    private static boolean isSystemProcess(String processName) {
        if (processName == null) return true;

        String lower = processName.toLowerCase();

        // only filter out the most basic system processes - keep the creepy ones
        // keep: smss.exe, csrss.exe, wininit.exe, services.exe, lsass.exe (these are creepy)
        // keep: svchost.exe, dwm.exe (commonly running, adds to realism)
        return lower.equals("system") ||
               lower.equals("idle") ||
               lower.equals("winlogon.exe") ||
               lower.equals("spoolsv.exe") ||
               lower.equals("taskhost.exe") ||
               lower.equals("conhost.exe") ||
               lower.equals("audiodg.exe") ||
               lower.equals("dllhost.exe") ||
               lower.equals("rundll32.exe") ||
               lower.equals("wuauclt.exe") ||
               lower.equals("taskhostw.exe") ||
               lower.equals("searchindexer.exe") ||
               lower.equals("msdtc.exe") ||
               lower.equals("wbem") ||
               lower.contains("microsoft") && lower.contains("antimalware");
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
