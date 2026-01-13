package lol.cqllmetoxic.nullpointerentity.system;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * scans user directories and generates file information.
 * accesses desktop, documents, and downloads folders.
 * used for file system intrusion events.
 */
public class FileSystemSimulator {
    private static final Random random = new Random();

    /**
     * stores information about a file on the system.
     */
    public static class FileInfo {
        public String name, path, type;
        public long size, lastModified;
        public boolean isHidden, isEncrypted;

        public FileInfo(String name, String path, String type, long size, long lastModified, boolean isHidden, boolean isEncrypted) {
            this.name = name; this.path = path; this.type = type; this.size = size;
            this.lastModified = lastModified; this.isHidden = isHidden; this.isEncrypted = isEncrypted;
        }
    }

    /**
     * asynchronously scans user directories for files.
     *
     * @return future containing list of file information
     */
    public static CompletableFuture<List<FileInfo>> scanUserFiles() {
        return CompletableFuture.supplyAsync(() -> {
            List<FileInfo> files = new ArrayList<>();
            String userHome = System.getProperty("user.home");

            // scan common directories
            scanDirectory(userHome + "\\Desktop", files, "Desktop");
            scanDirectory(userHome + "\\Documents", files, "Documents");
            scanDirectory(userHome + "\\Downloads", files, "Downloads");
            scanDirectory(userHome + "\\Pictures", files, "Pictures");
            scanDirectory(userHome + "\\Videos", files, "Videos");

            // add some suspicious files for immersion
            files.addAll(generateSuspiciousFiles());

            return files;
        });
    }

    private static void scanDirectory(String dirPath, List<FileInfo> files, String category) {
        try {
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) return;

            File[] dirFiles = dir.listFiles();
            if (dirFiles == null) return;

            for (File file : dirFiles) {
                if (files.size() > 100) break; // limit for performance

                if (file.isFile()) {
                    String name = file.getName();
                    String extension = getFileExtension(name);
                    boolean suspicious = isSuspiciousFile(name);

                    files.add(new FileInfo(
                        name,
                        file.getAbsolutePath(),
                        extension,
                        file.length(),
                        file.lastModified(),
                        file.isHidden() || suspicious,
                        suspicious && name.contains("private")
                    ));
                }
            }
        } catch (Exception e) {
            // could not scan directory
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "unknown";
    }

    private static boolean isSuspiciousFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.contains("homework") || lower.contains("private") || lower.contains("secret") ||
               lower.contains("personal") || lower.contains("definitely") || lower.contains("not") ||
               lower.contains("backup") || lower.contains("old") || fileName.startsWith(".");
    }

    private static List<FileInfo> generateSuspiciousFiles() {
        return List.of(
            new FileInfo("definitely_not_homework.zip", "C:\\Users\\" + NullPointerEntity.WINDOWS_USERNAME + "\\Desktop\\definitely_not_homework.zip", "zip", 47583921L, System.currentTimeMillis() - 86400000, true, false),
            new FileInfo("private_stuff.rar", "C:\\Users\\" + NullPointerEntity.WINDOWS_USERNAME + "\\Documents\\private_stuff.rar", "rar", 238749123L, System.currentTimeMillis() - 172800000, true, true),
            new FileInfo("old_passwords.txt", "C:\\Users\\" + NullPointerEntity.WINDOWS_USERNAME + "\\Documents\\old_passwords.txt", "txt", 2847L, System.currentTimeMillis() - 2592000000L, true, false),
            new FileInfo("voice_memo_001.wav", "C:\\Users\\" + NullPointerEntity.WINDOWS_USERNAME + "\\Documents\\voice_memo_001.wav", "wav", 15728640L, System.currentTimeMillis() - 604800000, false, false),
            new FileInfo("backup_browser_data.json", "C:\\Users\\" + NullPointerEntity.WINDOWS_USERNAME + "\\Downloads\\backup_browser_data.json", "json", 891234L, System.currentTimeMillis() - 1209600000, true, false)
        );
    }

    public static String analyzeFileSystem() {
        List<FileInfo> files = scanUserFiles().join();

        StringBuilder analysis = new StringBuilder("FILE SYSTEM ANALYSIS COMPLETE\n");
        analysis.append("=".repeat(35)).append("\n");
        analysis.append("Total files scanned: ").append(files.size()).append("\n\n");

        // categorize files
        long totalSize = files.stream().mapToLong(f -> f.size).sum();
        long hiddenFiles = files.stream().filter(f -> f.isHidden).count();
        long encryptedFiles = files.stream().filter(f -> f.isEncrypted).count();

        analysis.append("STATISTICS:\n");
        analysis.append("- Total storage used: ").append(formatBytes(totalSize)).append("\n");
        analysis.append("- Hidden files: ").append(hiddenFiles).append("\n");
        analysis.append("- Encrypted files: ").append(encryptedFiles).append("\n\n");

        analysis.append("SUSPICIOUS FILES DETECTED:\n");
        files.stream().filter(f -> f.isHidden || f.isEncrypted || isSuspiciousFile(f.name))
              .forEach(f -> analysis.append("- ").append(f.name).append(" (").append(formatBytes(f.size)).append(")\n"));

        analysis.append("\nFILE TYPE BREAKDOWN:\n");
        files.stream().collect(java.util.stream.Collectors.groupingBy(f -> f.type, java.util.stream.Collectors.counting()))
              .forEach((type, count) -> analysis.append("- ").append(type.toUpperCase()).append(": ").append(count).append(" files\n"));

        return analysis.toString();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
