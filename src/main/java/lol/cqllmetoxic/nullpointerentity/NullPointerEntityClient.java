package lol.cqllmetoxic.nullpointerentity;

import lol.cqllmetoxic.nullpointerentity.entity.ModEntities;
import lol.cqllmetoxic.nullpointerentity.entity.client.FakePlayerEntityRenderer;
import lol.cqllmetoxic.nullpointerentity.client.JumpscareFlash;
import lol.cqllmetoxic.nullpointerentity.client.FakeDeathScreen;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyEventHandler;
import lol.cqllmetoxic.nullpointerentity.client.ClientWakeDetection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * client-side initialization for the mod.
 * handles rendering, UI screens, and client-only features.
 * runs separately from server-side code. (server side being your world)
 */
public class NullPointerEntityClient implements ClientModInitializer {
    private static boolean shouldShowPrivacyScreen = false;
    private static boolean shouldTriggerJumpscareFlash = false;
    private static boolean shouldShowFakeDeathScreen = false;
    private static long fakeDeathDuration = 5000;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.FAKE_PLAYER_ENTITY, FakePlayerEntityRenderer::new);

        PrivacyEventHandler.initialize();
        JumpscareFlash.initialize();
        ClientWakeDetection.initialize();
        lol.cqllmetoxic.nullpointerentity.client.SleepPauseDetector.initialize();
        lol.cqllmetoxic.nullpointerentity.client.RedRainRenderer.initialize();

        // tick handler for client-side screen triggers
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) return;

            if (shouldShowPrivacyScreen) {
                shouldShowPrivacyScreen = false;
                client.execute(() -> {
                    client.setScreen(new lol.cqllmetoxic.nullpointerentity.privacy.PrivacyScreen(client.currentScreen));
                });
            }

            if (shouldTriggerJumpscareFlash) {
                shouldTriggerJumpscareFlash = false;
                JumpscareFlash.triggerJumpscareFlash();
            }

            if (shouldShowFakeDeathScreen) {
                shouldShowFakeDeathScreen = false;
                client.execute(() -> {
                    client.setScreen(new FakeDeathScreen(fakeDeathDuration));
                });
            }
        });
    }

    /**
     * queues the privacy screen to show on the next client tick.
     * thread-safe - can be called from server-side code.
     */
    public static void triggerPrivacyScreenTest() {
        shouldShowPrivacyScreen = true;
    }

    /**
     * queues a jumpscare flash effect for the next tick.
     */
    public static void triggerJumpscareFlash() {
        shouldTriggerJumpscareFlash = true;
    }

    /**
     * queues the fake death screen to display.
     *
     * @param duration how long to show the fake death screen (milliseconds)
     */
    public static void triggerFakeDeathScreen(long duration) {
        fakeDeathDuration = duration;
        shouldShowFakeDeathScreen = true;
    }
}
