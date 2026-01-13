package lol.cqllmetoxic.nullpointerentity.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lol.cqllmetoxic.nullpointerentity.util.LauncherDetection;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * registers the /launcher command to display which minecraft launcher is being used.
 * useful for debugging and testing launcher detection.
 */
public class LauncherCommand {

    /**
     * registers the launcher info command.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("launcher")
            .executes(LauncherCommand::executeLauncherInfo));
    }

    private static int executeLauncherInfo(CommandContext<ServerCommandSource> context) {
        LauncherDetection.Launcher launcher = LauncherDetection.getLauncher();
        String launcherName = launcher.getDisplayName();

        // send launcher info to player
        context.getSource().sendFeedback(
            () -> Text.literal("§fYou are using: §b" + launcherName),
            false
        );

        // additional info based on launcher type
        String additionalInfo = getAdditionalLauncherInfo(launcher);
        if (additionalInfo != null) {
            context.getSource().sendFeedback(
                () -> Text.literal("§7" + additionalInfo),
                false
            );
        }

        return 1;
    }

    private static String getAdditionalLauncherInfo(LauncherDetection.Launcher launcher) {
        return switch (launcher) {
            case FEATHER_LUNAR -> "PvP-focused client with cosmetics and performance enhancements. Not sure why you're on a PvP client for a horror mod, but you do you.";
            case MULTIMC -> "MultiMC-based launchers offer great instance management.";
            case MODRINTH -> "Modrinth provides a curated mod experience.";
            case CURSEFORGE -> "CurseForge offers the a huge mod library.";
            case ATLAUNCHER -> "ATLauncher specializes in modpack automation.";
            case TECHNIC -> "Technic Platform focuses on easy modpack installation.";
            case GDLAUNCHER -> "GDLauncher combines features from multiple launchers.";
            case MCUPDATER -> "MCUpdater provides automatic modpack updates.";
            case VANILLA -> "Using the default Minecraft launcher.";
        };
    }
}
