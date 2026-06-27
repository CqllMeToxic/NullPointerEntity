package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.network.payload.PassiveEffectsPayload;

/**
 * client-side store of the LOCAL player's active passive "loss of control" effects, synced from the
 * server via {@link PassiveEffectsPayload}. the client input/render mixins read from here so each
 * effect applies on the player's own machine - not just the host's, which is all the old read of the
 * server-side {@code PassiveEvents} static state could reach in multiplayer.
 */
public final class ClientPassiveEffects {
    private static volatile boolean inputInversion;
    private static volatile boolean movementLag;
    private static volatile boolean gravityFluctuation;
    private static volatile boolean blockBreakDelay;
    private static volatile boolean cameraShake;
    private static volatile float mouseSensitivityMultiplier = 1.0f;
    private static volatile int screenTintRed;
    private static volatile int screenTintGreen;
    private static volatile int screenTintBlue;

    private ClientPassiveEffects() {
    }

    /** applies a freshly received snapshot of this player's active passive effects. */
    public static void apply(PassiveEffectsPayload payload) {
        int f = payload.flags();
        inputInversion = (f & PassiveEffectsPayload.INPUT_INVERSION) != 0;
        movementLag = (f & PassiveEffectsPayload.MOVEMENT_LAG) != 0;
        gravityFluctuation = (f & PassiveEffectsPayload.GRAVITY_FLUCTUATION) != 0;
        blockBreakDelay = (f & PassiveEffectsPayload.BLOCK_BREAK_DELAY) != 0;
        cameraShake = (f & PassiveEffectsPayload.CAMERA_SHAKE) != 0;
        mouseSensitivityMultiplier = payload.mouseSensitivityMultiplier();
        int tint = payload.tintPacked();
        screenTintRed = (tint >> 16) & 0xFF;
        screenTintGreen = (tint >> 8) & 0xFF;
        screenTintBlue = tint & 0xFF;
    }

    /** clears all effects (on disconnect, so stale effects don't carry into the next session). */
    public static void clear() {
        inputInversion = false;
        movementLag = false;
        gravityFluctuation = false;
        blockBreakDelay = false;
        cameraShake = false;
        mouseSensitivityMultiplier = 1.0f;
        screenTintRed = 0;
        screenTintGreen = 0;
        screenTintBlue = 0;
    }

    public static boolean hasInputInversion() {
        return inputInversion;
    }

    public static boolean hasMovementLag() {
        return movementLag;
    }

    public static boolean hasGravityFluctuation() {
        return gravityFluctuation;
    }

    public static boolean hasBlockBreakDelay() {
        return blockBreakDelay;
    }

    public static boolean hasCameraShake() {
        return cameraShake;
    }

    public static float getMouseSensitivityMultiplier() {
        return mouseSensitivityMultiplier;
    }

    public static int getScreenTintRed() {
        return screenTintRed;
    }

    public static int getScreenTintGreen() {
        return screenTintGreen;
    }

    public static int getScreenTintBlue() {
        return screenTintBlue;
    }
}
