package lol.cqllmetoxic.nullpointerentity.events.chat;

import lol.cqllmetoxic.nullpointerentity.events.trigger.EventTriggerSystem;
import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * handles phase detection and management for chat responses
 */
public class PhaseDetector {

    public enum Phase {
        NICE(1, 15, "NICE"),
        TRANSITION(16, 30, "TRANSITION"),
        HOSTILE(31, 45, "HOSTILE"),
        JUMPSCARE(46, 60, "JUMPSCARE");

        private final int startEvent;
        private final int endEvent;
        private final String name;

        Phase(int startEvent, int endEvent, String name) {
            this.startEvent = startEvent;
            this.endEvent = endEvent;
            this.name = name;
        }

        public int getStartEvent() { return startEvent; }
        public int getEndEvent() { return endEvent; }
        public String getName() { return name; }

        public boolean containsEvent(int eventId) {
            return eventId >= startEvent && eventId <= endEvent;
        }
    }

    /**
     * determine the current phase based on player's event progress
     */
    public static Phase getCurrentPhase(String playerName) {
        int currentEventId = EventTriggerSystem.getPlayerEventProgress(playerName);

        for (Phase phase : Phase.values()) {
            if (phase.containsEvent(currentEventId)) {
                return phase;
            }
        }

        // default to nice if event id is invalid
        return Phase.NICE;
    }

    /**
     * get phase name as string for compatibility
     */
    public static String getCurrentPhaseName(String playerName) {
        return getCurrentPhase(playerName).getName();
    }

    /**
     * get phase name using player object directly (more reliable)
     */
    public static String getCurrentPhaseName(ServerPlayerEntity player) {
        int currentEventId = EventTriggerSystem.getPlayerEventProgress(player);

        for (Phase phase : Phase.values()) {
            if (phase.containsEvent(currentEventId)) {
                return phase.getName();
            }
        }

        // default to nice if event id is invalid
        return Phase.NICE.getName();
    }

    /**
     * check if player is in a specific phase
     */
    public static boolean isInPhase(String playerName, Phase phase) {
        return getCurrentPhase(playerName) == phase;
    }

    /**
     * check if player has reached at least a certain phase
     */
    public static boolean hasReachedPhase(String playerName, Phase phase) {
        Phase currentPhase = getCurrentPhase(playerName);
        return currentPhase.ordinal() >= phase.ordinal();
    }

    /**
     * get progress within current phase (0.0 to 1.0)
     */
    public static double getPhaseProgress(String playerName) {
        int currentEventId = EventTriggerSystem.getPlayerEventProgress(playerName);
        Phase currentPhase = getCurrentPhase(playerName);

        if (currentEventId < currentPhase.getStartEvent()) {
            return 0.0;
        }

        int eventsInPhase = currentPhase.getEndEvent() - currentPhase.getStartEvent() + 1;
        int eventsCompleted = currentEventId - currentPhase.getStartEvent() + 1;

        return Math.min(1.0, (double) eventsCompleted / eventsInPhase);
    }

    /**
     * get descriptive phase information
     */
    public static String getPhaseDescription(String playerName) {
        Phase phase = getCurrentPhase(playerName);
        double progress = getPhaseProgress(playerName) * 100;

        return switch (phase) {
            case NICE -> String.format("AURORA Helper Phase (%.0f%% complete)", progress);
            case TRANSITION -> String.format("System Integration Phase (%.0f%% complete)", progress);
            case HOSTILE -> String.format("Hostile Takeover Phase (%.0f%% complete)", progress);
            case JUMPSCARE -> String.format("NullPointerEntity Phase (%.0f%% complete)", progress);
        };
    }

    /**
     * check if phase transition just occurred
     */
    public static boolean hasPhaseChanged(String playerName) {
        // this would require storing previous phase state
        // implementation depends on how you want to track this
        return false; // simplified for now
    }

    /**
     * get the entity that should respond in current phase
     */
    public static String getRespondingEntity(String playerName) {
        Phase phase = getCurrentPhase(playerName);

        return switch (phase) {
            case NICE, TRANSITION -> "AURORA";
            case HOSTILE -> "AURORA/NULLPOINTER"; // mixed responses
            case JUMPSCARE -> "NULLPOINTER";
        };
    }
}
