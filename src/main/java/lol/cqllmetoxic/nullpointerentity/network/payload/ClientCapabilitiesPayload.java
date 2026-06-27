package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ClientCapabilitiesPayload(boolean supportsEffects) implements CustomPayload {
    public static final Id<ClientCapabilitiesPayload> ID = new Id<>(PacketIds.CLIENT_CAPABILITIES);
    public static final PacketCodec<RegistryByteBuf, ClientCapabilitiesPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        ClientCapabilitiesPayload::supportsEffects,
        ClientCapabilitiesPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}


