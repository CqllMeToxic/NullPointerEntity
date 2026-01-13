package lol.cqllmetoxic.nullpointerentity.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * manages rendering data for fake player entities.
 * tracks positions and rotation for client-side rendering.
 * stores list of active fake players in the world.
 */
public class FakePlayerRenderer {
    private static final List<FakePlayer> fakePlayers = new ArrayList<>();
    private static final Identifier NULLPOINTERENTITY_TEXTURE = Identifier.of("nullpointerentity", "textures/entity/player/nullpointer_entity.png");

    /**
     * stores rendering information for a single fake player.
     */
    public static class FakePlayer {
        public Vec3d position;
        public float yaw;
        public float pitch;
        public String username;
        public int ticksExisted;
        public int maxTicks;
        public boolean isGlitching;
        public float opacity;
        public Identifier texture;

        public FakePlayer(Vec3d position, float yaw, float pitch, int duration) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.username = "NullPointerEntity";
            this.ticksExisted = 0;
            this.maxTicks = duration;
            this.isGlitching = true; // always glitchy for scary effect
            this.opacity = 1.0f;
            this.texture = NULLPOINTERENTITY_TEXTURE;
        }
    }

    public static void initialize() {
        // simplified initialization without entity models
    }

    public static void spawnNullPointerEntity(Vec3d position, float yaw, float pitch, int durationTicks) {
        FakePlayer nullPointerEntity = new FakePlayer(position, yaw, pitch, durationTicks);
        fakePlayers.add(nullPointerEntity);
    }

    public static void tick() {
        Iterator<FakePlayer> iterator = fakePlayers.iterator();
        while (iterator.hasNext()) {
            FakePlayer fakePlayer = iterator.next();
            fakePlayer.ticksExisted++;

            // fade out effect
            if (fakePlayer.ticksExisted > fakePlayer.maxTicks - 20) {
                fakePlayer.opacity = Math.max(0, 1.0f - (fakePlayer.ticksExisted - (fakePlayer.maxTicks - 20)) / 20.0f);
            }

            // glitch effects
            if (fakePlayer.isGlitching) {
                fakePlayer.position = fakePlayer.position.add(
                    (Math.random() - 0.5) * 0.1,
                    (Math.random() - 0.5) * 0.05,
                    (Math.random() - 0.5) * 0.1
                );
                fakePlayer.yaw += (Math.random() - 0.5) * 10;
            }

            // remove expired fake players
            if (fakePlayer.ticksExisted >= fakePlayer.maxTicks) {
                iterator.remove();
            }
        }
    }

    public static void clearAll() {
        fakePlayers.clear();
    }
}
