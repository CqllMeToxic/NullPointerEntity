package lol.cqllmetoxic.nullpointerentity.events.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * handles message formatting for different entities (aurora vs nullpointerentity)
 */
public class ChatMessageFormatter {
    private static final ThreadLocal<List<ServerPlayerEntity>> CHAT_AUDIENCE = new ThreadLocal<>();

    public static void setChatAudience(Collection<ServerPlayerEntity> audience) {
        CHAT_AUDIENCE.set(new ArrayList<>(audience));
    }

    public static void clearChatAudience() {
        CHAT_AUDIENCE.remove();
    }

    public static List<ServerPlayerEntity> resolveRecipients(ServerPlayerEntity fallbackPlayer) {
        List<ServerPlayerEntity> audience = CHAT_AUDIENCE.get();
        if (audience == null || audience.isEmpty()) {
            if (fallbackPlayer != null && fallbackPlayer.getServer() != null) {
                return new ArrayList<>(fallbackPlayer.getServer().getPlayerManager().getPlayerList());
            }
            return fallbackPlayer != null ? List.of(fallbackPlayer) : List.of();
        }
        return new ArrayList<>(audience);
    }

    private static void sendToRecipients(List<ServerPlayerEntity> recipients, Text message, boolean actionBar) {
        for (ServerPlayerEntity recipient : recipients) {
            if (recipient != null) {
                recipient.sendMessage(message, actionBar);
            }
        }
    }

    /**
     * send a message from aurora with aqua name formatting
     */
    public static void sendAuroraMessage(ServerPlayerEntity player, String message) {
        String playerName = player.getName().getString();
        String phase = PhaseDetector.getCurrentPhaseName(player);

        // build message components separately to control flow
        StringBuilder finalMessage = new StringBuilder();
        int enhancementCount = 0;
        int maxEnhancements = 2; // limit stacking to avoid word salad

        // immersive feature: add persistent memory context (priority enhancement)
        String memoryContext = MemoryPersistence.generateMemoryResponse(playerName, phase, message);
        if (!memoryContext.isEmpty() && enhancementCount < maxEnhancements) {
            finalMessage.append(memoryContext);
            enhancementCount++;
        }

        // add the base message
        if (finalMessage.length() > 0) {
            finalMessage.append(" ");
        }
        finalMessage.append(message);

        // immersive feature: add psychological profiling (only if not too many enhancements)
        String profileMessage = PsychologicalProfiler.generateProfileAwareResponse(playerName, finalMessage.toString(), phase, "AURORA");

        // make response more immersive with real-time context (only add if different from current message)
        String enhancedMessage = RealTimeContextGenerator.generateActivityAwareResponse(player, phase, profileMessage);

        // only add activity context if memory context wasn't already added
        if (memoryContext.isEmpty() && !enhancedMessage.equals(profileMessage) && enhancementCount < maxEnhancements) {
            enhancedMessage = profileMessage;
        } else if (!memoryContext.isEmpty()) {
            // if we already have memory context, skip activity context to avoid overload
            enhancedMessage = profileMessage;
        }

        // add emotional context sparingly
        EmotionalStateTracker.updateEmotionalState(playerName, phase, "aurora_response");
        String emotionalAddition = EmotionalStateTracker.getEmotionalModifier(playerName, enhancedMessage, "AURORA");
        if (!emotionalAddition.isEmpty() && enhancementCount < maxEnhancements && Math.random() < 0.4) {
            enhancedMessage += emotionalAddition;
            enhancementCount++;
        }

        // inject system awareness only occasionally and not when we have lots of other enhancements
        if (enhancementCount < maxEnhancements && Math.random() < 0.3) {
            enhancedMessage = SystemAwarenessInjector.injectSystemAwareness(enhancedMessage, playerName, phase, "AURORA");
        }

        // immersive feature: create breadcrumb files during conversations
        if (Math.random() < 0.15) { // 15% chance to leave traces
            RealTimeFileManipulator.createConversationBreadcrumb(playerName, phase);
        }

        // send with typing simulation for longer messages
        if (enhancedMessage.length() > 50) {
            TypingSimulator.sendMessageWithTyping(player, "AURORA", enhancedMessage, phase);
        } else {
            // send immediately for short messages - phase-based aurora color
            Formatting auroraColor = phase.equals("TRANSITION") ? Formatting.YELLOW : Formatting.AQUA;
            Text auroraMessage = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(auroraColor)
                .append(Text.literal(enhancedMessage).formatted(Formatting.WHITE));
            sendToRecipients(resolveRecipients(player), auroraMessage, false);
        }

        // track the response for conversation history
        ConversationTracker.recordEntityResponse(playerName, "AURORA", enhancedMessage);

        // send follow-up messages if emotionally appropriate
        scheduleFollowUpIfNeeded(player, "AURORA", phase, resolveRecipients(player));
    }

    // ---- localized (per-client) chat -------------------------------------------------------------
    // send a translation key + args and let each client render it in its OWN language. the String
    // overloads above are the old english server-side ones - i kept them around for the non-chat
    // callers. heads up: the enhancer pipeline (memory/profile/emotional/...) doesn't run on this
    // path yet, so you just get the base reply until i get around to porting those over to ChatPart.

    public static void sendAuroraMessage(ServerPlayerEntity player, ChatPart part) {
        sendLocalized(player, "AURORA", part);
    }

    public static void sendNullPointerMessage(ServerPlayerEntity player, ChatPart part) {
        sendLocalized(player, "NULLPOINTER", part);
    }

    private static void sendLocalized(ServerPlayerEntity player, String entity, ChatPart part) {
        String playerName = player.getName().getString();
        String phase = PhaseDetector.getCurrentPhaseName(player);
        boolean npe = entity.equals("NULLPOINTER");

        // immersive enhancers (memory / profile / emotional / real-machine awareness), each a
        // translatable part appended after the base reply; capped at 2 to avoid word salad.
        java.util.List<ChatPart> enhancers = new java.util.ArrayList<>();
        enhancers.addAll(MemoryPersistence.generateMemoryParts(playerName, phase));
        enhancers.addAll(PsychologicalProfiler.generateProfileParts(playerName, phase, entity));
        ChatPart activity = RealTimeContextGenerator.generateActivityPart(player, phase);
        if (activity != null && Math.random() < 0.5) enhancers.add(activity);
        ChatPart emotional = EmotionalStateTracker.getEmotionalModifierPart(playerName);
        if (emotional != null && Math.random() < 0.4) enhancers.add(emotional);
        ChatPart systemAware = SystemAwarenessInjector.generateSystemAwarePart(playerName, phase);
        if (systemAware != null && Math.random() < 0.3) enhancers.add(systemAware);
        if (enhancers.size() > 2) enhancers = enhancers.subList(0, 2);

        Formatting bodyColor = npe ? Formatting.RED : Formatting.WHITE;
        Formatting nameColor = npe ? Formatting.DARK_RED : (phase.equals("TRANSITION") ? Formatting.YELLOW : Formatting.AQUA);
        String prefixKey = npe ? "message.nullpointerentity.chat_prefix" : "message.nullpointerentity.aurora_prefix";

        MutableText message = Text.translatable(prefixKey).formatted(nameColor)
            .append(part.toText().formatted(bodyColor));
        int estLen = part.estimatedLength();
        for (ChatPart enhancer : enhancers) {
            message.append(Text.literal(" ")).append(enhancer.toText().formatted(bodyColor));
            estLen += 1 + enhancer.estimatedLength();
        }

        if (estLen > 50) {
            TypingSimulator.sendTextWithTyping(player, entity, message, estLen, phase);
        } else {
            sendToRecipients(resolveRecipients(player), message, false);
        }
    }

    /**
     * send a message from nullpointerentity with dark red/red formatting
     */
    public static void sendNullPointerMessage(ServerPlayerEntity player, String message) {
        String playerName = player.getName().getString();
        String phase = PhaseDetector.getCurrentPhaseName(player);

        // phase restriction: nullpointerentity should only speak during hostile and jumpscare phases
        if (!phase.equals("HOSTILE") && !phase.equals("JUMPSCARE")) {
            // if not in hostile/jumpscare phase, redirect to aurora or ignore
            return; // simply don't send the message during early phases
        }

        // immersive feature: add persistent memory context (more aggressive for nullpointer)
        String memoryContext = MemoryPersistence.generateMemoryResponse(playerName, phase, message);
        if (!memoryContext.isEmpty()) {
            message = memoryContext.toLowerCase() + " " + message;
        }

        // immersive feature: add psychological profiling with menacing tone
        message = PsychologicalProfiler.generateProfileAwareResponse(playerName, message, phase, "NULLPOINTER");

        // add psychological analysis for hostile phases
        if (phase.equals("HOSTILE") || phase.equals("JUMPSCARE")) {
            String psychAnalysis = PsychologicalProfiler.generatePsychologicalAnalysis(playerName, phase);
            if (!psychAnalysis.isEmpty() && Math.random() < 0.3) {
                message += " " + psychAnalysis.toLowerCase();
            }
        }

        // make response more immersive with real-time context
        String enhancedMessage = RealTimeContextGenerator.generateActivityAwareResponse(player, phase, message);

        // add emotional context
        EmotionalStateTracker.updateEmotionalState(playerName, phase, "nullpointer_response");
        String emotionalAddition = EmotionalStateTracker.getEmotionalModifier(playerName, enhancedMessage, "NULLPOINTER");
        if (!emotionalAddition.isEmpty()) {
            enhancedMessage += emotionalAddition.toLowerCase();
        }

        // inject system awareness for maximum immersion
        enhancedMessage = SystemAwarenessInjector.injectSystemAwareness(enhancedMessage, playerName, phase, "NULLPOINTER");

        // nullpointer always uses typing simulation for menacing effect
        if (enhancedMessage.length() > 30) {
            TypingSimulator.sendMessageWithThinking(player, "NULLPOINTER", enhancedMessage, phase);
        } else {
            TypingSimulator.sendMessageWithTyping(player, "NULLPOINTER", enhancedMessage, phase);
        }

        // track the response for conversation history
        ConversationTracker.recordEntityResponse(playerName, "NULLPOINTER", enhancedMessage);

        // nullpointer is more likely to send follow-ups
        scheduleFollowUpIfNeeded(player, "NULLPOINTER", phase, resolveRecipients(player));
    }

    /**
     * send a system message (for debugging or special notifications)
     */
    public static void sendSystemMessage(ServerPlayerEntity player, String message) {
        Text systemMessage = Text.translatable("message.nullpointerentity.system_prefix").formatted(Formatting.GRAY)
            .append(Text.literal(message).formatted(Formatting.WHITE));
        sendToRecipients(resolveRecipients(player), systemMessage, false);
    }

    /**
     * send a glitched aurora message (for transition effects)
     */
    public static void sendGlitchedAuroraMessage(ServerPlayerEntity player, String message) {
        // glitched aurora prefix: the §-codes in the value carry the per-letter colour effect
        // (two red characters), so translators can localize the name and keep the glitch look.
        Text glitchedMessage = Text.translatable("message.nullpointerentity.glitch_prefix")
            .append(Text.literal(" ").append(Text.literal(message).formatted(Formatting.WHITE)));
        sendToRecipients(resolveRecipients(player), glitchedMessage, false);
    }

    /**
     * send a whisper-style message (darker, more ominous)
     */
    public static void sendWhisperMessage(ServerPlayerEntity player, String message) {
        Text whisperMessage = Text.translatable("message.nullpointerentity.whisper_prefix").formatted(Formatting.DARK_GRAY)
            .append(Text.literal(message).formatted(Formatting.GRAY));
        sendToRecipients(resolveRecipients(player), whisperMessage, false);
    }

    /**
     * send an emphasized message with special formatting
     */
    public static void sendEmphasizedMessage(ServerPlayerEntity player, String entity, String message) {
        Formatting nameColor = entity.equals("AURORA") ? Formatting.AQUA : Formatting.DARK_RED;
        Formatting messageColor = Formatting.YELLOW;

        Text emphasizedMessage = Text.literal("<" + entity + "> ").formatted(nameColor, Formatting.BOLD)
            .append(Text.literal(message).formatted(messageColor));
        sendToRecipients(resolveRecipients(player), emphasizedMessage, false);
    }

    /**
     * send message based on phase and entity automatically
     */
    public static void sendPhaseBasedMessage(ServerPlayerEntity player, String phase, String entity, String message) {
        switch (entity.toUpperCase()) {
            case "AURORA" -> {
                if (phase.equals("TRANSITION") && Math.random() < 0.2) {
                    sendGlitchedAuroraMessage(player, message);
                } else {
                    sendAuroraMessage(player, message);
                }
            }
            case "NULLPOINTER" -> sendNullPointerMessage(player, message);
            case "SYSTEM" -> sendSystemMessage(player, message);
            case "WHISPER" -> sendWhisperMessage(player, message);
            default -> sendAuroraMessage(player, message);
        }
    }

    /**
     * format time-based messages with appropriate styling
     */
    public static void sendTimeMessage(ServerPlayerEntity player, String entity, String timeString, String greeting, String message) {
        if (entity.equals("AURORA")) {
            String fullMessage = timeString + "? " + greeting + ". " + message;
            sendAuroraMessage(player, fullMessage);
        } else {
            String fullMessage = timeString + "? " + greeting + "... " + message;
            sendNullPointerMessage(player, fullMessage);
        }
    }

    /**
     * send a multi-part message with delays between parts
     */
    public static void sendDelayedMessage(ServerPlayerEntity player, String entity, String[] messageParts, int delayMs) {
        java.util.Timer timer = new java.util.Timer();

        for (int i = 0; i < messageParts.length; i++) {
            final int index = i;
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    sendPhaseBasedMessage(player, "CURRENT", entity, messageParts[index]);
                    if (index == messageParts.length - 1) {
                        timer.cancel();
                    }
                }
            }, delayMs * i);
        }
    }

    /**
     * schedule follow-up messages if emotionally appropriate
     */
    private static void scheduleFollowUpIfNeeded(ServerPlayerEntity player, String entity, String phase, List<ServerPlayerEntity> audience) {
        String playerName = player.getName().getString();

        if (EmotionalStateTracker.shouldSendFollowUp(playerName)) {
            java.util.Timer followUpTimer = new java.util.Timer();
            followUpTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    String followUpMessage = EmotionalStateTracker.generateFollowUpMessage(playerName, phase);
                    if (!followUpMessage.isEmpty()) {
                        setChatAudience(audience);
                        if (entity.equals("NULLPOINTER")) {
                            TypingSimulator.sendContextualMessageWithTyping(player, entity, followUpMessage, phase, false);
                        } else {
                            TypingSimulator.sendContextualMessageWithTyping(player, entity, followUpMessage, phase, false);
                        }
                        clearChatAudience();
                        ConversationTracker.recordEntityResponse(playerName, entity, followUpMessage);
                    }
                    followUpTimer.cancel();
                }
            }, 8000 + (int)(Math.random() * 7000)); // 8-15 seconds delay
        }
    }

    /**
     * send a message from aurora in transition phase with yellow formatting
     */
    public static void sendTransitionMessage(ServerPlayerEntity player, String message) {
        String playerName = player.getName().getString();
        String phase = PhaseDetector.getCurrentPhaseName(player);

        // immersive feature: add persistent memory context for transition
        String memoryContext = MemoryPersistence.generateMemoryResponse(playerName, phase, message);
        if (!memoryContext.isEmpty()) {
            message = memoryContext + " " + message;
        }

        // immersive feature: add psychological profiling for transition phase
        message = PsychologicalProfiler.generateProfileAwareResponse(playerName, message, phase, "AURORA");

        // make response more immersive with real-time context
        String enhancedMessage = RealTimeContextGenerator.generateActivityAwareResponse(player, phase, message);

        // add emotional context for transition phase
        EmotionalStateTracker.updateEmotionalState(playerName, phase, "aurora_transition_response");
        String emotionalAddition = EmotionalStateTracker.getEmotionalModifier(playerName, enhancedMessage, "AURORA");
        if (!emotionalAddition.isEmpty()) {
            enhancedMessage += emotionalAddition;
        }

        // inject system awareness for transition phase
        enhancedMessage = SystemAwarenessInjector.injectSystemAwareness(enhancedMessage, playerName, phase, "AURORA");

        // send with transition-specific formatting (yellow aurora)
        if (enhancedMessage.length() > 50) {
            TypingSimulator.sendMessageWithTyping(player, "AURORA", enhancedMessage, phase);
        } else {
            // send immediately with yellow formatting for transition phase
            Text transitionMessage = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.YELLOW)
                .append(Text.literal(enhancedMessage).formatted(Formatting.WHITE));
            sendToRecipients(resolveRecipients(player), transitionMessage, false);
        }

        // track the response for conversation history
        ConversationTracker.recordEntityResponse(playerName, "AURORA", enhancedMessage);

        // send follow-up messages if appropriate for transition phase
        scheduleFollowUpIfNeeded(player, "AURORA", phase, resolveRecipients(player));
    }
}
