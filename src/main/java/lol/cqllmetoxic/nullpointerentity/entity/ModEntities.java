package lol.cqllmetoxic.nullpointerentity.entity;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

/**
 * registers custom entity types for the mod.
 * currently just handles the fake player entity (Nul).
 */
public class ModEntities {

    /**
     * the nullpointer entity - looks like a player, spawns behind you.
     * uses player dimensions and limited tracking.
     */
    public static final EntityType<FakePlayerEntity> FAKE_PLAYER_ENTITY = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(NullPointerEntity.MOD_ID, "nullpointerentity"),
        EntityType.Builder.<FakePlayerEntity>create(FakePlayerEntity::new, SpawnGroup.MISC)
            .dimensions(0.6f, 1.8f)
            .maxTrackingRange(10)
            .trackingTickInterval(3)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(NullPointerEntity.MOD_ID, "nullpointerentity")))
    );

    /**
     * called during mod init to register entities with minecraft.
     */
    public static void registerEntities() {
        NullPointerEntity.LOGGER.info("Registering entities for " + NullPointerEntity.MOD_ID);

        FabricDefaultAttributeRegistry.register(FAKE_PLAYER_ENTITY, FakePlayerEntity.createFakePlayerAttributes());
    }
}
