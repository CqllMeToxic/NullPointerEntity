package lol.cqllmetoxic.nullpointerentity.tracking;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.item.Item;
import java.util.Set;
import java.util.HashSet;
import java.util.Calendar;
import java.util.UUID;

/**
 * comprehensive player activity tracking system.
 * monitors mining, building, combat, movement, and gameplay patterns.
 * provides data for aurora's contextual messages about player behavior.
 */
public class PlayerTrackingSystem {
    private static final Map<String, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    /**
     * stores detailed statistics for a single player's session.
     * tracks everything from ore finds to building patterns to combat performance.
     */
    public static class PlayerData {
        public String playerName;
        public long sessionStartTime;
        public long totalPlayTime;
        public int blocksMinedThisSession;
        public int totalBlocksMined;

        public int diamondsFound = 0;
        public int ironFound = 0;
        public int coalFound = 0;
        public int goldFound = 0;
        public int emeraldFound = 0;
        public int redstoneFound = 0;
        public int lapisFound = 0;
        public int copperFound = 0;

        public final Map<Block, Integer> blocksMined = new HashMap<>();
        public final List<BlockPos> miningLocations = new ArrayList<>();
        public double miningEfficiency = 100.0;

        public final Map<Block, Integer> blocksPlaced = new HashMap<>();
        public int totalBlocksPlaced = 0;
        public final Set<BlockPos> buildingLocations = new HashSet<>();
        public long lastRecordedPlacedStatTotal = 0;

        public double totalDistanceTraveled = 0;
        public final Map<String, Double> biomeTimeSpent = new HashMap<>();
        public long timeInNether = 0;
        public long timeInEnd = 0;
        public long timeInOverworld = 0;

        public final Map<String, Integer> mobKillCounts = new HashMap<>();

        public final Map<Item, Integer> itemsCollected = new HashMap<>();
        public int totalItemsCollected = 0;

        public final Map<Integer, Long> hourlyActivity = new HashMap<>();

        public int chatMessagesSent = 0;
        public final List<String> favoriteCommands = new ArrayList<>();

        public double cpuUsage = 0;
        public int deathCount;
        public int mobKills;
        public int timesJumped;
        public int itemsCrafted;
        public double totalDistanceWalked;
        public double totalDistanceSprinted;
        public BlockPos currentPosition;
        public BlockPos lastKnownPosition;
        public String currentDimension;
        public String currentBiome;
        public long lastActivityTime;
        public List<String> recentBlocks = new ArrayList<>();
        public Map<String, Integer> blockTypesMined = new HashMap<>();
        public Map<String, Integer> itemsUsed = new HashMap<>();
        public boolean isAfk = false;
        public int afkTimeMinutes = 0;
        public double averageHealthPercent = 100.0;
        public int foodLevel = 20;
        public boolean isInCombat = false;
        public long lastCombatTime = 0;

        public PlayerData(String name) {
            this.playerName = name;
            this.sessionStartTime = System.currentTimeMillis();
            this.lastActivityTime = System.currentTimeMillis();
        }

        public long getSessionDurationMinutes() {
            return (System.currentTimeMillis() - sessionStartTime) / 60000;
        }


        public String getPrimaryActivity() {
            if (blocksMinedThisSession > 50) return "mining";
            if (itemsCrafted > 10) return "crafting";
            if (mobKills > 5) return "combat";
            if (totalDistanceWalked > 1000) return "exploring";
            return "idle";
        }

        public double getPlaytimeHours() {
            return (totalPlayTime + (System.currentTimeMillis() - sessionStartTime)) / 3600000.0;
        }
    }

    // initialize player tracking when they join
    public static void initializePlayer(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        UUID playerId = player.getUuid();

        if (!playerDataMap.containsKey(playerName)) {
            // create player data and load from persistent storage
            PlayerData data = new PlayerData(playerName);

            // load existing data from persistent storage if it exists
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(playerId);

            // load saved mining and combat data
            data.diamondsFound = persistentData.diamondsFound;
            data.ironFound = persistentData.ironFound;
            data.goldFound = persistentData.goldFound;
            data.coalFound = persistentData.coalFound;
            data.emeraldFound = persistentData.emeraldFound;
            data.redstoneFound = persistentData.redstoneFound;
            data.lapisFound = persistentData.lapisFound;
            data.copperFound = persistentData.copperFound;
            data.totalBlocksMined = persistentData.totalBlocksMined;
            data.totalBlocksPlaced = persistentData.totalBlocksPlaced;
            data.mobKills = persistentData.mobKills;
            data.deathCount = persistentData.deathCount;
            data.totalDistanceWalked = persistentData.totalDistanceWalked;
            data.totalDistanceSprinted = persistentData.totalDistanceSprinted;
            data.itemsUsed.putAll(persistentData.itemsUsed);
            data.mobKillCounts.putAll(persistentData.mobKillCounts);
            data.lastRecordedPlacedStatTotal = getCurrentPlacedBlockStatTotal(player);

            playerDataMap.put(playerName, data);
        } else {
            // update session start time for returning player
            PlayerData data = playerDataMap.get(playerName);
            data.sessionStartTime = System.currentTimeMillis();
            data.lastActivityTime = System.currentTimeMillis();
            data.lastRecordedPlacedStatTotal = getCurrentPlacedBlockStatTotal(player);
        }
    }

    // update player position and activity
    public static void updatePlayerPosition(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.lastKnownPosition = data.currentPosition;
            data.currentPosition = player.getBlockPos();
            data.currentDimension = player.getWorld().getRegistryKey().getValue().toString();
            data.currentBiome = player.getWorld().getBiome(player.getBlockPos()).getIdAsString();
            data.lastActivityTime = System.currentTimeMillis();
            data.isAfk = false;

            // calculate distance traveled
            if (data.lastKnownPosition != null) {
                double distance = data.currentPosition.getSquaredDistance(data.lastKnownPosition);
                if (player.isSprinting()) {
                    data.totalDistanceSprinted += Math.sqrt(distance);
                } else {
                    data.totalDistanceWalked += Math.sqrt(distance);
                }
            }

            // update health and food tracking
            data.averageHealthPercent = (data.averageHealthPercent + (player.getHealth() / player.getMaxHealth() * 100)) / 2;
            data.foodLevel = player.getHungerManager().getFoodLevel();
        }
    }

    // track block mining
    public static void trackBlockMined(ServerPlayerEntity player, Block block) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.blocksMinedThisSession++;
            data.totalBlocksMined++;
            data.lastActivityTime = System.currentTimeMillis();

            String blockName = block.getName().getString().toLowerCase();
            data.blockTypesMined.put(blockName, data.blockTypesMined.getOrDefault(blockName, 0) + 1);


            // track recent blocks (last 10)
            data.recentBlocks.add(0, blockName);
            if (data.recentBlocks.size() > 10) {
                data.recentBlocks.remove(10);
            }

            // ore tracking checks both registry names and display names
            String registryName = block.getTranslationKey().toLowerCase();

            // diamond ores (including deepslate)
            if (blockName.contains("diamond") || registryName.contains("diamond_ore") || registryName.contains("deepslate_diamond_ore")) {
                data.diamondsFound++;
            }
            // iron ores (including deepslate)
            else if (blockName.contains("iron") || registryName.contains("iron_ore") || registryName.contains("deepslate_iron_ore")) {
                data.ironFound++;
            }
            // coal ores (including deepslate)
            else if (blockName.contains("coal") || registryName.contains("coal_ore") || registryName.contains("deepslate_coal_ore")) {
                data.coalFound++;
            }
            // gold ores (including deepslate and nether)
            else if (blockName.contains("gold") || registryName.contains("gold_ore") || registryName.contains("deepslate_gold_ore") || registryName.contains("nether_gold_ore")) {
                data.goldFound++;
            }
            // emerald ores (including deepslate)
            else if (blockName.contains("emerald") || registryName.contains("emerald_ore") || registryName.contains("deepslate_emerald_ore")) {
                data.emeraldFound++;
            }
            // redstone ores (including deepslate)
            else if (blockName.contains("redstone") || registryName.contains("redstone_ore") || registryName.contains("deepslate_redstone_ore")) {
                data.redstoneFound++;
            }
            // lapis ores (including deepslate)
            else if (blockName.contains("lapis") || registryName.contains("lapis_ore") || registryName.contains("deepslate_lapis_ore")) {
                data.lapisFound++;
            }
            // copper ores (including deepslate)
            else if (blockName.contains("copper") || registryName.contains("copper_ore") || registryName.contains("deepslate_copper_ore")) {
                data.copperFound++;
            }

            // save mining data to persistent storage after any ore is found
            saveToPersistentStorage(player, data);
        }
    }

    // methods merged from playerdatatracker

    // get player data with both string name and serverplayerentity
    public static PlayerData getPlayerData(ServerPlayerEntity player) {
        return playerDataMap.get(player.getName().getString());
    }

    // track block mining with position
    public static void trackBlockMined(ServerPlayerEntity player, Block block, BlockPos pos) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            // use existing trackblockmined logic but add position tracking
            trackBlockMined(player, block);

            // add tracking from playerdatatracker
            data.blocksMined.put(block, data.blocksMined.getOrDefault(block, 0) + 1);
            data.miningLocations.add(pos);
            updateMiningEfficiency(data);
        }
    }

    // track block placement
    public static void trackBlockPlaced(ServerPlayerEntity player, Block block, BlockPos pos) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.blocksPlaced.put(block, data.blocksPlaced.getOrDefault(block, 0) + 1);
            data.buildingLocations.add(pos);
            data.totalBlocksPlaced++;
            data.lastActivityTime = System.currentTimeMillis();
        }
    }

    /**
     * Updates placement tracking using vanilla stats deltas so all placed block items are counted.
     */
    public static void refreshBlockPlacementTracking(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data == null) {
            return;
        }

        long currentPlacedTotal = getCurrentPlacedBlockStatTotal(player);

        // Stats can reset between worlds/saves; resync baseline without applying a negative delta.
        if (currentPlacedTotal < data.lastRecordedPlacedStatTotal) {
            data.lastRecordedPlacedStatTotal = currentPlacedTotal;
            return;
        }

        long placedDelta = currentPlacedTotal - data.lastRecordedPlacedStatTotal;
        if (placedDelta > 0) {
            data.totalBlocksPlaced += (int) placedDelta;
            data.lastActivityTime = System.currentTimeMillis();
        }

        data.lastRecordedPlacedStatTotal = currentPlacedTotal;
    }

    private static long getCurrentPlacedBlockStatTotal(ServerPlayerEntity player) {
        ServerStatHandler stats = player.getStatHandler();
        long total = 0;

        // Sum all block-item uses; this tracks real placement behavior better than a short hardcoded item list.
        for (Item item : Registries.ITEM) {
            if (item instanceof net.minecraft.item.BlockItem) {
                total += stats.getStat(Stats.USED.getOrCreateStat(item));
            }
        }

        return total;
    }

    // movement tracking
    public static void trackMovement(ServerPlayerEntity player, double distance) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.totalDistanceTraveled += distance;
            data.lastActivityTime = System.currentTimeMillis();

            // biome tracking
            String biomeName = player.getWorld().getBiome(player.getBlockPos()).getKey().toString();
            data.biomeTimeSpent.put(biomeName, data.biomeTimeSpent.getOrDefault(biomeName, 0.0) + 1.0);

            // dimension tracking
            String dimensionName = player.getWorld().getDimensionEntry().getIdAsString();
            if (dimensionName.contains("nether")) data.timeInNether++;
            else if (dimensionName.contains("end")) data.timeInEnd++;
            else data.timeInOverworld++;
        }
    }

    // mob kill tracking
    public static void trackMobKill(ServerPlayerEntity player, String mobType) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.mobKills++;
            data.mobKillCounts.put(mobType, data.mobKillCounts.getOrDefault(mobType, 0) + 1);
            data.lastActivityTime = System.currentTimeMillis();
            data.isInCombat = true;
            data.lastCombatTime = System.currentTimeMillis();
        }
    }

    // track item collection
    public static void trackItemCollected(ServerPlayerEntity player, Item item, int count) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.itemsCollected.put(item, data.itemsCollected.getOrDefault(item, 0) + count);
            data.totalItemsCollected += count;
            data.lastActivityTime = System.currentTimeMillis();
        }
    }

    // track chat messages and commands
    public static void trackChatMessage(ServerPlayerEntity player, String message) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.chatMessagesSent++;
            data.lastActivityTime = System.currentTimeMillis();

            if (message.startsWith("/")) {
                data.favoriteCommands.add(message.split(" ")[0]);
            }
        }
    }

    // update mining efficiency calculation
    private static void updateMiningEfficiency(PlayerData data) {
        if (data.totalBlocksMined > 0) {
            double valuableOres = data.diamondsFound * 10 + data.goldFound * 8 + data.emeraldFound * 12 +
                                 data.ironFound * 2 + data.copperFound * 1.5 +
                                 data.coalFound * 0.5 + data.redstoneFound * 1 + data.lapisFound * 1;
            data.miningEfficiency = (valuableOres / data.totalBlocksMined) * 100;
        }
    }

    // get mining efficiency
    public static double getMiningEfficiency(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.miningEfficiency : 0.0;
    }

    // get preferred mining y level
    public static int getPreferredMiningYLevel(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        if (data == null || data.miningLocations.isEmpty()) return 11;

        return (int) data.miningLocations.stream()
            .mapToInt(BlockPos::getY)
            .average()
            .orElse(11);
    }

    // get most active time of day
    public static String getMostActiveTimeOfDay(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        if (data == null) return "Unknown";

        return data.hourlyActivity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + ":00")
            .orElse("Unknown");
    }

    // get favorite command
    public static String getFavoriteCommand(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        if (data == null || data.favoriteCommands.isEmpty()) return "/help";

        Map<String, Integer> commandCounts = new HashMap<>();
        for (String cmd : data.favoriteCommands) {
            commandCounts.put(cmd, commandCounts.getOrDefault(cmd, 0) + 1);
        }

        return commandCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("/help");
    }

    // update system stats with real system monitoring
    public static void updateSystemStats(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        if (data == null) return;

        // get real system monitoring data
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();

            // get actual cpu usage
            double cpuUsage = osBean.getProcessCpuLoad() * 100.0;
            if (cpuUsage >= 0.0) {
                data.cpuUsage = cpuUsage;
            } else {
                // fallback to memory-based estimation if process cpu is not available
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                data.cpuUsage = (double) usedMemory / totalMemory * 50.0; // scale to reasonable cpu percentage
            }
        } catch (Exception e) {
            // fallback to memory-based estimation if cpu monitoring fails
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            data.cpuUsage = (double) usedMemory / totalMemory * 100.0;
        }

        // track hourly activity
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        data.hourlyActivity.put(hour, data.hourlyActivity.getOrDefault(hour, 0L) + 1);
    }

    // track player death
    public static void trackPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.deathCount++;
            data.lastActivityTime = System.currentTimeMillis();
            data.isInCombat = false;
        }
    }

    // track mob kill
    public static void trackMobKill(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.mobKills++;
            data.lastActivityTime = System.currentTimeMillis();
            data.isInCombat = true;
            data.lastCombatTime = System.currentTimeMillis();
        }
    }

    // track jumping
    public static void trackPlayerJump(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.timesJumped++;
            data.lastActivityTime = System.currentTimeMillis();
        }
    }

    // track crafting
    public static void trackItemCrafted(ServerPlayerEntity player, ItemStack item) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.itemsCrafted++;
            data.lastActivityTime = System.currentTimeMillis();

            String itemName = item.getName().getString();
            data.itemsUsed.put(itemName + "_crafted", data.itemsUsed.getOrDefault(itemName + "_crafted", 0) + 1);
        }
    }

    // check for afk players
    public static void updateAfkStatus() {
        long currentTime = System.currentTimeMillis();

        for (PlayerData data : playerDataMap.values()) {
            if (currentTime - data.lastActivityTime > 300000) { // 5 minutes
                data.isAfk = true;
                data.afkTimeMinutes = (int) ((currentTime - data.lastActivityTime) / 60000);
            }

            // update combat status
            if (data.isInCombat && currentTime - data.lastCombatTime > 10000) { // 10 seconds
                data.isInCombat = false;
            }
        }
    }

    // get player data for analysis
    public static PlayerData getPlayerData(String playerName) {
        return playerDataMap.get(playerName);
    }

    // get all tracked players
    public static Map<String, PlayerData> getAllPlayerData() {
        return new HashMap<>(playerDataMap);
    }

    // save player session data when they disconnect
    public static void savePlayerSession(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        PlayerData data = playerDataMap.get(playerName);

        if (data != null) {
            data.totalPlayTime += (System.currentTimeMillis() - data.sessionStartTime);
        }
    }

    // generate activity summary for events
    public static String generateActivitySummary(String playerName) {
        PlayerData data = playerDataMap.get(playerName);
        if (data == null) return "no data available";

        StringBuilder summary = new StringBuilder();
        summary.append("session: ").append(data.getSessionDurationMinutes()).append(" minutes, ");
        summary.append("primary activity: ").append(data.getPrimaryActivity()).append(", ");
        summary.append("blocks mined: ").append(data.blocksMinedThisSession).append(", ");
        summary.append("distance: ").append(String.format("%.1f", data.totalDistanceWalked + data.totalDistanceSprinted)).append(" blocks");

        if (!data.recentBlocks.isEmpty()) {
            summary.append(", recent blocks: ").append(String.join(", ", data.recentBlocks.subList(0, Math.min(3, data.recentBlocks.size()))));
        }

        return summary.toString();
    }

    // get detailed player stats for hostile events
    public static String generateDetailedStats(String playerName) {
        PlayerData data = playerDataMap.get(playerName);
        if (data == null) return "player not found";

        return String.format(
            "playtime: %.1f hours | deaths: %d | kills: %d | blocks mined: %d | items crafted: %d | current health: %.1f%% | food: %d/20 | dimension: %s",
            data.getPlaytimeHours(),
            data.deathCount,
            data.mobKills,
            data.totalBlocksMined,
            data.itemsCrafted,
            data.averageHealthPercent,
            data.foodLevel,
            data.currentDimension.replace("minecraft:", "")
        );
    }

    // clear all tracking data
    public static void clearAllData() {
        playerDataMap.clear();
    }

    // save mining data to persistent storage
    private static void saveToPersistentStorage(ServerPlayerEntity player, PlayerData data) {
        try {
            UUID playerId = player.getUuid();
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(playerId);

            // update persistent data with current mining data
            persistentData.totalBlocksMined = data.totalBlocksMined;
            persistentData.diamondsFound = data.diamondsFound;
            persistentData.ironFound = data.ironFound;
            persistentData.coalFound = data.coalFound;
            persistentData.goldFound = data.goldFound;
            persistentData.emeraldFound = data.emeraldFound;
            persistentData.redstoneFound = data.redstoneFound;
            persistentData.lapisFound = data.lapisFound;
            persistentData.copperFound = data.copperFound;
            persistentData.blockTypesMined.clear();
            persistentData.blockTypesMined.putAll(data.blockTypesMined);

            // update other tracking data
            persistentData.totalBlocksPlaced = data.totalBlocksPlaced;
            persistentData.mobKills = data.mobKills;
            persistentData.deathCount = data.deathCount;
            persistentData.itemsCrafted = data.itemsCrafted;
            persistentData.totalDistanceWalked = data.totalDistanceWalked;
            persistentData.totalDistanceSprinted = data.totalDistanceSprinted;
            persistentData.itemsUsed.clear();
            persistentData.itemsUsed.putAll(data.itemsUsed);
            persistentData.mobKillCounts.clear();
            persistentData.mobKillCounts.putAll(data.mobKillCounts);

            // save to disk
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(playerId, persistentData);

        } catch (Exception e) {
            // failed to save mining data to persistent storage
        }
    }
}
