package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * C2S: the client reports its own local wall-clock time (hour 0-23, minute 0-59) so that
 * "welcome back" return messages show the joining player's local time and time-of-day greeting
 * instead of the host's clock.
 */
public record ClientLocalTimePayload(int hour, int minute) implements CustomPayload {
    public static final Id<ClientLocalTimePayload> ID = new Id<>(PacketIds.CLIENT_LOCAL_TIME);
    public static final PacketCodec<RegistryByteBuf, ClientLocalTimePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        ClientLocalTimePayload::hour,
        PacketCodecs.VAR_INT,
        ClientLocalTimePayload::minute,
        ClientLocalTimePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
