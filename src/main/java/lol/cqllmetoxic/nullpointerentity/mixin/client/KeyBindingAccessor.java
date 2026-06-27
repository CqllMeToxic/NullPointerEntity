package lol.cqllmetoxic.nullpointerentity.mixin.client;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * lets us rewrite the display name of an existing key binding. used to relabel Shriek's voice
 * keybind from its default "Push To Mute" text to a push-to-talk label, so it's findable in
 * Options -> Controls. only the (final) translation key field is touched; the bound key, category,
 * and recording behaviour are left alone.
 */
@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Mutable
    @Accessor("translationKey")
    void nullpointerentity$setTranslationKey(String translationKey);
}
