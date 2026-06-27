package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record EffectTriggerPayload(String effectType, long durationMs) implements CustomPayload {
    public static final Id<EffectTriggerPayload> ID = new Id<>(PacketIds.EFFECT_TRIGGER);
    public static final PacketCodec<RegistryByteBuf, EffectTriggerPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING,
        EffectTriggerPayload::effectType,
        PacketCodecs.VAR_LONG,
        EffectTriggerPayload::durationMs,
        EffectTriggerPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

