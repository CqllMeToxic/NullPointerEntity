package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.client.BSoDOverlay;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * intercepts HUD rendering to hide it when BSoD overlay is active.
 * prevents HUD from rendering over the fake blue screen.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    /**
     * hides HUD rendering when BSoD is showing.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hideBSoDWhenActive(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // cancel normal hud rendering when bsod is active (but after we render bsod)
        if (BSoDOverlay.isActive()) {
            // render the bsod overlay first, then cancel hud rendering
            BSoDOverlay.render(context, tickCounter);
            ci.cancel();
        }
    }
}
