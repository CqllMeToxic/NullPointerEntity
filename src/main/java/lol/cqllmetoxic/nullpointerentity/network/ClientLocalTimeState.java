package lol.cqllmetoxic.nullpointerentity.network;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * server-side cache of each player's own local wall-clock time, reported by their client on join
 * (see {@link lol.cqllmetoxic.nullpointerentity.network.payload.ClientLocalTimePayload}). used so
 * "welcome back" return messages show the joining player's local time instead of the host's clock.
 */
public final class ClientLocalTimeState {
    /** hour (0-23) and minute (0-59) packed as hour * 60 + minute */
    private static final ConcurrentHashMap<UUID, Integer> MINUTES_OF_DAY = new ConcurrentHashMap<>();

    private ClientLocalTimeState() {
    }

    public static void set(UUID playerId, int hour, int minute) {
        if (playerId == null) {
            return;
        }
        int h = ((hour % 24) + 24) % 24;
        int m = ((minute % 60) + 60) % 60;
        MINUTES_OF_DAY.put(playerId, h * 60 + m);
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            MINUTES_OF_DAY.remove(playerId);
        }
    }

    /** the player's reported hour (0-23), or the server's local hour if none was reported yet. */
    public static int getHour(UUID playerId) {
        Integer packed = playerId != null ? MINUTES_OF_DAY.get(playerId) : null;
        return packed != null ? packed / 60 : java.time.LocalTime.now().getHour();
    }

    /** the player's reported minute (0-59), or the server's local minute if none was reported yet. */
    public static int getMinute(UUID playerId) {
        Integer packed = playerId != null ? MINUTES_OF_DAY.get(playerId) : null;
        return packed != null ? packed % 60 : java.time.LocalTime.now().getMinute();
    }
}
