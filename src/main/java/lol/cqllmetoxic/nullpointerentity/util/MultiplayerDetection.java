package lol.cqllmetoxic.nullpointerentity.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * detects whether the player is in singleplayer or multiplayer.
 * the mod gates its system-invasive features in multiplayer via the session policy
 * (see {@link lol.cqllmetoxic.nullpointerentity.network.MultiplayerPolicy} and
 * {@code ServerNetworking.resolvePolicy}) rather than disabling outright.
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
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return false;
        }

        // use reflection to avoid hard-linking client-only classes on dedicated servers.
        try {
            Class<?> minecraftClientClass = Class.forName("net.minecraft.client.MinecraftClient");
            Object client = minecraftClientClass.getMethod("getInstance").invoke(null);
            if (client == null) {
                return false;
            }

            Object integratedServer = minecraftClientClass.getMethod("getServer").invoke(client);
            if (integratedServer == null) {
                // not in a world yet (title screen) - treat as singleplayer for privacy screen purposes
                return false;
            }

            return !(Boolean) integratedServer.getClass().getMethod("isSingleplayer").invoke(integratedServer);
        } catch (Exception ignored) {
            return true;
        }
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
