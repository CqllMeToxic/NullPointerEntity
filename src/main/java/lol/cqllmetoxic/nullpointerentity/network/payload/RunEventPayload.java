package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * S2C: tells the receiving client to run a story event locally, against its own machine, so each
 * player sees their own data on their own client. the data never travels back to the server.
 */
public record RunEventPayload(int eventId) implements CustomPayload {
    public static final Id<RunEventPayload> ID = new Id<>(PacketIds.RUN_EVENT);
    public static final PacketCodec<RegistryByteBuf, RunEventPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        RunEventPayload::eventId,
        RunEventPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
