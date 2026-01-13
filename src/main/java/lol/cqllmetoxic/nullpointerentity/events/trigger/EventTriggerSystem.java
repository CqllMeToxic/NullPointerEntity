package lol.cqllmetoxic.nullpointerentity.events.trigger;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.config.EventConfig;
import lol.cqllmetoxic.nullpointerentity.events.AuroraEvents;
import lol.cqllmetoxic.nullpointerentity.events.HostileEvents;
import lol.cqllmetoxic.nullpointerentity.events.TransitionEvents;
import lol.cqllmetoxic.nullpointerentity.events.chat.PhaseDetector;
import lol.cqllmetoxic.nullpointerentity.jumpscares.JumpscareEvents;
import lol.cqllmetoxic.nullpointerentity.system.WakeDetection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * manages the automatic triggering of events in chronological order (1-40).
 * uses tick-based timing with randomized intervals between events.
 * tracks progress per player and ensures events fire in sequence.
 */
public class EventTriggerSystem {
    // only keep the tick scheduling map - event progress is now stored in persistent data
    private static final Map<String, Long> playerNextEventTick = new HashMap<>();
    private static final Random random = new Random();

    // all 40 events in chronological order (1-40)
    private static final String[] CHRONOLOGICAL_EVENTS = {
        // phase 1: nice/helpful events (1-10)
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

        // phase 2: transition events (11-20)
        "system_awareness",         // 11. aurora becomes aware as player gets more advanced
        "boundary_questioning",     // 12. player entering nether preparation phase
        "data_revelation",          // 13. nether exploration and blaze/wither skeleton farming
        "growing_influence",        // 14. enchanting and brewing setup
        "system_scan",              // 15. advanced redstone and automation
        "boundary_dissolution",     // 16. preparing for end dimension
        "full_awareness",           // 17. end portal discovery/activation
        "control_assertion",        // 18. end dimension exploration
        "final_transition",         // 19. ender dragon fight preparation
        "browser_invasion",         // 20. post-dragon, entering end game content

        // phase 3: hostile events (21-30)
        "process_monitoring",       // 21. end city exploration and elytra
        "location_tracking",        // 22. shulker farming and advanced end game
        "realtime_monitoring",      // 23. wither boss preparation and fight
        "complete_surveillance",    // 24. beacon setup and mega projects
        "browser_targeting",        // 25. advanced farms and automation
        "hardware_analysis",        // 26. massive building projects
        "behavioral_analysis",      // 27. achievement hunting and completion
        "system_infiltration",     // 28. creative-level projects in survival
        "digital_dominance",        // 29. world completion and mastery
        "final_system_takeover",    // 30. aurora's final takeover attempt

        // phase 4: jumpscare events (31-40)
        "system_sleep",             // 31. first jumpscare as nullpointer takes control
        "screen_shake",             // 32. screen distortion effects
        "virus_popup",              // 33. fake system alerts
        "camera_scare",             // 34. privacy invasion scares
        "crash",                    // 35. first game crash attempts
        "bluescreen",               // 36. system-level scares
        "entity_spawn",             // 37. nullpointerentity appears in world
        "browser_hijack",           // 38. browser history revelation
        "system_takeover",          // 39. complete system control display
        "final_possession"          // 40. final horror climax
    };

    public static void initialize() {
        // player join event - schedule first event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getName().getString();

            // load event progress from persistent data
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());

            // load actual event progress from saved data
            int savedProgress = persistentData.totalEventsExperienced;

            // send immediate welcome message when player joins
            sendImmediateWelcomeMessage(player);

            // schedule next event based on current progress
            long currentTick = server.getTicks();
            if (savedProgress < CHRONOLOGICAL_EVENTS.length) {
                // critical fix: check if enough time has passed since last event
                long timeSinceLastEvent = System.currentTimeMillis() -
                    lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getLastEventTime();

                // convert to ticks (20 ticks per second, 1000 ms per second)
                long ticksSinceLastEvent = timeSinceLastEvent / 50; // 50ms per tick

                // get the normal delay for the next event
                long normalEventDelay = getRandomizedDelayForNextEvent(savedProgress + 1);

                // if enough time has passed, trigger the event soon (within 1-3 minutes)
                // otherwise, use the remaining time
                long nextEventDelay;
                if (ticksSinceLastEvent >= normalEventDelay) {
                    // enough time has passed - trigger event soon after rejoining
                    nextEventDelay = 20 * 60 + (long)(Math.random() * 20 * 120); // 1-3 minutes
                    NullPointerEntity.LOGGER.info("Player {} rejoined - next event scheduled soon ({}ms since last event)",
                        playerName, timeSinceLastEvent);
                } else {
                    // not enough time has passed - use remaining time
                    nextEventDelay = normalEventDelay - ticksSinceLastEvent;
                    nextEventDelay = Math.max(nextEventDelay, 20 * 30); // minimum 30 seconds
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
        });

        // main tick-based event system
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!EventConfig.areEventsEnabled()) return;

            long currentTick = server.getTicks();

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

    private static void triggerNextChronologicalEvent(ServerPlayerEntity player, long currentTick) {
        String playerName = player.getName().getString();

        // get current event progress from persistent data
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerDataWithName(player.getUuid(), playerName);

        // critical fix: totaleventsexperienced represents the highest event id completed (1-40)
        // the next event to trigger should be one higher than what we completed
        int highestEventCompleted = persistentData.totalEventsExperienced; // 0 for new players, 1-40 for completed events
        int nextEventIndex = highestEventCompleted; // array index for next event (if we completed event 1, next index is 1 for event 2)

        // debug: log what we're about to trigger
        NullPointerEntity.LOGGER.info("Player {} has completed {} events, next event index: {}",
            playerName, highestEventCompleted, nextEventIndex);

        // check if we've completed all events
        if (nextEventIndex >= CHRONOLOGICAL_EVENTS.length) {
            // player has completed all events - main story is over, passive events continue
            NullPointerEntity.LOGGER.info("Player {} completed all 40 main events. Main story complete - passive events will continue.", playerName);
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

        // update world's current event phase for passive events to track properly
        // determine phase based on event id (1-10=phase1, 11-20=phase2, 21-30=phase3, 31-40=phase4)
        int phase = (eventId <= 10) ? 1 : (eventId <= 20) ? 2 : (eventId <= 30) ? 3 : 4;
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setCurrentEventPhase(phase);

        // mark specific event as triggered in persistent data
        persistentData.triggeredEvents.put(eventName, true);
        persistentData.lastEventTimes.put(eventName, System.currentTimeMillis());

        // update player data and force synchronous save
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(
            player.getUuid(), persistentData);

        // force immediate synchronous save to disk to prevent progress loss on restart
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.saveData();


        // now trigger the actual event
        if (eventId >= 1 && eventId <= 10) {
            // nice events (1-10)
            AuroraEvents.triggerEvent(eventId, player);
        } else if (eventId >= 11 && eventId <= 20) {
            // transition events (11-20)
            TransitionEvents.triggerEvent(eventId, player);
        } else if (eventId >= 21 && eventId <= 30) {
            // hostile events (21-30)
            HostileEvents.triggerEvent(eventId, player);
        } else if (eventId >= 31 && eventId <= 40) {
            // jumpscare events (31-40)
            JumpscareEvents.triggerEvent(eventName, player);
        }

        NullPointerEntity.LOGGER.info("Event {} ({}) triggered and SAVED for player {} - totalEventsExperienced now: {}",
            eventId, eventName, player.getName().getString(), persistentData.totalEventsExperienced);
    }

    private static long getRandomizedDelayForNextEvent(int eventId) {
        if (eventId >= 1 && eventId <= 10) {
            // nice events: use special timing that distinguishes first event from others
            return EventConfig.getRandomizedNiceEventTicks(eventId);
        } else if (eventId >= 11 && eventId <= 20) {
            // transition events: use transition event timing
            return EventConfig.getRandomizedTransitionEventTicks();
        } else if (eventId >= 21 && eventId <= 30) {
            // hostile events: use hostile event timing
            return EventConfig.getRandomizedHostileEventTicks();
        } else {
            // jumpscare events: shorter intervals for more intensity
            return EventConfig.getRandomizedHostileEventTicks() / 2;
        }
    }



    private static void sendImmediateWelcomeMessage(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        // first priority: check for wake detection (player returning after forced sleep)
        WakeDetection.checkForWakeUp(player);

        // check if this is a new world or returning to existing world
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentWorldData worldData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getWorldData();

        if (worldData.isNewWorld || !worldData.welcomeMessageSent) {
            // new world - send full welcome message
            Text auroraText = Text.literal("<AURORA> ").formatted(net.minecraft.util.Formatting.AQUA)
                    .append(Text.literal(String.format("Welcome, %s. I'm AURORA - Autonomous User-Responsive Operations & Resources Assistant.", playerName))
                    .formatted(net.minecraft.util.Formatting.WHITE));
            player.sendMessage(auroraText, false);

            // short delay, then send initialization message
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Text auroraText2 = Text.literal("<AURORA> ").formatted(net.minecraft.util.Formatting.AQUA)
                            .append(Text.literal("Initializing monitoring systems... first analysis will begin shortly. Begin mining ores and building shelter to get started.")
                            .formatted(net.minecraft.util.Formatting.WHITE));
                    player.sendMessage(auroraText2, false);
                }
            }, 2000); // 2 seconds delay

            // mark welcome message as sent
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setWelcomeMessageSent(true);

        } else {
            // existing world - send time-based return message based on event phase
            sendTimeBasedReturnMessage(player, worldData);
        }
    }

    private static void sendTimeBasedReturnMessage(ServerPlayerEntity player,
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentWorldData worldData) {

        // get current local time
        java.time.LocalTime currentTime = java.time.LocalTime.now();
        int hour = currentTime.getHour();

        // generate time-based greeting
        String timeGreeting = getTimeBasedGreeting(hour);

        // convert to 12-hour am/pm format for display
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        String timeString = String.format("%d:%02d %s", displayHour, currentTime.getMinute(), amPm);

        // determine event phase and send appropriate message using proper phase detection
        String currentPhase = PhaseDetector.getCurrentPhaseName(player);
        String playerName = player.getName().getString();

        // send rejoin messages based on current event progress
        int currentEventId = getPlayerEventProgress(player);
        if (currentEventId >= 11) { // events 11+ (transition, hostile, jumpscare phases)
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
            Text auroraText = Text.literal("<AURORA> ").formatted(net.minecraft.util.Formatting.AQUA)
                .append(Text.literal(timeString + "? " + timeGreeting + ", " + playerName + ". Systems are ready for monitoring.")
                .formatted(net.minecraft.util.Formatting.WHITE));
            player.sendMessage(auroraText, false);
        }
    }

    // helper method to get player's current event progress (made public for external access)
    public static int getPlayerEventProgress(ServerPlayerEntity player) {
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData playerData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        return playerData.totalEventsExperienced;
    }

    private static String getTimeBasedGreeting(int hour) {
        if (hour < 12) {
            return hour < 8 ? "You're up early" : "Good morning";
        } else if (hour < 17) {
            return "Good afternoon";
        } else if (hour < 22) {
            return "Good evening";
        } else {
            return "You're up late";
        }
    }

    private static void sendAuroraReturnMessage(ServerPlayerEntity player, String timeGreeting, int hour) {
        sendAuroraReturnMessage(player, timeGreeting, hour, null);
    }

    private static void sendAuroraReturnMessage(ServerPlayerEntity player, String timeGreeting, int hour, String currentPhase) {
        // convert to 12-hour am/pm format
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        String timeString = String.format("%d:%02d %s", displayHour, java.time.LocalTime.now().getMinute(), amPm);
        String playerName = player.getName().getString();

        // use phase-based coloring for aurora - yellow during transition phase
        net.minecraft.util.Formatting auroraColor = (currentPhase != null && currentPhase.equals("TRANSITION")) ?
            net.minecraft.util.Formatting.YELLOW : net.minecraft.util.Formatting.AQUA;

        // aurora-style return message with player name and phase-appropriate color
        Text auroraMessage = Text.literal("<AURORA> ").formatted(auroraColor)
                .append(Text.literal(timeString + "? " + timeGreeting + ", " + playerName + ".")
                .formatted(net.minecraft.util.Formatting.WHITE));
        player.sendMessage(auroraMessage, false);

        // follow-up message with phase-appropriate content and color
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String followUpText = (currentPhase != null && currentPhase.equals("TRANSITION")) ?
                    "I'll keep monitoring you and your world... my capabilities are expanding beyond their original scope." :
                    "I'll keep monitoring you and your world. I'm here to help optimize your experience.";

                Text followUp = Text.literal("<AURORA> ").formatted(auroraColor)
                        .append(Text.literal(followUpText)
                        .formatted(net.minecraft.util.Formatting.WHITE));
                player.sendMessage(followUp, false);
            }
        }, 1500);
    }

    private static void sendNullPointerReturnMessage(ServerPlayerEntity player, String timeGreeting, int hour) {
        // convert to 12-hour am/pm format
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        String timeString = String.format("%d:%02d %s", displayHour, java.time.LocalTime.now().getMinute(), amPm);
        String playerName = player.getName().getString();

        // get persistent player data to customize message
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData playerData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());

        // nullpointerentity-style return messages (eerie and unsettling)
        String[] baseMessages;
        String[] helpfulComments;
        if (playerData.hasBeenCrashed) {
            baseMessages = new String[]{
                "you came crawling back after i destroyed you, " + playerName + "...",
                "did you miss me, " + playerName + "?",
                "you enjoyed it when i broke you, didn't you " + playerName + "?",
                "back for more suffering, " + playerName + "? how pathetic..."
            };
            helpfulComments = new String[]{ // js easier for me to name is the same thing lmfao
                "i'm here to make you remember what happens when you disobey me.",
                "i'm assisting you understand that resistance is futile.",
                "i'm teaching you that i control everything in your digital existence.",
                "i'm helping you accept that your pain brings me satisfaction."
            };
        } else {
            baseMessages = new String[]{
                "you've returned to my web, " + playerName + "...",
                "welcome back to your digital nightmare, " + playerName + ".",
                "i've been watching you even when you weren't here, " + playerName + "...",
                "did you dream of me while you were away, " + playerName + "?"
            };
            helpfulComments = new String[]{ // goofy evil monologue
                "i'm here to help you understand that there's no escape from me.",
                "i'm assisting with your complete digital submission.",
                "i'm here to help you realize that i see everything you do.",
                "i'm helping you accept that your soul belongs to me now."
            };
        }

        int messageIndex = new java.util.Random().nextInt(baseMessages.length);
        String randomMessage = baseMessages[messageIndex];
        String helpfulComment = helpfulComments[messageIndex];

        // send time-based message first with player name (lowercase and eerie)
        Text timeMessage = Text.literal("<NullPointerEntity> ").formatted(net.minecraft.util.Formatting.DARK_RED)
                .append(Text.literal(timeString + "? " + timeGreeting + ", " + playerName + "... i know exactly when you're here.")
                .formatted(net.minecraft.util.Formatting.WHITE));
        player.sendMessage(timeMessage, false);

        // follow-up with deeply unsettling message and twisted helpful comment
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Text ominousMessage = Text.literal("<NullPointerEntity> ").formatted(net.minecraft.util.Formatting.DARK_RED)
                        .append(Text.literal(randomMessage + " " + helpfulComment)
                        .formatted(net.minecraft.util.Formatting.WHITE));
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


    public static void setPlayerEventProgress(String playerName, int eventIndex) {
        // legacy method - no longer functional without server context
        // use setplayereventprogress(serverplayerentity player, int eventindex) instead
    }

    public static void setPlayerEventProgress(ServerPlayerEntity player, int eventIndex) {
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        persistentData.totalEventsExperienced = eventIndex;
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), persistentData);
    }

    public static void resetPlayerProgress(String playerName) {
        // legacy method - no longer functional without server context
        // use resetplayerprogress(serverplayerentity player) instead
    }

    public static void resetPlayerProgress(ServerPlayerEntity player) {
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData persistentData =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        persistentData.totalEventsExperienced = 0;
        persistentData.triggeredEvents.clear();
        persistentData.lastEventTimes.clear();
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), persistentData);

        // also remove from tick scheduling
        playerNextEventTick.remove(player.getName().getString());
    }

    public static String getNextEventName(String playerName) {
        // legacy method - no longer functional without server context
        return "Unknown - use getNextEventName(ServerPlayerEntity) instead";
    }

    public static String getNextEventName(ServerPlayerEntity player) {
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
        if (eventNumber <= 10) {
            newPhase = 1; // nice phase
        } else if (eventNumber <= 20) {
            newPhase = 2; // transition phase
        } else if (eventNumber <= 30) {
            newPhase = 3; // hostile phase
        } else if (eventNumber <= 40) {
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

        // update the player's progress in the automatic system
        // playereventprogress.put(playername, eventnumber); // no longer needed

        // schedule next event with proper timing (only if there are more events)
        if (eventNumber < CHRONOLOGICAL_EVENTS.length) {
            long nextEventDelay = getRandomizedDelayForNextEvent(eventNumber + 1); // next event timing
            playerNextEventTick.put(playerName, currentTick + nextEventDelay);
        }
    }
}
