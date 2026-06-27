package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * S2C: the host's Privacy Mode setting, which becomes the session-wide setting that drives each
 * client's own invasive event execution (host priority).
 */
public record SessionPrivacyPayload(boolean privacyEnabled) implements CustomPayload {
    public static final Id<SessionPrivacyPayload> ID = new Id<>(PacketIds.SESSION_PRIVACY);
    public static final PacketCodec<RegistryByteBuf, SessionPrivacyPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        SessionPrivacyPayload::privacyEnabled,
        SessionPrivacyPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
