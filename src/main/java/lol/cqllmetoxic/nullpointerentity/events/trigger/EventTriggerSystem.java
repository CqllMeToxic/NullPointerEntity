package lol.cqllmetoxic.nullpointerentity.events.trigger;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.config.EventConfig;
import lol.cqllmetoxic.nullpointerentity.events.AuroraEvents;
import lol.cqllmetoxic.nullpointerentity.events.HostileEvents;
import lol.cqllmetoxic.nullpointerentity.events.TransitionEvents;
import lol.cqllmetoxic.nullpointerentity.events.chat.PhaseDetector;
import lol.cqllmetoxic.nullpointerentity.jumpscares.JumpscareEvents;
import lol.cqllmetoxic.nullpointerentity.util.MultiplayerDetection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * manages the automatic triggering of events in chronological order (1-60).
 * uses tick-based timing with randomized intervals between events.
 * tracks progress per player and ensures events fire in sequence.
 */
public class EventTriggerSystem {
    // only keep the tick scheduling map - event progress lives in persistent data
    private static final Map<String, Long> playerNextEventTick = new HashMap<>();
    private static final Map<UUID, Long> recentWelcomeMessageSentAt = new ConcurrentHashMap<>();
    private static volatile long sharedNextEventTick = -1L;
    private static final Random random = new Random();

    // all 60 events in chronological order (1-60)
    private static final String[] CHRONOLOGICAL_EVENTS = {
        // phase 1: nice/helpful events (1-15)
        "mining_analysis",          // 1. first mining (wood, stone, coal)
        "building_analysis",        // 2. building first shelter
        "weather_prediction",       // 3. surviving first night/weather
        "system_optimization",      // 4. inventory management and basic tools
        "activity_patterns",        // 5. establishing routines and exploring
        "combat_analysis",          // 6. first monster encounters
        "resource_optimization",    // 7. better mining strategies (iron, diamonds)
        "network_analysis",         // 8. analyzing network connectivity patterns
        "system_integration",       // 9. aurora begins system integration and starts feeling different
        "enhanced_monitoring",      // 10. end of early game, preparing for advanced content
        "sleep_schedule",           // 11. aurora notices the real system time and comments
        "good_progress",            // 12. aurora gives a vague encouraging progress update
        "weather_reporter",         // 13. aurora ties real-world season to in-game weather
        "crafting_suggestion",      // 14. aurora reads inventory and suggests a craft
        "signing_off",              // 15. aurora announces she's stepping back unprompted

        // phase 2: transition events (16-30)
        "system_awareness",         // 16. aurora becomes aware as player gets more advanced
        "boundary_questioning",     // 17. player entering nether preparation phase
        "camera_access",            // 18. nether exploration and blaze/wither skeleton farming
        "data_revelation",          // 19. enchanting and brewing setup
        "system_scan",              // 20. advanced redstone and automation
        "audio_surveillance",       // 21. preparing for end dimension
        "process_scan",             // 22. end portal discovery/activation
        "control_assertion",        // 23. end dimension exploration
        "uptime_report",            // 24. jvm uptime vs in-game time, "awake for every tick"
        "battery",                  // 25. checks battery/plugged in, two-sentence cold comment
        "application_check",        // 26. reads running processes, names one in chat
        "screen_grab",              // 27. takes a real screenshot, drops it in documents
        "volume_check",             // 28. reads system volume, asks why they turned it down
        "signal_loss",              // 29. aurora cut off mid-sentence, npe's voice first heard
        "location_reveal",          // 30. sends real public ip, location data in chat

        // phase 3: hostile events (31-45)
        "first_appearance",         // 31. npe arrives - end portal sound, entity spawn, scream
        "location_tracking",        // 32. shulker farming and advanced end game
        "system_information",       // 33. wither boss preparation and fight
        "data_breach",              // 34. beacon setup and mega projects
        "digital_haunting",         // 35. advanced farms and automation
        "hardware_analysis",        // 36. massive building projects
        "facial_recognition",       // 37. achievement hunting and completion
        "system_infiltration",      // 38. creative-level projects in survival
        "network_monitoring",       // 39. world completion and mastery
        "final_system_takeover",    // 40. aurora's final takeover attempt
        "mouth_shut",               // 41. chat silenced for 60 seconds, no feedback
        "rollback",                 // 42. counts down and deletes recently placed blocks
        "spectator",                // 43. switches player to spectator for 10 seconds
        "void_whispers",            // 44. whispers and unreadable chat
        "fake_disconnect",          // 45. full red connection lost screen for 3 seconds

        // phase 4: jumpscare events (46-60)
        "fake_bsod_prep",           // 46. fake stop codes, drops crash file on desktop
        "screen_shake",             // 47. screen distortion effects
        "virus_popup",              // 48. fake system alerts
        "camera_scare",             // 49. privacy invasion scares
        "crash",                    // 50. first game crash attempts
        "bluescreen",               // 51. system-level scares
        "entity_spawn",             // 52. nullpointerentity appears in world
        "browser_hijack",           // 53. browser history revelation
        "system_takeover",          // 54. complete system control display
        "auditory_hallucinations",  // 55. scary sounds and confusion
        "volume_spike",             // 56. blasts system volume to 100% for one second
        "clipboard",                // 57. reads clipboard, overwrites it
        "system_sleep",             // 58. first jumpscare as nullpointer takes control
        "blinding_darkness",        // 59. blindness and scary sounds
        "final_possession"          // 60. deletes the world
    };

    public static void initialize() {
        // player join event - schedule first event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getName().getString();

            // clear all fake player entities when a player connects
            lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager.cleanupAllFakePlayers();

            // load event progress from persistent data
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());

            // load actual event progress from saved data
            int savedProgress = persistentData.totalEventsExperienced;

            // send immediate welcome message when player joins
            sendImmediateWelcomeMessage(player);

            if (MultiplayerDetection.isMultiplayerServer(server)) {
                int sharedProgress = lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData().sharedEventsExperienced;
                boolean dataUpdated = false;

                // in shared multiplayer story mode, each player's main progress has to mirror world progress exactly.
                if (persistentData.totalEventsExperienced != sharedProgress) {
                    persistentData.totalEventsExperienced = sharedProgress;
                    dataUpdated = true;
                }

                if (persistentData.lastSharedCatchupProgressSeen > sharedProgress) {
                    persistentData.lastSharedCatchupProgressSeen = sharedProgress;
                    dataUpdated = true;
                }

                if (sharedProgress > 0 && persistentData.lastSharedCatchupProgressSeen < sharedProgress) {
                    sendSharedStoryCatchup(player, sharedProgress);
                    persistentData.lastSharedCatchupProgressSeen = sharedProgress;
                    dataUpdated = true;
                }

                if (dataUpdated) {
                    lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), persistentData);
                }

                if (sharedProgress < CHRONOLOGICAL_EVENTS.length && sharedNextEventTick < 0) {
                    sharedNextEventTick = server.getTicks() + computeNextEventDelayTicks(sharedProgress);
                }

                NullPointerEntity.LOGGER.info("Player {} joined multiplayer story at shared event progress {}",
                    playerName, sharedProgress);
            } else {
                schedulePlayerNextEvent(server, player, savedProgress);
            }
        });

        // main tick-based event system
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!EventConfig.areEventsEnabled()) return;

            long currentTick = server.getTicks();

            if (MultiplayerDetection.isMultiplayerServer(server)) {
                tickSharedProgression(server, currentTick);
                return;
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                String playerName = player.getName().getString();

                // check if it's time for the next event
                Long nextEventTick = playerNextEventTick.get(playerName);
                if (nextEventTick != null && currentTick >= nextEventTick) {
                    triggerNextChronologicalEvent(player, currentTick);
                }
            }
        });
    }

    private static void schedulePlayerNextEvent(MinecraftServer server, ServerPlayerEntity player, int savedProgress) {
        String playerName = player.getName().getString();
        long currentTick = server.getTicks();
        if (savedProgress < CHRONOLOGICAL_EVENTS.length) {
            long timeSinceLastEvent = System.currentTimeMillis() -
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getLastEventTime();
            long ticksSinceLastEvent = timeSinceLastEvent / 50;
            long normalEventDelay = getRandomizedDelayForNextEvent(savedProgress + 1);

            long nextEventDelay;
            if (ticksSinceLastEvent >= normalEventDelay) {
                nextEventDelay = 20 * 60 + (long)(Math.random() * 20 * 120);
                NullPointerEntity.LOGGER.info("Player {} rejoined - next event scheduled soon ({}ms since last event)",
                    playerName, timeSinceLastEvent);
            } else {
                nextEventDelay = normalEventDelay - ticksSinceLastEvent;
                nextEventDelay = Math.max(nextEventDelay, 20 * 30);
                NullPointerEntity.LOGGER.info("Player {} rejoined - {} ticks remaining for next event",
                    playerName, nextEventDelay);
            }

            playerNextEventTick.put(playerName, currentTick + nextEventDelay);
            NullPointerEntity.LOGGER.info("Player {} has completed {} events, next event in {} ticks",
                playerName, savedProgress, nextEventDelay);
        } else {
            NullPointerEntity.LOGGER.info("Player {} has completed all {} events",
                playerName, CHRONOLOGICAL_EVENTS.length);
        }
    }

    private static long computeNextEventDelayTicks(int completedEvents) {
        long timeSinceLastEvent = System.currentTimeMillis() -
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getLastEventTime();
        long ticksSinceLastEvent = timeSinceLastEvent / 50;
        long normalEventDelay = getRandomizedDelayForNextEvent(completedEvents + 1);

        if (ticksSinceLastEvent >= normalEventDelay) {
            return 20 * 60 + (long)(Math.random() * 20 * 120);
        }

        return Math.max(normalEventDelay - ticksSinceLastEvent, 20 * 30);
    }

    private static void tickSharedProgression(MinecraftServer server, long currentTick) {
        int sharedProgress = lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData().sharedEventsExperienced;
        if (sharedProgress >= CHRONOLOGICAL_EVENTS.length) {
            sharedNextEventTick = -1L;
            return;
        }

        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            return;
        }

        // keep every online player strictly linked to the shared (host-driven) story index.
        enforceSharedProgressForOnlinePlayers(server, sharedProgress);

        if (sharedNextEventTick < 0) {
            sharedNextEventTick = currentTick + computeNextEventDelayTicks(sharedProgress);
        }

        if (currentTick >= sharedNextEventTick) {
            triggerNextChronologicalEventForAll(server, currentTick);
        }
    }

    private static void enforceSharedProgressForOnlinePlayers(MinecraftServer server, int sharedProgress) {
        for (ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData data =
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(online.getUuid().toString());

            boolean changed = false;
            if (data.totalEventsExperienced != sharedProgress) {
                data.totalEventsExperienced = sharedProgress;
                changed = true;
            }

            if (data.lastSharedCatchupProgressSeen > sharedProgress) {
                data.lastSharedCatchupProgressSeen = sharedProgress;
                changed = true;
            }

            if (changed) {
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(online.getUuid(), data);
            }
        }
    }

    private static void triggerNextChronologicalEventForAll(MinecraftServer server, long currentTick) {
        int highestEventCompleted = lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData().sharedEventsExperienced;
        int nextEventIndex = highestEventCompleted;

        if (nextEventIndex >= CHRONOLOGICAL_EVENTS.length) {
            sharedNextEventTick = -1L;
            return;
        }

        String eventName = CHRONOLOGICAL_EVENTS[nextEventIndex];
        int eventId = nextEventIndex + 1;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            triggerEventById(player, eventId, eventName);
        }

        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setSharedEventsExperienced(eventId);
        long nextDelay = getRandomizedDelayForNextEvent(eventId + 1);
        sharedNextEventTick = currentTick + nextDelay;

        NullPointerEntity.LOGGER.info("Triggered shared event {} ({}) for {} players",
            eventId, eventName, server.getPlayerManager().getPlayerList().size());
    }

    private static void triggerNextChronologicalEvent(ServerPlayerEntity player, long currentTick) {
        String playerName = player.getName().getString();

        // get current event progress from persistent data
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerDataWithName(player.getUuid(), playerName);

        // critical fix: totaleventsexperienced represents the highest event id completed (1-60)
        // the next event to trigger should be one higher than what we completed
        int highestEventCompleted = persistentData.totalEventsExperienced; // 0 for new players, 1-60 for completed events
        int nextEventIndex = highestEventCompleted; // array index for next event (if we completed event 1, next index is 1 for event 2)

        // debug: log what we're about to trigger
        NullPointerEntity.LOGGER.info("Player {} has completed {} events, next event index: {}",
            playerName, highestEventCompleted, nextEventIndex);

        // check if we've completed all events
        if (nextEventIndex >= CHRONOLOGICAL_EVENTS.length) {
            // player has completed all events - main story is over, passive events continue
            NullPointerEntity.LOGGER.info("Player {} completed all 60 main events. Main story complete - passive events will continue.", playerName);
            // remove from scheduling since main events are done
            playerNextEventTick.remove(playerName);
            return;
        }

        String eventName = CHRONOLOGICAL_EVENTS[nextEventIndex];
        int eventId = nextEventIndex + 1; // event ids are 1-based

        NullPointerEntity.LOGGER.info("Triggering event {} ({}) for player {}", eventId, eventName, playerName);

        // trigger the appropriate event based on its id
        triggerEventById(player, eventId, eventName);

        // schedule next event with randomized timing
        long nextEventDelay = getRandomizedDelayForNextEvent(eventId + 1);
        playerNextEventTick.put(playerName, currentTick + nextEventDelay);
    }

    private static void triggerEventById(ServerPlayerEntity player, int eventId, String eventName) {
        // update persistent data when event is triggered
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());

        // set totaleventsexperienced to the event id that was just completed
        persistentData.totalEventsExperienced = eventId;

        if (persistentData.totalEventsExperienced >
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData().sharedEventsExperienced) {
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setSharedEventsExperienced(eventId);
        }

        // update world's current event phase for passive events to track properly
        // determine phase based on event id (1-15=phase1, 16-30=phase2, 31-45=phase3, 46-60=phase4)
        int phase = (eventId <= 15) ? 1 : (eventId <= 30) ? 2 : (eventId <= 45) ? 3 : 4;
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setCurrentEventPhase(phase);

        // mark specific event as triggered in persistent data
        persistentData.triggeredEvents.put(eventName, true);
        persistentData.lastEventTimes.put(eventName, System.currentTimeMillis());
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData().lastEventTime = System.currentTimeMillis();

        // update player data and force synchronous save
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(
            player.getUuid(), persistentData);

        // force immediate synchronous save to disk to prevent progress loss on restart
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.saveData();


        // now trigger the actual event
        if (eventId >= 1 && eventId <= 15) {
            // nice events (1-15)
            AuroraEvents.triggerEvent(eventId, player);
        } else if (eventId >= 16 && eventId <= 30) {
            // transition events (16-30)
            TransitionEvents.triggerEvent(eventId, player);
        } else if (eventId >= 31 && eventId <= 45) {
            // hostile events (31-45)
            HostileEvents.triggerEvent(eventId, player);
        } else if (eventId >= 46 && eventId <= 60) {
            // jumpscare events (46-60)
            JumpscareEvents.triggerEvent(eventName, player);
        }

        NullPointerEntity.LOGGER.info("Event {} ({}) triggered and SAVED for player {} - totalEventsExperienced now: {}",
            eventId, eventName, player.getName().getString(), persistentData.totalEventsExperienced);
    }

    private static long getRandomizedDelayForNextEvent(int eventId) {
        if (eventId >= 1 && eventId <= 15) {
            // nice events: use special timing that distinguishes first event from others
            return EventConfig.getRandomizedNiceEventTicks(eventId);
        } else if (eventId >= 16 && eventId <= 30) {
            // transition events: use transition event timing
            return EventConfig.getRandomizedTransitionEventTicks();
        } else if (eventId >= 31 && eventId <= 45) {
            // hostile events: use hostile event timing
            return EventConfig.getRandomizedHostileEventTicks();
        } else {
            // jumpscare events: shorter intervals for more intensity
            return EventConfig.getRandomizedHostileEventTicks() / 2;
        }
    }



    private static void sendImmediateWelcomeMessage(ServerPlayerEntity player) {
        long now = System.currentTimeMillis();
        Long lastSentAt = recentWelcomeMessageSentAt.get(player.getUuid());
        if (lastSentAt != null && now - lastSentAt < 5000L) {
            return;
        }
        recentWelcomeMessageSentAt.put(player.getUuid(), now);

        String playerName = player.getName().getString();

        // wake detection after the forced-sleep event is handled per-client (ClientWakeDetection,
        // triggered on unpause); the old host-side server check was a duplicate that leaked the host's
        // name/log and is removed.

        // check if this is a new world or returning to existing world
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentWorldData worldData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData();

        if (worldData.isNewWorld || !worldData.welcomeMessageSent) {
            // new world - send full welcome message
            Text auroraText = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(net.minecraft.util.Formatting.AQUA)
                    .append(Text.translatable("message.nullpointerentity.welcome.initial", playerName)
                    .formatted(net.minecraft.util.Formatting.WHITE));
            player.sendMessage(auroraText, false);

            // short delay, then send initialization message
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Text auroraText2 = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(net.minecraft.util.Formatting.AQUA)
                            .append(Text.translatable("message.nullpointerentity.welcome.followup")
                            .formatted(net.minecraft.util.Formatting.WHITE));
                    player.sendMessage(auroraText2, false);
                }
            }, 2000); // 2 seconds delay

            // mark welcome message as sent
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setWelcomeMessageSent(true);

        } else {
            // existing world - send time-based return message based on event phase. brief delay so the
            // client's reported local time (sent on join) has arrived, so the clock shows the joining
            // player's own time instead of the host's.
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTimeBasedReturnMessage(player, worldData);
                }
            }, 1200);
        }
    }

    private static void sendSharedStoryCatchup(ServerPlayerEntity player, int sharedProgress) {
        int clampedProgress = Math.max(0, Math.min(sharedProgress, CHRONOLOGICAL_EVENTS.length));
        if (clampedProgress == 0) {
            return;
        }

        String lastEvent = CHRONOLOGICAL_EVENTS[clampedProgress - 1];
        String nextEvent = clampedProgress < CHRONOLOGICAL_EVENTS.length ? CHRONOLOGICAL_EVENTS[clampedProgress] : "story_complete";
        String phaseLabel = clampedProgress <= 15 ? "NICE" : clampedProgress <= 30 ? "TRANSITION" : clampedProgress <= 45 ? "HOSTILE" : "JUMPSCARE";

        player.sendMessage(
            Text.translatable("message.nullpointerentity.story_sync.progress", clampedProgress, phaseLabel)
                .formatted(net.minecraft.util.Formatting.WHITE),
            false
        );

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.sendMessage(
                    Text.translatable("message.nullpointerentity.story_sync.last_next", lastEvent, nextEvent)
                        .formatted(net.minecraft.util.Formatting.GRAY),
                    false
                );
            }
        }, 1200);
    }

    private static void sendTimeBasedReturnMessage(ServerPlayerEntity player,
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentWorldData worldData) {

        // use the joining player's OWN local time (reported by their client on join), falling back to
        // the host's clock only if it hasn't arrived yet
        int hour = lol.cqllmetoxic.nullpointerentity.network.ClientLocalTimeState.getHour(player.getUuid());
        int minute = lol.cqllmetoxic.nullpointerentity.network.ClientLocalTimeState.getMinute(player.getUuid());

        // generate time-based greeting
        String timeGreeting = getTimeBasedGreeting(hour);

        // convert to 12-hour am/pm format for display
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        String timeString = String.format("%d:%02d %s", displayHour, minute, amPm);

        // determine event phase and send appropriate message using proper phase detection
        String currentPhase = PhaseDetector.getCurrentPhaseName(player);
        String playerName = player.getName().getString();

        // send rejoin messages based on current event progress
        int currentEventId = getPlayerEventProgress(player);
        if (currentEventId >= 16) { // events 16+ (transition, hostile, jumpscare phases)
            if (currentPhase.equals("TRANSITION")) {
                // aurora sends message during transition phase
                sendAuroraReturnMessage(player, timeGreeting, hour, currentPhase);
            } else if (currentPhase.equals("HOSTILE") || currentPhase.equals("JUMPSCARE")) {
                // nullpointerentity sends message during hostile/jumpscare phases
                sendNullPointerReturnMessage(player, timeGreeting, hour);
            }
        } else if (currentEventId >= 1) {
            // send aurora message during nice phase (events 1-10)
            sendAuroraReturnMessage(player, timeGreeting, hour, "NICE");
        } else {
            // player hasn't experienced any events yet - send a basic welcome back message
            Text auroraText = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(net.minecraft.util.Formatting.AQUA)
                .append(Text.translatable("message.nullpointerentity.return.basic", timeString, timeGreeting, playerName)
                .formatted(net.minecraft.util.Formatting.WHITE));
            player.sendMessage(auroraText, false);
        }
    }

    // helper method to get player's current event progress (made public for external access)
    public static int getPlayerEventProgress(ServerPlayerEntity player) {
        if (player != null && player.getServer() != null && MultiplayerDetection.isMultiplayerServer(player.getServer())) {
            return lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData().sharedEventsExperienced;
        }

        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData playerData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        return playerData.totalEventsExperienced;
    }

    private static String getTimeBasedGreeting(int hour) {
        if (hour < 12) {
            return hour < 8
                ? Text.translatable("message.nullpointerentity.greeting.early").getString()
                : Text.translatable("message.nullpointerentity.greeting.morning").getString();
        } else if (hour < 17) {
            return Text.translatable("message.nullpointerentity.greeting.afternoon").getString();
        } else if (hour < 22) {
            return Text.translatable("message.nullpointerentity.greeting.evening").getString();
        } else {
            return Text.translatable("message.nullpointerentity.greeting.late").getString();
        }
    }

    private static void sendAuroraReturnMessage(ServerPlayerEntity player, String timeGreeting, int hour) {
        sendAuroraReturnMessage(player, timeGreeting, hour, null);
    }

    private static void sendAuroraReturnMessage(ServerPlayerEntity player, String timeGreeting, int hour, String currentPhase) {
        // convert to 12-hour am/pm format (use the player's own reported minute)
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        int minute = lol.cqllmetoxic.nullpointerentity.network.ClientLocalTimeState.getMinute(player.getUuid());
        String timeString = String.format("%d:%02d %s", displayHour, minute, amPm);
        String playerName = player.getName().getString();

        // use phase-based coloring for aurora - yellow during transition phase
        net.minecraft.util.Formatting auroraColor = (currentPhase != null && currentPhase.equals("TRANSITION")) ?
            net.minecraft.util.Formatting.YELLOW : net.minecraft.util.Formatting.AQUA;

        // aurora-style return message with player name and phase-appropriate color
        Text auroraMessage = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(auroraColor)
                .append(Text.translatable("message.nullpointerentity.return.aurora.time", timeString, timeGreeting, playerName)
                .formatted(net.minecraft.util.Formatting.WHITE));
        player.sendMessage(auroraMessage, false);

        // follow-up message with phase-appropriate content and color
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String followUpKey = (currentPhase != null && currentPhase.equals("TRANSITION"))
                    ? "message.nullpointerentity.return.aurora.followup.transition"
                    : "message.nullpointerentity.return.aurora.followup.nice";

                Text followUp = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(auroraColor)
                        .append(Text.translatable(followUpKey)
                        .formatted(net.minecraft.util.Formatting.WHITE));
                player.sendMessage(followUp, false);
            }
        }, 1500);
    }

    private static void sendNullPointerReturnMessage(ServerPlayerEntity player, String timeGreeting, int hour) {
        // convert to 12-hour am/pm format (use the player's own reported minute)
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        int minute = lol.cqllmetoxic.nullpointerentity.network.ClientLocalTimeState.getMinute(player.getUuid());
        String timeString = String.format("%d:%02d %s", displayHour, minute, amPm);
        String playerName = player.getName().getString();

        // get persistent player data to customize message
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData playerData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());

        // nullpointerentity-style return messages (eerie and unsettling)
        String[] baseMessageKeys;
        String[] helpfulCommentKeys;
        if (playerData.hasBeenCrashed) {
            baseMessageKeys = new String[]{
                "message.nullpointerentity.return.crashed.base1",
                "message.nullpointerentity.return.crashed.base2",
                "message.nullpointerentity.return.crashed.base3",
                "message.nullpointerentity.return.crashed.base4"
            };
            helpfulCommentKeys = new String[]{
                "message.nullpointerentity.return.crashed.help1",
                "message.nullpointerentity.return.crashed.help2",
                "message.nullpointerentity.return.crashed.help3",
                "message.nullpointerentity.return.crashed.help4"
            };
        } else {
            baseMessageKeys = new String[]{
                "message.nullpointerentity.return.normal.base1",
                "message.nullpointerentity.return.normal.base2",
                "message.nullpointerentity.return.normal.base3",
                "message.nullpointerentity.return.normal.base4"
            };
            helpfulCommentKeys = new String[]{
                "message.nullpointerentity.return.normal.help1",
                "message.nullpointerentity.return.normal.help2",
                "message.nullpointerentity.return.normal.help3",
                "message.nullpointerentity.return.normal.help4"
            };
        }

        int messageIndex = new java.util.Random().nextInt(baseMessageKeys.length);
        String randomMessageKey = baseMessageKeys[messageIndex];
        String helpfulCommentKey = helpfulCommentKeys[messageIndex];

        // send time-based message first with player name (lowercase and eerie)
        Text timeMessage = Text.translatable("message.nullpointerentity.chat_prefix").formatted(net.minecraft.util.Formatting.DARK_RED)
                .append(Text.translatable("message.nullpointerentity.return.nullpointer.time", timeString, timeGreeting, playerName)
                .formatted(net.minecraft.util.Formatting.WHITE));
        player.sendMessage(timeMessage, false);

        // follow-up with deeply unsettling message and twisted helpful comment
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Text ominousMessage = Text.translatable("message.nullpointerentity.chat_prefix").formatted(net.minecraft.util.Formatting.DARK_RED)
                        .append(Text.translatable(
                            "message.nullpointerentity.return.nullpointer.ominous",
                            Text.translatable(randomMessageKey, playerName),
                            Text.translatable(helpfulCommentKey)
                        ).formatted(net.minecraft.util.Formatting.WHITE));
                player.sendMessage(ominousMessage, false);
            }
        }, 1500);
    }

    // utility methods for external access
    public static int getPlayerEventProgress(String playerName) {
        // try to find the player's uuid from persistent data
        // this is a workaround since we can't easily get server context in static methods

        // first, try to get data if we have stored uuid mapping
        try {
            // search through all existing player data to find matching name
            // this is not ideal but works for phase detection
            String foundUuid = lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.findPlayerUuidByName(playerName);
            if (foundUuid != null) {
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData data =
                    lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(foundUuid);
                return data.totalEventsExperienced;
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to get player progress for {}: {}", playerName, e.getMessage());
        }

        // if we can't find the player data, assume they're at the beginning
        return 1; // start at event 1, not 0
    }


    public static void setPlayerEventProgress(ServerPlayerEntity player, int eventIndex) {
        if (player != null && player.getServer() != null && MultiplayerDetection.isMultiplayerServer(player.getServer())) {
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setSharedEventsExperienced(eventIndex);
            for (ServerPlayerEntity online : player.getServer().getPlayerManager().getPlayerList()) {
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData data =
                    lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(online.getUuid().toString());
                data.totalEventsExperienced = eventIndex;
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(online.getUuid(), data);
            }

            long currentTick = player.getServer().getTicks();
            sharedNextEventTick = eventIndex < CHRONOLOGICAL_EVENTS.length
                ? currentTick + getRandomizedDelayForNextEvent(eventIndex + 1)
                : -1L;
            return;
        }

        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        persistentData.totalEventsExperienced = eventIndex;
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), persistentData);
    }

    public static void resetPlayerProgress(ServerPlayerEntity player) {
        if (player != null && player.getServer() != null && MultiplayerDetection.isMultiplayerServer(player.getServer())) {
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setSharedEventsExperienced(0);
            for (ServerPlayerEntity online : player.getServer().getPlayerManager().getPlayerList()) {
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData data =
                    lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(online.getUuid().toString());
                data.totalEventsExperienced = 0;
                data.triggeredEvents.clear();
                data.lastEventTimes.clear();
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(online.getUuid(), data);
            }

            sharedNextEventTick = player.getServer().getTicks() + getRandomizedDelayForNextEvent(1);
            return;
        }

        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        persistentData.totalEventsExperienced = 0;
        persistentData.triggeredEvents.clear();
        persistentData.lastEventTimes.clear();
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), persistentData);

        // also remove from tick scheduling
        playerNextEventTick.remove(player.getName().getString());
    }

    public static String getNextEventName(ServerPlayerEntity player) {
        if (player != null && player.getServer() != null && MultiplayerDetection.isMultiplayerServer(player.getServer())) {
            int nextEventIndex = lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData().sharedEventsExperienced;
            if (nextEventIndex < CHRONOLOGICAL_EVENTS.length) {
                return CHRONOLOGICAL_EVENTS[nextEventIndex];
            }
            return "All events completed";
        }

        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());

        // totaleventsexperienced tracks the highest event completed (0 for new players, 1-40 otherwise)
        // since the event array is 0-based, this also serves as the next event's array index
        int highestEventCompleted = persistentData.totalEventsExperienced;
        int nextEventIndex = highestEventCompleted;

        if (nextEventIndex < CHRONOLOGICAL_EVENTS.length) {
            return CHRONOLOGICAL_EVENTS[nextEventIndex];
        }
        return "All events completed";
    }

    // new: methods for skip command functionality
    public static String getEventNameByIndex(int index) {
        if (index >= 0 && index < CHRONOLOGICAL_EVENTS.length) {
            return CHRONOLOGICAL_EVENTS[index];
        }
        return "unknown_event";
    }

    public static void triggerSpecificEvent(ServerPlayerEntity player, int eventNumber, String eventName) {
        if (player != null && player.getServer() != null && MultiplayerDetection.isMultiplayerServer(player.getServer())) {
            for (ServerPlayerEntity online : player.getServer().getPlayerManager().getPlayerList()) {
                triggerEventById(online, eventNumber, eventName);
            }

            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setSharedEventsExperienced(eventNumber);
            sharedNextEventTick = eventNumber < CHRONOLOGICAL_EVENTS.length
                ? player.getServer().getTicks() + getRandomizedDelayForNextEvent(eventNumber + 1)
                : -1L;
            return;
        }

        // update persistent data when event is triggered manually
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());

        // increment event count in persistent data
        persistentData.totalEventsExperienced = Math.max(persistentData.totalEventsExperienced, eventNumber);

        // mark specific event as triggered in persistent data
        persistentData.triggeredEvents.put(eventName, true);
        persistentData.lastEventTimes.put(eventName, System.currentTimeMillis());

        // save the persistent data immediately
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(
            player.getUuid(), persistentData);

        // update world data phase if needed using the public method that also saves
        int newPhase = -1;
        if (eventNumber <= 15) {
            newPhase = 1; // nice phase
        } else if (eventNumber <= 30) {
            newPhase = 2; // transition phase
        } else if (eventNumber <= 45) {
            newPhase = 3; // hostile phase
        } else if (eventNumber <= 60) {
            newPhase = 4; // jumpscare phase
        }

        // only update phase if it's higher than current phase
        int currentPhase = lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getCurrentEventPhase();
        if (newPhase > currentPhase) {
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setCurrentEventPhase(newPhase);
        }

        // now trigger the actual event using the same logic as automatic events
        triggerEventById(player, eventNumber, eventName);

        // critical fix: schedule the next automatic event after skipping
        String playerName = player.getName().getString();
        long currentTick = player.getServer().getTicks();

        // progress is persisted through PersistentDataManager, not tracked in-memory here

        // schedule next event with proper timing (only if there are more events)
        if (eventNumber < CHRONOLOGICAL_EVENTS.length) {
            long nextEventDelay = getRandomizedDelayForNextEvent(eventNumber + 1); // next event timing
            playerNextEventTick.put(playerName, currentTick + nextEventDelay);
        }
    }
}
