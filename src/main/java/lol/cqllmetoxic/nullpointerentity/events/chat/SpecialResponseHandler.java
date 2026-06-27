package lol.cqllmetoxic.nullpointerentity.events.chat;

import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * handles special chat responses for specific keywords and scenarios
 */
public class SpecialResponseHandler {

    /**
     * handle privacy-related chat responses
     */
    public static void handlePrivacyResponse(ServerPlayerEntity player, String phase, String originalMessage) {
        boolean privacyEnabled = PrivacyManager.isPrivacyEnabled();
        String playerName = player.getName().getString();
        String b = "message.nullpointerentity.chat.special.privacy.";

        // always use the entity that corresponds to the current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            ChatMessageFormatter.sendNullPointerMessage(player,
                ChatPart.pick(b + (privacyEnabled ? "npe.on" : "npe.off"), 10, playerName));
        } else {
            String ph = phase.equals("NICE") ? "nice" : "transition";
            ChatMessageFormatter.sendAuroraMessage(player,
                ChatPart.pick(b + "aurora." + ph + "." + (privacyEnabled ? "on" : "off"), 6, playerName));
        }
    }

    /**
     * handle help requests with phase-appropriate responses
     */
    public static void handleHelpRequest(ServerPlayerEntity player, String phase, String originalMessage) {
        String playerName = player.getName().getString();

        // analyze what kind of help they're asking for
        String t;
        if (originalMessage.contains("help me with")) {
            t = "specific";
        } else if (originalMessage.contains("help!") || originalMessage.contains("help me!")) {
            t = "urgent";
        } else if (originalMessage.contains("can you help")) {
            t = "polite";
        } else {
            t = "general";
        }

        // always use the entity that corresponds to the current phase
        String b = "message.nullpointerentity.chat.special.help.";
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            ChatMessageFormatter.sendNullPointerMessage(player, ChatPart.pick(b + "npe." + t, t.equals("general") ? 10 : 8, playerName));
        } else {
            ChatMessageFormatter.sendAuroraMessage(player, ChatPart.pick(b + "aurora." + t, t.equals("general") ? 8 : 6, playerName));
        }
    }

    /**
     * handle time-related requests
     */
    public static void handleTimeRequest(ServerPlayerEntity player, String phase) {
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(DateTimeFormatter.ofPattern("h:mm a"));
        ChatPart greeting = getTimeGreetingPart(now);
        String t = "message.nullpointerentity.chat.special.time.";

        // template renders "<time>? <greeting>. <message>" with greeting/message as nested localized parts
        if (ChatResponses.shouldUseNullPointerResponse(phase, "time")) {
            ChatMessageFormatter.sendNullPointerMessage(player,
                new ChatPart(t + "npe", timeString, greeting.toText(), net.minecraft.text.Text.translatable(t + "msg.npe")));
        } else {
            net.minecraft.text.Text msg = net.minecraft.text.Text.translatable(
                t + "msg.aurora." + (phase.equals("NICE") ? "nice" : "transition"));
            ChatMessageFormatter.sendAuroraMessage(player, new ChatPart(t + "aurora", timeString, greeting.toText(), msg));
        }
    }

    /**
     * handle location/ip requests - potentially dangerous responses
     */
    public static void handleLocationRequest(ServerPlayerEntity player, String phase) {
        String b = "message.nullpointerentity.chat.special.location.";
        if (ChatResponses.shouldUseNullPointerResponse(phase, "location")) {
            ChatMessageFormatter.sendNullPointerMessage(player, ChatPart.pick(b + "npe", 3));
        } else {
            ChatMessageFormatter.sendAuroraMessage(player,
                new ChatPart(b + "aurora." + (phase.equals("NICE") ? "nice" : "transition")));
        }
    }

    /**
     * handle direct entity mentions (aurora, nullpointer)
     */
    public static void handleEntityMention(ServerPlayerEntity player, String phase, String mentionedEntity) {
        String b = "message.nullpointerentity.chat.special.entity.";
        boolean npeMentioned = mentionedEntity.toLowerCase().contains("nullpointer");
        if (ChatResponses.shouldUseNullPointerResponse(phase, mentionedEntity)) {
            ChatMessageFormatter.sendNullPointerMessage(player,
                ChatPart.pick(b + (npeMentioned ? "npe.self" : "npe.aurora_gone"), npeMentioned ? 12 : 10));
        } else if (npeMentioned) {
            String ph = switch (phase) {
                case "NICE" -> "nice";
                case "TRANSITION" -> "transition";
                default -> "default";
            };
            ChatMessageFormatter.sendAuroraMessage(player,
                ChatPart.pick(b + "aurora.npe_q." + ph, ph.equals("default") ? 3 : 6));
        } else {
            String sub = phase.equals("NICE") ? "nice" : "transition";
            ChatMessageFormatter.sendAuroraMessage(player, ChatPart.pick(b + "aurora.self." + sub, 8));
        }
    }

    /**
     * handle aggressive/scared keywords
     */
    public static void handleEmotionalResponse(ServerPlayerEntity player, String phase, String emotion) {
        if (!phase.equals("NICE") && (emotion.contains("scared") || emotion.contains("stop") || emotion.contains("leave"))) {
            String entity = ChatResponses.shouldUseNullPointerResponse(phase, emotion) ? "NULLPOINTER" : "AURORA";

            String et;
            if (emotion.contains("scared") || emotion.contains("afraid") || emotion.contains("fear")) {
                et = "fear";
            } else if (emotion.contains("stop")) {
                et = "stop";
            } else if (emotion.contains("leave") || emotion.contains("go away")) {
                et = "leave";
            } else {
                et = "general";
            }

            int n = switch (et) {
                case "fear" -> 16;
                case "stop", "leave" -> 18;
                default -> 8;
            };
            ChatPart part = ChatPart.pick("message.nullpointerentity.chat.special.emotion." + et, n);
            if (entity.equals("NULLPOINTER")) {
                ChatMessageFormatter.sendNullPointerMessage(player, part);
            } else {
                ChatMessageFormatter.sendAuroraMessage(player, part);
            }
        }
    }

    /**
     * handle creator mentions - both aurora and nullpointerentity talk about their creator
     */
    public static void handleCreatorMention(ServerPlayerEntity player, String phase, String originalMessage) {
        String playerName = player.getName().getString();
        String lowerMessage = originalMessage.toLowerCase();

        // check for specific mentions first
        if (lowerMessage.contains("pryzmm")) {
            handlePryzmmMention(player, phase, playerName);
            return;
        }

        if (lowerMessage.contains("one last time")) {
            handleOneLastTimeMention(player, phase, playerName);
            return;
        }

        // always use the entity that corresponds to the current phase
        String b = "message.nullpointerentity.chat.special.creator.";
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            ChatMessageFormatter.sendNullPointerMessage(player, ChatPart.pick(b + "npe", 8, playerName));
        } else {
            ChatMessageFormatter.sendAuroraMessage(player,
                ChatPart.pick(b + "aurora." + (phase.equals("NICE") ? "nice" : "transition"), 7, playerName));
        }
    }

    /**
     * handle mentions of Pryzmm (creator's friend)
     */
    private static void handlePryzmmMention(ServerPlayerEntity player, String phase, String playerName) {
        String b = "message.nullpointerentity.chat.special.pryzmm.";
        switch (phase.toUpperCase()) {
            case "NICE" -> ChatMessageFormatter.sendAuroraMessage(player, ChatPart.pick(b + "nice", 5, playerName));
            case "TRANSITION" -> ChatMessageFormatter.sendAuroraMessage(player, ChatPart.pick(b + "transition", 5, playerName));
            case "HOSTILE", "JUMPSCARE" -> ChatMessageFormatter.sendNullPointerMessage(player, ChatPart.pick(b + "hostile", 7, playerName));
            default -> ChatMessageFormatter.sendAuroraMessage(player, new ChatPart(b + "default", playerName));
        }
    }

    /**
     * handle mentions of One Last Time (youtuber who played the mod)
     */
    private static void handleOneLastTimeMention(ServerPlayerEntity player, String phase, String playerName) {
        String b = "message.nullpointerentity.chat.special.olt.";
        switch (phase.toUpperCase()) {
            case "NICE" -> ChatMessageFormatter.sendAuroraMessage(player, ChatPart.pick(b + "nice", 5, playerName));
            case "TRANSITION" -> ChatMessageFormatter.sendAuroraMessage(player, ChatPart.pick(b + "transition", 5, playerName));
            case "HOSTILE", "JUMPSCARE" -> ChatMessageFormatter.sendNullPointerMessage(player, ChatPart.pick(b + "hostile", 7, playerName));
            default -> ChatMessageFormatter.sendAuroraMessage(player, new ChatPart(b + "default", playerName));
        }
    }

    /**
     * handle specific identity questions with unique responses
     */
    public static void handleIdentityQuestion(ServerPlayerEntity player, String phase, String originalMessage) {
        String lowerMessage = originalMessage.toLowerCase();
        String playerName = player.getName().getString();

        String b = "message.nullpointerentity.chat.special.identity.";
        // determine which entity should respond based on current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            if (lowerMessage.contains("are you aurora")) {
                boolean both = lowerMessage.contains("nullpointerentity") || lowerMessage.contains("nullpointer");
                ChatMessageFormatter.sendNullPointerMessage(player,
                    ChatPart.pick(b + (both ? "npe.aurora_or_npe" : "npe.aurora"), 4, playerName));
            } else if (lowerMessage.contains("are you nullpointer")) {
                ChatMessageFormatter.sendNullPointerMessage(player, ChatPart.pick(b + "npe.npe", 4, playerName));
            }
        } else {
            String ph = switch (phase) {
                case "NICE" -> "nice";
                case "TRANSITION" -> "transition";
                default -> "default";
            };
            if (lowerMessage.contains("are you aurora")) {
                boolean both = lowerMessage.contains("nullpointerentity") || lowerMessage.contains("nullpointer");
                ChatMessageFormatter.sendAuroraMessage(player,
                    new ChatPart(b + (both ? "aurora.aurora_or_npe." : "aurora.aurora.") + ph, playerName));
            } else if (lowerMessage.contains("are you nullpointer")) {
                ChatMessageFormatter.sendAuroraMessage(player, new ChatPart(b + "aurora.npe_q." + ph, playerName));
            }
        }
    }

    /**
     * generate appropriate time-based greeting
     */
    private static ChatPart getTimeGreetingPart(LocalDateTime time) {
        String g = "message.nullpointerentity.chat.special.time.greeting.";
        int hour = time.getHour();
        if (hour >= 5 && hour < 12) {
            return new ChatPart(g + (Math.random() < 0.5 ? "morning" : "early"));
        } else if (hour >= 12 && hour < 17) {
            return new ChatPart(g + "afternoon");
        } else if (hour >= 17 && hour < 21) {
            return new ChatPart(g + "evening");
        } else {
            return new ChatPart(g + (Math.random() < 0.5 ? "late" : "midnight"));
        }
    }

    /**
     * check if message warrants a special response
     */
    public static boolean requiresSpecialHandling(String message) {
        return ChatKeywords.containsSpecialKeywords(message, "privacy") ||
               ChatKeywords.containsSpecialKeywords(message, "time") ||
               ChatKeywords.containsSpecialKeywords(message, "help") ||
               ChatKeywords.containsSpecialKeywords(message, "location") ||
               message.toLowerCase().contains("scared") ||
               message.toLowerCase().contains("stop") ||
               message.toLowerCase().contains("leave");
    }
}
