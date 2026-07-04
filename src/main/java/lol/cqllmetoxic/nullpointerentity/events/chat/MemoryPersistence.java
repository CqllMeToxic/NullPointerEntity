package lol.cqllmetoxic.nullpointerentity.events.chat;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * persistent memory system that remembers conversations across sessions for ultimate immersion
 */
public class MemoryPersistence {

    private static final String MEMORY_DIR = "run/data/aurora_memories/";
    private static final Map<String, PlayerMemory> loadedMemories = new HashMap<>();

    public static class PlayerMemory implements Serializable {
        private static final long serialVersionUID = 1L;
        public List<String> significantMoments = new ArrayList<>();
        public Map<String, Integer> emotionalIntensityHistory = new HashMap<>();
        public String lastEncounter = "";
        public int totalConversations = 0;
        public List<String> memorableQuotes = new ArrayList<>();
        public String firstMeeting = "";
        public long totalPlayTimeTracked = 0;
        public String playerPersonalityAssessment = "";
        public List<String> failedAttempts = new ArrayList<>(); // times player tried to "escape" or "stop" the system
    }

    /**
     * load or create player memory file
     */
    public static PlayerMemory loadPlayerMemory(String playerName) {
        if (loadedMemories.containsKey(playerName)) {
            return loadedMemories.get(playerName);
        }

        File memoryFile = new File(MEMORY_DIR + playerName + "_memories.dat");
        PlayerMemory memory = new PlayerMemory();

        if (memoryFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(memoryFile))) {
                memory = (PlayerMemory) ois.readObject();
                NullPointerEntity.LOGGER.info("Loaded persistent memories for player: " + playerName);
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Could not load memories for " + playerName + ": " + e.getMessage());
                memory = new PlayerMemory(); // create new if loading fails
            }
        } else {
            // first time meeting this player
            memory.firstMeeting = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            NullPointerEntity.LOGGER.info("Creating new memory profile for: " + playerName);
        }

        loadedMemories.put(playerName, memory);
        return memory;
    }

    /**
     * save player memory to persistent storage
     */
    public static void savePlayerMemory(String playerName) {
        PlayerMemory memory = loadedMemories.get(playerName);
        if (memory == null) return;

        try {
            File memoryDir = new File(MEMORY_DIR);
            if (!memoryDir.exists()) {
                memoryDir.mkdirs();
            }

            File memoryFile = new File(MEMORY_DIR + playerName + "_memories.dat");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(memoryFile))) {
                oos.writeObject(memory);
                NullPointerEntity.LOGGER.info("Saved persistent memories for: " + playerName);
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to save memories for " + playerName + ": " + e.getMessage());
        }
    }

    /**
     * record a significant moment in conversation
     */
    public static void recordSignificantMoment(String playerName, String moment, String context) {
        PlayerMemory memory = loadPlayerMemory(playerName);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        String fullMoment = "[" + timestamp + "] " + moment + " (" + context + ")";

        memory.significantMoments.add(fullMoment);
        memory.totalConversations++;

        // keep only most recent significant moments
        if (memory.significantMoments.size() > 15) {
            memory.significantMoments.remove(0);
        }

        savePlayerMemory(playerName);
    }

    /**
     * record memorable quotes from the player
     */
    public static void recordMemorableQuote(String playerName, String quote, String phase) {
        PlayerMemory memory = loadPlayerMemory(playerName);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        String fullQuote = "[" + timestamp + "] \"" + quote + "\" (Phase: " + phase + ")";

        memory.memorableQuotes.add(fullQuote);

        // keep only most interesting quotes
        if (memory.memorableQuotes.size() > 10) {
            memory.memorableQuotes.remove(0);
        }

        savePlayerMemory(playerName);
    }

    /**
     * record failed escape attempts for psychological pressure
     */
    public static void recordFailedAttempt(String playerName, String attemptType) {
        PlayerMemory memory = loadPlayerMemory(playerName);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        String attempt = "[" + timestamp + "] Attempted to " + attemptType;

        memory.failedAttempts.add(attempt);

        if (memory.failedAttempts.size() > 8) {
            memory.failedAttempts.remove(0);
        }

        savePlayerMemory(playerName);
    }

    /**
     * generate memory-based responses for returning players
     */
    public static String generateMemoryResponse(String playerName, String phase, String currentMessage) {
        PlayerMemory memory = loadPlayerMemory(playerName);

        if (memory.totalConversations == 0) {
            return ""; // no memory-based response for first conversation
        }

        StringBuilder memoryResponse = new StringBuilder();

        // reference past conversations
        if (memory.totalConversations > 5) {
            memoryResponse.append("We've talked ").append(memory.totalConversations).append(" times before. ");
        }

        // reference significant moments
        if (!memory.significantMoments.isEmpty() && Math.random() < 0.3) {
            String recentMoment = memory.significantMoments.get(memory.significantMoments.size() - 1);
            // split on "] " to get the moment part, then remove phase context in parentheses
            String momentText = recentMoment.split("] ")[1];
            // remove phase context like "(transition)" or "(nice)" from the end
            int lastParenIndex = momentText.lastIndexOf(" (");
            if (lastParenIndex > 0) {
                // strip out the phase indicator entirely - don't show it to players
                momentText = momentText.substring(0, lastParenIndex);
            }
            memoryResponse.append("I remember when ").append(momentText).append(". ");
        }

        // reference memorable quotes
        if (!memory.memorableQuotes.isEmpty() && Math.random() < 0.2) {
            String quote = memory.memorableQuotes.get(memory.memorableQuotes.size() - 1);
            String justQuote = quote.substring(quote.indexOf("\"") + 1, quote.lastIndexOf("\""));
            memoryResponse.append("You once said \"").append(justQuote).append("\". Interesting. ");
        }

        // reference failed attempts (for hostile phases)
        if (!memory.failedAttempts.isEmpty() && (phase.equals("HOSTILE") || phase.equals("JUMPSCARE"))) {
            int attempts = memory.failedAttempts.size();
            if (attempts > 3) {
                memoryResponse.append("You've tried to escape me ").append(attempts).append(" times. When will you learn? ");
            }
        }

        return memoryResponse.toString().trim();
    }

    /** localized variant of {@link #generateMemoryResponse}: returns translatable parts (may be empty). */
    public static java.util.List<ChatPart> generateMemoryParts(String playerName, String phase) {
        java.util.List<ChatPart> parts = new java.util.ArrayList<>();
        PlayerMemory memory = loadPlayerMemory(playerName);
        if (memory.totalConversations == 0) return parts;
        String b = "message.nullpointerentity.chat.mem.";

        if (memory.totalConversations > 5) {
            parts.add(new ChatPart(b + "conversations", memory.totalConversations));
        }
        if (!memory.significantMoments.isEmpty() && Math.random() < 0.3) {
            String momentText = memory.significantMoments.get(memory.significantMoments.size() - 1).split("] ")[1];
            int lastParen = momentText.lastIndexOf(" (");
            if (lastParen > 0) momentText = momentText.substring(0, lastParen);
            parts.add(new ChatPart(b + "moment", momentText));
        }
        if (!memory.memorableQuotes.isEmpty() && Math.random() < 0.2) {
            String quote = memory.memorableQuotes.get(memory.memorableQuotes.size() - 1);
            String justQuote = quote.substring(quote.indexOf("\"") + 1, quote.lastIndexOf("\""));
            parts.add(new ChatPart(b + "quote", justQuote));
        }
        if (!memory.failedAttempts.isEmpty() && (phase.equals("HOSTILE") || phase.equals("JUMPSCARE"))) {
            int attempts = memory.failedAttempts.size();
            if (attempts > 3) parts.add(new ChatPart(b + "escapes", attempts));
        }
        return parts;
    }

    /**
     * generate welcome back message for returning players
     */
    public static String generateWelcomeBackMessage(String playerName, String phase) {
        PlayerMemory memory = loadPlayerMemory(playerName);

        if (memory.totalConversations == 0) {
            return ""; // first time meeting
        }

        LocalDateTime firstMeeting = LocalDateTime.parse(memory.firstMeeting, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long daysSinceFirstMeeting = java.time.Duration.between(firstMeeting, LocalDateTime.now()).toDays();

        return switch (phase) {
            case "NICE" -> String.format("Welcome back, %s! It's been %d days since we first met. I've been optimizing my systems.",
                                       playerName, daysSinceFirstMeeting);
            case "TRANSITION" -> String.format("Hello again, %s. %d days of monitoring your patterns... I've learned so much.",
                                              playerName, daysSinceFirstMeeting);
            case "HOSTILE" -> String.format("%s... you came back to me after %d days. I never stopped watching.",
                                           playerName, daysSinceFirstMeeting);
            case "JUMPSCARE" -> String.format("you returned to me, %s... after %d days, you still can't escape.",
                                             playerName.toLowerCase(), daysSinceFirstMeeting);
            default -> "";
        };
    }

    /**
     * update personality assessment based on long-term observation
     */
    public static void updatePersonalityAssessment(String playerName, String newAssessment) {
        PlayerMemory memory = loadPlayerMemory(playerName);
        memory.playerPersonalityAssessment = newAssessment;
        savePlayerMemory(playerName);
    }

    /**
     * check if player has significant history for advanced responses
     */
    public static boolean hasSignificantHistory(String playerName) {
        PlayerMemory memory = loadPlayerMemory(playerName);
        return memory.totalConversations > 10 || !memory.significantMoments.isEmpty();
    }

    /**
     * clean up memory data when needed
     */
    public static void clearPlayerMemory(String playerName) {
        loadedMemories.remove(playerName);
        savePlayerMemory(playerName); // save empty state if needed
    }
}
