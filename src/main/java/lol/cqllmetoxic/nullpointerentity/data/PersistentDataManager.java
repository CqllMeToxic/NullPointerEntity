package lol.cqllmetoxic.nullpointerentity.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * manages persistent data storage for the mod.
 * saves player stats, event progression, and world state to JSON files.
 * data persists between game sessions and is stored per-world.
 */
public class PersistentDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE_NAME = "nullpointer_entity_data.json";
    private static PersistentWorldData worldData = new PersistentWorldData();
    private static File dataFile;
    private static String currentWorldId;

    /**
     * stores world-level data including event progression and player records.
     * contains maps of all player data and tracks which events have been triggered.
     */
    public static class PersistentWorldData {
        public Map<String, PersistentPlayerData> playerData = new ConcurrentHashMap<>();
        public long worldCreationTime = System.currentTimeMillis();
        public String worldId;
        public boolean isNewWorld = true;

        public boolean welcomeMessageSent = false;
        public int currentEventPhase = 0;
        public long lastEventTime = 0;
        public int eventsTriggeredThisSession = 0;

        public boolean fakePlayerSpawned = false;
        public Map<String, Boolean> playerHasSeenFakePlayer = new ConcurrentHashMap<>();
    }

    /**
     * stores per-player statistics and progression.
     * tracks entity encounters, event history, mining stats, and gameplay metrics.
     */
    public static class PersistentPlayerData {
        public boolean hasSeenNullPointerEntity = false;
        public boolean shouldReceiveReturnMessage = false;
        public long lastInteractionTime = System.currentTimeMillis();
        public int warningLevel = 0;
        public int timesEncounteredEntity = 0;
        public boolean hasBeenCrashed = false;

        public Map<String, Boolean> triggeredEvents = new ConcurrentHashMap<>();
        public Map<String, Long> lastEventTimes = new ConcurrentHashMap<>();
        public int totalEventsExperienced = 0;
        public boolean privacyModeEnabled = true;
        public long totalPlayTime = 0;
        public int sessionsPlayed = 0;

        public int totalBlocksMined = 0;
        public int diamondsFound = 0;
        public int ironFound = 0;
        public int coalFound = 0;
        public int goldFound = 0;
        public int emeraldFound = 0;
        public int redstoneFound = 0;
        public int lapisFound = 0;
        public int copperFound = 0;
        public Map<String, Integer> blockTypesMined = new ConcurrentHashMap<>();

        public int totalBlocksPlaced = 0;
        public int mobKills = 0;
        public int deathCount = 0;
        public int itemsCrafted = 0;
        public double totalDistanceWalked = 0;
        public double totalDistanceSprinted = 0;
        public Map<String, Integer> itemsUsed = new ConcurrentHashMap<>();
        public Map<String, Integer> mobKillCounts = new ConcurrentHashMap<>();

        // last known player name for lookup
        public String lastKnownPlayerName = "";
    }

    public static void initialize(MinecraftServer server) {
        try {
            Path worldDir = server.getSavePath(WorldSavePath.ROOT);

            // create world-specific identifier based on world directory name
            currentWorldId = worldDir.getFileName().toString();

            dataFile = worldDir.resolve(DATA_FILE_NAME).toFile();

            loadWorldData();
        } catch (Exception e) {
            // initialization failed
        }
    }

    private static void loadWorldData() {
        if (dataFile == null || !dataFile.exists()) {
            // brand new world - create fresh data
            worldData = new PersistentWorldData();
            worldData.worldId = currentWorldId;
            worldData.isNewWorld = true;
            worldData.worldCreationTime = System.currentTimeMillis();

            // save the new world data immediately
            saveData();
        } else {
            // existing world - load saved data
            try {
                String jsonData = Files.readString(dataFile.toPath());
                worldData = GSON.fromJson(jsonData, PersistentWorldData.class);

                if (worldData == null) {
                    worldData = new PersistentWorldData();
                    worldData.worldId = currentWorldId;
                    worldData.isNewWorld = true;
                } else {
                    // existing world - events should continue where they left off
                    worldData.isNewWorld = false;
                }
            } catch (Exception e) {
                worldData = new PersistentWorldData();
                worldData.worldId = currentWorldId;
                worldData.isNewWorld = true;
            }
        }
    }

    public static void saveData() {
        if (dataFile == null) return;

        try {
            // ensure parent directory exists
            File parentDir = dataFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            String jsonData = GSON.toJson(worldData);
            Files.writeString(dataFile.toPath(), jsonData);
        } catch (Exception e) {
            // save failed
        }
    }

    // world-specific methods
    public static String getCurrentWorldId() {
        return currentWorldId;
    }

    public static PersistentWorldData getWorldData() {
        return worldData;
    }

    // fake player tracking methods
    public static boolean hasFakePlayerSpawned() {
        return worldData.fakePlayerSpawned;
    }

    public static void setFakePlayerSpawned(boolean spawned) {
        worldData.fakePlayerSpawned = spawned;
        saveDataAsync();
    }

    public static boolean hasPlayerSeenFakePlayer(String playerId) {
        return worldData.playerHasSeenFakePlayer.getOrDefault(playerId, false);
    }

    // player data methods
    public static PersistentPlayerData getPlayerData(UUID playerId) {
        return getPlayerData(playerId.toString());
    }

    public static PersistentPlayerData getPlayerData(String playerId) {
        PersistentPlayerData data = worldData.playerData.get(playerId);
        if (data == null) {
            data = new PersistentPlayerData();
            worldData.playerData.put(playerId, data);
            saveDataAsync();
        }
        return data;
    }

    public static void savePlayerData(String playerId, PersistentPlayerData data) {
        worldData.playerData.put(playerId, data);
        saveDataAsync();
    }

    // missing methods for transforming message tracking
    public static void setTransformingMessageSent(UUID playerId) {
        PersistentPlayerData data = getPlayerData(playerId.toString());
        data.shouldReceiveReturnMessage = true;
        savePlayerData(playerId.toString(), data);
    }

    public static boolean needsTransformingMessage(UUID playerId) {
        PersistentPlayerData data = getPlayerData(playerId.toString());
        return data.shouldReceiveReturnMessage;
    }

    // privacy settings
    public static boolean isPrivacyEnabled(String playerId) {
        PersistentPlayerData data = getPlayerData(playerId);
        return data.privacyModeEnabled;
    }

    // welcome message tracking
    public static void setWelcomeMessageSent(boolean sent) {
        worldData.welcomeMessageSent = sent;
        saveDataAsync();
    }

    // additional missing methods to fix compilation errors
    public static void updatePlayerData(UUID playerId, PersistentPlayerData data) {
        savePlayerData(playerId.toString(), data);
    }


    public static PersistentPlayerData getPlayerDataWithName(UUID playerId, String playerName) {
        PersistentPlayerData data = getPlayerData(playerId.toString());
        data.lastKnownPlayerName = playerName;
        savePlayerData(playerId.toString(), data);
        return data;
    }

    public static String findPlayerUuidByName(String playerName) {
        for (Map.Entry<String, PersistentPlayerData> entry : worldData.playerData.entrySet()) {
            if (playerName.equals(entry.getValue().lastKnownPlayerName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static int getCurrentEventPhase() {
        return worldData.currentEventPhase;
    }

    public static void setCurrentEventPhase(int phase) {
        worldData.currentEventPhase = phase;
        saveDataAsync();
    }

    public static void onPlayerJoin(UUID playerId) {
        PersistentPlayerData data = getPlayerData(playerId.toString());
        data.sessionsPlayed++;
        data.lastInteractionTime = System.currentTimeMillis();
        savePlayerData(playerId.toString(), data);
    }

    public static void onPlayerLeave(UUID playerId, long sessionDuration) {
        PersistentPlayerData data = getPlayerData(playerId.toString());
        data.totalPlayTime += sessionDuration;
        savePlayerData(playerId.toString(), data);
    }

    public static long getLastEventTime() {
        return worldData.lastEventTime;
    }

    // async save to prevent blocking
    private static void saveDataAsync() {
        new Thread(() -> {
            try {
                saveData();
            } catch (Exception e) {
                // silently handle save errors in async context
            }
        }).start();
    }
}
