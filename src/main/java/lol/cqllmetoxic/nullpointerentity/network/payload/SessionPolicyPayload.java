package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SessionPolicyPayload(String policy) implements CustomPayload {
    public static final Id<SessionPolicyPayload> ID = new Id<>(PacketIds.SESSION_POLICY);
    public static final PacketCodec<RegistryByteBuf, SessionPolicyPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING,
        SessionPolicyPayload::policy,
        SessionPolicyPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

