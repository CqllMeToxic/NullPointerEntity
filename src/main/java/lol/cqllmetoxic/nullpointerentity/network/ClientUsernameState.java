package lol.cqllmetoxic.nullpointerentity.network;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * server-side cache of each player's own display username, reported by their client (see
 * {@link lol.cqllmetoxic.nullpointerentity.network.payload.ClientUsernamePayload}). used so that
 * server-side event chat/files name each player's own machine instead of the host's. the value is
 * already privacy-processed on the client: an anonymized "User####" while the session's Privacy Mode
 * is on, the real OS username only when it's off.
 */
public final class ClientUsernameState {
    private static final ConcurrentHashMap<UUID, String> DISPLAY_NAMES = new ConcurrentHashMap<>();

    private ClientUsernameState() {
    }

    public static void set(UUID playerId, String displayName) {
        if (playerId != null && displayName != null && !displayName.isEmpty()) {
            DISPLAY_NAMES.put(playerId, displayName);
        }
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            DISPLAY_NAMES.remove(playerId);
        }
    }

    /** the player's reported display name, or {@code null} if their client hasn't reported one yet. */
    public static String get(UUID playerId) {
        return playerId != null ? DISPLAY_NAMES.get(playerId) : null;
    }
}
