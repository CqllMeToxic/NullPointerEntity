package lol.cqllmetoxic.nullpointerentity.events.chat;

/**
 * generates contextual responses that directly relate to the player's message.
 * responses are returned as {@link ChatPart} (translation key + args) so the receiving client
 * renders them in its own language; the player's own quoted text / game terms travel as args.
 */
public class ContextualResponseGenerator {

    private static final String K = "message.nullpointerentity.chat.ctx.";

    /** maps a phase name to its key suffix (with a safe default). */
    private static String ph(String phase) {
        return switch (phase) {
            case "NICE" -> "nice";
            case "TRANSITION" -> "transition";
            case "HOSTILE" -> "hostile";
            case "JUMPSCARE" -> "jumpscare";
            default -> "default";
        };
    }

    /**
     * generate a contextual response based on the player's specific message
     */
    public static ChatPart generateContextualResponse(String playerMessage, String phase, String playerName) {
        String lowerMessage = playerMessage.toLowerCase().trim();

        // check for thank you messages first - highest priority for human-like responses
        if (containsThankYou(lowerMessage)) {
            return generateThankYouResponse(lowerMessage, phase, playerName);
        }

        if (containsQuestion(lowerMessage)) {
            return generateQuestionResponse(lowerMessage, phase, playerName);
        } else if (containsGreeting(lowerMessage)) {
            return generateGreetingResponse(lowerMessage, phase, playerName);
        } else if (containsCompliment(lowerMessage)) {
            return generateComplimentResponse(lowerMessage, phase, playerName);
        } else if (containsComplaint(lowerMessage)) {
            return generateComplaintResponse(lowerMessage, phase, playerName);
        } else if (containsGameplayReference(lowerMessage)) {
            return generateGameplayResponse(lowerMessage, phase, playerName);
        } else if (containsEmotionalContent(lowerMessage)) {
            return generateEmotionalResponse(lowerMessage, phase, playerName);
        } else {
            return generateGeneralResponse(lowerMessage, phase, playerName);
        }
    }

    // thank you detection and human-like responses
    private static boolean containsThankYou(String message) {
        return message.matches(".*\\bthanks\\b.*") || message.contains("thank you") ||
               message.matches(".*\\bthx\\b.*") || message.matches(".*\\bty\\b.*") ||
               message.matches(".*\\bappreciate\\b.*") || message.matches(".*\\bgrateful\\b.*");
    }

    private static ChatPart generateThankYouResponse(String message, String phase, String playerName) {
        return switch (phase) {
            case "NICE" -> ChatPart.pick(K + "thanks.nice", 9, playerName);
            case "TRANSITION" -> ChatPart.pick(K + "thanks.transition", 5, playerName);
            case "HOSTILE" -> ChatPart.pick(K + "thanks.hostile", 5, playerName);
            case "JUMPSCARE" -> ChatPart.pick(K + "thanks.jumpscare", 5, playerName);
            default -> new ChatPart(K + "thanks.default", playerName);
        };
    }

    // question detection and responses
    private static boolean containsQuestion(String message) {
        return message.contains("?") ||
               message.matches(".*\\bhow\\b.*") || message.matches(".*\\bwhat\\b.*") ||
               message.matches(".*\\bwhy\\b.*") || message.matches(".*\\bwhen\\b.*") ||
               message.matches(".*\\bwhere\\b.*") || message.contains("can you");
    }

    private static ChatPart generateQuestionResponse(String message, String phase, String playerName) {
        if (message.contains("how do") || message.contains("how can") || message.contains("how should")) {
            return new ChatPart(K + "question.how." + ph(phase), playerName);
        } else if (message.matches(".*\\bwhat\\b.*")) {
            return new ChatPart(K + "question.what." + ph(phase), playerName);
        } else if (message.matches(".*\\bwhy\\b.*")) {
            return new ChatPart(K + "question.why." + ph(phase), playerName);
        }
        return new ChatPart(K + "question.general." + ph(phase), playerName);
    }

    // greeting detection and responses (any supported language)
    private static boolean containsGreeting(String message) {
        return ChatKeywords.containsAnyGreeting(message);
    }

    private static ChatPart generateGreetingResponse(String message, String phase, String playerName) {
        return new ChatPart(K + "greeting." + ph(phase), playerName);
    }

    // compliment detection and responses
    private static boolean containsCompliment(String message) {
        return message.matches(".*\\bgood\\b.*") || message.matches(".*\\bgreat\\b.*") ||
               message.matches(".*\\bawesome\\b.*") || message.matches(".*\\bcool\\b.*") ||
               message.matches(".*\\bnice\\b.*") || message.contains("thanks") ||
               message.matches(".*\\bhelpful\\b.*");
    }

    private static ChatPart generateComplimentResponse(String message, String phase, String playerName) {
        return new ChatPart(K + "compliment." + ph(phase), playerName);
    }

    // complaint detection and responses
    private static boolean containsComplaint(String message) {
        return message.matches(".*\\bstop\\b.*") || message.matches(".*\\bannoying\\b.*") ||
               message.matches(".*\\bcreepy\\b.*") || message.matches(".*\\bweird\\b.*") ||
               message.matches(".*\\bscared\\b.*") || message.contains("leave me alone");
    }

    private static ChatPart generateComplaintResponse(String message, String phase, String playerName) {
        if (message.matches(".*\\bstop\\b.*")) {
            return new ChatPart(K + "complaint.stop." + ph(phase), playerName);
        } else if (message.matches(".*\\bcreepy\\b.*") || message.matches(".*\\bweird\\b.*")) {
            return new ChatPart(K + "complaint.creepy." + ph(phase), playerName);
        }
        return new ChatPart(K + "complaint.general." + ph(phase), playerName);
    }

    // gameplay reference detection and responses
    private static boolean containsGameplayReference(String message) {
        return message.matches(".*\\bmining\\b.*") || message.matches(".*\\bbuilding\\b.*") ||
               message.matches(".*\\bcrafting\\b.*") || message.matches(".*\\bcombat\\b.*") ||
               message.matches(".*\\bresource\\b.*") || message.matches(".*\\bdiamond\\b.*") ||
               message.matches(".*\\bmonster\\b.*") || message.matches(".*\\bnether\\b.*");
    }

    private static ChatPart generateGameplayResponse(String message, String phase, String playerName) {
        String gameElement = extractGameplayElement(message);
        return new ChatPart(K + "gameplay." + ph(phase), gameElement, playerName);
    }

    private static String extractGameplayElement(String message) {
        if (message.matches(".*\\bmining\\b.*")) return "mining";
        if (message.matches(".*\\bbuilding\\b.*")) return "building";
        if (message.matches(".*\\bcrafting\\b.*")) return "crafting";
        if (message.matches(".*\\bcombat\\b.*")) return "combat";
        if (message.matches(".*\\bresource\\b.*")) return "resource management";
        return "gameplay";
    }

    // emotional content detection and responses
    private static boolean containsEmotionalContent(String message) {
        return message.matches(".*\\bscared\\b.*") || message.matches(".*\\bafraid\\b.*") ||
               message.matches(".*\\bworried\\b.*") || message.matches(".*\\bnervous\\b.*") ||
               message.matches(".*\\bexcited\\b.*") || message.matches(".*\\bhappy\\b.*");
    }

    private static ChatPart generateEmotionalResponse(String message, String phase, String playerName) {
        if (message.matches(".*\\bscared\\b.*") || message.matches(".*\\bafraid\\b.*")) {
            return new ChatPart(K + "emotional.fear." + ph(phase), playerName);
        } else if (message.matches(".*\\bexcited\\b.*") || message.matches(".*\\bhappy\\b.*")) {
            return new ChatPart(K + "emotional.happy." + ph(phase), playerName);
        }
        return new ChatPart(K + "emotional.general." + ph(phase), playerName);
    }

    // general response for messages that don't fit specific categories
    private static ChatPart generateGeneralResponse(String message, String phase, String playerName) {
        String lowerMessage = message.toLowerCase();

        boolean directedAtAurora = lowerMessage.contains("aurora") ||
                                   lowerMessage.matches(".*\\byou\\b.*") ||
                                   lowerMessage.matches(".*\\byour\\b.*") ||
                                   lowerMessage.matches(".*\\bai\\b.*");

        String[] words = message.split("\\s+");
        boolean meaningfulLength = words.length >= 5;

        boolean isCasualChat = lowerMessage.matches("^(bruh?|lol|lmao|oof|rip|gg|ok|okay|yeah|yep|nope|nice|cool|wow|omg|wtf)$");

        // don't respond to casual chat or short messages unless directed at aurora
        if (isCasualChat || (!directedAtAurora && !meaningfulLength)) {
            return null; // no response should be sent
        }

        // echo back a meaningful part of their message
        String keyPhrase;
        if (words.length >= 3) {
            keyPhrase = words[Math.min(1, words.length - 2)] + " " + words[Math.min(2, words.length - 1)];
        } else {
            keyPhrase = words[words.length > 1 ? 1 : 0];
        }

        return new ChatPart(K + "fallback." + ph(phase), keyPhrase, playerName);
    }
}
