package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record PrivacyConsentPayload(boolean consentGranted) implements CustomPayload {
    public static final Id<PrivacyConsentPayload> ID = new Id<>(PacketIds.PRIVACY_CONSENT);
    public static final PacketCodec<RegistryByteBuf, PrivacyConsentPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        PrivacyConsentPayload::consentGranted,
        PrivacyConsentPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}


