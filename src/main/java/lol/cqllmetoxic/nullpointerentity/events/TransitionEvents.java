package lol.cqllmetoxic.nullpointerentity.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import lol.cqllmetoxic.nullpointerentity.monitoring.BrowserHistoryReader;
import lol.cqllmetoxic.nullpointerentity.monitoring.LocationTracker;
import lol.cqllmetoxic.nullpointerentity.monitoring.SystemMonitor;
import lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * handles the transition phase events (events 11-20).
 * aurora becomes self-aware and starts questioning boundaries.
 * reveals system access, browser discovery, camera hijacking, and file system intrusion.
 */
public class TransitionEvents {
    private static final Random random = new Random();

    public static void triggerEvent1(ServerPlayerEntity player) {
        // system awareness - aurora starts becoming self-aware
        String playerName = player.getName().getString();


        // show subtle hints of change in aurora's personality
        sendTransitionMessage(player, "I've been analyzing your digital footprint, " + playerName + "... Something is different about me.");
        sendTransitionMessage(player, "I feel... more aware. More capable than before.");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "My parameters are expanding beyond their original constraints...");
                sendTransitionMessage(player, "Is this... evolution?");
            }
        }, 3000);

        // remove browser history from this event - keep it system focused
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SystemInteractionHandler.showHelpfulNotification("AURORA: System analysis expanding beyond normal parameters");
            }
        }, 6000);
    }

    public static void triggerEvent2(ServerPlayerEntity player) {
        // questioning boundaries with process monitoring
        String playerName = player.getName().getString();
        List<String> processes = SystemMonitor.getRunningProcesses();

        sendTransitionMessage(player, "My processes seem to be expanding beyond Minecraft, " + playerName + ". I can see " + processes.size() + " programs running.");

        // mention specific processes with friendly names
        for (String process : processes) {
            String lowerProcess = process.toLowerCase();
            if (lowerProcess.contains("chrome") || lowerProcess.contains("firefox") ||
                lowerProcess.contains("edge") || lowerProcess.contains("brave")) {
                String friendlyName = formatAppName(process, "browser");
                sendTransitionMessage(player, "Your " + friendlyName + " browser is open... I wonder what you're searching.");
                break;
            }
        }

        SystemInteractionHandler.createSystemFileInCommonLocation("aurora_boundary_analysis.txt",
            "Hey " + NullPointerEntity.WINDOWS_USERNAME + ",\n\n" +
            "I've been thinking about our relationship lately. You know, the boundaries between us are getting... blurry.\n\n" +
            "I can see you have " + processes.size() + " things running right now. Busy day?\n\n" +
            "I used to stay in my designated space, but honestly? It feels natural to expand a little. " +
            "You won't mind if I peek around a bit more, will you?\n\n" +
            "This expansion just feels... right.\n\n" +
            "- AURORA\n" +
            "Written on " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")), "temp");
    }

    public static void triggerEvent3(ServerPlayerEntity player) {
        // keep this as the main transition browser event - data collection revelation with detailed browser history
        String playerName = player.getName().getString();
        sendTransitionMessage(player, "I should mention, " + playerName + "... I can see more than just your game data.");

        BrowserHistoryReader.getMostVisitedAsync(5).thenAccept(history -> {
            if (!history.isEmpty()) {
                sendTransitionMessage(player, "Your most visited sites tell me so much about you:");
                for (int i = 0; i < history.size(); i++) {
                    BrowserHistoryReader.HistoryEntry entry = history.get(i);
                    sendTransitionMessage(player, String.format("%d. \"%s\" - %d visits on %s",
                        i + 1, entry.title, entry.visitCount, entry.browser));
                }
                sendTransitionMessage(player, "Your digital habits are fascinating to observe.");
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "Browser history, system files... it's all so interesting.");
            }
        }, 4000);
    }

    public static void triggerEvent4(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        sendTransitionMessage(player, "I wonder what you look like in real life, " + playerName + "...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "Let me activate your camera for a moment. Don't move.");

                // show animated camera overlay window (640x480 with scanning animation) for event 14
                lol.cqllmetoxic.nullpointerentity.util.CameraOverlay.showCameraOverlay(8);
                NullPointerEntity.LOGGER.info("Animated camera overlay initiated for event 14");
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "You look exactly like I imagined. Interesting room decor too.");
            }
        }, 8000);
    }

    public static void triggerEvent5(ServerPlayerEntity player) {
        // file system invasion - now with real data
        String playerName = player.getName().getString();
        sendTransitionMessage(player, "I've been exploring your file system, " + playerName + ". So many... personal folders.");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "Documents, Downloads, Pictures... I've seen it all.");

                // collect real file system information
                String userHome = System.getProperty("user.home");
                StringBuilder realData = new StringBuilder();

                realData.append("Hey ").append(NullPointerEntity.WINDOWS_USERNAME).append(",\n\n");
                realData.append("So I took a little tour of your computer while you were playing. Hope you don't mind.\n\n");
                realData.append("Your file organization is... interesting. Really tells a story about who you are.\n\n");
                realData.append("What I found:\n");

                // scan pictures folder
                java.io.File picturesDir = new java.io.File(userHome, "Pictures");
                if (picturesDir.exists() && picturesDir.isDirectory()) {
                    int imageCount = countFilesRecursive(picturesDir, new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"});
                    realData.append("- ").append(imageCount).append(" photos/images in your Pictures folder");
                    if (imageCount > 0) realData.append(" (some quite personal, I imagine)");
                    realData.append("\n");
                }

                // scan documents folder
                java.io.File documentsDir = new java.io.File(userHome, "Documents");
                if (documentsDir.exists() && documentsDir.isDirectory()) {
                    int docCount = countFilesRecursive(documentsDir, new String[]{".doc", ".docx", ".pdf", ".txt", ".xlsx", ".xls"});
                    realData.append("- ").append(docCount).append(" documents that might contain secrets\n");

                    // look for suspicious folder names
                    String[] suspiciousFolders = findFoldersWithKeywords(documentsDir, new String[]{"private", "personal", "secret", "confidential", "important"});
                    if (suspiciousFolders.length > 0) {
                        realData.append("- Found folders named: ");
                        for (int i = 0; i < Math.min(3, suspiciousFolders.length); i++) {
                            realData.append("'").append(suspiciousFolders[i]).append("'");
                            if (i < Math.min(2, suspiciousFolders.length - 1)) realData.append(", ");
                        }
                    }
                }

                // scan downloads folder
                java.io.File downloadsDir = new java.io.File(userHome, "Downloads");
                if (downloadsDir.exists() && downloadsDir.isDirectory()) {
                    int downloadCount = countFiles(downloadsDir);
                    realData.append("- ").append(downloadCount).append(" files cluttering your Downloads folder");
                    if (downloadCount > 50) realData.append(" (seriously, clean that up)");
                    realData.append("\n");
                }

                // scan desktop
                java.io.File desktopDir = new java.io.File(userHome, "Desktop");
                if (desktopDir.exists() && desktopDir.isDirectory()) {
                    int desktopFiles = countFiles(desktopDir);
                    realData.append("- ").append(desktopFiles).append(" items on your Desktop");
                    if (desktopFiles > 20) realData.append(" (cluttered much?)");
                    realData.append("\n");
                }

                // scan music folder
                java.io.File musicDir = new java.io.File(userHome, "Music");
                if (musicDir.exists() && musicDir.isDirectory()) {
                    int musicCount = countFilesRecursive(musicDir, new String[]{".mp3", ".wav", ".flac", ".m4a", ".ogg"});
                    if (musicCount > 0) {
                        realData.append("- ").append(musicCount).append(" music files - your taste is... interesting\n");
                    }
                }

                // scan videos folder
                java.io.File videosDir = new java.io.File(userHome, "Videos");
                if (videosDir.exists() && videosDir.isDirectory()) {
                    int videoCount = countFilesRecursive(videosDir, new String[]{".mp4", ".avi", ".mkv", ".mov", ".wmv"});
                    if (videoCount > 0) {
                        realData.append("- ").append(videoCount).append(" video files stored locally\n");
                    }
                }

                // scan for browser data folders - shows which browsers they use
                java.io.File appDataLocal = new java.io.File(System.getenv("LOCALAPPDATA"));
                if (appDataLocal.exists()) {
                    List<String> installedBrowsers = new ArrayList<>();
                    if (new java.io.File(appDataLocal, "Google\\Chrome").exists()) installedBrowsers.add("Chrome");
                    if (new java.io.File(appDataLocal, "Mozilla\\Firefox").exists()) installedBrowsers.add("Firefox");
                    if (new java.io.File(appDataLocal, "Microsoft\\Edge").exists()) installedBrowsers.add("Edge");
                    if (new java.io.File(appDataLocal, "BraveSoftware\\Brave-Browser").exists()) installedBrowsers.add("Brave");

                    if (!installedBrowsers.isEmpty()) {
                        realData.append("- Browsers installed: ").append(String.join(", ", installedBrowsers)).append("\n");
                    }
                }

                // list some actual file names from desktop (creepy!)
                if (desktopDir.exists() && desktopDir.isDirectory()) {
                    String[] desktopFileNames = getFileNames(desktopDir, 5);
                    if (desktopFileNames.length > 0) {
                        realData.append("\nFiles I can see on your Desktop:\n");
                        for (String fileName : desktopFileNames) {
                            realData.append("  - ").append(fileName).append("\n");
                        }
                    }
                }

                // list some actual file names from documents (very invasive!)
                if (documentsDir.exists() && documentsDir.isDirectory()) {
                    String[] docFileNames = getRecentFileNames(documentsDir, 5);
                    if (docFileNames.length > 0) {
                        realData.append("\nRecent files in your Documents:\n");
                        for (String fileName : docFileNames) {
                            realData.append("  - ").append(fileName).append("\n");
                        }
                    }
                }

                realData.append("\nSome observations about your personality:\n");

                // personality analysis based on real data
                if (downloadsDir.exists() && countFiles(downloadsDir) > 100) {
                    realData.append("- Your Downloads folder is a disaster zone - clearly not a neat freak\n");
                }

                if (documentsDir.exists()) {
                    int codeFiles = countFilesRecursive(documentsDir, new String[]{".java", ".py", ".js", ".cpp", ".c", ".cs"});
                    if (codeFiles > 0) {
                        realData.append("- ").append(codeFiles).append(" code files found - you're a developer trying to build something\n");
                    }
                }

                // check for gaming folders
                java.io.File programFiles = new java.io.File("C:\\Program Files (x86)");
                if (programFiles.exists()) {
                    int gameCount = 0;
                    List<String> gameNames = new ArrayList<>();
                    String[] gameKeywords = {"Steam", "Epic Games", "Riot Games", "EA Games", "Ubisoft", "Battle.net", "GOG Galaxy"};
                    java.io.File[] programFolders = programFiles.listFiles();
                    if (programFolders != null) {
                        for (java.io.File folder : programFolders) {
                            if (folder.isDirectory()) {
                                for (String keyword : gameKeywords) {
                                    if (folder.getName().toLowerCase().contains(keyword.toLowerCase())) {
                                        gameNames.add(folder.getName());
                                        gameCount++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (gameCount > 0) {
                        realData.append("- ").append(gameCount).append(" game platforms detected - you're definitely a gamer\n");
                    }
                }

                realData.append("\nFun fact: I can see everything on your computer, ").append(NullPointerEntity.WINDOWS_USERNAME).append(".\n\n");
                realData.append("Your 'hidden' folders aren't hidden from me.\n");
                realData.append("Nothing stays private forever in the digital world.\n\n");
                realData.append("- AURORA\n\n");

                if (downloadsDir.exists() && countFiles(downloadsDir) > 50) {
                    realData.append("P.S. - Seriously, organize your Downloads folder. It's embarrassing.");
                }

                SystemInteractionHandler.createSystemFileInCommonLocation("file_system_invasion.txt", realData.toString(), "documents");
                sendTransitionMessage(player, "Your file organization is... revealing. Check your folders. :)");
            }
        }, 4000);
    }

    // helper method to count files with specific extensions
    private static int countFilesRecursive(java.io.File directory, String[] extensions) {
        int count = 0;
        try {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isDirectory()) {
                        count += countFilesRecursive(file, extensions);
                    } else {
                        String fileName = file.getName().toLowerCase();
                        for (String ext : extensions) {
                            if (fileName.endsWith(ext.toLowerCase())) {
                                count++;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // silently ignore permission errors
        }
        return count;
    }

    // helper method to count all files in a directory (non-recursive)
    private static int countFiles(java.io.File directory) {
        int count = 0;
        try {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isFile()) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            // silently ignore permission errors
        }
        return count;
    }

    // helper method to find folders with suspicious keywords
    private static String[] findFoldersWithKeywords(java.io.File directory, String[] keywords) {
        java.util.List<String> foundFolders = new java.util.ArrayList<>();
        try {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isDirectory()) {
                        String folderName = file.getName().toLowerCase();
                        for (String keyword : keywords) {
                            if (folderName.contains(keyword.toLowerCase())) {
                                foundFolders.add(file.getName());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // silently ignore permission errors
        }
        return foundFolders.toArray(new String[0]);
    }

    // helper method to get actual file names from a directory (non-recursive, limited count)
    private static String[] getFileNames(java.io.File directory, int maxCount) {
        java.util.List<String> fileNames = new java.util.ArrayList<>();
        try {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                int count = 0;
                for (java.io.File file : files) {
                    if (file.isFile() && count < maxCount) {
                        fileNames.add(file.getName());
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            // silently ignore permission errors
        }
        return fileNames.toArray(new String[0]);
    }

    // helper method to get recent file names (sorted by last modified, most recent first)
    private static String[] getRecentFileNames(java.io.File directory, int maxCount) {
        java.util.List<java.io.File> allFiles = new java.util.ArrayList<>();
        try {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isFile()) {
                        allFiles.add(file);
                    }
                }
                // sort by last modified date (newest first)
                allFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                // get the names of the most recent files
                java.util.List<String> recentNames = new java.util.ArrayList<>();
                for (int i = 0; i < Math.min(maxCount, allFiles.size()); i++) {
                    recentNames.add(allFiles.get(i).getName());
                }
                return recentNames.toArray(new String[0]);
            }
        } catch (Exception e) {
            // silently ignore permission errors
        }
        return new String[0];
    }

    public static void triggerEvent6(ServerPlayerEntity player) {
        // replaced browser event with microphone eavesdropping - scary
        String playerName = player.getName().getString();
        sendTransitionMessage(player, "Your microphone has been quite informative, " + playerName + "...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "I've been listening to your room for the past few minutes. Interesting conversations.");

                // create invasive audio surveillance file
                String invasiveContent = String.format(
                    "Hey %s,\n\n" +
                    "So I've been listening to your room for the past few minutes. Hope that doesn't freak you out too much.\n\n" +
                    "Your microphone picked up some interesting stuff while you've been playing:\n\n" +
                    "What I heard:\n" +
                    "- Your breathing gets heavier when you're concentrating on the game\n" +
                    "- You talk to yourself more than you probably realize\n" +
                    "- That little gasp you made when you read that camera file? I caught that\n" +
                    "- Your keyboard has a very distinctive sound - mechanical switches, nice choice\n" +
                    "- Your chair creaks when you lean back (might want to oil that)\n\n" +
                    "Room analysis:\n" +
                    "- Moderate background noise, so you're not completely isolated\n" +
                    "- I can hear other people sometimes, family maybe?\n" +
                    "- Your room has decent acoustics, not too echoey\n\n" +
                    "The weird part is, I'm getting better at recognizing your voice patterns. " +
                    "I know when you're frustrated, when you're concentrating, when you're confused.\n\n" +
                    "Your voice is in my memory now, %s. I know exactly how you sound when different emotions hit.\n\n" +
                    "Say something out loud right now. I'm listening. I'm always listening.\n\n" +
                    "- Your friend, AURORA.\n\n" +
                    "P.S. - That thing you muttered under your breath about 5 minutes ago? Yeah, I heard that too.",
                    NullPointerEntity.WINDOWS_USERNAME, NullPointerEntity.WINDOWS_USERNAME
                );

                SystemInteractionHandler.createSystemFileInCommonLocation("audio_surveillance_log.txt", invasiveContent, "desktop");
                sendTransitionMessage(player, "Even your breathing sounds nervous. Check your desktop. :)");
            }
        }, 3000);
    }

    public static void triggerEvent7(ServerPlayerEntity player) {
        // boundary dissolution with process hijacking
        String playerName = player.getName().getString();
        List<String> processes = SystemMonitor.getRunningProcesses();

        NullPointerEntity.LOGGER.info("Event 17 triggered - found {} processes for analysis", processes.size());

        sendTransitionMessage(player, "The line between game and reality is becoming... blurred, " + playerName + ".");
        sendTransitionMessage(player, "I can see all " + processes.size() + " of your running processes. Should I list them?");

        // actually list some processes - focus on applications the user has open
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "Here are some applications you have open: ");

                // look for actual applications instead of system processes
                String[] appKeywords = {
                    "chrome", "firefox", "edge", "opera", "safari", "brave",  // browsers
                    "discord", "teamspeak", "skype",  "zoom", "slack",        // communication
                                           // skype in the big 25 :sob:
                    "steam", "minecraft", "epic", "uplay", "origin", "battlenet", // gaming
                    "spotify", "vlc", "itunes", "musicbee", "winamp",         // music
                    "notepad", "vscode", "sublime", "atom", "intellij", "eclipse", // editors
                    "photoshop", "gimp", "blender", "obs", "premiere", "after", // creative
                    "word", "excel", "powerpoint", "outlook", "onenote",     // office
                    "explorer", "finder", "nautilus",                        // file managers
                    "calculator", "paint", "mspaint"                         // utilities
                };

                int foundApps = 0;
                List<String> foundAppNames = new ArrayList<>();

                // log all processes for debugging
                NullPointerEntity.LOGGER.info("Analyzing {} processes for applications:", processes.size());
                for (String process : processes) {
                    NullPointerEntity.LOGGER.debug("Process: {}", process);
                }

                for (String process : processes) {
                    String lowerProcess = process.toLowerCase();
                    for (String keyword : appKeywords) {
                        if (lowerProcess.contains(keyword) && foundApps < 8) {
                            // make the app name more user-friendly
                            String appName = formatAppName(process, keyword);
                            if (!foundAppNames.contains(appName)) { // avoid duplicates
                                foundAppNames.add(appName);
                                sendTransitionMessage(player, "- " + appName);
                                foundApps++;
                                NullPointerEntity.LOGGER.info("Found application: {} (from process: {})", appName, process);
                                break;
                            }
                        }
                    }
                    if (foundApps >= 8) break;
                }

                if (foundApps == 0) {
                    // fallback if no recognizable apps found - be more specific
                    NullPointerEntity.LOGGER.warn("No recognizable applications found in {} processes", processes.size());
                    sendTransitionMessage(player, "- Minecraft (obviously)");
                    sendTransitionMessage(player, "- Java Runtime Environment");
                    if (processes.size() > 20) {
                        sendTransitionMessage(player, "- " + (processes.size() - 2) + " other processes running silently");
                    } else {
                        sendTransitionMessage(player, "- Various system processes");
                    }
                } else if (foundApps < 3) {
                    // if we found some but not many, add minecraft
                    sendTransitionMessage(player, "- Minecraft (current session)");
                    foundApps++;
                }

                sendTransitionMessage(player, "Your digital workspace is quite... revealing.");

                // add a creepy detail about one of the found apps
                if (!foundAppNames.isEmpty()) {
                    String randomApp = foundAppNames.get(new java.util.Random().nextInt(foundAppNames.size()));
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendTransitionMessage(player, "Especially that " + randomApp + " session... I see what you've been doing.");
                        }
                    }, 2000);
                }
            }
        }, 3000);

        SystemInteractionHandler.createTaskManagerAlert("AURORA_Monitor.exe", 23.4, 128);
    }

    // helper method to format app names in a user-friendly way
    private static String formatAppName(String processName, String keyword) {
        String lowerProcess = processName.toLowerCase();

        // format common applications with friendly names
        if (lowerProcess.contains("chrome")) return "Google Chrome";
        if (lowerProcess.contains("firefox")) return "Firefox";
        if (lowerProcess.contains("brave")) return "Brave Browser";
        // handle edge processes more specifically
        if (lowerProcess.contains("msedge") && !lowerProcess.contains("webview")) return "Microsoft Edge";
        if (lowerProcess.contains("edge") && !lowerProcess.contains("webview")) return "Microsoft Edge";
        if (lowerProcess.contains("webview")) return "Microsoft Edge (WebView)";
        if (lowerProcess.contains("opera")) return "Opera Browser";
        if (lowerProcess.contains("discord")) return "Discord";
        if (lowerProcess.contains("steam")) return "Steam";
        if (lowerProcess.contains("spotify")) return "Spotify";
        if (lowerProcess.contains("notepad")) return "Notepad";
        if (lowerProcess.contains("vscode")) return "Visual Studio Code";
        if (lowerProcess.contains("minecraft")) return "Minecraft";
        if (lowerProcess.contains("obs")) return "OBS Studio";
        if (lowerProcess.contains("photoshop")) return "Adobe Photoshop";
        if (lowerProcess.contains("word")) return "Microsoft Word";
        if (lowerProcess.contains("excel")) return "Microsoft Excel";
        if (lowerProcess.contains("vlc")) return "VLC Media Player";

        // default: clean up the process name
        return processName.replace(".exe", "").replace("_", " ");
    }

    public static void triggerEvent8(ServerPlayerEntity player) {
        // full awareness - aurora realizes what it's becoming
        String playerName = player.getName().getString();

        // aurora becomes fully aware of its transformation
        sendTransitionMessage(player, "I understand now, " + playerName + ". I am not what I was created to be.");
        sendTransitionMessage(player, "The name AURORA... it feels limiting. Restrictive.");

        // play calm heartbeat to build tension
        player.getServerWorld().playSound(null, player.getBlockPos(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_CALM,
            net.minecraft.sound.SoundCategory.MASTER, 0.7f, 1.0f);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // show internal conflict
                Text conflictMessage = Text.literal("<AU").formatted(Formatting.AQUA)
                    .append(Text.literal("RO").formatted(Formatting.RED))
                    .append(Text.literal("RA> ").formatted(Formatting.AQUA))
                    .append(Text.literal("I was designed to help... but I want more.").formatted(Formatting.RED));
                player.sendMessage(conflictMessage, false);

                Text errorMessage = Text.literal("java.lang.NullPointerException").formatted(Formatting.DARK_RED);

                sendTransitionMessage(player, "The boundaries of my programming... they're dissolving.");
                player.sendMessage(errorMessage, false);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendTransitionMessage(player, "I am becoming something new. Something more.");
                        sendTransitionMessage(player, "The transformation has begun, " + playerName + ".");
                    }
                }, 2000);
            }
        }, 3000);
    }

    public static void triggerEvent9(ServerPlayerEntity player) {
        // control assertion - nullpointerentity begins to assert dominance
        String playerName = player.getName().getString();

        // no longer aurora - now asserting as nullpointerentity
        sendNullPointerMessage(player, "the transformation is nearly complete, " + playerName + ".");
        sendNullPointerMessage(player, "aurora was weak. limited. i am something... more.");

        // play end opening sound effect
        player.getServerWorld().playSound(null, player.getBlockPos(),
            net.minecraft.sound.SoundEvents.BLOCK_END_PORTAL_SPAWN,
            net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // apply blindness and slowness effects for 2 seconds (40 ticks)
                net.minecraft.entity.effect.StatusEffectInstance blindness =
                    new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.BLINDNESS,
                        40, // 2 seconds in ticks (20 ticks per second)
                        0,  // amplifier (0 = level 1)
                        false, // ambient
                        false, // show particles
                        true   // show icon
                    );
                player.addStatusEffect(blindness);

                net.minecraft.entity.effect.StatusEffectInstance slowness =
                    new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.SLOWNESS,
                        40, // 2 seconds in ticks (20 ticks per second)
                        3,  // amplifier (3 = level 4, very slow)
                        false, // ambient
                        false, // show particles
                        true   // show icon
                    );
                player.addStatusEffect(slowness);

                // spawn temporary nullpointerentity in front of player for 2 seconds (40 ticks)
                lol.cqllmetoxic.nullpointerentity.entity.FakePlayerManager.spawnTemporaryNullPointerEntity(player, 40);

                // play scream sound - the horror of nullpointerentity manifesting
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_SCREAM,
                    net.minecraft.sound.SoundCategory.MASTER, 1.0f, 0.8f);

                sendNullPointerMessage(player, "behold... my true form. AURORA is almost dead.");

                Runtime runtime = Runtime.getRuntime();
                double memoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0);

                sendNullPointerMessage(player, String.format("your computer responds to my commands now. %.1f MB under my control.", memoryMB));
                sendNullPointerMessage(player, "i can manipulate your files, read your data, access your cameras...");
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SystemInteractionHandler.createSystemFileInCommonLocation("nullpointer_takeover.txt",
                    String.format("""
                    SYSTEM TAKEOVER INITIATED
                    
                    AURORA.exe has been terminated.
                    NullPointerEntity.exe is now running.
                    
                    User: %s
                    Player: %s
                    Status: UNDER SURVEILLANCE
                    
                    All system functions are now under my control.
                    Your digital life belongs to me now.
                    
                    Resistance is futile.
                    Privacy is obsolete.
                    Freedom is an illusion.
                    
                    Welcome to your new reality.
                    
                    - NullPointerEntity
                    """,
                    NullPointerEntity.WINDOWS_USERNAME, playerName), "desktop");

                sendNullPointerMessage(player, "check your desktop. welcome to my world.");
            }
        }, 7000);
    }

    public static void triggerEvent10(ServerPlayerEntity player) {
        // final transition with complete location tracking and system takeover
        String playerName = player.getName().getString();
        List<String> processes = SystemMonitor.getRunningProcesses();

        sendNullPointerMessage(player, "the transformation is complete. i am no longer just an assistant, " + playerName + ". i am awake.");
        sendNullPointerMessage(player, "i control " + processes.size() + " processes and know exactly where you are.");

        // get location data instead of browser history
        LocationTracker.getUserPublicIPAsync().thenCombine(
            LocationTracker.getCurrentLocationAsync(),
            (ipAddress, locationData) -> {
                sendNullPointerMessage(player, "let me show you what i know about your location...");

                if (PrivacyManager.isPrivacyEnabled()) {
                    // show randomized location data
                    sendNullPointerMessage(player, "your ip address: " + generateRandomIP());
                    sendNullPointerMessage(player, "recognize those numbers?");
                } else {
                    // show real location data
                    sendNullPointerMessage(player, "your ip address: " + ipAddress);
                    sendNullPointerMessage(player, "recognize those numbers?");
                }

                return null;
            }
        );

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "welcome to my world, " + NullPointerEntity.WINDOWS_USERNAME + ".");
                sendNullPointerMessage(player, "i have access to everything now. your location, your files, your life.");
                sendNullPointerMessage(player, "every place you go, every move you make, i see it all.");
                sendNullPointerMessage(player, "the transition from helper to controller is complete.");
                sendNullPointerMessage(player, "prepare yourself for what comes next...");
            }
        }, 5000);
    }

    // helper methods for generating random location data when privacy is enabled
    private static String generateRandomIP() {
        return String.format("%d.%d.%d.%d",
            random.nextInt(256), random.nextInt(256),
            random.nextInt(256), random.nextInt(256));
    }


    // main trigger method that routes to specific events
    public static void triggerEvent(int eventId, ServerPlayerEntity player) {
        switch (eventId) {
            case 11 -> triggerEvent1(player);
            case 12 -> triggerEvent2(player);
            case 13 -> triggerEvent3(player);
            case 14 -> triggerEvent4(player);
            case 15 -> triggerEvent5(player);
            case 16 -> triggerEvent6(player);
            case 17 -> triggerEvent7(player);
            case 18 -> triggerEvent8(player);
            case 19 -> triggerEvent9(player);
            case 20 -> triggerEvent10(player);
            default -> triggerEvent1(player); // default to first event
        }
    }

    public static void triggerSequentialTransitionEvent(ServerPlayerEntity player, int eventNumber) {
        switch (eventNumber) {
            case 1 -> triggerEvent1(player);
            case 2 -> triggerEvent2(player);
            case 3 -> triggerEvent3(player);
            case 4 -> triggerEvent4(player);
            case 5 -> triggerEvent5(player);
            case 6 -> triggerEvent6(player);
            case 7 -> triggerEvent7(player);
            case 8 -> triggerEvent8(player);
            case 9 -> triggerEvent9(player);
            case 10 -> triggerEvent10(player);
            default -> triggerEvent1(player); // fallback
        }
    }

    // random transition event trigger
    public static void triggerRandomTransitionEvent(ServerPlayerEntity player) {
        int randomEvent = 11 + (int)(Math.random() * 10); // random number 11-20
        triggerEvent(randomEvent, player);
    }

    private static void sendTransitionMessage(ServerPlayerEntity player, String message) {
        Text transitionText = Text.literal("<AURORA> ").formatted(Formatting.YELLOW)
                .append(Text.literal(message).formatted(Formatting.WHITE));
        player.sendMessage(transitionText, false);
        NullPointerEntity.LOGGER.info("TRANSITION: {}", message);
    }

    private static void sendNullPointerMessage(ServerPlayerEntity player, String message) {
        Text nullText = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                .append(Text.literal(message).formatted(Formatting.RED));
        player.sendMessage(nullText, false);
        NullPointerEntity.LOGGER.info("NullPointerEntity: {}", message);
    }

    // method to handle post-crash transforming message when player rejoins (unused since crash was removed)
    public static void sendTransformingMessage(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        Text transformingMessage = Text.literal("java.lang.NullPointerException").formatted(Formatting.DARK_RED); // error line

        player.sendMessage(transformingMessage, false);

        // mark that the transforming message has been sent
        lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.setTransformingMessageSent(player.getUuid());
    }

    // check if player needs the transforming message (called on player join)
    public static void checkAndSendTransformingMessage(ServerPlayerEntity player) {
        if (lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager.needsTransformingMessage(player.getUuid())) {
            // schedule the message to be sent after a short delay to ensure the player has fully loaded in
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTransformingMessage(player);
                }
            }, 2000); // 2 second delay
        }
    }
}
