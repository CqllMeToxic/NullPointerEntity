package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.config.VoiceChatConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * two little client-side things for shriek's push-to-talk:
 * <ul>
 *   <li>a "listening" marker on screen while shriek is actually recording the mic
 *       ({@code ShriekClient.recordingSpeech}) in ptt mode, so you can tell when aurora hears you.</li>
 *   <li>a {@value #TAIL_MS}ms tail after you let go of the key, so the end of your sentence doesn't get
 *       chopped off before vosk transcribes it.</li>
 * </ul>
 * shriek recomputes {@code recordingSpeech = vKeyMapping.isPressed()} every tick (architectury CLIENT_POST),
 * so for the tail we just keep the keybind "pressed" from {@link ClientTickEvents#START_CLIENT_TICK} (runs
 * earlier in the tick) and shriek's later read still sees it held. real key state comes from glfw so we
 * never hide an actual press. no-ops when shriek isn't installed.
 */

// i just learned you can bullet javadocs that shits cool as hell!!!


@Environment(EnvType.CLIENT)
public final class VoicePushToTalk {
    private static final long TAIL_MS = 3000L;

    private static boolean shriekLoaded = false;
    private static boolean wasHeld = false;
    private static long tailUntilMs = 0L;

    private VoicePushToTalk() {
    }

    public static void initialize() {
        shriekLoaded = FabricLoader.getInstance().isModLoaded("shriek");
        ClientTickEvents.START_CLIENT_TICK.register(VoicePushToTalk::onClientTick);
        HudRenderCallback.EVENT.register(VoicePushToTalk::renderIndicator);
    }

    /** keeps shriek's voice key "pressed" for {@link #TAIL_MS} after you actually let go. */
    private static void onClientTick(MinecraftClient client) {
        if (!shriekLoaded || client == null || client.getWindow() == null) {
            return;
        }
        KeyBinding key;
        try {
            key = com.pryzmm.ShriekClient.vKeyMapping;
        } catch (Throwable t) {
            return;
        }
        if (key == null) {
            return;
        }

        boolean enabled = VoiceChatConfig.isVoiceChatEnabled() && VoiceChatConfig.isPushToTalkEnabled();
        if (!enabled) {
            // don't leave the key stuck pressed if a tail was mid-flight
            if (tailUntilMs != 0L) {
                key.setPressed(false);
                tailUntilMs = 0L;
            }
            wasHeld = false;
            return;
        }

        boolean held = isPhysicallyHeld(client, key);
        long now = System.currentTimeMillis();

        if (held) {
            tailUntilMs = 0L;
        } else if (wasHeld) {
            // just let go -> keep hearing for a few more seconds to catch the tail of the sentence
            tailUntilMs = now + TAIL_MS;
        }
        wasHeld = held;

        if (!held && tailUntilMs != 0L) {
            if (now < tailUntilMs) {
                key.setPressed(true);
            } else {
                key.setPressed(false); // tail's up -> let shriek finalize the transcript
                tailUntilMs = 0L;
            }
        }
    }

    /** true while you're actually holding the bound voice key down (asks glfw, not the keybind field). */
    private static boolean isPhysicallyHeld(MinecraftClient client, KeyBinding key) {
        try {
            InputUtil.Key bound = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey());
            if (bound == null || bound.equals(InputUtil.UNKNOWN_KEY)) {
                return false;
            }
            long handle = client.getWindow().getHandle();
            if (bound.getCategory() == InputUtil.Type.MOUSE) {
                return GLFW.glfwGetMouseButton(handle, bound.getCode()) == GLFW.GLFW_PRESS;
            }
            return InputUtil.isKeyPressed(handle, bound.getCode());
        } catch (Throwable t) {
            return false;
        }
    }

    /** draws the "aurora is listening" marker while shriek is recording the mic in ptt mode. */
    private static void renderIndicator(DrawContext context, RenderTickCounter tickCounter) {
        if (!shriekLoaded
            || !VoiceChatConfig.isVoiceChatEnabled()
            || !VoiceChatConfig.isPushToTalkEnabled()) {
            return;
        }
        boolean recording;
        try {
            recording = com.pryzmm.ShriekClient.recordingSpeech;
        } catch (Throwable t) {
            return;
        }
        if (!recording) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) {
            return;
        }

        Text label = Text.translatable("hud.nullpointerentity.voice.listening");
        int textWidth = client.textRenderer.getWidth(label);
        int fontHeight = client.textRenderer.fontHeight;
        int dotSize = 4;
        int gap = 5;
        int contentWidth = dotSize + gap + textWidth;
        int startX = (context.getScaledWindowWidth() - contentWidth) / 2;
        int y = 6;

        context.fill(startX - 5, y - 3, startX + contentWidth + 5, y + fontHeight + 2, 0x90000000);

        // pulsing red recording dot
        double pulse = (Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 2.0) + 1.0) / 2.0;
        int alpha = 130 + (int) (pulse * 125);
        int dotColor = (alpha << 24) | 0xFF3636;
        int dotY = y + (fontHeight - dotSize) / 2;
        context.fill(startX, dotY, startX + dotSize, dotY + dotSize, dotColor);

        context.drawTextWithShadow(client.textRenderer, label, startX + dotSize + gap, y, 0xFFFF5555);
    }
}
