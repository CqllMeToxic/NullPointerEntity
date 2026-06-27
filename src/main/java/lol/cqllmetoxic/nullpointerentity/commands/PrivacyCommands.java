package lol.cqllmetoxic.nullpointerentity.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * registers the /nullpointer privacy command for toggling privacy settings.
 * allows checking and changing privacy mode from in-game.
 */
public class PrivacyCommands {

    /**
     * registers privacy-related commands.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("nullpointer")
            .then(CommandManager.literal("privacy")
                .executes(context -> executePrivacyStatus(context))
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> executePrivacyToggle(context))
                )
            )
        );
    }

    private static int executePrivacyStatus(CommandContext<ServerCommandSource> context) {
        boolean privacyEnabled = PrivacyManager.isPrivacyEnabled();
        Text statusText = privacyEnabled
            ? Text.translatable("command.nullpointerentity.privacy.status.enabled").formatted(Formatting.GREEN)
            : Text.translatable("command.nullpointerentity.privacy.status.disabled").formatted(Formatting.RED);

        context.getSource().sendFeedback(() -> statusText, false);
        context.getSource().sendFeedback(() -> Text.translatable("command.nullpointerentity.privacy.usage"), false);

        return 1;
    }

    private static int executePrivacyToggle(CommandContext<ServerCommandSource> context) {
        boolean newValue = BoolArgumentType.getBool(context, "enabled");
        PrivacyManager.setPrivacyEnabled(newValue);

        Text feedbackText = newValue
            ? Text.translatable("command.nullpointerentity.privacy.toggle.enabled").formatted(Formatting.GREEN)
            : Text.translatable("command.nullpointerentity.privacy.toggle.disabled").formatted(Formatting.RED);

        context.getSource().sendFeedback(() -> feedbackText, false);

        if (!newValue) {
            context.getSource().sendFeedback(
                () -> Text.translatable("command.nullpointerentity.privacy.warning").formatted(Formatting.YELLOW),
                false
            );
        }

        return 1;
    }
}
