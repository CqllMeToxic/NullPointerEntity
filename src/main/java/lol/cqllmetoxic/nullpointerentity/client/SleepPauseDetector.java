package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

/**
 * sleep-pause startup hook. the actual pause happens per-player, on each client, right before the
 * system suspend (see {@code ClientEventExecutor#runJ58}); this class only keeps the init log.
 */
public class SleepPauseDetector {

    /**
     * initializes the sleep pause detector.
     * the pause itself is triggered per-player from the sleep event, not here.
     */
    public static void initialize() {
        NullPointerEntity.LOGGER.info("Sleep Pause Detector initialized");
    }
}
