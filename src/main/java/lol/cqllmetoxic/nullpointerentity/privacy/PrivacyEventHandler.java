package lol.cqllmetoxic.nullpointerentity.privacy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

/**
 * handles showing the privacy configuration screen on first world join.
 * tracks which worlds have seen the privacy screen to avoid showing it multiple times.
 * loads and saves privacy configuration.
 */
@Environment(EnvType.CLIENT)
public class PrivacyEventHandler {
    private static boolean hasShownPrivacyScreen = false;
    private static int ticksAfterJoin = 0;
    private static final int TICKS_BEFORE_SHOWING = 20;
    private static String currentWorldName = null;

    /**
     * initializes privacy event handling on the client.
     */
    public static void initialize() {
        // load privacy configuration when client starts
        PrivacyManager.loadConfig();

        NullPointerEntity.LOGGER.info("Privacy Event Handler initialized");

        // reset flag when joining a world and get world name
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            hasShownPrivacyScreen = false;
            ticksAfterJoin = 0;

            // get the world name with better detection
            currentWorldName = getWorldName(client);

            NullPointerEntity.LOGGER.info("Joined world: '{}' - Privacy screen needed: {}",
                currentWorldName, PrivacyManager.shouldShowPrivacyScreen(currentWorldName));
        });

        // check if we should show privacy screen on tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null && !hasShownPrivacyScreen && currentWorldName != null) {
                ticksAfterJoin++;
                
                // show privacy screen only if this world hasn't been processed before
                if (ticksAfterJoin >= TICKS_BEFORE_SHOWING && PrivacyManager.shouldShowPrivacyScreen(currentWorldName)) {
                    showPrivacyScreen(client);
                    hasShownPrivacyScreen = true;
                    NullPointerEntity.LOGGER.info("Showing privacy screen for world: '{}'", currentWorldName);
                } else if (ticksAfterJoin == TICKS_BEFORE_SHOWING + 20) {
                    // log why we're not showing the screen after some time
                    NullPointerEntity.LOGGER.info("Privacy screen not shown - already processed: {}",
                        PrivacyManager.isWorldProcessed(currentWorldName));
                }
            }
        });
    }

    private static String getWorldName(MinecraftClient client) {
        // try multiple methods to get world name
        String worldName = null;

        // method 1: integrated server (singleplayer)
        if (client.getServer() != null && client.getServer().getSaveProperties() != null) {
            worldName = client.getServer().getSaveProperties().getLevelName();
            if (worldName != null && !worldName.isEmpty()) {
                NullPointerEntity.LOGGER.info("Got world name from integrated server: '{}'", worldName);
                return worldName;
            }
        }

        // method 2: try to get from current server data
        if (client.getCurrentServerEntry() != null) {
            worldName = client.getCurrentServerEntry().address;
            if (worldName != null && !worldName.isEmpty()) {
                worldName = "server_" + worldName.replace(":", "_").replace(".", "_");
                NullPointerEntity.LOGGER.info("Got world name from server entry: '{}'", worldName);
                return worldName;
            }
        }

        // method 3: generate based on current time for multiplayer/unknown worlds
        if (worldName == null || worldName.isEmpty()) {
            // use a combination of timestamp for unique world identification
            long timestamp = System.currentTimeMillis() / 30000; // 30 second intervals to group quick rejoins
            worldName = "world_" + timestamp;
            NullPointerEntity.LOGGER.info("Generated fallback world name: '{}'", worldName);
        }

        return worldName;
    }

    private static void showPrivacyScreen(MinecraftClient client) {
        // make sure we're not interrupting important screens
        if (client.currentScreen == null ||
            client.currentScreen instanceof TitleScreen ||
            !(client.currentScreen instanceof CreateWorldScreen) &&
            !(client.currentScreen instanceof SelectWorldScreen)) {

            NullPointerEntity.LOGGER.info("Attempting to show privacy screen...");
            client.setScreen(new PrivacyScreen(client.currentScreen));
        } else {
            // wait a bit more if there's another important screen open
            ticksAfterJoin = TICKS_BEFORE_SHOWING - 10;
            NullPointerEntity.LOGGER.info("Delaying privacy screen - another screen is open: {}",
                client.currentScreen.getClass().getSimpleName());
        }
    }

    // force show privacy screen (for testing or manual trigger)
    public static void forceShowPrivacyScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            currentWorldName = getWorldName(client);
            NullPointerEntity.LOGGER.info("Force showing privacy screen for world: '{}'", currentWorldName);
            client.setScreen(new PrivacyScreen(client.currentScreen));
        }
    }

    // reset the privacy screen flag (for testing)
    public static void resetPrivacyScreenFlag() {
        hasShownPrivacyScreen = false;
        ticksAfterJoin = 0;
        NullPointerEntity.LOGGER.info("Privacy screen flag reset");
    }
}
