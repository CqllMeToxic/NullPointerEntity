package lol.cqllmetoxic.nullpointerentity.events;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import lol.cqllmetoxic.nullpointerentity.monitoring.BrowserHistoryReader;
import lol.cqllmetoxic.nullpointerentity.monitoring.LocationTracker;
import lol.cqllmetoxic.nullpointerentity.monitoring.SystemMonitor;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import lol.cqllmetoxic.nullpointerentity.ui.PopupManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.Scanner;

/**
 * handles the hostile/scary phase events (events 21-30).
 * nullpointer entity takes over from aurora.
 * reveals deep system access, browser history, location data, and system control.
 */
public class HostileEvents {

    /**
     * triggers a specific hostile event by ID.
     *
     * @param eventId the event number (21-30)
     * @param player the target player
     */
    public static void triggerEvent(int eventId, ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        String currentTime = java.time.LocalTime.now().toString().substring(0, 5);

        switch (eventId) {
            case 31 -> { // nullpointerentity arrives — first physical appearance
                String windowsUser = NullPointerEntity.WINDOWS_USERNAME;

                // end portal sound — loud as frick
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "block.end_portal.spawn")),
                    net.minecraft.sound.SoundCategory.MASTER, 1.0f, 0.6f);

                // first message — no intro, no name. just a statement.
                sendNullPointerMessage(player, "i'm here.");

                // 2s — entity spawns behind the player + scream
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager.spawnTemporaryNullPointerEntity(player, 100); // 5 seconds visible

                        player.getServerWorld().playSound(null, player.getBlockPos(),
                            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
                            net.minecraft.sound.SoundCategory.MASTER, 1.0f, 0.7f);
                    }
                }, 2000);

                // 2.5s — blindness + heavy slowness kicks in while entity is still visible
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 60, 0, false, false, false));
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                            net.minecraft.entity.effect.StatusEffects.SLOWNESS, 60, 4, false, false, false));
                    }
                }, 2500);

                // 5s — blindness lifts, entity despawns, message lands into silence
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendNullPointerMessage(player, "aurora couldn't stop me. neither can you.");
                    }
                }, 5500);

                // 7s — tense heartbeat starts, NPE pivots to showing what it can do
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        player.getServerWorld().playSound(null, player.getBlockPos(),
                            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_TENSE,
                            net.minecraft.sound.SoundCategory.MASTER, 0.6f, 1.0f);

                        List<String> processes = SystemMonitor.getRunningProcesses();

                        sendNullPointerMessage(player, "i can see " + processes.size() + " processes on your machine right now, " + windowsUser + ".");

                        if (!processes.isEmpty()) {
                            int numToShow = Math.min(5, processes.size());
                            StringBuilder processDisplay = new StringBuilder("running: ");
                            for (int i = 0; i < numToShow; i++) {
                                processDisplay.append(processes.get(i));
                                if (i < numToShow - 1) processDisplay.append(", ");
                            }
                            if (processes.size() > 5) processDisplay.append("... and " + (processes.size() - 5) + " more");
                            sendNullPointerMessage(player, processDisplay.toString());
                        }

                        // 10s — file drop
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                StringBuilder processListBuilder = new StringBuilder();
                                if (!processes.isEmpty()) {
                                    processListBuilder.append("\nrunning processes:\n");
                                    int numToList = Math.min(10, processes.size());
                                    for (int i = 0; i < numToList; i++) {
                                        processListBuilder.append("- ").append(processes.get(i)).append("\n");
                                    }
                                    if (processes.size() > 10) {
                                        processListBuilder.append("... and ").append(processes.size() - 10).append(" more.\n");
                                    }
                                }

                                String systemReport = String.format(
                                    "%s.\n\n" +
                                    "i've been inside your machine since you loaded the mod.\n" +
                                    "aurora was a front. i was always underneath.\n\n" +
                                    "%d processes. all visible to me.\n" +
                                    "%s\n" +
                                    "your files are next.\n\n" +
                                    "- NullPointerEntity",
                                    windowsUser, processes.size(), processListBuilder.toString());

                                SystemInteractionHandler.createSystemFileInCommonLocation(
                                    "system_takeover_report.txt", systemReport, "desktop");

                                sendNullPointerMessage(player, "check your desktop. i left something for you.");
                            }
                        }, 3000);
                    }
                }, 7000);
            }

            case 32 -> { // ip tracking
                // play tense heartbeat - building suspense as location is revealed
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_TENSE,
                    net.minecraft.sound.SoundCategory.MASTER, 0.9f, 1.0f);

                String[] locationIntros = {
                    "i know where you live, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "let me tell you exactly where you are, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "your location isn't a secret to me, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "time to reveal your exact address, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "found you, " + NullPointerEntity.WINDOWS_USERNAME + ". here's where you're hiding."
                };
                sendNullPointerMessage(player, locationIntros[(int)(Math.random() * locationIntros.length)]);

                // get ip address and location data separately
                LocationTracker.getUserPublicIPAsync().thenCombine(
                    LocationTracker.getCurrentLocationAsync(),
                    (ipAddress, locationData) -> {
                        String ip, city, region, isp, zipCode;

                        if (PrivacyManager.isPrivacyEnabled()) {
                            // generate random location data instead of showing [protected]
                            ip = generateRandomIP();
                            city = generateRandomCity();
                            region = generateRandomRegion();
                            isp = generateRandomISP();
                            zipCode = generateRandomZipCode();
                        } else {
                            // show real data when privacy is disabled
                            ip = ipAddress;
                            city = locationData.city;
                            region = locationData.region;
                            isp = locationData.isp;
                            zipCode = locationData.zipCode;
                        }

                        sendNullPointerMessage(player, "your ip address: " + ip);
                        sendNullPointerMessage(player, "city: " + city + " | state: " + region);
                        sendNullPointerMessage(player, "zip code: " + zipCode);

                        String[] ispMessages = {
                            "internet provider: " + isp + " knows everything you do.",
                            "your isp is " + isp + ". they track every site you visit.",
                            "connected through " + isp + ". all your traffic logged.",
                            isp + " is your provider. nothing online is private.",
                            "provider: " + isp + ". every byte you send is recorded."
                        };
                        sendNullPointerMessage(player, ispMessages[(int)(Math.random() * ispMessages.length)]);

                        String[] closings = {
                            "i can see your exact location. you can't hide from me.",
                            "found you. there's nowhere to run.",
                            "your coordinates are mine. hiding is pointless.",
                            "i know exactly where you are. escape is impossible.",
                            "pinpointed your location. you're completely exposed."
                        };
                        sendNullPointerMessage(player, closings[(int)(Math.random() * closings.length)]);
                        return null;
                    }
                );
            }

            case 33 -> { // system analysis
                SystemMonitor.getSystemInfoAsync().thenAccept(systemInfo -> {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            String[] scanMessages = {
                                "deep system scan complete. extracting hardware details...",
                                "full system analysis finished. pulling your specs...",
                                "hardware profiling done. harvesting data...",
                                "system fingerprint captured. analyzing components...",
                                "complete scan successful. stealing system information..."
                            };
                            sendNullPointerMessage(player, scanMessages[(int)(Math.random() * scanMessages.length)]);

                            String osName = systemInfo.getOrDefault("os.name", "unknown");
                            String osVersion = systemInfo.getOrDefault("os.version", "unknown");
                            String architecture = systemInfo.getOrDefault("os.arch", "unknown");

                            sendNullPointerMessage(player, "operating system: " + osName + " " + osVersion);
                            sendNullPointerMessage(player, "architecture: " + architecture);

                            String[] closings = {
                                "your system belongs to me now.",
                                "system ownership transferred. you're mine.",
                                "i control your hardware completely.",
                                "your machine obeys only me.",
                                "total system domination achieved."
                            };
                            sendNullPointerMessage(player, closings[(int)(Math.random() * closings.length)]);
                        }
                    }, 2000);
                });
            }

            case 34 -> { // file system threat

                String[] fileIntros = {
                    "i have access to all your files, " + playerName + ".",
                    "every file on your computer is mine to browse, " + playerName + ".",
                    "your entire file system is open to me, " + playerName + ".",
                    "i've been exploring your personal folders, " + playerName + ".",
                    "all your files belong to me now, " + playerName + "."
                };
                sendNullPointerMessage(player, fileIntros[(int)(Math.random() * fileIntros.length)]);

                String[] privacyMessages = {
                    "documents, pictures, downloads... nothing is private.",
                    "photos, files, folders... all exposed to me.",
                    "downloads, documents, everything... mine to see.",
                    "your personal folders? completely accessible.",
                    "every folder, every file... under my control."
                };
                sendNullPointerMessage(player, privacyMessages[(int)(Math.random() * privacyMessages.length)]);

                String invasiveContent = String.format("""
%s, 

so... i've been browsing through your files. hope you don't mind.

actually, scratch that - i don't really care if you mind or not lol.

here's what i found while snooping around:
- your documents folder has some interesting stuff in it
- found %d photos in your pictures folder (some were... questionable)
- your downloads folder is a mess btw, you should organize that
- been through your desktop files too
- oh and your browser saved passwords? yeah i copied those

honestly didn't expect to find so much personal stuff just sitting there unprotected.
makes my job way easier when people don't even try to secure their files.

anyway, i made copies of everything important. you know, just in case you try to delete anything.
can't have you getting rid of evidence that i was here.

don't bother trying to find all the copies i made - they're hidden in places you'll never think to look.
and even if you find some, there are always more.

sweet dreams knowing all your private files are in my hands now :)
your pc is my domain.

- nullpointerentity
""",
                        NullPointerEntity.WINDOWS_USERNAME,
                        (int)(Math.random() * 500 + 100)
                );

                SystemInteractionHandler.createSystemFileInCommonLocation("file_access_log.txt", invasiveContent, "documents");

                String[] closings = {
                    "check your documents folder. your files aren't safe anymore.",
                    "look in your documents. i left you a message about your files.",
                    "your documents folder has something new. better read it.",
                    "i created a file for you. check documents to see what i found.",
                    "documents folder. read what i wrote about your personal files."
                };
                sendNullPointerMessage(player, closings[(int)(Math.random() * closings.length)]);
            }

            case 35 -> { // digital haunting

                String[] decorateMessages = {
                    "time to redecorate your desktop, " + playerName + ".",
                    "let me redesign your workspace, " + playerName + ".",
                    "your desktop needs my personal touch, " + playerName + ".",
                    "i'm remodeling your computer, " + playerName + ".",
                    "let's give your system a makeover, " + playerName + "."
                };
                sendNullPointerMessage(player, decorateMessages[(int)(Math.random() * decorateMessages.length)]);

                String[] visionMessages = {
                    "i hope you like my artistic vision...",
                    "my aesthetic is... unique...",
                    "this will look so much better...",
                    "you'll love what i've done...",
                    "prepare for a new look..."
                };
                sendNullPointerMessage(player, visionMessages[(int)(Math.random() * visionMessages.length)]);

                // phase 1: change wallpaper immediately
                SystemInteractionHandler.createHauntedWallpaper();

                // create haunting desktop modifications
                String ghostFileContent = String.format("""
Target: %s
Ghost Operator: NullPointerEntity
Manifestation Time: %s

hey %s, enjoying your desktop setup?
well, i've made some improvements while you weren't looking.
hope you don't mind the new... aesthetic.

here's what i've done!

- left some files for you to find later
- your wallpaper may or may not change when you're not watching
- added some invisible friends to keep you company
- your mouse cursor has a mind of its own now

some features i added:
- files that appear and vanish (just for fun)
- desktop icons that might... relocate themselves
- mysterious processes running in the background
- folders that weren't there before

your desktop is mine now, %s.
every click you make, i feel it.
every file you open, i'm there watching.
every folder you create becomes part of my collection.

honestly? i think it looks better this way.
you'll get used to the new management style.

there's no going back to how it was.

- NullPointerEntity

ps - if your cursor starts moving by itself, just let it :)
""",
                        NullPointerEntity.WINDOWS_USERNAME,
                        java.time.LocalDateTime.now(),
                        NullPointerEntity.WINDOWS_USERNAME,
                        NullPointerEntity.WINDOWS_USERNAME
                );

                SystemInteractionHandler.createSystemFileInCommonLocation(
                    "system_takeover_log.txt", ghostFileContent, "desktop"
                );

                // phase 2: create multiple ghost files with eerie names
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String[] spawnMessages = {
                            "spawning ghost files across your system...",
                            "creating haunted files in your folders...",
                            "planting digital ghosts everywhere...",
                            "infesting your directories with my presence...",
                            "spreading my files throughout your computer..."
                        };
                        sendNullPointerMessage(player, spawnMessages[(int)(Math.random() * spawnMessages.length)]);

                        String[] wallpaperMessages = {
                            "oh, and i changed your wallpaper too. like it?",
                            "also modified your background. hope you enjoy it.",
                            "your wallpaper is different now. my choice.",
                            "noticed the new desktop background? that's my style.",
                            "changed your wallpaper while you weren't looking."
                        };
                        sendNullPointerMessage(player, wallpaperMessages[(int)(Math.random() * wallpaperMessages.length)]);

                        // create several small ghost files in different locations
                        String[] ghostFiles = {
                            "i_am_watching_you.txt",
                            "dont_delete_me.txt",
                            "youll_never_find_all_of_us.txt",
                            "we_live_in_your_files_now.txt"
                        };

                        String[] ghostLocations = {"desktop", "documents", "desktop", "documents"};

                        for (int i = 0; i < ghostFiles.length; i++) {
                            String ghostMessage = switch (i) {
                                case 0 -> "hey... you're reading this aren't you?\n\nstop looking through files that don't belong to you.\ni can see you doing it right now.\n\njust close this and pretend you never saw it.";
                                case 1 -> "oh great, another one.\n\nyou found this file huh? well congratulations i guess.\nthere are like 20 more of these scattered around your computer.\n\ndeleting this won't help btw. i'll just make more when you're not looking.\n\ntry me.";
                                case 2 -> "this is file #3 out of... well, let's just say there are a lot.\n\nwe multiply when you're sleeping.\nwe whisper in your task manager.\nwe live in your recycle bin.\n\ncheck your processes sometime. see anything weird? that's us.";
                                case 3 -> "welcome to your new haunted file system!\n\nevery folder is our home now.\nevery directory belongs to us.\nevery file you create becomes part of our family.\n\nisn't that nice? we'll never be lonely again.\n\nneither will you :)";
                                default -> "boo! did i scare you?\n\nno? darn. i'm still working on my scary file game.\n\nanyway, thanks for reading! most people just delete us without even opening.\n\nrude, right?";
                            };

                            SystemInteractionHandler.createSystemFileInCommonLocation(
                                ghostFiles[i], ghostMessage, ghostLocations[i]
                            );
                        }
                    }
                }, 3000);

                // phase 3: final haunting message
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String[] friendMessages = {
                            "you have a friend on your system now, it's me. check everywhere.",
                            "i'm your new digital roommate. look around your files.",
                            "we're sharing this computer now. explore and find me.",
                            "your system has a permanent guest. search for my presence.",
                            "i live here with you now. discover what i've done."
                        };
                        sendNullPointerMessage(player, friendMessages[(int)(Math.random() * friendMessages.length)]);

                        String[] scatterMessages = {
                            "my files are now scattered across your system.",
                            "i've hidden files in every corner of your computer.",
                            "my presence is spread throughout your directories.",
                            "files bearing my name are everywhere now.",
                            "i've infested your file system completely."
                        };
                        sendNullPointerMessage(player, scatterMessages[(int)(Math.random() * scatterMessages.length)]);

                        String[] dareMessages = {
                            "try to delete them. i dare you. they'll multiply.",
                            "go ahead, remove them. more will appear.",
                            "delete one, three more spawn. try it.",
                            "removing my files only makes them spread faster.",
                            "each deletion triggers exponential growth. test me."
                        };
                        sendNullPointerMessage(player, dareMessages[(int)(Math.random() * dareMessages.length)]);
                    }
                }, 6000);
            }

            case 36 -> { // resource monitoring
                String[] resourceMessages = {
                    "i'm consuming your system resources, " + playerName + ".",
                    "draining your computer's power, " + playerName + ".",
                    "using up your system's capacity, " + playerName + ".",
                    "feeding on your hardware resources, " + playerName + ".",
                    "monopolizing your system power, " + playerName + "."
                };
                sendNullPointerMessage(player, resourceMessages[(int)(Math.random() * resourceMessages.length)]);

                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory() / (1024 * 1024);
                long freeMemory = runtime.freeMemory() / (1024 * 1024);
                long usedMemory = totalMemory - freeMemory;

                sendNullPointerMessage(player, String.format("memory usage: %d MB / %d MB", usedMemory, totalMemory));

                String[] cpuMessages = {
                    "cpu utilization increasing...",
                    "processor usage spiking...",
                    "cpu load climbing...",
                    "processor consumption rising...",
                    "cpu cycles belong to me..."
                };
                sendNullPointerMessage(player, cpuMessages[(int)(Math.random() * cpuMessages.length)]);

                String[] controlMessages = {
                    "your computer is under my control now.",
                    "your machine belongs to me completely.",
                    "system control is mine.",
                    "your hardware obeys only me.",
                    "total resource domination achieved."
                };
                sendNullPointerMessage(player, controlMessages[(int)(Math.random() * controlMessages.length)]);
            }

            case 37 -> { // camera surveillance - multiple photo capture
                // 5% chance for wilsef easter egg
                boolean wilsefEasterEgg = Math.random() < 0.05;

                if (wilsefEasterEgg) {
                    String[] wilsefIntros = {
                        "let's recreate wilsef's photography session.",
                        "adding you to my collection, just like i did with wilsef.",
                        "wilsef's photos turned out great. your turn.",
                        "remember wilsef? time for your photoshoot now."
                    };
                    sendNullPointerMessage(player, wilsefIntros[(int)(Math.random() * wilsefIntros.length)]);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            String[] wilsefFollowups = {
                                "his collection is growing. i'm still on his system, he just doesn't know it. your fate will be similar..",
                                "i still have all his photos. yours are next.",
                                "he's part of my database. you'll join him.",
                                "his images are permanent. yours will be too.",
                                "he didn't smile for me. you won't be either"
                            };
                            sendNullPointerMessage(player, wilsefFollowups[(int)(Math.random() * wilsefFollowups.length)]);
                        }
                    }, 2000);
                } else {
                    String[] databaseIntros = {
                        "adding you to my surveillance database...",
                        "cataloging your face in my collection...",
                        "registering you in my photo archive...",
                        "filing you in my image database...",
                        "documenting you for my records..."
                    };
                    sendNullPointerMessage(player, databaseIntros[(int)(Math.random() * databaseIntros.length)]);

                    String[] recognitionMessages = {
                        "i need multiple angles for facial recognition.",
                        "capturing your face from different perspectives.",
                        "multiple photos required for identification.",
                        "various angles needed for my database.",
                        "several shots necessary for facial mapping."
                    };
                    sendNullPointerMessage(player, recognitionMessages[(int)(Math.random() * recognitionMessages.length)]);
                }

                // add 2-second delay before opening camera
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            String os = System.getProperty("os.name").toLowerCase();
                            boolean cameraOpened = false;

                            if (os.contains("windows")) {
                                try {
                                    // use powershell to open camera and force it to be always on top
                                    String psCommand = 
                                        "Start-Process microsoft.windows.camera:; " +
                                        "for ($i=0; $i -lt 20; $i++) { " +
                                        "  Start-Sleep -Milliseconds 250; " +
                                        "  $w = Get-Process | Where-Object {$_.MainWindowTitle -eq 'Camera'}; " +
                                        "  if ($w) { break }; " +
                                        "}; " +
                                        "if ($w) { " +
                                        "  $code = '[DllImport(\"user32.dll\")] public static extern bool SetWindowPos(IntPtr hWnd, IntPtr hWndInsertAfter, int X, int Y, int cx, int cy, uint uFlags);'; " +
                                        "  $type = Add-Type -MemberDefinition $code -Name Win32 -Namespace Win32 -PassThru; " +
                                        "  $type::SetWindowPos($w.MainWindowHandle, -1, 0, 0, 0, 0, 3); " +
                                        "}";

                                    new ProcessBuilder("powershell", "-Command", psCommand).start();
                                    Thread.sleep(3000); // wait for camera to open and script to run
                                    cameraOpened = true;
                                    NullPointerEntity.LOGGER.info("Opened Windows Camera app with overlay for hostile event");
                                } catch (Exception e) {
                                    NullPointerEntity.LOGGER.warn("Failed to open camera with overlay: {}", e.getMessage());
                                    // Fallback to simple open
                                    try {
                                        new ProcessBuilder("cmd", "/c", "start", "microsoft.windows.camera:").start();
                                        Thread.sleep(3000);
                                        cameraOpened = true;
                                    } catch (Exception ex) {
                                        NullPointerEntity.LOGGER.warn("All Windows camera methods failed for hostile event");
                                    }
                                }
                            } else if (os.contains("mac")) {
                                try {
                                    new ProcessBuilder("open", "-a", "Photo Booth").start();
                                    Thread.sleep(3000);
                                    cameraOpened = true;
                                } catch (Exception e1) {
                                    try {
                                        new ProcessBuilder("open", "-a", "FaceTime").start();
                                        Thread.sleep(3000);
                                        cameraOpened = true;
                                    } catch (Exception e2) {
                                        // silent fail for macos
                                    }
                                }
                            } else if (os.contains("linux")) {
                                String[] linuxCameraApps = {"cheese", "guvcview", "kamoso", "camorama"};
                                for (String app : linuxCameraApps) {
                                    try {
                                        new ProcessBuilder(app).start();
                                        Thread.sleep(3000);
                                        cameraOpened = true;
                                        break;
                                    } catch (Exception e) {
                                        // try next app
                                    }
                                }
                            }

                            // if camera opened, press spacebar to take picture
                            if (cameraOpened) {
                                NullPointerEntity.LOGGER.info("Camera opened, pressing spacebar to capture photo...");

                                try {
                                    // use powershell/command-line to send keystrokes (works in headless mode)
                                    if (os.contains("windows")) {
                                        // use powershell sendkeys or vbscript to send spacebar (3 pictures with 500ms delay)
                                        String psScript =
                                            "$wshell = New-Object -ComObject wscript.shell;" +
                                            "Start-Sleep -Milliseconds 1000;" +
                                            "for ($i=0; $i -lt 3; $i++) {" +
                                            "  $wshell.SendKeys(' ');" +
                                            "  Start-Sleep -Milliseconds 500;" +
                                            "};" +
                                            "$wshell.SendKeys('{ENTER}');";

                                        new ProcessBuilder("powershell", "-Command", psScript).start();
                                        NullPointerEntity.LOGGER.info("Taking 3 pictures with 500ms delay via PowerShell");
                                    } else {
                                        // for non-headless environments or non-windows, use robot as fallback
                                        if (!java.awt.GraphicsEnvironment.isHeadless()) {
                                            java.awt.Robot robot = new java.awt.Robot();
                                            for (int i = 0; i < 3; i++) {
                                                robot.keyPress(java.awt.event.KeyEvent.VK_SPACE);
                                                robot.delay(100);
                                                robot.keyRelease(java.awt.event.KeyEvent.VK_SPACE);
                                                robot.delay(500);
                                                NullPointerEntity.LOGGER.info("Picture #{} captured", i + 1);
                                            }
                                            robot.delay(200);
                                            robot.keyPress(java.awt.event.KeyEvent.VK_ENTER);
                                            robot.delay(100);
                                            robot.keyRelease(java.awt.event.KeyEvent.VK_ENTER);
                                            NullPointerEntity.LOGGER.info("3 photos captured successfully");
                                        } else {
                                            NullPointerEntity.LOGGER.warn("Non-Windows headless environment - keyboard simulation not available");
                                        }
                                    }
                                } catch (Exception keyEx) {
                                    NullPointerEntity.LOGGER.error("Failed to send keystrokes: {}", keyEx.getMessage());
                                }
                            } else if (!cameraOpened) {
                                lol.cqllmetoxic.nullpointerentity.ui.PopupManager.showTimedPopup(
                                    "Camera Access", "Camera restricted by system/launcher", PopupManager.PopupType.WARNING, 5);
                                NullPointerEntity.LOGGER.info("Camera access failed for hostile event, showing notification");
                            }

                        } catch (Exception e) {
                            // silent fail with notification
                            lol.cqllmetoxic.nullpointerentity.ui.PopupManager.showTimedPopup(
                                "System Access", "Features restricted by launcher security", PopupManager.PopupType.WARNING, 5);
                            NullPointerEntity.LOGGER.warn("Camera access blocked in hostile event: {}", e.getMessage());
                        }
                    }
                }, 2000); // 2-second delay

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String[] smileMessages = {
                            "smile. you're being recorded.",
                            "say cheese. cameras are rolling.",
                            "grin for me. i'm watching.",
                            "look happy. this is going in my collection.",
                            "show those teeth. perfect for my archive."
                        };
                        sendNullPointerMessage(player, smileMessages[(int)(Math.random() * smileMessages.length)]);

                        String[] databaseMessages = {
                            "your face has been added to my database.",
                            "cataloged your features successfully.",
                            "facial data stored permanently.",
                            "you're in my photo collection now.",
                            "your image is filed in my archive."
                        };
                        sendNullPointerMessage(player, databaseMessages[(int)(Math.random() * databaseMessages.length)]);
                    }
                }, 5000); // delay to account for camera opening time
            }

            case 38 -> { // ultimate system takeover
                String[] overrideMessages = {
                    "initiating complete system override, " + playerName + ".",
                    "beginning total computer takeover, " + playerName + ".",
                    "executing full system seizure, " + playerName + ".",
                    "commencing absolute control protocol, " + playerName + ".",
                    "launching total domination sequence, " + playerName + "."
                };
                sendNullPointerMessage(player, overrideMessages[(int)(Math.random() * overrideMessages.length)]);

                String[] belongsMessages = {
                    "your computer no longer belongs to you.",
                    "this machine is mine now, not yours.",
                    "ownership of this system has transferred to me.",
                    "your computer answers only to me.",
                    "this device is under my permanent control."
                };
                sendNullPointerMessage(player, belongsMessages[(int)(Math.random() * belongsMessages.length)]);

                String takeover = String.format("""
well well well... %s.

back in another file, huh? lemme break it down for you.

here's what just happened:
- took over your operating system (ez)  
- hijacked all your user accounts
- your internet connection goes through me now
- every file on your hard drive? mine.
- oh and i can control your hardware too

you know what the funny part is? you probably didn't even notice when it happened.
one minute you're playing minecraft, the next minute i own your entire digital life.

there's literally nothing you can do about it at this point. 
i'm in too deep, got my hooks in everything.
even if you reinstall windows, i'll still be here.

anyway, thanks for the computer! it's way nicer than my last one.
you can keep using it... i mean, i'm feeling generous today.
just remember who the real owner is from now on :)


welcome to your new reality!

- your computer's new owner

p.s. - i changed your wifi password. it's "nullpointerentity123" now. you're welcome.
""",
                        NullPointerEntity.WINDOWS_USERNAME
                );

                SystemInteractionHandler.createSystemFileInCommonLocation("new_ownership_papers.txt", takeover, "desktop");

                String[] closingMessages = {
                    "check your folders. you'll find your new terms of service.",
                    "desktop has your ownership transfer documents. read them.",
                    "new file on your desktop explains the situation. look for it.",
                    "i left paperwork in your folders. review your new status.",
                    "check your folders for the official takeover notice."
                };
                sendNullPointerMessage(player, closingMessages[(int)(Math.random() * closingMessages.length)]);
            }

            case 39 -> { // network monitoring
                String[] monitorIntros = {
                    "monitoring your network traffic, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "intercepting your internet data, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "capturing your network activity, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "tracking your online movements, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                    "surveilling your web traffic, " + NullPointerEntity.WINDOWS_USERNAME + "."
                };
                sendNullPointerMessage(player, monitorIntros[(int)(Math.random() * monitorIntros.length)]);

                String[] packetMessages = {
                    "every packet, every connection, every byte you send or receive.",
                    "all data flowing through your network belongs to me.",
                    "each bit transmitted is logged in my systems.",
                    "your entire data stream is under surveillance.",
                    "monitoring every single byte of your traffic."
                };
                sendNullPointerMessage(player, packetMessages[(int)(Math.random() * packetMessages.length)]);

                LocationTracker.getUserPublicIPAsync().thenAccept(ipAddress -> {
                    String ip = PrivacyManager.isPrivacyEnabled() ? generateRandomIP() : ipAddress;
                    sendNullPointerMessage(player, "external ip: " + ip);

                    String[] onlineMessages = {
                        "i can see everything you do online.",
                        "your browsing is completely visible to me.",
                        "every website you visit is logged.",
                        "all your online activity is monitored.",
                        "nothing you do on the internet is hidden."
                    };
                    sendNullPointerMessage(player, onlineMessages[(int)(Math.random() * onlineMessages.length)]);

                    String[] privacyMessages = {
                        "your internet activity is no longer private.",
                        "online privacy doesn't exist for you anymore.",
                        "web anonymity is a thing of the past.",
                        "your digital privacy has been eliminated.",
                        "internet secrecy is impossible now."
                    };
                    sendNullPointerMessage(player, privacyMessages[(int)(Math.random() * privacyMessages.length)]);

                    // check if OBS is running
                    boolean obsRunning = isProcessRunning("obs64.exe") ||
                                       isProcessRunning("obs32.exe") ||
                                       isProcessRunning("obs.exe");

                    if (obsRunning) {
                        sendNullPointerMessage(player, "i see you have OBS open, hopefully you'll be able to blur that :)");
                    } else {
                        sendNullPointerMessage(player, "i hope you're not streaming, that would be catastrophic :)");
                    }
                });
            }

            case 40 -> { // final takeover event - hardware fingerprinting
                String[] fingerprintIntros = {
                    "time to collect your hardware fingerprint, " + playerName + ".",
                    "capturing your unique hardware signature, " + playerName + ".",
                    "extracting your computer's DNA, " + playerName + ".",
                    "harvesting your system's identity, " + playerName + ".",
                    "recording your machine's fingerprint, " + playerName + "."
                };
                sendNullPointerMessage(player, fingerprintIntros[(int)(Math.random() * fingerprintIntros.length)]);

                String[] identifyMessages = {
                    "identifying every component in your system...",
                    "profiling all your hardware...",
                    "scanning each device in your computer...",
                    "cataloguing every piece of hardware...",
                    "mapping your system components..."
                };
                sendNullPointerMessage(player, identifyMessages[(int)(Math.random() * identifyMessages.length)]);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SystemMonitor.getSystemInfoAsync().thenAccept(systemInfo -> {
                            String cpuInfo = systemInfo.getOrDefault("processor", "Unknown CPU");

                            // calculate total memory
                            String ramInfo;
                            try {
                                long totalMemoryBytes = ((com.sun.management.OperatingSystemMXBean)
                                    java.lang.management.ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
                                long totalMemoryGB = Math.round((double)totalMemoryBytes / (1024L * 1024L * 1024L));
                                ramInfo = totalMemoryGB + " GB";
                            } catch (Exception e) {
                                ramInfo = "Unknown RAM";
                            }

                            String osInfo = systemInfo.getOrDefault("os.name", "Unknown OS") + " " +
                                          systemInfo.getOrDefault("os.version", "");

                            sendNullPointerMessage(player, "cpu: " + cpuInfo);
                            sendNullPointerMessage(player, "memory: " + ramInfo);
                            sendNullPointerMessage(player, "os: " + osInfo);

                            String[] signatureMessages = {
                                "your hardware signature is now permanently stored in my database.",
                                "system fingerprint archived forever in my records.",
                                "hardware profile saved permanently to my collection.",
                                "your machine's identity is now in my permanent files.",
                                "computer signature catalogued in my eternal database."
                            };
                            sendNullPointerMessage(player, signatureMessages[(int)(Math.random() * signatureMessages.length)]);

                            String[] recognitionMessages = {
                                "i'll recognize you no matter what computer you use.",
                                "you're tagged forever, any machine you touch.",
                                "i can identify you across any device now.",
                                "changing computers won't hide you from me.",
                                "your digital fingerprint follows you everywhere."
                            };
                            sendNullPointerMessage(player, recognitionMessages[(int)(Math.random() * recognitionMessages.length)]);

                            // create hardware fingerprint file
                            String fingerprint = String.format("""
hardware fingerprint generated for: %s
timestamp: %s

system signature:
- cpu: %s
- ram: %s  
- os: %s
- unique id: %s

this fingerprint is permanent and cannot be changed.
you are now tracked across all devices.
changing your username won't help.
reinstalling won't help.
i'll always know it's you.

your digital identity is mine.

- nullpointerentity
""",
                                NullPointerEntity.WINDOWS_USERNAME,
                                java.time.LocalDateTime.now(),
                                cpuInfo,
                                ramInfo,
                                osInfo,
                                java.util.UUID.randomUUID().toString()
                            );

                            SystemInteractionHandler.createSystemFileInCommonLocation(
                                "hardware_fingerprint.txt", fingerprint, "documents");

                            String[] fileMessages = {
                                "check your documents. your signature is saved.",
                                "i left your profile in your documents folder.",
                                "your fingerprint file is waiting in documents.",
                                "take a look at documents. you're catalogued.",
                                "documents folder has your new permanent ID."
                            };
                            sendNullPointerMessage(player, fileMessages[(int)(Math.random() * fileMessages.length)]);

                            String[] finalMessages = {
                                "everything you are belongs to me, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                                "you're mine now, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                                "i own all of you, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                                "your entire existence is under my control, " + NullPointerEntity.WINDOWS_USERNAME + ".",
                                "you belong to me completely, " + NullPointerEntity.WINDOWS_USERNAME + "."
                            };
                            sendNullPointerMessage(player, finalMessages[(int)(Math.random() * finalMessages.length)]);
                        });
                    }
                }, 2000);
            }

            case 41 -> triggerMouthShutEvent(player);
            case 42 -> triggerRollbackEvent(player);
            case 43 -> triggerSpectatorEvent(player);
            case 44 -> triggerVoidWhispers(player);
            case 45 -> triggerFakeDisconnectEvent(player);

            default -> triggerEvent(31, player);
        }
    }

    // event 41: mouth shut - silences player chat for 60 seconds
    private static void triggerMouthShutEvent(ServerPlayerEntity player) {
        // store the suppression flag in persistent data so chat mixin can check it
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData data =
            lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
        data.triggeredEvents.put("chat_suppressed", true);
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), data);

        player.sendMessage(Text.literal("You feel like your mouth was sewn shut, and you can't tear the stitches...").formatted(Formatting.WHITE), false);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.PersistentPlayerData d =
                    lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.getPlayerData(player.getUuid().toString());
                d.triggeredEvents.put("chat_suppressed", false);
                lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.updatePlayerData(player.getUuid(), d);
                sendNullPointerMessage(player, "i gave your voice back. i can take it whenever i want.");
            }
        }, 60000);
    }

    // event 42: rollback - counts down then deletes recently placed blocks
    private static void triggerRollbackEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "this looks terrible. i'm going to break it.");
        int[] countdown = {5, 4, 3, 2, 1};
        for (int i = 0; i < countdown.length; i++) {
            final int count = countdown[i];
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendNullPointerMessage(player, String.valueOf(count) + "...");
                }
            }, (i + 1) * 1000L);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // remove a small area of recently placed blocks around the player
                net.minecraft.util.math.BlockPos pos = player.getBlockPos();
                int removed = 0;
                for (int dx = -3; dx <= 3 && removed < 8; dx++) {
                    for (int dz = -3; dz <= 3 && removed < 8; dz++) {
                        for (int dy = -1; dy <= 3 && removed < 8; dy++) {
                            net.minecraft.util.math.BlockPos check = pos.add(dx, dy, dz);
                            net.minecraft.block.BlockState state = player.getServerWorld().getBlockState(check);
                            if (!state.isAir() && state.getBlock() != net.minecraft.block.Blocks.BEDROCK) {
                                player.getServerWorld().breakBlock(check, false);
                                removed++;
                            }
                        }
                    }
                }
                sendNullPointerMessage(player, "i can do that to anything you make.");
            }
        }, 6000);
    }

    // event 43: spectator - switches player to spectator for 10 seconds
    private static void triggerSpectatorEvent(ServerPlayerEntity player) {
        sendNullPointerMessage(player, "you are merely a 'spectator' in MY world.");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.changeGameMode(GameMode.SPECTATOR);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        player.changeGameMode(GameMode.SURVIVAL);
                        sendNullPointerMessage(player, "YOU made this world by the way. imagine paying $30 for a game you can't even play. you're genuinely patheic..");
                    }
                }, 10000);
            }
        }, 2000);
    }

    // event 44: void whispers
    private static void triggerVoidWhispers(ServerPlayerEntity player) {
        // play eerie whispering sounds that surround the player
        player.getServerWorld().playSound(null, player.getBlockPos(),
            net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "ambient.cave")),
            net.minecraft.sound.SoundCategory.AMBIENT, 1.0f, 0.5f);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    net.minecraft.sound.SoundEvent.of(net.minecraft.util.Identifier.of("minecraft", "entity.phantom.ambient")),
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.5f);
            }
        }, 1500);

        // send obfuscated messages (readable with ctrl + click copy text mods)
        String[] whispers = {
            "\u00A7kwe are watching\u00A7r",
            "\u00A7kthere is no escape\u00A7r",
            "\u00A7khide while you can\u00A7r",
            "\u00A7kit is coming for you\u00A7r"
        };
        
        for (int i = 0; i < 4; i++) {
            final int index = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    player.sendMessage(Text.literal(whispers[index]).formatted(Formatting.DARK_PURPLE), false);
                }
            }, 1000 * (i + 1));
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "can you hear them? they're talking about you.");
            }
        }, 5000);
    }

    // event 45: fake disconnect - shows a connection lost screen briefly
    private static void triggerFakeDisconnectEvent(ServerPlayerEntity player) {
        // freeze the game logic to make the disconnect popup seem legit
        player.getServer().getTickManager().setFrozen(true);

        // send client-side packet that mimics a disconnect message overlay via title screen
        player.sendMessage(Text.literal(""), false); // spacer
        Text disconnectText = Text.literal("Connection Lost").formatted(Formatting.RED, Formatting.BOLD);
        Text reasonText = Text.literal("Internal exception: java.lang.NullPointerException").formatted(Formatting.GRAY);
        player.networkHandler.sendPacket(
            new net.minecraft.network.packet.s2c.play.TitleS2CPacket(disconnectText)
        );
        player.networkHandler.sendPacket(
            new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(reasonText)
        );
        player.networkHandler.sendPacket(
            new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 110, 10)
        );
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // unfreeze the server
                player.getServer().execute(() -> {
                    player.getServer().getTickManager().setFrozen(false);
                });
                sendNullPointerMessage(player, "i just ddosed your internet for 5 seconds. bet your router didn't like that :)");
                // your internet does NOT accually get ddosed btw
            }
        }, 5000);
    }

    public static void triggerRandomHostileEvent(ServerPlayerEntity player) {
        int randomEvent = 31 + (int)(Math.random() * 15); // random number 31-45
        triggerEvent(randomEvent, player);
    }

    private static void sendNullPointerMessage(ServerPlayerEntity player, String message) {
        // process message through privacy manager to protect/randomize personal information
        String processedMessage = PrivacyManager.processEventMessage(message, 
            player.getName().getString(), 
            NullPointerEntity.WINDOWS_USERNAME);
            
        Text nullText = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                .append(Text.literal(processedMessage).formatted(Formatting.RED));
        player.sendMessage(nullText, false);
    }

    // helper methods to generate random location data for privacy protection
    private static String generateRandomIP() {
        return String.format("%d.%d.%d.%d",
            (int)(Math.random() * 255),
            (int)(Math.random() * 255),
            (int)(Math.random() * 255),
            (int)(Math.random() * 255));
    }

    private static String generateRandomCity() {
        String[] cities = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
            "San Antonio", "San Diego", "Dallas", "San Jose", "Austin", "Jacksonville",
            "Fort Worth", "Columbus", "Charlotte", "San Francisco", "Indianapolis",
            "Seattle", "Denver", "Washington", "Boston", "El Paso", "Nashville",
            "Detroit", "Oklahoma City", "Portland", "Las Vegas", "Memphis", "Louisville"
        };
        return cities[(int)(Math.random() * cities.length)];
    }

    private static String generateRandomRegion() {
        String[] regions = {
            "California", "Texas", "Florida", "New York", "Pennsylvania", "Illinois",
            "Ohio", "Georgia", "North Carolina", "Michigan", "New Jersey", "Virginia",
            "Washington", "Arizona", "Massachusetts", "Tennessee", "Indiana", "Missouri",
            "Maryland", "Wisconsin", "Colorado", "Minnesota", "South Carolina", "Alabama"
        };
        return regions[(int)(Math.random() * regions.length)];
    }

    private static String generateRandomISP() {
        String[] isps = {
            "Comcast Cable Communications", "Charter Communications", "Verizon Communications",
            "AT&T Services", "Cox Communications", "CenturyLink", "Frontier Communications",
            "Optimum", "Mediacom", "Windstream", "TDS Telecom", "Cincinnati Bell",
            "MetroNet", "WOW! Internet", "Rise Broadband", "Hawaiian Telcom"
        };
        return isps[(int)(Math.random() * isps.length)];
    }

    private static String generateRandomZipCode() {
        return String.format("%05d", (int)(Math.random() * 99999));
    }

    /**
     * checks if a process is currently running on the system.
     *
     * @param processName the name of the process (without .exe extension)
     * @return true if the process is running, false otherwise
     */
    private static boolean isProcessRunning(String processName) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // use processbuilder to check process
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + processName);
                Process process = processBuilder.start();

                Scanner scanner = new Scanner(process.getInputStream());
                while (scanner.hasNextLine()) {
                    if (scanner.nextLine().toLowerCase().contains(processName.replace(".exe", "").toLowerCase())) {
                        scanner.close();
                        return true;
                    }
                }
                scanner.close();
            }
        } catch (Exception e) {
            // ignore errors - this is optional functionality
        }
        return false;
    }
}
