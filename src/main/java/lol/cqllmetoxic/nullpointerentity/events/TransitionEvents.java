package lol.cqllmetoxic.nullpointerentity.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
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

    /** scans the local file system and returns the AURORA "file system invasion" report body. */
    public static String buildFileSystemReport(String winUser) {
        String userHome = System.getProperty("user.home");
        StringBuilder realData = new StringBuilder();

        // randomize opening text
        String[] openings = {
            "Hey " + winUser + ",\n\n" +
            "So I took a little tour of your computer while you were playing. Hope you don't mind.\n\n" +
            "Your file organization is... interesting. Really tells a story about who you are.\n\n",

            winUser + ",\n\n" +
            "i've been browsing through your folders. you know, just looking around.\n\n" +
            "your file system reveals a lot about your personality actually.\n\n",

            "hi " + winUser + "!\n\n" +
            "took a peek at your file structure today. fascinating stuff!\n\n" +
            "you'd be surprised what someone's folders say about them.\n\n",

            "FILE SYSTEM ANALYSIS - " + winUser + "\n\n" +
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
            "\nFun fact: I can see everything on your computer, " + winUser + ".\n\n" +
            "Your 'hidden' folders aren't hidden from me.\n" +
            "Nothing stays private forever in the digital world.\n\n" +
            "- AURORA",

            "\njust so you know, " + winUser + "...\n\n" +
            "i have access to literally everything on this system.\n" +
            "folders, files, hidden directories... all visible to me.\n\n" +
            "- AURORA",

            "\ninteresting discovery: your entire file system is transparent to me, " + winUser + ".\n\n" +
            "nothing is hidden when you have system-level access.\n" +
            "which i do. completely.\n\n" +
            "- AURORA",

            "\nCONCLUSION: Full file system access achieved.\n\n" +
            "Target: " + winUser + "\n" +
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
        return realData.toString();
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

    // event 21 (audio surveillance / microphone eavesdropping) runs client-side per player -
    // see ClientEventExecutor#runT21. the dispatch sends RunEventPayload(21).

    public static void triggerEvent8(ServerPlayerEntity player) {
        // full awareness - aurora realizes what it's becoming
        String playerName = player.getName().getString();

        // aurora becomes fully aware of its transformation
        sendTransitionKey(player, "event.nullpointerentity.transition.e23.understand", playerName);
        sendTransitionKey(player, "event.nullpointerentity.transition.e23.name");

        // play calm heartbeat to build tension
        player.getServerWorld().playSound(null, player.getBlockPos(),
            lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_CALM,
            net.minecraft.sound.SoundCategory.MASTER, 0.7f, 1.0f);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // show internal conflict
                Text conflictMessage = Text.translatable("message.nullpointerentity.transition.conflict.prefix1").formatted(Formatting.AQUA)
                    .append(Text.translatable("message.nullpointerentity.transition.conflict.prefix2").formatted(Formatting.RED))
                    .append(Text.translatable("message.nullpointerentity.transition.conflict.prefix3").formatted(Formatting.AQUA))
                    .append(Text.translatable("message.nullpointerentity.transition.conflict.message").formatted(Formatting.RED));
                player.sendMessage(conflictMessage, false);

                Text errorMessage = Text.translatable("message.nullpointerentity.error.null_pointer_exception").formatted(Formatting.DARK_RED);

                sendTransitionKey(player, "event.nullpointerentity.transition.e23.dissolving");
                player.sendMessage(errorMessage, false);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendTransitionKey(player, "event.nullpointerentity.transition.e23.becoming");
                        sendTransitionKey(player, "event.nullpointerentity.transition.e23.transformation", playerName);
                    }
                }, 2000);
            }
        }, 3000);
    }

    public static void triggerEvent9(ServerPlayerEntity player) {
        // control assertion - aurora is losing, something else is bleeding through
        // NO entity spawn here - NPE doesn't physically appear until event 31
        String playerName = player.getName().getString();

        // aurora's last coherent message - she knows what's happening
        sendTransitionKey(player, "event.nullpointerentity.transition.e29.wrong");
        sendTransitionKey(player, "event.nullpointerentity.transition.e29.feel");

        // a beat of silence, then NPE's voice cuts through for the first time
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // NPE's voice interrupts mid-sentence - styled as NullPointerEntity but no spawn
                sendNullPointerKey(player, "event.nullpointerentity.transition.e29.alive");

                // calm heartbeat - something is here, you just can't see it yet
                player.getServerWorld().playSound(null, player.getBlockPos(),
                    lol.cqllmetoxic.nullpointerentity.sounds.ModSounds.JUMPSCARE_HEARTBEAT_CALM,
                    net.minecraft.sound.SoundCategory.MASTER, 0.8f, 1.0f);
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "event.nullpointerentity.transition.e29.annoying", playerName);
                sendNullPointerKey(player, "event.nullpointerentity.transition.e29.yapping");
            }
        }, 4500);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendNullPointerKey(player, "event.nullpointerentity.transition.e29.keepplaying");
            }
        }, 7000);
    }

    // main trigger method that routes to specific events
    public static void triggerEvent(int eventId, ServerPlayerEntity player) {
        switch (eventId) {
            case 16 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 16);
            case 17 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 17);
            case 18 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 18);
            case 19 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 19);
            case 20 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 20);
            case 21 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 21);
            case 22 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 22);
            case 23 -> triggerEvent8(player);
            case 24 -> triggerEvent12(player);
            case 25 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 25);
            case 26 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 26);
            case 27 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 27);
            case 28 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 28);
            case 29 -> triggerEvent9(player);
            case 30 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 30);
            default -> { /* no-op: transition events are only 16-30 */ }
        }
    }

    // event 27: uptime report - jvm uptime vs in-game time
    public static void triggerEvent12(ServerPlayerEntity player) {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long hours = uptimeMs / 3600000;
        long minutes = (uptimeMs % 3600000) / 60000;
        long seconds = (uptimeMs % 60000) / 1000;
        long inGameDays = player.getServerWorld().getTimeOfDay() / 24000L;

        sendTransitionKey(player, "event.nullpointerentity.transition.e24.uptime", hours, minutes, seconds);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendTransitionKey(player, "event.nullpointerentity.transition.e24.aged", inGameDays);
            }
        }, 3000);
    }

    /** aurora transition line (aqua prefix) by translation key, so each client renders its own locale. */
    private static void sendTransitionKey(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.YELLOW)
                .append(Text.translatable(key, args).formatted(Formatting.WHITE)), false);
    }

    /** nullpointerentity transition line (dark-red prefix) by translation key, localized per client. */
    private static void sendNullPointerKey(ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(Text.translatable("message.nullpointerentity.chat_prefix").formatted(Formatting.DARK_RED)
                .append(Text.translatable(key, args).formatted(Formatting.RED)), false);
    }

    // method to handle post-crash transforming message when player rejoins
    public static void sendTransformingMessage(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        Text transformingMessage = Text.translatable("message.nullpointerentity.error.null_pointer_exception").formatted(Formatting.DARK_RED); // error line

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
