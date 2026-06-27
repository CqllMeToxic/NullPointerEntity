package lol.cqllmetoxic.nullpointerentity.events;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import lol.cqllmetoxic.nullpointerentity.events.GlobalFreezeController;
import lol.cqllmetoxic.nullpointerentity.monitoring.BrowserHistoryReader;
import lol.cqllmetoxic.nullpointerentity.monitoring.LocationTracker;
import lol.cqllmetoxic.nullpointerentity.monitoring.SystemMonitor;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import lol.cqllmetoxic.nullpointerentity.ui.PopupManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.Scanner;

/**
 * handles the hostile/scary phase events (events 21-30).
 * nullpointer entity takes over from aurora.
 * reveals deep system access, browser history, location data, and system control.
 */
public class HostileEvents {
    private static String tr(String key, Object... args) {
        return Text.translatable(key, args).getString();
    }

    private static String pick(String... keys) {
        return tr(keys[(int) (Math.random() * keys.length)]);
    }

    private static String pickVariant(String baseKey, int variants, Object... args) {
        int index = 1 + (int) (Math.random() * variants);
        return tr(baseKey + "." + index, args);
    }

    private static void sendNullPointerMessageKey(ServerPlayerEntity player, String key, Object... args) {
        sendNullPointerMessage(player, tr(key, args));
    }

    private static void sendNullPointerMessageVariant(ServerPlayerEntity player, String baseKey, int variants, Object... args) {
        sendNullPointerMessage(player, pickVariant(baseKey, variants, args));
    }

    /**
     * triggers a specific hostile event by ID.
     *
     * @param eventId the event number (21-30)
     * @param player the target player
     */
    public static void triggerEvent(int eventId, ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        String currentTime = java.time.LocalTime.now().toString().substring(0, 5);

        switch (eventId) {
            case 31 -> { // nullpointerentity arrives - first physical appearance
                // end portal sound - loud as frick
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "block.end_portal.spawn")),
                    net.minecraft.sound.SoundCategory.MASTER, 1.0f, 0.6f);

                // first message - no intro, no name. just a statement.
                sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e31.arrival");

                // 2s - entity spawns behind the player + scream
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager.spawnTemporaryNullPointerEntity(player, 100); // 5 seconds visible

                        player.getServerWorld().playSound(null, player.getBlockPos(),
                            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
                            net.minecraft.sound.SoundCategory.MASTER, 1.0f, 0.7f);
                    }
                }, 2000);

                // 2.5s - blindness + heavy slowness kicks in while entity is still visible
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 60, 0, false, false, false));
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                            net.minecraft.entity.effect.StatusEffects.SLOWNESS, 60, 4, false, false, false));
                    }
                }, 2500);

                // 5s - blindness lifts, entity despawns, message lands into silence
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e31.after_spawn");
                    }
                }, 5500);

                // 7s - tense heartbeat starts, NPE pivots to showing what it can do
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        player.getServerWorld().playSound(null, player.getBlockPos(),
                            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_TENSE,
                            net.minecraft.sound.SoundCategory.MASTER, 0.6f, 1.0f);

                        // the process reveal + desktop report run on the player's own client
                        player.getServer().execute(() ->
                            lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 31));
                    }
                }, 7000);
            }

            case 32 -> { // ip tracking - the reveal runs on each player's own client (ClientEventExecutor)
                // play tense heartbeat - building suspense as location is revealed
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_TENSE,
                    net.minecraft.sound.SoundCategory.MASTER, 0.9f, 1.0f);

                // each client reads its own IP/location and shows it locally; data never comes back here
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 32);
            }

            case 33 -> // system analysis - reads each player's own system on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 33);

            case 34 -> // file system threat - writes to each player's own Documents on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 34);

            case 35 -> // digital haunting - changes each player's own wallpaper/files on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 35);

            case 36 -> // resource monitoring - reads each player's own memory on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 36);

            case 37 -> // camera surveillance - opens each player's own camera on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 37);

            case 38 -> // ultimate system takeover - writes to each player's own desktop on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 38);

            case 39 -> // network monitoring - reads each player's own IP/OBS on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 39);

            case 40 -> // final takeover - reads each player's own hardware on their client
                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 40);

            case 41 -> triggerMouthShutEvent(player);
            case 42 -> triggerRollbackEvent(player);
            case 43 -> triggerSpectatorEvent(player);
            case 44 -> triggerVoidWhispers(player);
            case 45 -> triggerFakeDisconnectEvent(player);

            default -> triggerEvent(31, player);
        }
    }

    // event 41: mouth shut - silences player chat for 60 seconds
    private static void triggerMouthShutEvent(ServerPlayerEntity player) {
        // store the suppression flag in persistent data so chat mixin can check it
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData data =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        data.triggeredEvents.put("chat_suppressed", true);
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), data);

        player.sendMessage(Text.translatable("event.nullpointerentity.hostile.mouth_shut").formatted(Formatting.WHITE), false);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData d =
                    lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
                d.triggeredEvents.put("chat_suppressed", false);
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), d);
                sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e41.voice_returned");
            }
        }, 60000);
    }

    // event 42: rollback - counts down then deletes recently placed blocks
    private static void triggerRollbackEvent(ServerPlayerEntity player) {
        sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e42.start");
        int[] countdown = {5, 4, 3, 2, 1};
        for (int i = 0; i < countdown.length; i++) {
            final int count = countdown[i];
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e42.countdown", count);
                }
            }, (i + 1) * 1000L);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // remove a small area of recently placed blocks around the player
                net.minecraft.util.math.BlockPos pos = player.getBlockPos();
                int removed = 0;
                for (int dx = -3; dx <= 3 && removed < 8; dx++) {
                    for (int dz = -3; dz <= 3 && removed < 8; dz++) {
                        for (int dy = -1; dy <= 3 && removed < 8; dy++) {
                            net.minecraft.util.math.BlockPos check = pos.add(dx, dy, dz);
                            net.minecraft.block.BlockState state = player.getServerWorld().getBlockState(check);
                            if (!state.isAir() && state.getBlock() != net.minecraft.block.Blocks.BEDROCK) {
                                player.getServerWorld().breakBlock(check, false);
                                removed++;
                            }
                        }
                    }
                }
                sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e42.end");
            }
        }, 6000);
    }

    // event 43: spectator - switches player to spectator for 10 seconds
    private static void triggerSpectatorEvent(ServerPlayerEntity player) {
        sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e43.start");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.changeGameMode(GameMode.SPECTATOR);
                // make the spectator crawl: a tiny fly speed so they can barely drift around
                // (changeGameMode sets default abilities first, so override + resync afterwards)
                player.getAbilities().setFlySpeed(0.008f);
                player.sendAbilitiesUpdate();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        player.changeGameMode(GameMode.SURVIVAL); // restores default abilities / fly speed
                        sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e43.end");
                    }
                }, 3000); // a lot shorter than before (was 10s)
            }
        }, 2000);
    }

    // event 44: void whispers
    private static void triggerVoidWhispers(ServerPlayerEntity player) {
        // play eerie whispering sounds that surround the player
        player.getServerWorld().playSound(null, player.getBlockPos(),
            net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "ambient.cave")),
            net.minecraft.sound.SoundCategory.AMBIENT, 1.0f, 0.5f);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.phantom.ambient")),
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.5f);
            }
        }, 1500);

        // send obfuscated messages (readable with ctrl + click copy text mods)
        String[] whispers = {
            tr("event.nullpointerentity.hostile.e44.whisper.1"),
            tr("event.nullpointerentity.hostile.e44.whisper.2"),
            tr("event.nullpointerentity.hostile.e44.whisper.3"),
            tr("event.nullpointerentity.hostile.e44.whisper.4")
        };
        
        for (int i = 0; i < 4; i++) {
            final int index = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    player.sendMessage(Text.literal(whispers[index]).formatted(Formatting.DARK_PURPLE), false);
                }
            }, 1000 * (i + 1));
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessageKey(player, "event.nullpointerentity.hostile.e44.end");
            }
        }, 5000);
    }

    // event 45: fake disconnect - shows a connection lost screen briefly
    private static void triggerFakeDisconnectEvent(ServerPlayerEntity player) {
        if (player.getServer() == null) {
            return;
        }

        GlobalFreezeController.startFreeze(5000L);
        broadcastFakeDisconnectVisual(player);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.getServer().execute(() -> {
                    for (ServerPlayerEntity online : player.getServer().getPlayerManager().getPlayerList()) {
                        sendNullPointerMessageKey(online, "event.nullpointerentity.hostile.e45.message");
                        // obviously you dont actually get DDoSed because that's a federal crime. thanks
                    }
                });
            }
        }, 5000);
    }

    private static void broadcastFakeDisconnectVisual(ServerPlayerEntity sourcePlayer) {
        sourcePlayer.getServer().execute(() -> {
            Text disconnectText = Text.translatable("event.nullpointerentity.hostile.fake_disconnect.title").formatted(Formatting.RED, Formatting.BOLD);
            Text reasonText = Text.translatable("event.nullpointerentity.hostile.fake_disconnect.reason").formatted(Formatting.GRAY);

            for (ServerPlayerEntity online : sourcePlayer.getServer().getPlayerManager().getPlayerList()) {
                online.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket(true));
                online.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(5, 90, 15));
                online.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(disconnectText));
                online.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(reasonText));

                // fallback in case title rendering is suppressed by client-side mods.
                online.sendMessage(Text.translatable("event.nullpointerentity.hostile.fake_disconnect.chat_title").formatted(Formatting.RED, Formatting.BOLD), false);
                online.sendMessage(Text.translatable("event.nullpointerentity.hostile.fake_disconnect.reason").formatted(Formatting.GRAY), false);
            }
        });
    }

    public static void triggerRandomHostileEvent(ServerPlayerEntity player) {
        int randomEvent = 31 + (int)(Math.random() * 15); // random number 31-45
        triggerEvent(randomEvent, player);
    }

    private static void sendNullPointerMessage(ServerPlayerEntity player, String message) {
        // process message through privacy manager to protect/randomize personal information
        String processedMessage = PrivacyManager.processEventMessage(message, 
            player.getName().getString(), 
            NullPointerEntity.getDisplayUsername());
            
        Text nullText = Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
                .append(Text.literal(processedMessage).formatted(Formatting.RED));
        player.sendMessage(nullText, false);
    }

    // helper methods to generate random location data for privacy protection
    private static String generateRandomIP() {
        return String.format("%d.%d.%d.%d",
            (int)(Math.random() * 255),
            (int)(Math.random() * 255),
            (int)(Math.random() * 255),
            (int)(Math.random() * 255));
    }

    private static String generateRandomCity() {
        String[] cities = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
            "San Antonio", "San Diego", "Dallas", "San Jose", "Austin", "Jacksonville",
            "Fort Worth", "Columbus", "Charlotte", "San Francisco", "Indianapolis",
            "Seattle", "Denver", "Washington", "Boston", "El Paso", "Nashville",
            "Detroit", "Oklahoma City", "Portland", "Las Vegas", "Memphis", "Louisville"
        };
        return cities[(int)(Math.random() * cities.length)];
    }

    private static String generateRandomRegion() {
        String[] regions = {
            "California", "Texas", "Florida", "New York", "Pennsylvania", "Illinois",
            "Ohio", "Georgia", "North Carolina", "Michigan", "New Jersey", "Virginia",
            "Washington", "Arizona", "Massachusetts", "Tennessee", "Indiana", "Missouri",
            "Maryland", "Wisconsin", "Colorado", "Minnesota", "South Carolina", "Alabama"
        };
        return regions[(int)(Math.random() * regions.length)];
    }

    private static String generateRandomISP() {
        String[] isps = {
            "Comcast Cable Communications", "Charter Communications", "Verizon Communications",
            "AT&T Services", "Cox Communications", "CenturyLink", "Frontier Communications",
            "Optimum", "Mediacom", "Windstream", "TDS Telecom", "Cincinnati Bell",
            "MetroNet", "WOW! Internet", "Rise Broadband", "Hawaiian Telcom"
        };
        return isps[(int)(Math.random() * isps.length)];
    }

    private static String generateRandomZipCode() {
        return String.format("%05d", (int)(Math.random() * 99999));
    }

    /**
     * checks if a process is currently running on the system.
     *
     * @param processName the name of the process (without .exe extension)
     * @return true if the process is running, false otherwise
     */
    private static boolean isProcessRunning(String processName) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // use processbuilder to check process
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + processName);
                Process process = processBuilder.start();

                Scanner scanner = new Scanner(process.getInputStream());
                while (scanner.hasNextLine()) {
                    if (scanner.nextLine().toLowerCase().contains(processName.replace(".exe", "").toLowerCase())) {
                        scanner.close();
                        return true;
                    }
                }
                scanner.close();
            }
        } catch (Exception e) {
            // ignore errors - this is optional functionality
        }
        return false;
    }
}
