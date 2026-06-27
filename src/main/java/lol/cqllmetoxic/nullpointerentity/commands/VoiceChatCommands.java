package lol.cqllmetoxic.nullpointerentity.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lol.cqllmetoxic.nullpointerentity.config.VoiceChatConfig;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * registers the /nullpointer voicechat command for toggling voice chat integration.
 * allows checking and changing voice chat settings from in-game.
 */
public class VoiceChatCommands {

    /**
     * registers voice chat-related commands.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("nullpointer")
            .then(CommandManager.literal("voicechat")
                .executes(VoiceChatCommands::executeVoiceChatStatus)
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                    .executes(VoiceChatCommands::executeVoiceChatToggle)
                )
            )
            .then(CommandManager.literal("pushtotalk")
                .executes(VoiceChatCommands::executePushToTalkStatus)
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                    .executes(VoiceChatCommands::executePushToTalkToggle)
                )
            )
        );
    }

    private static int executeVoiceChatStatus(CommandContext<ServerCommandSource> context) {
        boolean voiceChatEnabled = VoiceChatConfig.isVoiceChatEnabled();
        Text statusText = voiceChatEnabled
            ? Text.translatable("command.nullpointerentity.voicechat.status.enabled").formatted(Formatting.GREEN)
            : Text.translatable("command.nullpointerentity.voicechat.status.disabled").formatted(Formatting.RED);

        context.getSource().sendFeedback(() -> statusText, false);
        context.getSource().sendFeedback(() -> Text.translatable("command.nullpointerentity.voicechat.usage"), false);

        return 1;
    }

    private static int executeVoiceChatToggle(CommandContext<ServerCommandSource> context) {
        boolean newValue = BoolArgumentType.getBool(context, "enabled");
        VoiceChatConfig.setVoiceChatEnabled(newValue);

        Text feedbackText = newValue
            ? Text.translatable("command.nullpointerentity.voicechat.toggle.enabled").formatted(Formatting.GREEN)
            : Text.translatable("command.nullpointerentity.voicechat.toggle.disabled").formatted(Formatting.RED);

        context.getSource().sendFeedback(() -> feedbackText, false);

        return 1;
    }

    private static int executePushToTalkStatus(CommandContext<ServerCommandSource> context) {
        boolean pushToTalkEnabled = VoiceChatConfig.isPushToTalkEnabled();
        Text statusText = pushToTalkEnabled
            ? Text.translatable("command.nullpointerentity.pushtotalk.status.enabled").formatted(Formatting.GREEN)
            : Text.translatable("command.nullpointerentity.pushtotalk.status.disabled").formatted(Formatting.RED);

        context.getSource().sendFeedback(() -> statusText, false);
        context.getSource().sendFeedback(() -> Text.translatable("command.nullpointerentity.pushtotalk.usage"), false);

        return 1;
    }

    private static int executePushToTalkToggle(CommandContext<ServerCommandSource> context) {
        boolean newValue = BoolArgumentType.getBool(context, "enabled");
        VoiceChatConfig.setPushToTalkEnabled(newValue);

        Text feedbackText = newValue
            ? Text.translatable("command.nullpointerentity.pushtotalk.toggle.enabled").formatted(Formatting.GREEN)
            : Text.translatable("command.nullpointerentity.pushtotalk.toggle.disabled").formatted(Formatting.RED);

        context.getSource().sendFeedback(() -> feedbackText, false);

        return 1;
    }
}

