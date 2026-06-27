package lol.cqllmetoxic.nullpointerentity.entity;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.network.ServerNetworking;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;
import org.joml.Vector3f;

import java.util.Timer;
import java.util.TimerTask;

/**
 * creates atmospheric effects when the nullpointer entity is active.
 * triggers weather changes, red rain, and particle effects.
 * makes the environment feel hostile and corrupted.
 */
public class NullPointerEntityEnvironment {
    
    private static Timer currentEffectTimer = null;

    /**
     * activates hostile environmental effects around the target player.
     * starts thunderstorm, enables red rain, and spawns particles.
     *
     * @param world the server world
     * @param targetPlayer the player to target with effects
     */
    public static void activateHostileEnvironment(ServerWorld world, ServerPlayerEntity targetPlayer) {
        if (world == null || targetPlayer == null) return;
        
        try {
            // start raining and thundering immediately
            world.setWeather(0, 12000, true, true); // rain for 10 minutes, with thunder

            // activate the red-rain overlay on the target player's OWN client (was host-only before)
            ServerNetworking.sendRedRain(targetPlayer, true);

            NullPointerEntity.LOGGER.info("Hostile environment activated - rain and thunder started");
            
            // activate red sky and rain effects
            activateRedSkyAndRain(world, targetPlayer);

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Error activating hostile environment: {}", e.getMessage());
        }
    }
    
    private static void activateRedSkyAndRain(ServerWorld world, ServerPlayerEntity targetPlayer) {
        // cancel any existing effect timer
        if (currentEffectTimer != null) {
            currentEffectTimer.cancel();
        }

        currentEffectTimer = new Timer();
        currentEffectTimer.scheduleAtFixedRate(new TimerTask() {
            private int duration = 0;
            
            @Override
            public void run() {
                // run effects for 10 minutes or until player disconnects
                if (duration >= 1200 || targetPlayer.isDisconnected()) { // 10 minutes = 1200 * 50ms
                    currentEffectTimer.cancel();
                    currentEffectTimer = null;
                    return;
                }
                
                if (world != null && targetPlayer != null && !targetPlayer.isRemoved()) {
                    createRedSkyEffects(world, targetPlayer);
                    createRedRainEffects(world, targetPlayer);
                }

                duration++;
            }
        }, 500, 50); // start after 0.5 seconds, repeat every 50ms
    }

    private static void createRedSkyEffects(ServerWorld world, ServerPlayerEntity targetPlayer) {
        double playerX = targetPlayer.getX();
        double playerY = targetPlayer.getY();
        double playerZ = targetPlayer.getZ();

        // create red fog/mist effects at sky level
        for (int i = 0; i < 15; i++) {
            double x = playerX + (Math.random() - 0.5) * 80; // large area around player
            double z = playerZ + (Math.random() - 0.5) * 80;
            double y = playerY + 25 + Math.random() * 15; // high in the sky

            // create red dust particles for sky effect
            world.spawnParticles(
                new DustParticleEffect(0xFF0000, 2.0f), // bright red, large size
                x, y, z,
                3, // spawn multiple particles per location
                2.0, 1.0, 2.0, // spread area
                0.01 // slow movement
            );

            // add some darker red particles for depth
            world.spawnParticles(
                new DustParticleEffect(0xCC0000, 1.5f), // dark red
                x + (Math.random() - 0.5) * 10,
                y - Math.random() * 5,
                z + (Math.random() - 0.5) * 10,
                2,
                1.0, 0.5, 1.0,
                0.005
            );
        }

        // create red lightning-like effects
        if (Math.random() < 0.1) { // 10% chance each tick
            for (int i = 0; i < 5; i++) {
                double x = playerX + (Math.random() - 0.5) * 60;
                double z = playerZ + (Math.random() - 0.5) * 60;
                double y = playerY + 30 + Math.random() * 20;

                // bright red flash particles
                world.spawnParticles(
                    new DustParticleEffect(0xFF3333, 3.0f),
                    x, y, z,
                    8,
                    0.5, 5.0, 0.5,
                    0.1
                );
            }
        }
    }

    private static void createRedRainEffects(ServerWorld world, ServerPlayerEntity targetPlayer) {
        // let the custom rain texture handle the main rain effect
        // we'll only add subtle ambient effects that complement the textured rain

        double playerX = targetPlayer.getX();
        double playerY = targetPlayer.getY();
        double playerZ = targetPlayer.getZ();

        // only add subtle ground splash effects and ambient mist
        // remove the heavy particle rain that was blocking the texture

        // create subtle red splash effects on the ground (much reduced)
        for (int i = 0; i < 3; i++) { // reduced from 10 to 3
            double x = playerX + (Math.random() - 0.5) * 15; // reduced range
            double z = playerZ + (Math.random() - 0.5) * 15;
            double y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int)x, (int)z) + 0.1;

            // ground splash particles (subtle)
            world.spawnParticles(
                new DustParticleEffect(0x990000, 0.4f), // smaller and more transparent
                x, y, z,
                2, // reduced particle count
                0.3, 0.1, 0.3,
                0.05
            );
        }

        // add very subtle ambient red mist at ground level (much reduced)
        if (Math.random() < 0.3) { // reduced from 0.7 to 0.3
            for (int i = 0; i < 3; i++) { // reduced from 8 to 3
                double x = playerX + (Math.random() - 0.5) * 20; // reduced range
                double z = playerZ + (Math.random() - 0.5) * 20;
                double y = playerY + Math.random() * 2;

                world.spawnParticles(
                    new DustParticleEffect(0xCC1919, 0.8f), // more transparent
                    x, y, z,
                    1,
                    0.5, 0.1, 0.5, // reduced spread
                    0.01 // slower movement
                );
            }
        }
    }
    
    public static void deactivateHostileEnvironment(ServerWorld world) {
        if (world == null) return;
        
        try {
            // cancel any running effects
            if (currentEffectTimer != null) {
                currentEffectTimer.cancel();
                currentEffectTimer = null;
            }

            // deactivate the red-rain overlay on every online player's own client
            if (world.getServer() != null) {
                for (ServerPlayerEntity p : world.getServer().getPlayerManager().getPlayerList()) {
                    ServerNetworking.sendRedRain(p, false);
                }
            }

            // clear the weather
            world.setWeather(6000, 0, false, false);
            NullPointerEntity.LOGGER.info("Hostile environment deactivated - weather cleared");
            
        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Error deactivating hostile environment: {}", e.getMessage());
        }
    }
}
