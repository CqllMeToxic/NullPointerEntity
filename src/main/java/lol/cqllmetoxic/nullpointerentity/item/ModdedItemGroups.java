package lol.cqllmetoxic.nullpointerentity.item;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * registers custom creative mode item groups for the mod.
 * creates a dedicated tab for nullpointerentity items.
 */
public class ModdedItemGroups {
    public static final ItemGroup NULLPOINTERENTITY_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(NullPointerEntity.MOD_ID, "nullpointerentity.items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModdedItems.SYSTEM_OVERRIDE_CHIP))
                    .displayName(Text.translatable("itemgroup.nullpointerentity.items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModdedItems.AURORA_CORE);
                        entries.add(ModdedItems.CORRUPTED_DATA_SHARD);
                        entries.add(ModdedItems.MEMORY_FRAGMENT);
                        entries.add(ModdedItems.SYSTEM_OVERRIDE_CHIP);
                    })

                    .build());

    public static void registerModdedItemGroups() {

    }
}
