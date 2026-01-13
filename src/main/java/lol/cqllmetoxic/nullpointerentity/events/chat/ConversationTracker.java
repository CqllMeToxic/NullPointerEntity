package lol.cqllmetoxic.nullpointerentity.events.chat;

import java.util.*;

/**
 * tracks conversation history to make responses more dynamic and avoid repetition
 */
public class ConversationTracker {

    private static final Map<String, List<String>> playerConversationHistory = new HashMap<>();
    private static final Map<String, Map<String, Integer>> playerTopicCounts = new HashMap<>();
    private static final int MAX_HISTORY_SIZE = 20;

    /**
     * record a message from the player
     */
    public static void recordPlayerMessage(String playerName, String message) {
        playerConversationHistory.computeIfAbsent(playerName, k -> new ArrayList<>())
            .add("PLAYER: " + message);

        // track topic frequency
        String topic = extractTopic(message);
        playerTopicCounts.computeIfAbsent(playerName, k -> new HashMap<>())
            .merge(topic, 1, Integer::sum);

        // keep history manageable
        List<String> history = playerConversationHistory.get(playerName);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    /**
     * record a response from aurora/nullpointer
     */
    public static void recordEntityResponse(String playerName, String entity, String response) {
        playerConversationHistory.computeIfAbsent(playerName, k -> new ArrayList<>())
            .add(entity + ": " + response);
    }

    /**
     * get conversation context for better responses
     */
    public static String getConversationContext(String playerName) {
        List<String> history = playerConversationHistory.get(playerName);
        if (history == null || history.isEmpty()) {
            return "first_interaction";
        }

        // analyze recent conversation patterns
        int recentMessages = Math.min(5, history.size());
        List<String> recent = history.subList(history.size() - recentMessages, history.size());

        long playerMessages = recent.stream().filter(msg -> msg.startsWith("PLAYER:")).count();
        long entityMessages = recent.stream().filter(msg -> !msg.startsWith("PLAYER:")).count();

        if (playerMessages > entityMessages * 2) {
            return "chatty_player"; // player is talking a lot
        } else if (entityMessages > playerMessages) {
            return "entity_dominant"; // entity is dominating conversation
        }

        return "balanced_conversation";
    }

    /**
     * check how many times player has mentioned a topic
     */
    public static int getTopicMentionCount(String playerName, String topic) {
        Map<String, Integer> topics = playerTopicCounts.get(playerName);
        if (topics == null) return 0;
        return topics.getOrDefault(topic.toLowerCase(), 0);
    }

    /**
     * check if player keeps repeating the same things
     */
    public static boolean isPlayerRepeating(String playerName, String message) {
        List<String> history = playerConversationHistory.get(playerName);
        if (history == null || history.size() < 3) return false;

        String normalizedMessage = normalizeMessage(message);

        // check last 3 player messages for similarity
        long similarCount = history.stream()
            .filter(msg -> msg.startsWith("PLAYER:"))
            .map(msg -> msg.substring(7)) // remove "player: " prefix
            .map(ConversationTracker::normalizeMessage)
            .filter(msg -> calculateSimilarity(msg, normalizedMessage) > 0.7)
            .count();

        return similarCount >= 2;
    }

    /**
     * get a conversation-aware response modifier
     */
    public static String getResponseModifier(String playerName, String message) {
        if (isPlayerRepeating(playerName, message)) {
            return "repetitive";
        }

        String context = getConversationContext(playerName);
        if (context.equals("chatty_player")) {
            return "overwhelmed";
        }

        int helpCount = getTopicMentionCount(playerName, "help");
        if (helpCount > 3) {
            return "desperate"; // player keeps asking for help
        }

        int privacyCount = getTopicMentionCount(playerName, "privacy");
        if (privacyCount > 2) {
            return "paranoid"; // player obsessed with privacy
        }

        return "normal";
    }

    /**
     * clear conversation history for a player
     */
    public static void clearPlayerHistory(String playerName) {
        playerConversationHistory.remove(playerName);
        playerTopicCounts.remove(playerName);
    }

    private static String extractTopic(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("help")) return "help";
        if (lower.contains("privacy") || lower.contains("personal")) return "privacy";
        if (lower.contains("time") || lower.contains("clock")) return "time";
        if (lower.contains("scared") || lower.contains("afraid")) return "fear";
        if (lower.contains("mining") || lower.contains("building")) return "gameplay";
        if (lower.contains("aurora")) return "aurora";
        if (lower.contains("nullpointer")) return "nullpointer";

        // default to first meaningful word
        String[] words = lower.split(" ");
        for (String word : words) {
            if (word.length() > 3) return word;
        }

        return "general";
    }

    private static String normalizeMessage(String message) {
        return message.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static double calculateSimilarity(String msg1, String msg2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(msg1.split(" ")));
        Set<String> words2 = new HashSet<>(Arrays.asList(msg2.split(" ")));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }
}
