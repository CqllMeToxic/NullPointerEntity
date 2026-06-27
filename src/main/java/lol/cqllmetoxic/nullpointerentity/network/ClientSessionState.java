package lol.cqllmetoxic.nullpointerentity.network;

/**
 * client-side multiplayer session state populated from server packets.
 */
public final class ClientSessionState {
    private static volatile MultiplayerPolicy currentPolicy = MultiplayerPolicy.SAFE_ONLY;
    private static volatile boolean serverModPresent = false;

    private ClientSessionState() {
    }

    public static MultiplayerPolicy getCurrentPolicy() {
        return currentPolicy;
    }

    public static void setCurrentPolicy(MultiplayerPolicy policy) {
        currentPolicy = policy;
    }

    public static boolean isServerModPresent() {
        return serverModPresent;
    }

    public static void setServerModPresent(boolean present) {
        serverModPresent = present;
    }
}
