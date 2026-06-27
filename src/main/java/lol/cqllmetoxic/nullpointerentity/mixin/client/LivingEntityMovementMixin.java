package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.client.ClientPassiveEffects;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * intercepts entity movement to apply lag effects.
 * slows down player movement during certain passive events.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMovementMixin {

    /**
     * applies movement slowdown effect.
     */
    @Inject(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
    private void applyMovementLag(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // only apply to client players
        if (!(entity instanceof ClientPlayerEntity player)) return;

        if (ClientPassiveEffects.hasMovementLag()) {
            // reduce movement by modifying velocity
            Vec3d velocity = player.getVelocity();
            player.setVelocity(velocity.multiply(0.6, 1.0, 0.6)); // only reduce horizontal movement
        }
    }

    // gravity fluctuation - modify fall speed
    @Inject(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
    private void modifyGravity(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // only apply to client players
        if (!(entity instanceof ClientPlayerEntity player)) return;

        if (ClientPassiveEffects.hasGravityFluctuation()) {
            // reduce gravity effect (slower falling)
            Vec3d velocity = player.getVelocity();
            if (velocity.y < -0.1) { // only when falling with significant speed
                player.setVelocity(velocity.x, velocity.y * 0.75, velocity.z);
            }
        }
    }
}
