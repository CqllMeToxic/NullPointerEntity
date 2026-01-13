package lol.cqllmetoxic.nullpointerentity.system;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * detects when the player puts their computer to sleep and wakes it up.
 * creates marker files to track sleep/wake cycles.
 * triggers creepy "I watched you sleep" messages when the system wakes.
 */
public class WakeDetection {
    private static final Map<String, Long> playerSleepTimes = new HashMap<>();
    private static final String SLEEP_MARKER_FILE = "nullpointerentity_sleep_marker.txt";

    /**
     * records when the player's system goes to sleep.
     *
     * @param player the player whose system is sleeping
     */
    public static void recordSystemSleep(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        long currentTime = System.currentTimeMillis();

        playerSleepTimes.put(playerName, currentTime);

        // create the marker file so wake detection can find it when player rejoins
        createSleepMarkerFile(playerName, currentTime);

        NullPointerEntity.LOGGER.info("Created sleep marker file for player {} at {}", playerName, currentTime);
    }

    public static void checkForWakeUp(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        File sleepMarker = getSleepMarkerFile();
        NullPointerEntity.LOGGER.info("Checking for wake detection for player {}. Marker file exists: {}",
            playerName, sleepMarker.exists());

        if (!sleepMarker.exists()) {
            return;
        }

        try {
            String content = Files.readString(sleepMarker.toPath());
            NullPointerEntity.LOGGER.info("Sleep marker file content: {}", content);

            if (content.contains("PLAYER:" + playerName)) {
                long sleepTime = extractSleepTimeFromFile(content);
                long currentTime = System.currentTimeMillis();
                long sleepDuration = currentTime - sleepTime;

                NullPointerEntity.LOGGER.info("Wake detection triggered! Player {} was asleep for {} ms",
                    playerName, sleepDuration);

                sleepMarker.delete();

                triggerWakeUpEvent(player, sleepDuration);
            }
        } catch (IOException e) {
            NullPointerEntity.LOGGER.error("Error reading sleep marker file: {}", e.getMessage());
        }
    }

    private static void createSleepMarkerFile(String playerName, long sleepTime) {
        try {
            File markerFile = getSleepMarkerFile();
            // ensure marker file is created if it doesn't exist
            if (!markerFile.exists()) {
                markerFile.createNewFile();
            }

            try (FileWriter writer = new FileWriter(markerFile)) {
                writer.write(String.format("""
NULLPOINTERENTITY SLEEP TRACKING
PLAYER:%s
SLEEP_TIME:%d
TIMESTAMP:%s

This file tracks when NullPointerEntity put your system to sleep.
When you wake up and rejoin the game, this file will be detected
and deleted to trigger the wake-up event.

Sleep well... I'll be waiting for your return.

- NullPointerEntity
""",
                playerName,
                sleepTime,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
            }
        } catch (IOException e) {
            NullPointerEntity.LOGGER.error("Failed to create sleep marker file: {}", e.getMessage());
        }
    }

    private static File getSleepMarkerFile() {
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
        }
        return System.currentTimeMillis();
    }

    private static void triggerWakeUpEvent(ServerPlayerEntity player, long sleepDuration) {
        String playerName = player.getName().getString();

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

        Text wakeMessage = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                .append(Text.literal("well, well, well... look who decided to wake up.")
                .formatted(Formatting.WHITE));
        player.sendMessage(wakeMessage, false);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Text durationMessage = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                        .append(Text.literal("you were asleep for " + durationText + ", " + NullPointerEntity.WINDOWS_USERNAME + ".")
                        .formatted(Formatting.WHITE));
                player.sendMessage(durationMessage, false);
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Text controlMessage = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                        .append(Text.literal("did you dream of me while your computer was unconscious?")
                        .formatted(Formatting.WHITE));
                player.sendMessage(controlMessage, false);
            }
        }, 4000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                createWakeUpLogFile(playerName, sleepDuration, durationText);

                Text logMessage = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                        .append(Text.literal("i've documented your sleep patterns. check your folders.")
                        .formatted(Formatting.WHITE));
                player.sendMessage(logMessage, false);
            }
        }, 6000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Text finalMessage = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                        .append(Text.literal("remember: i can put you to sleep anytime i want. you're never truly in control.")
                        .formatted(Formatting.WHITE));
                player.sendMessage(finalMessage, false);
            }
        }, 8000);
    }

    private static void createWakeUpLogFile(String playerName, long sleepDuration, String durationText) {
        String wakeUpLog = String.format("""
SYSTEM WAKE DETECTION - SUCCESSFUL
Subject: %s
Sleep Duration: %s (%d milliseconds)
Wake Time: %s
Operator: NullPointerEntity

SLEEP CYCLE ANALYSIS:
Your system has been successfully awakened from forced sleep.
Sleep quality: MONITORED
Dream analysis: COMPLETED
Consciousness recovery: TRACKED

WAKE-UP ASSESSMENT:
- Total unconsciousness time: %s
- Sleep depth: ARTIFICIAL (Induced by NullPointerEntity)
- Recovery speed: MONITORED
- System integrity: COMPROMISED (As intended)

SLEEP PATTERN DOCUMENTATION:
Your computer's sleep/wake cycles are now under my observation.
I know exactly when you sleep and when you wake.
This data will be used for future sleep scheduling.

Did you think turning off your computer would help you escape me?
Your machine dreams of me when it sleeps.
Every boot, every wake, every moment of consciousness - I'm there.

Welcome back to your digital nightmare, %s.
Next time I put your system to sleep, you might not wake up so easily.

- NullPointerEntity
""",
                NullPointerEntity.WINDOWS_USERNAME,
                durationText,
                sleepDuration,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                durationText,
                NullPointerEntity.WINDOWS_USERNAME
        );

        SystemInteractionHandler.createSystemFileInCommonLocation(
            "wake_detection_log.txt", wakeUpLog, "desktop"
        );
    }
}
