package lol.cqllmetoxic.nullpointerentity.events.chat;

import java.util.HashMap;
import java.util.Map;

/**
 * manages response cooldowns to prevent spam and make interactions feel more natural
 */
public class ResponseCooldowns {

    private static final Map<String, Long> playerLastResponse = new HashMap<>();
    private static final Map<String, Map<String, Long>> playerKeywordCooldowns = new HashMap<>();

    // cooldown times in milliseconds
    private static final long GENERAL_COOLDOWN = 500; // 0.5 seconds between any responses (reduced from 1 second)
    private static final long KEYWORD_COOLDOWN = 5000; // 5 seconds before same keyword triggers again (reduced from 8 seconds)
    private static final long ENTITY_MENTION_COOLDOWN = 2000; // 2 seconds for entity mentions (reduced from 3 seconds)

    /**
     * check if player can receive a response (general cooldown)
     */
    public static boolean canRespond(String playerName) {
        long currentTime = System.currentTimeMillis();
        Long lastResponse = playerLastResponse.get(playerName);

        if (lastResponse == null) {
            return true;
        }

        return (currentTime - lastResponse) >= GENERAL_COOLDOWN;
    }

    /**
     * check if specific keyword can trigger a response for this player
     */
    public static boolean canRespondToKeyword(String playerName, String keyword) {
        long currentTime = System.currentTimeMillis();

        Map<String, Long> playerKeywords = playerKeywordCooldowns.get(playerName);
        if (playerKeywords == null) {
            return true;
        }

        Long lastKeywordUse = playerKeywords.get(keyword);
        if (lastKeywordUse == null) {
            return true;
        }

        // entity mentions have shorter cooldowns
        long cooldownTime = (keyword.equals("aurora") || keyword.equals("nullpointer")) ?
            ENTITY_MENTION_COOLDOWN : KEYWORD_COOLDOWN;

        return (currentTime - lastKeywordUse) >= cooldownTime;
    }

    /**
     * record that a response was sent to this player
     */
    public static void recordResponse(String playerName, String keyword) {
        long currentTime = System.currentTimeMillis();

        // record general response time
        playerLastResponse.put(playerName, currentTime);

        // record keyword-specific time
        playerKeywordCooldowns.computeIfAbsent(playerName, k -> new HashMap<>())
            .put(keyword, currentTime);
    }

    /**
     * clear cooldowns for a player (when they disconnect)
     */
    public static void clearPlayerCooldowns(String playerName) {
        playerLastResponse.remove(playerName);
        playerKeywordCooldowns.remove(playerName);
    }

    /**
     * get remaining cooldown time in seconds
     */
    public static int getRemainingCooldown(String playerName) {
        Long lastResponse = playerLastResponse.get(playerName);
        if (lastResponse == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastResponse;
        long remaining = GENERAL_COOLDOWN - elapsed;

        return remaining > 0 ? (int)(remaining / 1000) : 0;
    }
}
