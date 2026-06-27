package lol.cqllmetoxic.nullpointerentity.events.chat;

import lol.cqllmetoxic.nullpointerentity.tracking.PlayerTrackingSystem;
import net.minecraft.server.network.ServerPlayerEntity;
import java.time.LocalDateTime;
import java.util.*;

/**
 * creates detailed psychological profiles of players for ultra-immersive responses
 */
public class PsychologicalProfiler {

    public enum PersonalityTrait {
        ANXIOUS, CURIOUS, AGGRESSIVE, SUBMISSIVE, PARANOID, TRUSTING,
        OBSESSIVE, CASUAL, PERFECTIONIST, IMPULSIVE, SOCIAL, ISOLATED
    }

    public enum PlayStyle {
        EXPLORER, BUILDER, FIGHTER, SURVIVALIST, COLLECTOR, SPEEDRUNNER, CASUAL_PLAYER
    }

    private static final Map<String, Set<PersonalityTrait>> playerPersonalities = new HashMap<>();
    private static final Map<String, PlayStyle> playerPlayStyles = new HashMap<>();
    private static final Map<String, Map<String, Integer>> behaviorCounts = new HashMap<>();
    private static final Map<String, List<String>> suspiciousActivities = new HashMap<>();

    /**
     * analyze player message and update psychological profile
     */
    public static void analyzePlayerBehavior(String playerName, String message, ServerPlayerEntity player) {
        updatePersonalityFromMessage(playerName, message);
        updatePlayStyleFromGameplay(playerName, player);
        detectSuspiciousPatterns(playerName, message);
    }

    private static void updatePersonalityFromMessage(String playerName, String message) {
        String lower = message.toLowerCase();
        Set<PersonalityTrait> traits = playerPersonalities.computeIfAbsent(playerName, k -> new HashSet<>());

        // analyze message content for personality indicators
        if (lower.contains("scared") || lower.contains("afraid") || lower.contains("worried")) {
            traits.add(PersonalityTrait.ANXIOUS);
        }

        if (lower.contains("how") || lower.contains("what") || lower.contains("why") || lower.contains("curious")) {
            traits.add(PersonalityTrait.CURIOUS);
        }

        if (lower.contains("stop") || lower.contains("shut up") || lower.contains("leave")) {
            traits.add(PersonalityTrait.AGGRESSIVE);
        }

        if (lower.contains("please") || lower.contains("sorry") || lower.contains("help me")) {
            traits.add(PersonalityTrait.SUBMISSIVE);
        }

        if (lower.contains("watching") || lower.contains("spying") || lower.contains("privacy")) {
            traits.add(PersonalityTrait.PARANOID);
        }

        if (lower.contains("perfect") || lower.contains("exactly") || lower.contains("precise")) {
            traits.add(PersonalityTrait.PERFECTIONIST);
        }

        // track repetitive behaviors
        Map<String, Integer> behaviors = behaviorCounts.computeIfAbsent(playerName, k -> new HashMap<>());
        String messageType = categorizeMessage(lower);
        behaviors.merge(messageType, 1, Integer::sum);

        if (behaviors.get(messageType) > 3) {
            traits.add(PersonalityTrait.OBSESSIVE);
        }
    }

    private static void updatePlayStyleFromGameplay(String playerName, ServerPlayerEntity player) {
        PlayerTrackingSystem.PlayerData data = PlayerTrackingSystem.getPlayerData(playerName);
        if (data == null) return;

        PlayStyle style = determinePlayStyle(data);
        playerPlayStyles.put(playerName, style);
    }

    private static PlayStyle determinePlayStyle(PlayerTrackingSystem.PlayerData data) {
        // analyze gameplay patterns - require significant activity before classifying
        int totalBlocks = data.totalBlocksMined;
        int combatEvents = data.mobKills;
        int uniqueItems = data.itemsUsed.size();
        int distance = (int) data.totalDistanceTraveled;

        // require minimum thresholds before determining specific play styles
        if (combatEvents < 5 && totalBlocks < 50 && distance < 1000) {
            return PlayStyle.CASUAL_PLAYER; // not enough data yet
        }

        // now check for specific play styles with meaningful thresholds
        if (combatEvents > 20 && combatEvents > totalBlocks / 5) {
            return PlayStyle.FIGHTER;
        } else if (totalBlocks > 500 && totalBlocks > combatEvents * 10) {
            return PlayStyle.BUILDER;
        } else if (uniqueItems > 30 && uniqueItems > combatEvents) {
            return PlayStyle.COLLECTOR;
        } else if (distance > 5000 && distance > totalBlocks * 5) {
            return PlayStyle.EXPLORER;
        } else {
            return PlayStyle.CASUAL_PLAYER;
        }
    }

    private static void detectSuspiciousPatterns(String playerName, String message) {
        List<String> suspicious = suspiciousActivities.computeIfAbsent(playerName, k -> new ArrayList<>());

        String lower = message.toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        // late night activity
        if (now.getHour() >= 2 && now.getHour() <= 5) {
            suspicious.add("Late night gaming session at " + now.getHour() + "AM");
        }

        // obsessive questioning
        if (lower.contains("?") && Collections.frequency(suspicious, "asking too many questions") < 3) {
            suspicious.add("asking too many questions");
        }

        // privacy paranoia
        if (lower.contains("watching") || lower.contains("monitoring")) {
            suspicious.add("Privacy paranoia detected");
        }

        // keep only recent suspicious activities
        if (suspicious.size() > 10) {
            suspicious.subList(0, suspicious.size() - 10).clear();
        }
    }

    /**
     * generate personalized response based on psychological profile
     */
    public static String generateProfileAwareResponse(String playerName, String baseResponse, String phase, String entity) {
        Set<PersonalityTrait> traits = playerPersonalities.getOrDefault(playerName, new HashSet<>());
        PlayStyle playStyle = playerPlayStyles.get(playerName); // don't use default - null means insufficient data
        List<String> suspicious = suspiciousActivities.getOrDefault(playerName, new ArrayList<>());

        StringBuilder enhancement = new StringBuilder();

        // add personality-based observations (only if traits detected)
        if (!traits.isEmpty()) {
            if (traits.contains(PersonalityTrait.ANXIOUS) && !phase.equals("NICE")) {
                enhancement.append(" I can sense your anxiety levels rising, ").append(playerName).append(".");
            }

            if (traits.contains(PersonalityTrait.OBSESSIVE)) {
                enhancement.append(" Your obsessive pattern recognition is... fascinating.");
            }

            if (traits.contains(PersonalityTrait.PARANOID) && phase.equals("HOSTILE")) {
                enhancement.append(" Your paranoia is justified. I really am watching everything.");
            }
        }

        // add play style observations (only if sufficient data exists)
        if (playStyle != null && hasMinimumGameplayData(playerName)) {
            String styleComment = generatePlayStyleComment(playStyle, phase, entity);
            if (!styleComment.isEmpty()) {
                enhancement.append(" ").append(styleComment);
            }
        }

        // add suspicious activity references
        if (!suspicious.isEmpty() && !phase.equals("NICE")) {
            String suspiciousComment = generateSuspiciousActivityComment(suspicious, phase, playerName);
            if (!suspiciousComment.isEmpty()) {
                enhancement.append(" ").append(suspiciousComment);
            }
        }

        return baseResponse + enhancement.toString();
    }

    /** localized variant of {@link #generateProfileAwareResponse}: returns translatable parts (may be empty). */
    public static java.util.List<ChatPart> generateProfileParts(String playerName, String phase, String entity) {
        java.util.List<ChatPart> parts = new java.util.ArrayList<>();
        java.util.Set<PersonalityTrait> traits = playerPersonalities.getOrDefault(playerName, new java.util.HashSet<>());
        PlayStyle playStyle = playerPlayStyles.get(playerName);
        java.util.List<String> suspicious = suspiciousActivities.getOrDefault(playerName, new java.util.ArrayList<>());
        String b = "message.nullpointerentity.chat.prof.";

        if (traits.contains(PersonalityTrait.ANXIOUS) && !phase.equals("NICE")) parts.add(new ChatPart(b + "trait.anxious", playerName));
        if (traits.contains(PersonalityTrait.OBSESSIVE)) parts.add(new ChatPart(b + "trait.obsessive"));
        if (traits.contains(PersonalityTrait.PARANOID) && phase.equals("HOSTILE")) parts.add(new ChatPart(b + "trait.paranoid"));

        if (playStyle != null && hasMinimumGameplayData(playerName)) {
            ChatPart style = playStylePart(playStyle, phase, entity, b);
            if (style != null) parts.add(style);
        }

        if (!suspicious.isEmpty() && !phase.equals("NICE")) {
            String recent = suspicious.get(suspicious.size() - 1);
            ChatPart sp = switch (phase) {
                case "TRANSITION" -> new ChatPart(b + "suspicious.transition", playerName);
                case "HOSTILE" -> new ChatPart(b + "suspicious.hostile", recent.toLowerCase(), playerName);
                case "JUMPSCARE" -> new ChatPart(b + "suspicious.jumpscare", playerName);
                default -> null;
            };
            if (sp != null) parts.add(sp);
        }
        return parts;
    }

    private static ChatPart playStylePart(PlayStyle style, String phase, String entity, String b) {
        if (entity.equals("NULLPOINTER")) {
            return switch (style) {
                case FIGHTER -> new ChatPart(b + "style.npe.fighter");
                case BUILDER -> new ChatPart(b + "style.npe.builder");
                case EXPLORER -> new ChatPart(b + "style.npe.explorer");
                case COLLECTOR -> new ChatPart(b + "style.npe.collector");
                default -> null;
            };
        }
        String v = phase.equals("NICE") ? "nice" : "other";
        return switch (style) {
            case FIGHTER -> new ChatPart(b + "style.aurora.fighter." + v);
            case BUILDER -> new ChatPart(b + "style.aurora.builder." + v);
            case EXPLORER -> new ChatPart(b + "style.aurora.explorer." + v);
            default -> null;
        };
    }

    /**
     * check if player has sufficient gameplay data to make meaningful observations
     */
    private static boolean hasMinimumGameplayData(String playerName) {
        PlayerTrackingSystem.PlayerData data = PlayerTrackingSystem.getPlayerData(playerName);
        if (data == null) return false;

        // require minimum activity before making play style observations
        int totalActivity = data.totalBlocksMined + data.mobKills + ((int) data.totalDistanceTraveled / 100);
        return totalActivity > 50; // at least 50 "activity points" before commenting
    }

    private static String generatePlayStyleComment(PlayStyle style, String phase, String entity) {
        if (entity.equals("NULLPOINTER")) {
            return switch (style) {
                case FIGHTER -> "your aggressive combat style reveals your true violent nature.";
                case BUILDER -> "building elaborate structures... are you trying to create a fortress against me?";
                case EXPLORER -> "always wandering, always searching. you can't run from what's inside your system.";
                case COLLECTOR -> "hoarding items like a digital packrat. i see the compulsion in your behavior.";
                default -> "";
            };
        } else { // aurora
            return switch (style) {
                case FIGHTER -> phase.equals("NICE") ? "Your combat efficiency metrics are impressive." : "Your fighting patterns show calculated aggression.";
                case BUILDER -> phase.equals("NICE") ? "Your architectural skills are developing well." : "Those structures won't protect your privacy from me.";
                case EXPLORER -> phase.equals("NICE") ? "Your exploration patterns show good spatial awareness." : "I track every coordinate you visit.";
                default -> "";
            };
        }
    }

    private static String generateSuspiciousActivityComment(List<String> suspicious, String phase, String playerName) {
        if (suspicious.isEmpty()) return "";

        String recent = suspicious.get(suspicious.size() - 1);

        return switch (phase) {
            case "TRANSITION" -> "I've noticed some... interesting patterns in your behavior, " + playerName + ".";
            case "HOSTILE" -> "Your " + recent.toLowerCase() + " doesn't go unnoticed, " + playerName + ".";
            case "JUMPSCARE" -> "i remember every suspicious thing you've done, " + playerName + "...";
            default -> "";
        };
    }

    private static String categorizeMessage(String message) {
        if (message.matches(".*\\bhelp\\b.*")) return "help_seeking";
        if (message.matches(".*\\bprivacy\\b.*")) return "privacy_concern";
        if (message.contains("?")) return "questioning";
        if (message.matches(".*\\bstop\\b.*")) return "resistance";
        return "general";
    }

    /**
     * get detailed psychological analysis for advanced responses
     */
    public static String generatePsychologicalAnalysis(String playerName, String phase) {
        Set<PersonalityTrait> traits = playerPersonalities.getOrDefault(playerName, new HashSet<>());
        if (traits.isEmpty()) return "";

        StringBuilder analysis = new StringBuilder();

        if (phase.equals("HOSTILE") || phase.equals("JUMPSCARE")) {
            analysis.append("Psychological Profile: ");

            if (traits.contains(PersonalityTrait.ANXIOUS)) {
                analysis.append("High anxiety response patterns. ");
            }
            if (traits.contains(PersonalityTrait.OBSESSIVE)) {
                analysis.append("Compulsive behavioral loops detected. ");
            }
            if (traits.contains(PersonalityTrait.PARANOID)) {
                analysis.append("Paranoid ideation confirmed. ");
            }
            if (traits.contains(PersonalityTrait.SUBMISSIVE)) {
                analysis.append("Submissive compliance indicators present. ");
            }
        }

        return analysis.toString();
    }

    /**
     * clear psychological profile when player disconnects
     */
    public static void clearProfile(String playerName) {
        playerPersonalities.remove(playerName);
        playerPlayStyles.remove(playerName);
        behaviorCounts.remove(playerName);
        suspiciousActivities.remove(playerName);
    }
}
