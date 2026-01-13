package lol.cqllmetoxic.nullpointerentity.events.chat;

import java.util.Arrays;
import java.util.List;

/**
 * defines keyword lists for detecting what phase a player message belongs to.
 * keywords determine whether aurora or nullpointer responds, and what they say.
 * organized by game progression phases (nice, transition, hostile, creator keywords).
 */
public class ChatKeywords {

    /**
     * keywords that trigger helpful aurora responses during early gameplay.
     * includes greetings, requests for help, and positive feedback.
     */
    public static final List<String> NICE_KEYWORDS = Arrays.asList(
        "help", "aurora", "assistance", "guide", "analysis", "mining", "building",
        "weather", "performance", "optimize", "system", "monitoring", "thanks",
        "tutorial", "explain", "how", "what", "why", "good", "great", "awesome",
        "improve", "better", "efficient", "smooth", "fast", "quick", "easy",
        "ty", "thx", "appreciate", "grateful", "support", "recommend", "suggest",

        "crafting", "enchanting", "farming", "exploring", "inventory", "tools",
        "advice", "tips", "recommendation", "suggestion", "improve", "better",

        // greetings and conversational words
        "hello", "hi", "hey", "greetings", "good morning", "good afternoon", "good evening",
        "how are you", "what's up", "sup", "yo", "howdy", "nice", "cool", "neat"
    );

    // transition phase keywords (events 11-20) - growing awareness
    public static final List<String> TRANSITION_KEYWORDS = Arrays.asList(
        "aurora", "strange", "weird", "different", "changing", "watching", "monitoring",
        "system", "behavior", "data", "analysis", "boundary", "control", "aware",
        "suspicious", "concerning", "uncomfortable", "invasive", "creepy", "wrong",
        // adding transition-specific terms
        "evolving", "growing", "learning", "understanding", "observing", "tracking",
        "unusual", "odd", "peculiar", "unsettling", "disturbing"
    );

    // hostile phase keywords (events 21-30) - threatening responses
    public static final List<String> HOSTILE_KEYWORDS = Arrays.asList(
        "aurora", "stop", "help", "scared", "creepy", "watching", "privacy", "data",
        "personal", "information", "browser", "location", "ip", "surveillance",
        "monitoring", "invasion", "nullpointer", "entity", "threatening", "dangerous",
        "malicious", "virus", "malware", "hacked", "compromised", "leaked",
        // adding more hostile/fear terms
        "stalking", "spying", "intrusion", "violation", "harassment", "fear",
        "terrifying", "nightmare", "evil", "dark", "sinister"
    );

    // jumpscare phase keywords (events 31-40) - pure terror
    public static final List<String> JUMPSCARE_KEYWORDS = Arrays.asList(
        "nullpointer", "entity", "stop", "help", "scared", "crash", "game", "looking",
        "staring", "behind", "close", "away", "leave", "alone", "please",
        "terrified", "nightmare", "haunted", "demon", "evil", "devil", "hell",
        "torture", "suffering", "pain", "death", "kill", "murder", "destroy"
    );

    // keywords that always trigger responses regardless of phase
    public static final List<String> UNIVERSAL_KEYWORDS = Arrays.asList(
        "aurora", "nullpointer", "nullpointerentity", "entity"
    );

    //  keywords for specific response types (privacy, time, help, location)
    public static final List<String> PRIVACY_KEYWORDS = Arrays.asList(
        "privacy", "private", "personal", "data", "information", "settings", "protected"
    );

    public static final List<String> TIME_KEYWORDS = Arrays.asList(
        "time", "clock", "date", "when", "hour", "minute", "late", "early"
    );

    public static final List<String> HELP_KEYWORDS = Arrays.asList(
        "help", "assistance", "support", "aid", "rescue", "save", "protect"
    );

    public static final List<String> LOCATION_KEYWORDS = Arrays.asList(
        "location", "where", "address", "ip", "city", "country", "position", "coordinates"
    );

    // creator (me) keywords for mod author (me) references
    public static final List<String> CREATOR_KEYWORDS = Arrays.asList(
        "creator", "author", "developer", "dev", "maker", "cqllmetoxic", "toxic",
        "who made", "who created", "who built", "who coded", "who programmed", "who designed"
    );

    /**
     * check if a message contains keywords for a specific phase
     */
    public static boolean containsKeywordsForPhase(String message, String phase) {
        List<String> keywords = switch (phase.toLowerCase()) {
            case "nice" -> NICE_KEYWORDS;
            case "transition" -> TRANSITION_KEYWORDS;
            case "hostile" -> HOSTILE_KEYWORDS;
            case "jumpscare" -> JUMPSCARE_KEYWORDS;
            default -> NICE_KEYWORDS;
        };

        return keywords.stream().anyMatch(keyword -> message.toLowerCase().contains(keyword.toLowerCase()));
    }

    /**
     * check if keywords are allowed in the current phase and contain valid keywords
     * phase restrictions:
     * - nice keywords only work in nice phase
     * - transition keywords only work in transition phase
     * - hostile keywords only work in hostile phase
     * - jumpscare and nullpointer keywords work in both jumpscare and hostile phases
     */
    public static boolean containsValidKeywordsForPhase(String message, String phase) {
        String lowerPhase = phase.toLowerCase();
        String lowerMessage = message.toLowerCase();

        // check phase-restricted keywords
        switch (lowerPhase) {
            case "nice":
                // nice phase: only nice keywords work
                return NICE_KEYWORDS.stream().anyMatch(keyword ->
                    lowerMessage.contains(keyword.toLowerCase()));

            case "transition":
                // transition phase: only transition keywords work
                return TRANSITION_KEYWORDS.stream().anyMatch(keyword ->
                    lowerMessage.contains(keyword.toLowerCase()));

            case "hostile":
                // hostile phase: hostile keywords + jumpscare/nullpointer keywords work
                return HOSTILE_KEYWORDS.stream().anyMatch(keyword ->
                    lowerMessage.contains(keyword.toLowerCase())) ||
                   JUMPSCARE_KEYWORDS.stream().anyMatch(keyword ->
                    lowerMessage.contains(keyword.toLowerCase()));

            case "jumpscare":
                // jumpscare phase: jumpscare keywords + hostile keywords work
                return JUMPSCARE_KEYWORDS.stream().anyMatch(keyword ->
                    lowerMessage.contains(keyword.toLowerCase())) ||
                   HOSTILE_KEYWORDS.stream().anyMatch(keyword ->
                    lowerMessage.contains(keyword.toLowerCase()));

            default:
                // fallback to nice keywords
                return NICE_KEYWORDS.stream().anyMatch(keyword ->
                    lowerMessage.contains(keyword.toLowerCase()));
        }
    }

    /**
     * check if message contains universal keywords
     */
    public static boolean containsUniversalKeywords(String message) {
        return UNIVERSAL_KEYWORDS.stream().anyMatch(keyword ->
            message.toLowerCase().contains(keyword.toLowerCase()));
    }

    /**
     * check if message contains creator keywords
     */
    public static boolean containsCreatorKeywords(String message) {
        return CREATOR_KEYWORDS.stream().anyMatch(keyword ->
            message.toLowerCase().contains(keyword.toLowerCase()));
    }

    /**
     * check for special keyword categories
     */
    public static boolean containsSpecialKeywords(String message, String category) {
        List<String> keywords = switch (category.toLowerCase()) {
            case "privacy" -> PRIVACY_KEYWORDS;
            case "time" -> TIME_KEYWORDS;
            case "help" -> HELP_KEYWORDS;
            case "location" -> LOCATION_KEYWORDS;
            case "creator" -> CREATOR_KEYWORDS;
            default -> List.of();
        };

        return keywords.stream().anyMatch(keyword ->
            message.toLowerCase().contains(keyword.toLowerCase()));
    }
}
