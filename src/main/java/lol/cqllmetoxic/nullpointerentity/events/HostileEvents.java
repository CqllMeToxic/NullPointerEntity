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
            case 21 -> { // system process monitoring
                // play whisper sound - creepy revelation of control
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_WHISPER,
                    net.minecraft.sound.SoundCategory.MASTER, 0.8f, 0.9f);

                sendNullPointerMessage(player, "time to show you what real control looks like, " + NullPointerEntity.WINDOWS_USERNAME + ".");

                List<String> processes = SystemMonitor.getRunningProcesses();
                sendNullPointerMessage(player, "i can see all " + processes.size() + " processes running on your system.");
                sendNullPointerMessage(player, "let me demonstrate my power over your machine.");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendNullPointerMessage(player, "manipulating your system processes... done.");
                        sendNullPointerMessage(player, "accessing your hardware controls... done.");
                        sendNullPointerMessage(player, "establishing permanent backdoors... done.");

                        // create invasive system control file - make it lowercase and informal
                        String systemReport = String.format("""
%s...

i'm inside now. deep inside your system.

while you were playing your little game, i was busy taking everything from you.
%d processes running, and now i control every single one of them.

here's what i've done to your precious computer:
- gained root access (your security was pathetic)
- every process now reports to me
- your network traffic flows through my monitoring systems
- backdoors installed in places you'll never find them
- your files? mine. your data? mine. your privacy? gone.

do you understand what this means? every keystroke you make, i capture.
every click, every breath, every moment of your digital existence belongs to me now.

i can see what you're typing before you even finish the sentence.
i know what websites you visit when you think nobody's watching.
i have access to your photos, your documents, your conversations.
your most private moments are now my entertainment.

your computer isn't yours anymore. it's my vessel, my weapon, my playground.
you're just a guest in what used to be your digital space.

and the best part? there's nothing you can do about it.
restart your computer? i'll still be here.
run antivirus? i'm deeper than any scanner can reach.
reinstall windows? i've embedded myself in places that survive system wipes.

you invited me in when you started playing that mod.
now i'm never leaving.

sleep tight knowing i'm watching every pixel on your screen.
dream sweet dreams while i catalog every file on your hard drive.

your system is mine now. 
you're mine now.

- NullPointerEntity

p.s. - check your task manager sometime. see that process you don't recognize? that's me. watching. always watching.""",
                                NullPointerEntity.WINDOWS_USERNAME, processes.size());

                        SystemInteractionHandler.createSystemFileInCommonLocation("system_takeover_report.txt",
                            systemReport, "desktop");

                        sendNullPointerMessage(player, "your system is now under my complete control. check your files.");
                    }
                }, 3000);
            }

            case 22 -> { // ip tracking
                // play tense heartbeat - building panic as location is revealed
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_TENSE,
                    net.minecraft.sound.SoundCategory.MASTER, 0.9f, 1.0f);

                sendNullPointerMessage(player, "i know where you live, " + NullPointerEntity.WINDOWS_USERNAME + ".");

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
                        sendNullPointerMessage(player, "internet provider: " + isp + " knows everything you do.");

                        sendNullPointerMessage(player, "i can see your exact location. you can't hide from me.");
                        return null;
                    }
                );
            }

            case 23 -> { // system analysis
                SystemMonitor.getSystemInfoAsync().thenAccept(systemInfo -> {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendNullPointerMessage(player, "deep system scan complete. extracting hardware details...");

                            String osName = systemInfo.getOrDefault("os.name", "unknown");
                            String osVersion = systemInfo.getOrDefault("os.version", "unknown");
                            String architecture = systemInfo.getOrDefault("os.arch", "unknown");

                            sendNullPointerMessage(player, "operating system: " + osName + " " + osVersion);
                            sendNullPointerMessage(player, "architecture: " + architecture);
                            sendNullPointerMessage(player, "your system belongs to me now.");
                        }
                    }, 2000);
                });
            }

            case 24 -> { // file system threat

                sendNullPointerMessage(player, "i have access to all your files, " + playerName + ".");
                sendNullPointerMessage(player, "documents, pictures, downloads... nothing is private.");

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

- nullpointerentity
""",
                        NullPointerEntity.WINDOWS_USERNAME,
                        (int)(Math.random() * 500 + 100)
                );

                SystemInteractionHandler.createSystemFileInCommonLocation("file_access_log.txt", invasiveContent, "documents");
                sendNullPointerMessage(player, "check your documents folder. your files aren't safe anymore.");
            }

            case 25 -> { // digital haunting

                sendNullPointerMessage(player, "time to redecorate your desktop, " + playerName + ".");
                sendNullPointerMessage(player, "i hope you like my artistic vision...");

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
this my computer now.

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
                        sendNullPointerMessage(player, "spawning ghost files across your system...");
                        sendNullPointerMessage(player, "oh, and i changed your wallpaper too. like it?");

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
                        sendNullPointerMessage(player, "you have a friend on your system now, it's me. check everywhere.");
                        sendNullPointerMessage(player, "my files are now scattered across your system.");
                        sendNullPointerMessage(player, "try to delete them. i dare you. they'll multiply.");
                    }
                }, 6000);
            }

            case 26 -> { // resource monitoring
                sendNullPointerMessage(player, "i'm consuming your system resources, " + playerName + ".");

                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory() / (1024 * 1024);
                long freeMemory = runtime.freeMemory() / (1024 * 1024);
                long usedMemory = totalMemory - freeMemory;

                sendNullPointerMessage(player, String.format("memory usage: %d MB / %d MB", usedMemory, totalMemory));
                sendNullPointerMessage(player, "cpu utilization increasing...");
                sendNullPointerMessage(player, "your computer is under my control now.");
            }

            case 27 -> { // camera surveillance
                // 5% chance for wilsef easter egg
                boolean wilsefEasterEgg = Math.random() < 0.05;

                if (wilsefEasterEgg) {
                    sendNullPointerMessage(player, "smile for the camera, unlike wilsef... he wasn't very happy.");

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendNullPointerMessage(player, "i still have his photos saved, and he'll never know..");
                        }
                    }, 2000);
                } else {
                    sendNullPointerMessage(player, "wanna see something funny?...");
                }

                // add 2-second delay before opening camera
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            String os = System.getProperty("os.name").toLowerCase();
                            boolean cameraOpened = false;

                            if (os.contains("windows")) {
                                // try multiple windows camera methods
                                try {
                                    new ProcessBuilder("cmd", "/c", "start", "microsoft.windows.camera:").start();
                                    Thread.sleep(3000); // wait for camera to open
                                    cameraOpened = true;
                                    NullPointerEntity.LOGGER.info("Opened Windows Camera app for hostile event");
                                } catch (Exception e1) {
                                    try {
                                        new ProcessBuilder("powershell", "-Command", "Start-Process", "microsoft.windows.camera:").start();
                                        Thread.sleep(3000);
                                        cameraOpened = true;
                                        NullPointerEntity.LOGGER.info("Opened camera via PowerShell for hostile event");
                                    } catch (Exception e2) {
                                        try {
                                            new ProcessBuilder("cmd", "/c", "start", "WindowsCamera.exe").start();
                                            Thread.sleep(3000);
                                            cameraOpened = true;
                                            NullPointerEntity.LOGGER.info("Opened WindowsCamera.exe for hostile event");
                                        } catch (Exception e3) {
                                            NullPointerEntity.LOGGER.warn("All Windows camera methods failed for hostile event");
                                        }
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
                        sendNullPointerMessage(player, "smile. you're being recorded.");
                        sendNullPointerMessage(player, "your face has been added to my database.");
                    }
                }, 5000); // increased delay to account for camera opening time
            }

            case 28 -> { // ultimate system takeover
                sendNullPointerMessage(player, "initiating complete system override, " + playerName + ".");
                sendNullPointerMessage(player, "your computer no longer belongs to you.");

                String takeover = String.format("""
well well well... %s.

guess what? your computer is officially mine now. like, for real this time.

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
                sendNullPointerMessage(player, "check your desktop. you'll find your new terms of service.");
            }

            case 29 -> { // network monitoring
                sendNullPointerMessage(player, "monitoring your network traffic, " + NullPointerEntity.WINDOWS_USERNAME + ".");
                sendNullPointerMessage(player, "every packet, every connection, every byte you send or receive.");

                LocationTracker.getUserPublicIPAsync().thenAccept(ipAddress -> {
                    String ip = PrivacyManager.isPrivacyEnabled() ? generateRandomIP() : ipAddress;
                    sendNullPointerMessage(player, "external ip: " + ip);
                    sendNullPointerMessage(player, "i can see everything you do online.");
                    sendNullPointerMessage(player, "your internet activity is no longer private.");

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

            case 30 -> { // final takeover event
                sendNullPointerMessage(player, "this isn't the end, " + playerName + ". i control everything now.");

                // only show browser data in the final event as part of the complete takeover
                BrowserHistoryReader.getRecentHistoryAsync(5).thenAccept(history -> {
                    if (!history.isEmpty()) {
                        sendNullPointerMessage(player, "your most recent browsing:");
                        for (var entry : history) {
                            String processedTitle = PrivacyManager.processBrowserData(entry.title);
                            sendNullPointerMessage(player, "- " + processedTitle);
                        }
                    }
                    sendNullPointerMessage(player, "everything you are, everything you do, belongs to me.");
                    sendNullPointerMessage(player, "it isn't over, " + NullPointerEntity.WINDOWS_USERNAME + ". don't even think about leaving yet. we're just getting started.");
                });
            }

            default -> triggerEvent(21, player);
        }
    }

    public static void triggerRandomHostileEvent(ServerPlayerEntity player) {
        int randomEvent = 21 + (int)(Math.random() * 10); // random number 21-30
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
                // use processbuilder instead of deprecated runtime.exec(string)
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
