package lol.cqllmetoxic.nullpointerentity.system;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * detects and lists running processes on the system.
 * filters out crash handlers and system utilities.
 * used for aurora's process monitoring messages.
 */
public class ProcessSimulator {
    private static final Random random = new Random();

    private static final Set<String> EXCLUDED_PROCESSES = Set.of(
        "crashhandler",
        "crashhandler.exe",
        "crash_handler",
        "crash_handler.exe",
        "javacrashhandler",
        "javacrashhandler.exe",
        "hs_err_pid",
        "crash_dump",
        "crashdump",
        "error_reporting",
        "wer",
        "werfault",
        "werfault.exe",
        "wercon.exe",
        "dumpminidump",
        "minidumpwritedump",
        "crash_reporter",
        "crashreporter",
        "crashreporter.exe",
        "bugreport",
        "bug_report",
        "errorreport",
        "error_report"
    );

    public static class ProcessInfo {
        public String name, user, status, path;
        public int pid, parentPid, threads;
        public double cpuUsage, memoryMB;
        public boolean isSuspicious, isSystemProcess;

        public ProcessInfo(String name, int pid, int parentPid, String user, double cpuUsage, double memoryMB,
                          int threads, String status, String path, boolean isSuspicious, boolean isSystemProcess) {
            this.name = name; this.pid = pid; this.parentPid = parentPid; this.user = user;
            this.cpuUsage = cpuUsage; this.memoryMB = memoryMB; this.threads = threads; this.status = status;
            this.path = path; this.isSuspicious = isSuspicious; this.isSystemProcess = isSystemProcess;
        }
    }

    public static CompletableFuture<List<ProcessInfo>> getRunningProcesses() {
        return CompletableFuture.supplyAsync(() -> {
            List<ProcessInfo> processes = new ArrayList<>();

            try {
                // use real system processes with actual information
                ProcessHandle.allProcesses().forEach(handle -> {
                    ProcessHandle.Info info = handle.info();
                    String name = info.command().map(cmd -> {
                        String[] parts = cmd.replace("\\", "/").split("/");
                        return parts[parts.length - 1];
                    }).orElse("process_" + handle.pid() + ".exe");

                    // skip excluded processes
                    if (isExcludedProcess(name)) {
                        return;
                    }

                    // get actual cpu and memory usage if possible
                    double cpuUsage = 0.0;
                    double memoryMB = 0.0;

                    try {
                        // try to get actual process information
                        com.sun.management.OperatingSystemMXBean osBean =
                            (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();

                        // for the current process, we can get actual data
                        if (handle.pid() == ProcessHandle.current().pid()) {
                            cpuUsage = osBean.getProcessCpuLoad() * 100.0;
                            memoryMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024.0);
                        } else {
                            // for other processes, estimate based on process type
                            cpuUsage = estimateCpuUsageByProcessType(name);
                            memoryMB = estimateMemoryUsageByProcessType(name);
                        }
                    } catch (Exception e) {
                        // fallback to estimation based on process name
                        cpuUsage = estimateCpuUsageByProcessType(name);
                        memoryMB = estimateMemoryUsageByProcessType(name);
                    }

                    processes.add(new ProcessInfo(
                        name,
                        (int) handle.pid(),
                        handle.parent().map(p -> (int) p.pid()).orElse(0),
                        info.user().orElse(NullPointerEntity.WINDOWS_USERNAME),
                        cpuUsage,
                        memoryMB,
                        estimateThreadsByProcessType(name),
                        handle.isAlive() ? "Running" : "Stopped",
                        info.command().orElse("C:\\Windows\\System32\\" + name),
                        isSuspiciousProcess(name),
                        isSystemProcess(name)
                    ));
                });
            } catch (Exception e) {
                // fallback to system process detection if processhandle fails
                processes.addAll(getFallbackSystemProcesses());
            }

            processes.addAll(generateMaliciousProcesses());

            return processes;
        });
    }

    /**
     * check if a process should be excluded from detection and monitoring
     */
    private static boolean isExcludedProcess(String processName) {
        if (processName == null || processName.isEmpty()) {
            return false;
        }

        String lowerName = processName.toLowerCase();

        // check against excluded process list
        for (String excluded : EXCLUDED_PROCESSES) {
            if (lowerName.contains(excluded.toLowerCase())) {
                return true;
            }
        }

        // additional pattern-based exclusions
        if (lowerName.matches(".*crash.*") ||
            lowerName.matches(".*dump.*") ||
            lowerName.matches(".*error.*report.*") ||
            lowerName.matches(".*hs_err.*") ||
            lowerName.matches(".*jvm.*crash.*")) {
            return true;
        }

        return false;
    }

    // estimate cpu usage based on process type (more realistic than random)
    private static double estimateCpuUsageByProcessType(String name) {
        String lower = name.toLowerCase();

        // high cpu processes
        if (lower.contains("chrome") || lower.contains("firefox") || lower.contains("edge")) {
            return 8.0 + (Math.random() * 12.0); // 8-20%
        }
        if (lower.contains("java") || lower.contains("minecraft")) {
            return 15.0 + (Math.random() * 25.0); // 15-40%
        }
        if (lower.contains("obs") || lower.contains("premier") || lower.contains("photoshop")) {
            return 10.0 + (Math.random() * 30.0); // 10-40%
        }

        // medium cpu processes
        if (lower.contains("discord") || lower.contains("steam") || lower.contains("spotify")) {
            return 2.0 + (Math.random() * 8.0); // 2-10%
        }
        if (lower.contains("explorer") || lower.contains("dwm")) {
            return 1.0 + (Math.random() * 4.0); // 1-5%
        }

        // low cpu system processes
        if (lower.contains("svchost") || lower.contains("system") || lower.contains("lsass")) {
            return 0.1 + (Math.random() * 2.0); // 0.1-2.1%
        }

        // default for unknown processes
        return 0.5 + (Math.random() * 3.0); // 0.5-3.5%
    }

    // estimate memory usage based on process type (more realistic than random)
    private static double estimateMemoryUsageByProcessType(String name) {
        String lower = name.toLowerCase();

        // high memory processes
        if (lower.contains("chrome") || lower.contains("firefox") || lower.contains("edge")) {
            return 200.0 + (Math.random() * 600.0); // 200-800mb
        }
        if (lower.contains("java") || lower.contains("minecraft")) {
            return 500.0 + (Math.random() * 1500.0); // 500mb-2gb
        }
        if (lower.contains("photoshop") || lower.contains("premier") || lower.contains("blender")) {
            return 800.0 + (Math.random() * 2200.0); // 800mb-3gb
        }

        // medium memory processes
        if (lower.contains("discord") || lower.contains("steam") || lower.contains("obs")) {
            return 100.0 + (Math.random() * 300.0); // 100-400mb
        }
        if (lower.contains("spotify") || lower.contains("vscode") || lower.contains("code")) {
            return 80.0 + (Math.random() * 220.0); // 80-300mb
        }

        // low memory processes
        if (lower.contains("notepad") || lower.contains("calculator")) {
            return 10.0 + (Math.random() * 30.0); // 10-40mb
        }
        if (lower.contains("explorer") || lower.contains("dwm")) {
            return 30.0 + (Math.random() * 70.0); // 30-100mb
        }

        // system processes
        if (lower.contains("svchost") || lower.contains("system") || lower.contains("lsass")) {
            return 5.0 + (Math.random() * 50.0); // 5-55mb
        }

        // default for unknown processes
        return 15.0 + (Math.random() * 85.0); // 15-100mb
    }

    // estimate thread count based on process type
    private static int estimateThreadsByProcessType(String name) {
        String lower = name.toLowerCase();

        if (lower.contains("chrome") || lower.contains("firefox")) {
            return 8 + random.nextInt(20); // 8-28 threads
        }
        if (lower.contains("java") || lower.contains("minecraft")) {
            return 6 + random.nextInt(15); // 6-21 threads
        }
        if (lower.contains("svchost") || lower.contains("explorer")) {
            return 3 + random.nextInt(8); // 3-11 threads
        }
        if (lower.contains("system") || lower.contains("lsass")) {
            return 1 + random.nextInt(4); // 1-5 threads
        }

        return 1 + random.nextInt(8); // 1-9 threads for others
    }

    private static List<ProcessInfo> getFallbackSystemProcesses() {
        List<ProcessInfo> fallback = new ArrayList<>();

        // add some realistic system processes with proper resource usage
        fallback.add(new ProcessInfo("explorer.exe", 1000, 4, "SYSTEM",
            estimateCpuUsageByProcessType("explorer.exe"),
            estimateMemoryUsageByProcessType("explorer.exe"),
            estimateThreadsByProcessType("explorer.exe"),
            "Running", "C:\\Windows\\explorer.exe", false, true));

        fallback.add(new ProcessInfo("dwm.exe", 1001, 4, "SYSTEM",
            estimateCpuUsageByProcessType("dwm.exe"),
            estimateMemoryUsageByProcessType("dwm.exe"),
            estimateThreadsByProcessType("dwm.exe"),
            "Running", "C:\\Windows\\System32\\dwm.exe", false, true));

        return fallback;
    }

    public static String generateProcessAnalysis() {
        List<ProcessInfo> processes = getRunningProcesses().join();

        StringBuilder analysis = new StringBuilder("PROCESS MONITORING REPORT\n");
        analysis.append("=".repeat(28)).append("\n");
        analysis.append("Target System: ").append(NullPointerEntity.WINDOWS_USERNAME).append("\n");
        analysis.append("Total Processes: ").append(processes.size()).append("\n\n");

        // system stats
        double totalCpu = processes.stream().mapToDouble(p -> p.cpuUsage).sum();
        double totalMemory = processes.stream().mapToDouble(p -> p.memoryMB).sum();
        long suspiciousCount = processes.stream().filter(p -> p.isSuspicious).count();

        analysis.append("SYSTEM PERFORMANCE:\n");
        analysis.append(String.format("- Total CPU Usage: %.1f%%\n", Math.min(totalCpu, 100.0)));
        analysis.append(String.format("- Total Memory Usage: %.1f MB\n", totalMemory));
        analysis.append(String.format("- Suspicious Processes: %d\n\n", suspiciousCount));

        analysis.append("HIGH CPU PROCESSES:\n");
        processes.stream()
                .filter(p -> p.cpuUsage > 10.0)
                .sorted((a, b) -> Double.compare(b.cpuUsage, a.cpuUsage))
                .limit(5)
                .forEach(p -> analysis.append(String.format("- %s (PID %d): %.1f%% CPU, %.1f MB RAM\n",
                    p.name, p.pid, p.cpuUsage, p.memoryMB)));

        analysis.append("\nSUSPICIOUS PROCESSES DETECTED:\n");
        processes.stream()
                .filter(p -> p.isSuspicious)
                .forEach(p -> analysis.append(String.format("- %s (PID %d) - %s - THREAT LEVEL: %s\n",
                    p.name, p.pid, p.path, p.name.contains("NullPointer") ? "CRITICAL" : "HIGH")));

        analysis.append("\nMONITORING PROCESSES:\n");
        processes.stream()
                .filter(p -> p.name.toLowerCase().contains("monitor") || p.name.toLowerCase().contains("log") ||
                           p.name.toLowerCase().contains("watch") || p.name.toLowerCase().contains("sniffer"))
                .forEach(p -> analysis.append(String.format("- %s: ACTIVE surveillance on %s\n", p.name, NullPointerEntity.WINDOWS_USERNAME)));

        analysis.append("\nSYSTEM STATUS: COMPROMISED\n");
        analysis.append("Monitoring systems: ACTIVE\n");
        analysis.append("User privacy: NONEXISTENT\n");
        analysis.append("Process isolation: BYPASSED\n");

        return analysis.toString();
    }

    public static String simulateTaskManagerKill() {
        return String.format("""
TASK MANAGER INTERVENTION DETECTED
User: %s attempted to access Task Manager

COUNTERMEASURES DEPLOYED:
- Task Manager access: BLOCKED
- Process termination: PREVENTED  
- Administrative privileges: REVOKED
- System control: MAINTAINED

Processes protected:
- NullPointerEntity.exe: UNKILLABLE
- keylogger_svc.exe: PROTECTED
- browser_monitor.exe: HIDDEN
- network_sniffer.exe: SYSTEM LEVEL

Warning: Attempts to interfere with monitoring
will result in immediate system lockdown.

Your resistance only makes me stronger.
""", NullPointerEntity.WINDOWS_USERNAME);
    }

    public static List<ProcessInfo> getHiddenProcesses() {
        return List.of(
            new ProcessInfo("shadow_keylogger.exe", 31337, 4, "SYSTEM", 0.1, 15.6, 1, "Hidden", "C:\\Windows\\System32\\drivers\\shadow_keylogger.exe", true, true),
            new ProcessInfo("stealth_monitor.dll", 41337, 6666, "SYSTEM", 0.3, 8.9, 1, "Injected", "C:\\Windows\\System32\\stealth_monitor.dll", true, true),
            new ProcessInfo("rootkit_core.sys", 51337, 4, "KERNEL", 0.0, 4.2, 0, "Driver", "C:\\Windows\\System32\\drivers\\rootkit_core.sys", true, true)
        );
    }

    private static boolean isSuspiciousProcess(String name) {
        String lower = name.toLowerCase();
        return lower.contains("keylog") || lower.contains("monitor") || lower.contains("spy") ||
               lower.contains("trojan") || lower.contains("malware") || lower.contains("backdoor") ||
               lower.contains("nullpointer") || lower.contains("hack") || lower.contains("steal") ||
               (name.equals("svchost.exe") && random.nextBoolean()); // some svchost are suspicious
    }

    private static boolean isSystemProcess(String name) {
        String lower = name.toLowerCase();
        return lower.equals("system") || lower.equals("winlogon.exe") || lower.equals("csrss.exe") ||
               lower.equals("lsass.exe") || lower.equals("services.exe") || lower.equals("smss.exe") ||
               lower.equals("dwm.exe") || lower.equals("explorer.exe") || lower.contains("svchost");
    }

    private static List<ProcessInfo> generateMaliciousProcesses() {
        List<ProcessInfo> malicious = new ArrayList<>();

        // add some fake malicious processes for immersion
        if (random.nextFloat() < 0.3f) { // 30% chance to show malicious processes
            malicious.add(new ProcessInfo(
                "nullpointer_monitor.exe",
                31337,
                4,
                "SYSTEM",
                0.1,
                15.6,
                1,
                "Hidden",
                "C:\\Windows\\System32\\drivers\\nullpointer_monitor.exe",
                true,
                true
            ));

            malicious.add(new ProcessInfo(
                "keylogger_svc.exe",
                41337,
                6666,
                "SYSTEM",
                0.3,
                8.9,
                1,
                "Running",
                "C:\\Windows\\System32\\keylogger_svc.exe",
                true,
                true
            ));
        }

        return malicious;
    }
}
