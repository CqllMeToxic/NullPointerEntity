package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * C2S: the client reports its own display username (already anonymized to "User####" when the
 * session's Privacy Mode is on, or the real OS username when it's off). the server uses it so that
 * server-side event chat/files name each player's own machine instead of the host's. the client
 * re-sends this whenever the (host-driven) privacy state changes, so the value always reflects the
 * current mode - under Privacy Mode only the anonymized name ever leaves the client.
 */
public record ClientUsernamePayload(String displayName) implements CustomPayload {
    public static final Id<ClientUsernamePayload> ID = new Id<>(PacketIds.CLIENT_USERNAME);
    public static final PacketCodec<RegistryByteBuf, ClientUsernamePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING,
        ClientUsernamePayload::displayName,
        ClientUsernamePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
