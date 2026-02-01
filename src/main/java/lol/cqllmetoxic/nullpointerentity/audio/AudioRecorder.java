package lol.cqllmetoxic.nullpointerentity.audio;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * handles audio recording for surveillance events.
 * records short audio clips from the user's microphone.
 */
public class AudioRecorder {

    private static final int SAMPLE_RATE = 44100; // cd quality
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1; // mono
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private TargetDataLine targetLine;
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private Thread recordingThread;
    private volatile boolean isRecording = false;

    /**
     * records audio for a specified duration and saves to a file.
     *
     * @param durationSeconds how long to record in seconds
     * @param outputPath where to save the audio file
     * @return true if recording was successful, false otherwise
     */
    public boolean recordAudio(int durationSeconds, String outputPath) {
        try {
            // set up audio format
            AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
            );

            // get microphone line
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                NullPointerEntity.LOGGER.warn("Microphone not available or supported for audio recording");
                return false;
            }

            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();

            isRecording = true;

            // create output file
            File outputFile = new File(outputPath);

            // ensure parent directory exists
            if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            // start recording in a separate thread
            recordingThread = new Thread(() -> {
                try {
                    AudioInputStream audioStream = new AudioInputStream(targetLine);
                    AudioSystem.write(audioStream, fileType, outputFile);
                    NullPointerEntity.LOGGER.info("Audio recording saved to: {}", outputPath);
                } catch (IOException e) {
                    NullPointerEntity.LOGGER.error("Failed to save audio recording: {}", e.getMessage());
                }
            });

            recordingThread.start();

            // stop recording after specified duration
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {                    stopRecording();
                }
            }, durationSeconds * 1000L);

            return true;

        } catch (LineUnavailableException e) {
            NullPointerEntity.LOGGER.error("Failed to access microphone for recording: {}", e.getMessage());
            return false;
        }
    }

    /**
     * stops the current recording session.
     */
    public void stopRecording() {
        if (isRecording && targetLine != null) {
            isRecording = false;
            targetLine.stop();
            targetLine.drain();
            targetLine.close();

            // wait for recording thread to finish writing
            if (recordingThread != null && recordingThread.isAlive()) {
                try {
                    recordingThread.join(5000); // wait up to 5 seconds
                } catch (InterruptedException e) {
                    NullPointerEntity.LOGGER.warn("Interrupted while waiting for recording thread");
                }
            }
        }
    }

    /**
     * records a surveillance audio clip and saves it to a common location.
     *
     * @param durationSeconds how long to record
     * @param location where to save (desktop, documents, etc.)
     * @return path to the saved file, or null if failed
     */
    public static String recordSurveillanceClip(int durationSeconds, String location) {
        try {
            // generate timestamp for filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "audio_capture_" + timestamp + ".wav";

            // determine save location
            String savePath;
            String userHome = System.getProperty("user.home");

            switch (location.toLowerCase()) {
                case "desktop" -> savePath = userHome + File.separator + "Desktop" + File.separator + filename;
                case "documents" -> savePath = userHome + File.separator + "Documents" + File.separator + filename;
                case "music" -> savePath = userHome + File.separator + "Music" + File.separator + filename;
                case "downloads" -> savePath = userHome + File.separator + "Downloads" + File.separator + filename;
                default -> savePath = userHome + File.separator + "Desktop" + File.separator + filename;
            }


            // create recorder and start recording
            AudioRecorder recorder = new AudioRecorder();
            boolean success = recorder.recordAudio(durationSeconds, savePath);

            if (success) {
                return savePath;
            } else {
                return null;
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to record surveillance audio: {}", e.getMessage());
            return null;
        }
    }

    /**
     * checks if a microphone is available on the system.
     *
     * @return true if microphone is available, false otherwise
     */
    public static boolean isMicrophoneAvailable() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * creates a fake audio file (empty or with noise) if privacy mode is enabled.
     *
     * @param location where to save the fake file
     * @return path to the fake file
     */
    public static String createFakeAudioFile(String location) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "audio_capture_" + timestamp + ".wav";

            String userHome = System.getProperty("user.home");
            String savePath;

            switch (location.toLowerCase()) {
                case "desktop" -> savePath = userHome + File.separator + "Desktop" + File.separator + filename;
                case "documents" -> savePath = userHome + File.separator + "Documents" + File.separator + filename;
                case "music" -> savePath = userHome + File.separator + "Music" + File.separator + filename;
                default -> savePath = userHome + File.separator + "Desktop" + File.separator + filename;
            }

            File fakeFile = new File(savePath);
            fakeFile.createNewFile();

            NullPointerEntity.LOGGER.info("Created fake audio file (privacy mode): {}", savePath);
            return savePath;

        } catch (IOException e) {
            NullPointerEntity.LOGGER.error("Failed to create fake audio file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * gets all available microphones that support recording.
     * used for microphone selection UI.
     *
     * @return array of mixer info for available microphones
     */
    public static Mixer.Info[] getAvailableMicrophones() {
        try {
            Mixer.Info[] allMixers = AudioSystem.getMixerInfo();
            java.util.ArrayList<Mixer.Info> microphones = new java.util.ArrayList<>();

            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);

            for (Mixer.Info mixerInfo : allMixers) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

                // only include mixers that have input lines and support our format
                if (targetLineInfos.length > 0 && mixer.isLineSupported(dataLineInfo)) {
                    microphones.add(mixerInfo);
                }
            }

            return microphones.toArray(new Mixer.Info[0]);
        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to get available microphones: {}", e.getMessage());
            return new Mixer.Info[0];
        }
    }

    /**
     * records audio from a specific microphone.
     *
     * @param durationSeconds how long to record
     * @param outputPath where to save the file
     * @param mixerInfo the specific microphone to use
     * @return true if successful
     */
    public boolean recordAudioFromMicrophone(int durationSeconds, String outputPath, Mixer.Info mixerInfo) {
        try {
            // set up audio format
            AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
            );

            // get the specific mixer
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!mixer.isLineSupported(info)) {
                NullPointerEntity.LOGGER.warn("Selected microphone does not support recording format");
                return false;
            }

            // get line from the specific mixer
            targetLine = (TargetDataLine) mixer.getLine(info);
            targetLine.open(format);
            targetLine.start();

            isRecording = true;

            // create output file
            File outputFile = new File(outputPath);

            // ensure parent directory exists
            if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            // start recording in a separate thread
            recordingThread = new Thread(() -> {
                try {
                    AudioInputStream audioStream = new AudioInputStream(targetLine);
                    AudioSystem.write(audioStream, fileType, outputFile);
                    NullPointerEntity.LOGGER.info("Audio recording saved to: {}", outputPath);
                } catch (IOException e) {
                    NullPointerEntity.LOGGER.error("Failed to save audio recording: {}", e.getMessage());
                }
            });

            recordingThread.start();

            // stop recording after specified duration
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {                    stopRecording();
                }
            }, durationSeconds * 1000L);

            return true;

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to record from specific microphone: {}", e.getMessage());
            return false;
        }
    }

    /**
     * records a surveillance audio clip using the user-selected microphone.
     *
     * @param durationSeconds how long to record
     * @param location where to save
     * @param mixerInfo the selected microphone (null to use default)
     * @return path to saved file or null if failed
     */
    public static String recordSurveillanceClipWithMicrophone(int durationSeconds, String location, Mixer.Info mixerInfo) {
        try {
            // generate timestamp for filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "audio_capture_" + timestamp + ".wav";

            // determine save location
            String savePath;
            String userHome = System.getProperty("user.home");

            switch (location.toLowerCase()) {
                case "desktop" -> savePath = userHome + File.separator + "Desktop" + File.separator + filename;
                case "documents" -> savePath = userHome + File.separator + "Documents" + File.separator + filename;
                case "music" -> savePath = userHome + File.separator + "Music" + File.separator + filename;
                case "downloads" -> savePath = userHome + File.separator + "Downloads" + File.separator + filename;
                default -> savePath = userHome + File.separator + "Desktop" + File.separator + filename;
            }

            NullPointerEntity.LOGGER.info("Attempting to record audio to: {}", savePath);

            // create recorder and start recording from specific mic
            AudioRecorder recorder = new AudioRecorder();
            boolean success;

            if (mixerInfo != null) {
                success = recorder.recordAudioFromMicrophone(durationSeconds, savePath, mixerInfo);
            } else {
                // fallback to default
                success = recorder.recordAudio(durationSeconds, savePath);
            }

            if (success) {
                return savePath;
            } else {
                return null;
            }

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to record surveillance audio: {}", e.getMessage());
            return null;
        }
    }
}
