package lol.cqllmetoxic.nullpointerentity.config;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import java.util.Random;

/**
 * defines timing configuration for event progression.
 * controls how long between events and when the first event triggers.
 * all times are in minecraft ticks (20 ticks = 1 second).
 */
public class EventConfig {
    private static final Random random = new Random();

    public static final int FIRST_EVENT_MIN_TICKS = 6000;   // 5 minutes
    public static final int FIRST_EVENT_MAX_TICKS = 9600;   // 8 minutes

    public static final int NICE_EVENT_MIN_TICKS = 2400;    // 2 minutes
    public static final int NICE_EVENT_MAX_TICKS = 6000;    // 5 minutes

    public static final int BASE_TRANSITION_EVENT_TICKS = 4200;  // 3.5 minutes
    public static final int BASE_HOSTILE_EVENT_TICKS = 3000;     // 2.5 minutes

    // randomization ranges (±50% of base value for transition/hostile events)
    public static final float RANDOMIZATION_FACTOR = 0.5f;

    public static final boolean EVENTS_ENABLED = true;

    private static boolean currentEventsEnabled = EVENTS_ENABLED;

    public static boolean areEventsEnabled() {
        return currentEventsEnabled;
    }

    public static void setEventsEnabled(boolean enabled) {
        currentEventsEnabled = enabled;
    }

    // get randomized tick intervals

    public static long getRandomizedTransitionEventTicks() {
        // current runtime settings (can be changed via commands)
        int currentTransitionBaseTicks = BASE_TRANSITION_EVENT_TICKS;
        int variation = (int) (currentTransitionBaseTicks * RANDOMIZATION_FACTOR);
        return currentTransitionBaseTicks + random.nextInt(variation * 2) - variation;
    }

    public static long getRandomizedHostileEventTicks() {
        int currentHostileBaseTicks = BASE_HOSTILE_EVENT_TICKS;
        int variation = (int) (currentHostileBaseTicks * RANDOMIZATION_FACTOR);
        return currentHostileBaseTicks + random.nextInt(variation * 2) - variation;
    }

    // method for nice events with first event special timing
    public static long getRandomizedNiceEventTicks(int eventId) {
        if (eventId == 1) {
            int range = FIRST_EVENT_MAX_TICKS - FIRST_EVENT_MIN_TICKS;
            return FIRST_EVENT_MIN_TICKS + random.nextInt(range + 1);
        } else {
            // rest of nice events: 3-9 minutes
            int range = NICE_EVENT_MAX_TICKS - NICE_EVENT_MIN_TICKS;
            return NICE_EVENT_MIN_TICKS + random.nextInt(range + 1);
        }
    }

}

