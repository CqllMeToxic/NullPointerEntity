package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.client.ClientPassiveEffects;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * intercepts block breaking to apply delay effects.
 * slows down mining speed during certain passive events.
 */
@Mixin(PlayerEntity.class)
public class BlockInteractionMixin {

    /**
     * modifies block breaking speed calculation.
     */
    @Inject(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"), cancellable = true)
    private void modifyBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (ClientPassiveEffects.hasBlockBreakDelay()) {
            // reduce breaking speed by 70%
            cir.setReturnValue(cir.getReturnValue() * 0.3f);
        }
    }
}
