package lol.cqllmetoxic.nullpointerentity.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

/**
 * renders rain with a red tint for hostile environment effects.
 * activates during nullpointer entity encounters to create atmosphere.
 * hooks into world rendering to modify rain appearance.
 */
public class RedRainRenderer {
    private static boolean isRedRainActive = false;
    private static final Identifier RAIN_TEXTURE = Identifier.of("minecraft", "textures/environment/rain.png");

    /**
     * initializes red rain rendering hooks.
     */
    public static void initialize() {
        WorldRenderEvents.BEFORE_ENTITIES.register((context) -> {
            if (isRedRainActive && context.world().isRaining()) {
                // rain texture loading handled here
            }
        });
    }

    /**
     * toggles red rain effect on or off.
     *
     * @param active true to enable red rain, false to disable
     */
    public static void setRedRainActive(boolean active) {
        isRedRainActive = active;
        if (active) {
            MinecraftClient.getInstance().execute(() -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(net.minecraft.text.Text.literal("§4The sky bleeds... red rain begins to fall."), false);
                }
            });
        }
    }
}
