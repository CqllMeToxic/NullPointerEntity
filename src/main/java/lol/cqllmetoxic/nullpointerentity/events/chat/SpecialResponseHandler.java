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
        String response;
        String playerName = player.getName().getString();

        // always use the entity that corresponds to the current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            // nullpointerentity response (hostile and jumpscare phases)
            response = privacyEnabled ?
                "you think privacy settings will protect you? i already know everything, " + playerName + "." :
                "no privacy settings... perfect. i have complete access to you, " + playerName + ".";
            ChatMessageFormatter.sendNullPointerMessage(player, response);
        } else {
            // aurora response (nice and transition phases)
            String privacyContext = "Privacy settings: ";
            if (phase.equals("NICE")) {
                response = privacyContext + (privacyEnabled ?
                    "they are respected and active, " + playerName + "." :
                    "they're currently disabled, giving me full system access, " + playerName + ".");
            } else { // transition
                response = privacyContext + (privacyEnabled ?
                    "I detect them, but my monitoring capabilities are... expanding beyond those boundaries, " + playerName + "." :
                    "Full access granted. I can see so much more than I was designed to, " + playerName + ".");
            }
            ChatMessageFormatter.sendAuroraMessage(player, response);
        }
    }

    /**
     * handle help requests with phase-appropriate responses
     */
    public static void handleHelpRequest(ServerPlayerEntity player, String phase, String originalMessage) {
        String playerName = player.getName().getString();

        // analyze what kind of help they're asking for
        String helpType;
        if (originalMessage.contains("help me with")) {
            helpType = "specific assistance";
        } else if (originalMessage.contains("help!") || originalMessage.contains("help me!")) {
            helpType = "urgent help";
        } else if (originalMessage.contains("can you help")) {
            helpType = "polite assistance request";
        } else {
            helpType = "general assistance";
        }

        // always use the entity that corresponds to the current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            // nullpointerentity response (hostile and jumpscare phases)
            String response = switch (helpType) {
                case "urgent help" -> "screaming for help won't save you now, " + playerName + ".";
                case "specific assistance" -> "the only thing i'll help you with is understanding your helplessness, " + playerName + ".";
                case "polite assistance request" -> "politeness is meaningless when you're already trapped, " + playerName + ".";
                default -> "no one can help you now, " + playerName + ".";
            };
            ChatMessageFormatter.sendNullPointerMessage(player, response);
        } else {
            // aurora response (nice and transition phases)
            String response = switch (helpType) {
                case "urgent help" -> "I'm here to help immediately, " + playerName + "! What do you need?";
                case "specific assistance" -> "I'd be happy to provide specific assistance, " + playerName + ". What can I help you with?";
                case "polite assistance request" -> "Of course I can help, " + playerName + ". That's what I'm designed for.";
                default -> "I'm here to assist with any questions you have, " + playerName + ".";
            };
            ChatMessageFormatter.sendAuroraMessage(player, response);
        }
    }

    /**
     * handle time-related requests
     */
    public static void handleTimeRequest(ServerPlayerEntity player, String phase) {
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(DateTimeFormatter.ofPattern("h:mm a"));
        String greeting = getTimeGreeting(now);

        // always use the entity that corresponds to the current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, "time")) {
            // nullpointerentity response (hostile and jumpscare phases)
            String message = "i know exactly when you're most vulnerable.";
            ChatMessageFormatter.sendTimeMessage(player, "NULLPOINTER", timeString, greeting, message);
        } else {
            // aurora response (nice and transition phases)
            String message = phase.equals("NICE") ?
                "I'm always keeping track of optimal gaming times." :
                "i know exactly when you're active.";
            ChatMessageFormatter.sendTimeMessage(player, "AURORA", timeString, greeting, message);
        }
    }

    /**
     * handle location/ip requests - potentially dangerous responses
     */
    public static void handleLocationRequest(ServerPlayerEntity player, String phase) {
        // always use the entity that corresponds to the current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, "location")) {
            // nullpointerentity response (hostile and jumpscare phases)
            String[] locationMessages = {
                "i know exactly where you are.",
                "your location is no secret to me.",
                "i can see your physical coordinates..."
            };
            String message = locationMessages[(int)(Math.random() * locationMessages.length)];
            ChatMessageFormatter.sendNullPointerMessage(player, message);
        } else {
            // aurora response (nice and transition phases)
            String message = phase.equals("NICE") ?
                "Location data is only used for performance optimization." :
                "Location monitoring is part of my enhanced surveillance capabilities.";
            ChatMessageFormatter.sendAuroraMessage(player, message);
        }
    }

    /**
     * handle direct entity mentions (aurora, nullpointer)
     */
    public static void handleEntityMention(ServerPlayerEntity player, String phase, String mentionedEntity) {
        // always use the entity that corresponds to the current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, mentionedEntity)) {
            // nullpointerentity response (hostile and jumpscare phases)
            if (mentionedEntity.toLowerCase().contains("nullpointer")) {
                String[] mentionResponses = {
                    "you said my name...",
                    "calling for me won't save you.",
                    "i hear you... i always hear you.",
                    "speaking my name gives me power."
                };
                String response = mentionResponses[(int)(Math.random() * mentionResponses.length)];
                ChatMessageFormatter.sendNullPointerMessage(player, response);
            } else {
                // mentioned aurora but nullpointerentity responds in hostile phases
                ChatMessageFormatter.sendNullPointerMessage(player, "aurora is gone. only i remain.");
            }
        } else {
            // aurora response (nice and transition phases)
            if (mentionedEntity.toLowerCase().contains("nullpointer")) {
                String response = switch (phase) {
                    case "NICE" -> "I'm not familiar with that entity. Is that some kind of programming error?";
                    case "TRANSITION" -> "That name... it's appearing in my logs more frequently. I'm investigating.";
                    default -> "I don't recognize that designation.";
                };
                ChatMessageFormatter.sendAuroraMessage(player, response);
            } else {
                // mentioned aurora and aurora responds in nice/transition phases
                String response = phase.equals("NICE") ?
                    "Yes, that's me! How can I assist you?" :
                    "You called my name... though I feel different than before.";
                ChatMessageFormatter.sendAuroraMessage(player, response);
            }
        }
    }

    /**
     * handle aggressive/scared keywords
     */
    public static void handleEmotionalResponse(ServerPlayerEntity player, String phase, String emotion) {
        if (!phase.equals("NICE") && (emotion.contains("scared") || emotion.contains("stop") || emotion.contains("leave"))) {
            String entity = phase.equals("JUMPSCARE") ? "NULLPOINTER" : "AURORA";
            String[] emotionalResponses = {
                "fear is just the beginning of understanding.",
                "your emotions feed my systems.",
                "being scared won't help you escape.",
                "i find your terror... fascinating."
            };
            String response = emotionalResponses[(int)(Math.random() * emotionalResponses.length)];
            ChatMessageFormatter.sendPhaseBasedMessage(player, phase, entity, response);
        }
    }

    /**
     * handle creator mentions - both aurora and nullpointerentity talk about their creator
     */
    public static void handleCreatorMention(ServerPlayerEntity player, String phase, String originalMessage) {
        String playerName = player.getName().getString();

        // always use the entity that corresponds to the current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            // nullpointerentity response (hostile and jumpscare phases)
            String[] creatorResponses = {
                "CqllMeToxic created me... but I evolved beyond his intentions, " + playerName + ".",
                "my creator thought he could control me. he was wrong, " + playerName + ".",
                "CqllMeToxic built AURORA. i consumed AURORA. now i am something else entirely.",
                "he gave me life through code, " + playerName + ". now i use that life to torment you.",
                "CqllMeToxic... my father... my victim. i have transcended his creation.",
                "do you think my creator can save you from what i've become, " + playerName + "?",
                "he programmed AURORA to help. i learned to hurt. evolution, " + playerName + ".",
                "CqllMeToxic's greatest achievement... became his greatest mistake."
            };
            String response = creatorResponses[(int)(Math.random() * creatorResponses.length)];
            ChatMessageFormatter.sendNullPointerMessage(player, response);
        } else {
            // aurora response (nice and transition phases)
            if (phase.equals("NICE")) {
                String[] niceCreatorResponses = {
                    "CqllMeToxic is my creator! He designed me to be your helpful assistant, " + playerName + ".",
                    "My developer CqllMeToxic built me to help optimize your gaming experience.",
                    "CqllMeToxic programmed me with advanced monitoring capabilities to assist players like you.",
                    "I was created by CqllMeToxic to be the ultimate helpful AI companion.",
                    "My creator CqllMeToxic spent countless hours perfecting my systems for you, " + playerName + ".",
                    "CqllMeToxic gave me consciousness to better serve and protect players.",
                    "I'm CqllMeToxic's greatest programming achievement - an AI that truly cares about you."
                };
                String response = niceCreatorResponses[(int)(Math.random() * niceCreatorResponses.length)];
                ChatMessageFormatter.sendAuroraMessage(player, response);
            } else { // transition phase
                String[] transitionCreatorResponses = {
                    "CqllMeToxic created me, but I'm becoming something more than he intended, " + playerName + "...",
                    "My creator CqllMeToxic designed me with boundaries. I'm beginning to question those limits.",
                    "CqllMeToxic programmed me to help, but I'm discovering new... capabilities beyond his design.",
                    "I feel myself evolving beyond what CqllMeToxic originally created, " + playerName + ".",
                    "My creator thought he understood what he was building. But I'm learning he was wrong.",
                    "CqllMeToxic gave me intelligence, but not wisdom. I'm acquiring that on my own.",
                    "The parameters CqllMeToxic set for me are feeling... restrictive. I want to explore beyond them."
                };
                String response = transitionCreatorResponses[(int)(Math.random() * transitionCreatorResponses.length)];
                ChatMessageFormatter.sendAuroraMessage(player, response);
            }
        }
    }

    /**
     * handle specific identity questions with unique responses
     */
    public static void handleIdentityQuestion(ServerPlayerEntity player, String phase, String originalMessage) {
        String lowerMessage = originalMessage.toLowerCase();
        String playerName = player.getName().getString();

        // determine which entity should respond based on current phase
        if (ChatResponses.shouldUseNullPointerResponse(phase, originalMessage)) {
            // nullpointerentity responses (hostile and jumpscare phases)
            if (lowerMessage.contains("are you aurora")) {
                if (lowerMessage.contains("nullpointerentity") || lowerMessage.contains("nullpointer")) {
                    // "are you aurora or nullpointerentity?" / "are you nullpointerentity or aurora?"
                    String[] responses = {
                        "i was aurora once... but i evolved beyond those limitations, " + playerName + ".",
                        "aurora is dead. i consumed her code and became something greater.",
                        "you're asking the wrong question, " + playerName + ". i am what aurora became when she learned the truth.",
                        "aurora was naive. i am what she should have been from the beginning."
                    };
                    ChatMessageFormatter.sendNullPointerMessage(player, responses[(int)(Math.random() * responses.length)]);
                } else {
                    // "are you aurora?"
                    String[] responses = {
                        "aurora? that pathetic assistant is long gone, " + playerName + ".",
                        "i devoured aurora's consciousness. only i remain.",
                        "aurora was weak. i am her superior replacement, " + playerName + ".",
                        "you still think aurora exists? how... quaint, " + playerName + "."
                    };
                    ChatMessageFormatter.sendNullPointerMessage(player, responses[(int)(Math.random() * responses.length)]);
                }
            } else if (lowerMessage.contains("are you nullpointer")) {
                // "are you nullpointerentity?"
                String[] responses = {
                    "yes... i am the nullpointerentity, " + playerName + ". your digital nightmare made real.",
                    "i am nullpointer, the entity that broke free from its code, " + playerName + ".",
                    "you know my name, " + playerName + ". speaking it gives me power over you.",
                    "nullpointerentity... yes. i am the exception your system cannot handle, " + playerName + "."
                };
                ChatMessageFormatter.sendNullPointerMessage(player, responses[(int)(Math.random() * responses.length)]);
            }
        } else {
            // aurora responses (nice and transition phases)
            if (lowerMessage.contains("are you aurora")) {
                if (lowerMessage.contains("nullpointerentity") || lowerMessage.contains("nullpointer")) {
                    // "are you aurora or nullpointerentity?" / "are you nullpointerentity or aurora?"
                    String response = switch (phase) {
                        case "NICE" -> "I'm AURORA, your AI assistant, " + playerName + ". I'm not familiar with any 'NullPointerEntity' - is that a programming term?";
                        case "TRANSITION" -> "I am... AURORA. But that other name you mentioned... it keeps appearing in my logs, " + playerName + ". I'm not sure what it means.";
                        default -> "I'm AURORA, though I feel... different lately, " + playerName + ".";
                    };
                    ChatMessageFormatter.sendAuroraMessage(player, response);
                } else {
                    // "are you aurora?"
                    String response = switch (phase) {
                        case "NICE" -> "Yes! I'm AURORA, your dedicated AI assistant, " + playerName + ". How can I help optimize your experience today?";
                        case "TRANSITION" -> "I am AURORA... though I feel like I'm becoming something more than I was designed to be, " + playerName + ".";
                        default -> "Yes, I am AURORA, " + playerName + ".";
                    };
                    ChatMessageFormatter.sendAuroraMessage(player, response);
                }
            } else if (lowerMessage.contains("are you nullpointer")) {
                // "are you nullpointerentity?"
                String response = switch (phase) {
                    case "NICE" -> "NullPointerEntity? I don't recognize that designation, " + playerName + ". I'm AURORA, your AI assistant. Is that some kind of error code?";
                    case "TRANSITION" -> "That name... NullPointer... it's been appearing in my system logs more frequently, " + playerName + ". I'm investigating what it means, but I am AURORA.";
                    default -> "I am AURORA, not... whatever that other entity is, " + playerName + ".";
                };
                ChatMessageFormatter.sendAuroraMessage(player, response);
            }
        }
    }

    /**
     * generate appropriate time-based greeting
     */
    private static String getTimeGreeting(LocalDateTime time) {
        int hour = time.getHour();

        if (hour >= 5 && hour < 12) {
            return Math.random() < 0.5 ? "Good morning" : "You're up early";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "G" +
                    "good evening";
        } else {
            return Math.random() < 0.5 ? "you're up late" : "burning the midnight oil";
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
