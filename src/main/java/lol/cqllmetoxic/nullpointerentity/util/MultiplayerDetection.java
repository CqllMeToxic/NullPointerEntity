package lol.cqllmetoxic.nullpointerentity.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * detects whether the player is in singleplayer or multiplayer.
 * the mod disables itself in multiplayer.
 */
public class MultiplayerDetection {

    /**
     * checks if the server is running in multiplayer mode.
     * considers dedicated servers and LAN games with multiple players as multiplayer.
     *
     * @param server the minecraft server instance
     * @return true if multiplayer, false if singleplayer
     */
    public static boolean isMultiplayerServer(MinecraftServer server) {
        if (server == null) return false;

        if (server.isDedicated()) {
            return true;
        }

        return !server.isSingleplayer() || server.getPlayerManager().getPlayerList().size() > 1;
    }

    /**
     * client-side check for multiplayer mode.
     * looks at whether we're connected to a remote server.
     *
     * @return true if connected to multiplayer server
     */
    public static boolean isMultiplayerClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return false;

        if (client.getServer() == null) {
            return true;
        }

        return !client.getServer().isSingleplayer();
    }

    /**
     * checks if a specific player is in a multiplayer environment.
     *
     * @param player the player to check
     * @return true if player is in multiplayer
     */
    public static boolean isPlayerInMultiplayer(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return false;
        return isMultiplayerServer(player.getServer());
    }

    /**
     * determines if the mod should disable itself for this server.
     *
     * @param server the server instance
     * @return true if mod should be disabled (multiplayer detected)
     */
    public static boolean shouldDisableMod(MinecraftServer server) {
        return isMultiplayerServer(server);
    }

    /**
     * determines if the mod should disable itself for a specific player.
     *
     * @param player the player to check
     * @return true if mod should be disabled for this player
     */
    public static boolean shouldDisableModForPlayer(ServerPlayerEntity player) {
        return isPlayerInMultiplayer(player);
    }

    /**
     * returns a string describing the environment type.
     *
     * @param server the server instance
     * @return "MULTIPLAYER" or "SINGLE PLAYER"
     */
    public static String getEnvironmentType(MinecraftServer server) {
        return isMultiplayerServer(server) ? "MULTIPLAYER" : "SINGLE PLAYER";
    }

    /**
     * client-side environment type check.
     *
     * @return "MULTIPLAYER" or "SINGLE PLAYER"
     */
    public static String getEnvironmentTypeClient() {
        return isMultiplayerClient() ? "MULTIPLAYER" : "SINGLE PLAYER";
    }
}
