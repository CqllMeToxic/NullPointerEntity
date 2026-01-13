package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.events.PassiveEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * intercepts player movement to apply passive effects.
 * can invert movement controls during certain events.
 */
@Mixin(ClientPlayerEntity.class)
public class MovementMixin {

    /**
     * modifies movement input during player tick.
     */
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void modifyMovementInput(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (PassiveEvents.hasInputInversion(player.getUuid())) {
            // apply input inversion by modifying velocity directly
            Vec3d velocity = player.getVelocity();
            // only invert horizontal movement, reduce intensity to avoid jitter
            if (Math.abs(velocity.x) > 0.01 || Math.abs(velocity.z) > 0.01) {
                player.setVelocity(-velocity.x * 0.8, velocity.y, -velocity.z * 0.8);
            }
        }
    }

    // movement lag & gravity effects - apply during tick
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void applyMovementEffects(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        Vec3d velocity = player.getVelocity();
        boolean modified = false;

        // apply movement lag
        if (PassiveEvents.hasMovementLag(player.getUuid())) {
            velocity = velocity.multiply(0.6, 1.0, 0.6); // reduce horizontal movement
            modified = true;
        }

        // apply gravity fluctuation
        if (PassiveEvents.hasGravityFluctuation(player.getUuid())) {
            if (velocity.y < -0.1) { // only when falling with significant speed
                velocity = new Vec3d(velocity.x, velocity.y * 0.75, velocity.z);
                modified = true;
            }
        }

        // apply the modified velocity if any changes were made
        if (modified) {
            player.setVelocity(velocity);
        }
    }
}
