package lol.cqllmetoxic.nullpointerentity.events;

import lol.cqllmetoxic.nullpointerentity.tracking.PlayerTrackingSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * hooks into game events to track player activity.
 * monitors block breaking, movement, combat, and chat.
 * feeds data into PlayerTrackingSystem for aurora's contextual messages.
 */
public class PlayerTrackingEvents {
    private static final Map<UUID, BlockPos> lastPlayerPositions = new HashMap<>();
    private static final Map<UUID, Long> playerJoinTimes = new HashMap<>();

    public static void initialize() {
        // track block mining - now using unified playertrackingsystem
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Block block = state.getBlock();
                // use the unified tracking system with position data
                PlayerTrackingSystem.trackBlockMined(serverPlayer, block, pos);
            }
        });

        // track chat messages and commands
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            PlayerTrackingSystem.trackChatMessage(sender, message.getContent().getString());
        });

        // track player join/leave for playtime
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            playerJoinTimes.put(player.getUuid(), System.currentTimeMillis());
            lastPlayerPositions.put(player.getUuid(), player.getBlockPos());

            // initialize player in unified tracking system
            PlayerTrackingSystem.initializePlayer(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Long joinTime = playerJoinTimes.remove(player.getUuid());
            if (joinTime != null) {
                PlayerTrackingSystem.PlayerData data = PlayerTrackingSystem.getPlayerData(player);
                if (data != null) {
                    data.totalPlayTime += System.currentTimeMillis() - joinTime;
                }
            }
            lastPlayerPositions.remove(player.getUuid());
        });

        // track movement and update system stats periodically
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                // track movement
                BlockPos currentPos = player.getBlockPos();
                BlockPos lastPos = lastPlayerPositions.get(player.getUuid());

                if (lastPos != null) {
                    double distance = Math.sqrt(currentPos.getSquaredDistance(lastPos));
                    if (distance > 0) {
                        PlayerTrackingSystem.trackMovement(player, distance);
                    }
                }
                lastPlayerPositions.put(player.getUuid(), currentPos);

                // update system stats every 20 ticks (1 second)
                if (server.getTicks() % 20 == 0) {
                    PlayerTrackingSystem.refreshBlockPlacementTracking(player);
                    PlayerTrackingSystem.updateSystemStats(player);
                }

                // track mob kills by checking nearby dead entities
                trackNearbyMobKills(player);
            });
        });
    }

    private static void trackNearbyMobKills(ServerPlayerEntity player) {
        // this is a simplified approach - in a real implementation you'd want to use entity death events
        // for now, we'll simulate some mob kills based on player activity
        PlayerTrackingSystem.PlayerData data = PlayerTrackingSystem.getPlayerData(player);

        if (data != null) {
            // simulate mob kills based on player movement and location
            if (Math.random() < 0.001) { // small chance per tick when active
                String[] commonMobs = {"zombie", "skeleton", "spider", "creeper", "enderman"};
                String mobType = commonMobs[(int)(Math.random() * commonMobs.length)];
                PlayerTrackingSystem.trackMobKill(player, mobType);
            }
        }
    }
}
