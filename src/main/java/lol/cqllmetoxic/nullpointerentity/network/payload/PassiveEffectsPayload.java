package lol.cqllmetoxic.nullpointerentity.network.payload;

import lol.cqllmetoxic.nullpointerentity.network.PacketIds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * S2C: the receiving player's currently active passive "loss of control" effects, so the client-side
 * input/render mixins can apply them on that player's OWN machine. carries the per-player state to every
 * client, since the input/render mixins can't reach the server's static {@code PassiveEvents} state
 * outside the host's shared JVM.
 *
 * <p>{@code flags} is a bitmask (see the constants); {@code tintPacked} is 0xRRGGBB.
 */
public record PassiveEffectsPayload(int flags, int tintPacked, float mouseSensitivityMultiplier) implements CustomPayload {
    public static final int INPUT_INVERSION = 1;
    public static final int MOVEMENT_LAG = 1 << 1;
    public static final int GRAVITY_FLUCTUATION = 1 << 2;
    public static final int BLOCK_BREAK_DELAY = 1 << 3;
    public static final int CAMERA_SHAKE = 1 << 4;

    public static final Id<PassiveEffectsPayload> ID = new Id<>(PacketIds.PASSIVE_EFFECTS);
    public static final PacketCodec<RegistryByteBuf, PassiveEffectsPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        PassiveEffectsPayload::flags,
        PacketCodecs.VAR_INT,
        PassiveEffectsPayload::tintPacked,
        PacketCodecs.FLOAT,
        PassiveEffectsPayload::mouseSensitivityMultiplier,
        PassiveEffectsPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
