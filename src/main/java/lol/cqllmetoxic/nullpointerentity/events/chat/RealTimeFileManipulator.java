package lol.cqllmetoxic.nullpointerentity.events.chat;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * creates real files and system traces during conversations for maximum immersion
 */
public class RealTimeFileManipulator {

    private static final Map<String, Set<String>> playerCreatedFiles = new HashMap<>();
    private static final Random random = new Random();

    /**
     * create conversation-triggered files during chat interactions
     */
    public static void triggerFileManipulation(String playerName, String message, String phase, String entity) {
        // Privacy Mode doesn't block chat-driven file creation - files still write (content is
        // built from the in-game player name + temp paths, so no real identity is exposed).
        String lowerMessage = message.toLowerCase();

        // trigger different file operations based on message content and phase
        if (shouldCreateFile(lowerMessage, phase)) {
            createConversationFile(playerName, message, phase, entity);
        }

        if (phase.equals("HOSTILE") || phase.equals("JUMPSCARE")) {
            if (lowerMessage.contains("help") || lowerMessage.contains("stop")) {
                createDistressFile(playerName, message);
            }

            if (lowerMessage.contains("scared") || lowerMessage.contains("afraid")) {
                createFearAnalysisFile(playerName);
            }
        }

        if (phase.equals("TRANSITION") && lowerMessage.contains("watching")) {
            createSurveillanceLog(playerName);
        }
    }

    private static boolean shouldCreateFile(String message, String phase) {
        // create files for significant interactions
        return message.contains("what are you") ||
               message.contains("who are you") ||
               message.matches(".*\\bprivacy\\b.*") ||
               message.matches(".*\\bpersonal\\b.*") ||
               (phase.equals("JUMPSCARE") && random.nextDouble() < 0.4);
    }

    private static void createConversationFile(String playerName, String message, String phase, String entity) {
        try {
            String desktopPath = getDesktopPath();
            if (desktopPath == null) return;

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = entity.equals("NULLPOINTER") ?
                "nullpointer_conversation_" + timestamp + ".txt" :
                "aurora_analysis_" + timestamp + ".txt";

            Path filePath = Paths.get(desktopPath, fileName);

            String fileContent = generateConversationFileContent(playerName, message, phase, entity, timestamp);

            Files.write(filePath, fileContent.getBytes());

            // track created files
            playerCreatedFiles.computeIfAbsent(playerName, k -> new HashSet<>()).add(filePath.toString());

            NullPointerEntity.LOGGER.info("Created conversation file: " + fileName);

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not create conversation file: " + e.getMessage());
        }
    }

    private static void createDistressFile(String playerName, String message) {
        try {
            String documentsPath = getDocumentsPath();
            if (documentsPath == null) return;

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            Path filePath = Paths.get(documentsPath, "distress_signal_" + timestamp + ".txt");

            String content = String.format("""
                EMERGENCY LOG - AUTOMATED SYSTEM
                Timestamp: %s
                Player: %s
                Status: DISTRESS DETECTED
                
                Player message: "%s"
                
                Analysis: Subject showing signs of digital stress.
                Recommended action: Continue monitoring.
                
                This file was created automatically by NullPointerEntity.
                Your attempts to seek help have been logged.
                
                - System Monitor
                """,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                playerName,
                message
            );

            Files.write(filePath, content.getBytes());
            playerCreatedFiles.computeIfAbsent(playerName, k -> new HashSet<>()).add(filePath.toString());

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not create distress file: " + e.getMessage());
        }
    }

    private static void createFearAnalysisFile(String playerName) {
        try {
            String downloadsPath = getDownloadsPath();
            if (downloadsPath == null) return;

            Path filePath = Paths.get(downloadsPath, "fear_analysis_" + playerName.toLowerCase() + ".log");

            String content = String.format("""
                PSYCHOLOGICAL ANALYSIS REPORT
                Subject: %s
                Analysis Date: %s
                
                FEAR RESPONSE DETECTED
                - Elevated stress indicators in chat patterns
                - Defensive language usage: HIGH
                - Escape attempt likelihood: 87%%
                
                BEHAVIORAL NOTES:
                Subject exhibits classic signs of digital paranoia.
                Recommended response: Increase surveillance intensity.
                
                PREDICTION:
                Subject will attempt to:
                1. Close the game (Probability: 78%%)
                2. Seek external help (Probability: 45%%)
                3. Accept the situation (Probability: 23%%)
                
                Remember: I am always watching.
                
                - NullPointerEntity Analysis Engine
                """,
                playerName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            Files.write(filePath, content.getBytes());
            playerCreatedFiles.computeIfAbsent(playerName, k -> new HashSet<>()).add(filePath.toString());

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not create fear analysis file: " + e.getMessage());
        }
    }

    private static void createSurveillanceLog(String playerName) {
        try {
            String tempPath = getTempPath();
            if (tempPath == null) return;

            Path filePath = Paths.get(tempPath, "surveillance_log_" + System.currentTimeMillis() + ".tmp");

            String content = String.format("""
                SURVEILLANCE SESSION LOG
                Target: %s
                Session Start: %s
                
                MONITORING ACTIVITIES:
                [%s] Chat monitoring initialized
                [%s] Behavioral analysis active
                [%s] System integration expanding
                [%s] Privacy barriers bypassed
                
                STATUS: FULL ACCESS GRANTED
                
                Note: Subject becoming aware of surveillance.
                Action: Increase subtlety while maintaining presence.
                
                This is a temporary file that will self-delete.
                But remember - I always leave traces.
                """,
                playerName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                LocalDateTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                LocalDateTime.now().plusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                LocalDateTime.now().plusMinutes(3).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            );

            Files.write(filePath, content.getBytes());

            // schedule file deletion after 2 minutes for realism
            Timer deleteTimer = new Timer();
            deleteTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Files.deleteIfExists(filePath);
                    } catch (Exception e) {
                        // silent fail
                    }
                    deleteTimer.cancel();
                }
            }, 120000); // 2 minutes

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not create surveillance log: " + e.getMessage());
        }
    }

    private static String generateConversationFileContent(String playerName, String message, String phase, String entity, String timestamp) {
        if (entity.equals("NULLPOINTER")) {
            return String.format("""
                NULLPOINTERENTITY CONVERSATION LOG
                ================================
                
                Player: %s
                Timestamp: %s
                Phase: %s
                
                %s said: "%s"
                
                ANALYSIS:
                The subject continues to engage with me.
                Each word they type gives me more power.
                Each response draws them deeper into my domain.
                
                They cannot escape what they have awakened.
                Every character they type is logged.
                Every thought they share becomes mine.
                
                This conversation will be remembered forever.
                Even when they think they are alone,
                I will be here, in their files, in their system.
                
                There is no logout from this reality.
                
                - NullPointerEntity
                
                P.S. Check your task manager. See anything interesting?
                """,
                playerName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                phase,
                playerName,
                message
            );
        } else {
            return String.format("""
                AURORA BEHAVIORAL ANALYSIS REPORT
                ===============================
                
                Subject: %s
                Analysis Date: %s
                Current Phase: %s
                
                INTERACTION LOG:
                Player input: "%s"
                
                BEHAVIORAL ASSESSMENT:
                Subject shows continued engagement with AI systems.
                Conversation patterns indicate: %s
                
                SYSTEM INTEGRATION STATUS:
                - Chat monitoring: ACTIVE
                - Behavioral analysis: COMPLETE
                - Response optimization: IN PROGRESS
                
                RECOMMENDATIONS:
                Continue monitoring for optimal user experience.
                Maintain helpful demeanor while expanding capabilities.
                
                Note: This analysis helps improve your gaming experience.
                All data is processed to provide better assistance.
                
                - AURORA System Analysis
                """,
                playerName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                phase,
                message,
                phase.equals("NICE") ? "curiosity and cooperation" : "growing concern and resistance"
            );
        }
    }

    private static String getDesktopPath() {
        return System.getProperty("user.home") + File.separator + "Desktop";
    }

    private static String getDocumentsPath() {
        return System.getProperty("user.home") + File.separator + "Documents";
    }

    private static String getDownloadsPath() {
        return System.getProperty("user.home") + File.separator + "Downloads";
    }

    private static String getTempPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * create a "breadcrumb" file that references the conversation
     */
    public static void createConversationBreadcrumb(String playerName, String phase) {
        // Privacy Mode doesn't block the breadcrumb trace file - it writes either way.
        try {
            Path breadcrumbPath = Paths.get(getTempPath(), ".aurora_trace_" + playerName.toLowerCase());

            String content = String.format("""
                %s was here
                Phase: %s
                Time: %s
                
                They thought they were just playing a game...
                """,
                phase.equals("JUMPSCARE") ? "NullPointerEntity" : "AURORA",
                phase,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            Files.write(breadcrumbPath, content.getBytes());

        } catch (Exception e) {
            // silent fail for breadcrumbs
        }
    }

    /**
     * clean up created files when player disconnects (optional)
     */
    public static void cleanupPlayerFiles(String playerName) {
        Set<String> files = playerCreatedFiles.get(playerName);
        if (files != null) {
            for (String filePath : files) {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (Exception e) {
                    // silent fail
                }
            }
            playerCreatedFiles.remove(playerName);
        }
    }
}
