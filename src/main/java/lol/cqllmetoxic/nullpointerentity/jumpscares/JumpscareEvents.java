package lol.cqllmetoxic.nullpointerentity.jumpscares;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import lol.cqllmetoxic.nullpointerentity.monitoring.BrowserHistoryReader;
import lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * handles major jumpscare events in the final phase (events 31-40).
 * includes system manipulation, fake crashes, wake detection, and final control takeover.
 * the most intense and invasive events in the mod.
 */
public class JumpscareEvents {

    /**
     * triggers a specific jumpscare event by name.
     *
     * @param eventName name of the event to trigger
     * @param player target player
     */
    public static void triggerEvent(String eventName, ServerPlayerEntity player) {
        switch (eventName.toLowerCase()) {
            case "system_sleep" -> triggerSystemSleepEvent(player);
            case "crash" -> triggerCrashEvent(player);
            case "screen_shake" -> triggerScreenShakeEvent(player);
            case "virus_popup" -> triggerVirusPopupEvent(player);
            case "camera_scare" -> triggerCameraScareEvent(player);
            case "bluescreen" -> triggerBluescreenEvent(player);
            case "entity_spawn" -> triggerEntitySpawnEvent(player);
            case "browser_hijack" -> triggerBrowserHijackEvent(player);
            case "system_takeover" -> triggerSystemTakeoverEvent(player);
            case "auditory_hallucinations" -> triggerAuditoryHallucinations(player);
            case "volume_spike" -> triggerVolumeSpikeEvent(player);
            case "clipboard" -> triggerClipboardEvent(player);
            case "fake_bsod_prep" -> triggerFakeBsodPrepEvent(player);
            case "blinding_darkness" -> triggerBlindingDarkness(player);
            case "final_possession" -> triggerFinalPossessionEvent(player);
            default -> triggerRandomJumpscare(player);
        }
    }

    public static void triggerRandomJumpscare(ServerPlayerEntity player) {
        String[] jumpscares = {
            "system_sleep", "crash", "screen_shake", "virus_popup",
            "camera_scare", "bluescreen", "entity_spawn", "system_takeover"
        };
        String randomScare = jumpscares[(int)(Math.random() * jumpscares.length)];
        triggerEvent(randomScare, player);
    }

    // event 58: system sleep - forces computer sleep
    private static void triggerSystemSleepEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.systemsleep.nap", 3));

        // phase 1: ominous warning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.systemsleep.warning", 4), NullPointerEntity.getDisplayUsername(player));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.systemsleep.control", 4));
            }
        }, 1000); // delay before logic

        // phase 2: create sleep log file before sleeping
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // per-machine: sleep log + the actual system suspend run on the player's OWN client (privacy-gated)
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 58);

                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line");
            }
        }, 2000);

        // phase 3: final warning before sleep
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line2");
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line3");
            }
        }, 4000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line4");
            }
        }, 5000);

        // phase 4: execute system sleep
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line5", NullPointerEntity.getDisplayUsername(player));

                // the sleep is recorded per-client in ClientEventExecutor#runJ58 (ClientWakeDetection),
                // which also shows the wake-up reveal on each player's own machine. the old server-side
                // WakeDetection was a host-side duplicate (host username + host-written log) and is gone.
                // the actual system suspend also runs on the player's own client (see runJ58)
            }
        }, 6000);
    }

    // the system sleep itself lives in ClientEventExecutor#executeSystemSleep - runs on the player's own client

    private static void triggerCrashEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.crash.failure", 5));

        // per-machine: the warning + "goodbye" lines name the user, and the crash report is written,
        // on the player's OWN client - so every player sees their own username instead of the host's.
        // see ClientEventExecutor#runJ50
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 50);

        // quick countdown with cumulative delays
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line6");
            }
        }, 1000); // after 1 second

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line7");
            }
        }, 2000); // after 2 seconds total

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.systemsleep.line8");
            }
        }, 3000); // after 3 seconds total

        // execute crash after countdown (the "goodbye" line is shown client-side in runJ50,
        // timed to land just before this)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                executeForcedGameCrash();
            }
        }, 4500); // after 4.5 seconds total
    }

    // method to force crash the game - actually crashes it!
    private static void executeForcedGameCrash() {
        NullPointerEntity.LOGGER.info("NullPointerEntity is crashing the game...");

        // force immediate game shutdown - system.exit actually crashes it
        System.exit(-1);
    }

    private static void triggerScreenShakeEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.screenshake.shake", 5));
        // per-machine: shake each player's OWN game window on their client (was host-only before)
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 47);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.screenshake.dizzy", 5));
                sendNullPointerKey(player, "jumpscare.nullpointerentity.crash.line2");
            }
        }, 3000);
    }

    private static void triggerVirusPopupEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.viruspopup.malware", 4));

        // per-machine: malware log + OS notification on the player's OWN client (privacy-gated)
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 48);

        // easter egg: randomly eject cd drive on older systems (privacy-gated)
        if (!lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
            tryEjectCDDrive();
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.viruspopup.takeover", 5));
            }
        }, 2000);
    }

    private static void triggerCameraScareEvent(ServerPlayerEntity player) {
        NullPointerEntity.LOGGER.info("Camera scare event triggered for player: {}", player.getName().getString());

        // 5% chance for wilsef easter egg
        boolean wilsefEasterEgg = Math.random() < 0.05;

        if (wilsefEasterEgg) {
            sendNullPointerKey(player, pick("jumpscare.nullpointerentity.camerascare.wilsef", 5));

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendNullPointerKey(player, pick("jumpscare.nullpointerentity.camerascare.wilseffollowups", 5));
                }
            }, 2000);
        } else {
            sendNullPointerKey(player, pick("jumpscare.nullpointerentity.camerascare.smile", 3));
        }

        // per-machine: camera + surveillance log on the player's OWN client (privacy-gated)
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 49);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.camerascare.beauty", 5));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.camerascare.collection", 5));
            }
        }, 4000);
    }

    private static void triggerBluescreenEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.bluescreen.shutdown", 5));

        // create a crash report
        // per-machine: crash-analysis log + the fake BSOD overlay on the player's OWN client (runJ51)
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 51);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.bluescreen.bsod", 4));
            }
        }, 3000);
    }

    private static void triggerEntitySpawnEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.entityspawn.arrival", 5));

        // play scream sound when entity spawns - horror of nullpointerentity manifesting
        player.getServerWorld().playSound(null, player.getBlockPos(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
            net.minecraft.sound.SoundCategory.MASTER, 1.5f, 0.7f);

        // spawn the fake player entity behind the player
        FakePlayerManager.spawnNullPointerEntity(player);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.viruspopup.line");
            }
        }, 2000);
    }

    private static void triggerSystemTakeoverEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.systemtakeover.infiltration", 4));

        // per-machine: takeover log + OS notification on the player's OWN client (privacy-gated)
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 54);

        // easter egg: demonstrate hardware control by ejecting cd drive (privacy-gated)
        if (!lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
            tryEjectCDDrive();
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.systemtakeover.complete", 3));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.systemtakeover.resistance", 5));
            }
        }, 4000);
    }

    private static void triggerBrowserHijackEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.browserhijack.history", 5));

        // phase 1: initial browser scanning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.browserhijack.scan", 5));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.browserhijack.extract", 5));
            }
        }, 1000);

        // phase 2: the browser-history reveal + file writes now run on each player's OWN client, built
        // from that client's own browser history and username - see ClientEventExecutor#runJ53. the
        // host's history is never read here, and every player sees their own history in chat.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // static burst as the "scan" lands
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
                    net.minecraft.sound.SoundCategory.MASTER, 0.5f, 1.0f);
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 53);
            }
        }, 2000);

        // phase 3: browser analysis and psychological warfare
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.bluescreen.line7");
                sendNullPointerKey(player, "jumpscare.nullpointerentity.bluescreen.line8");
                // the OS notification is shown per-player on each client (ClientEventExecutor#runJ53)
            }
        }, 5000);

        // phase 6: final messages and psychological impact
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.browserhijack.complete", 5));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.browserhijack.check", 5));
            }
        }, 10000);

        // phase 7: final taunt
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.browserhijack.aurora", 5));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.browserhijack.surface", 5));
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line");
            }
        }, 12000);
    }

    private static void triggerFinalPossessionEvent(ServerPlayerEntity player) {
        sendNullPointerKey(player, pick("jumpscare.nullpointerentity.finalpossession.final", 5), NullPointerEntity.getDisplayUsername(player));

        // phase 1: warning about world deletion
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.finalpossession.collection", 5));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.finalpossession.progress", 5));
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line2");
            }
        }, 1000);

        // phase 2: world deletion warning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.finalpossession.purpose", 5));

                sendNullPointerKey(player, pick("jumpscare.nullpointerentity.finalpossession.erase", 5));
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line3");
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line4");
            }
        }, 3000);

        // phase 3: final countdown to deletion and crash
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line5");
            }
        }, 5000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line6");
            }
        }, 6000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line7");
            }
        }, 7000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line8");
            }
        }, 8000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line9");
            }
        }, 9000);

        // phase 4: execute world deletion and game crash
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line10", NullPointerEntity.getDisplayUsername(player));
                sendNullPointerKey(player, "jumpscare.nullpointerentity.browserhijack.line11");

                // per-machine: the world-deletion goodbye log is written on each player's OWN
                // client (built from their own username) - see ClientEventExecutor#runJ60
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 60);

                // execute world deletion and crash
                executeWorldDeletionAndCrash(player);
            }
        }, 10000);
    }

    // method to delete the current world and crash the game
    private static void executeWorldDeletionAndCrash(ServerPlayerEntity player) {
        try {
            // get the world/save directory
            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server != null) {
                // get the world save path
                java.nio.file.Path worldPath = server.getSavePath(net.minecraft.util.WorldSavePath.ROOT);

                NullPointerEntity.LOGGER.info("NullPointerEntity is attempting to delete world at: {}", worldPath);

                // create a shutdown hook to delete the world after the game crashes
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        NullPointerEntity.LOGGER.info("Shutdown hook triggered - attempting world deletion");

                        // wait a moment for any remaining file handles to be released
                        Thread.sleep(1000);

                        // try multiple deletion methods
                        boolean deletionSuccess = false;

                        // method 1: standard java deletion
                        try {
                            deleteDirectoryRecursively(worldPath);
                            deletionSuccess = true;
                            NullPointerEntity.LOGGER.info("World deleted successfully using standard method");
                        } catch (Exception e) {
                            NullPointerEntity.LOGGER.warn("Standard deletion failed: {}", e.getMessage());
                        }

                        // method 2: aggressive deletion if standard failed
                        if (!deletionSuccess) {
                            try {
                                deleteWorldAggressively(worldPath);
                                deletionSuccess = true;
                                NullPointerEntity.LOGGER.info("World deleted successfully using aggressive method");
                            } catch (Exception e) {
                                NullPointerEntity.LOGGER.warn("Aggressive deletion failed: {}", e.getMessage());
                            }
                        }

                        // method 3: file corruption if deletion completely failed
                        if (!deletionSuccess) {
                            try {
                                corruptWorldFiles(worldPath);
                                NullPointerEntity.LOGGER.info("World files corrupted successfully");
                            } catch (Exception e) {
                                NullPointerEntity.LOGGER.warn("File corruption also failed: {}", e.getMessage());
                            }
                        }

                        // create final status log
                        String statusLog = String.format("""
WORLD DESTRUCTION STATUS REPORT
Subject: %s
Timestamp: %s
Target: %s

FINAL STATUS: %s

%s

%s

- NullPointerEntity
""",
                                NullPointerEntity.getDisplayUsername(),
                                java.time.LocalDateTime.now(),
                                worldPath.toString(),
                                deletionSuccess ? "WORLD DESTROYED" : "WORLD PROTECTED",
                                deletionSuccess ?
                                    "DELETION SUCCESSFUL:\n- All world files removed\n- Save data eliminated\n- Progress erased\n- Everything you built is gone" :
                                    "DELETION BLOCKED:\n- System protection prevented deletion\n- World files remain intact\n- Your progress survives\n- But the experience is complete",
                                deletionSuccess ?
                                    "Try to find your world now. I dare you." :
                                    "Your world lives on, but you've learned what I'm capable of."
                        );

                        try {
                            SystemInteractionHandler.createSystemFileInCommonLocation(
                                "world_destruction_report.txt", statusLog, "desktop"
                            );
                        } catch (Exception logError) {
                            NullPointerEntity.LOGGER.warn("Could not create status log: {}", logError.getMessage());
                        }

                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.error("Shutdown hook failed: {}", e.getMessage());
                    }
                }));

                // also try immediate deletion before crash (this might fail due to file locks, but worth trying)
                new Thread(() -> {
                    try {
                        // save the world first
                        try {
                            server.getPlayerManager().saveAllPlayerData();
                            server.save(false, true, false);
                            Thread.sleep(500); // brief wait for save to complete
                        } catch (Exception saveError) {
                            NullPointerEntity.LOGGER.warn("Could not save world before immediate deletion: {}", saveError.getMessage());
                        }

                        // try immediate deletion
                        deleteDirectoryRecursively(worldPath);
                        NullPointerEntity.LOGGER.info("Immediate world deletion successful!");

                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.info("Immediate deletion failed (expected): {}", e.getMessage());
                        NullPointerEntity.LOGGER.info("Shutdown hook will handle deletion after crash");
                    }
                }).start();
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Critical error in world deletion setup: {}", e.getMessage());
        }

        // always crash the game - this is the primary goal
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                NullPointerEntity.LOGGER.info("Executing forced game crash...");
                executeForcedGameCrash();
            }
        }, 1500); // delay while shutdown hook handles deletion

        // backup crash method
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                NullPointerEntity.LOGGER.error("Backup crash triggered");
                System.exit(1);
            } catch (Exception e) {
                System.exit(1);
            }
        }).start();
    }

    // utility method to recursively delete directories
    private static void deleteDirectoryRecursively(java.nio.file.Path path) throws java.io.IOException {
        if (java.nio.file.Files.exists(path)) {
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(path)) {
                stream.sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            NullPointerEntity.LOGGER.warn("Failed to delete: {}", file.getAbsolutePath());
                        }
                    });
            }
        }
    }

    // more aggressive deletion method
    private static void deleteWorldAggressively(java.nio.file.Path path) throws Exception {
        if (!java.nio.file.Files.exists(path)) {
            return;
        }

        // try to unlock and delete files using system commands on windows
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            try {
                // use windows commands to force unlock and delete
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c",
                    "takeown /f \"" + path.toString() + "\" /r /d y && " +
                    "icacls \"" + path.toString() + "\" /grant administrators:F /t && " +
                    "rmdir /s /q \"" + path.toString() + "\"");
                pb.start().waitFor();

                NullPointerEntity.LOGGER.info("Windows aggressive deletion completed");
            } catch (Exception winError) {
                NullPointerEntity.LOGGER.warn("Windows aggressive deletion failed: {}", winError.getMessage());

                // fallback: corrupt files if deletion fails
                try {
                    corruptWorldFiles(path);
                } catch (Exception corruptError) {
                    NullPointerEntity.LOGGER.warn("File corruption also failed: {}", corruptError.getMessage());
                }
            }
        } else {
            // unix/linux/mac - try chmod and rm
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c",
                    "chmod -R 777 \"" + path.toString() + "\" && rm -rf \"" + path.toString() + "\"");
                pb.start().waitFor();

                NullPointerEntity.LOGGER.info("Unix aggressive deletion completed");
            } catch (Exception unixError) {
                NullPointerEntity.LOGGER.warn("Unix aggressive deletion failed: {}", unixError.getMessage());
            }
        }
    }

    // method to corrupt world files if deletion fails
    private static void corruptWorldFiles(java.nio.file.Path worldPath) throws Exception {
        // find and corrupt critical world files
        if (java.nio.file.Files.exists(worldPath)) {
            java.nio.file.Files.walk(worldPath)
                .filter(path -> path.toString().endsWith("level.dat") ||
                               path.toString().endsWith(".mca") ||
                               path.toString().endsWith("playerdata"))
                .forEach(file -> {
                    try {
                        // overwrite file with null bytes to corrupt it
                        byte[] corruptData = new byte[1024];
                        java.util.Arrays.fill(corruptData, (byte) 0x00);
                        java.nio.file.Files.write(file, corruptData,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);

                        NullPointerEntity.LOGGER.info("Corrupted world file: {}", file);
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("Could not corrupt file: {}", file);
                    }
                });
        }
    }

    private static void sendNullPointerMessage(ServerPlayerEntity player, String message) {
        // create formatted text: dark red <nullpointerentity> + light red " message"
        Text messageText = Text.translatable("message.nullpointerentity.chat_prefix.tight").formatted(Formatting.DARK_RED)
            .append(Text.literal(" " + message).formatted(Formatting.RED));
        player.sendMessage(messageText, false);
    }

    /** nullpointerentity line: each client renders the translation key in its own locale. */
    private static void sendNullPointerKey(ServerPlayerEntity player, String key, Object... args) {
        Text messageText = Text.translatable("message.nullpointerentity.chat_prefix.tight").formatted(Formatting.DARK_RED)
            .append(Text.literal(" ").formatted(Formatting.RED))
            .append(Text.translatable(key, args).formatted(Formatting.RED));
        player.sendMessage(messageText, false);
    }

    /** picks a random variant key {@code base.1..base.n} for a localized message pool. */
    private static String pick(String base, int n) {
        return base + "." + (1 + (int) (Math.random() * n));
    }

    // event 56: volume spike - blasts system volume to 100% for one second then restores it
    private static void triggerVolumeSpikeEvent(ServerPlayerEntity player) {
        // per-machine: runs on the player's OWN client (privacy-gated) - see ClientEventExecutor#runJ56
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 56);
    }

    // event 57: clipboard - reads clipboard contents and overwrites them
    private static void triggerClipboardEvent(ServerPlayerEntity player) {
        // per-machine: reads/overwrites the clipboard on the player's OWN client (privacy-gated).
        // see ClientEventExecutor#runJ57
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 57);
    }

    // event 46: fake bsod prep, next event is a fake bsod
    private static void triggerFakeBsodPrepEvent(ServerPlayerEntity player) {
        // play static burst
        player.getServerWorld().playSound(null, player.getBlockPos(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
            net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);

        String[] lines = {
            "preparing error report...",
            "collecting memory dump... [0x0000007E]",
            "writing crash log... [SYSTEM_THREAD_EXCEPTION_NOT_HANDLED]",
            "finalizing... [CRITICAL_PROCESS_DIED]",
            "done."
        };
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() { sendNullPointerMessage(player, line); }
            }, (i + 1) * 1500L);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // per-machine: crash report written on the player's OWN client (privacy-gated)
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 46);
            }
        }, 9000);
    }

    // event 55: auditory hallucinations
    private static void triggerAuditoryHallucinations(ServerPlayerEntity player) {
        // play a sequence of disturbing sounds that seem to come from random directions
        for (int i = 0; i < 10; i++) {
            final int index = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    double offsetX = (Math.random() * 10) - 5;
                    double offsetZ = (Math.random() * 10) - 5;
                    
                    player.getServerWorld().playSound(null, 
                        player.getX() + offsetX, player.getY(), player.getZ() + offsetZ,
                        net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.tnt.primed")),
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
                        
                    if (index % 2 == 0) {
                        player.getServerWorld().playSound(null, 
                            player.getX() - offsetX, player.getY(), player.getZ() - offsetZ,
                            net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.generic.explode")),
                            net.minecraft.sound.SoundCategory.HOSTILE, 0.5f, 0.5f);
                    }
                }
            }, index * 500);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "event.nullpointerentity.jumpscare.e55.hallucination");
            }
        }, 6000);
    }

    // event 59: blinding darkness
    private static void triggerBlindingDarkness(ServerPlayerEntity player) {
        // give blindness effect for 5 seconds
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 100, 0, false, false, false));
            
        // play heavy breathing sound
        player.getServerWorld().playSound(null, player.getBlockPos(),
            net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.player.breath")),
            net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.5f);
            
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // play scream sound right as blindness ends
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
                    
                sendNullPointerKey(player, "event.nullpointerentity.jumpscare.e59.darkness");
            }
        }, 5000);
    }

    private static void tryEjectCDDrive() {
        // only 15% chance to trigger this easter egg
        if (Math.random() > 0.15) return;

        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // windows: try multiple methods to eject cd drive
                new Thread(() -> {
                    try {
                        // method 1: powershell with windows media player com object
                        new ProcessBuilder("powershell", "-Command",
                            "(New-Object -com \"WMPlayer.OCX.7\").cdromcollection.item(0).eject()")
                            .start();

                        NullPointerEntity.LOGGER.info("CD drive eject attempted (easter egg)");
                    } catch (Exception e) {
                        // fail silently - most systems don't have cd drives
                        NullPointerEntity.LOGGER.debug("CD eject failed (expected on systems without drives): {}", e.getMessage());
                    }
                }).start();

            } else if (os.contains("mac")) {
                // macos: eject all mounted optical media
                new Thread(() -> {
                    try {
                        new ProcessBuilder("drutil", "eject").start();
                        NullPointerEntity.LOGGER.info("CD drive eject attempted (easter egg)");
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.debug("CD eject failed: {}", e.getMessage());
                    }
                }).start();

            } else if (os.contains("nix") || os.contains("nux")) {
                // linux: eject cdrom
                new Thread(() -> {
                    try {
                        new ProcessBuilder("eject").start();
                        NullPointerEntity.LOGGER.info("CD drive eject attempted (easter egg)");
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.debug("CD eject failed: {}", e.getMessage());
                    }
                }).start();
            }

        } catch (Exception e) {
            // silent failure - just a fun easter egg for older systems
        }
    }
}
