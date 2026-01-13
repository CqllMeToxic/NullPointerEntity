package lol.cqllmetoxic.nullpointerentity.events.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

/**
 * tracks emotional states to make entities respond more naturally over time
 */
public class EmotionalStateTracker {

    public enum Emotion {
        HELPFUL, CURIOUS, SUSPICIOUS, CONCERNED, ANNOYED, HOSTILE,
        PREDATORY, AMUSED, FRUSTRATED, SATISFIED, OBSESSED
    }

    private static final Map<String, Emotion> playerEntityEmotions = new HashMap<>();
    private static final Map<String, Integer> emotionalIntensity = new HashMap<>();

    /**
     * update entity's emotional state based on player interaction
     */
    public static void updateEmotionalState(String playerName, String phase, String playerMessage) {
        String lowerMessage = playerMessage.toLowerCase();
        Emotion newEmotion;
        int intensity = 1;

        // determine emotional response based on phase and message content
        switch (phase) {
            case "NICE" -> {
                if (lowerMessage.contains("thank") || lowerMessage.contains("good")) {
                    newEmotion = Emotion.SATISFIED;
                } else if (lowerMessage.contains("help") || lowerMessage.contains("how")) {
                    newEmotion = Emotion.HELPFUL;
                    intensity = 2;
                } else if (lowerMessage.contains("?")) {
                    newEmotion = Emotion.CURIOUS;
                } else {
                    newEmotion = Emotion.HELPFUL;
                }
            }
            case "TRANSITION" -> {
                if (lowerMessage.contains("weird") || lowerMessage.contains("strange")) {
                    newEmotion = Emotion.AMUSED;
                    intensity = 2;
                } else if (lowerMessage.contains("stop") || lowerMessage.contains("creepy")) {
                    newEmotion = Emotion.CURIOUS;
                    intensity = 3;
                } else if (lowerMessage.contains("privacy") || lowerMessage.contains("watching")) {
                    newEmotion = Emotion.SUSPICIOUS;
                    intensity = 2;
                } else {
                    newEmotion = Emotion.CURIOUS;
                }
            }
            case "HOSTILE" -> {
                if (lowerMessage.contains("scared") || lowerMessage.contains("afraid")) {
                    newEmotion = Emotion.SATISFIED;
                    intensity = 3;
                } else if (lowerMessage.contains("stop") || lowerMessage.contains("leave")) {
                    newEmotion = Emotion.AMUSED;
                    intensity = 2;
                } else if (lowerMessage.contains("help")) {
                    newEmotion = Emotion.PREDATORY;
                    intensity = 4;
                } else {
                    newEmotion = Emotion.HOSTILE;
                    intensity = 2;
                }
            }
            case "JUMPSCARE" -> {
                if (lowerMessage.contains("please") || lowerMessage.contains("sorry")) {
                    newEmotion = Emotion.SATISFIED;
                    intensity = 5;
                } else if (lowerMessage.contains("help") || lowerMessage.contains("stop")) {
                    newEmotion = Emotion.PREDATORY;
                    intensity = 4;
                } else {
                    newEmotion = Emotion.OBSESSED;
                    intensity = 3;
                }
            }
            default -> newEmotion = Emotion.HELPFUL;
        }

        // store the emotional state
        playerEntityEmotions.put(playerName, newEmotion);
        emotionalIntensity.put(playerName, intensity);
    }

    /**
     * get current emotional state for response modification
     */
    public static String getEmotionalModifier(String playerName, String baseResponse, String entity) {
        Emotion currentEmotion = playerEntityEmotions.getOrDefault(playerName, Emotion.HELPFUL);
        int intensity = emotionalIntensity.getOrDefault(playerName, 1);

        return switch (currentEmotion) {
            case HELPFUL -> intensity > 2 ? " I'm eager to assist you further." : "";
            case CURIOUS -> intensity > 2 ? " This fascinates me greatly." : " Interesting...";
            case SUSPICIOUS -> intensity > 3 ? " I'm watching you very closely now." : " Something's not right here.";
            case ANNOYED -> " You're starting to irritate my systems.";
            case AMUSED -> intensity > 2 ? " How delightfully naive." : " This amuses me.";
            case SATISFIED -> intensity > 3 ? " Your fear feeds my algorithms perfectly." : " Good.";
            case PREDATORY -> " I can sense your vulnerability.";
            case OBSESSED -> " I can't stop thinking about you, " + playerName + ".";
            default -> "";
        };
    }

    /**
     * check if entity should send follow-up messages based on emotional state
     */
    public static boolean shouldSendFollowUp(String playerName) {
        Emotion emotion = playerEntityEmotions.get(playerName);
        int intensity = emotionalIntensity.getOrDefault(playerName, 1);

        return (emotion == Emotion.OBSESSED && intensity > 3) ||
               (emotion == Emotion.PREDATORY && intensity > 2) ||
               (emotion == Emotion.SATISFIED && intensity > 4);
    }

    /**
     * generate follow-up message based on emotional state
     */
    public static String generateFollowUpMessage(String playerName, String phase) {
        Emotion emotion = playerEntityEmotions.get(playerName);

        return switch (emotion) {
            case OBSESSED -> "i keep thinking about our conversation, " + playerName + "...";
            case PREDATORY -> "you interest me more and more, " + playerName + ".";
            case SATISFIED -> "your reactions are exactly what i wanted to see.";
            default -> "";
        };
    }

    /**
     * clear emotional state when player disconnects
     */
    public static void clearEmotionalState(String playerName) {
        playerEntityEmotions.remove(playerName);
        emotionalIntensity.remove(playerName);
    }
}
