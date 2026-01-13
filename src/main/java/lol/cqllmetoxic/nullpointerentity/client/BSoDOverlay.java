package lol.cqllmetoxic.nullpointerentity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

/**
 * renders a fake blue screen of death overlay that covers the entire game screen.
 * detects your OS and shows the appropriate BSOD style (windows blue screen, mac kernel panic, or linux kernel oops).
 * mutes all game audio while active to make it more realistic.
 * has built-in cooldown to prevent spam triggering.
 */
@Environment(EnvType.CLIENT)
public class BSoDOverlay {
    private static boolean isActive = false;
    private static long startTime = 0;
    /** how long the BSOD stays on screen, default 8 seconds */
    private static int duration = 8000;
    private static String currentOS = "";
    /** timestamp of last trigger, used to enforce cooldown */
    private static long lastTriggerTime = 0;
    /** minimum time between BSOD triggers to prevent spam */
    private static final long TRIGGER_COOLDOWN = 10000;

    /** stores the master volume level before muting so it can be restored after */
    private static float previousMasterVolume = -1.0f;
    /** tracks whether we actually stored volume settings successfully */
    private static boolean volumeStored = false;

    /** classic windows blue screen color */
    private static final int WINDOWS_BLUE = 0xFF0000AA;
    /** dark gray used for mac kernel panics */
    private static final int MAC_GRAY = 0xFF2D2D2D;
    /** dark red for linux kernel oops messages */
    private static final int LINUX_RED = 0xFF8B0000;

    /** path to custom windows BSOD texture. falls back to text if missing */
    private static final Identifier WINDOWS_BSOD_TEXTURE = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/bsod_windows.png");
    /** path to custom mac kernel panic texture */
    private static final Identifier MAC_BSOD_TEXTURE = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/bsod_mac.png");
    /** path to custom linux kernel oops texture */
    private static final Identifier LINUX_BSOD_TEXTURE = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/bsod_linux.png");

    /**
     * triggers the BSOD overlay for the specified duration.
     * won't activate if already showing or if cooldown hasn't expired yet.
     * automatically detects OS and mutes audio.
     *
     * @param durationMs how long to show the BSOD in milliseconds
     */
    public static void show(int durationMs) {
        long currentTime = System.currentTimeMillis();

        if (isActive) {
            NullPointerEntity.LOGGER.info("BSoD already active, ignoring new trigger");
            return;
        }

        if (currentTime - lastTriggerTime < TRIGGER_COOLDOWN) {
            NullPointerEntity.LOGGER.info("BSoD on cooldown, ignoring trigger ({}ms remaining)",
                TRIGGER_COOLDOWN - (currentTime - lastTriggerTime));
            return;
        }

        isActive = true;
        startTime = currentTime;
        lastTriggerTime = currentTime;
        duration = durationMs;

        // grab OS name to pick the right BSOD style
        currentOS = System.getProperty("os.name").toLowerCase();

        // save current volume and mute everything
        storeAndMuteVolume();

        NullPointerEntity.LOGGER.info("BSoD overlay activated for {} ms on {} (cooldown active for {}ms) - audio muted",
            durationMs, currentOS, TRIGGER_COOLDOWN);
    }

    /**
     * hides the BSOD overlay and restores audio.
     * safe to call multiple times - won't do anything if already hidden.
     */
    public static void hide() {
        if (!isActive) return;

        isActive = false;
        currentOS = "";

        // bring back the audio
        restoreVolume();

        NullPointerEntity.LOGGER.info("BSoD overlay deactivated - audio restored");
    }

    /**
     * checks if the BSOD is currently being displayed.
     * @return true if active, false otherwise
     */
    public static boolean isActive() {
        return isActive;
    }

    /**
     * called every frame to draw the BSOD overlay.
     * automatically hides itself when duration expires.
     * adds screen flicker effect for realism.
     *
     * @param context the drawing context for rendering
     * @param tickCounter game tick information (not used currently)
     */
    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!isActive) return;

        // time's up - hide the overlay
        if (System.currentTimeMillis() - startTime > duration) {
            hide();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // pick the right BSOD based on OS
        if (currentOS.contains("win")) {
            renderOSBSoD(context, screenWidth, screenHeight, WINDOWS_BSOD_TEXTURE, WINDOWS_BLUE);
        } else if (currentOS.contains("mac")) {
            renderOSBSoD(context, screenWidth, screenHeight, MAC_BSOD_TEXTURE, MAC_GRAY);
        } else {
            renderOSBSoD(context, screenWidth, screenHeight, LINUX_BSOD_TEXTURE, LINUX_RED);
        }

        // occasional white flash to simulate screen issues
        if (System.currentTimeMillis() % 150 < 15) {
            context.fill(0, 0, screenWidth, screenHeight, 0x20FFFFFF);
        }
    }

    /**
     * saves current master volume and sets it to 0 for the BSOD effect.
     * only touches master volume to keep things simple.
     * marks volumeStored as true if successful so we know we can restore it later.
     */
    private static void storeAndMuteVolume() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.options != null) {
                // grab current master volume
                previousMasterVolume = client.options.getSoundVolumeOption(net.minecraft.sound.SoundCategory.MASTER).getValue().floatValue();

                volumeStored = true;

                // set master to 0 for silence
                client.options.getSoundVolumeOption(net.minecraft.sound.SoundCategory.MASTER).setValue(0.0);

                NullPointerEntity.LOGGER.info("stored and muted all game volume for BSoD overlay");
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("failed to store and mute volume: {}", e.getMessage());
            volumeStored = false;
        }
    }

    /**
     * puts master volume back to what it was before the BSOD.
     * won't do anything if we never successfully stored the volume.
     */
    private static void restoreVolume() {
        try {
            if (!volumeStored) {
                NullPointerEntity.LOGGER.warn("no volume settings were stored, cannot restore");
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.options != null) {
                // put master volume back
                if (previousMasterVolume >= 0.0f) client.options.getSoundVolumeOption(net.minecraft.sound.SoundCategory.MASTER).setValue((double)previousMasterVolume);

                // clear the stored values
                resetVolumeStorage();

                NullPointerEntity.LOGGER.info("restored all game volume after BSoD overlay");
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("failed to restore volume: {}", e.getMessage());
        }
    }

    /**
     * resets volume tracking variables to their defaults.
     * called after successfully restoring volume.
     */
    private static void resetVolumeStorage() {
        previousMasterVolume = -1.0f;
        volumeStored = false;
    }

    /**
     * draws the BSOD texture for the detected OS.
     * fills background with OS-appropriate color first for proper contrast.
     *
     * @param context drawing context
     * @param width screen width
     * @param height screen height
     * @param texture the texture to draw
     * @param backgroundColor background color for this OS
     */
    private static void renderOSBSoD(DrawContext context, int width, int height, Identifier texture, int backgroundColor) {
        // paint the background first
        context.fill(0, 0, width, height, backgroundColor);

        // draw the BSOD texture
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, texture, 0, 0, 0.0f, 0.0f, width, height, width, height);
    }

    /**
     * alias method for backward compatibility with older code.
     * just calls show() internally.
     *
     * @param durationMs how long to display the BSOD
     */
    public static void showOSSpecific(int durationMs) {
        show(durationMs);
    }
}
