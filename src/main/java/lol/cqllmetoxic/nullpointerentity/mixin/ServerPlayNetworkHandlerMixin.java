package lol.cqllmetoxic.nullpointerentity.mixin;

import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager;
import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (shouldSuppressChat()) {
            ci.cancel();
            sendSuppressedMessage();
        }
    }

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void onCommandExecution(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        // block specific commands that allow messaging
        String command = packet.command(); 
        if (shouldSuppressChat() && (command.startsWith("msg ") || command.startsWith("tell ") || command.startsWith("w ") || command.startsWith("me "))) {
            ci.cancel();
            sendSuppressedMessage();
        }
    }

    @Unique
    private boolean shouldSuppressChat() {
        if (player == null) return false;
        PersistentPlayerData data = PersistentDataManager.getPlayerData(player.getUuid().toString());
        return data != null && Boolean.TRUE.equals(data.triggeredEvents.get("chat_suppressed"));
    }

    @Unique
    private void sendSuppressedMessage() {
        if (player != null) {
            player.sendMessage(Text.literal("shh...").formatted(Formatting.RED, Formatting.ITALIC), true);
        }
    }
}
