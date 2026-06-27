package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * the fake bsod. it's a full-screen {@link Screen} (not a hud overlay) so it draws on top of everything -
 * hud, chat, and other mods' hud overlays like keystroke displays. closes itself after the duration and
 * mutes the PC's whole system audio (not just the game) while it's up, restoring it on close.
 * {@link #show} is the entry point and also handles the spam cooldown.
 */
@Environment(EnvType.CLIENT)
public class BSoDScreen extends Screen {
    private static final int WINDOWS_BLUE = 0xFF0000AA;
    private static final int MAC_GRAY = 0xFF2D2D2D;
    private static final int LINUX_RED = 0xFF8B0000;
    private static final Identifier WINDOWS_BSOD_TEXTURE = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/bsod_windows.png");
    private static final Identifier MAC_BSOD_TEXTURE = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/bsod_mac.png");
    private static final Identifier LINUX_BSOD_TEXTURE = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/bsod_linux.png");

    private final int durationMs;
    private final long startTime;
    private final String os;
    private boolean systemAudioMuted = false;

    // ---- trigger + cooldown (static entry points) -------------------------------------------------
    private static long lastTriggerTime = 0;
    private static final long TRIGGER_COOLDOWN = 10000;

    /**
     * shows the bsod for the given duration. does nothing if one's already up or we're still on cooldown.
     *
     * @param durationMs how long to show it, in ms
     */
    public static void show(int durationMs) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (isActive()) {
            NullPointerEntity.LOGGER.info("BSoD already active, ignoring new trigger");
            return;
        }

        if (currentTime - lastTriggerTime < TRIGGER_COOLDOWN) {
            NullPointerEntity.LOGGER.info("BSoD on cooldown, ignoring trigger ({}ms remaining)",
                TRIGGER_COOLDOWN - (currentTime - lastTriggerTime));
            return;
        }

        lastTriggerTime = currentTime;
        client.execute(() -> client.setScreen(new BSoDScreen(durationMs)));
        NullPointerEntity.LOGGER.info("BSoD screen activated for {} ms", durationMs);
    }

    /** true while the fake bsod is on screen. */
    public static boolean isActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.currentScreen instanceof BSoDScreen;
    }

    /** closes the bsod early (the screen's close puts the volume back). */
    public static void hide() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen instanceof BSoDScreen screen) {
            screen.close();
        }
    }

    public BSoDScreen(int durationMs) {
        super(Text.literal(""));
        this.durationMs = durationMs;
        this.startTime = System.currentTimeMillis();
        this.os = System.getProperty("os.name").toLowerCase();
    }

    @Override
    protected void init() {
        // mute the PC's whole system audio (not just the game) for the duration of the fake crash.
        if (!systemAudioMuted) {
            lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.muteSystemAudio();
            systemAudioMuted = true;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width;
        int h = this.height;

        Identifier texture;
        int background;
        if (os.contains("win")) {
            texture = WINDOWS_BSOD_TEXTURE; background = WINDOWS_BLUE;
        } else if (os.contains("mac")) {
            texture = MAC_BSOD_TEXTURE; background = MAC_GRAY;
        } else {
            texture = LINUX_BSOD_TEXTURE; background = LINUX_RED;
        }

        context.fill(0, 0, w, h, background);
        context.drawTexture(RenderLayer::getGuiTextured, texture, 0, 0, 0.0f, 0.0f, w, h, w, h);

        if (System.currentTimeMillis() % 150 < 15) {
            context.fill(0, 0, w, h, 0x20FFFFFF);
        }
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - startTime > durationMs) {
            close();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // no closing dat shit!
    }

    @Override
    public void close() {
        // put the PC's audio back when the fake crash ends
        if (systemAudioMuted) {
            lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.restoreSystemAudio();
            systemAudioMuted = false;
        }
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }
}
