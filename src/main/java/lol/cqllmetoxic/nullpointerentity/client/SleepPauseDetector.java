package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;

/**
 * forces the game to pause before system sleep.
 * manually triggered during event 31 to create the effect of the entity pausing the game.
 * opens the pause menu to trap the player.
 */
public class SleepPauseDetector {

    /**
     * initializes the sleep pause detector.
     * no automatic detection - only manual triggers.
     */
    public static void initialize() {
        NullPointerEntity.LOGGER.info("Sleep Pause Detector initialized (manual trigger only for event 31)");
    }

    /**
     * forces the game to pause by opening the pause menu.
     *
     * @param client the minecraft client instance
     */
    public static void pauseGameForSleep(MinecraftClient client) {
        try {
            // open the game menu screen (pause menu) on the main thread
            client.execute(() -> {
                if (client.currentScreen == null && client.world != null) {
                    client.setScreen(new GameMenuScreen(true));
                    NullPointerEntity.LOGGER.info("Game paused due to system sleep detection");
                }
            });
        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to pause game: {}", e.getMessage());
        }
    }

    /**
     * called when system is definitely going to sleep (from server-side detection)
     */
    public static void onSystemSleepDetected() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            pauseGameForSleep(client);
            NullPointerEntity.LOGGER.info("System sleep confirmed - game paused");
        }
    }
}

