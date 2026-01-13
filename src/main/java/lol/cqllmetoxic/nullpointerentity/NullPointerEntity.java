package lol.cqllmetoxic.nullpointerentity;

import lol.cqllmetoxic.nullpointerentity.aurora.AuroraEventSystem;
import lol.cqllmetoxic.nullpointerentity.commands.AuroraCommands;
import lol.cqllmetoxic.nullpointerentity.commands.LauncherCommand;
import lol.cqllmetoxic.nullpointerentity.commands.PrivacyCommands;
import lol.cqllmetoxic.nullpointerentity.events.PassiveEvents;
import lol.cqllmetoxic.nullpointerentity.events.PlayerTrackingEvents;
import lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager;
import lol.cqllmetoxic.nullpointerentity.item.ModdedItemGroups;
import lol.cqllmetoxic.nullpointerentity.item.ModdedItems;
import lol.cqllmetoxic.nullpointerentity.util.LauncherDetection;
import lol.cqllmetoxic.nullpointerentity.util.MultiplayerDetection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main mod entry point - handles initialization of all game systems.
 * sets up commands, event listeners, and registers custom content.
 * also detects the launcher being used and stores usernames for later use.
 */
public class NullPointerEntity implements ModInitializer {
	public static final String MOD_ID = "nullpointerentity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static String WINDOWS_USERNAME;
	public static String MINECRAFT_USERNAME;

	@Override
	public void onInitialize() {
		WINDOWS_USERNAME = System.getProperty("user.name");

		/** detect launcher n dat innit */
		LauncherDetection.Launcher launcher = LauncherDetection.detectLauncher();
		LOGGER.info("NullPointerEntity initialized");
		LOGGER.info("Detected Launcher: {}", launcher.getDisplayName());

        /** set up all the core systems */

		/** load sound files into the game */
		lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.registerSounds();

		AuroraEventSystem.initialize();

		PassiveEvents.initialize();

		ModdedItems.registerModdedItems();
		ModdedItemGroups.registerModdedItemGroups();

		lol.cqllmetoxic.nullpointerentity.entity.ModEntities.registerEntities();

		/** hook up all the slash commands */
		CommandRegistrationCallback.EVENT.register(AuroraCommands::register);
		CommandRegistrationCallback.EVENT.register(PrivacyCommands::register);
		CommandRegistrationCallback.EVENT.register(LauncherCommand::register);

		/** listen for chat messages to trigger entity responses */
		net.fabricmc.fabric.api.message.v1.ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			if (!MultiplayerDetection.shouldDisableModForPlayer(sender)) {
				lol.cqllmetoxic.nullpointerentity.events.ChatResponseSystem.handleChatMessage(sender, message.getContent().getString());
			}
		});

		/** boot up the persistent data system once the server is running */
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (MultiplayerDetection.shouldDisableMod(server)) {
				return;
			}

			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.initialize(server);
		});

		/** save all tracking data when the game closes (singleplayer only) */
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			if (MultiplayerDetection.shouldDisableMod(server)) {
				/** skip data saving in multiplayer for safety */
				return;
			}

			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.saveData();
		});

		/** grab minecraft username and set up player-specific systems on join */
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			MINECRAFT_USERNAME = handler.getPlayer().getName().getString();

			if (MultiplayerDetection.shouldDisableModForPlayer(handler.getPlayer())) {
				/** let the player know why the mod isn't active */
				handler.getPlayer().sendMessage(
					net.minecraft.text.Text.literal("§7[NullPointerEntity] §cMod disabled in multiplayer for privacy and safety reasons."),
					false
				);
				return;
			}

			/** boot up tracking for singleplayer sessions */
			lol.cqllmetoxic.nullpointerentity.tracking.PlayerTrackingSystem.initializePlayer(handler.getPlayer());
			PlayerTrackingEvents.initialize();

			/** record this play session */
			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.onPlayerJoin(handler.getPlayer().getUuid());

			/** handle fake player entities saying stuff when you log back in */
			FakePlayerManager.onPlayerJoin(handler.getPlayer());
		});

		/** clean up when player leaves (singleplayer only) */
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			if (MultiplayerDetection.shouldDisableModForPlayer(handler.getPlayer())) {
				/** don't track multiplayer sessions */
				return;
			}

			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.onPlayerLeave(handler.getPlayer().getUuid(), 0);
			FakePlayerManager.onPlayerDisconnect(handler.getPlayer().getUuid());

			/** clear out chat memory for this player */
			lol.cqllmetoxic.nullpointerentity.events.ChatResponseSystem.onPlayerDisconnect(handler.getPlayer().getName().getString());
		});

		/** runs every tick to update fake players and track rotation (singleplayer only) */
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (MultiplayerDetection.shouldDisableMod(server)) {
				/** skip tick logic in multiplayer */
				return;
			}

			FakePlayerManager.tick();
		});
	}

    /**
     * triggers the fake death screen overlay from server-side code.
     * checks if we're running on client before calling the client-only method.
     *
     * @param duration how long to show the fake death screen in milliseconds
     */
	public static void triggerFakeDeathScreen(long duration) {
		/** this will be called on client via nullpointerentityclient */
		net.fabricmc.api.EnvType env = net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType();
		if (env == net.fabricmc.api.EnvType.CLIENT) {
			NullPointerEntityClient.triggerFakeDeathScreen(duration);
		}
	}
}
