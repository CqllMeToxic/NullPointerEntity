package lol.cqllmetoxic.nullpointerentity.events;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * freezes all entities and players for a short global event window.
 */
public final class GlobalFreezeController {
    private static volatile long freezeEndTimeMs = 0L;
    private static final Map<UUID, Snapshot> frozenSnapshots = new ConcurrentHashMap<>();

    private record Snapshot(double x, double y, double z, float yaw, float pitch) {
    }

    private GlobalFreezeController() {
    }

    public static void startFreeze(long durationMs) {
        long now = System.currentTimeMillis();
        long newEnd = now + durationMs;
        if (newEnd > freezeEndTimeMs) {
            freezeEndTimeMs = newEnd;
        }
    }

    public static boolean isFrozen() {
        return System.currentTimeMillis() < freezeEndTimeMs;
    }

    public static void tick(MinecraftServer server) {
        if (!isFrozen()) {
            frozenSnapshots.clear();
            return;
        }

        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                    continue;
                }

                Snapshot snapshot = frozenSnapshots.computeIfAbsent(entity.getUuid(), uuid ->
                    new Snapshot(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch())
                );

                entity.setVelocity(Vec3d.ZERO);
                entity.setOnGround(true);

                if (entity instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.requestTeleport(
                        snapshot.x(),
                        snapshot.y(),
                        snapshot.z(),
                        snapshot.yaw(),
                        snapshot.pitch()
                    );
                    serverPlayer.fallDistance = 0.0f;
                } else {
                    entity.refreshPositionAndAngles(
                        snapshot.x(),
                        snapshot.y(),
                        snapshot.z(),
                        snapshot.yaw(),
                        snapshot.pitch()
                    );
                }
            }
        }
    }
}

