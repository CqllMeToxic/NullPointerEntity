package lol.cqllmetoxic.nullpointerentity.entity;

import com.mojang.authlib.GameProfile;
import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * manages spawning and tracking of fake player entities (the nullpointer entity).
 * handles rotation tracking to detect when players turn around.
 * spawns the entity behind players when they complete a turn.
 * tracks entities per-world to prevent cross-world issues.
 */
public class FakePlayerManager {
    /** tracks fake players per world - key is world ID, value is map of player UUID to fake entity */
    private static final Map<String, Map<UUID, FakePlayerEntity>> worldFakePlayers = new ConcurrentHashMap<>();
    /** stores spawn timestamps per world to enforce cooldowns */
    private static final Map<String, Map<UUID, Long>> worldSpawnTimes = new ConcurrentHashMap<>();
    /** cooldown between spawns - 5 minutes */
    private static final long SPAWN_COOLDOWN = 300000;

    /** stores each player's previous yaw value for rotation comparison */
    private static final Map<UUID, Float> playerLastYaw = new ConcurrentHashMap<>();
    /** tracks whether a player is currently executing a turn */
    private static final Map<UUID, Boolean> playerIsTurning = new ConcurrentHashMap<>();
    /** remembers the yaw when the turn started */
    private static final Map<UUID, Float> turnStartYaw = new ConcurrentHashMap<>();
    /** timestamp of when the current turn began */
    private static final Map<UUID, Long> lastTurnTime = new ConcurrentHashMap<>();
    /** how many degrees rotation before we consider it a "turn" */
    private static final float TURN_THRESHOLD = 30.0f;
    /** minimum degrees for halfway point (around 90 degrees) */
    private static final float HALFWAY_TURN_MIN = 80.0f;
    /** maximum degrees for halfway point */
    private static final float HALFWAY_TURN_MAX = 100.0f;
    /** turns faster than this are ignored (0.5 seconds) */
    private static final long TURN_TIME_MIN = 500;
    /** turns slower than this are ignored (3 seconds) */
    private static final long TURN_TIME_MAX = 3000;

    /**
     * spawns the nullpointer entity immediately for a player.
     * bypasses all event progression checks - use this for testing or forced spawns.
     * places the entity where the player was looking before they turned.
     *
     * @param targetPlayer the player to spawn the entity for
     */
    public static void spawnNullPointerEntity(ServerPlayerEntity targetPlayer) {
        if (targetPlayer == null || targetPlayer.getWorld().isClient) {
            NullPointerEntity.LOGGER.warn("Cannot spawn fake player: invalid player or client world");
            return;
        }

        NullPointerEntity.LOGGER.info("Spawning NullPointerEntity for player: {}", targetPlayer.getName().getString());

        String worldId = PersistentDataManager.getCurrentWorldId();
        UUID playerUuid = targetPlayer.getUuid();

        // get world-specific maps
        Map<UUID, FakePlayerEntity> worldPlayers = worldFakePlayers.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
        Map<UUID, Long> worldTimes = worldSpawnTimes.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());

        // remove existing fake player for this target in this world
        removeFakePlayer(playerUuid, worldId);

        ServerWorld world = (ServerWorld) targetPlayer.getWorld();

        // find spawn position behind the player
        BlockPos spawnPos = findSpawnLocationBehindPlayer(world, targetPlayer);

        if (spawnPos != null) {
            try {
                // create our custom fake player entity
                FakePlayerEntity fakePlayer = new FakePlayerEntity(ModEntities.FAKE_PLAYER_ENTITY, world);
                fakePlayer.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                fakePlayer.setTargetPlayer(targetPlayer);

                // spawn the entity in the world
                world.spawnEntity(fakePlayer);

                // track the fake player
                worldPlayers.put(playerUuid, fakePlayer);
                worldTimes.put(playerUuid, System.currentTimeMillis());

                // mark that fake player has been spawned in this world (for testing)
                PersistentDataManager.setFakePlayerSpawned(true);

                NullPointerEntity.LOGGER.info("Spawned NullPointerEntity for {} at position {} in world {}",
                    targetPlayer.getName().getString(), spawnPos, worldId);

                // send initial warning message
                scheduleInitialMessage(targetPlayer);

            } catch (Exception e) {
                NullPointerEntity.LOGGER.error("Failed to spawn NullPointerEntity for {} in world {}",
                    targetPlayer.getName().getString(), worldId, e);
            }
        } else {
            NullPointerEntity.LOGGER.warn("Could not find suitable spawn location for NullPointerEntity in world: {}", worldId);
        }
    }


    // find spawn location behind the player
    private static BlockPos findSpawnLocationBehindPlayer(ServerWorld world, ServerPlayerEntity player) {
        Vec3d playerPos = player.getPos();

        // get the player's current yaw and add 180 degrees to spawn behind them
        float behindYaw = player.getYaw() + 180.0f;

        // normalize the angle to stay within -180 to 180 range
        while (behindYaw > 180.0f) behindYaw -= 360.0f;
        while (behindYaw < -180.0f) behindYaw += 360.0f;

        // calculate position in the direction behind where they were originally facing
        double lookAngle = Math.toRadians(behindYaw);

        // spawn 10-20 blocks away behind the player
        int distance = 10 + new Random().nextInt(11);

        // minecraft coordinate system: yaw 0 = north (-z), yaw 90 = east (+x)
        double spawnX = playerPos.x - Math.sin(lookAngle) * distance;
        double spawnZ = playerPos.z + Math.cos(lookAngle) * distance;

        // find suitable y coordinate (ground level or player level)
        for (int y = (int)playerPos.y + 3; y > (int)playerPos.y - 5; y--) {
            BlockPos testPos = new BlockPos((int)spawnX, y, (int)spawnZ);

            if (world.getBlockState(testPos).isAir() &&
                world.getBlockState(testPos.up()).isAir() &&
                !world.getBlockState(testPos.down()).isAir()) {

                return testPos;
            }
        }

        // fallback: use player's y level
        return new BlockPos((int)spawnX, (int)playerPos.y, (int)spawnZ);
    }

    private static void scheduleInitialMessage(ServerPlayerEntity player) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (player.isDisconnected()) {
                    timer.cancel();
                    return;
                }


                timer.cancel();
            }
        }, 3000); // 3 seconds delay
    }

    public static void removeFakePlayer(UUID targetPlayerUuid, String worldId) {
        Map<UUID, FakePlayerEntity> worldPlayers = worldFakePlayers.get(worldId);
        if (worldPlayers != null) {
            FakePlayerEntity fakePlayer = worldPlayers.remove(targetPlayerUuid);
            if (fakePlayer != null && !fakePlayer.isRemoved()) {
                fakePlayer.discard();
                NullPointerEntity.LOGGER.debug("Removed fake player for {} from world {}", targetPlayerUuid, worldId);
            }
        }
    }

    // overloaded method for backwards compatibility
    public static void removeFakePlayer(UUID targetPlayerUuid) {
        String currentWorldId = PersistentDataManager.getCurrentWorldId();
        if (currentWorldId != null) {
            removeFakePlayer(targetPlayerUuid, currentWorldId);
        }
    }

    public static void cleanupFakePlayersForWorld(String worldId) {
        Map<UUID, FakePlayerEntity> worldPlayers = worldFakePlayers.get(worldId);
        if (worldPlayers != null) {
            for (FakePlayerEntity fakePlayer : worldPlayers.values()) {
                if (!fakePlayer.isRemoved()) {
                    fakePlayer.discard();
                }
            }
            worldPlayers.clear();
            NullPointerEntity.LOGGER.debug("Cleaned up all fake players for world: {}", worldId);
        }

        // also clean up spawn times
        Map<UUID, Long> worldTimes = worldSpawnTimes.get(worldId);
        if (worldTimes != null) {
            worldTimes.clear();
        }
    }

    public static void cleanupAllFakePlayers() {
        for (String worldId : worldFakePlayers.keySet()) {
            cleanupFakePlayersForWorld(worldId);
        }
        worldFakePlayers.clear();
        worldSpawnTimes.clear();
        NullPointerEntity.LOGGER.debug("Cleaned up all fake players from all worlds");
    }


    // method to check if fake player should be spawned for a specific event
    public static boolean shouldSpawnFakePlayerForEvent(ServerPlayerEntity player, String eventType) {
        String worldId = PersistentDataManager.getCurrentWorldId();

        // don't spawn if already spawned in this world
        if (PersistentDataManager.hasFakePlayerSpawned()) {
            return false;
        }

        // only spawn for certain hostile events
        return eventType.equals("hostile") && !PersistentDataManager.hasPlayerSeenFakePlayer(player.getUuid().toString());
    }

    // add missing methods that are being called from nullpointerentity.java
    public static void onPlayerJoin(ServerPlayerEntity player) {
        String worldId = PersistentDataManager.getCurrentWorldId();

        NullPointerEntity.LOGGER.debug("Player {} joined world {}", player.getName().getString(), worldId);

        // initialize world-specific tracking if needed
        worldFakePlayers.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
        worldSpawnTimes.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
    }

    public static void onPlayerDisconnect(UUID playerUuid) {
        String worldId = PersistentDataManager.getCurrentWorldId();

        NullPointerEntity.LOGGER.debug("Player {} disconnected from world {}", playerUuid, worldId);

        // clean up any fake players for this disconnected player
        removeFakePlayer(playerUuid, worldId);
    }

    public static void tick() {
        // periodic cleanup and maintenance
        // remove fake players that have been active too long or are in invalid states
        for (String worldId : worldFakePlayers.keySet()) {
            Map<UUID, FakePlayerEntity> worldPlayers = worldFakePlayers.get(worldId);
            if (worldPlayers != null) {
                worldPlayers.entrySet().removeIf(entry -> {
                    FakePlayerEntity fakePlayer = entry.getValue();
                    if (fakePlayer == null || fakePlayer.isRemoved() || !fakePlayer.isAlive()) {
                        return true; // remove invalid entries
                    }
                    return false;
                });
            }
        }
    }

    // temporary spawn for event 19 - spawns directly in front of player and disappears after duration
    public static void spawnTemporaryNullPointerEntity(ServerPlayerEntity targetPlayer, int durationTicks) {
        if (targetPlayer == null || targetPlayer.getWorld().isClient) {
            NullPointerEntity.LOGGER.warn("Cannot spawn temporary NullPointerEntity: invalid player or client world");
            return;
        }

        ServerWorld world = targetPlayer.getServerWorld();

        // calculate position 3 blocks in front of the player
        Vec3d playerPos = targetPlayer.getPos();
        Vec3d lookDirection = targetPlayer.getRotationVec(1.0f);
        Vec3d spawnPos = playerPos.add(lookDirection.multiply(3.0));

        try {
            // create the temporary fake player entity
            FakePlayerEntity fakePlayer = new FakePlayerEntity(ModEntities.FAKE_PLAYER_ENTITY, world);
            fakePlayer.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
            fakePlayer.setTargetPlayer(targetPlayer);

            // set custom name to show "nullpointerentity" instead of the entity type
            fakePlayer.setCustomName(Text.literal("NullPointerEntity").formatted(Formatting.DARK_RED));
            fakePlayer.setCustomNameVisible(true);

            // spawn the entity
            world.spawnEntity(fakePlayer);

            NullPointerEntity.LOGGER.info("Spawned temporary NullPointerEntity for {} at position {} for {} ticks",
                targetPlayer.getName().getString(), spawnPos, durationTicks);

            // schedule removal after the specified duration
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (fakePlayer != null && !fakePlayer.isRemoved()) {
                        fakePlayer.discard();
                        NullPointerEntity.LOGGER.info("Removed temporary NullPointerEntity for {}",
                            targetPlayer.getName().getString());
                    }
                }
            }, durationTicks * 50); // convert ticks to milliseconds (20 ticks = 1 second = 1000ms)

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to spawn temporary NullPointerEntity: {}", e.getMessage());
        }
    }
}
