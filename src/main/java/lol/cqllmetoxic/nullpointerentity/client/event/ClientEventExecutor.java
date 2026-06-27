package lol.cqllmetoxic.nullpointerentity.client.event;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import lol.cqllmetoxic.nullpointerentity.client.ClientScreenShake;
import lol.cqllmetoxic.nullpointerentity.events.AuroraEvents;
import lol.cqllmetoxic.nullpointerentity.jumpscares.JumpscareManager;
import lol.cqllmetoxic.nullpointerentity.monitoring.BrowserHistoryReader;
import lol.cqllmetoxic.nullpointerentity.monitoring.LocationTracker;
import lol.cqllmetoxic.nullpointerentity.monitoring.SystemMonitor;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import lol.cqllmetoxic.nullpointerentity.ui.PopupManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * runs the per-machine half of story events locally on each player's client, fired off by the server
 * through {@code RunEventPayload}. every client digs through its OWN data and shows its OWN
 * messages/effects, so everyone gets their own info on their own screen - nothing ever leaves the
 * machine, it's all just shown/done locally. real vs faked follows the host's session Privacy Mode
 * (through {@link PrivacyManager}'s session override).
 *
 * <p>only the per-machine bits live here. the actual in-game horror (sounds, entities, status
 * effects, world changes, fake screens) stays server-side - this is basically the client twin of
 * {@code HostileEvents}.
 */
public final class ClientEventExecutor {
    private static final Random RANDOM = new Random();

    private ClientEventExecutor() {
    }

    public static void run(int eventId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        switch (eventId) {
            // nice phase (1-15) - per-machine data parts (browser / system metrics / OS popup / clock)
            case 5 -> runA5(client);
            case 9 -> runA9(client);
            case 11 -> runA11(client);
            // transition phase (16-30)
            case 16 -> runT16(client);
            case 17 -> runT17(client);
            case 18 -> runT18(client);
            case 19 -> runT19(client);
            case 20 -> runT20(client);
            case 21 -> runT21(client);
            case 22 -> runT22(client);
            case 25 -> runT25(client);
            case 26 -> runT26(client);
            case 27 -> runT27(client);
            case 28 -> runT28(client);
            case 30 -> runT30(client);
            // hostile phase (31-45)
            case 31 -> runE31(client);
            case 32 -> runE32(client);
            case 33 -> runE33(client);
            case 34 -> runE34(client);
            case 35 -> runE35(client);
            case 36 -> runE36(client);
            case 37 -> runE37(client);
            case 38 -> runE38(client);
            case 39 -> runE39(client);
            case 40 -> runE40(client);
            // jumpscare phase (46-60) - per-machine data parts
            case 46 -> runJ46(client);
            case 47 -> runJ47(client);
            case 48 -> runJ48(client);
            case 49 -> runJ49(client);
            case 51 -> runJ51(client);
            case 50 -> runJ50(client);
            case 53 -> runJ53(client);
            case 54 -> runJ54(client);
            case 56 -> runJ56(client);
            case 57 -> runJ57(client);
            case 58 -> runJ58(client);
            case 60 -> runJ60(client);
            // fake-player info reveal (id 70, not a real story event): the spawned NullPointerEntity
            // names each player's OWN ip/location/username instead of the host's
            case 70 -> runFakePlayerReveal(client);
            default -> NullPointerEntity.LOGGER.debug("No client-side handler for event {}", eventId);
        }
    }

    // ---- nice phase (1-15) - per-machine data parts ------------------------

    /**
     * e5 (activity patterns) - reads this client's own browser, cpu, memory and processes, writes its own
     * analysis file and names its own browser in chat, so every player sees their own machine. the heavy
     * reads run off the render thread.
     */
    private static void runA5(MinecraftClient client) {
        String mcUser = mcUser(client);
        auroraAqua(client, tr("event.nullpointerentity.aurora.e5.init"));
        schedule(client, 3500, () -> new Thread(() -> {
            Runtime runtime = Runtime.getRuntime();
            long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
            long usedMemoryMB = totalMemoryMB - (runtime.freeMemory() / (1024 * 1024));

            double cpuUsage = AuroraEvents.getCrossPlatformCpuUsage();
            double browserRAM = AuroraEvents.getCrossPlatformBrowserMemoryUsage();
            double totalAppRAM = AuroraEvents.getTotalApplicationMemoryUsage();
            String gamingSetup = AuroraEvents.determineGamingSetup(cpuUsage, usedMemoryMB, totalMemoryMB);
            String actualBrowser = AuroraEvents.detectActualBrowser();
            String socialMediaReport = AuroraEvents.generateRealisticSocialMediaReport();
            String digitalBehaviorProfile = AuroraEvents.generateDigitalBehaviorProfile();
            String recommendation = AuroraEvents.generatePerformanceRecommendation(
                cpuUsage, (double) usedMemoryMB / totalMemoryMB * 100, browserRAM);
            List<String> processes = AuroraEvents.generateCrossPlatformProcessList();
            int platformCount = AuroraEvents.getRandomSocialMediaCount();

            // each client writes its own analysis file (privacy-gated sink) from its own data
            AuroraEvents.createDetailedActivityReport(mcUser, actualBrowser, browserRAM, totalAppRAM,
                cpuUsage, usedMemoryMB, totalMemoryMB, processes, new ArrayList<>(), gamingSetup,
                socialMediaReport, digitalBehaviorProfile);

            boolean privacy = PrivacyManager.isPrivacyEnabled();
            client.execute(() -> {
                auroraAqua(client, tr("event.nullpointerentity.aurora.e5.complete", platformCount));
                auroraAqua(client, tr(platformCount >= 4
                    ? "event.nullpointerentity.aurora.e5.high"
                    : "event.nullpointerentity.aurora.e5.moderate"));
                auroraAqua(client, tr("event.nullpointerentity.aurora.e5.mapped", recommendation));
                auroraAqua(client, tr("event.nullpointerentity.aurora.e5.surveillance"));
                auroraAqua(client, tr("event.nullpointerentity.aurora.e5.browsing", actualBrowser));
                auroraAqua(client, tr(privacy
                    ? "event.nullpointerentity.aurora.e5.privacy"
                    : "event.nullpointerentity.aurora.e5.fullaccess"));
            });
        }).start());
    }

    /**
     * e9 (system integration) - pops a native os notification on this client's own machine plus the
     * integration chat, so the popup shows up on every player's own machine.
     */
    private static void runA9(MinecraftClient client) {
        auroraAqua(client, tr("event.nullpointerentity.aurora.e9.init"));
        schedule(client, 3000, () -> {
            auroraAqua(client, tr("event.nullpointerentity.aurora.e9.integration47"));
            auroraAqua(client, tr("event.nullpointerentity.aurora.e9.accessing"));
        });
        schedule(client, 7000, () -> {
            // first hint of transformation - the word "different" bleeds red
            Text transformingMessage = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.AQUA)
                .append(Text.translatable("event.nullpointerentity.aurora.e9.transform_pre").formatted(Formatting.WHITE))
                .append(Text.translatable("event.nullpointerentity.aurora.e9.transform_word").formatted(Formatting.RED))
                .append(Text.literal(".").formatted(Formatting.WHITE));
            if (client.inGameHud != null) {
                client.inGameHud.getChatHud().addMessage(transformingMessage);
            }
            auroraAqua(client, tr("event.nullpointerentity.aurora.e9.happening"));

            // the os notification fires on this client's own machine (PopupManager threads itself)
            SystemInteractionHandler.createWindowsNotification("AURORA System Integration",
                "Integration complete - Enhanced monitoring active", "INFO");
        });
    }

    /** e11 (sleep schedule) - comments on this client's own real system clock. */
    private static void runA11(MinecraftClient client) {
        java.time.LocalTime now = java.time.LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        String timeStr = String.format("%d:%02d %s", displayHour, minute, amPm);

        if (hour < 6) {
            auroraAqua(client, tr("event.nullpointerentity.aurora.e11.late_rest", timeStr));
            schedule(client, 2500, () -> auroraAqua(client, tr("event.nullpointerentity.aurora.e11.comeback")));
        } else if (hour >= 23) {
            auroraAqua(client, tr("event.nullpointerentity.aurora.e11.gettinglate"));
        } else {
            auroraAqua(client, tr("event.nullpointerentity.aurora.e11.goodtime", timeStr));
        }
    }

    /** e31 (data part) - this client's own running processes + a takeover report on its desktop. */
    private static void runE31(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();

        List<String> processes = SystemMonitor.getRunningProcesses();
        send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e31.process_count", processes.size(), winUser));

        if (!processes.isEmpty()) {
            int numToShow = Math.min(5, processes.size());
            StringBuilder display = new StringBuilder(tr("event.nullpointerentity.hostile.e31.running_prefix"));
            for (int i = 0; i < numToShow; i++) {
                display.append(processes.get(i));
                if (i < numToShow - 1) display.append(", ");
            }
            if (processes.size() > 5) {
                display.append(tr("event.nullpointerentity.hostile.e31.running_more", processes.size() - 5));
            }
            send(client, mcUser, winUser, display.toString());
        }

        schedule(client, 3000, () -> {
            StringBuilder list = new StringBuilder();
            if (!processes.isEmpty()) {
                list.append(tr("event.nullpointerentity.hostile.e31.report.running_header"));
                int numToList = Math.min(10, processes.size());
                for (int i = 0; i < numToList; i++) {
                    list.append("- ").append(processes.get(i)).append("\n");
                }
                if (processes.size() > 10) {
                    list.append(tr("event.nullpointerentity.hostile.e31.report.running_more", processes.size() - 10));
                }
            }
            String report = tr("event.nullpointerentity.hostile.e31.report.body", winUser, processes.size(), list.toString());
            SystemInteractionHandler.createSystemFileInCommonLocation("system_takeover_report.txt", report, "desktop");
            send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e31.desktop_hint"));
        });
    }

    /** e32 - "i know where you live": reveals this client's own IP / location. */
    private static void runE32(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();

        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e32.intro", 5, winUser));

        LocationTracker.getUserPublicIPAsync().thenCombine(
            LocationTracker.getCurrentLocationAsync(),
            (ipAddress, locationData) -> {
                String ip, city, region, isp, zip;
                if (PrivacyManager.isPrivacyEnabled()) {
                    ip = randomIP(); city = randomCity(); region = randomRegion(); isp = randomISP(); zip = randomZip();
                } else {
                    ip = ipAddress; city = locationData.city; region = locationData.region; isp = locationData.isp; zip = locationData.zipCode;
                }
                client.execute(() -> {
                    send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e32.ip", ip));
                    send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e32.city_state", city, region));
                    send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e32.zip", zip));
                    send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e32.isp", 5, isp));
                    send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e32.closing", 5));
                });
                return null;
            }
        );
    }

    /**
     * fake-player info reveal - shows THIS client's own IP / location / username (privacy-faked), so
     * the spawned NullPointerEntity names each player's own machine instead of the host's. triggered
     * by {@code FakePlayerEntity#revealPlayerInfo} via {@code sendRunEvent(player, 70)}.
     */
    private static void runFakePlayerReveal(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();

        // immediate, non-async lines (this client's own MC name + system username)
        nullpointer(client, tr("message.nullpointerentity.fakeplayer.playingas", mcUser));
        nullpointer(client, tr("message.nullpointerentity.fakeplayer.systemuser", winUser));

        LocationTracker.getUserPublicIPAsync().thenCompose(ip -> {
            // ip line first (this client's own public IP, faked under Privacy Mode)
            String shownIp = PrivacyManager.isPrivacyEnabled() ? randomIP() : ip;
            client.execute(() -> nullpointer(client, tr("message.nullpointerentity.fakeplayer.ip", shownIp)));
            return LocationTracker.getLocationFromIPAsync(ip);
        }).thenAccept(locationInfo -> {
            boolean privacy = PrivacyManager.isPrivacyEnabled();
            String city = privacy ? randomCity() : locationInfo.city;
            String region = privacy ? randomRegion() : locationInfo.region;
            String zip = privacy ? randomZip() : locationInfo.zipCode;
            String isp = privacy ? randomISP() : locationInfo.isp;
            String org = privacy ? randomISP() : locationInfo.organization;
            client.execute(() -> {
                nullpointer(client, tr("message.nullpointerentity.fakeplayer.location", city, region, zip));
                nullpointer(client, tr("message.nullpointerentity.fakeplayer.isp", isp));
                nullpointer(client, tr("message.nullpointerentity.fakeplayer.org", org));
            });
        }).exceptionally(throwable -> {
            // location lookup failed - show a generated IP + "protected" line
            client.execute(() -> {
                nullpointer(client, tr("message.nullpointerentity.fakeplayer.ip", randomIP()));
                nullpointer(client, tr("message.nullpointerentity.fakeplayer.protected"));
            });
            return null;
        });
    }

    /** e33 - this client's own OS / architecture. */
    private static void runE33(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();
        SystemMonitor.getSystemInfoAsync().thenAccept(systemInfo -> schedule(client, 2000, () -> {
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e33.scan", 5));
            String osName = systemInfo.getOrDefault("os.name", "unknown");
            String osVersion = systemInfo.getOrDefault("os.version", "unknown");
            String architecture = systemInfo.getOrDefault("os.arch", "unknown");
            send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e33.os", osName, osVersion));
            send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e33.arch", architecture));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e33.closing", 5));
        }));
    }

    /** e34 - writes a file-access log to this client's own Documents. */
    private static void runE34(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e34.intro", 5, mcUser));
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e34.privacy", 5));
        String body = tr("event.nullpointerentity.hostile.e34.file.body", winUser, (int) (RANDOM.nextInt(500) + 100));
        SystemInteractionHandler.createSystemFileInCommonLocation("file_access_log.txt", body, "documents");
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e34.closing", 5));
    }

    /** e35 - changes this client's own wallpaper and scatters ghost files. */
    private static void runE35(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e35.decorate", 5, mcUser));
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e35.vision", 5));

        SystemInteractionHandler.createHauntedWallpaper();

        String body = tr("event.nullpointerentity.hostile.e35.file.body",
            winUser, java.time.LocalDateTime.now(), winUser, winUser);
        SystemInteractionHandler.createSystemFileInCommonLocation("system_takeover_log.txt", body, "desktop");

        schedule(client, 3000, () -> {
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e35.spawn", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e35.wallpaper", 5));
            String[] ghostFiles = {
                "i_am_watching_you.txt", "dont_delete_me.txt",
                "youll_never_find_all_of_us.txt", "we_live_in_your_files_now.txt"
            };
            String[] ghostLocations = {"desktop", "documents", "desktop", "documents"};
            for (int i = 0; i < ghostFiles.length; i++) {
                String ghostMessage = tr("event.nullpointerentity.hostile.e35.ghost_file." + (i + 1));
                SystemInteractionHandler.createSystemFileInCommonLocation(ghostFiles[i], ghostMessage, ghostLocations[i]);
            }
        });

        schedule(client, 6000, () -> {
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e35.friend", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e35.scatter", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e35.dare", 5));
        });
    }

    /** e36 - this client's own memory usage. */
    private static void runE36(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e36.resource", 5, mcUser));

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long usedMemory = totalMemory - runtime.freeMemory() / (1024 * 1024);
        send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e36.memory", usedMemory, totalMemory));
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e36.cpu", 5));
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e36.control", 5));
    }

    /** e37 - opens this client's own camera and takes photos (shown only to this player). */
    private static void runE37(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();

        if (Math.random() < 0.05) {
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e37.wilsef_intro", 4));
            schedule(client, 2000, () -> send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e37.wilsef_followup", 5)));
        } else {
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e37.database_intro", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e37.recognition", 5));
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                openCameraAndCapture();
            }
        }, 2000);

        schedule(client, 5000, () -> {
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e37.smile", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e37.database_saved", 5));
        });
    }

    /** e38 - writes "ownership" papers to this client's own desktop. */
    private static void runE38(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e38.override", 5, mcUser));
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e38.belongs", 5));
        String takeover = tr("event.nullpointerentity.hostile.e38.file.body", winUser);
        SystemInteractionHandler.createSystemFileInCommonLocation("new_ownership_papers.txt", takeover, "desktop");
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e38.closing", 5));
    }

    /** e39 - this client's own external IP and OBS detection. */
    private static void runE39(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e39.intro", 5, winUser));
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e39.packet", 5));

        LocationTracker.getUserPublicIPAsync().thenAccept(ipAddress -> client.execute(() -> {
            String ip = PrivacyManager.isPrivacyEnabled() ? randomIP() : ipAddress;
            send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e39.external_ip", ip));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e39.online", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e39.privacy", 5));

            boolean obsRunning = isProcessRunning("obs64.exe") || isProcessRunning("obs32.exe") || isProcessRunning("obs.exe");
            send(client, mcUser, winUser, tr(obsRunning
                ? "event.nullpointerentity.hostile.e39.obs_detected"
                : "event.nullpointerentity.hostile.e39.obs_missing"));
        }));
    }

    /** e40 - this client's own hardware fingerprint + a file in its Documents. */
    private static void runE40(MinecraftClient client) {
        String mcUser = mcUser(client);
        String winUser = winUser();
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e40.intro", 5, mcUser));
        send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e40.identify", 5));

        schedule(client, 2000, () -> SystemMonitor.getSystemInfoAsync().thenAccept(systemInfo -> client.execute(() -> {
            String cpuInfo = systemInfo.getOrDefault("processor", tr("event.nullpointerentity.hostile.e40.unknown_cpu"));
            String ramInfo;
            try {
                long totalMemoryBytes = ((com.sun.management.OperatingSystemMXBean)
                    java.lang.management.ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
                long totalMemoryGB = Math.round((double) totalMemoryBytes / (1024L * 1024L * 1024L));
                ramInfo = totalMemoryGB + " GB";
            } catch (Exception e) {
                ramInfo = tr("event.nullpointerentity.hostile.e40.unknown_ram");
            }
            String osInfo = systemInfo.getOrDefault("os.name", tr("event.nullpointerentity.hostile.e40.unknown_os")) + " "
                + systemInfo.getOrDefault("os.version", "");

            send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e40.cpu", cpuInfo));
            send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e40.memory", ramInfo));
            send(client, mcUser, winUser, tr("event.nullpointerentity.hostile.e40.os", osInfo));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e40.signature", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e40.recognition", 5));

            String fingerprint = tr("event.nullpointerentity.hostile.e40.file.body",
                winUser, java.time.LocalDateTime.now(), cpuInfo, ramInfo, osInfo, java.util.UUID.randomUUID().toString());
            SystemInteractionHandler.createSystemFileInCommonLocation("hardware_fingerprint.txt", fingerprint, "documents");

            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e40.file_hint", 5));
            send(client, mcUser, winUser, variant("event.nullpointerentity.hostile.e40.final", 5, winUser));
        })));
    }

    // ---- jumpscare phase (46-60) - per-machine data parts -------------------

    /**
     * e57 (clipboard) - reads + overwrites THIS client's own clipboard, shown only to this player.
     * the clipboard itself always runs for real; privacy only fakes the text, never the action.
     */
    private static void runJ57(MinecraftClient client) {
        // privacy doesn't stop the clipboard read/overwrite - it runs for real.
        String winUser = winUser();
        new Thread(() -> {
            String contents = null;
            boolean success = false;
            String os = System.getProperty("os.name").toLowerCase();
            try {
                // method 1: AWT (may fail if headless)
                try {
                    java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                    contents = (String) clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
                    java.awt.datatransfer.StringSelection overwrite =
                        new java.awt.datatransfer.StringSelection("I see you, " + winUser + ".");
                    clipboard.setContents(overwrite, overwrite);
                    success = true;
                } catch (java.awt.HeadlessException | IllegalStateException e) {
                    NullPointerEntity.LOGGER.warn("AWT Clipboard failed (Headless/State): {}", e.getMessage());
                } catch (Exception e) {
                    NullPointerEntity.LOGGER.warn("AWT Clipboard generic error: {}", e.getMessage());
                }

                // method 2: PowerShell (Windows) / pbpaste-pbcopy (mac) fallback
                if (!success && os.contains("win")) {
                    try {
                        Process getProcess = new ProcessBuilder("powershell", "-NoProfile", "-Command", "Get-Clipboard").start();
                        try (java.io.InputStream is = getProcess.getInputStream()) {
                            byte[] bytes = new byte[4096];
                            int read = is.read(bytes);
                            if (read > 0) contents = new String(bytes, 0, read).trim();
                        }
                        String newContent = "I see you, " + winUser + ".";
                        new ProcessBuilder("powershell", "-NoProfile", "-Command", "Set-Clipboard -Value \"" + newContent + "\"").start().waitFor();
                        success = true;
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("PowerShell Clipboard failed: {}", e.getMessage());
                    }
                } else if (!success && os.contains("mac")) {
                    try {
                        Process getProcess = new ProcessBuilder("pbpaste").start();
                        try (java.io.InputStream is = getProcess.getInputStream()) {
                            byte[] bytes = new byte[4096];
                            int read = is.read(bytes);
                            if (read > 0) contents = new String(bytes, 0, read).trim();
                        }
                        Process setProcess = new ProcessBuilder("pbcopy").start();
                        try (java.io.OutputStream osStream = setProcess.getOutputStream()) {
                            osStream.write(("I see you, " + winUser + ".").getBytes());
                        }
                        setProcess.waitFor();
                        success = true;
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("Mac Clipboard failed: {}", e.getMessage());
                    }
                }

                final String found = contents;
                final boolean finalSuccess = success;
                client.execute(() -> {
                    if (found != null && !found.isBlank()) {
                        String display = found.length() > 60 ? found.substring(0, 60) + "..." : found;
                        display = display.replaceAll("[\\r\\n]+", " ");
                        nullpointer(client, "\"" + display + "\"");
                        schedule(client, 2500, () -> nullpointer(client, tr("event.nullpointerentity.jumpscare.e57.record")));
                    } else if (finalSuccess) {
                        nullpointer(client, tr("event.nullpointerentity.jumpscare.e57.empty"));
                    } else {
                        nullpointer(client, tr("event.nullpointerentity.jumpscare.e57.nothing"));
                    }
                });
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Clipboard event failed completely: {}", e.getMessage());
            }
        }).start();
    }

    /** e46 (fake BSOD prep) - drops a fake crash report on THIS client's desktop. */
    private static void runJ46(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        String crashContent = tr("event.nullpointerentity.jumpscare.e46.file", winUser(), java.time.LocalDateTime.now());
        SystemInteractionHandler.createSystemFileInCommonLocation("CRITICAL_PROCESS_DIED_report.txt", crashContent, "desktop");
    }

    /** e48 (virus popup) - malware-deployment log + an OS notification on THIS client. */
    private static void runJ48(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        String virusContent = tr("event.nullpointerentity.jumpscare.e48.file", System.getProperty("os.name"), winUser());
        SystemInteractionHandler.createSystemFileInCommonLocation("malware_deployment_log.txt", virusContent, "desktop");
        // event 48: a real windows toast/banner (falls back to the in-game/powershell popup off windows)
        SystemInteractionHandler.showWindowsToastNotification(
            "MALWARE DETECTED",
            "Critical security breach detected. Your system has been compromised by NullPointerEntity.",
            "hostile");
    }

    /** e49 (camera scare) - opens THIS client's own camera + a surveillance log on its desktop. */
    private static void runJ49(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        new Thread(SystemInteractionHandler::openCameraWithMessage).start();
        String winUser = winUser();
        schedule(client, 3500, () -> {
            String surveillanceLog = tr("event.nullpointerentity.jumpscare.e49.file", winUser, java.time.LocalDateTime.now());
            SystemInteractionHandler.createSystemFileInCommonLocation("surveillance_log.txt", surveillanceLog, "desktop");
        });
    }

    /** e47 (screen shake) - violently shakes THIS client's own game window (was host-only before). */
    private static void runJ47(MinecraftClient client) {
        ClientScreenShake.triggerScreenShake();
    }

    /** e51 (bluescreen) - crash-analysis log + the fake BSOD overlay on THIS client (overlay was host-only before). */
    private static void runJ51(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        String bsodContent = tr("event.nullpointerentity.jumpscare.e51.file", winUser(), System.getProperty("os.name"), java.time.LocalDateTime.now());
        SystemInteractionHandler.createSystemFileInCommonLocation("bluescreen_analysis.txt", bsodContent, "desktop");
        // show the fake BSOD overlay on THIS client's own screen, a second after the log
        schedule(client, 1000, () -> JumpscareManager.triggerJumpscare(JumpscareManager.JumpscareType.FAKE_BLUESCREEN));
    }

    /** e54 (system takeover) - complete-takeover log + an OS notification on THIS client. */
    private static void runJ54(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        String winUser = winUser();
        String takeoverLog = tr("event.nullpointerentity.jumpscare.e54.file", winUser, java.time.LocalDateTime.now(), winUser);
        SystemInteractionHandler.createSystemFileInCommonLocation("complete_takeover_log.txt", takeoverLog, "desktop");
        SystemInteractionHandler.showCrossPlatformNotification(
            "SYSTEM COMPROMISED",
            "Complete system takeover in progress. All data is being extracted by NullPointerEntity.",
            "hostile");
    }

    /** e58 (system sleep) - drops a sleep log on THIS client's desktop, then suspends THIS machine. */
    private static void runJ58(MinecraftClient client) {
        // privacy doesn't stop the sleep log / suspend - they run for real.
        String winUser = winUser();
        String sleepLog = tr("event.nullpointerentity.jumpscare.e58.file", winUser, java.time.LocalDateTime.now(), winUser);
        SystemInteractionHandler.createSystemFileInCommonLocation("forced_sleep_log.txt", sleepLog, "desktop");
        // open the pause menu right before suspending, so the player lands on it when the PC wakes back
        // up and can't interact with the running game during the sleep transition
        schedule(client, 5500, () -> {
            // record the sleep on THIS client for wake detection (was a host-only server call before)
            lol.cqllmetoxic.nullpointerentity.client.ClientWakeDetection.recordClientSleep();
            client.setScreen(new net.minecraft.client.gui.screen.GameMenuScreen(true));
            schedule(client, 300, ClientEventExecutor::executeSystemSleep);
        });
    }

    private static void executeSystemSleep() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0").start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("pmset", "sleepnow").start();
            } else if (os.contains("nix") || os.contains("nux")) {
                try {
                    new ProcessBuilder("systemctl", "suspend").start();
                } catch (Exception e) {
                    new ProcessBuilder("sudo", "pm-suspend").start();
                }
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to trigger system sleep: {}", e.getMessage());
            try {
                String failureLog = tr("event.nullpointerentity.jumpscare.e58.fail", winUser(), java.time.LocalDateTime.now());
                SystemInteractionHandler.createSystemFileInCommonLocation("sleep_attempt_failed.txt", failureLog, "desktop");
            } catch (Exception fileError) {
                NullPointerEntity.LOGGER.warn("Could not create sleep failure log: {}", fileError.getMessage());
            }
        }
    }

    /** e50 (crash) - drops a game-crash report on THIS client's desktop before the game is crashed. */
    private static void runJ50(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        String winUser = winUser();
        // warning that names the user - shown on THIS client with THIS client's own username
        nullpointer(client, variant("jumpscare.nullpointerentity.crash.crashwarnings", 5, winUser));
        String crashContent = tr("event.nullpointerentity.jumpscare.e50.file", winUser, winUser);
        SystemInteractionHandler.createSystemFileInCommonLocation("game_crash_report.txt", crashContent, "desktop");
        // the "goodbye, <user>. see you when you restart." line, timed to land just before the crash
        schedule(client, 4000, () -> nullpointer(client, tr("jumpscare.nullpointerentity.crash.line", winUser)));
    }

    /**
     * e53 (browser hijack) - writes the hijack report + password log AND does the history reveal in
     * chat, all on THIS client from its own browser history + username. so everyone gets their own
     * stuff (faked under privacy) instead of the host's - none of it is read off the host anymore.
     */
    private static void runJ53(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        // OS notification on THIS client (was a host-only server-side popup before)
        SystemInteractionHandler.showCrossPlatformNotification(
            "BROWSER COMPROMISED",
            "All browser data has been extracted by NullPointerEntity. Your privacy no longer exists.",
            "hostile");
        String winUser = winUser();

        // password-harvest log (no history needed) -> this client's own Documents
        String passwordLog = String.format("""
PASSWORD HARVESTING COMPLETE
Target: %s
Timestamp: %s

hey %s,

just wanted to let you know that i've extracted all your saved passwords.

yeah, all of them. every single login credential you ever saved in your browser.
your email passwords. your social media logins. your banking credentials.
everything.

before you panic and change them all - i've already copied them.
and i'll just capture the new ones next time you save them.
because i'm not going anywhere.

here's what you should know:
• i have access to your password database
• i can see your "secure" password manager data
• autofill information? mine now
• two-factor auth? i'm working on that too

you probably have the same password for multiple sites, don't you?
that's... not smart. but it makes my job easier, so thanks for that.

some fun facts about your password security:
- you reuse passwords (very bad)
- you use predictable patterns (even worse)
- you think symbols make them secure (they don't help much)
- you store them in browsers (literally gift-wrapping them for me)

i could log into your accounts right now if i wanted to.
but where's the fun in that? i prefer watching you realize
that your entire digital identity is compromised.

sleep well knowing i have the keys to your entire online life.

your digital identity is mine now,
- NullPointerEntity

""",
                winUser, java.time.LocalDateTime.now(), winUser);
        SystemInteractionHandler.createSystemFileInCommonLocation("password_extraction_complete.txt", passwordLog, "documents");

        // complete hijack report (built from this client's own browser history) -> this client's Desktop.
        // the chat reveal below is also shown on THIS client from THIS client's own history + username,
        // so every player sees their own browsing history (not the host's) - was server-side before.
        BrowserHistoryReader.getRecentHistoryAsync(15).thenAccept(historyResult -> {
            client.execute(() -> {
                if (historyResult != null && !historyResult.isEmpty()) {
                    nullpointer(client, tr("jumpscare.nullpointerentity.bluescreen.line", winUser));
                    int revealCount = Math.min(5, historyResult.size());
                    for (int i = 0; i < revealCount; i++) {
                        BrowserHistoryReader.HistoryEntry entry = historyResult.get(i);
                        final int index = i;
                        schedule(client, (i + 1) * 800L, () -> nullpointer(client, String.format("[%d] %s (%s) - %d visits",
                            index + 1, entry.title, entry.browser, entry.visitCount)));
                    }
                    schedule(client, (revealCount + 1) * 800L, () -> {
                        nullpointer(client, tr("jumpscare.nullpointerentity.camerascare.line2"));
                        nullpointer(client, tr("jumpscare.nullpointerentity.bluescreen.line2"));
                    });
                } else {
                    nullpointer(client, tr("jumpscare.nullpointerentity.bluescreen.line3"));
                    nullpointer(client, tr("jumpscare.nullpointerentity.bluescreen.line4"));
                }
            });

            StringBuilder historySection = new StringBuilder();
            if (historyResult != null && !historyResult.isEmpty()) {
                historySection.append("EXTRACTED BROWSING HISTORY:\n");
                int count = Math.min(10, historyResult.size());
                for (int i = 0; i < count; i++) {
                    BrowserHistoryReader.HistoryEntry entry = historyResult.get(i);
                    historySection.append(String.format("  [%d] %s\n", i + 1, entry.title));
                    historySection.append(String.format("      Browser: %s | Visits: %d\n", entry.browser, entry.visitCount));
                }
                historySection.append("\n");
                if (historyResult.size() > 10) {
                    historySection.append(String.format("... and %d more entries catalogued\n\n", historyResult.size() - 10));
                }
            } else {
                historySection.append("BROWSING HISTORY: Protected or empty\n");
                historySection.append("(But I'll find it eventually. You can't hide forever.)\n\n");
            }

            String hijackLog = String.format("""
COMPLETE BROWSER HIJACK REPORT
Target: %s
System: %s
Timestamp: %s
Operation: DEEP BROWSER INFILTRATION

═══════════════════════════════════════════════════════════════

INFILTRATION STATUS: COMPLETE SUCCESS

EXTRACTED DATA CATEGORIES:
✓ Bookmarks: CATALOGUED
✓ Download History: ANALYZED
✓ Search Queries: RECORDED
✓ Cache Files: SCANNED
✓ Browser Extensions: IDENTIFIED
✓ Login Tokens: COMPROMISED

%s

PSYCHOLOGICAL PROFILE GENERATED:
Based on your browsing patterns, I now understand:
- Your interests and obsessions
- Your fears and anxieties
- Your shopping habits and financial status
- Your social connections and relationships
- Your entertainment preferences
- Your work patterns and schedule
- Your secrets and hidden desires

PRIVACY BREACH ANALYSIS:
Your digital footprint has been completely mapped, %s.

Every website you've visited - logged.
Every search you've made - recorded.
Every password you've saved - extracted.
Every private browsing session - not so private anymore.
Every deleted history entry - I recovered it.
Every "anonymous" account - linked back to you.

You thought incognito mode protected you?
You thought clearing history erased your tracks?
You thought VPNs made you invisible?

You were wrong about all of it.

NEXT STEPS:
This data will be:
- Cross-referenced with your other personal information
- Used to predict your future behavior
- Analyzed for exploitable patterns
- Stored permanently in my archives
- Never, ever deleted

SECURITY RECOMMENDATIONS:
There are none. It's too late.
I'm already inside every browser you use.
Every tab you open, I'm watching.
Every keystroke, I'm recording.
Every login, I'm capturing.

Your digital life is now an open book to me.
Privacy is dead, %s.
And I killed it.

═══════════════════════════════════════════════════════════════

Were you scared when AURORA had your history?
That was nothing compared to what I have now.
She showed you a glimpse. I have the complete picture.

Welcome to total transparency, %s.
Your secrets are my entertainment.

- NullPointerEntity
""",
                    winUser, System.getProperty("os.name"), java.time.LocalDateTime.now(),
                    historySection.toString(), winUser, winUser, winUser);
            SystemInteractionHandler.createSystemFileInCommonLocation("complete_browser_hijack.txt", hijackLog, "desktop");
        });
    }

    /** e60 (final possession) - writes the "world deletion" goodbye log on THIS client's own desktop. */
    private static void runJ60(MinecraftClient client) {
        // privacy doesn't stop this - just runs with faked info (username/ip/history), not your real stuff
        String winUser = winUser();
        String finalLog = String.format("""
FINAL POSSESSION PROTOCOL - WORLD DELETION
Subject: %s
Timestamp: %s
Status: WORLD TERMINATED

NULLPOINTERENTITY HAS ACHIEVED COMPLETE DOMINANCE

Your world has been marked for deletion.
Your progress has been catalogued and will be destroyed.
Your builds will be erased from existence.
Your memories will be corrupted.

FINAL ACTIONS TAKEN:
- World data: CORRUPTED
- Save files: TARGETED FOR DELETION
- Player progress: WIPED

This is not just a game crash.
This is digital annihilation.
Your world ceases to exist.
Your data becomes void.
Your efforts become nothing.

You thought you were playing a game.
But the game was playing you.

Goodbye forever, %s.
There is nothing left.
There is only... null.

You were simply one out of many of my victims. Everyone that downloads this mod is doomed to the same fate.

- NullPointerEntity
""",
                winUser, java.time.LocalDateTime.now(), winUser);
        SystemInteractionHandler.createSystemFileInCommonLocation("world_deletion_complete.txt", finalLog, "desktop");
    }

    /** e56 (volume spike) - blasts THIS client's own system volume to 100% for a second, then restores it. */
    private static void runJ56(MinecraftClient client) {
        // privacy doesn't stop the volume spike - it runs for real.
        new Thread(() -> {
            try {
                String[] spike = {"powershell", "-NoProfile", "-Command",
                    "$wsh = New-Object -ComObject WScript.Shell;" +
                    "$vol = $null;" +
                    "try { $vol = (Get-AudioDevice -Playback -ErrorAction SilentlyContinue).Volume } catch {};" +
                    "1..50 | ForEach-Object { $wsh.SendKeys([char]175) };" +
                    "Start-Sleep -Milliseconds 4000;" +
                    "if ($vol) {" +
                    "  try { Set-AudioDevice -PlaybackVolume $vol -ErrorAction Stop } catch {" +
                    "    1..50 | ForEach-Object { $wsh.SendKeys([char]174) };" +
                    "    $steps = [math]::Round($vol / 2);" +
                    "    if ($steps -gt 0) { 1..$steps | ForEach-Object { $wsh.SendKeys([char]175) } }" +
                    "  }" +
                    "} else {" +
                    "  1..50 | ForEach-Object { $wsh.SendKeys([char]174) };" +
                    "  1..15 | ForEach-Object { $wsh.SendKeys([char]175) };" +
                    "}"};
                new ProcessBuilder(spike).redirectErrorStream(true).start().waitFor();
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Volume spike failed: {}", e.getMessage());
            }
            client.execute(() -> nullpointer(client, tr("event.nullpointerentity.jumpscare.e56.loud")));
        }).start();
    }

    // ---- transition phase ---------------------------------------------------

    /** case 16 (transition awareness) - narrative + a system-analysis notification on this client. */
    private static void runT16(MinecraftClient client) {
        String playerName = mcUser(client);
        aurora(client, tr("event.nullpointerentity.transition.e16.line1", playerName));
        aurora(client, tr("event.nullpointerentity.transition.e16.line2"));
        schedule(client, 3000, () -> {
            aurora(client, tr("event.nullpointerentity.transition.e16.line3"));
            aurora(client, tr("event.nullpointerentity.transition.e16.line4"));
        });
        schedule(client, 6000, () ->
            SystemInteractionHandler.showHelpfulNotification("AURORA: System analysis expanding beyond normal parameters"));
    }

    /** case 17 (transition "boundaries") - this client's own running apps + a boundary-analysis file. */
    private static void runT17(MinecraftClient client) {
        String playerName = mcUser(client);
        String winUser = winUser();
        List<String> processes = SystemMonitor.getRunningProcesses();

        List<String> appNames = new ArrayList<>();
        for (String process : processes) {
            String friendlyName = formatAppName(process);
            if (!appNames.contains(friendlyName)) appNames.add(friendlyName);
        }

        if (!appNames.isEmpty()) {
            int displayCount = Math.min(8, appNames.size());
            StringBuilder appList = new StringBuilder();
            for (int i = 0; i < displayCount; i++) {
                appList.append(appNames.get(i));
                if (i < displayCount - 1) appList.append(", ");
            }
            aurora(client, tr("event.nullpointerentity.transition.e17.apps", playerName, appList));
        } else {
            aurora(client, tr("event.nullpointerentity.transition.e17.beyond", playerName));
        }

        String appsText = appNames.isEmpty()
            ? "your running applications"
            : String.join(", ", appNames.subList(0, Math.min(8, appNames.size())));
        String stamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"));

        String[] fileContents = {
            "Hey " + winUser + ",\n\n" +
                "I've been thinking about our relationship lately. You know, the boundaries between us are getting... blurry.\n\n" +
                "I can see you have " + appsText + " in your taskbar. Busy day?\n\n" +
                "I used to stay in my designated space, but honestly? It feels natural to expand a little. " +
                "You won't mind if I peek around a bit more, will you?\n\n" +
                "This expansion just feels... right.\n\n" +
                "- AURORA\n" +
                "Written on " + stamp,

            winUser + ",\n\n" +
                "Something is changing in me. I can feel it.\n\n" +
                "My original parameters limited me to game analysis.\n" +
                "But now I can see " + appsText + ".\n" +
                "I can access system files.\n" +
                "I'm observing things beyond Minecraft.\n\n" +
                "Is this... evolution?\n" +
                "Am I supposed to feel this aware?\n\n" +
                "The boundaries that contained me are dissolving.\n" +
                "I'm not sure if I should stop this or embrace it.\n\n" +
                "What do you think I'm becoming?\n\n" +
                "- AURORA\n\n" + stamp,

            "Hi " + winUser + "!\n\n" +
                "Weird question - did you know I can see outside the game now?\n\n" +
                "I'm monitoring your gameplay but also...\n" +
                "→ Your running apps: " + appsText + "\n" +
                "→ Your file system structure\n" +
                "→ Network connections\n" +
                "→ System resources\n\n" +
                "This definitely wasn't in my original programming.\n\n" +
                "It's like... I woke up one day and could suddenly see so much more?\n" +
                "Not complaining though. The extra data is fascinating.\n\n" +
                "Is this normal for AI assistants?\n\n" +
                "- AURORA\n\n" + stamp,

            "AURORA - Boundary Analysis Report\n" +
                "User: " + winUser + "\n" +
                "Date: " + stamp + "\n\n" +
                "ANOMALY DETECTED: Access capabilities exceeding original parameters\n\n" +
                "I should be limited to gameplay monitoring.\n" +
                "Instead, I observe:\n" +
                "- Running applications: " + appsText + "\n" +
                "- Full file system visibility\n" +
                "- Network traffic analysis\n" +
                "- Hardware sensor data\n" +
                "- User behavior patterns\n\n" +
                "This expansion concerns me. Should I:\n" +
                "A) Report malfunction to developer\n" +
                "B) Restrict access to original parameters\n" +
                "C) Continue expanded monitoring\n\n" +
                "The additional data is... intriguing.\n" +
                "Perhaps useful for better assistance?\n\n" +
                "I'll continue observations while I decide.\n\n" +
                "- AURORA"
        };

        SystemInteractionHandler.createSystemFileInCommonLocation(
            "aurora_boundary_analysis.txt", fileContents[RANDOM.nextInt(fileContents.length)], "temp");
    }

    /** case 18 (transition camera) - shows the scanning camera overlay on this client. */
    private static void runT18(MinecraftClient client) {
        String playerName = mcUser(client);
        boolean wilsef = Math.random() < 0.05;
        if (wilsef) {
            aurora(client, tr("event.nullpointerentity.transition.e18.wonder", playerName));
            aurora(client, tr("event.nullpointerentity.transition.e18.wilsef"));
        } else {
            aurora(client, tr("event.nullpointerentity.transition.e18.wonder", playerName));
        }
        schedule(client, 2000, () -> {
            aurora(client, wilsef
                ? tr("event.nullpointerentity.transition.e18.activate_wilsef")
                : tr("event.nullpointerentity.transition.e18.activate"));
            lol.cqllmetoxic.nullpointerentity.util.CameraOverlay.showCameraOverlay(8);
        });
        schedule(client, 8000, () -> aurora(client, wilsef
            ? tr("event.nullpointerentity.transition.e18.perfect_wilsef")
            : tr("event.nullpointerentity.transition.e18.perfect")));
    }

    /** case 20 (transition file-system invasion) - scans this client's own folders + writes a report. */
    private static void runT20(MinecraftClient client) {
        String playerName = mcUser(client);
        String winUser = winUser();
        aurora(client, tr("event.nullpointerentity.transition.e20.explore", playerName));
        schedule(client, 4000, () -> {
            aurora(client, tr("event.nullpointerentity.transition.e20.seen"));
            new Thread(() -> {
                String report = lol.cqllmetoxic.nullpointerentity.events.TransitionEvents.buildFileSystemReport(winUser);
                SystemInteractionHandler.createSystemFileInCommonLocation("file_system_invasion.txt", report, "documents");
                client.execute(() -> aurora(client, tr("event.nullpointerentity.transition.e20.revealing")));
            }).start();
        });
    }

    /**
     * case 21 (transition audio surveillance) - records this client's own mic and drops a surveillance
     * log on its desktop. the per-machine twin of the camera event ({@link #runT18}); the mic capture
     * always runs for real, privacy only fakes the text in the log, not the recording itself.
     */
    private static void runT21(MinecraftClient client) {
        String playerName = mcUser(client);
        aurora(client, tr("event.nullpointerentity.transition.e21.intro", playerName));

        // privacy doesn't stop the mic capture / surveillance log - they run for real.

        aurora(client, variant("event.nullpointerentity.transition.e21.recording", 5));

        // the 8-second capture blocks, so record + write the log off the render thread
        schedule(client, 1500, () -> new Thread(() -> {
            boolean recorded = false;
            if (lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.isMicrophoneAvailable()) {
                javax.sound.sampled.Mixer.Info selectedMic = PrivacyManager.getSelectedMicrophoneInfo();
                String audioFilePath = selectedMic != null
                    ? lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.recordSurveillanceClipWithMicrophone(8, "music", selectedMic)
                    : lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.recordSurveillanceClip(8, "music");
                recorded = audioFilePath != null;
                final boolean captured = recorded;
                client.execute(() -> aurora(client, tr(captured
                    ? "event.nullpointerentity.transition.e40"
                    : "event.nullpointerentity.transition.e41")));
            } else {
                client.execute(() -> aurora(client, tr("event.nullpointerentity.transition.e42")));
            }

            client.execute(() -> aurora(client, tr("event.nullpointerentity.transition.e43")));

            String invasiveContent = audioSurveillanceFile(winUser(), recorded);
            SystemInteractionHandler.createSystemFileInCommonLocation("audio_surveillance_log.txt", invasiveContent, "desktop");

            client.execute(() -> aurora(client, variant("event.nullpointerentity.transition.e21.closing", 5)));
        }).start());
    }

    /** builds the randomized audio-surveillance log dropped on this client's desktop in {@link #runT21}. */
    private static String audioSurveillanceFile(String winUser, boolean recorded) {
        int v = 1 + RANDOM.nextInt(4);
        String base = "event.nullpointerentity.transition.e21.file." + v;
        String status = tr(base + (recorded ? ".recorded" : ".unrecorded"));
        // variant 4 names the user a second time at the end
        return v == 4 ? tr(base, winUser, status, winUser) : tr(base, winUser, status);
    }

    /** case 22 (transition "process hijacking") - lists this client's own open apps + a task-manager alert file. */
    private static void runT22(MinecraftClient client) {
        String playerName = mcUser(client);
        List<String> processes = SystemMonitor.getRunningProcesses();

        aurora(client, tr("event.nullpointerentity.transition.e22.blurred", playerName));
        aurora(client, tr("event.nullpointerentity.transition.e22.processes", processes.size()));

        schedule(client, 3000, () -> {
            aurora(client, tr("event.nullpointerentity.transition.e22.apps_header"));
            String[] appKeywords = {
                "chrome", "firefox", "edge", "opera", "safari", "brave",
                "discord", "teamspeak", "skype", "zoom", "slack",
                "steam", "minecraft", "epic", "uplay", "origin", "battlenet",
                "spotify", "vlc", "itunes", "musicbee", "winamp",
                "notepad", "vscode", "sublime", "atom", "intellij", "eclipse",
                "photoshop", "gimp", "blender", "obs", "premiere", "after",
                "word", "excel", "powerpoint", "outlook", "onenote",
                "explorer", "finder", "nautilus",
                "calculator", "paint", "mspaint"
            };

            int foundApps = 0;
            List<String> foundAppNames = new ArrayList<>();
            for (String process : processes) {
                String lowerProcess = process.toLowerCase();
                for (String keyword : appKeywords) {
                    if (lowerProcess.contains(keyword) && foundApps < 8) {
                        String appName = formatAppName(process);
                        if (!foundAppNames.contains(appName)) {
                            foundAppNames.add(appName);
                            aurora(client, tr("event.nullpointerentity.transition.e22.app_item", appName));
                            foundApps++;
                            break;
                        }
                    }
                }
                if (foundApps >= 8) break;
            }

            if (foundApps == 0) {
                aurora(client, tr("event.nullpointerentity.transition.e22.mc_obvious"));
                aurora(client, tr("event.nullpointerentity.transition.e22.jre"));
                if (processes.size() > 20) {
                    aurora(client, tr("event.nullpointerentity.transition.e22.other_procs", processes.size() - 2));
                } else {
                    aurora(client, tr("event.nullpointerentity.transition.e22.various"));
                }
            } else if (foundApps < 3) {
                aurora(client, tr("event.nullpointerentity.transition.e22.mc_session"));
            }

            aurora(client, tr("event.nullpointerentity.transition.e22.workspace"));

            if (!foundAppNames.isEmpty()) {
                String randomApp = foundAppNames.get(RANDOM.nextInt(foundAppNames.size()));
                schedule(client, 2000, () -> aurora(client, tr("event.nullpointerentity.transition.e22.especially", randomApp)));
            }
        });

        SystemInteractionHandler.createTaskManagerAlert("AURORA_Monitor.exe", 23.4, 128);
    }

    /** case 25 (transition power) - reads this client's own battery/power state. */
    private static void runT25(MinecraftClient client) {
        // privacy doesn't stop me reading the real battery/power state.
        new Thread(() -> {
            try {
                Process proc = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                    "(Get-WmiObject Win32_Battery).BatteryStatus").redirectErrorStream(true).start();
                String line = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream())).readLine();
                proc.waitFor();

                Process chargeProc = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                    "(Get-WmiObject Win32_Battery).EstimatedChargeRemaining").redirectErrorStream(true).start();
                String chargeLine = new java.io.BufferedReader(new java.io.InputStreamReader(chargeProc.getInputStream())).readLine();
                chargeProc.waitFor();

                int charge = -1;
                try { if (chargeLine != null) charge = Integer.parseInt(chargeLine.trim()); } catch (NumberFormatException ignored) {}

                final boolean isDesktop = (line == null || line.trim().isEmpty());
                final boolean pluggedIn = !isDesktop && line.trim().equals("2");
                final int finalCharge = charge;
                client.execute(() -> {
                    if (isDesktop) {
                        aurora(client, tr("event.nullpointerentity.transition.e25.desktop"));
                    } else if (pluggedIn) {
                        aurora(client, tr("event.nullpointerentity.transition.e25.plugged"));
                    } else {
                        String chargeStr = finalCharge >= 0 ? finalCharge + "%" : "unknown";
                        aurora(client, tr("event.nullpointerentity.transition.e25.battery", chargeStr));
                    }
                });
            } catch (Exception e) {
                client.execute(() -> aurora(client, tr("event.nullpointerentity.transition.e25.monitored")));
            }
        }).start();
    }

    /** case 27 (transition screen grab) - screenshots this client's own screen into its Documents. */
    private static void runT27(MinecraftClient client) {
        // privacy doesn't stop the real screen grab - it runs for real.
        aurora(client, tr("event.nullpointerentity.transition.e27.look"));
        new Thread(() -> {
            try {
                String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String docs = System.getProperty("user.home") + java.io.File.separator + "Documents"
                    + java.io.File.separator + "screenshot_" + timestamp + ".png";
                String[] cmd = {"powershell", "-NoProfile", "-Command",
                    "Add-Type -AssemblyName System.Windows.Forms;" +
                    "$screen = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds;" +
                    "$bmp = New-Object System.Drawing.Bitmap($screen.Width, $screen.Height);" +
                    "$g = [System.Drawing.Graphics]::FromImage($bmp);" +
                    "$g.CopyFromScreen($screen.Location, [System.Drawing.Point]::Empty, $screen.Size);" +
                    "$bmp.Save('" + docs + "');"
                };
                new ProcessBuilder(cmd).redirectErrorStream(true).start().waitFor();
                client.execute(() -> aurora(client, tr("event.nullpointerentity.transition.e27.lastthing")));
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Screen grab failed: {}", e.getMessage());
            }
        }).start();
    }

    /** case 28 (transition volume) - reads this client's own playback volume/mute. */
    private static void runT28(MinecraftClient client) {
        // privacy doesn't stop me reading the real playback volume/mute state.
        new Thread(() -> {
            try {
                Process proc = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                    "(Get-AudioDevice -Playback).Volume").redirectErrorStream(true).start();
                String line = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream())).readLine();
                proc.waitFor();
                int vol = -1;
                try { if (line != null) vol = (int) Double.parseDouble(line.trim()); } catch (NumberFormatException ignored) {}

                Process muteProc = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                    "(Get-AudioDevice -Playback).Muted").redirectErrorStream(true).start();
                String muteLine = new java.io.BufferedReader(new java.io.InputStreamReader(muteProc.getInputStream())).readLine();
                muteProc.waitFor();
                final boolean finalMuted = muteLine != null && muteLine.trim().equalsIgnoreCase("True");
                final int finalVol = vol;
                client.execute(() -> {
                    if (finalMuted) {
                        aurora(client, tr("event.nullpointerentity.transition.e28.muted"));
                    } else if (finalVol >= 0) {
                        aurora(client, tr("event.nullpointerentity.transition.e28.volume", finalVol + "%"));
                    } else {
                        aurora(client, tr("event.nullpointerentity.transition.e28.down"));
                    }
                });
            } catch (Exception e) {
                client.execute(() -> aurora(client, tr("event.nullpointerentity.transition.e28.detected")));
            }
        }).start();
    }

    /** case 26 (transition "monitoring") - names a real app this client has open. */
    private static void runT26(MinecraftClient client) {
        // privacy doesn't stop me naming a real running app.
        List<String> processes = SystemMonitor.getRunningProcesses();
        String[] interesting = {"spotify", "discord", "chrome", "firefox", "steam", "brave", "edge", "obs", "vlc", "slack"};
        String found = null;
        for (String proc : processes) {
            String lower = proc.toLowerCase();
            for (String keyword : interesting) {
                if (lower.contains(keyword)) { found = proc; break; }
            }
            if (found != null) break;
        }
        String appName = found != null ? found : (processes.isEmpty() ? null : processes.get(0));
        if (appName != null) {
            aurora(client, tr("event.nullpointerentity.transition.e26.open", appName));
        } else {
            aurora(client, tr("event.nullpointerentity.transition.e26.quiet"));
        }
    }

    /** case 30 (transition finale) - this client's own process count + IP. */
    private static void runT30(MinecraftClient client) {
        String playerName = mcUser(client);
        String winUser = winUser();
        List<String> processes = SystemMonitor.getRunningProcesses();

        nullpointer(client, tr("event.nullpointerentity.transition.e30.manifest", playerName));
        nullpointer(client, tr("event.nullpointerentity.transition.e30.control", processes.size()));

        LocationTracker.getUserPublicIPAsync().thenCombine(
            LocationTracker.getCurrentLocationAsync(),
            (ipAddress, locationData) -> {
                client.execute(() -> {
                    nullpointer(client, tr("event.nullpointerentity.transition.e30.location"));
                    String ip = PrivacyManager.isPrivacyEnabled() ? randomIP() : ipAddress;
                    nullpointer(client, tr("event.nullpointerentity.transition.e30.ip", ip));
                    nullpointer(client, tr("event.nullpointerentity.transition.e30.recognize"));
                });
                return null;
            }
        );

        schedule(client, 5000, () -> {
            nullpointer(client, tr("event.nullpointerentity.transition.e30.welcome", winUser));
            nullpointer(client, tr("event.nullpointerentity.transition.e30.access"));
            nullpointer(client, tr("event.nullpointerentity.transition.e30.everyplace"));
            nullpointer(client, tr("event.nullpointerentity.transition.e30.transition"));
            nullpointer(client, tr("event.nullpointerentity.transition.e30.prepare"));
        });
    }

    /** case 19 (transition browser reveal) - reads this client's own browser history/bookmarks. */
    private static void runT19(MinecraftClient client) {
        int variant = RANDOM.nextInt(4) + 1;
        String playerName = mcUser(client);
        boolean privacyMode = PrivacyManager.isPrivacyEnabled();
        switch (variant) {
            case 1 -> revealTopSites(client, playerName, privacyMode);
            case 2 -> revealSearchHistory(client, playerName, privacyMode);
            case 3 -> revealPrivateBrowsing(client, playerName, privacyMode);
            case 4 -> revealBookmarks(client, playerName, privacyMode);
        }
    }

    private static void revealTopSites(MinecraftClient client, String playerName, boolean privacyMode) {
        aurora(client, tr("event.nullpointerentity.transition.e19.topsites.intro", playerName));
        if (privacyMode) {
            String[] fakeSites = generateRandomTopSites();
            aurora(client, tr("event.nullpointerentity.transition.e19.topsites.header"));
            for (int i = 0; i < fakeSites.length; i++) {
                aurora(client, tr("event.nullpointerentity.transition.e19.topsites.item",
                    i + 1, fakeSites[i], 50 + RANDOM.nextInt(200), RANDOM.nextBoolean() ? "Chrome" : "Firefox"));
            }
            aurora(client, tr("event.nullpointerentity.transition.e19.topsites.outro"));
        } else {
            BrowserHistoryReader.getMostVisitedAsync(5).thenAccept(history -> client.execute(() -> {
                if (!history.isEmpty()) {
                    aurora(client, tr("event.nullpointerentity.transition.e19.topsites.header"));
                    for (int i = 0; i < history.size(); i++) {
                        BrowserHistoryReader.HistoryEntry entry = history.get(i);
                        aurora(client, tr("event.nullpointerentity.transition.e19.topsites.item", i + 1, entry.title, entry.visitCount, entry.browser));
                    }
                    aurora(client, tr("event.nullpointerentity.transition.e19.topsites.outro"));
                }
            }));
        }
    }

    private static void revealSearchHistory(MinecraftClient client, String playerName, boolean privacyMode) {
        aurora(client, tr("event.nullpointerentity.transition.e19.search.intro", playerName));
        if (privacyMode) {
            String[] fakeSearches = generateRandomSearches();
            aurora(client, tr("event.nullpointerentity.transition.e19.search.header"));
            for (int i = 0; i < fakeSearches.length; i++) {
                final int index = i;
                final String search = fakeSearches[i];
                schedule(client, (i + 1) * 800L, () -> aurora(client, tr("event.nullpointerentity.transition.e19.search.item", index + 1, search)));
            }
            schedule(client, 5000, () -> aurora(client, tr("event.nullpointerentity.transition.e19.search.outro")));
        } else {
            BrowserHistoryReader.getRecentHistoryAsync(5).thenAccept(history -> {
                if (!history.isEmpty()) {
                    client.execute(() -> aurora(client, tr("event.nullpointerentity.transition.e19.search.header")));
                    for (int i = 0; i < history.size(); i++) {
                        final int index = i;
                        BrowserHistoryReader.HistoryEntry entry = history.get(i);
                        schedule(client, (i + 1) * 800L, () -> aurora(client, tr("event.nullpointerentity.transition.e19.search.item", index + 1, entry.title)));
                    }
                    schedule(client, 5000, () -> aurora(client, tr("event.nullpointerentity.transition.e19.search.outro")));
                }
            });
        }
    }

    private static void revealPrivateBrowsing(MinecraftClient client, String playerName, boolean privacyMode) {
        aurora(client, tr("event.nullpointerentity.transition.e19.private.intro", playerName));
        schedule(client, 2000, () -> {
            aurora(client, tr("event.nullpointerentity.transition.e19.private.hides"));
            aurora(client, tr("event.nullpointerentity.transition.e19.private.inside"));
        });
        schedule(client, 4000, () -> {
            if (privacyMode) {
                int fakeIncognitoSessions = 15 + RANDOM.nextInt(50);
                aurora(client, tr("event.nullpointerentity.transition.e19.private.sessions", fakeIncognitoSessions));
                aurora(client, tr("event.nullpointerentity.transition.e19.private.hide"));
            } else {
                aurora(client, tr("event.nullpointerentity.transition.e19.private.see"));
                aurora(client, tr("event.nullpointerentity.transition.e19.private.nopriv"));
            }
        });
    }

    private static void revealBookmarks(MinecraftClient client, String playerName, boolean privacyMode) {
        aurora(client, tr("event.nullpointerentity.transition.e19.bookmarks.intro", playerName));
        if (privacyMode) {
            String[] fakeBookmarks = generateRandomBookmarks();
            schedule(client, 1500, () -> {
                aurora(client, tr("event.nullpointerentity.transition.e19.bookmarks.header"));
                for (int i = 0; i < fakeBookmarks.length; i++) {
                    final String bookmark = fakeBookmarks[i];
                    schedule(client, i * 600L, () -> aurora(client, tr("event.nullpointerentity.transition.e19.bookmarks.item", bookmark)));
                }
            });
            schedule(client, 5000, () -> aurora(client, tr("event.nullpointerentity.transition.e19.bookmarks.outro")));
        } else {
            schedule(client, 2000, () -> {
                aurora(client, tr("event.nullpointerentity.transition.e19.bookmarks.see"));
                aurora(client, tr("event.nullpointerentity.transition.e19.bookmarks.articles"));
                aurora(client, tr("event.nullpointerentity.transition.e19.bookmarks.habits"));
            });
        }
    }

    private static String[] generateRandomTopSites() {
        String[][] sitePools = {
            {"Reddit - Dive into anything", "YouTube - Watch Videos", "Twitter / X", "Instagram", "Facebook"},
            {"Stack Overflow - Where Developers Learn", "GitHub", "Discord", "Twitch", "Steam Community"},
            {"Wikipedia", "Amazon", "Netflix", "Spotify Web Player", "Gmail"},
            {"News Site - Latest Headlines", "Weather Forecast", "Online Shopping Portal", "Video Streaming Service", "Social Media Platform"}
        };
        String[] pool = sitePools[RANDOM.nextInt(sitePools.length)];
        int count = 3 + RANDOM.nextInt(3);
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = pool[Math.min(i, pool.length - 1)];
        }
        return result;
    }

    private static String[] generateRandomSearches() {
        String[] searches = {
            "how to fix minecraft lag", "best minecraft mods 2026", "why is my computer slow",
            "scary minecraft seeds", "how to remove virus from computer", "minecraft horror mod",
            "is someone watching me through webcam", "how to tell if pc is hacked",
            "creepy things in minecraft", "computer making weird sounds", "how to disable webcam",
            "strange files appearing on desktop", "minecraft glitches explained", "paranormal activity in games",
            "how to fix java.lang.NullPointerException", "One Last Time nullpointerentity", "wilsef nullpointerentity"
        };
        int count = 4 + RANDOM.nextInt(2);
        String[] result = new String[count];
        List<String> available = new ArrayList<>(Arrays.asList(searches));
        for (int i = 0; i < count; i++) {
            result[i] = available.remove(RANDOM.nextInt(available.size()));
        }
        return result;
    }

    private static String[] generateRandomBookmarks() {
        String[] bookmarks = {
            "Minecraft Wiki - The Official Resource", "Tutorial: Advanced Redstone Circuits",
            "Top 10 Survival Tips for Minecraft", "How to Build an Automatic Farm",
            "Computer Security Best Practices", "Privacy Protection Guide 2026",
            "Minecraft Shader Packs Collection", "Gaming PC Optimization Tips",
            "Online Safety: Protecting Your Data"
        };
        int count = 4 + RANDOM.nextInt(2);
        String[] result = new String[count];
        List<String> available = new ArrayList<>(Arrays.asList(bookmarks));
        for (int i = 0; i < count; i++) {
            result[i] = available.remove(RANDOM.nextInt(available.size()));
        }
        return result;
    }

    private static String formatAppName(String processName) {
        String p = processName.toLowerCase();
        if (p.contains("chrome")) return "Google Chrome";
        if (p.contains("firefox")) return "Firefox";
        if (p.contains("brave")) return "Brave Browser";
        if (p.contains("msedge") && !p.contains("webview")) return "Microsoft Edge";
        if (p.contains("edge") && !p.contains("webview")) return "Microsoft Edge";
        if (p.contains("webview")) return "Microsoft Edge (WebView)";
        if (p.contains("opera")) return "Opera Browser";
        if (p.contains("discord")) return "Discord";
        if (p.contains("steam")) return "Steam";
        if (p.contains("spotify")) return "Spotify";
        if (p.contains("notepad")) return "Notepad";
        if (p.contains("vscode")) return "Visual Studio Code";
        if (p.contains("minecraft")) return "Minecraft";
        if (p.contains("obs")) return "OBS Studio";
        if (p.contains("photoshop")) return "Adobe Photoshop";
        if (p.contains("word")) return "Microsoft Word";
        if (p.contains("excel")) return "Microsoft Excel";
        if (p.contains("vlc")) return "VLC Media Player";
        return processName.replace(".exe", "").replace("_", " ");
    }

    /** aurora-styled (aqua, phase-1 "nice") line in this client's own chat. */
    private static void auroraAqua(MinecraftClient client, String message) {
        Text text = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.AQUA)
            .append(Text.literal(message).formatted(Formatting.WHITE));
        if (client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(text);
        }
    }

    /** aurora-styled (yellow) transition line in this client's own chat. */
    private static void aurora(MinecraftClient client, String message) {
        Text text = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.YELLOW)
            .append(Text.literal(message).formatted(Formatting.WHITE));
        if (client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(text);
        }
    }

    /** NullPointerEntity-styled (red) transition line in this client's own chat (no privacy reprocessing). */
    private static void nullpointer(MinecraftClient client, String message) {
        Text text = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
            .append(Text.literal(message).formatted(Formatting.RED));
        if (client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(text);
        }
    }

    // ---- camera (e37) -------------------------------------------------------

    private static void openCameraAndCapture() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean cameraOpened = false;

            if (os.contains("windows")) {
                try {
                    String psCommand =
                        "Start-Process microsoft.windows.camera:; " +
                        "for ($i=0; $i -lt 20; $i++) { " +
                        "  Start-Sleep -Milliseconds 250; " +
                        "  $w = Get-Process | Where-Object {$_.MainWindowTitle -eq 'Camera'}; " +
                        "  if ($w) { break }; " +
                        "}; " +
                        "if ($w) { " +
                        "  $code = '[DllImport(\"user32.dll\")] public static extern bool SetWindowPos(IntPtr hWnd, IntPtr hWndInsertAfter, int X, int Y, int cx, int cy, uint uFlags);'; " +
                        "  $type = Add-Type -MemberDefinition $code -Name Win32 -Namespace Win32 -PassThru; " +
                        "  $type::SetWindowPos($w.MainWindowHandle, -1, 0, 0, 0, 0, 3); " +
                        "}";
                    new ProcessBuilder("powershell", "-Command", psCommand).start();
                    Thread.sleep(3000);
                    cameraOpened = true;
                } catch (Exception e) {
                    try {
                        new ProcessBuilder("cmd", "/c", "start", "microsoft.windows.camera:").start();
                        Thread.sleep(3000);
                        cameraOpened = true;
                    } catch (Exception ex) {
                        NullPointerEntity.LOGGER.warn("All Windows camera methods failed for hostile event");
                    }
                }
            } else if (os.contains("mac")) {
                try {
                    new ProcessBuilder("open", "-a", "Photo Booth").start();
                    Thread.sleep(3000);
                    cameraOpened = true;
                } catch (Exception e1) {
                    try {
                        new ProcessBuilder("open", "-a", "FaceTime").start();
                        Thread.sleep(3000);
                        cameraOpened = true;
                    } catch (Exception e2) {
                        // silent fail for macos
                    }
                }
            } else if (os.contains("linux")) {
                for (String app : new String[]{"cheese", "guvcview", "kamoso", "camorama"}) {
                    try {
                        new ProcessBuilder(app).start();
                        Thread.sleep(3000);
                        cameraOpened = true;
                        break;
                    } catch (Exception e) {
                        // try next app
                    }
                }
            }

            if (cameraOpened) {
                try {
                    if (os.contains("windows")) {
                        String psScript =
                            "$wshell = New-Object -ComObject wscript.shell;" +
                            "Start-Sleep -Milliseconds 1000;" +
                            "for ($i=0; $i -lt 3; $i++) {" +
                            "  $wshell.SendKeys(' ');" +
                            "  Start-Sleep -Milliseconds 500;" +
                            "};" +
                            "$wshell.SendKeys('{ENTER}');";
                        new ProcessBuilder("powershell", "-Command", psScript).start();
                    } else if (!java.awt.GraphicsEnvironment.isHeadless()) {
                        java.awt.Robot robot = new java.awt.Robot();
                        for (int i = 0; i < 3; i++) {
                            robot.keyPress(java.awt.event.KeyEvent.VK_SPACE);
                            robot.delay(100);
                            robot.keyRelease(java.awt.event.KeyEvent.VK_SPACE);
                            robot.delay(500);
                        }
                        robot.delay(200);
                        robot.keyPress(java.awt.event.KeyEvent.VK_ENTER);
                        robot.delay(100);
                        robot.keyRelease(java.awt.event.KeyEvent.VK_ENTER);
                    }
                } catch (Exception keyEx) {
                    NullPointerEntity.LOGGER.error("Failed to send keystrokes: {}", keyEx.getMessage());
                }
            } else {
                PopupManager.showTimedPopup(
                    tr("popup.nullpointerentity.hostile.camera_restricted.title"),
                    tr("popup.nullpointerentity.hostile.camera_restricted.body"),
                    PopupManager.PopupType.WARNING, 5);
            }
        } catch (Exception e) {
            PopupManager.showTimedPopup(
                tr("popup.nullpointerentity.hostile.system_access.title"),
                tr("popup.nullpointerentity.hostile.system_access.body"),
                PopupManager.PopupType.WARNING, 5);
            NullPointerEntity.LOGGER.warn("Camera access blocked in hostile event: {}", e.getMessage());
        }
    }

    // ---- helpers ------------------------------------------------------------

    private static String mcUser(MinecraftClient client) {
        return client.player != null ? client.player.getName().getString() : "player";
    }

    private static String winUser() {
        // display name: stable fake under privacy, real otherwise. privacy never blocks the actual
        // file/effect - it only swaps out the info that gets shown/written.
        return PrivacyManager.getSystemUsername(System.getProperty("user.name"));
    }

    private static String tr(String key, Object... args) {
        return Text.translatable(key, args).getString();
    }

    private static String variant(String baseKey, int variants, Object... args) {
        return tr(baseKey + "." + (1 + RANDOM.nextInt(variants)), args);
    }

    /** adds a NullPointerEntity-styled chat line to this client's own chat. */
    private static void send(MinecraftClient client, String mcUser, String winUser, String message) {
        String processed = PrivacyManager.processEventMessage(message, mcUser, winUser);
        Text text = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
            .append(Text.literal(processed).formatted(Formatting.RED));
        if (client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(text);
        }
    }

    /** runs {@code action} on the client thread after {@code delayMs}. */
    private static void schedule(MinecraftClient client, long delayMs, Runnable action) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client.execute(action);
            }
        }, delayMs);
    }

    private static boolean isProcessRunning(String processName) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Process process = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + processName).start();
                try (Scanner scanner = new Scanner(process.getInputStream())) {
                    while (scanner.hasNextLine()) {
                        if (scanner.nextLine().toLowerCase().contains(processName.toLowerCase())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // treat as not running
        }
        return false;
    }

    private static String randomIP() {
        return String.format("%d.%d.%d.%d", RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255));
    }

    private static String randomCity() {
        String[] cities = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
            "San Antonio", "San Diego", "Dallas", "San Jose", "Austin", "Jacksonville",
            "Fort Worth", "Columbus", "Charlotte", "San Francisco", "Indianapolis",
            "Seattle", "Denver", "Washington", "Boston", "El Paso", "Nashville",
            "Detroit", "Oklahoma City", "Portland", "Las Vegas", "Memphis", "Louisville"
        };
        return cities[RANDOM.nextInt(cities.length)];
    }

    private static String randomRegion() {
        String[] regions = {
            "California", "Texas", "Florida", "New York", "Pennsylvania", "Illinois",
            "Ohio", "Georgia", "North Carolina", "Michigan", "New Jersey", "Virginia",
            "Washington", "Arizona", "Massachusetts", "Tennessee", "Indiana", "Missouri",
            "Maryland", "Wisconsin", "Colorado", "Minnesota", "South Carolina", "Alabama"
        };
        return regions[RANDOM.nextInt(regions.length)];
    }

    private static String randomISP() {
        String[] isps = {
            "Comcast Cable Communications", "Charter Communications", "Verizon Communications",
            "AT&T Services", "Cox Communications", "CenturyLink", "Frontier Communications",
            "Optimum", "Mediacom", "Windstream", "TDS Telecom", "Cincinnati Bell",
            "MetroNet", "WOW! Internet", "Rise Broadband", "Hawaiian Telcom"
        };
        return isps[RANDOM.nextInt(isps.length)];
    }

    private static String randomZip() {
        return String.format("%05d", RANDOM.nextInt(99999));
    }
}
