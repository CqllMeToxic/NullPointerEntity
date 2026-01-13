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
        Text statusText = privacyEnabled ?
            Text.literal("Privacy Mode: ENABLED - Personally identifiable information will be randomized").formatted(Formatting.GREEN) :
            Text.literal("Privacy Mode: DISABLED - Personal information will be shown. Use '/nullpointer privacy true' to hide personal information").formatted(Formatting.RED);
        
        context.getSource().sendFeedback(() -> statusText, false);
        context.getSource().sendFeedback(() -> Text.literal("Use '/nullpointer privacy true' to enable or '/nullpointer privacy false' to disable"), false);
        
        return 1;
    }

    private static int executePrivacyToggle(CommandContext<ServerCommandSource> context) {
        boolean newValue = BoolArgumentType.getBool(context, "enabled");
        PrivacyManager.setPrivacyEnabled(newValue);
        
        Text feedbackText = newValue ?
            Text.literal("Privacy Mode ENABLED - Personal information will now be randomized").formatted(Formatting.GREEN) :
            Text.literal("Privacy Mode DISABLED - Personal information will be shown.").formatted(Formatting.RED);
            
        context.getSource().sendFeedback(() -> feedbackText, false);
        
        if (!newValue) {
            context.getSource().sendFeedback(() -> 
                Text.literal("WARNING: NullPointerEntity will now use randomized data instead of your real information").formatted(Formatting.YELLOW), false);
        }
        
        return 1;
    }
}
