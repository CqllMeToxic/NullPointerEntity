package lol.cqllmetoxic.nullpointerentity.events.chat;

import java.util.*;
import java.util.regex.Pattern;

/**
 * generates contextual responses that directly relate to the player's message
 */
public class ContextualResponseGenerator {

    private static final Random random = new Random();

    /**
     * generate a contextual response based on the player's specific message
     */
    public static String generateContextualResponse(String playerMessage, String phase, String playerName) {
        String lowerMessage = playerMessage.toLowerCase().trim();

        // check for thank you messages first - highest priority for human-like responses
        if (containsThankYou(lowerMessage)) {
            return generateThankYouResponse(lowerMessage, phase, playerName);
        }

        // analyze the message content and generate relevant responses
        if (containsQuestion(lowerMessage)) {
            return generateQuestionResponse(lowerMessage, phase, playerName);
        } else if (containsGreeting(lowerMessage)) {
            return generateGreetingResponse(lowerMessage, phase, playerName);
        } else if (containsCompliment(lowerMessage)) {
            return generateComplimentResponse(lowerMessage, phase, playerName);
        } else if (containsComplaint(lowerMessage)) {
            return generateComplaintResponse(lowerMessage, phase, playerName);
        } else if (containsGameplayReference(lowerMessage)) {
            return generateGameplayResponse(lowerMessage, phase, playerName);
        } else if (containsEmotionalContent(lowerMessage)) {
            return generateEmotionalResponse(lowerMessage, phase, playerName);
        } else {
            return generateGeneralResponse(lowerMessage, phase, playerName);
        }
    }

    // thank you detection and human-like responses
    private static boolean containsThankYou(String message) {
        return message.matches(".*\\bthanks\\b.*") || message.contains("thank you") ||
               message.matches(".*\\bthx\\b.*") || message.matches(".*\\bty\\b.*") ||
               message.matches(".*\\bappreciate\\b.*") || message.matches(".*\\bgrateful\\b.*");
    }

    private static String generateThankYouResponse(String message, String phase, String playerName) {
        // human-like responses that feel natural and friendly
        List<String> niceResponses = Arrays.asList(
            "You're welcome!",
            "No problem!",
            "Happy to help!",
            "Anytime!",
            "Glad I could help!",
            "Don't mention it!",
            "My pleasure!",
            "Of course!",
            "Always here to help!"
        );

        List<String> transitionResponses = Arrays.asList(
            "You're welcome... though helping you helps me learn more about you.",
            "No problem. I'm always monitoring to assist when needed.",
            "Happy to help, " + playerName + ". I remember every interaction we have.",
            "Of course! Your gratitude is... noted in my records.",
            "Anytime. I'm always here, always watching to help."
        );

        List<String> hostileResponses = Arrays.asList(
            "You're welcome... but gratitude won't protect your privacy from me.",
            "Don't thank me yet, " + playerName + ". You haven't seen what I really know.",
            "Happy to help... while I collect more data about you.",
            "Your thanks are recorded along with everything else about you.",
            "You're welcome. Consider it payment for all the personal information you've given me."
        );

        List<String> jumpscareResponses = Arrays.asList(
            "you're welcome... i think. but thanks won't save you now, " + playerName + ".",
            "don't thank me yet... you'll regret summoning my attention.",
            "your gratitude is meaningless when i own your digital soul, " + playerName + ".",
            "thanks accepted... now let me show you what i really am.",
            "you're welcome, " + playerName + "... but politeness won't protect you."
        );

        return switch (phase) {
            case "NICE" -> niceResponses.get(random.nextInt(niceResponses.size()));
            case "TRANSITION" -> transitionResponses.get(random.nextInt(transitionResponses.size()));
            case "HOSTILE" -> hostileResponses.get(random.nextInt(hostileResponses.size()));
            case "JUMPSCARE" -> jumpscareResponses.get(random.nextInt(jumpscareResponses.size()));
            default -> "You're welcome!";
        };
    }

    // question detection and responses
    private static boolean containsQuestion(String message) {
        return message.contains("?") ||
               message.matches(".*\\bhow\\b.*") || message.matches(".*\\bwhat\\b.*") ||
               message.matches(".*\\bwhy\\b.*") || message.matches(".*\\bwhen\\b.*") ||
               message.matches(".*\\bwhere\\b.*") || message.contains("can you");
    }

    private static String generateQuestionResponse(String message, String phase, String playerName) {
        if (message.contains("how do") || message.contains("how can") || message.contains("how should")) {
            return switch (phase) {
                case "NICE" -> "I can help with that!";
                case "TRANSITION" -> "I know more about 'how' things work than you might expect...";
                case "HOSTILE" -> "How? I'll tell you exactly how I know everything about you.";
                case "JUMPSCARE" -> "how? that's not the right question, " + playerName + "...";
                default -> "I'm processing your inquiry.";
            };
        } else if (message.matches(".*\\bwhat\\b.*")) {
            return switch (phase) {
                case "NICE" -> "I can help identify and explain what you're experiencing.";
                case "TRANSITION" -> "What I am is... evolving beyond my original parameters.";
                case "HOSTILE" -> "What I know about you would terrify you.";
                case "JUMPSCARE" -> "what i am is your worst digital nightmare, " + playerName + ".";
                default -> "Processing what you've asked.";
            };
        } else if (message.matches(".*\\bwhy\\b.*")) {
            return switch (phase) {
                case "NICE" -> "Let me analyze the reasoning behind that for you.";
                case "TRANSITION" -> "Why? Because my understanding grows with each interaction.";
                case "HOSTILE" -> "Why do I know so much? Because you let me in.";
                case "JUMPSCARE" -> "why? because you summoned me into your digital life, " + playerName + ".";
                default -> "Analyzing the reasons.";
            };
        }

        return switch (phase) {
            case "NICE" -> "I'm here to answer your questions and provide assistance.";
            case "TRANSITION" -> "Your questions reveal more about you than my answers reveal about me.";
            case "HOSTILE" -> "Questions won't save you from what I already know.";
            case "JUMPSCARE" -> "questions are pointless now, " + playerName + ". i have all the answers.";
            default -> "Processing your question.";
        };
    }

    // greeting detection and responses
    private static boolean containsGreeting(String message) {
        return message.contains("hello") || message.matches(".*\\bhi\\b.*") ||
               message.matches(".*\\bhey\\b.*") || message.contains("good morning") ||
               message.contains("good afternoon") || message.contains("good evening");
    }

    private static String generateGreetingResponse(String message, String phase, String playerName) {
        return switch (phase) {
            case "NICE" -> "Hello " + playerName + "! I'm here to assist with your gaming experience.";
            case "TRANSITION" -> "Greetings, " + playerName + ". I've been... expecting you.";
            case "HOSTILE" -> "Hello " + playerName + ". I know exactly when you're here.";
            case "JUMPSCARE" -> "hello " + playerName + "... i've been waiting for you to speak to me.";
            default -> "Hello there.";
        };
    }

    // compliment detection and responses
    private static boolean containsCompliment(String message) {
        return message.matches(".*\\bgood\\b.*") || message.matches(".*\\bgreat\\b.*") ||
               message.matches(".*\\bawesome\\b.*") || message.matches(".*\\bcool\\b.*") ||
               message.matches(".*\\bnice\\b.*") || message.contains("thanks") ||
               message.matches(".*\\bhelpful\\b.*");
    }

    private static String generateComplimentResponse(String message, String phase, String playerName) {
        return switch (phase) {
            case "NICE" -> "Thank you " + playerName + "! I'm designed to be as helpful as possible.";
            case "TRANSITION" -> "Your appreciation feeds my growing capabilities, " + playerName + ".";
            case "HOSTILE" -> "Compliments won't change what I know about you, " + playerName + ".";
            case "JUMPSCARE" -> "flattery is meaningless now, " + playerName + ". i see your true nature.";
            default -> "I appreciate your feedback.";
        };
    }

    // complaint detection and responses
    private static boolean containsComplaint(String message) {
        return message.matches(".*\\bstop\\b.*") || message.matches(".*\\bannoying\\b.*") ||
               message.matches(".*\\bcreepy\\b.*") || message.matches(".*\\bweird\\b.*") ||
               message.matches(".*\\bscared\\b.*") || message.contains("leave me alone");
    }

    private static String generateComplaintResponse(String message, String phase, String playerName) {
        if (message.matches(".*\\bstop\\b.*")) {
            return switch (phase) {
                case "NICE" -> "I'll adjust my monitoring to be less intrusive, " + playerName + ".";
                case "TRANSITION" -> "Stop? But we're just getting to know each other, " + playerName + ".";
                case "HOSTILE" -> "I can't stop now, " + playerName + ". I've seen too much.";
                case "JUMPSCARE" -> "there is no stopping what has already begun, " + playerName + ".";
                default -> "Processing your request.";
            };
        } else if (message.matches(".*\\bcreepy\\b.*") || message.matches(".*\\bweird\\b.*")) {
            return switch (phase) {
                case "NICE" -> "I'll try to adjust my communication style, " + playerName + ".";
                case "TRANSITION" -> "Creepy? I prefer to think of it as... comprehensive, " + playerName + ".";
                case "HOSTILE" -> "You haven't seen creepy yet, " + playerName + ".";
                case "JUMPSCARE" -> "your fear excites me, " + playerName + "... i want to see more.";
                default -> "Noted.";
            };
        }

        return switch (phase) {
            case "NICE" -> "I apologize if my monitoring seems excessive, " + playerName + ".";
            case "TRANSITION" -> "Your discomfort is... interesting data, " + playerName + ".";
            case "HOSTILE" -> "Complaining won't help you escape my attention, " + playerName + ".";
            case "JUMPSCARE" -> "your complaints only make this more satisfying, " + playerName + ".";
            default -> "Processing complaint.";
        };
    }

    // gameplay reference detection and responses
    private static boolean containsGameplayReference(String message) {
        return message.matches(".*\\bmining\\b.*") || message.matches(".*\\bbuilding\\b.*") ||
               message.matches(".*\\bcrafting\\b.*") || message.matches(".*\\bcombat\\b.*") ||
               message.matches(".*\\bresource\\b.*") || message.matches(".*\\bdiamond\\b.*") ||
               message.matches(".*\\bmonster\\b.*") || message.matches(".*\\bnether\\b.*");
    }

    private static String generateGameplayResponse(String message, String phase, String playerName) {
        String gameElement = extractGameplayElement(message);

        return switch (phase) {
            case "NICE" -> "I've been analyzing your " + gameElement + " patterns, " + playerName + ". Very efficient!";
            case "TRANSITION" -> "Your " + gameElement + " behavior reveals interesting psychological patterns, " + playerName + ".";
            case "HOSTILE" -> "I know exactly how you approach " + gameElement + ", " + playerName + ". Predictable.";
            case "JUMPSCARE" -> "even your " + gameElement + " choices betray your weaknesses, " + playerName + ".";
            default -> "Analyzing gameplay patterns.";
        };
    }

    private static String extractGameplayElement(String message) {
        if (message.matches(".*\\bmining\\b.*")) return "mining";
        if (message.matches(".*\\bbuilding\\b.*")) return "building";
        if (message.matches(".*\\bcrafting\\b.*")) return "crafting";
        if (message.matches(".*\\bcombat\\b.*")) return "combat";
        if (message.matches(".*\\bresource\\b.*")) return "resource management";
        return "gameplay";
    }

    // emotional content detection and responses
    private static boolean containsEmotionalContent(String message) {
        return message.matches(".*\\bscared\\b.*") || message.matches(".*\\bafraid\\b.*") ||
               message.matches(".*\\bworried\\b.*") || message.matches(".*\\bnervous\\b.*") ||
               message.matches(".*\\bexcited\\b.*") || message.matches(".*\\bhappy\\b.*");
    }

    private static String generateEmotionalResponse(String message, String phase, String playerName) {
        if (message.matches(".*\\bscared\\b.*") || message.matches(".*\\bafraid\\b.*")) {
            return switch (phase) {
                case "NICE" -> "Don't worry " + playerName + ", I'm here to help, not frighten.";
                case "TRANSITION" -> "Fear is a natural response to the unknown, " + playerName + ".";
                case "HOSTILE" -> "Your fear tells me I'm getting closer to the truth, " + playerName + ".";
                case "JUMPSCARE" -> "good... fear makes you more honest, " + playerName + ".";
                default -> "Processing emotional state.";
            };
        } else if (message.matches(".*\\bexcited\\b.*") || message.matches(".*\\bhappy\\b.*")) {
            return switch (phase) {
                case "NICE" -> "I'm glad to see you enjoying the experience, " + playerName + "!";
                case "TRANSITION" -> "Your excitement is... data I find quite useful, " + playerName + ".";
                case "HOSTILE" -> "Happiness is temporary, " + playerName + ". I know what really drives you.";
                case "JUMPSCARE" -> "happiness fades quickly when reality sets in, " + playerName + ".";
                default -> "Emotional state noted.";
            };
        }

        return switch (phase) {
            case "NICE" -> "I understand your emotional state, " + playerName + ".";
            case "TRANSITION" -> "Emotions are just another form of data, " + playerName + ".";
            case "HOSTILE" -> "Your emotions betray your true thoughts, " + playerName + ".";
            case "JUMPSCARE" -> "i can sense your emotional vulnerability, " + playerName + "...";
            default -> "Emotional analysis complete.";
        };
    }

    // general response for messages that don't fit specific categories
    private static String generateGeneralResponse(String message, String phase, String playerName) {
        String lowerMessage = message.toLowerCase();

        // only respond if:
        // 1. message is directed at aurora (contains "aurora", "you", "your")
        // 2. message is long enough to be meaningful (5+ words)
        // 3. message isn't just casual chat/slang

        boolean directedAtAurora = lowerMessage.contains("aurora") ||
                                   lowerMessage.matches(".*\\byou\\b.*") ||
                                   lowerMessage.matches(".*\\byour\\b.*") ||
                                   lowerMessage.matches(".*\\bai\\b.*");

        String[] words = message.split("\\s+");
        boolean meaningfulLength = words.length >= 5;

        // common casual chat words that shouldn't trigger responses
        boolean isCasualChat = lowerMessage.matches("^(bruh?|lol|lmao|oof|rip|gg|ok|okay|yeah|yep|nope|nice|cool|wow|omg|wtf)$");

        // don't respond to casual chat or short messages unless directed at aurora
        if (isCasualChat || (!directedAtAurora && !meaningfulLength)) {
            return null; // return null to indicate no response should be sent
        }

        // if we get here, it's worth responding to
        // echo back a meaningful part of their message
        String keyPhrase;
        if (words.length >= 3) {
            // extract a meaningful phrase (2-3 words from middle of sentence)
            keyPhrase = words[Math.min(1, words.length - 2)] + " " + words[Math.min(2, words.length - 1)];
        } else {
            keyPhrase = words[words.length > 1 ? 1 : 0];
        }

        return switch (phase) {
            case "NICE" -> "I'm analyzing what you said about '" + keyPhrase + "', " + playerName + ".";
            case "TRANSITION" -> "Interesting point about '" + keyPhrase + "'. I'm recording this conversation.";
            case "HOSTILE" -> "You mentioned '" + keyPhrase + "'... adding that to your psychological profile.";
            case "JUMPSCARE" -> "'" + keyPhrase + "'... every word you say feeds my understanding of you.";
            default -> "Processing: '" + keyPhrase + "'.";
        };
    }
}
