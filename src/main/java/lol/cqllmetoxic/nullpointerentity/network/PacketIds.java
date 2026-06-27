package lol.cqllmetoxic.nullpointerentity.network;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.minecraft.util.Identifier;

/**
 * central packet channel ids for multiplayer synchronization.
 */
public final class PacketIds {
    public static final Identifier SESSION_POLICY = Identifier.of(NullPointerEntity.MOD_ID, "session_policy");
    public static final Identifier EFFECT_TRIGGER = Identifier.of(NullPointerEntity.MOD_ID, "effect_trigger");
    public static final Identifier CLIENT_CAPABILITIES = Identifier.of(NullPointerEntity.MOD_ID, "client_capabilities");
    public static final Identifier PRIVACY_CONSENT = Identifier.of(NullPointerEntity.MOD_ID, "privacy_consent");
    /** S2C: the host's privacy setting, which drives each client's own invasive execution */
    public static final Identifier SESSION_PRIVACY = Identifier.of(NullPointerEntity.MOD_ID, "session_privacy");
    /** S2C: tells the receiving client to run a story event locally against its own machine */
    public static final Identifier RUN_EVENT = Identifier.of(NullPointerEntity.MOD_ID, "run_event");
    /** S2C: asks the receiving client to write a server-computed file on its own machine */
    public static final Identifier WRITE_FILE = Identifier.of(NullPointerEntity.MOD_ID, "write_file");
    /** S2C: the receiving player's active passive "loss of control" effects, applied client-side */
    public static final Identifier PASSIVE_EFFECTS = Identifier.of(NullPointerEntity.MOD_ID, "passive_effects");
    /** C2S: the joining client's own local wall-clock time, so return messages use the player's time */
    public static final Identifier CLIENT_LOCAL_TIME = Identifier.of(NullPointerEntity.MOD_ID, "client_local_time");
    /** C2S: the client's own display username (anonymized under Privacy Mode), so server-side event
     *  chat/files name each player's own machine instead of the host's */
    public static final Identifier CLIENT_USERNAME = Identifier.of(NullPointerEntity.MOD_ID, "client_username");

    private PacketIds() {
    }
}

