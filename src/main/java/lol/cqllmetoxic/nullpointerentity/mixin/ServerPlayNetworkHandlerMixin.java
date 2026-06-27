package lol.cqllmetoxic.nullpointerentity.mixin;

import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager;
import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData;
import lol.cqllmetoxic.nullpointerentity.events.GlobalFreezeController;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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

    // regular chat isn't suppressed here: cancelling onChatMessage at HEAD skips vanilla's chat
    // acknowledgment/signature processing, which desyncs the 1.21 "last seen messages" chain and kicks
    // the player with disconnect.chat_validation_failed the next time they chat (e.g. when event 41's
    // silence lifts). suppression happens via ServerMessageEvents.ALLOW_CHAT_MESSAGE (registered in
    // NullPointerEntity), which fires after vanilla has validated + acked the message, so the chain stays
    // intact. command-sourced messages (/msg, /tell, ...) carry no signature chain in 1.21, so cancelling
    // those at HEAD below is fine.

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void onCommandExecution(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        // block specific commands that allow messaging
        String command = packet.command(); 
        if (shouldSuppressChat() && (command.startsWith("msg ") || command.startsWith("tell ") || command.startsWith("w ") || command.startsWith("me "))) {
            ci.cancel();
            sendSuppressedMessage();
        }
    }

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (shouldBlockForFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (shouldBlockForFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerInteractBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        if (shouldBlockForFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerInteractItem", at = @At("HEAD"), cancellable = true)
    private void onPlayerInteractItem(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        if (shouldBlockForFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    private void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if (shouldBlockForFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "onHandSwing", at = @At("HEAD"), cancellable = true)
    private void onHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        if (shouldBlockForFreeze()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean shouldSuppressChat() {
        if (player == null) return false;
        PersistentPlayerData data = PersistentDataManager.getPlayerData(player.getUuid().toString());
        return data != null && Boolean.TRUE.equals(data.triggeredEvents.get("chat_suppressed"));
    }

    @Unique
    private boolean shouldBlockForFreeze() {
        return player != null && GlobalFreezeController.isFrozen();
    }

    @Unique
    private void sendSuppressedMessage() {
        if (player != null) {
            player.sendMessage(Text.translatable("message.nullpointerentity.chat_suppressed").formatted(Formatting.RED, Formatting.ITALIC), true);
        }
    }
}
