package lol.cqllmetoxic.nullpointerentity.network;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * rracks per-player consent for sensitive local effects in multiplayer sessions.
 */
public final class ConsentState {
    private static final ConcurrentMap<UUID, Boolean> CONSENT = new ConcurrentHashMap<>();

    private ConsentState() {
    }

    public static void setConsent(UUID playerId, boolean consentGranted) {
        CONSENT.put(playerId, consentGranted);
    }

    public static boolean hasConsent(UUID playerId) {
        return CONSENT.getOrDefault(playerId, false);
    }

    public static void clear(UUID playerId) {
        CONSENT.remove(playerId);
    }
}

