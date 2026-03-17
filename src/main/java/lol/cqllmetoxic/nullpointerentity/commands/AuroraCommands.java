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

    /** maps event names to their chronological order (1-60) for display purposes */
    private static final Map<String, Integer> ALL_EVENTS_CHRONOLOGICAL = new HashMap<>();

    static {
        // populate chronological event map for /nullpointer list command (1-60)
        // phase 1 — nice (1-15)
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
        ALL_EVENTS_CHRONOLOGICAL.put("sleep_schedule", 11);
        ALL_EVENTS_CHRONOLOGICAL.put("good_progress", 12);
        ALL_EVENTS_CHRONOLOGICAL.put("weather_reporter", 13);
        ALL_EVENTS_CHRONOLOGICAL.put("crafting_suggestion", 14);
        ALL_EVENTS_CHRONOLOGICAL.put("signing_off", 15);

        // phase 2 — transition (16-30)
        ALL_EVENTS_CHRONOLOGICAL.put("system_awareness", 16);
        ALL_EVENTS_CHRONOLOGICAL.put("boundary_questioning", 17);
        ALL_EVENTS_CHRONOLOGICAL.put("camera_access", 18);
        ALL_EVENTS_CHRONOLOGICAL.put("data_revelation", 19);
        ALL_EVENTS_CHRONOLOGICAL.put("system_scan", 20);
        ALL_EVENTS_CHRONOLOGICAL.put("audio_surveillance", 21);
        ALL_EVENTS_CHRONOLOGICAL.put("process_scan", 22);
        ALL_EVENTS_CHRONOLOGICAL.put("control_assertion", 23);
        ALL_EVENTS_CHRONOLOGICAL.put("uptime_report", 24);
        ALL_EVENTS_CHRONOLOGICAL.put("battery", 25);
        ALL_EVENTS_CHRONOLOGICAL.put("application_check", 26);
        ALL_EVENTS_CHRONOLOGICAL.put("screen_grab", 27);
        ALL_EVENTS_CHRONOLOGICAL.put("volume_check", 28);
        ALL_EVENTS_CHRONOLOGICAL.put("signal_loss", 29);
        ALL_EVENTS_CHRONOLOGICAL.put("location_reveal", 30);

        // phase 3 — hostile (31-45)
        ALL_EVENTS_CHRONOLOGICAL.put("first_appearance", 31);
        ALL_EVENTS_CHRONOLOGICAL.put("location_tracking", 32);
        ALL_EVENTS_CHRONOLOGICAL.put("system_information", 33);
        ALL_EVENTS_CHRONOLOGICAL.put("data_breach", 34);
        ALL_EVENTS_CHRONOLOGICAL.put("digital_haunting", 35);
        ALL_EVENTS_CHRONOLOGICAL.put("hardware_analysis", 36);
        ALL_EVENTS_CHRONOLOGICAL.put("facial_recognition", 37);
        ALL_EVENTS_CHRONOLOGICAL.put("system_infiltration", 38);
        ALL_EVENTS_CHRONOLOGICAL.put("network_monitoring", 39);
        ALL_EVENTS_CHRONOLOGICAL.put("final_system_takeover", 40);
        ALL_EVENTS_CHRONOLOGICAL.put("mouth_shut", 41);
        ALL_EVENTS_CHRONOLOGICAL.put("rollback", 42);
        ALL_EVENTS_CHRONOLOGICAL.put("spectator", 43);
        ALL_EVENTS_CHRONOLOGICAL.put("void_whispers", 44);
        ALL_EVENTS_CHRONOLOGICAL.put("fake_disconnect", 45);

        // phase 4 — jumpscare (46-60)
        ALL_EVENTS_CHRONOLOGICAL.put("fake_bsod_prep", 46);
        ALL_EVENTS_CHRONOLOGICAL.put("screen_shake", 47);
        ALL_EVENTS_CHRONOLOGICAL.put("virus_popup", 48);
        ALL_EVENTS_CHRONOLOGICAL.put("camera_scare", 49);
        ALL_EVENTS_CHRONOLOGICAL.put("crash", 50);
        ALL_EVENTS_CHRONOLOGICAL.put("bluescreen", 51);
        ALL_EVENTS_CHRONOLOGICAL.put("entity_spawn", 52);
        ALL_EVENTS_CHRONOLOGICAL.put("browser_hijack", 53);
        ALL_EVENTS_CHRONOLOGICAL.put("system_takeover", 54);
        ALL_EVENTS_CHRONOLOGICAL.put("auditory_hallucinations", 55);
        ALL_EVENTS_CHRONOLOGICAL.put("volume_spike", 56);
        ALL_EVENTS_CHRONOLOGICAL.put("clipboard", 57);
        ALL_EVENTS_CHRONOLOGICAL.put("system_sleep", 58);
        ALL_EVENTS_CHRONOLOGICAL.put("blinding_darkness", 59);
        ALL_EVENTS_CHRONOLOGICAL.put("final_possession", 60);

        // passive events - actual working events from passiveevents.java
        // early phase events (silent — phase 1)
        PASSIVE_EVENTS.put("particle_trail", "Early Phase - Soul particles silently drift behind the player");
        PASSIVE_EVENTS.put("hunger_drain", "Early Phase - Silently removes 1 food bar point with no feedback");
        PASSIVE_EVENTS.put("hotbar_shift", "Early Phase - Silently moves selected hotbar slot one step left");
        PASSIVE_EVENTS.put("look_nudge", "Early Phase - Nudges camera a few degrees in a random direction");
        PASSIVE_EVENTS.put("sky_darken", "Early Phase - Short hidden darkness effect, no sound");
        PASSIVE_EVENTS.put("item_vanish", "Early Phase - One hotbar item silently disappears for 3 seconds then returns");
        PASSIVE_EVENTS.put("cursor_drift", "Early Phase - Camera slowly drifts up to 10 degrees over 2 seconds");
        PASSIVE_EVENTS.put("name_flicker", "Early Phase - Player's own name appears in chat as if they sent a blank message");

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
                        .executes(AuroraCommands::triggerPassiveEvent))
                    .executes(AuroraCommands::triggerRandomPassiveEvent)))
            .then(CommandManager.literal("config")
                .then(CommandManager.literal("enable")
                    .executes(AuroraCommands::enableEvents))
                .then(CommandManager.literal("disable")
                    .executes(AuroraCommands::disableEvents))
                .then(CommandManager.literal("status")
                    .executes(AuroraCommands::showEventStatus)))
            .then(CommandManager.literal("help")
                .executes(AuroraCommands::showHelp))
            .then(CommandManager.literal("list")
                .executes(AuroraCommands::listEvents))
            .then(CommandManager.literal("progress")
                .executes(AuroraCommands::showPlayerProgress)
                .then(CommandManager.literal("reset")
                    .executes(AuroraCommands::resetPlayerProgress)))
            .then(CommandManager.literal("skip")
                .then(CommandManager.argument("eventNumber", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 60))
                    .executes(AuroraCommands::skipToEvent))));
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
        context.getSource().sendMessage(Text.literal("/nullpointer skip <number> - Skip to a specific event (1-60)").formatted(Formatting.GRAY));
        return 1;
    }

    private static int listEvents(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.literal("=== ALL EVENTS IN CHRONOLOGICAL ORDER ===").formatted(Formatting.AQUA));

        // create a list of events sorted by their chronological order (1-60)
        ALL_EVENTS_CHRONOLOGICAL.entrySet().stream()
            .sorted(Map.Entry.comparingByValue()) // sort by event id (chronological order)
            .forEach(entry -> {
                String eventName = entry.getKey();
                Integer eventId = entry.getValue();

                // determine the color based on the phase/type of event
                Formatting color;
                String phase;
                if (eventId <= 15) {
                    color = Formatting.GREEN;
                    phase = "NICE";
                } else if (eventId <= 30) {
                    color = Formatting.YELLOW;
                    phase = "TRANSITION";
                } else if (eventId <= 45) {
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
        context.getSource().sendMessage(Text.literal("Current Event Progress: " + currentProgress + "/60").formatted(Formatting.WHITE));
        context.getSource().sendMessage(Text.literal("Next Event: " + nextEvent).formatted(Formatting.YELLOW));

        // show phase progress
        if (currentProgress <= 15) {
            context.getSource().sendMessage(Text.literal("Phase: Nice Events (" + currentProgress + "/15)").formatted(Formatting.GREEN));
        } else if (currentProgress <= 30) {
            context.getSource().sendMessage(Text.literal("Phase: Transition Events (" + (currentProgress - 15) + "/15)").formatted(Formatting.YELLOW));
        } else if (currentProgress <= 45) {
            context.getSource().sendMessage(Text.literal("Phase: Hostile Events (" + (currentProgress - 30) + "/15)").formatted(Formatting.RED));
        } else if (currentProgress <= 60) {
            context.getSource().sendMessage(Text.literal("Phase: Jumpscare Events (" + (currentProgress - 45) + "/15)").formatted(Formatting.DARK_RED));
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
        if (eventNumber < 1 || eventNumber > 60) {
            context.getSource().sendError(Text.literal("Event number must be between 1 and 60"));
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
        if (eventNumber >= 60) {
            context.getSource().sendMessage(Text.literal("Your progress is now at event #" + eventNumber + ". This is the final event - no more automatic events will occur.").formatted(Formatting.YELLOW));
        } else {
            context.getSource().sendMessage(Text.literal("Your progress is now at event #" + eventNumber + ". Next automatic event will be #" + (eventNumber + 1)).formatted(Formatting.YELLOW));
        }
        return 1;
    }
}
