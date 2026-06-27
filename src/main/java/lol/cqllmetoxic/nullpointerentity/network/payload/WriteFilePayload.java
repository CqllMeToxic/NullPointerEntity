package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * S2C: asks the receiving client to write a server-computed file on its own machine, so events whose
 * content depends on server-authoritative data (e.g. the phase-1 analysis files built from Minecraft
 * statistics) land on each player's OWN computer, not just the host's. the client write goes
 * through {@code SystemInteractionHandler.createSystemFileInCommonLocation}, which is privacy-gated,
 * so nothing is written when Privacy Mode is on.
 */
public record WriteFilePayload(String fileName, String content, String location) implements CustomPayload {
    public static final Id<WriteFilePayload> ID = new Id<>(PacketIds.WRITE_FILE);
    public static final PacketCodec<RegistryByteBuf, WriteFilePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING,
        WriteFilePayload::fileName,
        PacketCodecs.STRING,
        WriteFilePayload::content,
        PacketCodecs.STRING,
        WriteFilePayload::location,
        WriteFilePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
