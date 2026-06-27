package lol.cqllmetoxic.nullpointerentity.entity;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * the nullpointer entity that spawns behind players.
 * appears when players turn around, tracks how long they look at it.
 * slowly approaches and triggers events when stared at too long.
 * uses player skin for rendering to look like a copy of the player.
 */
public class FakePlayerEntity extends MobEntity {
    private ServerPlayerEntity targetPlayer;
    /** every player that shares this single entity (when they're within 100 blocks of each other) */
    private final List<ServerPlayerEntity> sharedTargets = new ArrayList<>();
    private int existenceTimer = 0;
    private boolean hasBeenSeen = false;
    private int lookingTimer = 0;
    private int proximityWarningTimer = 0;
    private boolean isWalking = false;
    private int walkingTimer = 0;
    private Vec3d walkingTarget = null;
    private boolean hasPlayedSpawnSound = false;

    // invisible barrier properties
    private static final double BARRIER_DISTANCE = 4.0; // blocks
    private int barrierWarningLevel = 0;
    private long lastBarrierWarning = 0;
    private static final long BARRIER_WARNING_COOLDOWN = 3000; // 3 seconds

    public FakePlayerEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);

        // make it look and behave like a player
        this.setCustomName(Text.translatable("screen.nullpointerentity.brand").formatted(Formatting.DARK_RED));
        this.setCustomNameVisible(false);
        this.setInvulnerable(true);
        this.setSilent(false); // allow sounds for spawn effect

        // clear default ai goals by removing all existing goals
        this.goalSelector.clear(goal -> true);
        this.targetSelector.clear(goal -> true);
    }

    public static DefaultAttributeContainer.Builder createFakePlayerAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.1)
                .add(EntityAttributes.ATTACK_DAMAGE, 1.0)
                .add(EntityAttributes.FOLLOW_RANGE, 35.0);
    }

    public void setTargetPlayer(ServerPlayerEntity player) {
        this.targetPlayer = player;
        sharedTargets.clear();
        sharedTargets.add(player);

        // update persistent data when player is targeted
        PersistentDataManager.PersistentPlayerData data = PersistentDataManager.getPlayerData(player.getUuid());
        data.timesEncounteredEntity++;
        data.lastInteractionTime = System.currentTimeMillis();
        PersistentDataManager.updatePlayerData(player.getUuid(), data);
    }

    /**
     * registers an additional player who shares this one entity (used when a second player is within
     * 100 blocks of the player it spawned for, so both interact with the same entity).
     */
    public void addTargetPlayer(ServerPlayerEntity player) {
        if (player == null || sharedTargets.contains(player)) {
            return;
        }
        sharedTargets.add(player);

        PersistentDataManager.PersistentPlayerData data = PersistentDataManager.getPlayerData(player.getUuid());
        data.timesEncounteredEntity++;
        data.lastInteractionTime = System.currentTimeMillis();
        PersistentDataManager.updatePlayerData(player.getUuid(), data);
    }

    /**
     * chooses which shared player the entity reacts to this tick: whoever is looking at it (the
     * nearest such), otherwise the nearest player. returns null when no shared player is still valid.
     * for the normal single-player case this just returns that one player.
     */
    private ServerPlayerEntity selectActiveTarget() {
        sharedTargets.removeIf(p -> p == null || !p.isAlive() || p.isDisconnected() || p.getWorld() != this.getWorld());

        ServerPlayerEntity looking = null;
        double lookingDist = Double.MAX_VALUE;
        ServerPlayerEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (ServerPlayerEntity p : sharedTargets) {
            double d = p.squaredDistanceTo(this);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = p;
            }
            if (isPlayerLookingAtEntity(p) && d < lookingDist) {
                lookingDist = d;
                looking = p;
            }
        }
        return looking != null ? looking : nearest;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            return;
        }

        // pick which shared player to react to this tick (identical to before for a single player)
        targetPlayer = selectActiveTarget();
        if (targetPlayer == null) {
            return;
        }

        existenceTimer++;

        // play spawn sound once when first spawned and trigger environmental effects
        if (!hasPlayedSpawnSound && existenceTimer > 5) { // small delay to ensure proper spawning
            playSpawnSound();
            triggerEnvironmentalEffects();
            hasPlayedSpawnSound = true;
        }

        // create realistic player-like behaviors
        simulatePlayerMovement();
        createSubtleMenacingEffects();
        handlePlayerInteraction();
        handleInvisibleBarrier(); // new barrier functionality
        facePlayer();

        // remove after 60 seconds if no major interaction
        if (existenceTimer > 1200) { // 60 seconds
            this.discard();
        }
    }

    private void playSpawnSound() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // play a creepy spawn sound - using ambient cave sound for creepiness
            serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMBIENT_CAVE, SoundCategory.HOSTILE,
                1.0f, 0.5f); // lower pitch for more ominous effect

            // also play entity hurt sound for additional creepiness
            serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE,
                0.7f, 0.3f);

            NullPointerEntity.LOGGER.info("NullPointerEntity spawn sound played at {}", this.getBlockPos());
        }
    }

    private void triggerEnvironmentalEffects() {
        if (this.getWorld() instanceof ServerWorld serverWorld && targetPlayer != null) {
            // turn sky red and make it rain red
            NullPointerEntityEnvironment.activateHostileEnvironment(serverWorld, targetPlayer);
            NullPointerEntity.LOGGER.info("Environmental effects activated for NullPointerEntity spawn");
        }
    }

    private void simulatePlayerMovement() {
        // occasionally "walk" around like a real player would
        if (!isWalking && existenceTimer % 200 == 0 && Math.random() < 0.3) {
            // start walking to a nearby position
            Vec3d currentPos = this.getPos();
            double angle = Math.random() * 2 * Math.PI;
            double distance = 2.0 + Math.random() * 3.0; // 2-5 blocks

            walkingTarget = currentPos.add(
                Math.cos(angle) * distance,
                0,
                Math.sin(angle) * distance
            );

            isWalking = true;
            walkingTimer = 0;
        }

        if (isWalking && walkingTarget != null) {
            walkingTimer++;

            // move slowly toward target
            Vec3d currentPos = this.getPos();
            Vec3d direction = walkingTarget.subtract(currentPos).normalize();

            if (currentPos.distanceTo(walkingTarget) > 0.5) {
                Vec3d newPos = currentPos.add(direction.multiply(0.02)); // slow walking speed
                this.setPosition(newPos.x, newPos.y, newPos.z);
            } else {
                // reached target, stop walking
                isWalking = false;
                walkingTarget = null;
            }
        }
    }

    private void createSubtleMenacingEffects() {
        if (this.getWorld() instanceof ServerWorld serverWorld && existenceTimer % 60 == 0) {
            // very subtle dark particles - like a real player might have
            if (hasBeenSeen && Math.random() < 0.3) {
                serverWorld.spawnParticles(
                    net.minecraft.particle.ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    1,
                    0.05, 0.05, 0.05,
                    0.005
                );
            }
        }
    }

    private void facePlayer() {
        if (targetPlayer != null && targetPlayer.isAlive()) {
            double diffX = targetPlayer.getX() - this.getX();
            double diffZ = targetPlayer.getZ() - this.getZ();
            double diffY = targetPlayer.getEyeY() - this.getEyeY();
            
            double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
            
            float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F);
            float pitch = (float)-(Math.toDegrees(Math.atan2(diffY, dist)));
            
            this.setYaw(yaw);
            this.setHeadYaw(yaw);
            this.setBodyYaw(yaw);
            this.setPitch(pitch);
            this.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, targetPlayer.getEyePos());
        }
    }

    private void handlePlayerInteraction() {
        if (targetPlayer == null) return;

        double distance = targetPlayer.squaredDistanceTo(this);
        PersistentDataManager.PersistentPlayerData data = PersistentDataManager.getPlayerData(targetPlayer.getUuid());
        boolean isPlayerLookingAtEntity = isPlayerLookingAtEntity(targetPlayer);

        // first time being seen - check persistent data
        if (isPlayerLookingAtEntity && !hasBeenSeen && distance < 100) {
            hasBeenSeen = true;
            data.hasSeenNullPointerEntity = true;
            data.lastInteractionTime = System.currentTimeMillis();
            this.setCustomNameVisible(true);

            // escalate message based on previous encounters
            String message = data.timesEncounteredEntity > 1 ?
                "you see me again..." :
                "you see me now...";

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendNullPointerMessage(targetPlayer, message);
                }
            }, 2000);

            PersistentDataManager.updatePlayerData(targetPlayer.getUuid(), data);
        }

        // staring behavior - use persistent warning level
        if (isPlayerLookingAtEntity && hasBeenSeen) {
            lookingTimer++;

            if (lookingTimer > 60 && data.warningLevel == 0) {
                sendNullPointerKey(targetPlayer, "message.nullpointerentity.fakeplayer.stare1");
                data.warningLevel = 1;
                PersistentDataManager.updatePlayerData(targetPlayer.getUuid(), data);
            }

            if (lookingTimer > 140 && data.warningLevel == 1) {
                sendNullPointerKey(targetPlayer, "message.nullpointerentity.fakeplayer.stare2");
                data.warningLevel = 2;
                PersistentDataManager.updatePlayerData(targetPlayer.getUuid(), data);
            }

            if (lookingTimer > 200 && data.warningLevel == 2) {
                // escalate based on if they've been crashed before
                String message = data.hasBeenCrashed ?
                    "you didn't learn your lesson. this time will be worse." :
                    "fine. you want to stare? let me give you something to look at.";

                sendNullPointerMessage(targetPlayer, message);
                revealPlayerInfo(targetPlayer);
                data.warningLevel = 3;
                PersistentDataManager.updatePlayerData(targetPlayer.getUuid(), data);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        crashPlayer(targetPlayer);
                    }
                }, 3000);
            }
        } else {
            lookingTimer = Math.max(0, lookingTimer - 1);
        }

        // proximity behavior
        if (distance < 36.0 && hasBeenSeen) {
            handleProximityEffects(targetPlayer, data, distance);
        }
    }

    private boolean isPlayerLookingAtEntity(ServerPlayerEntity player) {
        Vec3d playerEyePos = player.getEyePos();
        Vec3d entityPos = this.getPos().add(0, 1.5, 0);
        Vec3d playerLook = player.getRotationVector();

        Vec3d toEntity = entityPos.subtract(playerEyePos).normalize();
        double dotProduct = playerLook.dotProduct(toEntity);

        return dotProduct > 0.7 && player.squaredDistanceTo(this) < 144;
    }

    private void handleProximityEffects(ServerPlayerEntity player, PersistentDataManager.PersistentPlayerData data, double distance) {
        proximityWarningTimer++;

        // proximity warnings fire on a long interval - every 10 seconds
        if (proximityWarningTimer > 200 && data.warningLevel < 3) {
            int warningIndex = Math.min(data.warningLevel, 4);
            sendNullPointerKey(player, "message.nullpointerentity.fakeplayer.proximity." + (warningIndex + 1));
            data.warningLevel++;
            proximityWarningTimer = 0;
            PersistentDataManager.updatePlayerData(player.getUuid(), data);
        }

        // only trigger final warning when very close (within 3 blocks)
        if (distance < 9.0 && data.hasSeenNullPointerEntity && proximityWarningTimer > 100) {
            sendNullPointerKey(player, data.hasBeenCrashed
                ? "message.nullpointerentity.fakeplayer.final.crashed"
                : "message.nullpointerentity.fakeplayer.final.first");
            data.shouldReceiveReturnMessage = true;
            proximityWarningTimer = 0; // reset timer to prevent spam
            PersistentDataManager.updatePlayerData(player.getUuid(), data);

            Vec3d pushDirection = player.getPos().subtract(this.getPos()).normalize();
            player.setVelocity(pushDirection.x * 1.5, 0.5, pushDirection.z * 1.5);
            player.velocityModified = true;

            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.BLINDNESS, 100, 0));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.SLOWNESS, 200, 3));

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    crashPlayer(player);
                }
            }, 2000);
        }
    }

    private void revealPlayerInfo(ServerPlayerEntity player) {
        // per-machine: the IP / location / username reveal runs on the player's OWN client, so each
        // player sees their own info (privacy-faked) instead of the host's IP/location/username.
        // reading LocationTracker / getDisplayUsername here would have leaked the HOST's data to
        // everyone. see ClientEventExecutor#runFakePlayerReveal.
        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 70);
    }

    private void crashPlayer(ServerPlayerEntity player) {
        PersistentDataManager.PersistentPlayerData data = PersistentDataManager.getPlayerData(player.getUuid());
        data.shouldReceiveReturnMessage = true;
        data.hasBeenCrashed = true;
        data.lastInteractionTime = System.currentTimeMillis();
        PersistentDataManager.updatePlayerData(player.getUuid(), data);

        // escalate messages based on crash history
        String[] msgKeys = data.timesEncounteredEntity > 3 ?
            new String[]{"message.nullpointerentity.fakeplayer.crash.repeat.1", "message.nullpointerentity.fakeplayer.crash.repeat.2", "message.nullpointerentity.fakeplayer.crash.repeat.3"} :
            new String[]{"message.nullpointerentity.fakeplayer.crash.first.1", "message.nullpointerentity.fakeplayer.crash.first.2", "message.nullpointerentity.fakeplayer.crash.first.3"};

        sendNullPointerKey(player, msgKeys[0]);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, msgKeys[1]);
            }
        }, 1000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, msgKeys[2], player.getName().getString());

                // play crash sounds - static + glitch for maximum horror
                if (FakePlayerEntity.this.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.playSound(null, player.getBlockPos(),
                        lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
                        net.minecraft.sound.SoundCategory.MASTER, 2.0f, 0.5f);
                    serverWorld.playSound(null, player.getBlockPos(),
                        lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
                        net.minecraft.sound.SoundCategory.MASTER, 2.0f, 0.8f);
                }

                // trigger the jumpscare flash before crashing
                triggerClientJumpscareFlash();

                // wait for flash and sounds to be heard, then crash the player's OWN client
                // (never System.exit here - on a dedicated server that would kill the shared server)
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendCrashGame(player);
                    }
                }, 800); // wait 800ms for sounds and flash to be experienced
            }
        }, 2500);
    }

    private void triggerClientJumpscareFlash() {
        // flash the target player's OWN screen via a packet (was a host-only static client call)
        if (targetPlayer != null && this.getWorld() instanceof ServerWorld) {
            lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendJumpscareFlash(targetPlayer);
        }
    }

    private void sendNullPointerMessage(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
            .append(Text.literal(message).formatted(Formatting.DARK_RED)), false);
    }

    /** nullpointerentity line by translation key, so each client renders its own locale. */
    private void sendNullPointerKey(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
            .append(Text.translatable(key, args).formatted(Formatting.DARK_RED)), false);
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }


    private void handleInvisibleBarrier() {
        if (targetPlayer == null) return;

        double distance = targetPlayer.distanceTo(this);

        // if player is getting too close to the barrier
        if (distance <= BARRIER_DISTANCE) {
            // push player back
            Vec3d entityPos = this.getPos();
            Vec3d playerPos = targetPlayer.getPos();

            // calculate push direction (away from entity)
            Vec3d pushDirection = playerPos.subtract(entityPos).normalize();

            // calculate desired position (just outside barrier)
            Vec3d desiredPos = entityPos.add(pushDirection.multiply(BARRIER_DISTANCE + 0.5));

            // smoothly push player back
            Vec3d currentVelocity = targetPlayer.getVelocity();
            Vec3d pushForce = desiredPos.subtract(playerPos).multiply(0.3); // gentle but firm push

            // apply the push
            targetPlayer.setVelocity(currentVelocity.add(pushForce));
            targetPlayer.velocityModified = true;

            // send warning messages with escalation
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBarrierWarning > BARRIER_WARNING_COOLDOWN) {
                sendBarrierWarning();
                lastBarrierWarning = currentTime;
            }

            // play barrier sound effect
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.playSound(
                    targetPlayer,
                    targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                    SoundEvents.BLOCK_GLASS_HIT,
                    SoundCategory.BLOCKS,
                    0.5f, // volume
                    1.5f  // higher pitch for barrier effect
                );
            }
        } else if (distance > BARRIER_DISTANCE + 1.0) {
            // reset warning level if player backs away far enough
            if (barrierWarningLevel > 0) {
                barrierWarningLevel = Math.max(0, barrierWarningLevel - 1);
            }
        }
    }

    private void sendBarrierWarning() {
        if (targetPlayer == null) return;

        String message;
        switch (barrierWarningLevel) {
            case 0:
                message = "don't get too close.";
                break;
            case 1:
                message = "STOP GETTING CLOSE.";
                break;
            case 2:
                message = "I WARNED YOU TO STAY BACK.";
                break;
            case 3:
                message = "YOU WON'T LISTEN, WILL YOU?";
                // trigger crash after this warning
                scheduleGameCrash();
                break;
            default:
                message = "...";
                break;
        }

        sendNullPointerMessage(targetPlayer, message);
        barrierWarningLevel++;
    }

    private void scheduleGameCrash() {
        // schedule a crash after final warning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (targetPlayer != null && !targetPlayer.isDisconnected()) {
                    // mark that player should receive return message
                    PersistentDataManager.PersistentPlayerData data = PersistentDataManager.getPlayerData(targetPlayer.getUuid());
                    data.shouldReceiveReturnMessage = true;
                    data.hasBeenCrashed = true;
                    PersistentDataManager.updatePlayerData(targetPlayer.getUuid(), data);

                    // crash the game
                    crashPlayer(targetPlayer);
                }
            }
        }, 2000); // 2 second delay before crash
    }
}
