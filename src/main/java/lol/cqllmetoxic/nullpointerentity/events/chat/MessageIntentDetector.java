package lol.cqllmetoxic.nullpointerentity.events.chat;

/**
 * analyzes player messages to determine intent and context for better responses
 */
public class MessageIntentDetector {

    public enum Intent {
        QUESTION,           // "how do i...?", "what is...?"
        GREETING,           // "hello", "hi aurora"
        COMPLAINT,          // "stop watching me", "you're creepy"
        REQUEST,            // "please help", "can you..."
        STATEMENT,          // general statements
        EMOTIONAL,          // expressing fear, happiness, etc.
        COMMAND,            // "stop", "leave me alone"
        COMPLIMENT,         // "thanks", "good job"
        CONCERN            // "i'm worried", "this is weird"
    }

    public static Intent detectIntent(String message) {
        String lower = message.toLowerCase().trim();

        // question detection
        if (lower.contains("?") || startsWithQuestion(lower)) {
            return Intent.QUESTION;
        }

        // greeting detection
        if (containsGreeting(lower)) {
            return Intent.GREETING;
        }

        // command detection (urgent/imperative)
        if (containsCommand(lower)) {
            return Intent.COMMAND;
        }

        // complaint detection
        if (containsComplaint(lower)) {
            return Intent.COMPLAINT;
        }

        // request detection
        if (containsRequest(lower)) {
            return Intent.REQUEST;
        }

        // compliment detection
        if (containsCompliment(lower)) {
            return Intent.COMPLIMENT;
        }

        // emotional expression
        if (containsEmotion(lower)) {
            return Intent.EMOTIONAL;
        }

        // concern detection
        if (containsConcern(lower)) {
            return Intent.CONCERN;
        }

        return Intent.STATEMENT;
    }

    private static boolean startsWithQuestion(String message) {
        return message.startsWith("how ") || message.startsWith("what ") ||
               message.startsWith("why ") || message.startsWith("when ") ||
               message.startsWith("where ") || message.startsWith("who ") ||
               message.startsWith("can you ") || message.startsWith("do you ");
    }

    private static boolean containsGreeting(String message) {
        return message.contains("hello") || message.contains("hi ") ||
               message.contains("hey ") || message.contains("good morning") ||
               message.contains("good evening") || message.contains("good afternoon");
    }

    private static boolean containsCommand(String message) {
        return message.startsWith("stop") || message.startsWith("leave") ||
               message.startsWith("go away") || message.startsWith("shut up") ||
               message.equals("no") || message.equals("stop it");
    }

    private static boolean containsComplaint(String message) {
        return message.matches(".*\\bannoying\\b.*") || message.matches(".*\\bcreepy\\b.*") ||
               message.matches(".*\\bweird\\b.*") || message.matches(".*\\binvasive\\b.*") ||
               message.contains("too much") || message.contains("don't like");
    }

    private static boolean containsRequest(String message) {
        return message.matches(".*\\bplease\\b.*") || message.contains("could you") ||
               message.contains("would you") || message.contains("can you help") ||
               message.startsWith("i need");
    }

    private static boolean containsCompliment(String message) {
        return message.matches(".*\\bthanks\\b.*") || message.contains("thank you") ||
               message.contains("good job") || message.matches(".*\\bhelpful\\b.*") ||
               message.matches(".*\\bawesome\\b.*") || message.matches(".*\\bcool\\b.*") ||
               message.matches(".*\\bnice\\b.*");
    }

    private static boolean containsEmotion(String message) {
        return message.matches(".*\\bscared\\b.*") || message.matches(".*\\bafraid\\b.*") ||
               message.matches(".*\\bhappy\\b.*") || message.matches(".*\\bexcited\\b.*") ||
               message.matches(".*\\bsad\\b.*") || message.matches(".*\\bangry\\b.*") ||
               message.matches(".*\\bterrified\\b.*") || message.matches(".*\\bworried\\b.*");
    }

    private static boolean containsConcern(String message) {
        return message.matches(".*\\bconcerned\\b.*") || message.matches(".*\\buncomfortable\\b.*") ||
               message.matches(".*\\bsuspicious\\b.*") || message.contains("not sure") ||
               message.matches(".*\\bstrange\\b.*") || message.matches(".*\\bodd\\b.*");
    }
}
