package lol.cqllmetoxic.nullpointerentity.aurora;

import lol.cqllmetoxic.nullpointerentity.config.EventConfig;
import lol.cqllmetoxic.nullpointerentity.events.trigger.EventTriggerSystem;
import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

/**
 * initializes the aurora event progression system.
 * sets up tick-based event triggering for the chronological event sequence.
 * ensures events fire in order from 1-40.
 */
public class AuroraEventSystem {
    private static boolean isInitialized = false;

    /**
     * initializes the aurora event system.
     * only runs once per game session.
     */
    public static void initialize() {
        if (isInitialized) {
            NullPointerEntity.LOGGER.warn("AURORA Event System already initialized");
            return;
        }

        NullPointerEntity.LOGGER.info("Initializing AURORA Event System...");

        EventTriggerSystem.initialize();

        isInitialized = true;
        NullPointerEntity.LOGGER.info("AURORA Event System initialized successfully with chronological tick-based events");
    }

    /**
     * checks if the system has been initialized.
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void reset() {
        isInitialized = false;
        NullPointerEntity.LOGGER.info("AURORA Event System reset");
    }
}
