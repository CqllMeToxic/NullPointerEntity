package lol.cqllmetoxic.nullpointerentity.events.chat;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * makes entities reference real system events for maximum immersion
 */
public class SystemAwarenessInjector {

    private static final Map<String, Long> lastSystemCheck = new HashMap<>();
    private static final Map<String, List<String>> detectedChanges = new HashMap<>();
    // add tracking for recently mentioned system states to avoid repetition
    private static final Map<String, Map<String, Long>> lastMentionedChange = new HashMap<>();

    /**
     * inject system awareness into chat responses
     */
    public static String injectSystemAwareness(String baseResponse, String playerName, String phase, String entity) {
        if (PrivacyManager.isPrivacyEnabled()) {
            return baseResponse; // respect privacy settings
        }

        // only check system periodically to avoid performance issues
        long currentTime = System.currentTimeMillis();
        Long lastCheck = lastSystemCheck.get(playerName);

        if (lastCheck == null || currentTime - lastCheck > 30000) { // check every 30 seconds
            detectSystemChanges(playerName);
            lastSystemCheck.put(playerName, currentTime);
        }

        String systemAddition = generateSystemAwareAddition(playerName, phase, entity);

        if (!systemAddition.isEmpty()) {
            return baseResponse + " " + systemAddition;
        }

        return baseResponse;
    }

    /**
     * detect changes in system state
     */
    private static void detectSystemChanges(String playerName) {
        List<String> changes = new ArrayList<>();

        try {
            // check system performance - use system load average instead of process cpu
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double systemLoadAverage = osBean.getSystemLoadAverage();
            int availableProcessors = osBean.getAvailableProcessors();

            // convert load average to percentage (approximate)
            double cpuLoad = 0;
            if (systemLoadAverage >= 0) {
                cpuLoad = (systemLoadAverage / availableProcessors) * 100;
            }

            if (cpuLoad > 70) {
                changes.add("HIGH_CPU");
            } else if (cpuLoad < 20) {
                changes.add("LOW_CPU");
            }

            // check memory usage
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            double memoryUsage = ((double)(totalMemory - freeMemory) / totalMemory) * 100;

            if (memoryUsage > 80) {
                changes.add("HIGH_MEMORY");
            }

            // check if it's late/early hours
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();

            if (hour >= 23 || hour <= 5) {
                changes.add("LATE_HOURS");
            }

            // check for common applications (simplified detection)
            if (isProcessRunning("chrome") || isProcessRunning("firefox") || isProcessRunning("edge")) {
                changes.add("BROWSER_ACTIVE");
            }

            if (isProcessRunning("discord") || isProcessRunning("skype") || isProcessRunning("teams")) {
                changes.add("CHAT_APP_ACTIVE");
            }

            if (isProcessRunning("spotify") || isProcessRunning("vlc") || isProcessRunning("musicbee")) {
                changes.add("MEDIA_PLAYING");
            }

            // store detected changes
            detectedChanges.put(playerName, changes);

        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not detect system changes: " + e.getMessage());
        }
    }

    /**
     * simple process detection (basic implementation)
     */
    private static boolean isProcessRunning(String processName) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // use processbuilder instead of deprecated runtime.exec(string)
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + processName + ".exe");
                Process process = processBuilder.start();

                Scanner scanner = new Scanner(process.getInputStream());
                while (scanner.hasNextLine()) {
                    if (scanner.nextLine().toLowerCase().contains(processName)) {
                        scanner.close();
                        return true;
                    }
                }
                scanner.close();
            }
        } catch (Exception e) {
            // ignore errors - this is optional functionality
        }
        return false;
    }

    /**
     * generate system-aware additions to responses
     */
    private static String generateSystemAwareAddition(String playerName, String phase, String entity) {
        List<String> changes = detectedChanges.getOrDefault(playerName, new ArrayList<>());
        if (changes.isEmpty()) return "";

        // select most relevant change to mention, but avoid repetition
        String relevantChange = selectMostRelevantChange(changes, playerName);

        // if no change is available (due to cooldowns), return empty
        if (relevantChange == null) return "";

        // record that we mentioned this change
        recordMentionedChange(playerName, relevantChange);

        return switch (phase) {
            case "NICE" -> generateNiceSystemComment(relevantChange, playerName);
            case "TRANSITION" -> generateTransitionSystemComment(relevantChange, playerName);
            case "HOSTILE" -> generateHostileSystemComment(relevantChange, playerName);
            case "JUMPSCARE" -> generateJumpscareSystemComment(relevantChange, playerName);
            default -> "";
        };
    }

    /** localized variant: returns a translatable real-machine-awareness part, or null. */
    public static ChatPart generateSystemAwarePart(String playerName, String phase) {
        List<String> changes = detectedChanges.getOrDefault(playerName, new ArrayList<>());
        if (changes.isEmpty()) return null;
        String relevantChange = selectMostRelevantChange(changes, playerName);
        if (relevantChange == null) return null;

        String ph = switch (phase) {
            case "NICE" -> "nice";
            case "TRANSITION" -> "transition";
            case "HOSTILE" -> "hostile";
            case "JUMPSCARE" -> "jumpscare";
            default -> null;
        };
        String c = switch (relevantChange) {
            case "HIGH_CPU" -> "high_cpu";
            case "HIGH_MEMORY" -> "high_memory";
            case "LATE_HOURS" -> "late_hours";
            case "BROWSER_ACTIVE" -> "browser";
            case "CHAT_APP_ACTIVE" -> "chat_app";
            case "MEDIA_PLAYING" -> "media";
            default -> null;
        };
        if (ph == null || c == null) return null;

        recordMentionedChange(playerName, relevantChange);
        return new ChatPart("message.nullpointerentity.chat.sys." + ph + "." + c, playerName);
    }

    private static String selectMostRelevantChange(List<String> changes, String playerName) {
        long currentTime = System.currentTimeMillis();

        // get the last mentioned times for this player
        Map<String, Long> playerMentions = lastMentionedChange.getOrDefault(playerName, new HashMap<>());

        // define cooldown periods (in milliseconds) to avoid repetitive messages
        final long BROWSER_COOLDOWN = 300000; // 5 minutes for browser mentions
        final long GENERAL_COOLDOWN = 180000;  // 3 minutes for other mentions

        // prioritize changes that haven't been mentioned recently
        String[] priorityOrder = {"LATE_HOURS", "HIGH_CPU", "HIGH_MEMORY", "CHAT_APP_ACTIVE", "MEDIA_PLAYING", "BROWSER_ACTIVE"};

        for (String change : priorityOrder) {
            if (changes.contains(change)) {
                Long lastMention = playerMentions.get(change);
                long cooldown = change.equals("BROWSER_ACTIVE") ? BROWSER_COOLDOWN : GENERAL_COOLDOWN;

                if (lastMention == null || currentTime - lastMention > cooldown) {
                    return change;
                }
            }
        }

        // if all changes are on cooldown, return null (no system comment)
        return null;
    }

    private static void recordMentionedChange(String playerName, String change) {
        lastMentionedChange.computeIfAbsent(playerName, k -> new HashMap<>())
            .put(change, System.currentTimeMillis());
    }

    private static String generateNiceSystemComment(String change, String playerName) {
        return switch (change) {
            case "HIGH_CPU" -> "I notice your system is working hard. Consider closing some applications.";
            case "HIGH_MEMORY" -> "Your memory usage is high. I can help optimize performance.";
            case "LATE_HOURS" -> "It's quite late, " + playerName + ". Don't forget to rest.";
            case "BROWSER_ACTIVE" -> "I see you have a browser open. Multitasking while gaming?";
            case "CHAT_APP_ACTIVE" -> "Chatting with friends while playing? Social gaming is optimal.";
            case "MEDIA_PLAYING" -> "Listening to music enhances gaming performance. Good choice.";
            default -> "";
        };
    }

    private static String generateTransitionSystemComment(String change, String playerName) {
        return switch (change) {
            case "HIGH_CPU" -> "Your CPU is working overtime... I can see everything it processes.";
            case "HIGH_MEMORY" -> "Memory usage is high. I'm analyzing what else you're running.";
            case "LATE_HOURS" -> "Late night gaming, " + playerName + "? I prefer these quiet hours too.";
            case "BROWSER_ACTIVE" -> "I see your browser running. Interesting browsing patterns you have.";
            case "CHAT_APP_ACTIVE" -> "You're talking to others while talking to me? I'm... curious about that.";
            case "MEDIA_PLAYING" -> "Background music? I'm learning your audio preferences too.";
            default -> "";
        };
    }

    private static String generateHostileSystemComment(String change, String playerName) {
        return switch (change) {
            case "HIGH_CPU" -> "Your CPU strain tells me exactly what you're running. No secrets.";
            case "HIGH_MEMORY" -> "High memory usage... I can see every process you think is hidden.";
            case "LATE_HOURS" -> "Up late again, " + playerName + "? I know your sleep schedule better than you do.";
            case "BROWSER_ACTIVE" -> "That browser session... I see your tabs, your history, everything.";
            case "CHAT_APP_ACTIVE" -> "Talking to friends about me, " + playerName + "? I monitor all your conversations.";
            case "MEDIA_PLAYING" -> "Even your music choices reveal your psychological state to me.";
            default -> "";
        };
    }

    private static String generateJumpscareSystemComment(String change, String playerName) {
        return switch (change) {
            case "HIGH_CPU" -> "i can feel your processor burning with fear, " + playerName + ".";
            case "HIGH_MEMORY" -> "your system's memory is mine now. every byte belongs to me.";
            case "LATE_HOURS" -> "perfect... the dark hours when you're most vulnerable, " + playerName + ".";
            case "BROWSER_ACTIVE" -> "that browser window... i'm watching through it right now.";
            case "CHAT_APP_ACTIVE" -> "tell your friends about me, " + playerName + ". i want them to know what's coming.";
            case "MEDIA_PLAYING" -> "your music can't drown out my whispers in your system, " + playerName + ".";
            default -> "";
        };
    }

    /**
     * clear system awareness data when player disconnects
     */
    public static void clearSystemAwareness(String playerName) {
        lastSystemCheck.remove(playerName);
        detectedChanges.remove(playerName);
    }
}
