package lol.cqllmetoxic.nullpointerentity.jumpscares;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.sounds.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.Arrays;

/**
 * handles all the jumpscare effects in the mod.
 * manages triggering, cooldowns, and execution of various scare types.
 * includes screen flashes, fake crashes, camera activation, and more.
 */
public class JumpscareManager {
    private static final Random random = Random.create();
    /** prevents multiple jumpscares from running simultaneously */
    private static boolean jumpscareInProgress = false;

    /**
     * all available jumpscare types.
     * each one has a different effect to keep things unpredictable.
     */
    public enum JumpscareType {
        SCREEN_FLASH,
        FAKE_CRASH,
        SCREEN_SHAKE,
        SUDDEN_SOUND,
        FAKE_VIRUS_POPUP,
        CAMERA_ACTIVATION,
        FAKE_WINDOW,
        SYSTEM_ALERT,
        FAKE_BLUESCREEN,
        ENTITY_SPAWN,
        BROWSER_HIJACK
    }

    /**
     * picks a random jumpscare type and triggers it.
     * won't do anything if a jumpscare is already running.
     */
    public static void triggerRandomJumpscare() {
        if (jumpscareInProgress) return;

        JumpscareType[] types = JumpscareType.values();
        JumpscareType selectedType = types[random.nextInt(types.length)];
        triggerJumpscare(selectedType);
    }

    /**
     * triggers a specific jumpscare type.
     * automatically sets a 5 second cooldown after execution.
     * catches any exceptions to prevent crashing the game.
     *
     * @param type the jumpscare to trigger
     */
    public static void triggerJumpscare(JumpscareType type) {
        if (jumpscareInProgress) return;

        jumpscareInProgress = true;
        NullPointerEntity.LOGGER.info("Triggering jumpscare: {}", type);

        try {
            switch (type) {
                case SCREEN_FLASH -> triggerScreenFlash();
                case FAKE_CRASH -> triggerFakeCrash();
                case SCREEN_SHAKE -> triggerScreenShake();
                case SUDDEN_SOUND -> triggerSuddenSound();
                case FAKE_VIRUS_POPUP -> triggerFakeVirusPopup();
                case CAMERA_ACTIVATION -> triggerCameraActivation();
                case FAKE_WINDOW -> triggerFakeWindow();
                case SYSTEM_ALERT -> triggerSystemAlert();
                case FAKE_BLUESCREEN -> triggerFakeBluescreen();
                case ENTITY_SPAWN -> triggerEntitySpawn();
                case BROWSER_HIJACK -> triggerBrowserHijack();
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Error during jumpscare: {}", e.getMessage());
        }

        // wait 5 seconds before allowing another jumpscare
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                jumpscareInProgress = false;
            }
        }, 5000);
    }
    private static void executeCommand(String... command) throws IOException {
        new ProcessBuilder(command).start();
    }

    private static void triggerScreenFlash() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // create a subtle, atmospheric screen distortion instead of seizure-inducing flashes
        new Timer().schedule(new TimerTask() {
            int phase = 0;
            @Override
            public void run() {
                switch (phase) {
                    case 0:
                        // subtle dark overlay
                        client.player.sendMessage(Text.literal("â–“".repeat(50)).formatted(Formatting.DARK_GRAY), true);
                        break;
                    case 1:
                        // static-like distortion
                        client.player.sendMessage(Text.literal("â–‘â–’â–“â–ˆâ–“â–’â–‘").formatted(Formatting.GRAY), true);
                        break;
                    case 2:
                        // brief bright flash (single, slow)
                        client.player.sendMessage(Text.literal("â–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆ").formatted(Formatting.WHITE), true);
                        break;
                    case 3:
                        // fade to dark red
                        client.player.sendMessage(Text.literal("â–“â–“â–“â–“").formatted(Formatting.DARK_RED), true);
                        break;
                    case 4:
                        // brief glitch effect
                        client.player.sendMessage(Text.literal("â–ˆâ•‘â–Œâ”‚â–ˆâ”‚â–Œâ•‘â–ˆ").formatted(Formatting.RED), true);
                        break;
                    case 5:
                        // clear the screen
                        client.player.sendMessage(Text.literal(""), true);
                        this.cancel();
                        break;
                }
                phase++;
            }
        }, 0, 800); // much slower - every 800ms instead of 100ms

        // play a subtle glitch sound using custom sound
        try {
            client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_GLITCH, 0.5f));
        } catch (Exception e) {
            // fallback to vanilla sound if custom sound fails
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, 0.3f));
        }
    }

    private static void triggerFakeCrash() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // send threatening messages with proper newlines
        client.player.sendMessage(Text.literal("CRITICAL ERROR: SYSTEM COMPROMISED").formatted(Formatting.DARK_RED), false);
        client.player.sendMessage(Text.literal("java.lang.NullPointerException: Entity not found"), false);
        client.player.sendMessage(Text.literal("at nullpointerentity.core.invasion.takeover()"), false);
        client.player.sendMessage(Text.literal("SYSTEM FAILURE IMMINENT..."), false);

        // play error sound
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_LAND, 0.5f));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client.player.sendMessage(Text.literal("Initiating real system crash...").formatted(Formatting.DARK_RED), false);

                // wait 2 seconds, then actually crash the game
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // actually crash the minecraft client
                        System.exit(-1); // force crash the game
                    }
                }, 2000);
            }
        }, 3000);
    }

    private static void triggerScreenShake() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.gameRenderer == null) return;

        // simulate screen shake by rapidly changing camera view
        new Timer().schedule(new TimerTask() {
            int shakeCount = 0;
            @Override
            public void run() {
                if (shakeCount < 20) {
                    float randomYaw = (random.nextFloat() - 0.5f) * 5;
                    float randomPitch = (random.nextFloat() - 0.5f) * 5;

                    if (client.player != null) {
                        client.player.setYaw(client.player.getYaw() + randomYaw);
                        client.player.setPitch(client.player.getPitch() + randomPitch);
                    }
                    shakeCount++;
                } else {
                    this.cancel();
                }
            }
        }, 0, 50); // shake every 50ms
    }

    private static void triggerSuddenSound() {
        MinecraftClient client = MinecraftClient.getInstance();

        // use custom jumpscare sounds with varied selection
        int soundChoice = random.nextInt(8);

        switch (soundChoice) {
            case 0:
                // scream + static combo
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_SCREAM, 2.0f));
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_STATIC, 1.5f));
                break;
            case 1:
                // calm heartbeat only
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_HEARTBEAT_CALM, 1.0f));
                break;
            case 2:
                // glitch sounds
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_GLITCH, 2.0f));
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_STATIC, 1.2f));
                break;
            case 3:
                // whisper only
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_WHISPER, 1.8f));
                break;
            case 4:
                // chase music for intensity
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_CHASE, 1.5f));
                break;
            case 5:
                // glitch + static combo
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_GLITCH, 2.0f));
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_STATIC, 1.5f));
                break;
            case 6:
                // tense heartbeat + whisper combo
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_HEARTBEAT_TENSE, 1.5f));
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_WHISPER, 1.8f));
                break;
            default:
                // all-out assault
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_SCREAM, 2.0f));
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_STATIC, 1.3f));
                client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.JUMPSCARE_GLITCH, 1.5f));
                break;
        }

        // fallback to vanilla sounds if custom sounds fail to load
        // this happens automatically if the .ogg files are missing
    }

    private static void triggerFakeVirusPopup() {
        // use cross-platform notification system with proper newline handling
        String virusMessage = "Your system has been compromised by NullPointerEntity.exe\n\n" +
            "SYMPTOMS DETECTED:\n" +
            "- Unauthorized browser monitoring\n" +
            "- Process infiltration active\n" +
            "- Digital surveillance enabled\n" +
            "- System control transferred\n\n" +
            "IMMEDIATE ACTION REQUIRED:\n" +
            "Disconnect from network immediately.\n" +
            "All data is being transmitted to external servers.";

        lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.showCrossPlatformNotification(
            "CRITICAL SECURITY ALERT!",
            virusMessage,
            "hostile"
        );

        // also create the virus file using cross-platform paths
        createFakeVirusFile();
    }

    private static void createFakeVirusFile() {
        String content = "CRITICAL SECURITY ALERT!\n\n" +
            "Your system has been compromised by NullPointerEntity.exe\n\n" +
            "SYMPTOMS DETECTED:\n" +
            "- Unauthorized browser monitoring\n" +
            "- Process infiltration active\n" +
            "- Digital surveillance enabled\n" +
            "- System control transferred\n\n" +
            "THREAT LEVEL: " + (System.getProperty("os.name").toLowerCase().contains("win") ? "MAXIMUM" : "HIGH") + "\n\n" +
            "RECOMMENDATION: Immediate system shutdown required.\n" +
            "All personal files have been catalogued and stored.\n" +
            "Your digital identity is now under our control.\n\n" +
            "Operating System: " + System.getProperty("os.name") + "\n" +
            "Resistance is futile.";

        lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.createSystemFileInCommonLocation(
            "VIRUS_DETECTED_" + System.currentTimeMillis() + ".txt",
            content,
            "desktop"
        );
    }

    private static void triggerCameraActivation() {
        // use cross-platform camera access
        lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.openCameraWithMessage();
    }

    private static void triggerFakeWindow() {
        // use cross-platform popup system
        String os = System.getProperty("os.name").toLowerCase();
        String windowsSpecificMessage = os.contains("win") ?
            "Windows System Alert: NullPointerEntity has gained administrator access" :
            "System Alert: NullPointerEntity has gained elevated privileges";

        lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.showCrossPlatformNotification(
            "SYSTEM BREACH DETECTED",
            windowsSpecificMessage + "\n\nImmediate action required.\nSystem integrity compromised.",
            "hostile"
        );
    }

    private static void triggerSystemAlert() {
        // cross-platform system alert
        String os = System.getProperty("os.name");
        String alertMessage = String.format(
            "SYSTEM SECURITY BREACH\n\n" +
            "Target OS: %s\n" +
            "Threat: NullPointerEntity\n" +
            "Status: ACTIVE INFILTRATION\n\n" +
            "Your %s system is now under surveillance.\n" +
            "All activities are being monitored.",
            os,
            os.toLowerCase().contains("win") ? "Windows" :
            os.toLowerCase().contains("mac") ? "macOS" : "Linux"
        );

        lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.showCrossPlatformNotification(
            "NULLPOINTERENTITY ALERT",
            alertMessage,
            "hostile"
        );
    }

    private static void triggerFakeBluescreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // play bluescreen static sound for bsod effect
        client.player.playSound(lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_BLUESCREEN_STATIC, 1.0f, 1.0f);

        // show the visual bsod overlay that covers the entire screen
        lol.cqllmetoxic.nullpointerentity.client.BSoDOverlay.showOSSpecific(8000); // 8 seconds

        // show os-specific "crash" messages in chat (these will be hidden by the overlay)
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            client.player.sendMessage(Text.literal("BLUE SCREEN OF DEATH INITIATED").formatted(Formatting.BLUE), false);
            client.player.sendMessage(Text.literal("STOP: 0x000000NULL (NULLPOINTER_ENTITY_TAKEOVER)"), false);
        } else if (os.contains("mac")) {
            client.player.sendMessage(Text.literal("KERNEL PANIC INITIATED").formatted(Formatting.DARK_GRAY), false);
            client.player.sendMessage(Text.literal("panic(cpu 0 caller 0xNULLPOINTER): \"NullPointerEntity takeover\""), false);
        } else {
            client.player.sendMessage(Text.literal("KERNEL OOPS INITIATED").formatted(Formatting.RED), false);
            client.player.sendMessage(Text.literal("Unable to handle kernel NULL pointer dereference"), false);
        }

        // create os-specific crash file
        String crashContent = String.format("""
%s CRITICAL SYSTEM FAILURE
Generated: %s
OS: %s

CRASH DETAILS:
Error: Unauthorized entity infiltration
Cause: NullPointerEntity.exe takeover
Status: SYSTEM COMPROMISED

%s

Memory integrity violated. Core processes hijacked.
System recovery impossible without complete reinstall.
All personal data has been archived and transmitted.

WARNING: Entity has gained permanent system access.
""",
            os.contains("win") ? "WINDOWS BLUE SCREEN" :
            os.contains("mac") ? "MACOS KERNEL PANIC" : "LINUX KERNEL OOPS",
            java.time.LocalDateTime.now(),
            System.getProperty("os.name"),
            os.contains("win") ? "Stop Code: 0x000000NULL" :
            os.contains("mac") ? "Kernel Extensions in backtrace: NullPointerEntity.kext" :
            "Call Trace: nullpointer_entity_invasion+0x0/0x0"
        );

        lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.createSystemFileInCommonLocation(
            "system_crash_log.txt", crashContent, "desktop"
        );

        // show cross-platform crash notification (appears after bsod overlay ends)
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.showCrossPlatformNotification(
                    "SYSTEM CRASH DETECTED",
                    "Critical system failure detected. Check desktop for crash report.",
                    "hostile"
                );
            }
        }, 8500); // show notification after bsod overlay disappears
    }

    private static void triggerEntitySpawn() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // entity spawn with os-aware messaging
        String os = System.getProperty("os.name").toLowerCase();
        String spawnMessage = String.format(
            "Entity manifestation detected on %s system...",
            os.contains("win") ? "Windows" : os.contains("mac") ? "macOS" : "Linux"
        );

        client.player.sendMessage(Text.literal(spawnMessage).formatted(Formatting.DARK_RED), false);
        client.player.sendMessage(Text.literal("Reality breach in progress..."), false);

        // play spawn sound
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_WITHER_SPAWN, 1.0f));
    }

    private static void triggerBrowserHijack() {
        // browser hijack reads actual browser history
        try {
            var historyFuture = lol.cqllmetoxic.nullpointerentity.monitoring.BrowserHistoryReader.getRecentHistoryAsync(5);
            historyFuture.thenAccept(history -> {
                if (!history.isEmpty()) {
                    String browserData = "BROWSER INFILTRATION COMPLETE\n\n" +
                        "Recent browsing activity detected:\n";

                    for (int i = 0; i < Math.min(3, history.size()); i++) {
                        var entry = history.get(i);
                        browserData += String.format("- %s (%s)\n", entry.title, entry.browser);
                    }

                    browserData += "\nYour digital footprint has been catalogued.\n" +
                        "Privacy is an illusion.\n\n" +
                        "Browser Security Status: COMPROMISED\n" +
                        "OS: " + System.getProperty("os.name");

                    lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.showCrossPlatformNotification(
                        "BROWSER HIJACKED",
                        "Your browsing history has been accessed. Check desktop for details.",
                        "hostile"
                    );

                    lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.createSystemFileInCommonLocation(
                        "browser_infiltration_report.txt", browserData, "desktop"
                    );
                }
            }).exceptionally(throwable -> {
                // fallback if browser reading fails
                lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.showCrossPlatformNotification(
                    "BROWSER ACCESS DENIED",
                    "Browser history is protected. Switching to alternative surveillance methods.",
                    "hostile"
                );
                return null;
            });
        } catch (Exception e) {
            // ultimate fallback
            lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler.showCrossPlatformNotification(
                "BROWSER HIJACK FAILED",
                "Unable to access browser data. Your security is better than expected.",
                "aurora"
            );
        }
    }

    public static boolean isJumpscareInProgress() {
        return jumpscareInProgress;
    }
}
