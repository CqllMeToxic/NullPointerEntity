package lol.cqllmetoxic.nullpointerentity.block;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModdedBlocks {

    public static final Block AURORA_BLOCK = registerBlock("computer",
        new Block(AbstractBlock.Settings.create().strength(4.0f).
                requiresTool().sounds(BlockSoundGroup.GLASS)));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(NullPointerEntity.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(NullPointerEntity.MOD_ID, name),
                new BlockItem(block, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NullPointerEntity.MOD_ID, name)))));
    }

    public static void registerModdedBlocks() {
        NullPointerEntity.LOGGER.info("Building AURORA's external components... ");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(AURORA_CPU);
        });
    }
}
