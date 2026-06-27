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

        // send launcher info to the player. the root aqua style colours the %s launcher name - the
        // value's §-codes don't reach the substituted arg, so the launcher name would render white without this.
        context.getSource().sendFeedback(
            () -> Text.translatable("command.nullpointerentity.launcher.using", launcherName)
                .formatted(net.minecraft.util.Formatting.AQUA),
            false
        );

        // additional info based on launcher type
        String additionalInfoKey = getAdditionalLauncherInfo(launcher);
        if (additionalInfoKey != null) {
            context.getSource().sendFeedback(
                () -> Text.translatable(additionalInfoKey),
                false
            );
        }

        return 1;
    }

    private static String getAdditionalLauncherInfo(LauncherDetection.Launcher launcher) {
        return switch (launcher) {
            case FEATHER_LUNAR -> "command.nullpointerentity.launcher.info.feather_lunar";
            case MULTIMC -> "command.nullpointerentity.launcher.info.multimc";
            case MODRINTH -> "command.nullpointerentity.launcher.info.modrinth";
            case CURSEFORGE -> "command.nullpointerentity.launcher.info.curseforge";
            case ATLAUNCHER -> "command.nullpointerentity.launcher.info.atlauncher";
            case TECHNIC -> "command.nullpointerentity.launcher.info.technic";
            case GDLAUNCHER -> "command.nullpointerentity.launcher.info.gdlauncher";
            case MCUPDATER -> "command.nullpointerentity.launcher.info.mcupdater";
            case VANILLA -> "command.nullpointerentity.launcher.info.vanilla";
        };
    }
}
