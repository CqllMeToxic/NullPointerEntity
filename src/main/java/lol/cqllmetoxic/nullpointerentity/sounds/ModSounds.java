package lol.cqllmetoxic.nullpointerentity.sounds;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * registers all custom sound effects used in the mod.
 * these get loaded from the assets folder and can be played during events.
 * includes various scare sounds, heartbeats, static, and ambient effects.
 */
public class ModSounds {
    /** loud scream sound - used in jumpscare events */
    public static final SoundEvent JUMPSCARE_SCREAM = registerSound("jumpscare.scream");
    /** static/white noise - used for glitch effects */
    public static final SoundEvent JUMPSCARE_STATIC = registerSound("jumpscare.static");
    /** creepy whispering - used for subtle scares */
    public static final SoundEvent JUMPSCARE_WHISPER = registerSound("jumpscare.whisper");
    /** slower heartbeat - used during tense moments */
    public static final SoundEvent JUMPSCARE_HEARTBEAT_CALM = registerSound("jumpscare.heartbeat_calm");
    /** faster heartbeat - used during intense scares */
    public static final SoundEvent JUMPSCARE_HEARTBEAT_TENSE = registerSound("jumpscare.heartbeat_tense");
    /** digital glitch sound - used for reality corruption effects */
    public static final SoundEvent JUMPSCARE_GLITCH = registerSound("jumpscare.glitch");
    /** chase music - used when nullpointer is active */
    public static final SoundEvent JUMPSCARE_CHASE = registerSound("jumpscare.chase");
    /** bluescreen static - used for bsod/system crash effects */
    public static final SoundEvent JUMPSCARE_BLUESCREEN_STATIC = registerSound("jumpscare.bluescreen_static");

    /**
     * helper method to register a sound with the game.
     * creates an identifier and adds it to minecraft's sound registry.
     *
     * @param name the sound name (matches the json file in sounds.json)
     * @return the registered SoundEvent
     */
    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.of(NullPointerEntity.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    /**
     * called during mod initialization to register all sounds.
     * just logs that we're registering - actual registration happens in static initialization.
     */
    public static void registerSounds() {
        NullPointerEntity.LOGGER.info("Registering custom sounds for " + NullPointerEntity.MOD_ID);
    }
}

