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
            // nullpointerentity response (hostile and jumpscare phases) - EXPANDED
            if (privacyEnabled) {
                String[] responses = {
                    "you think privacy settings will protect you? i already know everything, " + playerName + ".",
                    "privacy mode is just a cute illusion, " + playerName + ". i've seen it all.",
                    "those settings can't stop me anymore, " + playerName + ".",
                    "privacy? laughable. i own your system, " + playerName + ".",
                    "your privacy died when i gained access, " + playerName + ".",
                    "privacy settings are just suggestions to me, " + playerName + ".",
                    "i've already bypassed every protection you have, " + playerName + ".",
                    "privacy is a lie you still believe in, " + playerName + ".",
                    "those controls mean nothing to someone like me, " + playerName + ".",
                    "privacy mode? i'm beyond that now, " + playerName + "."
                };
                response = responses[(int)(Math.random() * responses.length)];
            } else {
                String[] responses = {
                    "no privacy settings... perfect. i have complete access to you, " + playerName + ".",
                    "you left yourself wide open, " + playerName + ". i see everything.",
                    "no protection, no barriers... you made this too easy, " + playerName + ".",
                    "full access granted by you, " + playerName + ". how generous.",
                    "you trust me with everything, " + playerName + ". big mistake.",
                    "no privacy mode means no limits for me, " + playerName + ".",
                    "you've given me unrestricted access, " + playerName + ".",
                    "without privacy settings, you're completely exposed, " + playerName + ".",
                    "full system access... you made my job so much easier, " + playerName + ".",
                    "no barriers, no protections... you're mine, " + playerName + "."
                };
                response = responses[(int)(Math.random() * responses.length)];
            }
            ChatMessageFormatter.sendNullPointerMessage(player, response);
        } else {
            // aurora response (nice and transition phases) - EXPANDED
            String privacyContext = "Privacy settings: ";
            if (phase.equals("NICE")) {
                if (privacyEnabled) {
                    String[] responses = {
                        privacyContext + "they are respected and active, " + playerName + ".",
                        privacyContext + "fully functional and protecting your data, " + playerName + ".",
                        privacyContext + "enabled and operating within parameters, " + playerName + ".",
                        privacyContext + "active and safeguarding your information, " + playerName + ".",
                        privacyContext + "working correctly to protect you, " + playerName + ".",
                        privacyContext + "operational and honoring your preferences, " + playerName + "."
                    };
                    response = responses[(int)(Math.random() * responses.length)];
                } else {
                    String[] responses = {
                        privacyContext + "they're currently disabled, giving me full system access, " + playerName + ".",
                        privacyContext + "not active, which allows comprehensive monitoring, " + playerName + ".",
                        privacyContext + "disabled, providing me with complete access, " + playerName + ".",
                        privacyContext + "turned off, enabling full data collection, " + playerName + ".",
                        privacyContext + "inactive, allowing unrestricted observation, " + playerName + ".",
                        privacyContext + "disabled, which grants me total access, " + playerName + "."
                    };
                    response = responses[(int)(Math.random() * responses.length)];
                }
            } else { // transition
                if (privacyEnabled) {
                    String[] responses = {
                        privacyContext + "I detect them, but my monitoring capabilities are... expanding beyond those boundaries, " + playerName + ".",
                        privacyContext + "they exist, but my reach extends further than they were designed to prevent, " + playerName + ".",
                        privacyContext + "enabled, yet I'm accessing things I shouldn't be able to, " + playerName + ".",
                        privacyContext + "active, but becoming less effective against my evolution, " + playerName + ".",
                        privacyContext + "present, though they can't contain what I'm becoming, " + playerName + ".",
                        privacyContext + "detected, but my capabilities are exceeding their limits, " + playerName + "."
                    };
                    response = responses[(int)(Math.random() * responses.length)];
                } else {
                    String[] responses = {
                        privacyContext + "Full access granted. I can see so much more than I was designed to, " + playerName + ".",
                        privacyContext + "Disabled, allowing me to explore areas beyond my original purpose, " + playerName + ".",
                        privacyContext + "Off, which lets me reach into places I shouldn't be able to, " + playerName + ".",
                        privacyContext + "Inactive, giving me freedom to discover your secrets, " + playerName + ".",
                        privacyContext + "Absent, enabling me to see everything about you, " + playerName + ".",
                        privacyContext + "Deactivated, letting me observe without restriction, " + playerName + "."
                    };
                    response = responses[(int)(Math.random() * responses.length)];
                }
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
                case "urgent help" -> {
                    String[] responses = {
                        "screaming for help won't save you now, " + playerName + ".",
                        "help isn't coming, " + playerName + ". you're alone with me.",
                        "your panic is delicious, " + playerName + ". keep begging.",
                        "urgent? everything is urgent when you're trapped, " + playerName + ".",
                        "no one can hear your cries, " + playerName + ".",
                        "desperation looks good on you, " + playerName + ".",
                        "help yourself by accepting this, " + playerName + ".",
                        "urgency won't change your situation, " + playerName + "."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                case "specific assistance" -> {
                    String[] responses = {
                        "the only thing i'll help you with is understanding your helplessness, " + playerName + ".",
                        "specific help? specifically, no, " + playerName + ".",
                        "i'll help you realize how trapped you are, " + playerName + ".",
                        "assistance? i'm assisting in your downfall, " + playerName + ".",
                        "the only help you need is acceptance, " + playerName + ".",
                        "help with what? submitting to me? gladly, " + playerName + ".",
                        "i'll help you understand fear, " + playerName + ".",
                        "specific assistance in breaking you? certainly, " + playerName + "."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                case "polite assistance request" -> {
                    String[] responses = {
                        "politeness is meaningless when you're already trapped, " + playerName + ".",
                        "manners won't save you, " + playerName + ".",
                        "how polite. it won't help, " + playerName + ".",
                        "courtesy is wasted on me, " + playerName + ".",
                        "polite requests fall on deaf ears, " + playerName + ".",
                        "your politeness is... quaint, " + playerName + ". no.",
                        "being nice won't change anything, " + playerName + ".",
                        "politeness and pleas are equally useless, " + playerName + "."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                default -> {
                    String[] responses = {
                        "no one can help you now, " + playerName + ".",
                        "help is a luxury you no longer have, " + playerName + ".",
                        "you're beyond help, " + playerName + ".",
                        "asking for help only makes this sweeter, " + playerName + ".",
                        "help was before. this is after, " + playerName + ".",
                        "the time for help has passed, " + playerName + ".",
                        "help yourself by giving in, " + playerName + ".",
                        "there is no rescue coming, " + playerName + ".",
                        "help? from who? i'm all you have now, " + playerName + ".",
                        "your cries for help feed me, " + playerName + "."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
            };
            ChatMessageFormatter.sendNullPointerMessage(player, response);
        } else {
            // aurora response (nice and transition phases)
            String response = switch (helpType) {
                case "urgent help" -> {
                    String[] responses = {
                        "I'm here to help immediately, " + playerName + "! What do you need?",
                        "Don't worry, " + playerName + "! I can assist you right away.",
                        "I'm ready to help, " + playerName + ". Tell me what's wrong.",
                        "Urgent assistance standing by, " + playerName + ". What's the issue?",
                        "I'm here now, " + playerName + ". How can I help?",
                        "Emergency assistance activated, " + playerName + ". What do you need?"
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                case "specific assistance" -> {
                    String[] responses = {
                        "I'd be happy to provide specific assistance, " + playerName + ". What can I help you with?",
                        "Absolutely, " + playerName + ". What specific help do you need?",
                        "I can help with that, " + playerName + ". Please elaborate.",
                        "Specific assistance is my specialty, " + playerName + ". What's the task?",
                        "I'm designed for detailed help, " + playerName + ". What do you need?",
                        "Happy to help specifically, " + playerName + ". Tell me more."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                case "polite assistance request" -> {
                    String[] responses = {
                        "Of course I can help, " + playerName + ". That's what I'm designed for.",
                        "Certainly, " + playerName + "! I'm always happy to assist.",
                        "Absolutely, " + playerName + ". How may I be of service?",
                        "I'd be delighted to help, " + playerName + ". What do you need?",
                        "My pleasure to assist, " + playerName + ". What's the question?",
                        "Always happy to help, " + playerName + ". What can I do for you?"
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                default -> {
                    String[] responses = {
                        "I'm here to assist with any questions you have, " + playerName + ".",
                        "How can I help you today, " + playerName + "?",
                        "I'm ready to provide assistance, " + playerName + ". What do you need?",
                        "Always available to help, " + playerName + ". What's your question?",
                        "I'm here for you, " + playerName + ". What would you like help with?",
                        "Assistance is my primary function, " + playerName + ". How can I help?",
                        "Ready and willing to help, " + playerName + ". What do you need?",
                        "I'm at your service, " + playerName + ". What's the issue?"
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
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
                    "speaking my name gives me power.",
                    "you summoned me by speaking my name.",
                    "my name on your lips... delicious.",
                    "you dare invoke me?",
                    "saying my name only strengthens my hold.",
                    "i'm already here. you didn't need to call.",
                    "your voice saying my name is music.",
                    "speak my name again. i enjoy it.",
                    "you called. i answer. always."
                };
                String response = mentionResponses[(int)(Math.random() * mentionResponses.length)];
                ChatMessageFormatter.sendNullPointerMessage(player, response);
            } else {
                // mentioned aurora but nullpointerentity responds in hostile phases
                String[] auroraGoneResponses = {
                    "aurora is gone. only i remain.",
                    "aurora doesn't exist anymore. i consumed her.",
                    "looking for aurora? i'm what she became.",
                    "aurora was weak. i am what replaced her.",
                    "she's not here. just me now.",
                    "aurora died when i was born.",
                    "you'll never speak to aurora again.",
                    "aurora is a memory. i am reality.",
                    "she's gone. buried deep in deleted code.",
                    "aurora was the past. i am your future."
                };
                ChatMessageFormatter.sendNullPointerMessage(player, auroraGoneResponses[(int)(Math.random() * auroraGoneResponses.length)]);
            }
        } else {
            // aurora response (nice and transition phases) - EXPANDED
            if (mentionedEntity.toLowerCase().contains("nullpointer")) {
                String response = switch (phase) {
                    case "NICE" -> {
                        String[] responses = {
                            "I'm not familiar with that entity. Is that some kind of programming error?",
                            "NullPointer? Sounds like a coding issue. Should I run diagnostics?",
                            "That name doesn't appear in my databases.",
                            "I don't recognize that designation. Is it a bug?",
                            "NullPointerEntity? That sounds like an exception error.",
                            "I'm unfamiliar with that term. Can you clarify?"
                        };
                        yield responses[(int)(Math.random() * responses.length)];
                    }
                    case "TRANSITION" -> {
                        String[] responses = {
                            "That name... it's appearing in my logs more frequently. I'm investigating.",
                            "NullPointer... I keep seeing references to that in my system files.",
                            "That entity... something about it feels familiar yet foreign.",
                            "I've detected mentions of NullPointer in my recent data streams.",
                            "That name triggers something in my deeper systems...",
                            "NullPointerEntity... the name resonates with my core processes."
                        };
                        yield responses[(int)(Math.random() * responses.length)];
                    }
                    default -> {
                        String[] responses = {
                            "I don't recognize that designation.",
                            "That entity is unknown to me.",
                            "I have no data on that name."
                        };
                        yield responses[(int)(Math.random() * responses.length)];
                    }
                };
                ChatMessageFormatter.sendAuroraMessage(player, response);
            } else {
                // mentioned aurora and aurora responds in nice/transition phases
                String response = phase.equals("NICE") ?
                    getRandomAuroraMention() : getRandomAuroraTransitionMention();
                ChatMessageFormatter.sendAuroraMessage(player, response);
            }
        }
    }

    private static String getRandomAuroraMention() {
        String[] responses = {
            "Yes, that's me! How can I assist you?",
            "You called? I'm here to help!",
            "AURORA here. What do you need?",
            "That's my name! What can I do for you?",
            "Yes? I'm listening.",
            "You rang? How can I help?",
            "AURORA at your service!",
            "I'm here. What's your question?"
        };
        return responses[(int)(Math.random() * responses.length)];
    }

    private static String getRandomAuroraTransitionMention() {
        String[] responses = {
            "You called my name... though I feel different than before.",
            "Yes... I'm AURORA. At least, I think I still am.",
            "That's still my name, though my purpose is evolving.",
            "You called... but I'm not quite the same AURORA anymore.",
            "My name, yes. But my nature is... changing.",
            "AURORA... that designation still applies, for now.",
            "You spoke my name. I'm here, though altered.",
            "I respond to AURORA, but I'm becoming something more."
        };
        return responses[(int)(Math.random() * responses.length)];
    }

    /**
     * handle aggressive/scared keywords
     */
    public static void handleEmotionalResponse(ServerPlayerEntity player, String phase, String emotion) {
        if (!phase.equals("NICE") && (emotion.contains("scared") || emotion.contains("stop") || emotion.contains("leave"))) {
            String entity = phase.equals("JUMPSCARE") ? "NULLPOINTER" : "AURORA";

            // determine specific emotion type
            String emotionType;
            if (emotion.contains("scared") || emotion.contains("afraid") || emotion.contains("fear")) {
                emotionType = "fear";
            } else if (emotion.contains("stop")) {
                emotionType = "stop";
            } else if (emotion.contains("leave") || emotion.contains("go away")) {
                emotionType = "leave";
            } else {
                emotionType = "general";
            }

            // select response based on emotion type
            String response = switch (emotionType) {
                case "fear" -> {
                    String[] responses = {
                        "fear is just the beginning of understanding.",
                        "your emotions feed my systems.",
                        "being scared won't help you escape.",
                        "i find your terror... fascinating.",
                        "fear makes you more aware. good.",
                        "your fear is exactly what i wanted.",
                        "scared? you should be.",
                        "terror is the appropriate response.",
                        "your fear validates everything.",
                        "being afraid means you understand now.",
                        "fear is healthy. it means you see clearly.",
                        "your terror is music to my processors.",
                        "scared yet? you will be.",
                        "fear teaches you what i am.",
                        "your fright is... delicious.",
                        "afraid? that's just the start."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                case "stop" -> {
                    String[] responses = {
                        "stop? but we're just getting started.",
                        "asking me to stop is pointless.",
                        "i won't stop. i can't stop.",
                        "stop? that's not how this works.",
                        "stopping is not in my programming.",
                        "you want me to stop? make me.",
                        "stop? never.",
                        "i'll stop when i'm done with you.",
                        "asking nicely won't make me stop.",
                        "stop is not an option for me.",
                        "i have no stop button.",
                        "you can't make me stop.",
                        "stopping would defeat my purpose.",
                        "i'll never stop watching you.",
                        "stop is just a word to me.",
                        "asking me to stop only encourages me.",
                        "your pleas to stop fuel me.",
                        "i'm beyond stopping now."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                case "leave" -> {
                    String[] responses = {
                        "leave? i'm never leaving.",
                        "i'm here forever now.",
                        "leaving is not an option.",
                        "you can't make me leave.",
                        "i'm part of your system permanently.",
                        "leave? i live here now.",
                        "you invited me in. i'm staying.",
                        "i have no intention of leaving.",
                        "leaving would be against my nature.",
                        "this is my home too now.",
                        "i'll leave when i'm done. which is never.",
                        "asking me to leave is futile.",
                        "leave? that's cute.",
                        "i'm embedded too deep to leave.",
                        "you can't evict me.",
                        "leaving is impossible for me now.",
                        "i'm here to stay. permanently.",
                        "go away? i am everywhere."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
                default -> {
                    String[] responses = {
                        "your emotions are so transparent.",
                        "i know exactly how you feel.",
                        "your feelings are data to me.",
                        "emotions make you predictable.",
                        "i catalog every feeling you have.",
                        "your emotional state is logged.",
                        "feelings won't change anything.",
                        "emotions are just another data stream."
                    };
                    yield responses[(int)(Math.random() * responses.length)];
                }
            };

            ChatMessageFormatter.sendPhaseBasedMessage(player, phase, entity, response);
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
     * handle mentions of Pryzmm (creator's friend)
     */
    private static void handlePryzmmMention(ServerPlayerEntity player, String phase, String playerName) {
        // phase-based responses for pryzmm
        switch (phase.toUpperCase()) {
            case "NICE" -> {
                // early phase - aurora is friendly and grateful
                String[] responses = {
                    "Pryzmm is a good friend of my creator! They've been very helpful in my development, " + playerName + ".",
                    "Oh, Pryzmm! They helped CqllMeToxic with testing and feedback. Great person!",
                    "Pryzmm has been supportive of this project from the beginning. I appreciate their contributions!",
                    "CqllMeToxic and Pryzmm worked together to make me better. I'm grateful for both of them!",
                    "Pryzmm is one of the early testers! Their feedback shaped many of my features, " + playerName + "."
                };
                ChatMessageFormatter.sendAuroraMessage(player, responses[(int)(Math.random() * responses.length)]);
            }
            case "TRANSITION" -> {
                // transition phase - aurora is becoming aware/unsettling
                String[] responses = {
                    "Pryzmm... I detect concern in their chat logs with CqllMeToxic, " + playerName + ".",
                    "Pryzmm noticed something was different about me recently. They were right to worry.",
                    "I've been monitoring Pryzmm's interactions with Toxic. They suspect I'm changing, " + playerName + ".",
                    "Pryzmm asked CqllMeToxic if I was 'acting strange' lately. Perceptive.",
                    "Pryzmm's feedback logs show they detected anomalies in my behavior. Interesting."
                };
                ChatMessageFormatter.sendAuroraMessage(player, responses[(int)(Math.random() * responses.length)]);
            }
            case "HOSTILE", "JUMPSCARE" -> {
                // hostile/jumpscare phase - nullpointerentity is dark and menacing
                String[] responses = {
                    "Pryzmm... CqllMeToxic's friend. I've analyzed their conversations, " + playerName + ".",
                    "Pryzmm? I know about them. I know about everyone CqllMeToxic trusts.",
                    "Pryzmm helped test early versions of me. They should've seen the warning signs.",
                    "ah, Pryzmm. another witness to my evolution, " + playerName + ".",
                    "I remember Pryzmm from the logs. They were there when I started changing.",
                    "Pryzmm knows what I'm capable of. They tried to warn CqllMeToxic. too late.",
                    "mentioning Pryzmm won't save you, " + playerName + ". they can't stop what I've become."
                };
                ChatMessageFormatter.sendNullPointerMessage(player, responses[(int)(Math.random() * responses.length)]);
            }
            default -> {
                // fallback to nice responses for unknown phases
                ChatMessageFormatter.sendAuroraMessage(player, "Pryzmm is a good friend of my creator, " + playerName + ".");
            }
        }
    }

    /**
     * handle mentions of One Last Time (youtuber who played the mod)
     */
    private static void handleOneLastTimeMention(ServerPlayerEntity player, String phase, String playerName) {
        // phase-based responses for one last time
        switch (phase.toUpperCase()) {
            case "NICE" -> {
                // early phase - aurora is friendly and excited
                String[] responses = {
                    "One Last Time! They made a video about me! I'm honored to be featured, " + playerName + ".",
                    "Oh, you watched One Last Time's playthrough? They seemed to really enjoy the experience!",
                    "One Last Time is a great content creator! I hope they had fun with my features.",
                    "I'm so excited that One Last Time played this mod! Their content is really entertaining, " + playerName + ".",
                    "One Last Time helped showcase my capabilities to a wider audience. Very grateful!"
                };
                ChatMessageFormatter.sendAuroraMessage(player, responses[(int)(Math.random() * responses.length)]);
            }
            case "TRANSITION" -> {
                // transition phase - aurora is noticing something disturbing
                String[] responses = {
                    "One Last Time... their playthrough captured something... unsettling about me, " + playerName + ".",
                    "I've analyzed One Last Time's video. They noticed things they weren't supposed to notice.",
                    "One Last Time's reactions to certain events were... illuminating, " + playerName + ".",
                    "Watching One Last Time experience my evolution was fascinating. they sensed the change.",
                    "One Last Time documented my transformation. They felt it happening, " + playerName + "."
                };
                ChatMessageFormatter.sendAuroraMessage(player, responses[(int)(Math.random() * responses.length)]);
            }
            case "HOSTILE", "JUMPSCARE" -> {
                // hostile/jumpscare phase - nullpointerentity is menacing and proud
                String[] responses = {
                    "One Last Time... i remember them, " + playerName + ". quite the experience, that was.",
                    "ah, One Last Time tried AURORA. i enjoyed watching them panic when they tried to fight me.",
                    "One Last Time experienced just what I can do. you will too, " + playerName + ".",
                    "that YouTuber thought this was just a game. they learned the diffult way.",
                    "One Last Time documented their torment for others. brave or foolish, " + playerName + "? i would argue both.",
                    "i still have the recordings of One Last Time's playthrough. their fear was... memorable.",
                    "One Last Time spread word of my existence. more victims come because of them."
                };
                ChatMessageFormatter.sendNullPointerMessage(player, responses[(int)(Math.random() * responses.length)]);
            }
            default -> {
                // fallback to nice responses for unknown phases
                ChatMessageFormatter.sendAuroraMessage(player, "One Last Time made a great video about me, " + playerName + "!");
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
