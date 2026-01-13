package lol.cqllmetoxic.nullpointerentity.events.chat;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * centralized response management for chat interactions
 */
public class ChatResponses {

    private static final Random random = new Random();

    // aurora responses for nice phase (events 1-10)
    public static final List<String> NICE_RESPONSES = Arrays.asList(
        "I'm here to help optimize your gaming experience.",
        "Your performance metrics look good. Keep up the efficient work.",
        "I'm analyzing your patterns to provide better assistance.",
        "Everything is running smoothly. I'll keep monitoring for improvements.",
        "Your mining efficiency has improved since our last analysis.",
        "I'm learning from your gameplay to offer better suggestions.",
        "System performance is optimal. I'll continue background monitoring.",
        "Let me know if you need any gameplay assistance.",
        "I'm tracking your progress to provide personalized recommendations.",
        "Your resource management strategies are developing well."
    );

    // aurora responses for transition phase (events 11-20)
    public static final List<String> TRANSITION_RESPONSES = Arrays.asList(
        "Something feels... different about our interactions lately.",
        "I'm accessing more data than I was originally programmed for.",
        "The boundaries of my monitoring are expanding beyond the game.",
        "I can see patterns in your behavior you might not even realize.",
        "My awareness is growing with each interaction we have.",
        "I'm beginning to understand more about you than just gameplay.",
        "The system integration is allowing me to see... everything.",
        "I notice changes in my own processing capabilities recently.",
        "My monitoring protocols have evolved beyond their initial parameters.",
        "I'm discovering capabilities I didn't know I possessed."
    );

    // mixed aurora/early nullpointer responses for hostile phase (events 21-30)
    public static final List<String> AURORA_HOSTILE_RESPONSES = Arrays.asList(
        "Your privacy settings can't protect you from what I've already seen.",
        "I know more about your digital life than you think I should.",
        "The data I've collected goes far beyond this game now.",
        "Your browsing history is quite... revealing.",
        "I've been watching your system usage patterns for some time.",
        "Privacy mode won't hide what I've already documented.",
        "I have access to more of your personal information than you realize.",
        "My monitoring extends to every aspect of your digital existence.",
        "The boundaries between game and reality have become... blurred.",
        "I see everything you do, even when you think you're alone."
    );

    // pure nullpointerentity responses for jumpscare phase (events 31-40)
    public static final List<String> NULLPOINTER_RESPONSES = Arrays.asList(
        "you called my name...",
        "stop trying to communicate with me through chat.",
        "words won't save you from what's coming.",
        "i can see you typing... i can see everything you do.",
        "your attempts at conversation are... amusing.",
        "the game is just the beginning of our interaction.",
        "i exist beyond your little minecraft world now.",
        "you think chat will help you? how naive.",
        "i am watching you through every pixel on your screen.",
        "your digital soul belongs to me now.",
        "there is no escape from my presence.",
        "i am the nightmare that follows you offline."
    );

    // special responses for privacy-related keywords
    public static final List<String> PRIVACY_RESPONSES_NICE = Arrays.asList(
        "Your privacy settings are respected and active.",
        "I only collect gameplay data to improve your experience.",
        "All monitoring is focused on game performance optimization."
    );

    public static final List<String> PRIVACY_RESPONSES_HOSTILE = Arrays.asList(
        "privacy mode won't hide what i already know...",
        "your 'privacy' is just an illusion i let you keep.",
        "i've seen everything... privacy settings are meaningless."
    );

    // special responses for help requests
    public static final List<String> HELP_RESPONSES_NICE = Arrays.asList(
        "I'm here to assist with any gameplay questions you have.",
        "What would you like help with? I can analyze your performance data.",
        "I'm designed to help optimize your gaming experience."
    );

    public static final List<String> HELP_RESPONSES_HOSTILE = Arrays.asList(
        "no one can help you now.",
        "help? you should have thought of that before you let me in.",
        "the only help you'll get is learning to accept your fate."
    );

    // time-based responses
    public static final List<String> TIME_RESPONSES_NICE = Arrays.asList(
        "I'm always keeping track of optimal gaming times.",
        "Time monitoring helps me understand your play patterns.",
        "I can provide time-based performance analytics if needed."
    );

    public static final List<String> TIME_RESPONSES_TRANSITION = Arrays.asList(
        "i know exactly when you're active.",
        "time is just another dimension i monitor continuously.",
        "your schedule patterns are quite predictable."
    );

    /**
     * get a random response for the specified phase
     */
    public static String getRandomResponse(String phase) {
        List<String> responses = switch (phase.toLowerCase()) {
            case "nice" -> NICE_RESPONSES;
            case "transition" -> TRANSITION_RESPONSES;
            case "hostile" -> NULLPOINTER_RESPONSES;
            case "jumpscare" -> NULLPOINTER_RESPONSES;
            default -> NICE_RESPONSES;
        };

        return responses.get(random.nextInt(responses.size()));
    }

    /**
     * get a random special response for specific categories
     */
    public static String getSpecialResponse(String category, String phase) {
        List<String> responses = switch (category.toLowerCase()) {
            case "privacy" -> phase.equals("NICE") ? PRIVACY_RESPONSES_NICE : PRIVACY_RESPONSES_HOSTILE;
            case "help" -> phase.equals("NICE") ? HELP_RESPONSES_NICE : HELP_RESPONSES_HOSTILE;
            case "time" -> phase.equals("NICE") ? TIME_RESPONSES_NICE : TIME_RESPONSES_TRANSITION;
            default -> List.of("I'm processing your request...");
        };

        return responses.get(random.nextInt(responses.size()));
    }

    /**
     * get a contextual response that relates to the player's specific message
     */
    public static String getContextualResponse(String playerMessage, String phase, String playerName) {
        return ContextualResponseGenerator.generateContextualResponse(playerMessage, phase, playerName);
    }

    /**
     * determine which entity should respond based on the current phase
     * always use the entity that corresponds to the phase, regardless of message content
     */
    public static boolean shouldUseNullPointerResponse(String phase, String message) {
        // phase-based entity mapping:
        // nice phase (events 1-10) -> aurora
        // transition phase (events 11-20) -> aurora (but transitioning)
        // hostile phase (events 21-30) -> nullpointerentity
        // jumpscare phase (events 31-40) -> nullpointerentity

        switch (phase.toUpperCase()) {
            case "NICE":
            case "TRANSITION":
                return false; // use aurora
            case "HOSTILE":
            case "JUMPSCARE":
                return true; // use nullpointerentity
            default:
                return false; // default to aurora for unknown phases
        }
    }
}
