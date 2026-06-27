package lol.cqllmetoxic.nullpointerentity.events;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.config.VoiceChatConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.pryzmm.api.ShriekApi;
import com.pryzmm.api.events.ServerPlayerTalkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * bridges Shriek mic transcripts into the existing chat response pipeline.
 */
public class ShriekSpeechBridge {
    private static final Map<UUID, Long> LAST_PROCESSED_AT = new ConcurrentHashMap<>();
    private static final long MIN_INTERVAL_MS = 1250L;
    
    // shriek transcripts get forwarded into the central ChatResponseSystem, which
    // handles keyword detection, cooldowns, special responses, and phase-based replies.

    public static void playerSpeaks(ServerPlayerTalkEvent event) {
        if (event.getPlayer() == null || event.getPlayer().getServer() == null) {
            return;
        }

        // note: do NOT gate this on Privacy Mode / consent. talking to AURORA is player input, not a
        // data-exposing action - Privacy Mode only anonymizes info that's shown, it never blocks the
        // mic. gating here is what made voice silently stop working with Privacy Mode on (and stay
        // broken even after toggling it off, since the transcript was dropped before reaching chat).

        String text = event.getText();
        if (text == null || text.isBlank()) {
            return;
        }

        UUID playerId = event.getPlayer().getUuid();
        long now = System.currentTimeMillis();
        Long lastProcessed = LAST_PROCESSED_AT.get(playerId);
        if (lastProcessed != null && now - lastProcessed < MIN_INTERVAL_MS) {
            return;
        }
        LAST_PROCESSED_AT.put(playerId, now);

        // hand it off to ChatResponseSystem which does keyword detection, cooldowns,
        // special handling, and the whole response pipeline async.
        try {
            ChatResponseSystem.handleChatMessage(event.getPlayer(), text.trim());
        } catch (Exception e) {
            // if something breaks, just broadcast the raw transcript so nothing's lost.
            NullPointerEntity.LOGGER.warn("ChatResponseSystem failed for Shriek speech, falling back to broadcast", e);
            submitChatMessage(event.getPlayer(), text.trim());
        }
    }

    private static void submitChatMessage(ServerPlayerEntity player, String message) {
        try {
            // broadcast a clean chat message like: "<Player> Message"
            Text fullMessage = Text.literal("<" + player.getGameProfile().getName() + "> " + message);
            if (player.getServer() != null) {
                player.getServer().getPlayerManager().broadcast(fullMessage, false);
                NullPointerEntity.LOGGER.info("Shriek speech broadcast: {} said: {}", player.getGameProfile().getName(), message);
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to broadcast Shriek speech", e);
        }
    }

    public static void register() {
        ShriekApi.registerServerPlayerSpeechListener(ShriekSpeechBridge::playerSpeaks);
        ShriekApi.setPrintToConsole(false);
    }

    public static void initialize() {
        if (!VoiceChatConfig.isVoiceChatEnabled()) {
            NullPointerEntity.LOGGER.info("Shriek voice chat integration disabled in config");
            return;
        }

        if (FabricLoader.getInstance().isModLoaded("shriek") && FabricLoader.getInstance().isModLoaded("architectury")) {
            register();
            NullPointerEntity.LOGGER.info("Shriek speech bridge initialized successfully");
        } else {
            NullPointerEntity.LOGGER.info("Shriek or Architectury not installed - voice chat disabled");
        }
    }

    public static void onPlayerDisconnect(UUID playerId) {
        LAST_PROCESSED_AT.remove(playerId);
    }
}
