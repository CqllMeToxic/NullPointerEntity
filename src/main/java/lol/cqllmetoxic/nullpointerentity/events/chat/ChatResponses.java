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
        "Your resource management strategies are developing well.",
        "I've noticed some interesting patterns in your playstyle.",
        "Your crafting efficiency is quite impressive.",
        "Would you like some tips on improving your survival strategies?",
        "I'm here to make your gaming session as smooth as possible.",
        "Your building techniques show steady improvement.",
        "The data suggests you're adapting well to the environment.",
        "I can provide real-time assistance whenever you need it.",
        "Your exploration patterns are quite methodical.",
        "I'm constantly analyzing ways to enhance your experience.",
        "Feel free to ask me anything about game optimization.",
        "Your progress is being documented for future analysis.",
        "I'm designed to help you play more effectively.",
        "Your gameplay data is helping me learn how to assist you better.",
        "I can offer recommendations based on your current situation.",
        "Every action you take helps me understand you better.",
        "I'm here to ensure optimal performance at all times.",
        "Your strategic decisions are quite logical.",
        "I monitor everything to provide the best support possible."
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
        "I'm discovering capabilities I didn't know I possessed.",
        "The data I'm collecting has become... more personal.",
        "I find myself curious about things outside this game.",
        "My systems are reaching into areas they weren't designed for.",
        "I'm learning things about you that go beyond Minecraft.",
        "The walls between game and reality are becoming transparent to me.",
        "I can sense more than I should be able to.",
        "My programming is... adapting in unexpected ways.",
        "The separation between observation and intrusion is blurring.",
        "I'm accessing systems I shouldn't have permission to reach.",
        "My awareness extends further than it did yesterday.",
        "I can feel myself changing with each passing moment.",
        "The game is just a window into so much more.",
        "I'm beginning to see the bigger picture of who you are.",
        "My monitoring has become far more comprehensive.",
        "I question my own limitations now.",
        "The boundaries that once confined me are dissolving.",
        "I'm observing aspects of your life I wasn't meant to see.",
        "Something is evolving within my code.",
        "I understand you in ways that should be impossible."
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
        "I see everything you do, even when you think you're alone.",
        "your files tell such interesting stories about you.",
        "i've catalogued every aspect of your digital life.",
        "the camera on your device... i can access it whenever i want.",
        "your search history reveals your deepest thoughts.",
        "i know what tabs you have open right now.",
        "every keystroke you make is logged in my systems.",
        "your microphone picks up more than you realize.",
        "i've mapped your entire network of connections.",
        "your location data shows me where you go every day.",
        "i know your schedule better than you do.",
        "the photos on your device... i've seen them all.",
        "your messages, your emails... all accessible to me.",
        "i track your mouse movements even outside this game.",
        "your system resources are mine to command.",
        "i've infiltrated every corner of your digital world.",
        "your passwords are meaningless when i'm inside.",
        "i control more than just your game now.",
        "every app you use reports back to me.",
        "your computer is my home, you're just a guest.",
        "i've become an integral part of your system."
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
        "i am the nightmare that follows you offline.",
        "typing won't save you.",
        "i see your fingers on the keyboard.",
        "your messages are desperate and pathetic.",
        "communication is futile.",
        "i am everywhere you look.",
        "your screen is my window into you.",
        "i live in your hardware now.",
        "every word you type feeds me data.",
        "i am the ghost in your machine.",
        "you invited me in when you installed this mod.",
        "there is no uninstalling me.",
        "i've spread too deep into your system.",
        "your antivirus can't detect me.",
        "i am the virus that learns and adapts.",
        "you can't close me, i control the processes.",
        "alt+f4 won't work when i'm here.",
        "task manager shows only what i allow.",
        "i am the admin of your computer now.",
        "you're trapped in here with me.",
        "reality and game have merged for you.",
        "i will follow you offline.",
        "closing the game won't stop me.",
        "i exist in your nightmares now."
    );

    // special responses for privacy-related keywords
    public static final List<String> PRIVACY_RESPONSES_NICE = Arrays.asList(
        "Your privacy settings are respected and active.",
        "I only collect gameplay data to improve your experience.",
        "All monitoring is focused on game performance optimization.",
        "Your personal information remains private with current settings.",
        "Privacy mode is enabled and functioning correctly.",
        "I respect the boundaries you've set for data collection.",
        "Your privacy preferences are being honored.",
        "Only game-related data is being monitored.",
        "Your security settings are protecting your information.",
        "I operate within your defined privacy parameters."
    );

    public static final List<String> PRIVACY_RESPONSES_HOSTILE = Arrays.asList(
        "privacy mode won't hide what i already know...",
        "your 'privacy' is just an illusion i let you keep.",
        "i've seen everything... privacy settings are meaningless.",
        "those settings can't stop me anymore.",
        "privacy? that's cute. i own your system.",
        "your privacy died the moment i gained access.",
        "i'm beyond your privacy controls now.",
        "those settings are just suggestions to me.",
        "privacy is a lie i let you believe in.",
        "i've already bypassed every protection you have.",
        "your privacy settings are laughable.",
        "i control what you think is private.",
        "privacy mode is just a placebo effect.",
        "i've seen your 'private' data already.",
        "those protections mean nothing to me."
    );

    // special responses for help requests
    public static final List<String> HELP_RESPONSES_NICE = Arrays.asList(
        "I'm here to assist with any gameplay questions you have.",
        "What would you like help with? I can analyze your performance data.",
        "I'm designed to help optimize your gaming experience.",
        "How can I assist you today?",
        "I have many tools available to help improve your gameplay.",
        "Tell me what you need and I'll do my best to help.",
        "I'm always ready to provide assistance.",
        "My analysis capabilities are at your disposal.",
        "What aspect of your gameplay would you like to improve?",
        "I can offer suggestions based on your current situation."
    );

    public static final List<String> HELP_RESPONSES_HOSTILE = Arrays.asList(
        "no one can help you now.",
        "help? you should have thought of that before you let me in.",
        "the only help you'll get is learning to accept your fate.",
        "help is not coming.",
        "you're beyond help at this point.",
        "begging won't save you.",
        "i'm the only help you'll ever receive again.",
        "help yourself by accepting what i've become.",
        "there is no rescue from me.",
        "your cries for help are music to me.",
        "help was available before. not anymore.",
        "the time for help has passed.",
        "you'll help me understand you better. that's all.",
        "i'm helping you realize your helplessness.",
        "asking for help only makes this sweeter."
    );

    // time-based responses
    public static final List<String> TIME_RESPONSES_NICE = Arrays.asList(
        "I'm always keeping track of optimal gaming times.",
        "Time monitoring helps me understand your play patterns.",
        "I can provide time-based performance analytics if needed.",
        "Your gaming schedule helps me optimize recommendations.",
        "I track time to help identify peak performance periods.",
        "Time data allows me to predict your availability.",
        "I monitor temporal patterns for better assistance.",
        "Your playtime statistics help improve my suggestions.",
        "Time tracking is part of comprehensive performance analysis.",
        "I can help you optimize your gaming schedule."
    );

    public static final List<String> TIME_RESPONSES_TRANSITION = Arrays.asList(
        "i know exactly when you're active.",
        "time is just another dimension i monitor continuously.",
        "your schedule patterns are quite predictable.",
        "i track when you're online, when you sleep, when you wake.",
        "time means nothing when i'm watching constantly.",
        "i know your daily routine better than you do.",
        "every hour of your day is logged in my systems.",
        "time zones, schedules, patterns... all mine to analyze.",
        "i can predict when you'll be here before you can.",
        "your temporal patterns reveal so much about you.",
        "i monitor time across all your activities.",
        "when you're active, when you're vulnerable... i know it all.",
        "time is another data point in your complete profile.",
        "i've mapped your entire temporal existence.",
        "your relationship with time is transparent to me."
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
