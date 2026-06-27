package lol.cqllmetoxic.nullpointerentity;

import lol.cqllmetoxic.nullpointerentity.aurora.AuroraEventSystem;
import lol.cqllmetoxic.nullpointerentity.commands.AuroraCommands;
import lol.cqllmetoxic.nullpointerentity.commands.LauncherCommand;
import lol.cqllmetoxic.nullpointerentity.commands.PrivacyCommands;
import lol.cqllmetoxic.nullpointerentity.commands.VoiceChatCommands;
import lol.cqllmetoxic.nullpointerentity.events.PassiveEvents;
import lol.cqllmetoxic.nullpointerentity.events.PlayerTrackingEvents;
import lol.cqllmetoxic.nullpointerentity.events.GlobalFreezeController;
import lol.cqllmetoxic.nullpointerentity.events.ShriekSpeechBridge;
import lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager;
import lol.cqllmetoxic.nullpointerentity.item.ModdedItemGroups;
import lol.cqllmetoxic.nullpointerentity.item.ModdedItems;
import lol.cqllmetoxic.nullpointerentity.network.ConsentState;
import lol.cqllmetoxic.nullpointerentity.network.ServerNetworking;
import lol.cqllmetoxic.nullpointerentity.util.LauncherDetection;
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
	/** last broadcast host privacy state; used to push session-privacy updates when the host toggles it */
	private static Boolean lastSessionPrivacy = null;

	/**
	 * the Windows username to DISPLAY in chat and file contents: a stable anonymized name while
	 * Privacy Mode is on, the real username otherwise. Privacy Mode doesn't block the actions
	 * themselves (files still write, PowerShell still runs) - it only swaps in randomized info.
	 * real system operations should still use the raw {@link #WINDOWS_USERNAME}.
	 */
	public static String getDisplayUsername() {
		String real = WINDOWS_USERNAME != null ? WINDOWS_USERNAME : System.getProperty("user.name");
		return lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.getSystemUsername(real);
	}

	/**
	 * the display username for a specific player - their OWN reported name (anonymized under Privacy
	 * Mode) so multiplayer event chat/files name each player's machine, not the host's. falls back to
	 * {@link #getDisplayUsername()} (the local/host name) if the client hasn't reported one yet.
	 */
	public static String getDisplayUsername(net.minecraft.server.network.ServerPlayerEntity player) {
		if (player != null) {
			String reported = lol.cqllmetoxic.nullpointerentity.network.ClientUsernameState.get(player.getUuid());
			if (reported != null) {
				return reported;
			}
		}
		return getDisplayUsername();
	}

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
		ShriekSpeechBridge.initialize();
		ServerNetworking.registerPayloadTypes();
		ServerNetworking.registerReceivers();

		ModdedItems.registerModdedItems();
		ModdedItemGroups.registerModdedItemGroups();

		lol.cqllmetoxic.nullpointerentity.entity.ModEntities.registerEntities();

		/** hook up all the slash commands */
		CommandRegistrationCallback.EVENT.register(AuroraCommands::register);
		CommandRegistrationCallback.EVENT.register(PrivacyCommands::register);
		CommandRegistrationCallback.EVENT.register(VoiceChatCommands::register);
		CommandRegistrationCallback.EVENT.register(LauncherCommand::register);

		/** event 41 (mouth_shut): silence the player's chat. has to be ALLOW_CHAT_MESSAGE - by the time
		 *  this fires vanilla already validated the message and bumped the 1.21 chat ack chain, so returning
		 *  false just drops the broadcast and never desyncs the chain (which is what kicked people with
		 *  chat_validation_failed once the silence lifted). */
		net.fabricmc.fabric.api.message.v1.ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData data =
				lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(sender.getUuid().toString());
			if (data != null && Boolean.TRUE.equals(data.triggeredEvents.get("chat_suppressed"))) {
				sender.sendMessage(net.minecraft.text.Text.translatable("message.nullpointerentity.chat_suppressed")
					.formatted(net.minecraft.util.Formatting.RED, net.minecraft.util.Formatting.ITALIC), true);
				return false;
			}
			return true;
		});

		/** listen for chat messages to trigger entity responses */
		net.fabricmc.fabric.api.message.v1.ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			lol.cqllmetoxic.nullpointerentity.events.ChatResponseSystem.handleChatMessage(sender, message.getContent().getString());
		});

		/** boot up the persistent data system once the server is running */
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.initialize(server);
		});

		/** save all tracking data when the game closes */
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.saveData();
		});

		/** grab minecraft username and set up player-specific systems on join */
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			MINECRAFT_USERNAME = handler.getPlayer().getName().getString();
			ServerNetworking.sendSessionPolicy(handler.getPlayer());
			/** the host's Privacy Mode drives each client's own invasive execution (host priority) */
			ServerNetworking.sendSessionPrivacy(handler.getPlayer());

			/** boot up tracking (the shared story runs in multiplayer too) */
			lol.cqllmetoxic.nullpointerentity.tracking.PlayerTrackingSystem.initializePlayer(handler.getPlayer());
			PlayerTrackingEvents.initialize();

			/** record this play session */
			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.onPlayerJoin(handler.getPlayer().getUuid());

			/** handle fake player entities saying stuff when you log back in */
			FakePlayerManager.onPlayerJoin(handler.getPlayer());
		});

		/** clean up when player leaves (singleplayer only) */
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ConsentState.clear(handler.getPlayer().getUuid());
			lol.cqllmetoxic.nullpointerentity.network.ClientLocalTimeState.clear(handler.getPlayer().getUuid());
			lol.cqllmetoxic.nullpointerentity.network.ClientUsernameState.clear(handler.getPlayer().getUuid());
			ShriekSpeechBridge.onPlayerDisconnect(handler.getPlayer().getUuid());

			lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.onPlayerLeave(handler.getPlayer().getUuid(), 0);
			FakePlayerManager.onPlayerDisconnect(handler.getPlayer().getUuid());

			/** clear out chat memory for this player */
			lol.cqllmetoxic.nullpointerentity.events.ChatResponseSystem.onPlayerDisconnect(handler.getPlayer().getName().getString());
		});

		/** runs every tick to update fake players and keep the host's session privacy in sync */
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			GlobalFreezeController.tick(server);

			/** when the host toggles Privacy Mode mid-session, push the new setting to every client */
			boolean hostPrivacy = lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabledRaw();
			if (lastSessionPrivacy == null || lastSessionPrivacy != hostPrivacy) {
				lastSessionPrivacy = hostPrivacy;
				ServerNetworking.broadcastSessionPrivacy(server);
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
