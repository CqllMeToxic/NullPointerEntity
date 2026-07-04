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
import net.minecraft.server.integrated.IntegratedServer;
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
    private static final int OP_LEVEL_EVENT_CONTROL = 2;

    /** maps event names to their descriptions for tab completion and help */
    private static final Map<String, String> PASSIVE_EVENTS = new HashMap<>();

    /** maps event names to their chronological order (1-60) for display purposes */
    private static final Map<String, Integer> ALL_EVENTS_CHRONOLOGICAL = new HashMap<>();

    static {
        // populate chronological event map for /nullpointer list command (1-60)
        // phase 1 - nice (1-15)
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

        // phase 2 - transition (16-30)
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

        // phase 3 - hostile (31-45)
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

        // phase 4 - jumpscare (46-60)
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
        // early phase events (silent - phase 1)
        PASSIVE_EVENTS.put("particle_trail", "command.nullpointerentity.passive.desc.particle_trail");
        PASSIVE_EVENTS.put("hunger_drain", "command.nullpointerentity.passive.desc.hunger_drain");
        PASSIVE_EVENTS.put("hotbar_shift", "command.nullpointerentity.passive.desc.hotbar_shift");
        PASSIVE_EVENTS.put("look_nudge", "command.nullpointerentity.passive.desc.look_nudge");
        PASSIVE_EVENTS.put("sky_darken", "command.nullpointerentity.passive.desc.sky_darken");
        PASSIVE_EVENTS.put("item_vanish", "command.nullpointerentity.passive.desc.item_vanish");
        PASSIVE_EVENTS.put("cursor_drift", "command.nullpointerentity.passive.desc.cursor_drift");
        PASSIVE_EVENTS.put("name_flicker", "command.nullpointerentity.passive.desc.name_flicker");

        PASSIVE_EVENTS.put("void_whispers", "command.nullpointerentity.passive.desc.void_whispers");
        PASSIVE_EVENTS.put("weather_control", "command.nullpointerentity.passive.desc.weather_control");
        PASSIVE_EVENTS.put("inventory_sort", "command.nullpointerentity.passive.desc.inventory_sort");
        PASSIVE_EVENTS.put("reality_shatter", "command.nullpointerentity.passive.desc.reality_shatter");
        PASSIVE_EVENTS.put("void_breach", "command.nullpointerentity.passive.desc.void_breach");
        PASSIVE_EVENTS.put("entity_mimic", "command.nullpointerentity.passive.desc.entity_mimic");
        PASSIVE_EVENTS.put("dimension_bleed", "command.nullpointerentity.passive.desc.dimension_bleed");
        PASSIVE_EVENTS.put("false_death", "command.nullpointerentity.passive.desc.false_death");
        PASSIVE_EVENTS.put("shadow_clone", "command.nullpointerentity.passive.desc.shadow_clone");
        PASSIVE_EVENTS.put("splitself", "command.nullpointerentity.passive.desc.splitself");

        PASSIVE_EVENTS.put("movement_lag", "command.nullpointerentity.passive.desc.movement_lag");
        PASSIVE_EVENTS.put("durability_drain", "command.nullpointerentity.passive.desc.durability_drain");
        PASSIVE_EVENTS.put("chat_injection", "command.nullpointerentity.passive.desc.chat_injection");
        PASSIVE_EVENTS.put("camera_shake", "command.nullpointerentity.passive.desc.camera_shake");
        PASSIVE_EVENTS.put("fake_damage", "command.nullpointerentity.passive.desc.fake_damage");
        PASSIVE_EVENTS.put("control_reversal", "command.nullpointerentity.passive.desc.control_reversal");
        PASSIVE_EVENTS.put("entity_possession", "command.nullpointerentity.passive.desc.entity_possession");

        PASSIVE_EVENTS.put("mouse_sensitivity", "command.nullpointerentity.passive.desc.mouse_sensitivity");
        PASSIVE_EVENTS.put("key_delay", "command.nullpointerentity.passive.desc.key_delay");
        PASSIVE_EVENTS.put("fake_lag", "command.nullpointerentity.passive.desc.fake_lag");
        PASSIVE_EVENTS.put("bsod_threat", "command.nullpointerentity.passive.desc.bsod_threat");
        PASSIVE_EVENTS.put("chunk_deletion", "command.nullpointerentity.passive.desc.chunk_deletion");
        PASSIVE_EVENTS.put("reality_corruption", "command.nullpointerentity.passive.desc.reality_corruption");
    }

    // suggestion providers for tab completion
    private static final SuggestionProvider<ServerCommandSource> PASSIVE_EVENT_SUGGESTIONS =
        (context, builder) -> CommandSource.suggestMatching(PASSIVE_EVENTS.keySet(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("nullpointer")
            .then(CommandManager.literal("trigger")
                .requires(source -> source.hasPermissionLevel(OP_LEVEL_EVENT_CONTROL))
                .then(CommandManager.literal("passive")
                    .then(CommandManager.argument("eventName", StringArgumentType.string())
                        .suggests(PASSIVE_EVENT_SUGGESTIONS)
                        .executes(AuroraCommands::triggerPassiveEvent))
                    .executes(AuroraCommands::triggerRandomPassiveEvent)))
            .then(CommandManager.literal("config")
                .requires(source -> source.hasPermissionLevel(OP_LEVEL_EVENT_CONTROL))
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
                    .requires(source -> source.hasPermissionLevel(OP_LEVEL_EVENT_CONTROL))
                    .executes(AuroraCommands::resetPlayerProgress)))
            .then(CommandManager.literal("skip")
                .requires(AuroraCommands::isIntegratedServerHost)
                .then(CommandManager.argument("eventNumber", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 60))
                    .executes(AuroraCommands::skipToEvent))));
    }

    // host-only gate for singleplayer/LAN sessions; dedicated servers don't expose this command.
    private static boolean isIntegratedServerHost(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            return false;
        }

        var server = source.getServer();
        if (!(server instanceof IntegratedServer integratedServer)) {
            return false;
        }

        return integratedServer.isHost(player.getGameProfile());
    }

    private static int triggerPassiveEvent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        String eventName = StringArgumentType.getString(context, "eventName");

        if (!PASSIVE_EVENTS.containsKey(eventName.toLowerCase())) {
            context.getSource().sendError(Text.translatable("command.nullpointerentity.passive.unknown", eventName));
            return 0;
        }

        // trigger the passive event using the passiveevents class
        if (player.getServer() != null && MultiplayerDetection.isMultiplayerServer(player.getServer())) {
            PassiveEvents.triggerEventForAll(eventName, player.getServer());
        } else {
            PassiveEvents.triggerEvent(eventName, player);
        }

        // send confirmation message with event description
        String description = PASSIVE_EVENTS.get(eventName.toLowerCase());
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.passive.triggered", eventName).formatted(Formatting.BLUE));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.passive.description", Text.translatable(description)).formatted(Formatting.GRAY));

        return 1;
    }

    private static int triggerRandomPassiveEvent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();


        // trigger a random passive event using the passiveevents class
        if (player.getServer() != null && MultiplayerDetection.isMultiplayerServer(player.getServer())) {
            PassiveEvents.triggerRandomPassiveEventForAll(player.getServer());
        } else {
            PassiveEvents.triggerRandomPassiveEvent(player);
        }

        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.passive.triggered_random").formatted(Formatting.BLUE));

        return 1;
    }

    private static int enableEvents(CommandContext<ServerCommandSource> context) {
        EventConfig.setEventsEnabled(true);

        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.config.enabled").formatted(Formatting.GREEN));
        return 1;
    }

    private static int disableEvents(CommandContext<ServerCommandSource> context) {
        EventConfig.setEventsEnabled(false);
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.config.disabled").formatted(Formatting.RED));
        return 1;
    }

    private static int showEventStatus(CommandContext<ServerCommandSource> context) {
        boolean enabled = EventConfig.areEventsEnabled();

        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.status.header").formatted(Formatting.AQUA));
        context.getSource().sendMessage(Text.translatable(enabled
                ? "command.nullpointerentity.status.events_enabled.yes"
                : "command.nullpointerentity.status.events_enabled.no")
            .formatted(enabled ? Formatting.GREEN : Formatting.RED));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.status.phases").formatted(Formatting.GRAY));
        return 1;
    }

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.help.header").formatted(Formatting.AQUA));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.help.trigger").formatted(Formatting.BLUE));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.help.config").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.help.status").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.help.list").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.help.progress").formatted(Formatting.GRAY));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.help.skip").formatted(Formatting.GRAY));
        return 1;
    }

    private static int listEvents(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.list.header").formatted(Formatting.AQUA));

        // create a list of events sorted by their chronological order (1-60)
        ALL_EVENTS_CHRONOLOGICAL.entrySet().stream()
            .sorted(Map.Entry.comparingByValue()) // sort by event id (chronological order)
            .forEach(entry -> {
                String eventName = entry.getKey();
                Integer eventId = entry.getValue();

                // determine the color based on the phase/type of event
                Formatting color;
                String phaseKey;
                if (eventId <= 15) {
                    color = Formatting.GREEN;
                    phaseKey = "nice";
                } else if (eventId <= 30) {
                    color = Formatting.YELLOW;
                    phaseKey = "transition";
                } else if (eventId <= 45) {
                    color = Formatting.RED;
                    phaseKey = "hostile";
                } else {
                    color = Formatting.LIGHT_PURPLE;
                    phaseKey = "jumpscare";
                }

                // display each event with its number, name, and phase (phase localizes per client)
                context.getSource().sendMessage(
                    Text.literal(String.format("%2d. %-25s [", eventId, eventName))
                        .append(Text.translatable("command.nullpointerentity.list.phase." + phaseKey))
                        .append(Text.literal("]"))
                        .formatted(color)
                );
            });

        context.getSource().sendMessage(Text.literal("").formatted(Formatting.WHITE));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.list.footer").formatted(Formatting.GRAY));
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

        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.header").formatted(Formatting.AQUA));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.current", currentProgress).formatted(Formatting.WHITE));
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.next", nextEvent).formatted(Formatting.YELLOW));

        // show phase progress
        if (currentProgress <= 15) {
            context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.phase.nice", currentProgress).formatted(Formatting.GREEN));
        } else if (currentProgress <= 30) {
            context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.phase.transition", currentProgress - 15).formatted(Formatting.YELLOW));
        } else if (currentProgress <= 45) {
            context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.phase.hostile", currentProgress - 30).formatted(Formatting.RED));
        } else if (currentProgress <= 60) {
            context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.phase.jumpscare", currentProgress - 45).formatted(Formatting.DARK_RED));
        } else {
            context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.phase.complete").formatted(Formatting.LIGHT_PURPLE));
        }

        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.reset_hint").formatted(Formatting.GRAY));
        return 1;
    }

    private static int resetPlayerProgress(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        // reset the player's progress using eventtriggersystem
        EventTriggerSystem.resetPlayerProgress(player);
        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.progress.reset_done").formatted(Formatting.GREEN));
        return 1;
    }

    private static int skipToEvent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int eventNumber = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "eventNumber");

        // validate event number
        if (eventNumber < 1 || eventNumber > 60) {
            context.getSource().sendError(Text.translatable("command.nullpointerentity.skip.invalid"));
            return 0;
        }

        // get the event name from the chronological events array
        String eventName = EventTriggerSystem.getEventNameByIndex(eventNumber - 1);

        // actually trigger the event immediately using the same system as automatic events
        EventTriggerSystem.triggerSpecificEvent(player, eventNumber, eventName);

        // set player progress to after this event (so next automatic event will be the following one)
        EventTriggerSystem.setPlayerEventProgress(player, eventNumber);

        context.getSource().sendMessage(Text.translatable("command.nullpointerentity.skip.triggered", eventNumber, getEventNameById(eventNumber)).formatted(Formatting.GREEN));

        // check if this is the final event (40) and adjust the message accordingly
        if (eventNumber >= 60) {
            context.getSource().sendMessage(Text.translatable("command.nullpointerentity.skip.final", eventNumber).formatted(Formatting.YELLOW));
        } else {
            context.getSource().sendMessage(Text.translatable("command.nullpointerentity.skip.next", eventNumber, eventNumber + 1).formatted(Formatting.YELLOW));
        }
        return 1;
    }
}
