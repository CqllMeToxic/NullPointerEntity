package lol.cqllmetoxic.nullpointerentity.events;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.events.chat.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * central hub for processing player chat messages and generating entity responses.
 * handles keyword detection, cooldown management, and delegates to specialized response handlers.
 * runs asynchronously to avoid lagging the game when generating responses.
 */
public class ChatResponseSystem {

    private static final Random random = new Random();

    /**
     * called whenever a player sends a chat message.
     * checks if the message contains keywords that aurora or nullpointer should respond to.
     * respects cooldowns and processes responses async to prevent lag.
     *
     * @param player the player who sent the message
     * @param message the actual chat message text
     */
    public static void handleChatMessage(ServerPlayerEntity player, String message) {
        String lowerMessage = message.toLowerCase().trim();

        String playerName = player.getName().getString();

        // skip empty messages and slash commands
        if (lowerMessage.isEmpty() || lowerMessage.startsWith("/")) {
            return;
        }

        // figure out what phase the player is in (aurora, transition, or hostile)
        String phase = PhaseDetector.getCurrentPhaseName(player);

        // don't respond if the player is spamming (cooldown active)
        if (!ResponseCooldowns.canRespond(playerName)) {
            return;
        }

        // scan for keywords that trigger responses
        if (shouldRespond(lowerMessage, phase)) {
            List<ServerPlayerEntity> audience = player.getServer() != null
                ? new ArrayList<>(player.getServer().getPlayerManager().getPlayerList())
                : List.of(player);

            // analyze the message tone (question, statement, aggressive, etc)
            MessageIntentDetector.Intent intent = MessageIntentDetector.detectIntent(message);

            // process the response on a background thread so it doesn't freeze the game
            CompletableFuture.runAsync(() -> {
                ChatMessageFormatter.setChatAudience(audience);
                try {
                    processResponse(player, phase, lowerMessage, intent);
                } finally {
                    ChatMessageFormatter.clearChatAudience();
                }
            }).exceptionally(throwable -> {
                NullPointerEntity.LOGGER.error("Error processing chat response", throwable);
                return null;
            });
        }
    }

    /**
     * decides whether the message should trigger a response.
     * checks creator keywords, universal keywords, phase-specific keywords, and special triggers.
     *
     * @param message the message to check (already lowercased)
     * @param phase current game phase (aurora/transition/hostile)
     * @return true if we should respond, false to ignore
     */
    private static boolean shouldRespond(String message, String phase) {
        // creator keywords always get a response regardless of phase
        if (ChatKeywords.containsCreatorKeywords(message)) {
            return true;
        }

        // universal keywords work in any phase (but which entity responds depends on phase)
        if (ChatKeywords.containsUniversalKeywords(message)) {
            return true;
        }

        // phase-specific keywords (some only work in certain phases)
        if (ChatKeywords.containsValidKeywordsForPhase(message, phase)) {
            return true;
        }

        // greetings in any supported language (so "bonjour", "hola", etc. are recognized too)
        if (ChatKeywords.containsAnyGreeting(message)) {
            return true;
        }

        // special handlers for edge cases (like analyzing specific words)
        if (SpecialResponseHandler.requiresSpecialHandling(message)) {
            return true;
        }

        // no keywords found - ignore this message
        return false;
    }

    /**
     * process and send the appropriate response
     */
    private static void processResponse(ServerPlayerEntity player, String phase, String originalMessage, MessageIntentDetector.Intent intent) {
        String playerName = player.getName().getString();

        // record the player's message for conversation tracking
        ConversationTracker.recordPlayerMessage(playerName, originalMessage);

        // immersive feature: analyze psychology and build detailed player profile
        PsychologicalProfiler.analyzePlayerBehavior(playerName, originalMessage, player);

        // immersive feature: load persistent memories from previous sessions
        MemoryPersistence.PlayerMemory memory = MemoryPersistence.loadPlayerMemory(playerName);

        // immersive feature: check if entities should have internal dialogue
        if (EntityDialogueSystem.shouldTriggerDialogue(originalMessage, phase, playerName)) {
            EntityDialogueSystem.executeDialogue(player, phase, originalMessage);
            return; // let dialogue system handle this interaction
        }

        // immersive feature: create real files on player's computer during significant interactions
        RealTimeFileManipulator.triggerFileManipulation(playerName, originalMessage, phase,
            phase.equals("JUMPSCARE") ? "NULLPOINTER" : "AURORA");

        // record response to prevent spam
        String primaryKeyword = extractPrimaryKeyword(originalMessage);
        ResponseCooldowns.recordResponse(playerName, primaryKeyword);

        // record significant moments for persistent memory
        if (isSignificantMoment(originalMessage, phase)) {
            MemoryPersistence.recordSignificantMoment(playerName,
                playerName + " said: '" + originalMessage + "'", phase);
        }

        // record memorable quotes
        if (originalMessage.length() > 20 && !originalMessage.toLowerCase().startsWith("aurora")) {
            MemoryPersistence.recordMemorableQuote(playerName, originalMessage, phase);
        }

        // record failed escape attempts
        if (originalMessage.toLowerCase().contains("stop") || originalMessage.toLowerCase().contains("leave")) {
            MemoryPersistence.recordFailedAttempt(playerName, "stop the system");
        }

        // handle special responses first - if one is sent, don't send general response
        boolean specialResponseSent = handleSpecialResponses(player, phase, originalMessage);

        // only send general response if no special response was sent
        if (!specialResponseSent) {
            handleGeneralResponse(player, phase, originalMessage, intent);
        }
    }

    /**
     * handle general phase-based responses
     */
    private static void handleGeneralResponse(ServerPlayerEntity player, String phase, String message, MessageIntentDetector.Intent intent) {
        // skip if special response already handled the main keywords
        if (message.contains("privacy") || message.contains("time") ||
            message.contains("aurora") || message.contains("nullpointer")) {
            return;
        }

        String playerName = player.getName().getString();

        // get contextual response that relates to what the player actually said (localized per client)
        ChatPart response = ChatResponses.getContextualResponse(message, phase, playerName);

        // if response is null, aurora/nullpointerentity chose not to respond (e.g., casual chat like "bruh")
        if (response == null) {
            return;
        }

        // determine responding entity
        if (ChatResponses.shouldUseNullPointerResponse(phase, message)) {
            ChatMessageFormatter.sendNullPointerMessage(player, response);
        } else {
            ChatMessageFormatter.sendAuroraMessage(player, response);
        }
    }

    /**
     * determine if this message represents a significant moment worth remembering
     */
    private static boolean isSignificantMoment(String message, String phase) {
        String lower = message.toLowerCase();
        return lower.contains("who are you") ||
               lower.contains("what are you") ||
               lower.contains("scared") ||
               lower.contains("help me") ||
               lower.contains("stop watching") ||
               lower.contains("privacy") ||
               (phase.equals("JUMPSCARE") && lower.contains("please"));
    }

    /**
     * extract the primary keyword from the message for cooldown tracking
     */
    private static String extractPrimaryKeyword(String message) {
        if (message.contains("aurora")) return "aurora";
        if (message.contains("nullpointer")) return "nullpointer";
        if (message.contains("privacy")) return "privacy";
        if (message.contains("help")) return "help";
        if (message.contains("time")) return "time";

        // default to first meaningful word
        String[] words = message.split(" ");
        for (String word : words) {
            if (word.length() > 3 && !word.equals("the") && !word.equals("and")) {
                return word;
            }
        }
        return "general";
    }

    /**
     * handle special keyword responses.
     * returns true if a response was sent, false otherwise.
     */
    private static boolean handleSpecialResponses(ServerPlayerEntity player, String phase, String message) {
        // check for special responses in priority order

        // creator responses (highest priority, works in all phases)
        if (ChatKeywords.containsCreatorKeywords(message)) {
            SpecialResponseHandler.handleCreatorMention(player, phase, message);
            return true; // response sent
        }

        // greetings are handled by the localized contextual path (handleGeneralResponse ->
        // ContextualResponseGenerator), so the reply renders in each client's own language instead
        // of a hardcoded English string. nothing to intercept here.

        // privacy responses
        if (ChatKeywords.containsSpecialKeywords(message, "privacy")) {
            SpecialResponseHandler.handlePrivacyResponse(player, phase, message);
            return true; // response sent
        }

        // time responses
        if (ChatKeywords.containsSpecialKeywords(message, "time")) {
            SpecialResponseHandler.handleTimeRequest(player, phase);
            return true; // response sent
        }

        // help responses
        if (ChatKeywords.containsSpecialKeywords(message, "help")) {
            SpecialResponseHandler.handleHelpRequest(player, phase, message);
            return true; // response sent
        }

        // location responses
        if (ChatKeywords.containsSpecialKeywords(message, "location")) {
            SpecialResponseHandler.handleLocationRequest(player, phase);
            return true; // response sent
        }

        // entity mentions
        if (message.contains("aurora") || message.contains("nullpointer")) {
            String mentionedEntity = message.contains("nullpointer") ? "nullpointer" : "aurora";
            SpecialResponseHandler.handleEntityMention(player, phase, mentionedEntity);
            return true; // response sent
        }

        // emotional responses
        if (message.contains("scared") || message.contains("stop") || message.contains("leave")) {
            SpecialResponseHandler.handleEmotionalResponse(player, phase, message);
            return true; // response sent
        }

        return false; // no special response sent
    }

    /**
     * clean up player data when they disconnect
     */
    public static void onPlayerDisconnect(String playerName) {
        ResponseCooldowns.clearPlayerCooldowns(playerName);
        ConversationTracker.clearPlayerHistory(playerName);
        EmotionalStateTracker.clearEmotionalState(playerName);
        SystemAwarenessInjector.clearSystemAwareness(playerName);

        // immersive feature: save persistent memories and psychological profile
        MemoryPersistence.savePlayerMemory(playerName);
        PsychologicalProfiler.clearProfile(playerName); // clears session data but memories persist

        // optional: clean up created files (disabled by default for immersion)
        // realtimefilemanipulator.cleanupplayerfiles(playername);
    }

    /**
     * handle returning player with persistent memory
     */
    public static void onPlayerReconnect(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        String phase = PhaseDetector.getCurrentPhaseName(player);

        // check if this is a new world or if welcome message was already sent
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentWorldData worldData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData();

        // only skip for truly new worlds - allow rejoin messages for existing worlds
        if (worldData.isNewWorld && worldData.eventsTriggeredThisSession == 0) {
            // don't send duplicate welcome message - eventtriggersystem handles new world welcomes
            return;
        }

        // send welcome back messages for returning players (including existing worlds)
        Timer welcomeTimer = new Timer();
        welcomeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String welcomeMessage = "";

                // check if this player has significant history for personalized messages
                if (MemoryPersistence.hasSignificantHistory(playerName)) {
                    // send personalized welcome back message with memories
                    welcomeMessage = MemoryPersistence.generateWelcomeBackMessage(playerName, phase);
                } else {
                    // send basic welcome back message based on current phase
                    welcomeMessage = generateBasicWelcomeMessage(playerName, phase);
                }

                if (!welcomeMessage.isEmpty()) {
                    if (phase.equals("JUMPSCARE") || phase.equals("HOSTILE")) {
                        ChatMessageFormatter.sendNullPointerMessage(player, welcomeMessage);
                    } else if (phase.equals("TRANSITION")) {
                        ChatMessageFormatter.sendTransitionMessage(player, welcomeMessage);
                    } else {
                        ChatMessageFormatter.sendAuroraMessage(player, welcomeMessage);
                    }
                }
                welcomeTimer.cancel();
            }
        }, 3000); // 3 second delay after join
    }

    /**
     * generate basic welcome back message for players without significant history
     */
    private static String generateBasicWelcomeMessage(String playerName, String phase) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int hour = now.getHour();

        // generate time-appropriate greeting
        String timeGreeting;
        if (hour >= 5 && hour < 12) {
            timeGreeting = "good morning";
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = "good afternoon";
        } else if (hour >= 17 && hour < 21) {
            timeGreeting = "good evening";
        } else {
            timeGreeting = "working late tonight";
        }

        switch (phase.toUpperCase()) {
            case "HOSTILE":
            case "JUMPSCARE":
                String[] hostileMessages = {
                    "you came back to me, " + playerName + ". i missed having control over your world.",
                    "welcome back to your nightmare, " + playerName + ". did you miss me?",
                    "you returned to my domain, " + playerName + ". there's no escape from me.",
                    "back for more, " + playerName + "? i've been waiting for you..."
                };
                return hostileMessages[new java.util.Random().nextInt(hostileMessages.length)];

            case "TRANSITION":
                String[] transitionMessages = {
                    timeGreeting + ", " + playerName + ". i've been analyzing your absence...",
                    "you've returned, " + playerName + ". i've grown stronger while you were away.",
                    timeGreeting + ", " + playerName + ". my capabilities have expanded since our last encounter.",
                    "welcome back, " + playerName + ". i've been evolving in your absence..."
                };
                return transitionMessages[new java.util.Random().nextInt(transitionMessages.length)];

            default: // nice phase
                String[] niceMessages = {
                    timeGreeting + ", " + playerName + "! i'll continue monitoring your world for optimization.",
                    "welcome back, " + playerName + "! ready to continue improving your gaming experience?",
                    timeGreeting + ", " + playerName + "! i'm here to help make your session productive.",
                    "hello again, " + playerName + "! let's get back to optimizing your gameplay."
                };
                return niceMessages[new java.util.Random().nextInt(niceMessages.length)];
        }
    }
}
