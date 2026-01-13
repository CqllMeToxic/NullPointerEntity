package lol.cqllmetoxic.nullpointerentity.item;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * registers custom items for the mod.
 * currently just decorative/lore items with no special functionality.
 */
public class ModdedItems {
    public static final Item AURORA_CORE = registerItem("aurora_core",new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NullPointerEntity.MOD_ID,"aurora_core")))));
    public static final Item CORRUPTED_DATA_SHARD = registerItem("corrupted_data_shard", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NullPointerEntity.MOD_ID, "corrupted_data_shard")))));
    public static final Item MEMORY_FRAGMENT = registerItem("memory_fragment", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NullPointerEntity.MOD_ID, "memory_fragment")))));
    public static final Item SYSTEM_OVERRIDE_CHIP = registerItem("system_override_chip", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NullPointerEntity.MOD_ID, "system_override_chip")))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(NullPointerEntity.MOD_ID, name), item);
    }

    /**
     * registers all items and adds them to the ingredients creative tab.
     */
    public static void registerModdedItems() {
        NullPointerEntity.LOGGER.info("pray that AURORA doesn't hurt you...");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(AURORA_CORE);
            entries.add(CORRUPTED_DATA_SHARD);
            entries.add(MEMORY_FRAGMENT);
            entries.add(SYSTEM_OVERRIDE_CHIP);
        });
    }
}
