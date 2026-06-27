package lol.cqllmetoxic.nullpointerentity.events.chat;

import java.util.Arrays;
import java.util.List;

/**
 * defines keyword lists for detecting what phase a player message belongs to.
 * keywords determine whether aurora or nullpointer responds, and what they say.
 * organized by game progression phases (nice, transition, hostile, creator keywords).
 */
public class ChatKeywords {

    // matching is just message.contains(keyword), so i keep these short - shriek dumps your speech into
    // chat, and common/short words ("how", or "hi" in "this", "kill" in "skill") make aurora spam replies.

    /**
     * keywords that trigger helpful aurora responses during early gameplay.
     * includes direct address, help requests, and a couple of conversational greetings.
     */
    public static final List<String> NICE_KEYWORDS = Arrays.asList(
        "aurora", "help", "assistance", "optimize", "performance", "monitoring",
        "tutorial", "crafting", "enchanting", "recommend",
        // greetings - skipping "hi"/"yo" etc, they match inside other words
        "hello", "greetings", "howdy"
    );

    // transition phase keywords - growing awareness
    public static final List<String> TRANSITION_KEYWORDS = Arrays.asList(
        "aurora", "watching", "monitoring", "creepy", "suspicious", "invasive",
        "observing", "tracking", "unsettling", "disturbing", "evolving"
    );

    // hostile phase keywords - threatening responses
    public static final List<String> HOSTILE_KEYWORDS = Arrays.asList(
        "aurora", "nullpointer", "scared", "creepy", "watching", "privacy",
        "surveillance", "stalking", "spying", "threatening", "malware", "hacked",
        "nightmare", "terrifying", "sinister"
    );

    // jumpscare phase keywords
    public static final List<String> JUMPSCARE_KEYWORDS = Arrays.asList(
        "nullpointer", "scared", "crash", "staring", "behind", "leave", "please",
        "terrified", "nightmare", "haunted", "demon", "murder"
    );

    // keywords that always trigger responses regardless of phase
    public static final List<String> UNIVERSAL_KEYWORDS = Arrays.asList(
        "aurora", "nullpointer", "nullpointerentity"
    );

    //  keywords for specific response types (privacy, time, help, location)
    public static final List<String> PRIVACY_KEYWORDS = Arrays.asList(
        "privacy", "private"
    );

    public static final List<String> TIME_KEYWORDS = Arrays.asList(
        "time", "clock"
    );

    public static final List<String> HELP_KEYWORDS = Arrays.asList(
        "help", "assistance", "rescue"
    );

    public static final List<String> LOCATION_KEYWORDS = Arrays.asList(
        "location", "address", "coordinates"
    );

    // greetings across the mod's supported languages. distinctive multi-char/accented/CJK forms are
    // safe to match as substrings; short ambiguous tokens are matched as whole words below so they
    // don't fire inside unrelated words (e.g. "oi" inside "going").
    public static final List<String> GREETING_SUBSTRINGS = Arrays.asList(
        // en
        "hello", "good morning", "good afternoon", "good evening", "howdy", "greetings",
        // fr
        "bonjour", "bonsoir", "salut", "coucou",
        // es
        "hola", "buenos días", "buenos dias", "buenas tardes", "buenas noches", "buenas",
        // de
        "hallo", "guten morgen", "guten tag", "guten abend", "servus",
        // it
        "ciao", "salve", "buongiorno", "buonasera",
        // pt
        "olá", "bom dia", "boa tarde", "boa noite",
        // pl
        "cześć", "czesc", "dzień dobry", "dzien dobry", "witaj", "siema",
        // ru
        "привет", "здравствуй", "добрый день", "добрый вечер", "доброе утро",
        // ja
        "こんにちは", "こんばんは", "おはよう", "もしもし",
        // ko
        "안녕", "여보세요",
        // zh
        "你好", "您好", "哈喽", "早上好", "晚上好",
        // tr
        "merhaba", "selam", "günaydın", "gunaydin", "iyi akşamlar"
    );

    // short/ambiguous greeting tokens matched only as whole words
    public static final List<String> GREETING_WORDS = Arrays.asList(
        "hi", "hey", "yo", "sup", "oi", "ola", "hej", "hei", "moin", "ahoj"
    );

    /**
     * detects a greeting in any supported language. distinctive forms match as substrings; short
     * ambiguous tokens (hi, hey, oi, ...) match only as whole words.
     */
    public static boolean containsAnyGreeting(String message) {
        String lower = message.toLowerCase();
        for (String greeting : GREETING_SUBSTRINGS) {
            if (lower.contains(greeting)) {
                return true;
            }
        }
        for (String word : lower.split("[^\\p{L}]+")) {
            if (GREETING_WORDS.contains(word)) {
                return true;
            }
        }
        return false;
    }

    // creator (me) keywords for mod author (me) as well as Pryzmm and One Last Time (much love for the video btw <3) references
    public static final List<String> CREATOR_KEYWORDS = Arrays.asList(
        "creator", "developer", "maker", "cqllmetoxic", "toxic",
        "who made", "who created", "who built", "who coded", "who programmed", "who designed",
        "pryzmm", "one last time"
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
