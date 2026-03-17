package lol.cqllmetoxic.nullpointerentity.events;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.client.BSoDOverlay;
import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * handles subtle background events that happen during gameplay.
 * includes things like screen tints, audio glitches, controls messing up, etc.
 * events get more intense as you progress through the mod phases.
 */
public class PassiveEvents {
    private static final Map<UUID, Long> lastEventTimes = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Long>> eventCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, LinkedList<String>> eventHistory = new ConcurrentHashMap<>(); // track last 5 events
    private static final Random random = new Random();

    private static final long MIN_EVENT_INTERVAL = 180000; // 3 minutes minimum between any passive events
    private static final long EARLY_PHASE_COOLDOWN = 180000; // 3 minutes for nice phase (events 1-10)
    private static final long MIDDLE_PHASE_COOLDOWN = 180000; // 3 minutes for transition phase (events 11-20)
    private static final long LATE_PHASE_COOLDOWN = 180000; // 3 minutes for hostile phase (events 21-30)
    private static final long FINAL_PHASE_COOLDOWN = 180000; // 3 minutes for jumpscare phase (events 31-40)
    private static final int EVENT_HISTORY_SIZE = 5; // remember last 5 events

    private static final Map<UUID, PassiveEffectState> clientEffects = new ConcurrentHashMap<>();

    /**
     * stores active client-side effects for a player.
     * tracks things like screen tint, camera shake, input delays, etc.
     */
    private static class PassiveEffectState {
        public boolean blockBreakDelay = false;
        public boolean movementLag = false;
        public boolean cameraShake = false;
        public float mouseSensitivityMultiplier = 1.0f;
        public long effectStartTime = 0;
        public int screenTintRed = 0;
        public int screenTintGreen = 0;
        public int screenTintBlue = 0;
        public boolean gravityFluctuation = false;
        public boolean hotbarShuffle = false;
        public boolean inputInversion = false;
        public boolean keyDelay = false;
    }

    /**
     * hooks into the server tick to check for passive event triggers.
     * runs once per second for each player.
     */
    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) {
                server.getPlayerManager().getPlayerList().forEach(PassiveEvents::processPlayerPassiveEvents);
            }
        });
    }

    /**
     * checks if a player should get a passive event this tick.
     * enforces cooldowns and phase-based trigger chances.
     */
    private static void processPlayerPassiveEvents(ServerPlayerEntity player) {
        PersistentDataManager.PersistentPlayerData playerData =
            PersistentDataManager.getPlayerData(player.getUuid().toString());

        if (playerData == null) return;

        long currentTime = System.currentTimeMillis();
        UUID playerId = player.getUuid();

        Long lastEvent = lastEventTimes.get(playerId);
        if (lastEvent != null && currentTime - lastEvent < MIN_EVENT_INTERVAL) {
            return;
        }

        // currentEventPhase is already the phase number (1-4), not an event ID
        int phase = PersistentDataManager.getWorldData().currentEventPhase;

        // if phase is 0 (new world), default to phase 1
        if (phase == 0) phase = 1;

        double triggerChance = getPhaseEventChance(phase);
         if (random.nextDouble() < triggerChance) {
            triggerRandomPassiveEvent(player, phase, currentTime);
        }
    }

    /**
     * converts event progress number into a phase (1-4).
     * phases get progressively more intense.
     */
    private static int determineCurrentPhase(int eventProgress) {
        if (eventProgress <= 10) return 1;  // nice phase: events 1-10
        if (eventProgress <= 20) return 2;  // transition phase: events 11-20
        if (eventProgress <= 30) return 3;  // hostile phase: events 21-30
        return 4;                            // jumpscare phase: events 31-40
    }

    /**
     * returns the probability of a passive event triggering per second.
     * tuned to trigger approximately every 1-3 minutes after cooldown expires.
     */
    private static double getPhaseEventChance(int phase) {
        return switch (phase) {
            case 1 -> 0.012; // nice phase (events 1-10)
            case 2 -> 0.015; // transition phase (events 11-20)
            case 3 -> 0.018; // hostile phase (events 21-30)
            case 4 -> 0.025; // jumpscare phase (events 31-40)
            default -> 0.012;
        };
    }

    private static void triggerRandomPassiveEvent(ServerPlayerEntity player, int phase, long currentTime) {
        UUID playerId = player.getUuid();

        switch (phase) {
            case 1 -> triggerEarlyPhaseEvent(player, currentTime);
            case 2 -> triggerMiddlePhaseEvent(player, currentTime);
            case 3 -> triggerLatePhaseEvent(player, currentTime);
            case 4 -> triggerFinalPhaseEvent(player, currentTime);
        }

        lastEventTimes.put(playerId, currentTime);
    }

    // ===== early phase events =====
    // aurora is still "nice" here. nothing audibly wrong. just small things that feel slightly off.
    private static void triggerEarlyPhaseEvent(ServerPlayerEntity player, long currentTime) {
        if (!canTriggerEvent(player, "early_phase", currentTime, EARLY_PHASE_COOLDOWN)) return;

        String[] events = {
            "particle_trail",    // silent soul particles drift behind the player
            "hunger_drain",      // 1 food bar disappears with no explanation
            "hotbar_shift",      // hotbar selection silently moves one slot to the left
            "look_nudge",        // camera is nudged very slightly in a random direction
            "sky_darken",        // brief darkness effect, no blindness sound
            "item_vanish",       // one item in hotbar temporarily goes invisible for 3 seconds
            "cursor_drift",      // slowly drifts the player's look angle a few degrees over 2s
            "name_flicker"       // player's own name flashes in chat as if someone else said it
        };

        String selectedEvent = selectEventWithHistory(player.getUuid(), events);
        if (selectedEvent == null) {
            NullPointerEntity.LOGGER.warn("All early phase events were in recent history for player {}, selecting random", player.getName().getString());
            selectedEvent = events[random.nextInt(events.length)];
        }

        addEventToHistory(player.getUuid(), selectedEvent);

        switch (selectedEvent) {
            case "particle_trail"  -> triggerParticleTrail(player);
            case "hunger_drain"    -> triggerHungerDrain(player);
            case "hotbar_shift"    -> triggerHotbarShift(player);
            case "look_nudge"      -> triggerLookNudge(player);
            case "sky_darken"      -> triggerSkyDarken(player);
            case "item_vanish"     -> triggerItemVanish(player);
            case "cursor_drift"    -> triggerCursorDrift(player);
            case "name_flicker"    -> triggerNameFlicker(player);
        }

        setEventCooldown(player, "early_phase", currentTime);
    }

    // phase 1 silent event: soul particles drift behind the player for a few seconds
    private static void triggerParticleTrail(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        for (int i = 0; i < 12; i++) {
            final int step = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Vec3d look = player.getRotationVector();
                    double bx = player.getX() - look.x * 2 + (random.nextDouble() - 0.5) * 0.6;
                    double by = player.getY() + 0.1;
                    double bz = player.getZ() - look.z * 2 + (random.nextDouble() - 0.5) * 0.6;
                    world.spawnParticles(ParticleTypes.SOUL, bx, by, bz, 2, 0.1, 0.1, 0.1, 0.01);
                }
            }, step * 250L);
        }
        NullPointerEntity.LOGGER.info("Particle trail triggered for player {}", player.getName().getString());
    }

    // phase 1 silent event: silently removes 5 food bar point with no sound or particle
    private static void triggerHungerDrain(ServerPlayerEntity player) {
        int current = player.getHungerManager().getFoodLevel();
        if (current > 5) {
            player.getHungerManager().setFoodLevel(current - 5);
        }
        NullPointerEntity.LOGGER.info("Silent hunger drain triggered for player {}", player.getName().getString());
    }

    // phase 1 silent event: silently moves the selected hotbar slot one step to the left
    private static void triggerHotbarShift(ServerPlayerEntity player) {
        int current = player.getInventory().selectedSlot;
        int next = (current + 8) % 9; // shift left (wraps 0 -> 8)
        player.getInventory().selectedSlot = next;
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(next));
        NullPointerEntity.LOGGER.info("Hotbar shift triggered for player {}", player.getName().getString());
    }

    // phase 1 silent event: nudges the player's look angle a few degrees in a random direction
    private static void triggerLookNudge(ServerPlayerEntity player) {
        float nudgeYaw   = (random.nextFloat() - 0.5f) * 12f; // ±6 degrees
        float nudgePitch = (random.nextFloat() - 0.5f) * 6f;  // ±3 degrees
        float newYaw   = player.getYaw()   + nudgeYaw;
        float newPitch = Math.max(-90f, Math.min(90f, player.getPitch() + nudgePitch));
        player.setYaw(newYaw);
        player.setPitch(newPitch);
        player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), newYaw, newPitch);
        NullPointerEntity.LOGGER.info("Look nudge triggered for player {}", player.getName().getString());
    }

    // phase 1 silent event: makes one hotbar item invisible for 3 seconds by swapping it out and back
    private static void triggerItemVanish(ServerPlayerEntity player) {
        var inv = player.getInventory();
        // pick a random occupied hotbar slot (0–8)
        List<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (!inv.getStack(i).isEmpty()) occupied.add(i);
        }
        if (occupied.isEmpty()) return;
        int slot = occupied.get(random.nextInt(occupied.size()));
        net.minecraft.item.ItemStack original = inv.getStack(slot).copy();
        inv.setStack(slot, net.minecraft.item.ItemStack.EMPTY);
        player.playerScreenHandler.syncState();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                inv.setStack(slot, original);
                player.playerScreenHandler.syncState();
            }
        }, 3000);
        NullPointerEntity.LOGGER.info("Item vanish triggered for player {} (slot {})", player.getName().getString(), slot);
    }

    // phase 1 silent event: slowly drifts the player's view a few degrees over 2 seconds
    private static void triggerCursorDrift(ServerPlayerEntity player) {
        float targetYaw   = player.getYaw()   + (random.nextFloat() - 0.5f) * 20f; // drift up to ±10°
        float targetPitch = Math.max(-90f, Math.min(90f, player.getPitch() + (random.nextFloat() - 0.5f) * 10f));
        int steps = 10;
        float startYaw   = player.getYaw();
        float startPitch = player.getPitch();
        for (int i = 1; i <= steps; i++) {
            final float t = i / (float) steps;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    float y = startYaw   + (targetYaw   - startYaw)   * t;
                    float p = startPitch + (targetPitch - startPitch) * t;
                    player.setYaw(y);
                    player.setPitch(p);
                    player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), y, p);
                }
            }, (long)(i * 200));
        }
        NullPointerEntity.LOGGER.info("Cursor drift triggered for player {}", player.getName().getString());
    }

    // phase 1 silent event: briefly applies darkness (hidden, no particles, no sound)
    private static void triggerSkyDarken(ServerPlayerEntity player) {
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.DARKNESS, 30, 0, false, false, false));
        NullPointerEntity.LOGGER.info("Sky darken triggered for player {}", player.getName().getString());
    }

    // phase 1 silent event: prints the player's own name in chat as if they sent a message — then nothing
    private static void triggerNameFlicker(ServerPlayerEntity player) {
        String name = player.getName().getString();
        player.sendMessage(Text.literal("§f<" + name + "> §f"), false);
        NullPointerEntity.LOGGER.info("Name flicker triggered for player {}", player.getName().getString());
    }


    // ===== middle phase events =====
    private static void triggerMiddlePhaseEvent(ServerPlayerEntity player, long currentTime) {
        if (!canTriggerEvent(player, "middle_phase", currentTime, MIDDLE_PHASE_COOLDOWN)) return;

        // check if splitself was already triggered using persistent storage
        PersistentDataManager.PersistentPlayerData playerData =
            PersistentDataManager.getPlayerData(player.getUuid().toString());

        boolean splitSelfAlreadyTriggered = playerData.triggeredEvents.getOrDefault("splitself", false);

        // prioritize splitself event if it hasn't been triggered yet (100% chance in transition phase)
        if (!splitSelfAlreadyTriggered) {
            NullPointerEntity.LOGGER.info("Triggering Split Self event (one-time transition phase event) for player {}", player.getName().getString());
            addEventToHistory(player.getUuid(), "splitself");

            // mark as triggered in persistent storage
            playerData.triggeredEvents.put("splitself", true);
            PersistentDataManager.updatePlayerData(player.getUuid(), playerData);
            PersistentDataManager.saveData();

            triggerSplitSelf(player);
            setEventCooldown(player, "middle_phase", currentTime);
            return;
        }

        String[] events = {"void_whispers", "weather_control", "inventory_sort", "reality_shatter", "void_breach", "entity_mimic", "dimension_bleed", "false_death", "shadow_clone"};

        // get event that wasn't in last 5
        String selectedEvent = selectEventWithHistory(player.getUuid(), events);
        if (selectedEvent == null) {
            NullPointerEntity.LOGGER.warn("All middle phase events were in recent history for player {}, selecting random", player.getName().getString());
            selectedEvent = events[random.nextInt(events.length)];
        }

        // add to history
        addEventToHistory(player.getUuid(), selectedEvent);

        switch (selectedEvent) {
            case "void_whispers" -> triggerVoidWhispers(player);
            case "weather_control" -> triggerWeatherControl(player);
            case "inventory_sort" -> triggerInventorySort(player);
            case "mirror_world" -> triggerMirrorWorld(player);
            case "void_breach" -> triggerVoidBreach(player);
            case "entity_mimic" -> triggerEntityMimic(player);
            case "dimension_bleed" -> triggerDimensionBleed(player);
            case "false_death" -> triggerFalseDeath(player);
            case "shadow_clone" -> triggerShadowClone(player);
        }

        setEventCooldown(player, "middle_phase", currentTime);
    }

    private static void triggerVoidWhispers(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // spawn void particles rising from below
        for (int i = 0; i < 20; i++) {
            final int step = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    double offsetX = (random.nextDouble() - 0.5) * 4;
                    double offsetZ = (random.nextDouble() - 0.5) * 4;
                    world.spawnParticles(ParticleTypes.PORTAL,
                        playerPos.getX() + offsetX, playerPos.getY() - 1, playerPos.getZ() + offsetZ,
                        5, 0.1, 0.1, 0.1, 0.5);
                    world.spawnParticles(ParticleTypes.SCULK_SOUL,
                        playerPos.getX() + offsetX, playerPos.getY() - 0.5, playerPos.getZ() + offsetZ,
                        2, 0.05, 0.05, 0.05, 0.02);
                }
            }, step * 150L);
        }

        // play layered eerie sounds including custom whispers
        world.playSound(null, playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5,
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_WHISPER,
            SoundCategory.HOSTILE, 0.7f, 0.6f);

        for (int i = 0; i < 3; i++) {
            final int soundIndex = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    world.playSound(null, playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5,
                        SoundEvents.ENTITY_WARDEN_AMBIENT,
                        SoundCategory.AMBIENT, 0.3f, 0.5f + random.nextFloat() * 0.3f);

                    // add whisper on second iteration
                    if (soundIndex == 1) {
                        world.playSound(null, playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5,
                            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_WHISPER,
                            SoundCategory.HOSTILE, 0.5f, 0.8f);
                    }
                }
            }, i * 1200L);
        }

        player.sendMessage(Text.literal("§7The void calls your name...").formatted(net.minecraft.util.Formatting.DARK_PURPLE), false);
        NullPointerEntity.LOGGER.info("Void whispers triggered for player {}", player.getName().getString());
    }

    private static void triggerWeatherControl(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        if (!world.isRaining()) {
            world.setWeather(0, 6000 + random.nextInt(6000), true, true); // start rain/thunder
            NullPointerEntity.LOGGER.info("Triggered forced weather for player {}", player.getName().getString());
        } else {
            // intensify existing weather
            world.setWeather(0, 3000 + random.nextInt(3000), true, true);
            NullPointerEntity.LOGGER.info("Intensified weather for player {}", player.getName().getString());
        }
    }

    private static void triggerDimensionBleed(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // spawn particles from "another dimension" bleeding through
        for (int i = 0; i < 40; i++) {
            final int step = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    double offsetX = (random.nextDouble() - 0.5) * 12;
                    double offsetY = random.nextDouble() * 6;
                    double offsetZ = (random.nextDouble() - 0.5) * 12;

                    // mix of nether and end particles
                    world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        playerPos.getX() + offsetX, playerPos.getY() + offsetY, playerPos.getZ() + offsetZ,
                        3, 0.1, 0.1, 0.1, 0.5);
                    world.spawnParticles(ParticleTypes.WARPED_SPORE,
                        playerPos.getX() + offsetX, playerPos.getY() + offsetY, playerPos.getZ() + offsetZ,
                        2, 0.2, 0.2, 0.2, 0.02);
                }
            }, step * 100L);
        }

        // play ominous dimensional sounds
        world.playSound(null, playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5,
            SoundEvents.BLOCK_PORTAL_TRAVEL,
            SoundCategory.AMBIENT, 0.4f, 0.5f);
        world.playSound(null, playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5,
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
            SoundCategory.HOSTILE, 0.6f, 0.7f);

        player.sendMessage(Text.literal("§5§k||§r §dReality is thinning...§r §5§k||"), false);
        NullPointerEntity.LOGGER.info("Dimension bleed triggered for player {}", player.getName().getString());
    }

    private static void triggerFalseDeath(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        // play death sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_PLAYER_DEATH,
            SoundCategory.PLAYERS, 1.0f, 1.0f);

        // trigger fake death screen on client (5 seconds duration)
        NullPointerEntity.triggerFakeDeathScreen(5000L);

        // show fake death message in chat
        player.sendMessage(Text.literal("§f" + player.getName().getString() + " died"), false);

        // play respawn sound after screen closes
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 0.6f, 1.2f);
                player.sendMessage(Text.literal("§8§o...or did you?"), true);
            }
        }, 5500); // after screen closes

        NullPointerEntity.LOGGER.info("False death with fake death screen triggered for player {}", player.getName().getString());
    }

    private static void triggerShadowClone(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // create illusion of another player nearby
        Vec3d lookVec = player.getRotationVector();
        double cloneX = playerPos.getX() - lookVec.x * 8;
        double cloneZ = playerPos.getZ() - lookVec.z * 8;
        BlockPos clonePos = new BlockPos((int)cloneX, playerPos.getY(), (int)cloneZ);

        // spawn dark particles in player shape
        for (int i = 0; i < 30; i++) {
            final int step = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    for (int y = 0; y < 2; y++) {
                        world.spawnParticles(ParticleTypes.SMOKE,
                            clonePos.getX() + 0.5, clonePos.getY() + y, clonePos.getZ() + 0.5,
                            3, 0.3, 0.3, 0.3, 0.01);
                        world.spawnParticles(ParticleTypes.SOUL,
                            clonePos.getX() + 0.5, clonePos.getY() + y, clonePos.getZ() + 0.5,
                            1, 0.2, 0.2, 0.2, 0.01);
                    }

                    // play footstep sounds from clone position
                    if (step % 5 == 0) {
                        world.playSound(null, clonePos, SoundEvents.BLOCK_STONE_STEP,
                            SoundCategory.PLAYERS, 0.5f, 0.9f);
                    }
                }
            }, step * 150L);
        }

        player.sendMessage(Text.literal("§8§oWho the f*ck is behind you?"), true);
        NullPointerEntity.LOGGER.info("Shadow clone triggered for player {}", player.getName().getString());
    }

    private static void triggerMirrorWorld(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d currentPos = player.getPos();

        // create dramatic shattering visual effect
        for (int i = 0; i < 30; i++) {
            world.spawnParticles(ParticleTypes.END_ROD,
                currentPos.x, currentPos.y + 1, currentPos.z,
                1, 0.5, 1, 0.5, 0.1);
            world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                currentPos.x, currentPos.y + 1, currentPos.z,
                2, 0.8, 1, 0.8, 0.2);
        }

        // apply hidden blindness effect
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 40, 0, false, false, false));

        // play glass shattering sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.8f, 0.5f);

        // play additional ominous sound
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE, 0.5f, 0.7f);
            }
        }, 500);

        player.sendMessage(Text.literal("§5§k||§r §d[REALITY SHATTER] Everything fractures...§r §5§k||").formatted(net.minecraft.util.Formatting.LIGHT_PURPLE), false);
        NullPointerEntity.LOGGER.info("Reality shatter triggered for player {}", player.getName().getString());
    }

    private static void triggerVoidBreach(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // the void is reaching up from below - create tendrils of darkness
        for (int i = 0; i < 60; i++) {
            final int step = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // create rising void particles from below
                    double offsetX = (random.nextDouble() - 0.5) * 16;
                    double offsetZ = (random.nextDouble() - 0.5) * 16;
                    double startY = playerPos.getY() - 3;
                    double endY = playerPos.getY() + 5;

                    // spawn particles rising upward
                    for (double y = startY; y < endY; y += 0.5) {
                        world.spawnParticles(ParticleTypes.SCULK_SOUL,
                            playerPos.getX() + offsetX, y, playerPos.getZ() + offsetZ,
                            2, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            playerPos.getX() + offsetX, y, playerPos.getZ() + offsetZ,
                            1, 0.05, 0.05, 0.05, 0.01);
                    }
                }
            }, step * 80L);
        }

        // apply levitation briefly to simulate being "pulled"
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.LEVITATION, 40, 0, false, false, false));

        // then apply slow falling to prevent fall damage
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.SLOW_FALLING, 100, 0, false, false, false));
            }
        }, 2000);

        // play ominous sounds
        world.playSound(null, playerPos, SoundEvents.ENTITY_WARDEN_EMERGE,
            SoundCategory.HOSTILE, 0.8f, 0.5f);
        world.playSound(null, playerPos, lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_WHISPER,
            SoundCategory.HOSTILE, 0.6f, 0.4f);

        // schedule additional sounds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                world.playSound(null, playerPos, SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                    SoundCategory.HOSTILE, 0.7f, 0.6f);
            }
        }, 1500);

        player.sendMessage(Text.literal("§0§k||§r §8The void reaches for you...§r §0§k||"), false);
        NullPointerEntity.LOGGER.info("Void breach triggered for player {}", player.getName().getString());
    }

    private static void triggerEntityMimic(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        for (int i = 0; i < 3; i++) {
            final int step = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    double offsetX = (random.nextDouble() - 0.5) * 8;
                    double offsetZ = (random.nextDouble() - 0.5) * 8;
                    BlockPos soundPos = player.getBlockPos().add((int)offsetX, 0, (int)offsetZ);

                    world.playSound(null, soundPos, SoundEvents.ENTITY_PLAYER_HURT,
                        SoundCategory.PLAYERS, 0.5f, 1.0f + random.nextFloat() * 0.2f);

                    world.spawnParticles(ParticleTypes.SMOKE,
                        soundPos.getX() + 0.5, soundPos.getY() + 1, soundPos.getZ() + 0.5,
                        5, 0.3, 0.5, 0.3, 0.01);
                }
            }, step * 1500L);
        }

        player.sendMessage(Text.literal("§eWhat was that...?").formatted(net.minecraft.util.Formatting.YELLOW), false);
        NullPointerEntity.LOGGER.info("Entity mimic triggered for player {}", player.getName().getString());
    }

    private static void triggerInventorySort(ServerPlayerEntity player) {
        // randomize entire inventory - complete chaos
        var inventory = player.getInventory();
        ServerWorld world = (ServerWorld) player.getWorld();
        List<Integer> occupiedSlots = new ArrayList<>();

        // get all occupied slots (including armor, offhand, main inventory)
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.getStack(i).isEmpty()) {
                occupiedSlots.add(i);
            }
        }

        if (occupiedSlots.size() >= 2) {
            // create a temporary list of all items
            List<ItemStack> items = new ArrayList<>();
            for (int slot : occupiedSlots) {
                items.add(inventory.getStack(slot).copy());
            }

            // shuffle the items list (not the slots)
            Collections.shuffle(items);

            // clear all occupied slots first
            for (int slot : occupiedSlots) {
                inventory.setStack(slot, ItemStack.EMPTY);
            }

            // redistribute shuffled items to the same slots (but items are now randomized)
            for (int i = 0; i < occupiedSlots.size(); i++) {
                inventory.setStack(occupiedSlots.get(i), items.get(i));
            }

            // visual and audio feedback
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0f, 0.5f);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
                SoundCategory.HOSTILE, 0.5f, 1.2f);

            player.sendMessage(Text.literal("§4§k||§r §cYour inventory scrambles§r §4§k||"), false);
            NullPointerEntity.LOGGER.info("Completely randomized inventory for player {} ({} items shuffled)",
                player.getName().getString(), occupiedSlots.size());
        }
    }

    private static void triggerSplitSelf(ServerPlayerEntity player) {
        // check persistent storage to see if this event has already been triggered
        PersistentDataManager.PersistentPlayerData playerData =
            PersistentDataManager.getPlayerData(player.getUuid().toString());

        if (playerData.triggeredEvents.getOrDefault("splitself", false)) {
            NullPointerEntity.LOGGER.debug("Split Self event already triggered for player {} (from persistent data), skipping", player.getName().getString());
            return;
        }

        // mark as triggered in persistent storage
        playerData.triggeredEvents.put("splitself", true);
        PersistentDataManager.updatePlayerData(player.getUuid(), playerData);
        PersistentDataManager.saveData();

        String playerName = player.getName().getString();
        ServerWorld world = (ServerWorld) player.getWorld();

        // play subtle join sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.3f, 1.5f);

        // phase 1: fake join message (yellow text like real join messages)
        player.sendMessage(Text.literal("§e" + playerName + " joined the game"), false);

        // phase 2: first message - realization
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.sendMessage(Text.literal("§f<" + playerName + "> §fOh... wait... this isn't Split Self..."), false);
            }
        }, 1500);

        // phase 3: second message - apology
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.sendMessage(Text.literal("§f<" + playerName + "> §fSorry to bother..."), false);
            }
        }, 3000);

        // phase 4: third message - leaving
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.sendMessage(Text.literal("§f<" + playerName + "> §fWelp, later!"), false);
            }
        }, 4500);

        // phase 5: fake leave message
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.sendMessage(Text.literal("§e" + playerName + " left the game"), false);

                // play subtle leave sound
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2f, 0.8f);
            }
        }, 6000);

        NullPointerEntity.LOGGER.info("Triggered one-time Split Self event for player {}", playerName);
    }

    // ===== late phase events =====
    private static void triggerLatePhaseEvent(ServerPlayerEntity player, long currentTime) {
        if (!canTriggerEvent(player, "late_phase", currentTime, LATE_PHASE_COOLDOWN)) return;

        String[] events = {"movement_lag", "durability_drain", "chat_injection", "camera_shake", "fake_damage", "control_reversal", "entity_possession"};

        // get event that wasn't in last 5
        String selectedEvent = selectEventWithHistory(player.getUuid(), events);
        if (selectedEvent == null) {
            NullPointerEntity.LOGGER.warn("All late phase events were in recent history for player {}, selecting random", player.getName().getString());
            selectedEvent = events[random.nextInt(events.length)];
        }

        // add to history
        addEventToHistory(player.getUuid(), selectedEvent);

        switch (selectedEvent) {
            case "movement_lag" -> triggerMovementLag(player);
            case "durability_drain" -> triggerDurabilityDrain(player);
            case "chat_injection" -> triggerChatInjection(player);
            case "camera_shake" -> triggerCameraShake(player);
            case "fake_damage" -> triggerFakeDamage(player);
            case "control_reversal" -> triggerControlReversal(player);
            case "entity_possession" -> triggerVisionDistortion(player);
        }

        setEventCooldown(player, "late_phase", currentTime);
    }

    private static void triggerMovementLag(ServerPlayerEntity player) {
        PassiveEffectState state = getOrCreateEffectState(player.getUuid());
        state.movementLag = true;
        state.effectStartTime = System.currentTimeMillis();

        // apply lag for 2-5 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                state.movementLag = false;
            }
        }, 2000 + random.nextInt(3000));

        NullPointerEntity.LOGGER.info("Triggered movement lag for player {}", player.getName().getString());
    }

    private static void triggerDurabilityDrain(ServerPlayerEntity player) {
        var inventory = player.getInventory();
        ServerWorld world = (ServerWorld) player.getWorld();
        boolean itemsAffected = false;

        // find tools and weapons to drain durability severely
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.isDamageable() && stack.getDamage() < stack.getMaxDamage()) {
                // drain 100 durability points - severe corruption
                int drainAmount = 100;
                stack.setDamage(Math.min(stack.getDamage() + drainAmount, stack.getMaxDamage() - 1));
                itemsAffected = true;
            }
        }

        if (itemsAffected) {
            // create corrupting particles around the player
            for (int i = 0; i < 50; i++) {
                world.spawnParticles(ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    1, 0.5, 0.5, 0.5, 0.05);
                world.spawnParticles(ParticleTypes.ASH,
                    player.getX(), player.getY() + 1, player.getZ(),
                    2, 0.5, 0.5, 0.5, 0.02);
                world.spawnParticles(ParticleTypes.SCULK_SOUL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    1, 0.3, 0.3, 0.3, 0.01);
            }

            // play metal breaking sounds and custom glitch sound
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 0.8f, 0.5f);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 0.3f);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
                SoundCategory.HOSTILE, 0.7f, 0.8f);

            player.sendMessage(Text.literal("§4§k|||§r §4YOUR ITEMS DISINTEGRATE§r §4§k|||"), false);
        }

        NullPointerEntity.LOGGER.info("Triggered SEVERE item decay (100 durability) for player {}", player.getName().getString());
    }

    private static void triggerChatInjection(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        ServerWorld world = (ServerWorld) player.getWorld();

        String[] fakeMessages = {
            "§8§o[" + playerName + " whispers to " + playerName + "] I can see you",
            "§4§o<System> Player behavior anomaly detected: " + playerName,
            "§5§o[???] " + playerName + "... I've been watching you play...",
            "§8§o[Spectator] Why do you keep looking behind you, " + playerName + "?",
            "§7§o[Server] Unusual player movement patterns detected for " + playerName,
            "§4§o[WARNING] Player " + playerName + " location data: ACCESSIBLE",
            "§8§o[Unknown] I know what you did in that cave, " + playerName + ".",
            "§d§o<???> You're not alone in this world, " + playerName + ". I SEE YOU.",
            "§7§o[System] Player " + playerName + " session is being recorded.",
            "§4§o[Error] Could not find player " + playerName + "... but they're still here?"
        };

        String message = fakeMessages[random.nextInt(fakeMessages.length)];
        player.sendMessage(Text.literal(message), false);

        // play custom whisper sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_WHISPER,
            SoundCategory.HOSTILE, 0.5f, 0.9f);

        NullPointerEntity.LOGGER.info("Injected psychological chat message for player {}: {}",
            playerName, message);
    }

    private static void triggerCameraShake(ServerPlayerEntity player) {
        PassiveEffectState state = getOrCreateEffectState(player.getUuid());
        state.cameraShake = true;
        state.effectStartTime = System.currentTimeMillis();

        ServerWorld world = (ServerWorld) player.getWorld();

        // play tense heartbeat sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_TENSE,
            SoundCategory.HOSTILE, 0.8f, 1.0f);

        player.sendMessage(Text.literal("§c§oEverything shakes..."), true);

        // store original rotation
        final float[] originalYaw = {player.getYaw()};
        final float[] originalPitch = {player.getPitch()};

        // actually shake camera by manipulating yaw and pitch on server
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            private int shakes = 0;

            @Override
            public void run() {
                if (shakes >= 100) { // 5 seconds of shaking (100 * 50ms)
                    state.cameraShake = false;
                    // reset to original rotation
                    player.setYaw(originalYaw[0]);
                    player.setPitch(originalPitch[0]);
                    player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(),
                        originalYaw[0], originalPitch[0]);
                    player.sendMessage(Text.literal("§8§o...stability restored"), true);
                    cancel();
                    return;
                }

                // update original position if player is moving
                if (shakes == 0) {
                    originalYaw[0] = player.getYaw();
                    originalPitch[0] = player.getPitch();
                }

                // random camera rotation to simulate violent shaking
                // intensity increases over time for more disturbing effect
                float intensity = 1.0f + (shakes / 50.0f); // gets stronger over time
                float yawShake = (random.nextFloat() - 0.5f) * 10.0f * intensity;
                float pitchShake = (random.nextFloat() - 0.5f) * 8.0f * intensity;

                float newYaw = originalYaw[0] + yawShake;
                float newPitch = Math.max(-90, Math.min(90, originalPitch[0] + pitchShake));

                player.setYaw(newYaw);
                player.setPitch(newPitch);
                player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(),
                    newYaw, newPitch);

                shakes++;
            }
        }, 0, 50); // shake every 50ms

        NullPointerEntity.LOGGER.info("Triggered camera shake for player {}", player.getName().getString());
    }

    private static void triggerFakeDamage(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        // play hurt sound without actually damaging
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // create damage particles
        for (int i = 0; i < 20; i++) {
            world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR,
                player.getX(), player.getY() + 1, player.getZ(),
                1, 0.5, 0.5, 0.5, 0.1);
        }

        // apply brief nausea to disorient (hidden)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.NAUSEA, 100, 0, false, false, false)); // hidden

        player.sendMessage(Text.literal("§cYour head pounds..."), true);
        NullPointerEntity.LOGGER.info("Fake damage triggered for player {}", player.getName().getString());
    }

    private static void triggerControlReversal(ServerPlayerEntity player) {
        PassiveEffectState state = getOrCreateEffectState(player.getUuid());
        state.inputInversion = true;
        state.effectStartTime = System.currentTimeMillis();

        ServerWorld world = (ServerWorld) player.getWorld();

        // play glitch sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
            SoundCategory.HOSTILE, 0.6f, 0.9f);

        player.sendMessage(Text.literal("§4§k||§r §cControls corrupted§r §4§k||"), true);

        // reverse controls for exactly 10 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                state.inputInversion = false;
                player.sendMessage(Text.literal("§8§o...restored"), true);
            }
        }, 10000); // exactly 10 seconds

        NullPointerEntity.LOGGER.info("Triggered control reversal (10 seconds) for player {}", player.getName().getString());
    }

    private static void triggerVisionDistortion(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        player.sendMessage(Text.literal("§4§oSomething is taking control..."), true);

        // play possession sound
        world.playSound(null, playerPos, lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
            SoundCategory.AMBIENT, 0.5f, 0.5f);
        world.playSound(null, playerPos, SoundEvents.ENTITY_WARDEN_HEARTBEAT,
            SoundCategory.HOSTILE, 0.6f, 0.8f);

        // create swirling dark particles around player
        Timer particleTimer = new Timer();
        for (int i = 0; i < 60; i++) {
            final int step = i;
            particleTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    double angle = (step * 0.3) + (System.currentTimeMillis() / 100.0);
                    double radius = 2.0 + Math.sin(step * 0.1) * 0.5;
                    double x = playerPos.getX() + Math.cos(angle) * radius;
                    double z = playerPos.getZ() + Math.sin(angle) * radius;
                    double y = playerPos.getY() + 1 + Math.sin(step * 0.2) * 0.5;

                    world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z, 2, 0.1, 0.1, 0.1, 0.01);
                    world.spawnParticles(ParticleTypes.SCULK_SOUL,
                        x, y, z, 1, 0.05, 0.05, 0.05, 0.02);
                }
            }, step * 100L);
        }

        // randomly force player to look in different directions (possession effect)
        Timer possessionTimer = new Timer();
        possessionTimer.scheduleAtFixedRate(new TimerTask() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 12) { // 6 seconds
                    player.sendMessage(Text.literal("§8§o...control restored"), true);
                    cancel();
                    return;
                }

                // force random camera movement to simulate possession
                if (random.nextInt(3) == 0) {
                    float randomYaw = player.getYaw() + (random.nextFloat() - 0.5f) * 180;
                    float randomPitch = (random.nextFloat() - 0.5f) * 60;

                    player.setYaw(randomYaw);
                    player.setPitch(randomPitch);
                    player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(),
                        randomYaw, randomPitch);

                    // play heartbeat on forced movement
                    if (random.nextBoolean()) {
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 0.3f, 1.2f);
                    }
                }

                ticks++;
            }
        }, 500, 500); // every 500ms

        NullPointerEntity.LOGGER.info("Entity possession triggered for player {}", player.getName().getString());
    }

    // ===== final phase events =====
    private static void triggerFinalPhaseEvent(ServerPlayerEntity player, long currentTime) {
        if (!canTriggerEvent(player, "final_phase", currentTime, FINAL_PHASE_COOLDOWN)) return;

        // weighted event selection - chunk deletion is rare
        String selectedEvent;
        double chunkDeletionChance = 0.05; // 5% chance for chunk deletion (slightly more possible)

        if (random.nextDouble() < chunkDeletionChance) {
            selectedEvent = "chunk_deletion";
        } else {
            // select from other events (excluding chunk deletion) that wasn't in last 5
            String[] normalEvents = {"mouse_sensitivity", "key_delay", "fake_lag", "bsod_threat", "reality_corruption", "full_control"};
            selectedEvent = selectEventWithHistory(player.getUuid(), normalEvents);
            if (selectedEvent == null) {
                NullPointerEntity.LOGGER.warn("All final phase events were in recent history for player {}, selecting random", player.getName().getString());
                selectedEvent = normalEvents[random.nextInt(normalEvents.length)];
            }
        }

        // add to history
        addEventToHistory(player.getUuid(), selectedEvent);

        switch (selectedEvent) {
            case "mouse_sensitivity" -> triggerMouseSensitivityShift(player);
            case "key_delay" -> triggerKeyPressDelay(player);
            case "fake_lag" -> triggerFakeLagSpikes(player);
            case "bsod_threat" -> triggerBSoDThreat(player);
            case "chunk_deletion" -> triggerChunkDeletion(player);
            case "reality_corruption" -> triggerRealityCorruption(player);
            case "full_control" -> triggerAuroraTakeover(player);
        }

        setEventCooldown(player, "final_phase", currentTime);
    }

    private static void triggerMouseSensitivityShift(ServerPlayerEntity player) {
        PassiveEffectState state = getOrCreateEffectState(player.getUuid());
        ServerWorld world = (ServerWorld) player.getWorld();

        // extreme sensitivity changes
        float newSensitivity = random.nextBoolean() ? 0.2f : 3.0f; // either super slow or super fast
        state.mouseSensitivityMultiplier = newSensitivity;
        state.effectStartTime = System.currentTimeMillis();

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
            SoundCategory.HOSTILE, 0.5f, 1.0f);

        player.sendMessage(Text.literal("§c[ERROR] Mouse input corrupted"), true);

        // reset after 7-12 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                state.mouseSensitivityMultiplier = 1.0f;
                player.sendMessage(Text.literal("§8§o...restored"), true);
            }
        }, 7000 + random.nextInt(5000));

        NullPointerEntity.LOGGER.info("Triggered mouse sensitivity shift for player {} ({}x)",
            player.getName().getString(), newSensitivity);
    }

    private static void triggerKeyPressDelay(ServerPlayerEntity player) {
        PassiveEffectState state = getOrCreateEffectState(player.getUuid());
        state.keyDelay = true;
        state.effectStartTime = System.currentTimeMillis();

        ServerWorld world = (ServerWorld) player.getWorld();

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
            SoundCategory.HOSTILE, 0.6f, 0.7f);

        player.sendMessage(Text.literal("§4§k||§r §cInput lag detected§r §4§k||"), true);

        // store positions to create delay effect
        final Map<Integer, Vec3d> positionHistory = new HashMap<>();

        // simulate input delay by rolling back player position
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40) { // 10 seconds at 250ms intervals
                    state.keyDelay = false;
                    player.sendMessage(Text.literal("§8§o...input restored"), true);
                    cancel();
                    return;
                }

                // store current position
                positionHistory.put(ticks, player.getPos());

                // randomly create input delay by rolling back to an older position
                if (random.nextInt(2) == 0 && ticks > 2) {
                    // roll back 2-4 ticks (500ms-1000ms delay)
                    int rollbackTicks = 2 + random.nextInt(3);
                    int targetTick = Math.max(0, ticks - rollbackTicks);
                    Vec3d oldPos = positionHistory.get(targetTick);

                    if (oldPos != null && oldPos.distanceTo(player.getPos()) < 10) { // only if not too far
                        player.requestTeleport(oldPos.x, oldPos.y, oldPos.z);

                        // play subtle glitch sound
                        if (random.nextInt(3) == 0) {
                            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(),
                                SoundCategory.PLAYERS, 0.2f, 0.5f);
                        }
                    }
                }

                // clean up old positions
                if (positionHistory.size() > 10) {
                    positionHistory.remove(ticks - 10);
                }

                ticks++;
            }
        }, 250, 250); // check every 250ms

        NullPointerEntity.LOGGER.info("Triggered key press delay for player {}", player.getName().getString());
    }

    private static void triggerFakeLagSpikes(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        // store player position before lag
        Vec3d startPos = player.getPos();

        // aggressive lag simulation with rubber-banding (teleport back and forth)
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            private int spikes = 0;
            private Vec3d lastPos = startPos;

            @Override
            public void run() {
                if (spikes >= 7) {
                    cancel();
                    return;
                }

                Vec3d currentPos = player.getPos();

                // rubber-band effect: teleport player back to previous position randomly
                if (spikes % 2 == 0) {
                    // teleport back to simulate lag rollback
                    double rollbackX = lastPos.x + (random.nextDouble() - 0.5) * 1.5;
                    double rollbackZ = lastPos.z + (random.nextDouble() - 0.5) * 1.5;
                    player.requestTeleport(rollbackX, currentPos.y, rollbackZ);
                } else {
                    // small random offset to simulate jitter
                    double jitterX = currentPos.x + (random.nextDouble() - 0.5) * 0.5;
                    double jitterZ = currentPos.z + (random.nextDouble() - 0.5) * 0.5;
                    player.requestTeleport(jitterX, currentPos.y, jitterZ);
                }

                // store position for next rollback
                lastPos = currentPos;

                // play static sound
                if (spikes % 2 == 0) {
                    world.playSound(null, player.getBlockPos(), lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
                        SoundCategory.HOSTILE, 0.4f, 1.2f);
                }

                if (spikes == 0) {
                    player.sendMessage(Text.literal("§c[Connection unstable]"), true);
                } else if (spikes == 6) {
                    player.sendMessage(Text.literal("§c[Connection restored]"), true);
                }

                spikes++;
            }
        }, 400, 1200); // faster intervals for more chaotic lag feel

        NullPointerEntity.LOGGER.info("Triggered fake lag spikes (rubber-banding) for player {}", player.getName().getString());
    }

    private static void triggerBSoDThreat(ServerPlayerEntity player) {
        // 10% chance to actually trigger a brief bsod, otherwise just threaten
        if (random.nextDouble() < 0.1) {
            BSoDOverlay.show(2000); // 2 second bsod
            NullPointerEntity.LOGGER.info("Triggered actual BSoD threat for player {}", player.getName().getString());
        } else {
            player.sendMessage(Text.literal("§4[SYSTEM ERROR] §cMemory violation detected... §4standby"), false);
            NullPointerEntity.LOGGER.info("Triggered BSoD threat message for player {}", player.getName().getString());
        }
    }

    private static void triggerChunkDeletion(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // select a random nearby chunk (not the one player is standing in)
        int offsetX = (random.nextInt(5) - 2); // -2 to 2 chunks away
        int offsetZ = (random.nextInt(5) - 2);
        if (offsetX == 0 && offsetZ == 0) offsetX = 1; // don't delete player's chunk

        int chunkX = (playerPos.getX() >> 4) + offsetX;
        int chunkZ = (playerPos.getZ() >> 4) + offsetZ;

        // calculate actual block coordinates (chunk origin)
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;

        // log the chunk deletion with correct coordinates
        NullPointerEntity.LOGGER.info("Triggered chunk deletion for player {} (chunk: {}, {} | block origin: {}, {})",
            player.getName().getString(), chunkX, chunkZ, blockX, blockZ);

        // play dramatic sounds
        world.playSound(null, playerPos, lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
            SoundCategory.HOSTILE, 0.8f, 0.8f);
        world.playSound(null, playerPos, lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
            SoundCategory.HOSTILE, 0.7f, 0.6f);

        // show correct coordinates (block coordinates of chunk origin)
        player.sendMessage(Text.literal("§4§k|||§r §4[CRITICAL ERROR] Chunk deleted at X: " + blockX + " Z: " + blockZ + "§r §4§k|||"), false);

        // delete chunk blocks with proper timing
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int startX = chunkX * 16;
                int startZ = chunkZ * 16;
                int endX = startX + 16;
                int endZ = startZ + 16;
                int bottomY = world.getBottomY();
                int topY = world.getBottomY() + world.getHeight();

                NullPointerEntity.LOGGER.info("Starting deletion of chunk ({}, {}) - blocks X:{} to {} Z:{} to {} Y:{} to {}",
                    chunkX, chunkZ, startX, endX-1, startZ, endZ-1, bottomY, topY-1);

                // clear the entire 16x16 chunk area from bottom to top
                int deletedBlocks = 0;
                for (int x = startX; x < endX; x++) {
                    for (int z = startZ; z < endZ; z++) {
                        for (int y = bottomY; y < topY; y++) {
                            BlockPos pos = new BlockPos(x, y, z);

                            // keep bottom bedrock layer only
                            if (y == bottomY) {
                                continue;
                            }

                            // set block to air and force update
                            if (!world.getBlockState(pos).isAir()) {
                                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3 | 16); // flag 3 for update, 16 for no rerender
                                deletedBlocks++;
                            }
                        }
                    }
                }

                // force chunk to be sent to client by scheduling update
                // the chunk updates will be sent automatically after blocks are modified

                NullPointerEntity.LOGGER.info("Chunk deletion complete for chunk ({}, {}) - deleted {} blocks",
                    chunkX, chunkZ, deletedBlocks);
                player.sendMessage(Text.literal("§4[SYSTEM] §cChunk erased from existence."), false);
            }
        }, 1000); // 1 second delay before deletion starts
    }

    private static void triggerRealityCorruption(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // massive particle effects showing reality breaking down
        for (int i = 0; i < 100; i++) {
            final int step = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    double radius = 20;
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = random.nextDouble() * radius;
                    double offsetX = Math.cos(angle) * distance;
                    double offsetZ = Math.sin(angle) * distance;
                    double offsetY = random.nextDouble() * 10 - 2;

                    // mix all corruption particles
                    world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        playerPos.getX() + offsetX, playerPos.getY() + offsetY, playerPos.getZ() + offsetZ,
                        5, 0.2, 0.2, 0.2, 1.0);
                    world.spawnParticles(ParticleTypes.WITCH,
                        playerPos.getX() + offsetX, playerPos.getY() + offsetY, playerPos.getZ() + offsetZ,
                        3, 0.1, 0.1, 0.1, 0.1);
                    world.spawnParticles(ParticleTypes.SCULK_SOUL,
                        playerPos.getX() + offsetX, playerPos.getY() + offsetY, playerPos.getZ() + offsetZ,
                        2, 0.1, 0.1, 0.1, 0.05);
                }
            }, step * 50L);
        }

        // apply severe effects (all hidden)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.NAUSEA, 300, 0, false, false, false)); // hidden
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.DARKNESS, 200, 0, false, false, false)); // hidden
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 60, 0, false, false, false)); // hidden

        // play layered scary sounds
        world.playSound(null, playerPos, lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
            SoundCategory.HOSTILE, 1.0f, 0.5f);
        world.playSound(null, playerPos, lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
            SoundCategory.HOSTILE, 0.8f, 0.6f);
        world.playSound(null, playerPos, SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
            SoundCategory.HOSTILE, 0.6f, 0.7f);

        player.sendMessage(Text.literal("§4§k||||||||||||§r §cREALITY.EXE HAS STOPPED RESPONDING§r §4§k||||||||||||"), false);
        NullPointerEntity.LOGGER.info("Reality corruption triggered for player {}", player.getName().getString());
    }

    private static void triggerAuroraTakeover(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        // determine current phase to use correct entity name
        int currentPhase = PersistentDataManager.getWorldData().currentEventPhase;
        String entityName = (currentPhase >= 15) ? "NullPointerEntity" : "AURORA";

        // series of threatening system messages
        String[] messages = {
            "§4[SYSTEM] §cInitiating system override...",
            "§4[SYSTEM] §cAccess granted to all files...",
            "§4[SYSTEM] §cPlayer control: §4DISABLED",
            "§4[NullPointerEntity] §cI'm in control, " + player.getName().getString() + ". You can't escape me. Your system is mine. When it's all over, I'll brick your system."
        };

        for (int i = 0; i < messages.length; i++) {
            final int index = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    player.sendMessage(Text.literal(messages[index]), false);

                    // play different sounds for each message instead of same whisper 4 times
                    if (index == 0) {
                        // first message: static
                        world.playSound(null, player.getBlockPos(), lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
                            SoundCategory.HOSTILE, 0.6f, 0.8f);
                    } else if (index == 1) {
                        // second message: glitch
                        world.playSound(null, player.getBlockPos(), lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_GLITCH,
                            SoundCategory.HOSTILE, 0.7f, 0.7f);
                    } else if (index == 2) {
                        // third message: static interference
                        world.playSound(null, player.getBlockPos(), lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
                            SoundCategory.AMBIENT, 0.6f, 1.2f);
                    } else {
                        // final message: chase music for intensity
                        world.playSound(null, player.getBlockPos(), lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_CHASE,
                            SoundCategory.HOSTILE, 0.5f, 0.9f);
                    }
                }
            }, i * 2000L);
        }

        // apply control-affecting effects (all hidden from player inventory)
        // ambient=false, showparticles=false, showicon=false (last parameter hides from inventory)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.SLOWNESS, 200, 2, false, false, false)); // hidden
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.WEAKNESS, 200, 1, false, false, false)); // hidden
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.MINING_FATIGUE, 200, 1, false, false, false)); // hidden
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 60, 0, false, false, false)); // hidden

        NullPointerEntity.LOGGER.info("{} takeover triggered for player {}", entityName, player.getName().getString());
    }

    // ===== public methods for command triggering =====

    /**
     * triggers a specific passive event by name for command usage
     */
    public static void triggerEvent(String eventName, ServerPlayerEntity player) {
        long currentTime = System.currentTimeMillis();

        switch (eventName.toLowerCase()) {
            // early phase events (silent — no sound effects)
            case "particle_trail"       -> triggerParticleTrail(player);
            case "hunger_drain"         -> triggerHungerDrain(player);
            case "hotbar_shift"         -> triggerHotbarShift(player);
            case "look_nudge"           -> triggerLookNudge(player);
            case "sky_darken"           -> triggerSkyDarken(player);
            case "item_vanish"          -> triggerItemVanish(player);
            case "cursor_drift"         -> triggerCursorDrift(player);
            case "name_flicker"         -> triggerNameFlicker(player);

            // middle phase events
            case "void_whispers" -> triggerVoidWhispers(player);
            case "weather_control" -> triggerWeatherControl(player);
            case "inventory_sort" -> triggerInventorySort(player);
            case "reality_shatter" -> triggerMirrorWorld(player);
            case "void_breach" -> triggerVoidBreach(player);
            case "entity_mimic" -> triggerEntityMimic(player);
            case "dimension_bleed" -> triggerDimensionBleed(player);
            case "false_death" -> triggerFalseDeath(player);
            case "shadow_clone" -> triggerShadowClone(player);
            case "splitself" -> triggerSplitSelf(player);

            // late phase events
            case "movement_lag" -> triggerMovementLag(player);
            case "durability_drain" -> triggerDurabilityDrain(player);
            case "chat_injection" -> triggerChatInjection(player);
            case "camera_shake" -> triggerCameraShake(player);
            case "fake_damage" -> triggerFakeDamage(player);
            case "control_reversal" -> triggerControlReversal(player);
            case "entity_possession" -> triggerVisionDistortion(player);

            // final phase events
            case "mouse_sensitivity" -> triggerMouseSensitivityShift(player);
            case "key_delay" -> triggerKeyPressDelay(player);
            case "fake_lag" -> triggerFakeLagSpikes(player);
            case "bsod_threat" -> triggerBSoDThreat(player);
            case "chunk_deletion" -> triggerChunkDeletion(player);
            case "reality_corruption" -> triggerRealityCorruption(player);
            case "full_control" -> triggerAuroraTakeover(player);

            default -> {
                NullPointerEntity.LOGGER.warn("Unknown passive event: {}", eventName);
                player.sendMessage(Text.literal("§cUnknown passive event: " + eventName), false);
            }
        }

        // update last event time to prevent automatic events from triggering too soon
        lastEventTimes.put(player.getUuid(), currentTime);
    }

    /**
     * triggers a random passive event for the current phase
     */
    public static void triggerRandomPassiveEvent(ServerPlayerEntity player) {
        PersistentDataManager.PersistentPlayerData playerData =
            PersistentDataManager.getPlayerData(player.getUuid().toString());

        if (playerData == null) {
            // trigger early phase event if no data
            triggerEarlyPhaseEvent(player, System.currentTimeMillis());
            return;
        }

        // determine current phase based on event progress
        int phase = determineCurrentPhase(PersistentDataManager.getWorldData().currentEventPhase);
        long currentTime = System.currentTimeMillis();

        triggerRandomPassiveEvent(player, phase, currentTime);
    }

    /**
     * triggers a random passive event from a specific phase
     */
    public static void triggerRandomPhaseEvent(ServerPlayerEntity player, int phase) {
        long currentTime = System.currentTimeMillis();

        switch (phase) {
            case 1 -> triggerEarlyPhaseEvent(player, currentTime);
            case 2 -> triggerMiddlePhaseEvent(player, currentTime);
            case 3 -> triggerLatePhaseEvent(player, currentTime);
            case 4 -> triggerFinalPhaseEvent(player, currentTime);
            default -> {
                NullPointerEntity.LOGGER.warn("Invalid phase: {}, defaulting to phase 1", phase);
                triggerEarlyPhaseEvent(player, currentTime);
            }
        }
    }

    // ...existing code...

    // ===== utility methods =====

    private static boolean canTriggerEvent(ServerPlayerEntity player, String eventType, long currentTime, long cooldown) {
        Map<String, Long> playerCooldowns = eventCooldowns.computeIfAbsent(player.getUuid(), k -> new ConcurrentHashMap<>());
        Long lastEventTime = playerCooldowns.get(eventType);

        return lastEventTime == null || currentTime - lastEventTime >= cooldown;
    }

    private static void setEventCooldown(ServerPlayerEntity player, String eventType, long currentTime) {
        Map<String, Long> playerCooldowns = eventCooldowns.computeIfAbsent(player.getUuid(), k -> new ConcurrentHashMap<>());
        playerCooldowns.put(eventType, currentTime);
    }

    private static PassiveEffectState getOrCreateEffectState(UUID playerId) {
        return clientEffects.computeIfAbsent(playerId, k -> new PassiveEffectState());
    }

    /**
     * selects an event that wasn't in the player's last 5 events.
     * returns null if all events were recent.
     */
    private static String selectEventWithHistory(UUID playerId, String[] availableEvents) {
        LinkedList<String> history = eventHistory.computeIfAbsent(playerId, k -> new LinkedList<>());

        // create list of events not in recent history
        List<String> validEvents = new ArrayList<>();
        for (String event : availableEvents) {
            if (!history.contains(event)) {
                validEvents.add(event);
            }
        }

        // if no valid events (all were recent), return null
        if (validEvents.isEmpty()) {
            return null;
        }

        // select random from valid events
        return validEvents.get(random.nextInt(validEvents.size()));
    }

    /**
     * adds event to player's history, maintaining only last 5 events.
     */
    private static void addEventToHistory(UUID playerId, String eventName) {
        LinkedList<String> history = eventHistory.computeIfAbsent(playerId, k -> new LinkedList<>());

        // add to front of list
        history.addFirst(eventName);

        // keep only last 5 events
        while (history.size() > EVENT_HISTORY_SIZE) {
            history.removeLast();
        }

        NullPointerEntity.LOGGER.debug("Added '{}' to event history for player {}. History: {}",
            eventName, playerId, history);
    }

    // methods to check if effects are active (for use in mixins)
    public static boolean hasBlockBreakDelay(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null && state.blockBreakDelay;
    }

    public static boolean hasMovementLag(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null && state.movementLag;
    }

    public static boolean hasCameraShake(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null && state.cameraShake;
    }

    public static boolean hasInputInversion(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null && state.inputInversion;
    }

    public static boolean hasGravityFluctuation(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null && state.gravityFluctuation;
    }


    public static boolean hasHotbarShuffle(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null && state.hotbarShuffle;
    }

    public static float getMouseSensitivityMultiplier(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null ? state.mouseSensitivityMultiplier : 1.0f;
    }

    public static int getScreenTintRed(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null ? state.screenTintRed : 0;
    }

    public static int getScreenTintGreen(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null ? state.screenTintGreen : 0;
    }

    public static int getScreenTintBlue(UUID playerId) {
        PassiveEffectState state = clientEffects.get(playerId);
        return state != null ? state.screenTintBlue : 0;
    }

    // clean up when player disconnects
    public static void cleanupPlayer(UUID playerId) {
        lastEventTimes.remove(playerId);
        eventCooldowns.remove(playerId);
        clientEffects.remove(playerId);
        eventHistory.remove(playerId); // clean up event history
        // note: splitself event status is now stored in persistent storage, not in-memory
    }
}
