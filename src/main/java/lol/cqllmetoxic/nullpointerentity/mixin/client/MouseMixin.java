package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.events.PassiveEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
/**
 * intercepts mouse movement to apply passive effects.
 * can change sensitivity or invert mouse controls during events.
 */
@Mixin(Mouse.class)
public class MouseMixin {

    /**
     * modifies horizontal mouse movement.
     * applies sensitivity multiplier and can invert the X axis.
     */
    @ModifyArg(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), index = 0)
    private double modifyMouseX(double deltaX) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            float multiplier = PassiveEvents.getMouseSensitivityMultiplier(client.player.getUuid());
            double modified = deltaX * multiplier;

            if (PassiveEvents.hasInputInversion(client.player.getUuid())) {
                modified = -modified;
            }

            return modified;
        }
        return deltaX;
    }

    /**
     * modifies vertical mouse movement.
     * applies sensitivity multiplier and can invert the Y axis.
     */
    @ModifyArg(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), index = 1)
    private double modifyMouseY(double deltaY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            float multiplier = PassiveEvents.getMouseSensitivityMultiplier(client.player.getUuid());
            double modified = deltaY * multiplier;

            if (PassiveEvents.hasInputInversion(client.player.getUuid())) {
                modified = -modified;
            }

            return modified;
        }
        return deltaY;
    }
}
