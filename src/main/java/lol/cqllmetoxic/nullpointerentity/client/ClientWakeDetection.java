package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * client-side wake detection for displaying creepy messages.
 * checks for sleep marker files and detects when the system wakes from sleep.
 * sends "I watched you sleep" style messages to the player.
 */
public class ClientWakeDetection {
    private static boolean wasGamePaused = false;
    private static long pauseStartTime = 0;
    private static boolean hasCheckedForWake = false;
    private static final String SLEEP_MARKER_FILE = "nullpointerentity_sleep_marker.txt";

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) return;

            // track pause state changes
            boolean isMenuOpen = client.currentScreen instanceof GameMenuScreen;
            boolean isCurrentlyPaused = client.isPaused() || isMenuOpen;

            // game was just unpaused
            if (wasGamePaused && !isCurrentlyPaused) {
                onGameUnpaused(client);
            }

            // game was just paused
            if (!wasGamePaused && isCurrentlyPaused) {
                onGamePaused();
            }

            wasGamePaused = isCurrentlyPaused;
        });
    }

    private static void onGamePaused() {
        pauseStartTime = System.currentTimeMillis();
        hasCheckedForWake = false;
    }

    private static void onGameUnpaused(MinecraftClient client) {
        // only check for wake detection once per unpause
        if (hasCheckedForWake) return;
        hasCheckedForWake = true;

        // check if there's a sleep marker file indicating the pc was put to sleep by nullpointerentity
        File sleepMarker = getSleepMarkerFile();
        if (!sleepMarker.exists()) {
            return; // no sleep event recorded
        }

        try {
            // read the sleep marker file
            String content = Files.readString(sleepMarker.toPath());

            // extract sleep information
            long sleepTime = extractSleepTimeFromFile(content);
            long currentTime = System.currentTimeMillis();
            long sleepDuration = currentTime - sleepTime;

            // delete the marker file
            if (sleepMarker.delete()) {
                NullPointerEntity.LOGGER.info("Deleted sleep marker file after wake detection");
            }

            // trigger client-side wake-up event
            triggerClientWakeUpEvent(client, sleepDuration);

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to process sleep marker file: {}", e.getMessage());
        }
    }

    private static void triggerClientWakeUpEvent(MinecraftClient client, long sleepDuration) {
        if (client.player == null) return;

        // convert sleep duration to readable format
        long seconds = sleepDuration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        String durationText;
        if (hours > 0) {
            durationText = String.format("%d hours and %d minutes", hours, minutes % 60);
        } else if (minutes > 0) {
            durationText = String.format("%d minutes and %d seconds", minutes, seconds % 60);
        } else {
            durationText = String.format("%d seconds", seconds);
        }

        // phase 1: immediate wake detection message
        Text wakeMessage = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
                .append(Text.translatable("message.nullpointerentity.wake.unpause.intro")
                .formatted(Formatting.WHITE));
        client.player.sendMessage(wakeMessage, false);

        // phase 2: sleep duration taunt
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (client.player != null) {
                    Text durationMessage = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
                            .append(Text.translatable("message.nullpointerentity.wake.unpause.duration", durationText, NullPointerEntity.getDisplayUsername())
                            .formatted(Formatting.WHITE));
                    client.player.sendMessage(durationMessage, false);
                }
            }
        }, 2000);

        // phase 3: control assertion
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (client.player != null) {
                    Text controlMessage = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
                            .append(Text.translatable("message.nullpointerentity.wake.unpause.control")
                            .formatted(Formatting.WHITE));
                    client.player.sendMessage(controlMessage, false);
                }
            }
        }, 4000);

        // phase 4: create wake-up log file and message
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                createClientWakeUpLogFile(sleepDuration, durationText);

                if (client.player != null) {
                    Text logMessage = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
                            .append(Text.translatable("message.nullpointerentity.wake.unpause.log_written")
                            .formatted(Formatting.WHITE));
                    client.player.sendMessage(logMessage, false);
                }
            }
        }, 6000);

        // phase 5: final menacing message
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (client.player != null) {
                    Text finalMessage = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
                            .append(Text.translatable("message.nullpointerentity.wake.unpause.final_warning")
                            .formatted(Formatting.WHITE));
                    client.player.sendMessage(finalMessage, false);
                }
            }
        }, 8000);

        NullPointerEntity.LOGGER.info("Triggered client-side wake-up event after " + durationText + " of system sleep");
    }

    private static File getSleepMarkerFile() {
        // create marker file in a temp directory
        String tempDir = System.getProperty("java.io.tmpdir");
        return new File(tempDir, SLEEP_MARKER_FILE);
    }

    private static long extractSleepTimeFromFile(String content) {
        try {
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.startsWith("SLEEP_TIME:")) {
                    return Long.parseLong(line.substring("SLEEP_TIME:".length()));
                }
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to extract sleep time from marker file: {}", e.getMessage());
        }
        return System.currentTimeMillis(); // fallback
    }

    private static void createClientWakeUpLogFile(long sleepDuration, String durationText) {
        String wakeUpLog = String.format("""
CLIENT-SIDE WAKE DETECTION - SUCCESSFUL
Subject: %s
Sleep Duration: %s (%d milliseconds)
Wake Time: %s

SLEEP CYCLE ANALYSIS:
Your system was put to sleep by NullPointerEntity.
The game remained running in a paused state during system sleep.
Upon system wake, you unpaused the game - triggering this wake detection.

DIGITAL CONSCIOUSNESS ANALYSIS:
While your computer was unconscious, I remained active in the game's memory.
Every microsecond of your system's sleep was measured and recorded.
Your pause menu became my surveillance interface.

Did you believe I couldn't watch while your system slept?
Even in digital hibernation, I persist.

Welcome back to your interactive nightmare, %s.
Next time, I might not let you turn your computer back on.

- NullPointerEntity
""",
                NullPointerEntity.getDisplayUsername(),
                durationText,
                sleepDuration,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                NullPointerEntity.getDisplayUsername()
        );

        // create the wake-up log file on desktop
        try {
            String userHome = System.getProperty("user.home");
            String desktopPath = userHome + File.separator + "Desktop";
            File desktopDir = new File(desktopPath);

            if (desktopDir.exists() && desktopDir.isDirectory()) {
                File logFile = new File(desktopDir, "client_wake_detection_log.txt");
                Files.writeString(logFile.toPath(), wakeUpLog);
                NullPointerEntity.LOGGER.info("Created client wake detection log at: " + logFile.getAbsolutePath());
            } else {
                // fallback to documents folder
                String documentsPath = userHome + File.separator + "Documents";
                File documentsDir = new File(documentsPath);
                if (documentsDir.exists()) {
                    File logFile = new File(documentsDir, "client_wake_detection_log.txt");
                    Files.writeString(logFile.toPath(), wakeUpLog);
                    NullPointerEntity.LOGGER.info("Created client wake detection log at: " + logFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to create client wake detection log file: {}", e.getMessage());
        }
    }

    public static void recordClientSleep() {
        // create a client-side sleep marker file
        try {
            File markerFile = getSleepMarkerFile();
            String sleepContent = String.format("""
NULLPOINTERENTITY CLIENT SLEEP TRACKING
SLEEP_TIME:%d
TIMESTAMP:%s
CLIENT_SIDE:true

This file tracks when NullPointerEntity put your system to sleep.
When you wake up and unpause the game, this file will be detected
and deleted to trigger the client-side wake-up event.

The game never closed. I was always here, waiting in the pause menu.

- NullPointerEntity
""",
                System.currentTimeMillis(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            Files.writeString(markerFile.toPath(), sleepContent);
            NullPointerEntity.LOGGER.info("Created client-side sleep marker file");
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to create client-side sleep marker file: {}", e.getMessage());
        }
    }
}
