package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.client.ClientPassiveEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * intercepts keyboard input to apply passive effects.
 * can delay key presses or modify key bindings during certain events.
 */
@Mixin(KeyBinding.class)
public class KeyBindingMixin {

    private static final ThreadLocal<Boolean> isCheckingKey = ThreadLocal.withInitial(() -> false);

    /**
     * modifies key press detection to apply input delays.
     * uses thread-local flag to prevent infinite recursion.
     */
    @Inject(method = "isPressed", at = @At("RETURN"), cancellable = true)
    private void modifyKeyPress(CallbackInfoReturnable<Boolean> cir) {
        if (isCheckingKey.get()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // only modify if input inversion is active
        if (!ClientPassiveEffects.hasInputInversion()) return;

        KeyBinding self = (KeyBinding) (Object) this;
        GameOptions options = client.options;

        // set flag to prevent recursion
        isCheckingKey.set(true);
        try {
            // invert movement keys
            if (self == options.forwardKey) {
                cir.setReturnValue(options.backKey.isPressed());
            } else if (self == options.backKey) {
                cir.setReturnValue(options.forwardKey.isPressed());
            } else if (self == options.leftKey) {
                cir.setReturnValue(options.rightKey.isPressed());
            } else if (self == options.rightKey) {
                cir.setReturnValue(options.leftKey.isPressed());
            } else if (self == options.jumpKey) {
                cir.setReturnValue(options.sneakKey.isPressed());
            } else if (self == options.sneakKey) {
                cir.setReturnValue(options.jumpKey.isPressed());
            }
        } finally {
            // always clear flag
            isCheckingKey.set(false);
        }
    }
}

