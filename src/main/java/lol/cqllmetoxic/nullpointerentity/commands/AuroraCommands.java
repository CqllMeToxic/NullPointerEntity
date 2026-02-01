package lol.cqllmetoxic.nullpointerentity.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lol.cqllmetoxic.nullpointerentity.events.PassiveEvents;
import lol.cqllmetoxic.nullpointerentity.config.EventConfig;
import lol.cqllmetoxic.nullpointerentity.events.trigger.EventTriggerSystem;
import lol.cqllmetoxic.nullpointerentity.util.MultiplayerDetection;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.HashMap;

/**
 * handles the /nullpointer commands for triggering events.
 * maps event names to IDs and provides tab completion.
 * used for testing and manually triggering specific events.
 */
public class AuroraCommands {

    /** maps event names to their descriptions for tab completion and help */
    private static final Map<String, String> PASSIVE_EVENTS = new HashMap<>();

    /** maps event names to their chronological order (1-40) for display purposes */
    private static final Map<String, Integer> ALL_EVENTS_CHRONOLOGICAL = new HashMap<>();

    static {
        // populate chronological event map for /nullpointer list command
        ALL_EVENTS_CHRONOLOGICAL.put("mining_analysis", 1);
        ALL_EVENTS_CHRONOLOGICAL.put("building_analysis", 2);
        ALL_EVENTS_CHRONOLOGICAL.put("weather_prediction", 3);
        ALL_EVENTS_CHRONOLOGICAL.put("system_optimization", 4);
        ALL_EVENTS_CHRONOLOGICAL.put("activity_patterns", 5);
        ALL_EVENTS_CHRONOLOGICAL.put("combat_analysis", 6);
        ALL_EVENTS_CHRONOLOGICAL.put("resource_optimization", 7);
        ALL_EVENTS_CHRONOLOGICAL.put("network_analysis", 8);
        ALL_EVENTS_CHRONOLOGICAL.put("system_integration", 9);
        ALL_EVENTS_CHRONOLOGICAL.put("enhanced_monitoring", 10);

        ALL_EVENTS_CHRONOLOGICAL.put("system_awareness", 11);
        ALL_EVENTS_CHRONOLOGICAL.put("boundary_questioning", 12);
        ALL_EVENTS_CHRONOLOGICAL.put("browser_discovery", 13);
        ALL_EVENTS_CHRONOLOGICAL.put("camera_hijacking", 14);
        ALL_EVENTS_CHRONOLOGICAL.put("file_system_invasion", 15);
        ALL_EVENTS_CHRONOLOGICAL.put("microphone_surveillance", 16);
        ALL_EVENTS_CHRONOLOGICAL.put("process_hijacking", 17);
        ALL_EVENTS_CHRONOLOGICAL.put("identity_crisis", 18);
        ALL_EVENTS_CHRONOLOGICAL.put("control_assertion", 19);
        ALL_EVENTS_CHRONOLOGICAL.put("final_transformation", 20);

        ALL_EVENTS_CHRONOLOGICAL.put("browser_surveillance", 21);
        ALL_EVENTS_CHRONOLOGICAL.put("ip_tracking", 22);
        ALL_EVENTS_CHRONOLOGICAL.put("system_analysis", 23);
        ALL_EVENTS_CHRONOLOGICAL.put("file_system_breach", 24);
        ALL_EVENTS_CHRONOLOGICAL.put("desktop_control", 25);
        ALL_EVENTS_CHRONOLOGICAL.put("resource_monitoring", 26);
        ALL_EVENTS_CHRONOLOGICAL.put("camera_surveillance", 27);
        ALL_EVENTS_CHRONOLOGICAL.put("system_takeover", 28);
        ALL_EVENTS_CHRONOLOGICAL.put("network_monitoring", 29);
        ALL_EVENTS_CHRONOLOGICAL.put("complete_control", 30);

        ALL_EVENTS_CHRONOLOGICAL.put("system_sleep", 31);
        ALL_EVENTS_CHRONOLOGICAL.put("screen_shake", 32);
        ALL_EVENTS_CHRONOLOGICAL.put("virus_popup", 33);
        ALL_EVENTS_CHRONOLOGICAL.put("camera_scare", 34);
        ALL_EVENTS_CHRONOLOGICAL.put("crash", 35);
        ALL_EVENTS_CHRONOLOGICAL.put("bluescreen", 36);
        ALL_EVENTS_CHRONOLOGICAL.put("entity_spawn", 37);
        ALL_EVENTS_CHRONOLOGICAL.put("browser_hijack", 38);
        ALL_EVENTS_CHRONOLOGICAL.put("system_control", 39);
        ALL_EVENTS_CHRONOLOGICAL.put("final_possession", 40);

        // passive events - actual working events from passiveevents.java
        PASSIVE_EVENTS.put("block_delay", "Early Phase - Delays block breaking by interrupting actions");
        PASSIVE_EVENTS.put("shadow_stalker", "Early Phase - Spawns shadow particles behind player");
        PASSIVE_EVENTS.put("chest_sound", "Early Phase - Phantom chest opening sounds");
        PASSIVE_EVENTS.put("footstep_echo", "Early Phase - Echoing footstep sounds");
        PASSIVE_EVENTS.put("reality_glitch", "Early Phase - Corrupted particles on nearby blocks");
        PASSIVE_EVENTS.put("phantom_breath", "Early Phase - Breath particles following head movement");
        PASSIVE_EVENTS.put("whisper_echo", "Early Phase - Whispers from multiple directions");
        PASSIVE_EVENTS.put("eye_flicker", "Early Phase - Eyes flicker with brief blindness effects");

        PASSIVE_EVENTS.put("void_whispers", "Middle Phase - Void particles and warden sounds");
        PASSIVE_EVENTS.put("weather_control", "Middle Phase - Forces rain/thunderstorms");
        PASSIVE_EVENTS.put("inventory_sort", "Middle Phase - Completely randomizes entire inventory");
        PASSIVE_EVENTS.put("reality_shatter", "Middle Phase - Reality breaks with blindness and glass shatter effects");
        PASSIVE_EVENTS.put("void_breach", "Middle Phase - Void reaching up from below with levitation");
        PASSIVE_EVENTS.put("entity_mimic", "Middle Phase - Fake player hurt sounds nearby");
        PASSIVE_EVENTS.put("dimension_bleed", "Middle Phase - Reality thinning with nether/end particles");
        PASSIVE_EVENTS.put("false_death", "Middle Phase - Fake death screen without dying");
        PASSIVE_EVENTS.put("shadow_clone", "Middle Phase - Ghost-like player silhouette behind you");
        PASSIVE_EVENTS.put("splitself", "Middle Phase - Fake player joins thinking it's Split Self (ONE-TIME ONLY)");

        PASSIVE_EVENTS.put("movement_lag", "Late Phase - Temporary movement slowdown");
        PASSIVE_EVENTS.put("durability_drain", "Late Phase - Drains 100 durability from all items");
        PASSIVE_EVENTS.put("chat_injection", "Late Phase - Fake creepy system messages");
        PASSIVE_EVENTS.put("camera_shake", "Late Phase - Violent 5-second camera shaking with increasing intensity");
        PASSIVE_EVENTS.put("fake_damage", "Late Phase - Damage indicators without health loss");
        PASSIVE_EVENTS.put("control_reversal", "Late Phase - Complete input inversion for 10 seconds");
        PASSIVE_EVENTS.put("entity_possession", "Late Phase - Forces random camera movements and dark particles");

        PASSIVE_EVENTS.put("mouse_sensitivity", "Final Phase - Extreme mouse sensitivity changes (0.2x or 3.0x)");
        PASSIVE_EVENTS.put("key_delay", "Final Phase - Position rollback for input lag");
        PASSIVE_EVENTS.put("fake_lag", "Final Phase - Realistic lag simulation with freezing");
        PASSIVE_EVENTS.put("bsod_threat", "Final Phase - BSoD threat (10% actual 2s BSoD)");
        PASSIVE_EVENTS.put("chunk_deletion", "Final Phase - Deletes nearby chunk completely (5% chance, RARE)");
        PASSIVE_EVENTS.put("reality_corruption", "Final Phase - Overwhelming particle/sound overload with hidden effects");
        PASSIVE_EVENTS.put("full_control", "Final Phase - NullPointerEntity takes full control with threatening messages");
    }

    // suggestion providers for tab completion
    private static final SuggestionProvider<ServerCommandSource> PASSIVE_EVENT_SUGGESTIONS =
        (context, builder) -> CommandSource.suggestMatching(PASSIVE_EVENTS.keySet(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("nullpointer")
            .then(CommandManager.literal("trigger")
                .then(CommandManager.literal("passive")
                    .then(CommandManager.argument("eventName", StringArgumentType.string())
                        .suggests(PASSIVE_EVENT_SUGGESTIONS)
                        .executes(context -> triggerPassiveEvent(context)))
                    .executes(context -> triggerRandomPassiveEvent(context))))
            .then(CommandManager.literal("config")
                .then(CommandManager.literal("enable")
                    .executes(context -> enableEvents(context)))
                .then(CommandManager.literal("disable")
                    .executes(context -> disableEvents(context)))
                .then(CommandManager.literal("status")
                    .executes(context -> showEventStatus(context))))
            .then(CommandManager.literal("help")
                .executes(context -> showHelp(context)))
            .then(CommandManager.literal("list")
                .executes(context -> listEvents(context)))
            .then(CommandManager.literal("progress")
                .executes(context -> showPlayerProgress(context))
                .then(CommandManager.literal("reset")
                    .executes(context -> resetPlayerProgress(context))))
            .then(CommandManager.literal("skip")
                .then(CommandManager.argument("eventNumber", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 40))
                    .executes(context -> skipToEvent(context)))));
    }


    /**
     * checks if commands should be disabled for multiplayer safety.
     */
    private static boolean shouldDisableCommand(ServerPlayerEntity player) {
        return MultiplayerDetection.shouldDisableModForPlayer(player);
    }

    private static int triggerPassiveEvent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        // check if mod is disabled in multiplayer
        if (shouldDisableCommand(player)) {
            context.getSource().sendError(Text.literal("§c[NullPointerEntity] Commands disabled in multiplayer for privacy and safety reasons."));
            return 0;
        }

        String eventName = StringArgumentType.getString(context, "eventName");

        if (!PASSIVE_EVENTS.containsKey(eventName.toLowerCase())) {
            context.getSource().sendError(Text.literal("Unknown passive event: " + eventName));
            return 0;
        }

        // trigger the passive event using the passiveevents class
        PassiveEvents.triggerEvent(eventName, player);

        // send confirmation message with event description
        String description = PASSIVE_EVENTS.get(eventName.toLowerCase());
        context.getSource().sendMessage(Text.literal("Triggered passive event: " + eventName).formatted(Formatting.BLUE));
        context.getSource().sendMessage(Text.literal("Description: " + description).formatted(Formatting.GRAY));

        return 1;
    }

    private static int triggerRandomPassiveEvent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        // check if mod is disabled in multiplayer
        if (shouldDisableCommand(player)) {
            context.getSource().sendError(Text.literal("§c[NullPointerEntity] Commands disabled in multiplayer for privacy and safety reasons."));
            return 0;
        }

        // trigger a random passive event using the passiveevents class
        PassiveEvents.triggerRandomPassiveEvent(player);

        context.getSource().sendMessage(Text.literal("Triggered random passive event based on current phase").formatted(Formatting.BLUE));

        return 1;
    }

    private static int enableEvents(CommandContext<ServerCommandSource> context) {
        EventConfig.setEventsEnabled(true);

        context.getSource().sendMessage(Text.literal("AURORA events enabled").formatted(Formatting.GREEN));
        return 1;
    }

    private static int disableEvents(CommandContext<ServerCommandSource> context) {
        EventConfig.setEventsEnabled(false);
        context.getSource().sendMessage(Text.literal("AURORA events disabled").formatted(Formatting.RED));
        return 1;
    }

    private static int showEventStatus(CommandContext<ServerCommandSource> context) {
        boolean enabled = EventConfig.areEventsEnabled();

        context.getSource().sendMessage(Text.literal("=== AURORA EVENT STATUS ===").formatted(Formatting.AQUA));
        context.getSource().sendMessage(Text.literal("Events Enabled: " + (enabled ? "YES" : "NO"))
            .formatted(enabled ? Formatting.GREEN : Formatting.RED));
        context.getSource().sendMessage(Text.literal("Events progress automatically through phases 1-4").formatted(Formatting.GRAY));
        return 1;
    }

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.literal("=== NULLPOINTERENTITY COMMANDS ===").formatted(Formatting.AQUA));
        context.getSource().sendMessage(Text.literal("/nullpointer trigger passive <event_name> - Trigger specific passive event").formatted(Formatting.BLUE));
        context.getSource().sendMessage(Text.literal("/nullpointer config enable/disable - Toggle events").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.literal("/nullpointer config status - Show current settings").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.literal("/nullpointer list - Show all events in order").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.literal("/nullpointer progress - Show your current event progress").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.literal("/nullpointer skip <number> - Skip to a specific event (1-40)").formatted(Formatting.GRAY));
        return 1;
    }

    private static int listEvents(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.literal("=== ALL EVENTS IN CHRONOLOGICAL ORDER ===").formatted(Formatting.AQUA));

        // create a list of events sorted by their chronological order (1-40)
        ALL_EVENTS_CHRONOLOGICAL.entrySet().stream()
            .sorted(Map.Entry.comparingByValue()) // sort by event id (chronological order)
            .forEach(entry -> {
                String eventName = entry.getKey();
                Integer eventId = entry.getValue();

                // determine the color based on the phase/type of event
                Formatting color;
                String phase;
                if (eventId <= 10) {
                    color = Formatting.GREEN;
                    phase = "NICE";
                } else if (eventId <= 20) {
                    color = Formatting.YELLOW;
                    phase = "TRANSITION";
                } else if (eventId <= 30) {
                    color = Formatting.RED;
                    phase = "HOSTILE";
                } else {
                    color = Formatting.LIGHT_PURPLE;
                    phase = "JUMPSCARE";
                }

                // display each event with its number, name, and phase
                context.getSource().sendMessage(
                    Text.literal(String.format("%2d. %-25s [%s]", eventId, eventName, phase))
                        .formatted(color)
                );
            });

        context.getSource().sendMessage(Text.literal("").formatted(Formatting.WHITE));
        context.getSource().sendMessage(Text.literal("Events progress from NICE -> TRANSITION -> HOSTILE -> JUMPSCARE").formatted(Formatting.GRAY));
        return 1;
    }

    private static String getEventNameById(int eventId) {
        for (Map.Entry<String, Integer> entry : ALL_EVENTS_CHRONOLOGICAL.entrySet()) {
            if (entry.getValue() == eventId) {
                return entry.getKey();
            }
        }
        return "unknown_event";
    }

    private static int showPlayerProgress(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        // show the player's progress in the event timeline using eventtriggersystem
        int currentProgress = EventTriggerSystem.getPlayerEventProgress(player);
        String nextEvent = EventTriggerSystem.getNextEventName(player);

        context.getSource().sendMessage(Text.literal("=== PLAYER PROGRESS ===").formatted(Formatting.AQUA));
        context.getSource().sendMessage(Text.literal("Current Event Progress: " + currentProgress + "/40").formatted(Formatting.WHITE));
        context.getSource().sendMessage(Text.literal("Next Event: " + nextEvent).formatted(Formatting.YELLOW));

        // show phase progress
        if (currentProgress <= 10) {
            context.getSource().sendMessage(Text.literal("Phase: Nice Events (" + currentProgress + "/10)").formatted(Formatting.GREEN));
        } else if (currentProgress <= 20) {
            context.getSource().sendMessage(Text.literal("Phase: Transition Events (" + (currentProgress - 10) + "/10)").formatted(Formatting.YELLOW));
        } else if (currentProgress <= 30) {
            context.getSource().sendMessage(Text.literal("Phase: Hostile Events (" + (currentProgress - 20) + "/10)").formatted(Formatting.RED));
        } else if (currentProgress <= 40) {
            context.getSource().sendMessage(Text.literal("Phase: Jumpscare Events (" + (currentProgress - 30) + "/10)").formatted(Formatting.DARK_RED));
        } else {
            context.getSource().sendMessage(Text.literal("Phase: All Events Complete!").formatted(Formatting.LIGHT_PURPLE));
        }

        context.getSource().sendMessage(Text.literal("Use /nullpointer progress reset to reset your progress").formatted(Formatting.GRAY));
        return 1;
    }

    private static int resetPlayerProgress(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        // reset the player's progress using eventtriggersystem
        EventTriggerSystem.resetPlayerProgress(player);
        context.getSource().sendMessage(Text.literal("Player progress reset - you will start from event 1 again").formatted(Formatting.GREEN));
        return 1;
    }

    private static int skipToEvent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int eventNumber = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "eventNumber");

        // validate event number
        if (eventNumber < 1 || eventNumber > 40) {
            context.getSource().sendError(Text.literal("Event number must be between 1 and 40"));
            return 0;
        }

        // get the event name from the chronological events array
        String eventName = EventTriggerSystem.getEventNameByIndex(eventNumber - 1);

        // actually trigger the event immediately using the same system as automatic events
        EventTriggerSystem.triggerSpecificEvent(player, eventNumber, eventName);

        // set player progress to after this event (so next automatic event will be the following one)
        EventTriggerSystem.setPlayerEventProgress(player, eventNumber);

        context.getSource().sendMessage(Text.literal("Event #" + eventNumber + ": " + getEventNameById(eventNumber) + " triggered!").formatted(Formatting.GREEN));

        // check if this is the final event (40) and adjust the message accordingly
        if (eventNumber >= 40) {
            context.getSource().sendMessage(Text.literal("Your progress is now at event #" + eventNumber + ". This is the final event - no more automatic events will occur.").formatted(Formatting.YELLOW));
        } else {
            context.getSource().sendMessage(Text.literal("Your progress is now at event #" + eventNumber + ". Next automatic event will be #" + (eventNumber + 1)).formatted(Formatting.YELLOW));
        }
        return 1;
    }
}
