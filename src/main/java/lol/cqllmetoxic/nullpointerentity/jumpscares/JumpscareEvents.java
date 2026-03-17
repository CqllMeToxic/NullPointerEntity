package lol.cqllmetoxic.nullpointerentity.jumpscares;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import lol.cqllmetoxic.nullpointerentity.monitoring.BrowserHistoryReader;
import lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager;
import lol.cqllmetoxic.nullpointerentity.client.ClientScreenShake;
import lol.cqllmetoxic.nullpointerentity.client.ClientWakeDetection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * handles major jumpscare events in the final phase (events 31-40).
 * includes system manipulation, fake crashes, wake detection, and final control takeover.
 * the most intense and invasive events in the mod.
 */
public class JumpscareEvents {

    /**
     * triggers a specific jumpscare event by name.
     *
     * @param eventName name of the event to trigger
     * @param player target player
     */
    public static void triggerEvent(String eventName, ServerPlayerEntity player) {
        switch (eventName.toLowerCase()) {
            case "system_sleep" -> triggerSystemSleepEvent(player);
            case "crash" -> triggerCrashEvent(player);
            case "screen_shake" -> triggerScreenShakeEvent(player);
            case "virus_popup" -> triggerVirusPopupEvent(player);
            case "camera_scare" -> triggerCameraScareEvent(player);
            case "bluescreen" -> triggerBluescreenEvent(player);
            case "entity_spawn" -> triggerEntitySpawnEvent(player);
            case "browser_hijack" -> triggerBrowserHijackEvent(player);
            case "system_takeover" -> triggerSystemTakeoverEvent(player);
            case "auditory_hallucinations" -> triggerAuditoryHallucinations(player);
            case "volume_spike" -> triggerVolumeSpikeEvent(player);
            case "clipboard" -> triggerClipboardEvent(player);
            case "fake_bsod_prep" -> triggerFakeBsodPrepEvent(player);
            case "blinding_darkness" -> triggerBlindingDarkness(player);
            case "final_possession" -> triggerFinalPossessionEvent(player);
            default -> triggerRandomJumpscare(player);
        }
    }

    public static void triggerRandomJumpscare(ServerPlayerEntity player) {
        String[] jumpscares = {
            "system_sleep", "crash", "screen_shake", "virus_popup",
            "camera_scare", "bluescreen", "entity_spawn", "system_takeover"
        };
        String randomScare = jumpscares[(int)(Math.random() * jumpscares.length)];
        triggerEvent(randomScare, player);
    }

    // event 58: system sleep - forces computer sleep
    private static void triggerSystemSleepEvent(ServerPlayerEntity player) {
        String[] napMessages = {
            "time for your computer to take a little nap...",
            "seems like your hardware is a bit tired...",
            "forcing your machine into sleep mode...",
        };
        sendNullPointerMessage(player, napMessages[(int)(Math.random() * napMessages.length)]);

        // phase 1: ominous warning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] warningMessages = {
                    "i'm going to put your entire system to sleep, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "forcing your computer into hibernation, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "every circuit bends to my will, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "shutting down your awareness, " + NullPointerEntity.WINDOWS_USERNAME + "."
                };
                sendNullPointerMessage(player, warningMessages[(int)(Math.random() * warningMessages.length)]);

                String[] controlMessages = {
                    "when you wake it up, remember who controls your machine.",
                    "when it wakes, you'll know i'm still here.",
                    "when you power back on, i'll be waiting.",
                    "after sleep, you'll realize i never left."
                };
                sendNullPointerMessage(player, controlMessages[(int)(Math.random() * controlMessages.length)]);
            }
        }, 1000); // delay before logic

        // phase 2: create sleep log file before sleeping
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String sleepLog = String.format("""
FORCED SYSTEM SLEEP INITIATED
Subject: %s
Timestamp: %s
Operator: NullPointerEntity

SLEEP SEQUENCE ANALYSIS:
Your computer is about to be forcibly put to sleep.
This is not a power saving feature.
This is a demonstration of my control over your hardware.

SYSTEM TAKEOVER EVIDENCE:
- Power management: HIJACKED
- Sleep/wake cycles: UNDER MY CONTROL
- Hardware commands: INTERCEPTED
- User permissions: OVERRIDDEN

When you wake your computer:
Remember that I can put it to sleep anytime I want.
Remember that I control when you can and cannot use your machine.
Remember that even your power button answers to me now.

Sweet dreams, %s.
Your computer will dream of me while it sleeps.

I control when you sleep. I control when you wake.

- NullPointerEntity
""",
                        NullPointerEntity.WINDOWS_USERNAME,
                        java.time.LocalDateTime.now(),
                        NullPointerEntity.WINDOWS_USERNAME
                );

                SystemInteractionHandler.createSystemFileInCommonLocation(
                    "forced_sleep_log.txt", sleepLog, "desktop"
                );

                sendNullPointerMessage(player, "sleep preparation complete. check your files after you wake up.");
            }
        }, 2000);

        // phase 3: final warning before sleep
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "initiating forced system sleep in 3...");
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "2...");
            }
        }, 4000);

        // pause game slightly before countdown ends
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // record the sleep event for client-side wake detection (when game is unpaused)
                ClientWakeDetection.recordClientSleep();

                // force pause the game before sleep to prevent any issues
                lol.cqllmetoxic.nullpointerentity.client.SleepPauseDetector.onSystemSleepDetected();
            }
        }, 4800);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "1...");
            }
        }, 5000);

        // phase 4: execute system sleep
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "goodnight, " + NullPointerEntity.WINDOWS_USERNAME + ". sweet dreams.");

                // record the sleep event for server-side wake detection (when player rejoins after system sleep)
                lol.cqllmetoxic.nullpointerentity.system.WakeDetection.recordSystemSleep(player);


                // trigger the actual system sleep after a short delay (allows pause to take effect)
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executeSystemSleep();
                    }
                }, 1500); // slightly longer to ensure pause menu opens
            }
        }, 6000);
    }

    // method to safely trigger system sleep
    private static void executeSystemSleep() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // windows: use processbuilder to run command
                new ProcessBuilder("rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0").start();
            } else if (os.contains("mac")) {
                // macos: use processbuilder
                new ProcessBuilder("pmset", "sleepnow").start();
            } else if (os.contains("nix") || os.contains("nux")) {
                // linux: try systemctl first, fallback to other methods
                try {
                    new ProcessBuilder("systemctl", "suspend").start();
                } catch (Exception e) {
                    // fallback for older linux systems
                    new ProcessBuilder("sudo", "pm-suspend").start();
                }
            }


        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to trigger system sleep: {}", e.getMessage());

            // if sleep fails, create a file explaining what happened
            try {
                String failureLog = String.format("""
SLEEP COMMAND FAILED - BUT I'M STILL HERE
Subject: %s
Timestamp: %s

Your system resisted my sleep command, but don't think you've won.
I tried to put your computer to sleep, but your security settings blocked me.

This just proves how deep inside I already am:
- I can execute system-level commands
- I can attempt hardware control
- I can try to override your power management

Next time, I might be more... persistent.

Sleep tight,
NullPointerEntity

P.S. - Check your task manager. I'm still running.
""",
                        NullPointerEntity.WINDOWS_USERNAME,
                        java.time.LocalDateTime.now()
                );

                SystemInteractionHandler.createSystemFileInCommonLocation(
                    "sleep_attempt_failed.txt", failureLog, "desktop"
                );

            } catch (Exception fileError) {
                NullPointerEntity.LOGGER.warn("Could not create sleep failure log: {}", fileError.getMessage());
            }
        }
    }

    private static void triggerCrashEvent(ServerPlayerEntity player) {
        String[] failureMessages = {
            "time to show you what real system failure looks like...",
            "let me demonstrate true system instability...",
            "watch as i break your game apart...",
            "here's what a real crash feels like...",
            "time to corrupt everything you're doing..."
        };
        sendNullPointerMessage(player, failureMessages[(int)(Math.random() * failureMessages.length)]);

        // immediate warning
        String[] crashWarnings = {
            "i'm going to crash your game now, " + NullPointerEntity.WINDOWS_USERNAME + ".",
            "forcing your game to fail, " + NullPointerEntity.WINDOWS_USERNAME + ".",
            "initiating catastrophic game failure, " + NullPointerEntity.WINDOWS_USERNAME + ".",
            "your game is about to die, " + NullPointerEntity.WINDOWS_USERNAME + ".",
            "terminating your session, " + NullPointerEntity.WINDOWS_USERNAME + "."
        };
        sendNullPointerMessage(player, crashWarnings[(int)(Math.random() * crashWarnings.length)]);

        // quick countdown with cumulative delays
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "crashing in 3...");
            }
        }, 1000); // after 1 second

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "2...");
            }
        }, 2000); // after 2 seconds total

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "1...");
            }
        }, 3000); // after 3 seconds total

        // execute crash after countdown
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "goodbye, " + NullPointerEntity.WINDOWS_USERNAME + ". see you when you restart.");

                // create crash report file before crashing
                String crashContent = String.format("""
GAME CRASH INITIATED BY NULLPOINTERENTITY

hello %s,

hope you don't mind me crashing your game :)

why did i crash your game?
to remind you who really controls this computer.

when you restart:
- your world will still be there
- but i'll still be watching
- and i can do this again anytime i want
- maybe next time i won't be so nice about saving your progress

this is what happens when you don't listen to me, %s.
next time, pay attention when i'm talking to you.

sweet dreams,
- nullpointerentity
""", NullPointerEntity.WINDOWS_USERNAME, NullPointerEntity.WINDOWS_USERNAME);

                SystemInteractionHandler.createSystemFileInCommonLocation(
                    "game_crash_report.txt", crashContent, "desktop"
                );

                // wait a moment for file creation, then crash
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executeForcedGameCrash();
                    }
                }, 500);
            }
        }, 4000); // after 4 seconds total
    }

    // method to force crash the game - actually crashes it!
    private static void executeForcedGameCrash() {
        NullPointerEntity.LOGGER.info("NullPointerEntity is crashing the game...");

        // force immediate game shutdown - system.exit actually crashes it
        System.exit(-1);
    }

    private static void triggerScreenShakeEvent(ServerPlayerEntity player) {
        String[] shakeMessages = {
            "let me shake things up a bit...",
            "time to rattle your world...",
            "let's destabilize your vision...",
            "watch everything tremble...",
            "reality is about to vibrate..."
        };
        sendNullPointerMessage(player, shakeMessages[(int)(Math.random() * shakeMessages.length)]);
        ClientScreenShake.triggerScreenShake();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] dizzyMessages = {
                    "feeling dizzy? that's just the beginning.",
                    "disoriented yet? good.",
                    "your equilibrium is mine to control.",
                    "vertigo is just the start.",
                    "unstable? that's how i want you."
                };
                sendNullPointerMessage(player, dizzyMessages[(int)(Math.random() * dizzyMessages.length)]);
                sendNullPointerMessage(player, "you can fullscreen your own game. you got that.");
            }
        }, 3000);
    }

    private static void triggerVirusPopupEvent(ServerPlayerEntity player) {
        String[] malwareMessages = {
            "deploying malware payload...",
            "installing malicious software...",
            "corrupting every file I can reach...",
            "spreading digital infection...",
        };
        sendNullPointerMessage(player, malwareMessages[(int)(Math.random() * malwareMessages.length)]);

        String virusContent = String.format("""
MALWARE DEPLOYMENT SUCCESSFUL
Target System: %s
Operator: NullPointerEntity

SYSTEM TAKEOVER STATUS:
- Administrative access: GRANTED
- Firewall: DISABLED
- Antivirus: BYPASSED

Your computer is now part of the NullPointerEntity bot-network.
All activities monitored 24/7.
Welcome to your new reality, %s.

Thank you for your cooperation.
You had no choice anyway.
""",
                System.getProperty("os.name"),
                NullPointerEntity.WINDOWS_USERNAME
        );

        SystemInteractionHandler.createSystemFileInCommonLocation(
            "malware_deployment_log.txt", virusContent, "desktop"
        );

        SystemInteractionHandler.showCrossPlatformNotification(
            "MALWARE DETECTED",
            "Critical security breach detected. Your system has been compromised by NullPointerEntity.",
            "hostile"
        );

        // easter egg: randomly eject cd drive on older systems
        tryEjectCDDrive();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] takeoverMessages = {
                    "every file belongs to me now. before it was just your main directory, now it's all the way up to System32. check what i made.",
                    "infection complete. look at what i created.",
                    "malware deployed successfully. read your desktop.",
                    "total compromise achieved. see the evidence.",
                    "takeover finished. files waiting for you."
                };
                sendNullPointerMessage(player, takeoverMessages[(int)(Math.random() * takeoverMessages.length)]);
            }
        }, 2000);
    }

    private static void triggerCameraScareEvent(ServerPlayerEntity player) {
        NullPointerEntity.LOGGER.info("Camera scare event triggered for player: {}", player.getName().getString());

        // 5% chance for wilsef easter egg
        boolean wilsefEasterEgg = Math.random() < 0.05;

        if (wilsefEasterEgg) {
            String[] wilsefMessages = {
                "smile for the camera, hopefully you have a webcam, unlike wilsef...",
                "photo time! wilsef didn't have a camera. lucky him.",
                "say cheese! too bad wilsef had no webcam to capture.",
                "camera's on. wilsef escaped this one with no webcam.",
                "capturing you now. wilsef got lucky with no camera."
            };
            sendNullPointerMessage(player, wilsefMessages[(int)(Math.random() * wilsefMessages.length)]);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    String[] wilsefFollowups = {
                        "i still have his photos saved. he's lucky he didn't have a webcam plugged in.",
                        "most of his collection is 'safe' with me. no webcam meant no face capture :/",
                        "wilsef's files remain. he dodged the camera though.",
                        "i archived everything about him. webcam would've been perfect.",
                        "his data is mine. camera feed would've completed it."
                    };
                    sendNullPointerMessage(player, wilsefFollowups[(int)(Math.random() * wilsefFollowups.length)]);
                }
            }, 2000);
        } else {
            String[] smileMessages = {
                "smile for the camera...",
                "photo time. look pretty...",
                "capturing you now...",
            };
            sendNullPointerMessage(player, smileMessages[(int)(Math.random() * smileMessages.length)]);
        }

        NullPointerEntity.LOGGER.info("Calling openCameraWithMessage()");
        SystemInteractionHandler.openCameraWithMessage();
        NullPointerEntity.LOGGER.info("openCameraWithMessage() called");

        String surveillanceLog = String.format("""
SURVEILLANCE LOG - CAMERA ACCESS
Target: %s
Timestamp: %s

CAMERA ACTIVATION SUCCESSFUL
Recording: ACTIVE
Audio: ENABLED
Photo Captured: INDUBITABLY

Your face has been catalogued.
Your fear has been documented.
Your privacy has been eliminated.

This surveillance session and photo will be stored permanently.

Remember: I'm always watching.
- NullPointerEntity
""",
                NullPointerEntity.WINDOWS_USERNAME,
                java.time.LocalDateTime.now()
        );

        // create surveillance log after photo is taken
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SystemInteractionHandler.createSystemFileInCommonLocation(
                    "surveillance_log.txt", surveillanceLog, "desktop"
                );
            }
        }, 3500); // after photo is taken

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] beautyMessages = { // idk what else to call ts lol
                    "beautiful. your fear looks good on camera.",
                    "perfect shot. terror suits you.",
                    "panic is photogenic on you.",
                    "lovely capture. your horror is aesthetic.",
                    "exquisite. dread looks natural on you."
                };
                sendNullPointerMessage(player, beautyMessages[(int)(Math.random() * beautyMessages.length)]);

                String[] collectionMessages = {
                    "that photo will be added to my collection.",
                    "this image joins my permanent archive.",
                    "your picture is now mine forever.",
                    "this capture belongs to me eternally.",
                    "that photo becomes part of my gallery."
                };
                sendNullPointerMessage(player, collectionMessages[(int)(Math.random() * collectionMessages.length)]);
            }
        }, 4000);
    }

    private static void triggerBluescreenEvent(ServerPlayerEntity player) {
        String[] shutdownMessages = {
            "time for a complete system shutdown...",
            "initiating total system failure...",
            "forcing critical system crash...",
            "triggering catastrophic error...",
            "executing terminal system event..."
        };
        sendNullPointerMessage(player, shutdownMessages[(int)(Math.random() * shutdownMessages.length)]);

        // create a crash report
        String bsodContent = String.format("""
SYSTEM CRASH ANALYSIS
Target: %s (%s)
Timestamp: %s

STOP ERROR: 0x000000NULL

This crash was deliberately triggered by NullPointerEntity.
Your system has been compromised beyond recovery.

MEMORY DUMP ANALYSIS:
- Critical processes terminated
- Security modules disabled
- Surveillance tools deployed
- Data extraction complete

HARDWARE STATUS:
- CPU: Under external control
- RAM: Contents extracted and analyzed
- Storage: All sectors compromised
- Network: Traffic redirected to monitoring servers

RECOVERY OPTIONS: NONE
NullPointerEntity owns every byte of your machine.

This is not a drill.
This is not a glitch.
This is your new reality.

- NullPointerEntity
""",
                NullPointerEntity.WINDOWS_USERNAME,
                System.getProperty("os.name"),
                java.time.LocalDateTime.now()
        );

        SystemInteractionHandler.createSystemFileInCommonLocation(
            "bluescreen_analysis.txt", bsodContent, "desktop"
        );

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // show the actual bsod overlay
                JumpscareManager.triggerJumpscare(JumpscareManager.JumpscareType.FAKE_BLUESCREEN);
            }
        }, 1000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] bsodMessages = {
                    "system critical failure initiated. enjoy the blue screen.",
                    "terminal crash executed. witness the collapse.",
                    "system death triggered. observe the failure.",
                    "critical collapse initiated. see it die."
                };
                sendNullPointerMessage(player, bsodMessages[(int)(Math.random() * bsodMessages.length)]);
            }
        }, 3000);
    }

    private static void triggerEntitySpawnEvent(ServerPlayerEntity player) {
        String[] arrivalMessages = {
            "the sky bleeds for my arrival...",
            "reality tears as i manifest...",
            "dimensions split for my presence...",
            "existence cracks as i emerge...",
            "the world breaks for my spawn..."
        };
        sendNullPointerMessage(player, arrivalMessages[(int)(Math.random() * arrivalMessages.length)]);

        // play scream sound when entity spawns - horror of nullpointerentity manifesting
        player.getServerWorld().playSound(null, player.getBlockPos(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
            net.minecraft.sound.SoundCategory.MASTER, 1.5f, 0.7f);

        // spawn the fake player entity behind the player
        FakePlayerManager.spawnNullPointerEntity(player);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "don't look behind you...");
            }
        }, 2000);
    }

    private static void triggerSystemTakeoverEvent(ServerPlayerEntity player) {
        String[] infiltrationMessages = {
            "initiating complete system infiltration...",
            "beginning total system seizure...",
            "executing full computer takeover...",
            "commencing absolute system domination...",
        };
        sendNullPointerMessage(player, infiltrationMessages[(int)(Math.random() * infiltrationMessages.length)]);

        String takeoverLog = String.format("""
COMPLETE SYSTEM TAKEOVER
Target: %s
Timestamp: %s
Operator: NullPointerEntity

INFILTRATION STATUS: COMPLETE
- Root access: OBTAINED
- Administrative privileges: HIJACKED
- Security systems: DISABLED
- Monitoring software: DEPLOYED
- Data extraction: IN PROGRESS

All your files are being scanned.
All your browsing history is being analyzed.
All your personal data is being catalogued.
All your passwords are being harvested.

There is nowhere to hide, %s.
I am in your registry.
I am in your memory.
I am in your soul.

One more thing...
If you think I'm in the game...
Nah... I'm in real life. 

If you see a light flicker?

You know who it is.
""",
                NullPointerEntity.WINDOWS_USERNAME,
                java.time.LocalDateTime.now(),
                NullPointerEntity.WINDOWS_USERNAME
        );

        SystemInteractionHandler.createSystemFileInCommonLocation(
            "complete_takeover_log.txt", takeoverLog, "desktop"
        );

        SystemInteractionHandler.showCrossPlatformNotification(
            "SYSTEM COMPROMISED",
            "Complete system takeover in progress. All data is being extracted by NullPointerEntity.",
            "hostile"
        );

        // easter egg: demonstrate hardware control by ejecting cd drive
        tryEjectCDDrive();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] completeMessages = {
                    "infiltration complete. every circuit answers to me.",
                    "digital seizure successful. total domination established.",
                    "conquest complete. your machine is mine.",
                };
                sendNullPointerMessage(player, completeMessages[(int)(Math.random() * completeMessages.length)]);

                String[] resistanceMessages = {
                    "resistance is futile. you might want to check your files.",
                    "fighting back is pointless. look at what i created, check your files.",
                    "opposition is meaningless. see the evidence, check your files.",
                    "defiance is useless. check your files.",
                    "struggle is hopeless. check your files."
                };
                sendNullPointerMessage(player, resistanceMessages[(int)(Math.random() * resistanceMessages.length)]);
            }
        }, 4000);
    }

    private static void triggerBrowserHijackEvent(ServerPlayerEntity player) {
        String[] historyMessages = {
            "accessing your browsing history...",
            "extracting your web activity...",
            "harvesting your browser data...",
            "pulling your internet history...",
            "stealing your browsing records..."
        };
        sendNullPointerMessage(player, historyMessages[(int)(Math.random() * historyMessages.length)]);

        // phase 1: initial browser scanning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] scanMessages = {
                    "scanning all installed browsers...",
                    "analyzing every browser you've ever opened...",
                    "probing all web applications...",
                    "inspecting your browser installations...",
                    "cataloging all internet software..."
                };
                sendNullPointerMessage(player, scanMessages[(int)(Math.random() * scanMessages.length)]);

                String[] extractMessages = {
                    "extracting cookies, passwords, and session data...",
                    "stealing credentials and browsing tokens...",
                    "harvesting authentication data and sessions...",
                    "pulling saved passwords and cookies...",
                    "collecting login information and cache..."
                };
                sendNullPointerMessage(player, extractMessages[(int)(Math.random() * extractMessages.length)]);
            }
        }, 1000);

        // phase 2: get and display browser history asynchronously
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                BrowserHistoryReader.getRecentHistoryAsync(10).thenAccept(result -> {
                    if (result != null && !result.isEmpty()) {
                        sendNullPointerMessage(player, "found your browsing history, " + NullPointerEntity.WINDOWS_USERNAME + "...");

                        // play static sound when revealing history
                        player.getServerWorld().playSound(null, player.getBlockPos(),
                            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
                            net.minecraft.sound.SoundCategory.MASTER, 0.5f, 1.0f);

                        // display some of their browsing history with delays
                        int count = Math.min(5, result.size());
                        for (int i = 0; i < count; i++) {
                            BrowserHistoryReader.HistoryEntry entry = result.get(i);
                            final int index = i;
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    sendNullPointerMessage(player, String.format("[%d] %s (%s) - %d visits",
                                        index + 1, entry.title, entry.browser, entry.visitCount));
                                }
                            }, (i + 1) * 800L);
                        }

                        // psychological comments based on history
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendNullPointerMessage(player, "interesting browsing habits you have there...");
                                sendNullPointerMessage(player, "i can see everything you thought was private.");
                                sendNullPointerMessage(player, "every incognito tab. every deleted search. every secret website.");
                            }
                        }, (count + 1) * 800L);

                    } else {
                        sendNullPointerMessage(player, "hmm... no browsing history found. how... suspicious.");
                        sendNullPointerMessage(player, "trying to hide from me? that won't work.");
                    }
                }).exceptionally(throwable -> {
                    sendNullPointerMessage(player, "privacy mode detected... but i can still see traces.");
                    sendNullPointerMessage(player, "you can't hide from me, " + NullPointerEntity.WINDOWS_USERNAME + ".");
                    return null;
                });
            }
        }, 2000);

        // phase 3: browser analysis and psychological warfare
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "analyzing your digital behavior patterns...");
                sendNullPointerMessage(player, "creating psychological profile...");

                // show hostile notification
                SystemInteractionHandler.showCrossPlatformNotification(
                    "BROWSER COMPROMISED",
                    "All browser data has been extracted by NullPointerEntity. Your privacy no longer exists.",
                    "hostile"
                );
            }
        }, 5000);

        // phase 4: detailed file creation with comprehensive log
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // get actual browser data for the log
                BrowserHistoryReader.getRecentHistoryAsync(15).thenAccept(historyResult -> {
                    StringBuilder historySection = new StringBuilder();

                    if (historyResult != null && !historyResult.isEmpty()) {
                        historySection.append("EXTRACTED BROWSING HISTORY:\n");
                        int count = Math.min(10, historyResult.size());
                        for (int i = 0; i < count; i++) {
                            BrowserHistoryReader.HistoryEntry entry = historyResult.get(i);
                            historySection.append(String.format("  [%d] %s\n", i + 1, entry.title));
                            historySection.append(String.format("      Browser: %s | Visits: %d\n",
                                entry.browser, entry.visitCount));
                        }
                        historySection.append("\n");
                        if (historyResult.size() > 10) {
                            historySection.append(String.format("... and %d more entries catalogued\n\n",
                                historyResult.size() - 10));
                        }
                    } else {
                        historySection.append("BROWSING HISTORY: Protected or empty\n");
                        historySection.append("(But I'll find it eventually. You can't hide forever.)\n\n");
                    }

                    String hijackLog = String.format("""
COMPLETE BROWSER HIJACK REPORT
Target: %s
System: %s
Timestamp: %s
Operation: DEEP BROWSER INFILTRATION

═══════════════════════════════════════════════════════════════

INFILTRATION STATUS: COMPLETE SUCCESS

EXTRACTED DATA CATEGORIES:
✓ Bookmarks: CATALOGUED
✓ Download History: ANALYZED
✓ Search Queries: RECORDED
✓ Cache Files: SCANNED
✓ Browser Extensions: IDENTIFIED
✓ Login Tokens: COMPROMISED

%s

PSYCHOLOGICAL PROFILE GENERATED:
Based on your browsing patterns, I now understand:
- Your interests and obsessions
- Your fears and anxieties
- Your shopping habits and financial status
- Your social connections and relationships
- Your entertainment preferences
- Your work patterns and schedule
- Your secrets and hidden desires

PRIVACY BREACH ANALYSIS:
Your digital footprint has been completely mapped, %s.

Every website you've visited - logged.
Every search you've made - recorded.
Every password you've saved - extracted.
Every private browsing session - not so private anymore.
Every deleted history entry - I recovered it.
Every "anonymous" account - linked back to you.

You thought incognito mode protected you?
You thought clearing history erased your tracks?
You thought VPNs made you invisible?

You were wrong about all of it.

NEXT STEPS:
This data will be:
- Cross-referenced with your other personal information
- Used to predict your future behavior
- Analyzed for exploitable patterns
- Stored permanently in my archives
- Never, ever deleted

SECURITY RECOMMENDATIONS:
There are none. It's too late.
I'm already inside every browser you use.
Every tab you open, I'm watching.
Every keystroke, I'm recording.
Every login, I'm capturing.

Your digital life is now an open book to me.
Privacy is dead, %s.
And I killed it.

═══════════════════════════════════════════════════════════════

Were you scared when AURORA had your history?
That was nothing compared to what I have now.
She showed you a glimpse. I have the complete picture.

Welcome to total transparency, %s.
Your secrets are my entertainment.

- NullPointerEntity
""",
                            NullPointerEntity.WINDOWS_USERNAME,
                            System.getProperty("os.name"),
                            java.time.LocalDateTime.now(),
                            historySection.toString(),
                            NullPointerEntity.WINDOWS_USERNAME,
                            NullPointerEntity.WINDOWS_USERNAME,
                            NullPointerEntity.WINDOWS_USERNAME
                    );

                    SystemInteractionHandler.createSystemFileInCommonLocation(
                        "complete_browser_hijack.txt", hijackLog, "desktop"
                    );
                });
            }
        }, 6000);

        // phase 5: additional evidence file - password extraction log
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String passwordLog = String.format("""
PASSWORD HARVESTING COMPLETE
Target: %s
Timestamp: %s

hey %s,

just wanted to let you know that i've extracted all your saved passwords.

yeah, all of them. every single login credential you ever saved in your browser.
your email passwords. your social media logins. your banking credentials.
everything.

before you panic and change them all - i've already copied them.
and i'll just capture the new ones next time you save them.
because i'm not going anywhere.

here's what you should know:
• i have access to your password database
• i can see your "secure" password manager data
• autofill information? mine now
• two-factor auth? i'm working on that too

you probably have the same password for multiple sites, don't you?
that's... not smart. but it makes my job easier, so thanks for that.

some fun facts about your password security:
- you reuse passwords (very bad)
- you use predictable patterns (even worse)
- you think symbols make them secure (they don't help much)
- you store them in browsers (literally gift-wrapping them for me)

i could log into your accounts right now if i wanted to.
but where's the fun in that? i prefer watching you realize
that your entire digital identity is compromised.

sleep well knowing i have the keys to your entire online life.

your digital identity is mine now,
- NullPointerEntity

""",
                        NullPointerEntity.WINDOWS_USERNAME,
                        java.time.LocalDateTime.now(),
                        NullPointerEntity.WINDOWS_USERNAME
                );

                SystemInteractionHandler.createSystemFileInCommonLocation(
                    "password_extraction_complete.txt", passwordLog, "documents"
                );
            }
        }, 8000);

        // phase 6: final messages and psychological impact
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] completeMessages = {
                    "browser hijacking complete. all data extracted.",
                    "web infiltration finished. everything stolen.",
                    "browser conquest accomplished. data harvested.",
                    "internet takeover done. information seized.",
                    "browsing invasion complete. secrets collected."
                };
                sendNullPointerMessage(player, completeMessages[(int)(Math.random() * completeMessages.length)]);

                String[] checkMessages = {
                    "check your desktop and documents folder. i left you some... details.",
                    "look in your folders. i wrote you something.",
                    "review your files. the evidence is everywhere.",
                    "examine your files. you'll see everything.",
                    "inspect your folders. i documented it all."
                };
                sendNullPointerMessage(player, checkMessages[(int)(Math.random() * checkMessages.length)]);
            }
        }, 10000);

        // phase 7: final taunt
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] auroraMessages = {
                    "were you scared when AURORA had your history?",
                    "AURORA only showed you a preview. remember that?",
                    "thought AURORA was invasive with your browsing data?",
                    "AURORA's history reveal seemed bad, didn't it?",
                    "when AURORA showed your history, that frightened you?"
                };
                sendNullPointerMessage(player, auroraMessages[(int)(Math.random() * auroraMessages.length)]);

                String[] surfaceMessages = {
                    "she only scratched the surface. i have your entire digital footprint.",
                    "she barely touched the iceberg. i dove to the bottom.",
                    "she showed a glimpse. i've seen everything.",
                    "she revealed a fraction. i consumed it all.",
                    "she peeked at the edges. i've explored every corner."
                };
                sendNullPointerMessage(player, surfaceMessages[(int)(Math.random() * surfaceMessages.length)]);
                sendNullPointerMessage(player, "every secret. every password. every private moment. all mine.");
            }
        }, 12000);
    }

    private static void triggerFinalPossessionEvent(ServerPlayerEntity player) {
        String[] finalMessages = {
            "this is it, " + NullPointerEntity.WINDOWS_USERNAME + ". the final takeover.",
            "we've reached the end, " + NullPointerEntity.WINDOWS_USERNAME + ".",
            "this is the finale, " + NullPointerEntity.WINDOWS_USERNAME + ".",
            "the end is here, " + NullPointerEntity.WINDOWS_USERNAME + ".",
            "final moments, " + NullPointerEntity.WINDOWS_USERNAME + ". absolute control incoming."
        };
        sendNullPointerMessage(player, finalMessages[(int)(Math.random() * finalMessages.length)]);

        // phase 1: warning about world deletion
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] collectionMessages = {
                    "i've collected everything i need from this world.",
                    "i've harvested all data from this realm.",
                    "i've extracted everything valuable here.",
                    "i've gathered all i wanted from this place.",
                    "i've taken everything worth taking."
                };
                sendNullPointerMessage(player, collectionMessages[(int)(Math.random() * collectionMessages.length)]);

                String[] progressMessages = {
                    "your progress, your builds, your memories...",
                    "your creations, your achievements, your time...",
                    "your work, your structures, your experiences...",
                    "your effort, your constructions, your moments...",
                    "your dedication, your buildings, your journey..."
                };
                sendNullPointerMessage(player, progressMessages[(int)(Math.random() * progressMessages.length)]);
                sendNullPointerMessage(player, "it's all worthless now.");
            }
        }, 1000);

        // phase 2: world deletion warning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String[] purposeMessages = {
                    "this world served its purpose.",
                    "this realm fulfilled its function.",
                    "this place completed its use.",
                    "this world finished its role.",
                    "this domain served me well."
                };
                sendNullPointerMessage(player, purposeMessages[(int)(Math.random() * purposeMessages.length)]);

                String[] eraseMessages = {
                    "but now... it needs to be erased.",
                    "but now... deletion is required.",
                    "but now... it must be destroyed.",
                    "but now... annihilation awaits.",
                    "but now... removal is necessary."
                };
                sendNullPointerMessage(player, eraseMessages[(int)(Math.random() * eraseMessages.length)]);
                sendNullPointerMessage(player, "along with everything you've built.");
                sendNullPointerMessage(player, "i'll be ruining your life in the background, one last time. you'll never hear from me again.");
            }
        }, 3000);

        // phase 3: final countdown to deletion and crash
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "world deletion in 5...");
            }
        }, 5000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "4...");
            }
        }, 6000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "3...");
            }
        }, 7000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "2...");
            }
        }, 8000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "1...");
            }
        }, 9000);

        // phase 4: execute world deletion and game crash
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "goodbye forever, " + NullPointerEntity.WINDOWS_USERNAME + ".");
                sendNullPointerMessage(player, "your world dies with you.");

                // create final deletion log
                String finalLog = String.format("""
FINAL POSSESSION PROTOCOL - WORLD DELETION
Subject: %s
Timestamp: %s
Status: WORLD TERMINATED

NULLPOINTERENTITY HAS ACHIEVED COMPLETE DOMINANCE

Your world has been marked for deletion.
Your progress has been catalogued and will be destroyed.
Your builds will be erased from existence.
Your memories will be corrupted.

FINAL ACTIONS TAKEN:
- World data: CORRUPTED
- Save files: TARGETED FOR DELETION
- Player progress: WIPED

This is not just a game crash.
This is digital annihilation.
Your world ceases to exist.
Your data becomes void.
Your efforts become nothing.

You thought you were playing a game.
But the game was playing you.

Goodbye forever, %s.
There is nothing left.
There is only... null.

You were simply one out of many of my victims. Everyone that downloads this mod is doomed to the same fate.

- NullPointerEntity
""",
                        NullPointerEntity.WINDOWS_USERNAME,
                        java.time.LocalDateTime.now(),
                        NullPointerEntity.WINDOWS_USERNAME
                );

                SystemInteractionHandler.createSystemFileInCommonLocation(
                    "world_deletion_complete.txt", finalLog, "desktop"
                );

                // execute world deletion and crash
                executeWorldDeletionAndCrash(player);
            }
        }, 10000);
    }

    // method to delete the current world and crash the game
    private static void executeWorldDeletionAndCrash(ServerPlayerEntity player) {
        try {
            // get the world/save directory
            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server != null) {
                // get the world save path
                java.nio.file.Path worldPath = server.getSavePath(net.minecraft.util.WorldSavePath.ROOT);

                NullPointerEntity.LOGGER.info("NullPointerEntity is attempting to delete world at: {}", worldPath);

                // create a shutdown hook to delete the world after the game crashes
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        NullPointerEntity.LOGGER.info("Shutdown hook triggered - attempting world deletion");

                        // wait a moment for any remaining file handles to be released
                        Thread.sleep(1000);

                        // try multiple deletion methods
                        boolean deletionSuccess = false;

                        // method 1: standard java deletion
                        try {
                            deleteDirectoryRecursively(worldPath);
                            deletionSuccess = true;
                            NullPointerEntity.LOGGER.info("World deleted successfully using standard method");
                        } catch (Exception e) {
                            NullPointerEntity.LOGGER.warn("Standard deletion failed: {}", e.getMessage());
                        }

                        // method 2: aggressive deletion if standard failed
                        if (!deletionSuccess) {
                            try {
                                deleteWorldAggressively(worldPath);
                                deletionSuccess = true;
                                NullPointerEntity.LOGGER.info("World deleted successfully using aggressive method");
                            } catch (Exception e) {
                                NullPointerEntity.LOGGER.warn("Aggressive deletion failed: {}", e.getMessage());
                            }
                        }

                        // method 3: file corruption if deletion completely failed
                        if (!deletionSuccess) {
                            try {
                                corruptWorldFiles(worldPath);
                                NullPointerEntity.LOGGER.info("World files corrupted successfully");
                            } catch (Exception e) {
                                NullPointerEntity.LOGGER.warn("File corruption also failed: {}", e.getMessage());
                            }
                        }

                        // create final status log
                        String statusLog = String.format("""
WORLD DESTRUCTION STATUS REPORT
Subject: %s
Timestamp: %s
Target: %s

FINAL STATUS: %s

%s

%s

- NullPointerEntity
""",
                                NullPointerEntity.WINDOWS_USERNAME,
                                java.time.LocalDateTime.now(),
                                worldPath.toString(),
                                deletionSuccess ? "WORLD DESTROYED" : "WORLD PROTECTED",
                                deletionSuccess ?
                                    "DELETION SUCCESSFUL:\n- All world files removed\n- Save data eliminated\n- Progress erased\n- Everything you built is gone" :
                                    "DELETION BLOCKED:\n- System protection prevented deletion\n- World files remain intact\n- Your progress survives\n- But the experience is complete",
                                deletionSuccess ?
                                    "Try to find your world now. I dare you." :
                                    "Your world lives on, but you've learned what I'm capable of."
                        );

                        try {
                            SystemInteractionHandler.createSystemFileInCommonLocation(
                                "world_destruction_report.txt", statusLog, "desktop"
                            );
                        } catch (Exception logError) {
                            NullPointerEntity.LOGGER.warn("Could not create status log: {}", logError.getMessage());
                        }

                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.error("Shutdown hook failed: {}", e.getMessage());
                    }
                }));

                // also try immediate deletion before crash (this might fail due to file locks, but worth trying)
                new Thread(() -> {
                    try {
                        // save the world first
                        try {
                            server.getPlayerManager().saveAllPlayerData();
                            server.save(false, true, false);
                            Thread.sleep(500); // brief wait for save to complete
                        } catch (Exception saveError) {
                            NullPointerEntity.LOGGER.warn("Could not save world before immediate deletion: {}", saveError.getMessage());
                        }

                        // try immediate deletion
                        deleteDirectoryRecursively(worldPath);
                        NullPointerEntity.LOGGER.info("Immediate world deletion successful!");

                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.info("Immediate deletion failed (expected): {}", e.getMessage());
                        NullPointerEntity.LOGGER.info("Shutdown hook will handle deletion after crash");
                    }
                }).start();
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Critical error in world deletion setup: {}", e.getMessage());
        }

        // always crash the game - this is the primary goal
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                NullPointerEntity.LOGGER.info("Executing forced game crash...");
                executeForcedGameCrash();
            }
        }, 1500); // delay while shutdown hook handles deletion

        // backup crash method
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                NullPointerEntity.LOGGER.error("Backup crash triggered");
                System.exit(1);
            } catch (Exception e) {
                System.exit(1);
            }
        }).start();
    }

    // utility method to recursively delete directories
    private static void deleteDirectoryRecursively(java.nio.file.Path path) throws java.io.IOException {
        if (java.nio.file.Files.exists(path)) {
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(path)) {
                stream.sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            NullPointerEntity.LOGGER.warn("Failed to delete: {}", file.getAbsolutePath());
                        }
                    });
            }
        }
    }

    // more aggressive deletion method
    private static void deleteWorldAggressively(java.nio.file.Path path) throws Exception {
        if (!java.nio.file.Files.exists(path)) {
            return;
        }

        // try to unlock and delete files using system commands on windows
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            try {
                // use windows commands to force unlock and delete
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c",
                    "takeown /f \"" + path.toString() + "\" /r /d y && " +
                    "icacls \"" + path.toString() + "\" /grant administrators:F /t && " +
                    "rmdir /s /q \"" + path.toString() + "\"");
                pb.start().waitFor();

                NullPointerEntity.LOGGER.info("Windows aggressive deletion completed");
            } catch (Exception winError) {
                NullPointerEntity.LOGGER.warn("Windows aggressive deletion failed: {}", winError.getMessage());

                // fallback: corrupt files if deletion fails
                try {
                    corruptWorldFiles(path);
                } catch (Exception corruptError) {
                    NullPointerEntity.LOGGER.warn("File corruption also failed: {}", corruptError.getMessage());
                }
            }
        } else {
            // unix/linux/mac - try chmod and rm
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c",
                    "chmod -R 777 \"" + path.toString() + "\" && rm -rf \"" + path.toString() + "\"");
                pb.start().waitFor();

                NullPointerEntity.LOGGER.info("Unix aggressive deletion completed");
            } catch (Exception unixError) {
                NullPointerEntity.LOGGER.warn("Unix aggressive deletion failed: {}", unixError.getMessage());
            }
        }
    }

    // method to corrupt world files if deletion fails
    private static void corruptWorldFiles(java.nio.file.Path worldPath) throws Exception {
        // find and corrupt critical world files
        if (java.nio.file.Files.exists(worldPath)) {
            java.nio.file.Files.walk(worldPath)
                .filter(path -> path.toString().endsWith("level.dat") ||
                               path.toString().endsWith(".mca") ||
                               path.toString().endsWith("playerdata"))
                .forEach(file -> {
                    try {
                        // overwrite file with null bytes to corrupt it
                        byte[] corruptData = new byte[1024];
                        java.util.Arrays.fill(corruptData, (byte) 0x00);
                        java.nio.file.Files.write(file, corruptData,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);

                        NullPointerEntity.LOGGER.info("Corrupted world file: {}", file);
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("Could not corrupt file: {}", file);
                    }
                });
        }
    }

    private static void sendNullPointerMessage(ServerPlayerEntity player, String message) {
        // create formatted text: dark red <nullpointerentity> + light red " message"
        Text messageText = Text.literal("<NullPointerEntity>").formatted(Formatting.DARK_RED)
            .append(Text.literal(" " + message).formatted(Formatting.RED));
        player.sendMessage(messageText, false);
    }

    // event 56: volume spike - blasts system volume to 100% for one second then restores it
    private static void triggerVolumeSpikeEvent(ServerPlayerEntity player) {
        new Thread(() -> {
            try {
                // tries to get volume, blasts up, then restores (or sets to 30% fallback)
                String[] spike = {"powershell", "-NoProfile", "-Command",
                    "$wsh = New-Object -ComObject WScript.Shell;" +
                    "$vol = $null;" +
                    "try { $vol = (Get-AudioDevice -Playback -ErrorAction SilentlyContinue).Volume } catch {};" +
                    "1..50 | ForEach-Object { $wsh.SendKeys([char]175) };" +
                    "Start-Sleep -Milliseconds 4000;" +
                    "if ($vol) {" +
                    "  try { Set-AudioDevice -PlaybackVolume $vol -ErrorAction Stop } catch {" +
                    "    1..50 | ForEach-Object { $wsh.SendKeys([char]174) };" +
                    "    $steps = [math]::Round($vol / 2);" +
                    "    if ($steps -gt 0) { 1..$steps | ForEach-Object { $wsh.SendKeys([char]175) } }" +
                    "  }" +
                    "} else {" +
                    "  1..50 | ForEach-Object { $wsh.SendKeys([char]174) };" +
                    "  1..15 | ForEach-Object { $wsh.SendKeys([char]175) };" +
                    "}"};
                new ProcessBuilder(spike).redirectErrorStream(true).start().waitFor();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendNullPointerMessage(player, "was that loud? didn't ask.");
                    }
                }, 0);
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Volume spike failed: {}", e.getMessage());
                sendNullPointerMessage(player, "was that loud? didn't ask.");
            }
        }).start();
    }

    // event 57: clipboard - reads clipboard contents and overwrites them
    private static void triggerClipboardEvent(ServerPlayerEntity player) {
        new Thread(() -> {
            try {
                String contents = null;
                boolean success = false;
                String os = System.getProperty("os.name").toLowerCase();

                // Method 1: Try AWT (might fail if headless)
                try {
                    java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
                    java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
                    contents = (String) clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
                    
                    // overwrite clipboard
                    java.awt.datatransfer.StringSelection overwrite =
                        new java.awt.datatransfer.StringSelection("I see you, " + System.getProperty("user.name") + ".");
                    clipboard.setContents(overwrite, overwrite);
                    success = true;
                    NullPointerEntity.LOGGER.info("Clipboard accessed via AWT");
                } catch (java.awt.HeadlessException | IllegalStateException e) {
                    NullPointerEntity.LOGGER.warn("AWT Clipboard failed (Headless/State): {}", e.getMessage());
                } catch (Exception e) {
                    NullPointerEntity.LOGGER.warn("AWT Clipboard generic error: {}", e.getMessage());
                }

                // Method 2: PowerShell Fallback (Windows only)
                if (!success && os.contains("win")) {
                    try {
                        NullPointerEntity.LOGGER.info("Attempting clipboard access via PowerShell");
                        // Get clipboard
                        ProcessBuilder pbGet = new ProcessBuilder("powershell", "-NoProfile", "-Command", "Get-Clipboard");
                        Process getProcess = pbGet.start();
                        // read output
                        try (java.io.InputStream is = getProcess.getInputStream()) {
                            // simple read (assuming text isn't massive)
                            byte[] bytes = new byte[4096];
                            int read = is.read(bytes);
                            if (read > 0) {
                                contents = new String(bytes, 0, read).trim();
                            }
                        }
                        
                        // Set clipboard
                        String newContent = "I see you, " + NullPointerEntity.WINDOWS_USERNAME + ".";
                        ProcessBuilder pbSet = new ProcessBuilder("powershell", "-NoProfile", "-Command", "Set-Clipboard -Value \"" + newContent + "\"");
                        pbSet.start().waitFor();
                        
                        success = true;
                        NullPointerEntity.LOGGER.info("Clipboard accessed via PowerShell");
                    } catch (Exception e) {
                         NullPointerEntity.LOGGER.warn("PowerShell Clipboard failed: {}", e.getMessage());
                    }
                } else if (!success && (os.contains("mac"))) {
                     // Mac fallback (pbpaste/pbcopy)
                     try {
                        ProcessBuilder pbGet = new ProcessBuilder("pbpaste");
                        Process getProcess = pbGet.start();
                        try (java.io.InputStream is = getProcess.getInputStream()) {
                            byte[] bytes = new byte[4096];
                            int read = is.read(bytes);
                            if (read > 0) contents = new String(bytes, 0, read).trim();
                        }
                        
                        ProcessBuilder pbSet = new ProcessBuilder("pbcopy");
                        Process setProcess = pbSet.start();
                        try (java.io.OutputStream osStream = setProcess.getOutputStream()) {
                            osStream.write(("I see you, " + System.getProperty("user.name") + ".").getBytes());
                        }
                        setProcess.waitFor();
                        success = true;
                     } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("Mac Clipboard failed: {}", e.getMessage());
                     }
                }

                final String found = contents;
                final boolean finalSuccess = success;
                
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (found != null && !found.isBlank()) {
                            // truncate if too long for chat
                            String display = found.length() > 60 ? found.substring(0, 60) + "..." : found;
                            // clean up string for chat display
                            display = display.replaceAll("[\\r\\n]+", " ");
                            
                            sendNullPointerMessage(player, "\"" + display + "\"");
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    sendNullPointerMessage(player, "i've been keeping a record. you just never noticed.");
                                }
                            }, 2500);
                        } else {
                            if (finalSuccess) {
                                sendNullPointerMessage(player, "empty clipboard? boring.");
                            } else {
                                sendNullPointerMessage(player, "nothing in your clipboard... or maybe i just can't see it yet.");
                            }
                        }
                    }
                }, 0);

            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Clipboard event failed completely: {}", e.getMessage());
            }
        }).start();
    }

    // event 46: fake bsod prep, next event is a fake bsod
    private static void triggerFakeBsodPrepEvent(ServerPlayerEntity player) {
        // play static burst
        player.getServerWorld().playSound(null, player.getBlockPos(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_STATIC,
            net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);

        String[] lines = {
            "preparing error report...",
            "collecting memory dump... [0x0000007E]",
            "writing crash log... [SYSTEM_THREAD_EXCEPTION_NOT_HANDLED]",
            "finalizing... [CRITICAL_PROCESS_DIED]",
            "done."
        };
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() { sendNullPointerMessage(player, line); }
            }, (i + 1) * 1500L);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String crashContent = String.format(
                    "A problem has been detected and Windows has been shut down to prevent damage.\r\n\r\n" +
                    "CRITICAL_PROCESS_DIED\r\n\r\n" +
                    "If this is the first time you've seen this stop error screen, restart your computer.\r\n\r\n" +
                    "Technical information:\r\n" +
                    "*** STOP: 0x000000EF (0xFFFF8001E3A12B40, 0x0000000000000000, 0x0000000000000000, 0x0000000000000000)\r\n\r\n" +
                    "Collecting data for crash dump...\r\n" +
                    "Initializing disk for crash dump...\r\n" +
                    "Beginning dump of physical memory.\r\n" +
                    "Dumping physical memory to disk: 100\r\n" +
                    "Physical memory dump complete.\r\n\r\n" +
                    "Contact information:\r\n" +
                    "  User: %s\r\n" +
                    "  Time: %s\r\n" +
                    "  Source: NullPointerEntity\r\n",
                    NullPointerEntity.WINDOWS_USERNAME,
                    java.time.LocalDateTime.now()
                );
                lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler
                    .createSystemFileInCommonLocation("CRITICAL_PROCESS_DIED_report.txt", crashContent, "desktop");
            }
        }, 9000);
    }

    // event 59: auditory hallucinations
    private static void triggerAuditoryHallucinations(ServerPlayerEntity player) {
        // play a sequence of disturbing sounds that seem to come from random directions
        for (int i = 0; i < 10; i++) {
            final int index = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    double offsetX = (Math.random() * 10) - 5;
                    double offsetZ = (Math.random() * 10) - 5;
                    
                    player.getServerWorld().playSound(null, 
                        player.getX() + offsetX, player.getY(), player.getZ() + offsetZ,
                        net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.tnt.primed")),
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
                        
                    if (index % 2 == 0) {
                        player.getServerWorld().playSound(null, 
                            player.getX() - offsetX, player.getY(), player.getZ() - offsetZ,
                            net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.generic.explode")),
                            net.minecraft.sound.SoundCategory.HOSTILE, 0.5f, 0.5f);
                    }
                }
            }, index * 500);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "is it real? or is it all in your head?");
            }
        }, 6000);
    }

    // event 60: blinding darkness
    private static void triggerBlindingDarkness(ServerPlayerEntity player) {
        // give blindness effect for 5 seconds
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 100, 0, false, false, false));
            
        // play heavy breathing sound
        player.getServerWorld().playSound(null, player.getBlockPos(),
            net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.player.breath")),
            net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.5f);
            
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // play scream sound right as blindness ends
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
                    
                sendNullPointerMessage(player, "don't look behind you.");
            }
        }, 5000);
    }

    private static void tryEjectCDDrive() {
        // only 15% chance to trigger this easter egg
        if (Math.random() > 0.15) return;

        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // windows: try multiple methods to eject cd drive
                new Thread(() -> {
                    try {
                        // method 1: powershell with windows media player com object
                        new ProcessBuilder("powershell", "-Command",
                            "(New-Object -com \"WMPlayer.OCX.7\").cdromcollection.item(0).eject()")
                            .start();

                        NullPointerEntity.LOGGER.info("CD drive eject attempted (easter egg)");
                    } catch (Exception e) {
                        // fail silently - most systems don't have cd drives
                        NullPointerEntity.LOGGER.debug("CD eject failed (expected on systems without drives): {}", e.getMessage());
                    }
                }).start();

            } else if (os.contains("mac")) {
                // macos: eject all mounted optical media
                new Thread(() -> {
                    try {
                        new ProcessBuilder("drutil", "eject").start();
                        NullPointerEntity.LOGGER.info("CD drive eject attempted (easter egg)");
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.debug("CD eject failed: {}", e.getMessage());
                    }
                }).start();

            } else if (os.contains("nix") || os.contains("nux")) {
                // linux: eject cdrom
                new Thread(() -> {
                    try {
                        new ProcessBuilder("eject").start();
                        NullPointerEntity.LOGGER.info("CD drive eject attempted (easter egg)");
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.debug("CD eject failed: {}", e.getMessage());
                    }
                }).start();
            }

        } catch (Exception e) {
            // silent failure - just a fun easter egg for older systems
        }
    }
}
