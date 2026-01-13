package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.events.PassiveEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * intercepts game rendering to apply visual effects.
 * handles camera shake and screen tint overlays for passive events.
 */
@Mixin(GameRenderer.class)
public class RenderMixin {

    /**
     * injects camera shake effect before world rendering.
     */
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void applyCameraShake(RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && PassiveEvents.hasCameraShake(client.player.getUuid())) {
            Camera camera = client.gameRenderer.getCamera();
            // apply random shake to camera position
            float shakeIntensity = 0.1f;
            float randomX = (float) (Math.random() - 0.5) * shakeIntensity;
            float randomY = (float) (Math.random() - 0.5) * shakeIntensity;
            float randomZ = (float) (Math.random() - 0.5) * shakeIntensity;

            // this would require access to camera internals - simplified example
            // camera.setpos(camera.getpos().add(randomx, randomy, randomz));
        }
    }

    // screen tint - apply color overlay
    @Inject(method = "render", at = @At("TAIL"))
    private void applyScreenTint(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            int redTint = PassiveEvents.getScreenTintRed(client.player.getUuid());
            int greenTint = PassiveEvents.getScreenTintGreen(client.player.getUuid());
            int blueTint = PassiveEvents.getScreenTintBlue(client.player.getUuid());

            if (redTint > 0 || greenTint > 0 || blueTint > 0) {
                // apply screen overlay with tint color
                DrawContext context = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
                int color = (redTint << 16) | (greenTint << 8) | blueTint | (30 << 24); // alpha = 30
                context.fill(0, 0, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), color);
            }
        }
    }
}
