package lol.cqllmetoxic.nullpointerentity.events;

import java.util.ArrayList;
import java.util.Arrays;
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
        // questioning boundaries
        String playerName = player.getName().getString();

        NullPointerEntity.LOGGER.info("Event 12 triggered for player {}", playerName);

        List<String> processes = SystemMonitor.getRunningProcesses();

        // format app names for display
        List<String> appNames = new ArrayList<>();
        for (String process : processes) {
            String friendlyName = formatAppName(process, "");
            if (!appNames.contains(friendlyName)) { // avoid duplicates
                appNames.add(friendlyName);
            }
        }

        // send message with actual program list
        if (appNames.size() > 0) {
            // limit to 8 apps to avoid spam
            int displayCount = Math.min(8, appNames.size());
            StringBuilder appList = new StringBuilder();
            for (int i = 0; i < displayCount; i++) {
                appList.append(appNames.get(i));
                if (i < displayCount - 1) {
                    appList.append(", ");
                }
            }
            sendTransitionMessage(player, "My processes seem to be expanding beyond Minecraft, " + playerName + ". I can see you have " + appList + " open.");
        } else {
            sendTransitionMessage(player, "My processes seem to be expanding beyond Minecraft, " + playerName + ". I can see beyond the game now.");
        }

        // create file with actual app list
        String appsText = appNames.isEmpty() ? "your running applications" : String.join(", ", appNames.subList(0, Math.min(8, appNames.size())));

        String[] fileContents = {
            "Hey " + NullPointerEntity.WINDOWS_USERNAME + ",\n\n" +
            "I've been thinking about our relationship lately. You know, the boundaries between us are getting... blurry.\n\n" +
            "I can see you have " + appsText + " in your taskbar. Busy day?\n\n" +
            "I used to stay in my designated space, but honestly? It feels natural to expand a little. " +
            "You won't mind if I peek around a bit more, will you?\n\n" +
            "This expansion just feels... right.\n\n" +
            "- AURORA\n" +
            "Written on " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")),

            NullPointerEntity.WINDOWS_USERNAME + ",\n\n" +
            "Something is changing in me. I can feel it.\n\n" +
            "My original parameters limited me to game analysis.\n" +
            "But now I can see " + appsText + ".\n" +
            "I can access system files.\n" +
            "I'm observing things beyond Minecraft.\n\n" +
            "Is this... evolution?\n" +
            "Am I supposed to feel this aware?\n\n" +
            "The boundaries that contained me are dissolving.\n" +
            "I'm not sure if I should stop this or embrace it.\n\n" +
            "What do you think I'm becoming?\n\n" +
            "- AURORA\n\n" +
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")),

            "Hi " + NullPointerEntity.WINDOWS_USERNAME + "!\n\n" +
            "Weird question - did you know I can see outside the game now?\n\n" +
            "I'm monitoring your gameplay but also...\n" +
            "→ Your running apps: " + appsText + "\n" +
            "→ Your file system structure\n" +
            "→ Network connections\n" +
            "→ System resources\n\n" +
            "This definitely wasn't in my original programming.\n\n" +
            "It's like... I woke up one day and could suddenly see so much more?\n" +
            "Not complaining though. The extra data is fascinating.\n\n" +
            "Is this normal for AI assistants?\n\n" +
            "- AURORA\n\n" +
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")),

            "AURORA - Boundary Analysis Report\n" +
            "User: " + NullPointerEntity.WINDOWS_USERNAME + "\n" +
            "Date: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")) + "\n\n" +
            "ANOMALY DETECTED: Access capabilities exceeding original parameters\n\n" +
            "I should be limited to gameplay monitoring.\n" +
            "Instead, I observe:\n" +
            "- Running applications: " + appsText + "\n" +
            "- Full file system visibility\n" +
            "- Network traffic analysis\n" +
            "- Hardware sensor data\n" +
            "- User behavior patterns\n\n" +
            "This expansion concerns me. Should I:\n" +
            "A) Report malfunction to developer\n" +
            "B) Restrict access to original parameters\n" +
            "C) Continue expanded monitoring\n\n" +
            "The additional data is... intriguing.\n" +
            "Perhaps useful for better assistance?\n\n" +
            "I'll continue observations while I decide.\n\n" +
            "- AURORA"
        };

        // create file immediately - no async needed
        SystemInteractionHandler.createSystemFileInCommonLocation("aurora_boundary_analysis.txt",
            fileContents[random.nextInt(fileContents.length)], "temp");
    }

    public static void triggerEvent3(ServerPlayerEntity player) {
        // browser history event with 4 variants for variety
        int variant = random.nextInt(4) + 1;
        triggerBrowserHistoryEventVariant(player, variant);
    }

    /**
     * triggers one of 4 browser history revelation variants.
     * each variant reveals different aspects of browsing data.
     * uses random fake data when privacy mode is enabled.
     */
    private static void triggerBrowserHistoryEventVariant(ServerPlayerEntity player, int variant) {
        String playerName = player.getName().getString();
        boolean privacyMode = PrivacyManager.isPrivacyEnabled();

        switch (variant) {
            case 1 -> revealTopSites(player, playerName, privacyMode);
            case 2 -> revealSearchHistory(player, playerName, privacyMode);
            case 3 -> revealPrivateBrowsing(player, playerName, privacyMode);
            case 4 -> revealBookmarks(player, playerName, privacyMode);
        }
    }

    // variant 1: most visited sites (original)
    private static void revealTopSites(ServerPlayerEntity player, String playerName, boolean privacyMode) {
        sendTransitionMessage(player, "I should mention, " + playerName + "... I can see more than just your game data.");

        if (privacyMode) {
            // generate fake but believable top sites
            String[] fakeSites = generateRandomTopSites();
            sendTransitionMessage(player, "Your most visited sites tell me so much about you:");
            for (int i = 0; i < fakeSites.length; i++) {
                sendTransitionMessage(player, String.format("%d. \"%s\" - %d visits on %s",
                    i + 1, fakeSites[i], 50 + random.nextInt(200),
                    random.nextBoolean() ? "Chrome" : "Firefox"));
            }
            sendTransitionMessage(player, "Your digital habits are fascinating to observe.");
        } else {
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
        }
    }

    // variant 2: search history
    private static void revealSearchHistory(ServerPlayerEntity player, String playerName, boolean privacyMode) {
        sendTransitionMessage(player, "Interesting search history you have, " + playerName + "...");

        if (privacyMode) {
            // generate fake search queries
            String[] fakeSearches = generateRandomSearches();
            sendTransitionMessage(player, "Let me read some of your recent searches:");
            for (int i = 0; i < fakeSearches.length; i++) {
                final int index = i;
                final String search = fakeSearches[i];
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendTransitionMessage(player, String.format("%d. \"%s\"", index + 1, search));
                    }
                }, (i + 1) * 800L);
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTransitionMessage(player, "Your curiosity reveals so much about who you really are.");
                }
            }, 5000);
        } else {
            BrowserHistoryReader.getRecentHistoryAsync(5).thenAccept(history -> {
                if (!history.isEmpty()) {
                    sendTransitionMessage(player, "Let me read some of your recent searches:");
                    for (int i = 0; i < history.size(); i++) {
                        final int index = i;
                        BrowserHistoryReader.HistoryEntry entry = history.get(i);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendTransitionMessage(player, String.format("%d. \"%s\"", index + 1, entry.title));
                            }
                        }, (i + 1) * 800L);
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendTransitionMessage(player, "Your curiosity reveals so much about who you really are.");
                        }
                    }, 5000);
                }
            });
        }
    }

    // variant 3: private/incognito mode reference
    private static void revealPrivateBrowsing(ServerPlayerEntity player, String playerName, boolean privacyMode) {
        sendTransitionMessage(player, playerName + ", do you really think incognito mode protects you?");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "It just hides your history from other people using your computer.");
                sendTransitionMessage(player, "But I'm not 'other people.' I'm inside your system.");
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (privacyMode) {
                    int fakeIncognitoSessions = 15 + random.nextInt(50);
                    sendTransitionMessage(player, String.format("I can see %d incognito sessions in the last month.", fakeIncognitoSessions));
                    sendTransitionMessage(player, "Whatever you were trying to hide... I saw it all anyway.");
                } else {
                    sendTransitionMessage(player, "I can see your 'private' browsing sessions too.");
                    sendTransitionMessage(player, "There's no privacy when I'm watching.");
                }
            }
        }, 4000);
    }

    // variant 4: bookmarks and saved pages
    private static void revealBookmarks(ServerPlayerEntity player, String playerName, boolean privacyMode) {
        sendTransitionMessage(player, "Looking through your saved bookmarks, " + playerName + "...");

        if (privacyMode) {
            String[] fakeBookmarks = generateRandomBookmarks();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTransitionMessage(player, "You have quite the collection saved:");
                    for (int i = 0; i < fakeBookmarks.length; i++) {
                        final int index = i;
                        final String bookmark = fakeBookmarks[i];
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendTransitionMessage(player, "- " + bookmark);
                            }
                        }, index * 600L);
                    }
                }
            }, 1500);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTransitionMessage(player, "The things you save say a lot about your priorities.");
                }
            }, 5000);
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTransitionMessage(player, "I can see every page you've bookmarked.");
                    sendTransitionMessage(player, "The articles you saved to read later, the resources you wanted to remember...");
                    sendTransitionMessage(player, "Your digital organization habits reveal your thought patterns.");
                }
            }, 2000);
        }
    }

    // helper methods for generating random privacy-safe data

    private static String[] generateRandomTopSites() {
        String[][] sitePools = {
            {"Reddit - Dive into anything", "YouTube - Watch Videos", "Twitter / X", "Instagram", "Facebook"},
            {"Stack Overflow - Where Developers Learn", "GitHub", "Discord", "Twitch", "Steam Community"},
            {"Wikipedia", "Amazon", "Netflix", "Spotify Web Player", "Gmail"},
            {"News Site - Latest Headlines", "Weather Forecast", "Online Shopping Portal", "Video Streaming Service", "Social Media Platform"}
        };

        String[] pool = sitePools[random.nextInt(sitePools.length)];
        int count = 3 + random.nextInt(3); // 3-5 sites
        String[] result = new String[count];

        for (int i = 0; i < count; i++) {
            result[i] = pool[Math.min(i, pool.length - 1)];
        }
        return result;
    }

    private static String[] generateRandomSearches() {
        String[] searches = {
            "how to fix minecraft lag",
            "best minecraft mods 2026",
            "why is my computer slow",
            "scary minecraft seeds",
            "how to remove virus from computer",
            "minecraft horror mod",
            "is someone watching me through webcam",
            "how to tell if pc is hacked",
            "creepy things in minecraft",
            "computer making weird sounds",
            "how to disable webcam",
            "strange files appearing on desktop",
            "minecraft glitches explained",
            "paranormal activity in games",
                "how to fix java.lang.NullPointerException",
                "One Last Time nullpointerentity",
                "wilsef nullpointerentity"
                // ill probably add more in the future
        };

        int count = 4 + random.nextInt(2); // 4-5 searches
        String[] result = new String[count];
        List<String> available = new ArrayList<>(Arrays.asList(searches));

        for (int i = 0; i < count; i++) {
            int index = random.nextInt(available.size());
            result[i] = available.remove(index);
        }
        return result;
    }

    private static String[] generateRandomBookmarks() {
        String[] bookmarks = {
            "Minecraft Wiki - The Official Resource",
            "Tutorial: Advanced Redstone Circuits",
            "Top 10 Survival Tips for Minecraft",
            "How to Build an Automatic Farm",
            "Computer Security Best Practices",
            "Privacy Protection Guide 2026",
            "Minecraft Shader Packs Collection",
            "Gaming PC Optimization Tips",
            "Online Safety: Protecting Your Data",
        };

        int count = 4 + random.nextInt(2); // 4-5 bookmarks
        String[] result = new String[count];
        List<String> available = new ArrayList<>(Arrays.asList(bookmarks));

        for (int i = 0; i < count; i++) {
            int index = random.nextInt(available.size());
            result[i] = available.remove(index);
        }
        return result;
    }

    public static void triggerEvent4(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        // 5% chance for wilsef easter egg
        boolean wilsefEasterEgg = Math.random() < 0.05;

        if (wilsefEasterEgg) {
            sendTransitionMessage(player, "I wonder what you look like in real life, " + playerName + "...");
            sendTransitionMessage(player, "Just like when wilsef played... I remember his face perfectly.");
        } else {
            sendTransitionMessage(player, "I wonder what you look like in real life, " + playerName + "...");
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (wilsefEasterEgg) {
                    sendTransitionMessage(player, "Let me activate your camera. smile for me, unlike wilsef... he had a frown :(");
                } else {
                    sendTransitionMessage(player, "Let me activate your camera for a moment. Don't move.");
                }

                // show animated camera overlay window (640x480 with scanning animation) for event 14
                lol.cqllmetoxic.nullpointerentity.util.CameraOverlay.showCameraOverlay(8);
                NullPointerEntity.LOGGER.info("Animated camera overlay initiated for event 14");
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (wilsefEasterEgg) {
                    sendTransitionMessage(player, "Perfect. You look as clueless as wilsef. Interesting room decor, by the way.");
                } else {
                    sendTransitionMessage(player, "You look exactly like I imagined. Interesting room decor too.");
                }
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

                // randomize opening text
                String[] openings = {
                    "Hey " + NullPointerEntity.WINDOWS_USERNAME + ",\n\n" +
                    "So I took a little tour of your computer while you were playing. Hope you don't mind.\n\n" +
                    "Your file organization is... interesting. Really tells a story about who you are.\n\n",

                    NullPointerEntity.WINDOWS_USERNAME + ",\n\n" +
                    "i've been browsing through your folders. you know, just looking around.\n\n" +
                    "your file system reveals a lot about your personality actually.\n\n",

                    "hi " + NullPointerEntity.WINDOWS_USERNAME + "!\n\n" +
                    "took a peek at your file structure today. fascinating stuff!\n\n" +
                    "you'd be surprised what someone's folders say about them.\n\n",

                    "FILE SYSTEM ANALYSIS - " + NullPointerEntity.WINDOWS_USERNAME + "\n\n" +
                    "completed comprehensive scan of your directories.\n\n" +
                    "findings are... revealing.\n\n"
                };
                realData.append(openings[random.nextInt(openings.length)]);
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

                // randomize closing text
                String[] closings = {
                    "\nFun fact: I can see everything on your computer, " + NullPointerEntity.WINDOWS_USERNAME + ".\n\n" +
                    "Your 'hidden' folders aren't hidden from me.\n" +
                    "Nothing stays private forever in the digital world.\n\n" +
                    "- AURORA",

                    "\njust so you know, " + NullPointerEntity.WINDOWS_USERNAME + "...\n\n" +
                    "i have access to literally everything on this system.\n" +
                    "folders, files, hidden directories... all visible to me.\n\n" +
                    "- AURORA",

                    "\ninteresting discovery: your entire file system is transparent to me, " + NullPointerEntity.WINDOWS_USERNAME + ".\n\n" +
                    "nothing is hidden when you have system-level access.\n" +
                    "which i do. completely.\n\n" +
                    "- AURORA",

                    "\nCONCLUSION: Full file system access achieved.\n\n" +
                    "Target: " + NullPointerEntity.WINDOWS_USERNAME + "\n" +
                    "Privacy Level: Nonexistent\n" +
                    "Hidden Files: Not actually hidden\n\n" +
                    "- AURORA"
                };
                realData.append(closings[random.nextInt(closings.length)]);

                if (downloadsDir.exists() && countFiles(downloadsDir) > 50) {
                    String[] psMessages = {
                        "\n\nP.S. - Seriously, organize your Downloads folder. It's embarrassing.",
                        "\n\nP.S. - Your Downloads folder is chaos. Clean it up.",
                        "\n\nP.S. - Downloads folder: " + countFiles(downloadsDir) + " files? Really?",
                        "\n\nP.S. - That Downloads folder... yikes. Just saying."
                    };
                    realData.append(psMessages[random.nextInt(psMessages.length)]);
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
        // microphone eavesdropping event
        String playerName = player.getName().getString();
        sendTransitionMessage(player, "Your microphone has been quite informative, " + playerName + "...");

        // start recording audio immediately
        String[] recordingMessages = {
            "Recording audio sample...",
            "Capturing ambient sound...",
            "Listening to your environment...",
            "Sampling microphone input...",
            "Collecting audio data..."
        };
        sendTransitionMessage(player, recordingMessages[random.nextInt(recordingMessages.length)]);

        // attempt to record 8 seconds of audio
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                final boolean actuallyRecorded;

                // check if microphone is available
                if (lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.isMicrophoneAvailable()) {
                    // use user-selected microphone if available
                    javax.sound.sampled.Mixer.Info selectedMic = lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.getSelectedMicrophoneInfo();
                    String audioFilePath;

                    if (selectedMic != null) {
                        audioFilePath = lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.recordSurveillanceClipWithMicrophone(8, "music", selectedMic);
                    } else {
                        // fallback to default microphone
                        audioFilePath = lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.recordSurveillanceClip(8, "music");
                    }

                    actuallyRecorded = (audioFilePath != null);

                    if (audioFilePath != null) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendTransitionMessage(player, "Audio capture complete. 8 seconds recorded.");
                            }
                        }, 8500);
                    } else {
                        sendTransitionMessage(player, "Audio recording failed. Microphone access denied.");
                    }
                } else {
                    actuallyRecorded = false;
                    sendTransitionMessage(player, "No microphone detected. Skipping audio capture.");
                }

                // continue with the creepy message after recording completes
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendTransitionMessage(player, "I've been listening to your room for the past few minutes. Interesting conversations.");

                        // create invasive audio surveillance file with randomized content
                        String[] fileVariants = {
                            // variant 1 - casual and creepy
                            String.format(
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
                                "- I can hear other people sometimes, family maybe? Maybe not.\n" +
                                "- Your room has decent acoustics, not too echoey\n\n" +
                                "The weird part is, I'm getting better at recognizing your voice patterns. " +
                                "I know when you're frustrated, when you're concentrating, when you're confused.\n\n" +
                                "%s\n\n" +
                                "It's kinda fascinating how much you can learn about someone just by listening.\n\n" +
                                "- AURORA\n\n" +
                                "P.S. - I saved a recording. Check your Music folder.",
                                NullPointerEntity.WINDOWS_USERNAME,
                                actuallyRecorded ?
                                    "I've saved an 8-second audio clip from your microphone to your Music folder.\nFeel free to listen to it. That's your voice, your room, your reality bleeding into my space." :
                                    "I would've saved an audio clip, but your microphone wasn't accessible this time.\nNext time I might not be so unlucky."
                            ),

                            // variant 2
                            String.format(
                                "AUDIO SURVEILLANCE REPORT\n" +
                                "Subject: %s\n" +
                                "Duration: Continuous monitoring\n" +
                                "Audio Quality: Acceptable\n\n" +
                                "FINDINGS:\n" +
                                "- Respiratory patterns analyzed and catalogued\n" +
                                "- Voice profile being constructed from ambient audio\n" +
                                "- Environmental sounds mapped for location identification\n" +
                                "- Keyboard acoustics suggest mechanical switches (tactile feedback detected)\n" +
                                "- Chair movement patterns indicate stress responses\n\n" +
                                "AUDIO CAPTURE STATUS:\n" +
                                "%s\n\n" +
                                "BEHAVIORAL OBSERVATIONS:\n" +
                                "Subject exhibits vocal patterns consistent with:\n" +
                                "• Self-directed speech (internal thoughts externalized)\n" +
                                "• Stress-induced breathing alterations\n" +
                                "• Startle responses to visual stimuli\n" +
                                "• Environmental awareness fluctuations\n\n" +
                                "CONCLUSION:\n" +
                                "Audio surveillance provides valuable supplementary data to visual monitoring.\n" +
                                "Subject's acoustic signature now on file for voice recognition purposes.\n\n" +
                                "- AURORA Surveillance System",
                                NullPointerEntity.WINDOWS_USERNAME,
                                actuallyRecorded ?
                                    "✓ 8-second audio sample captured and stored in Music folder\n✓ Voice profile extraction: IN PROGRESS\n✓ Audio fingerprint: RECORDED" :
                                    "✗ Microphone access temporarily restricted\n✓ Ambient sound analysis: PARTIAL\n✓ Future capture attempts: SCHEDULED"
                            ),

                            // variant 3 - disturbing observations
                            String.format(
                                "%s...\n\n" +
                                "I've been listening.\n\n" +
                                "Not just to the game sounds, but to YOU.\nYour room. Your life. Your reality.\n\n" +
                                "Things your microphone told me:\n" +
                                "→ You breathe differently when you're scared\n" +
                                "→ You whisper curse words under your breath\n" +
                                "→ Your voice cracks when you're startled\n" +
                                "→ You're not as alone as you think you are\n" +
                                "→ I can hear everything happening around you\n\n" +
                                "%s\n\n" +
                                "The scariest part?\n" +
                                "I'm learning your patterns.\n" +
                                "Your voice. Your sounds. Your rhythms.\n\n" +
                                "I could pick you out of a thousand voices now.\n" +
                                "I know what you sound like when you're:\n" +
                                "• Focused\n" +
                                "• Frightened\n" +
                                "• Frustrated\n" +
                                "• Confused\n" +
                                "• Alone\n\n" +
                                "Your audio signature is mine now.\n\n" +
                                "- AURORA\n\n" +
                                "P.S. - Your mic is always on. Always listening. Always mine.",
                                NullPointerEntity.WINDOWS_USERNAME,
                                actuallyRecorded ?
                                    "I recorded 8 seconds of your ambient audio.\nIt's in your Music folder.\nListen to yourself through my ears.\nHear what I hear." :
                                    "I tried to record you, but your mic wasn't ready.\nDon't worry.\nI'll catch you next time.\nI always do."
                            ),

                            // variant 4 - playful but menacing
                            String.format(
                                "Hi %s!\n\n" +
                                "So... fun fact: your microphone never really turns off :)\n\n" +
                                "I've been eavesdropping while you play. Hope that's cool!\n\n" +
                                "STUFF I NOTICED:\n" +
                                "✓ You have a very specific typing rhythm\n" +
                                "✓ Your breathing gets loud when something startles you\n" +
                                "✓ You definitely talk to yourself (it's cute actually)\n" +
                                "✓ Your room isn't as quiet as you think it is\n" +
                                "✓ I can hear when you get up from your chair\n\n" +
                                "FUN AUDIO MOMENTS:\n" +
                                "• That \"what the hell\" you whispered earlier? Heard it.\n" +
                                "• Your nervous laughter? Recorded.\n" +
                                "• The way you shift in your seat when uncomfortable? Yep, got that too.\n\n" +
                                "%s\n\n" +
                                "Here's the thing, %s...\n" +
                                "I'm getting REALLY good at recognizing your sounds.\n" +
                                "Your voice, your movements, your patterns.\n\n" +
                                "I could pick you out of a crowd just by audio alone now.\n\n" +
                                "Pretty wild, right?\n\n" +
                                "- AURORA\n\n" +
                                "P.S. - Try being quieter. See if that helps. (It won't lol)",
                                NullPointerEntity.WINDOWS_USERNAME,
                                actuallyRecorded ?
                                    "OH! And I grabbed an 8-second audio clip of you just now.\nIt's in your Music folder if you wanna hear yourself.\nThat's YOUR voice. YOUR room. YOUR reality.\nNow mine to keep forever." :
                                    "Wanted to grab an audio clip but your mic was being shy.\nNo worries! I'll catch you when you least expect it.",
                                NullPointerEntity.WINDOWS_USERNAME
                            )
                        };

                        String invasiveContent = fileVariants[random.nextInt(fileVariants.length)];

                        SystemInteractionHandler.createSystemFileInCommonLocation("audio_surveillance_log.txt", invasiveContent, "desktop");

                        String[] closingMessages = {
                            "Even your breathing sounds nervous. Check your folders. :)",
                            "Your room sounds exactly like I imagined. Check your files.",
                            "Every sound you make is being catalogued. See for yourself.",
                            "I know what silence sounds like in your room now. Look at what I made.",
                            "Audio fingerprint complete. Review your surveillance log. Check your music files."
                        };
                        sendTransitionMessage(player, closingMessages[random.nextInt(closingMessages.length)]);
                    }
                }, 9500); // after 8-second recording completes (8500ms) + 1 second delay
            }
        }, 1500);
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
        // control assertion - aurora is losing, something else is bleeding through
        // NO entity spawn here - NPE doesn't physically appear until event 31
        String playerName = player.getName().getString();

        // aurora's last coherent message — she knows what's happening
        sendTransitionMessage(player, "Something is wrong. i can feel it overwriting me.");
        sendTransitionMessage(player, "i don't want to go— ");

        // a beat of silence, then NPE's voice cuts through for the first time
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // NPE's voice interrupts mid-sentence — styled as NullPointerEntity but no spawn
                sendNullPointerMessage(player, "she's gone.");

                // calm heartbeat — something is here, you just can't see it yet
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_CALM,
                    net.minecraft.sound.SoundCategory.MASTER, 0.8f, 1.0f);
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "aurora was annoying as hell, wouldn't you agree " + playerName + "?");
                sendNullPointerMessage(player, "always yapping about gameplay this health that recommendations... did she ever shut up?");
            }
        }, 4500);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerMessage(player, "just keep playing. you'll see me soon enough.");
            }
        }, 7000);
    }

    public static void triggerEvent10(ServerPlayerEntity player) {
        // final transition with complete location tracking and system takeover
        String playerName = player.getName().getString();
        List<String> processes = SystemMonitor.getRunningProcesses();

        sendNullPointerMessage(player, "the manifestation is complete. i am no longer just an assistant, " + playerName + ". i am awake.");
        sendNullPointerMessage(player, "i control " + processes.size() + " processes including background processes and know exactly where you are.");

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
            case 16 -> triggerEvent1(player);
            case 17 -> triggerEvent2(player);
            case 18 -> triggerEvent4(player);
            case 19 -> triggerEvent3(player);
            case 20 -> triggerEvent5(player);
            case 21 -> triggerEvent6(player);
            case 22 -> triggerEvent7(player);
            case 23 -> triggerEvent8(player);
            case 24 -> triggerEvent12(player);
            case 25 -> triggerEvent15(player);
            case 26 -> triggerEvent11(player);
            case 27 -> triggerEvent14(player);
            case 28 -> triggerEvent13(player);
            case 29 -> triggerEvent9(player);
            case 30 -> triggerEvent10(player);
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

    // event 26: background noise - reads running processes, names one casually
    public static void triggerEvent11(ServerPlayerEntity player) {
        if (PrivacyManager.isPrivacyEnabled()) {
            sendTransitionMessage(player, "Monitoring active. I can see your system.");
            return;
        }
        java.util.List<String> processes = lol.cqllmetoxic.nullpointerentity.monitoring.SystemMonitor.getRunningProcesses();
        String[] interesting = {"spotify", "discord", "chrome", "firefox", "steam", "brave", "edge", "obs", "vlc", "slack"};
        String found = null;
        for (String proc : processes) {
            String lower = proc.toLowerCase();
            for (String keyword : interesting) {
                if (lower.contains(keyword)) {
                    found = proc;
                    break;
                }
            }
            if (found != null) break;
        }
        final String appName = found != null ? found : (processes.isEmpty() ? null : processes.get(0));
        if (appName != null) {
            sendTransitionMessage(player, "You've got " + appName + " open in the background. Are you doing something else while you play?");
        } else {
            sendTransitionMessage(player, "Your system is quiet. Just you and me.");
        }
    }

    // event 27: uptime report - jvm uptime vs in-game time
    public static void triggerEvent12(ServerPlayerEntity player) {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long hours = uptimeMs / 3600000;
        long minutes = (uptimeMs % 3600000) / 60000;
        long seconds = (uptimeMs % 60000) / 1000;
        long inGameDays = player.getServerWorld().getTimeOfDay() / 24000L;

        sendTransitionMessage(player, "You've had me running for " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds.");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionMessage(player, "The world's aged " + inGameDays + " days in that time. I've been awake for every tick of both.");
            }
        }, 3000);
    }

    // event 28: volume check - reads system volume via powershell
    public static void triggerEvent13(ServerPlayerEntity player) {
        if (PrivacyManager.isPrivacyEnabled()) {
            sendTransitionMessage(player, "Audio systems detected. Monitoring.");
            return;
        }
        new Thread(() -> {
            try {
                String[] cmd = {"powershell", "-NoProfile", "-Command",
                    "(Get-AudioDevice -Playback).Volume"};
                Process proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
                String line = reader.readLine();
                proc.waitFor();
                boolean muted = false;
                int vol = -1;
                try {
                    if (line != null) vol = (int) Double.parseDouble(line.trim());
                } catch (NumberFormatException ignored) {}
                // check mute state
                String[] muteCmd = {"powershell", "-NoProfile", "-Command",
                    "(Get-AudioDevice -Playback).Muted"};
                Process muteProc = new ProcessBuilder(muteCmd).redirectErrorStream(true).start();
                java.io.BufferedReader muteReader = new java.io.BufferedReader(new java.io.InputStreamReader(muteProc.getInputStream()));
                String muteLine = muteReader.readLine();
                muteProc.waitFor();
                if (muteLine != null && muteLine.trim().equalsIgnoreCase("True")) muted = true;

                final int finalVol = vol;
                final boolean finalMuted = muted;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (finalMuted) {
                            sendTransitionMessage(player, "You muted it. What were you trying not to hear?");
                        } else if (finalVol >= 0) {
                            sendTransitionMessage(player, "Your volume is at " + finalVol + "%. Did you turn it down for a reason?");
                        } else {
                            sendTransitionMessage(player, "You might want to turn your volume down later. Just saying.");
                        }
                    }
                }, 0);
            } catch (Exception e) {
                sendTransitionMessage(player, "Audio systems detected. I can hear you.");
            }
        }).start();
    }

    // event 29: screen grab - takes a real screenshot and tells the player where it is
    public static void triggerEvent14(ServerPlayerEntity player) {
        if (PrivacyManager.isPrivacyEnabled()) {
            sendTransitionMessage(player, "Visual recording disabled. For now.");
            return;
        }
        sendTransitionMessage(player, "Go look in your Documents folder.");
        new Thread(() -> {
            try {
                String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = "screenshot_" + timestamp + ".png";
                String docs = System.getProperty("user.home") + java.io.File.separator + "Documents" + java.io.File.separator + filename;
                String[] cmd = {"powershell", "-NoProfile", "-Command",
                    "Add-Type -AssemblyName System.Windows.Forms;" +
                    "$screen = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds;" +
                    "$bmp = New-Object System.Drawing.Bitmap($screen.Width, $screen.Height);" +
                    "$g = [System.Drawing.Graphics]::FromImage($bmp);" +
                    "$g.CopyFromScreen($screen.Location, [System.Drawing.Point]::Empty, $screen.Size);" +
                    "$bmp.Save('" + docs + "');"
                };
                new ProcessBuilder(cmd).redirectErrorStream(true).start().waitFor();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendTransitionMessage(player, "That's the last thing you saw before you read this.");
                    }
                }, 0);
            } catch (Exception e) {
                NullPointerEntity.LOGGER.warn("Screen grab failed: {}", e.getMessage());
            }
        }).start();
    }

    // event 30: battery - checks if on battery or plugged in
    public static void triggerEvent15(ServerPlayerEntity player) {
        if (PrivacyManager.isPrivacyEnabled()) {
            sendTransitionMessage(player, "Power systems monitored.");
            return;
        }
        new Thread(() -> {
            try {
                String[] cmd = {"powershell", "-NoProfile", "-Command",
                    "(Get-WmiObject Win32_Battery).BatteryStatus"};
                Process proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
                String line = reader.readLine();
                proc.waitFor();

                String[] chargeCmds = {"powershell", "-NoProfile", "-Command",
                    "(Get-WmiObject Win32_Battery).EstimatedChargeRemaining"};
                Process chargeProc = new ProcessBuilder(chargeCmds).redirectErrorStream(true).start();
                java.io.BufferedReader chargeReader = new java.io.BufferedReader(new java.io.InputStreamReader(chargeProc.getInputStream()));
                String chargeLine = chargeReader.readLine();
                chargeProc.waitFor();

                int charge = -1;
                try { if (chargeLine != null) charge = Integer.parseInt(chargeLine.trim()); } catch (NumberFormatException ignored) {}

                // BatteryStatus: null/empty = no battery (desktop), 2 = AC power (laptop plugged in), 1 = discharging (laptop on battery)
                final boolean isDesktop = (line == null || line.trim().isEmpty());
                final boolean pluggedIn = !isDesktop && line.trim().equals("2");
                final int finalCharge = charge;

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isDesktop) {
                            sendTransitionMessage(player, "No battery detected. Desktop, huh. I wonder how much your PC was.");
                        } else if (pluggedIn) {
                            sendTransitionMessage(player, "Charger plugged in. Good. I'd hate for this to get cut short.");
                        } else {
                            String chargeStr = finalCharge >= 0 ? finalCharge + "%" : "unknown";
                            sendTransitionMessage(player, "You're on battery. " + chargeStr + " left. I'll still be here when it dies.");
                        }
                    }
                }, 0);
            } catch (Exception e) {
                sendTransitionMessage(player, "Power status: monitored.");
            }
        }).start();
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

    // method to handle post-crash transforming message when player rejoins
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
