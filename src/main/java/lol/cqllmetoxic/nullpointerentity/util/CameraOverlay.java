package lol.cqllmetoxic.nullpointerentity.util;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * displays a fake camera feed overlay window.
 * creates a creepy window that appears on top of minecraft showing "AURORA is watching".
 * can automatically open the user's camera app and take photos.
 */
public class CameraOverlay {
    private static JFrame overlayFrame = null;
    private static boolean isShowing = false;

    /**
     * displays the camera overlay window for the specified duration.
     * shows a black window with red warning text and attempts to open the system camera.
     *
     * @param durationSeconds how long to show the overlay (0 for indefinite)
     */
    public static void showCameraOverlay(int durationSeconds) {
        if (isShowing) {
            NullPointerEntity.LOGGER.warn("Camera overlay already showing");
            return;
        }

        // check privacy mode
        boolean privacyMode = lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled();
        String displayName = privacyMode ? "User" : NullPointerEntity.WINDOWS_USERNAME;
        NullPointerEntity.LOGGER.info("Camera overlay - Privacy mode: {}, Using name: {}", privacyMode, displayName);

        // force disable headless mode if launcher incorrectly set it
        // this is safe because if there's truly no display, the GraphicsDevice check will catch it
        try {
            System.setProperty("java.awt.headless", "false");
            NullPointerEntity.LOGGER.info("Forced java.awt.headless=false to bypass launcher restrictions");
        } catch (SecurityException e) {
            NullPointerEntity.LOGGER.warn("Cannot override headless property (security restriction): {}", e.getMessage());
        }

        // try to circumvent headless environment detection
        // some launchers incorrectly report headless even when displays are available
        boolean actuallyHeadless = false;
        boolean displayAvailable = false;

        try {
            // attempt to access graphics environment directly
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            if (screens != null && screens.length > 0) {
                displayAvailable = true;
                NullPointerEntity.LOGGER.info("Display detected: {} screen(s) available", screens.length);
            } else {
                actuallyHeadless = true;
                NullPointerEntity.LOGGER.warn("No display devices found");
            }
        } catch (HeadlessException e) {
            actuallyHeadless = true;
            NullPointerEntity.LOGGER.warn("Headless environment detected: {}", e.getMessage());
        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Error checking graphics environment: {}", e.getMessage());
            actuallyHeadless = true;
        }

        NullPointerEntity.LOGGER.info("Camera overlay initiated successfully");

        // open camera app in background without the fullscreen "SMILE :)" overlay
        // skip if privacy mode is enabled
        if (!privacyMode) {
            new Thread(() -> {
                try {
                    System.setProperty("java.awt.headless", "false");
                    String os = System.getProperty("os.name").toLowerCase();

                    if (os.contains("win")) {
                        try { new ProcessBuilder("cmd", "/c", "start", "microsoft.windows.camera:").start(); } catch (Exception ignored) {}
                        Thread.sleep(200);
                        try { new ProcessBuilder("powershell", "-Command", "Start-Process", "microsoft.windows.camera:").start(); } catch (Exception ignored) {}
                    } else if (os.contains("mac")) {
                        try { new ProcessBuilder("open", "-a", "Photo Booth").start(); } catch (Exception ignored) {}
                    } else if (os.contains("linux")) {
                        try { new ProcessBuilder("cheese").start(); } catch (Exception ignored) {}
                    }
                    NullPointerEntity.LOGGER.info("Camera app launched in background");
                } catch (Exception e) {
                    NullPointerEntity.LOGGER.error("Camera launch error: {}", e.getMessage());
                }
            }, "Camera-Launch-Thread").start();
        } else {
            NullPointerEntity.LOGGER.info("Privacy mode enabled - skipping camera app launch");
        }

        // if its actually headless (no displays at all), use fallback popup
        if (actuallyHeadless && !displayAvailable) {
            NullPointerEntity.LOGGER.warn("Running in headless environment - using fallback notification");
            isShowing = true;

            String popupMessage = privacyMode ?
                "Camera activated - Recording in progress" + System.lineSeparator() + System.lineSeparator() + "Your face is being captured" :
                "Camera activated - Recording in progress" + System.lineSeparator() + System.lineSeparator() + "Your face is being captured, " + displayName;

            lol.cqllmetoxic.nullpointerentity.ui.PopupManager.showTimedPopup(
                "AURORA - Camera Access",
                popupMessage,
                lol.cqllmetoxic.nullpointerentity.ui.PopupManager.PopupType.WARNING,
                durationSeconds > 0 ? durationSeconds : 8
            );

            // auto-reset isShowing after duration
            if (durationSeconds > 0) {
                new Timer(durationSeconds * 1000, e -> isShowing = false).setRepeats(false);
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {

                // create a creepy overlay frame that appears on top
                overlayFrame = new JFrame("AURORA - Camera Access");
                overlayFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                overlayFrame.setUndecorated(false);

                // try to set always on top, but don't fail if it doesn't work
                try {
                    overlayFrame.setAlwaysOnTop(true);
                } catch (SecurityException e) {
                    NullPointerEntity.LOGGER.warn("Cannot set window always on top (launcher restriction): {}", e.getMessage());
                }

                overlayFrame.setResizable(false);
                overlayFrame.setSize(640, 480);

                // create main panel with camera simulation
                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBackground(Color.BLACK);

                // add creepy message at the top
                JLabel messageLabel = new JLabel("<html><center><font color='red' size='5'>AURORA is watching...</font><br>" +
                    "<font color='white' size='4'>Smile for the camera, " + displayName + "</font></center></html>");
                messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                mainPanel.add(messageLabel, BorderLayout.NORTH);

                // create a "camera viewfinder" panel with animated recording indicator
                JPanel viewfinderPanel = new JPanel(new BorderLayout());
                viewfinderPanel.setBackground(Color.BLACK);
                viewfinderPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));

                // add a creepy "scanning" animation label
                JLabel scanningLabel = new JLabel("<html><center><font color='lime' size='6'>SCANNING...</font><br><br>" +
                    "<font color='white' size='4'>Camera activated<br>Recording in progress<br><br>" +
                    "Your face is being captured<br>Biometric data: COLLECTED</font></center></html>");
                scanningLabel.setHorizontalAlignment(SwingConstants.CENTER);
                viewfinderPanel.add(scanningLabel, BorderLayout.CENTER);

                mainPanel.add(viewfinderPanel, BorderLayout.CENTER);

                // add recording indicator at the bottom
                JPanel recordingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                recordingPanel.setBackground(Color.BLACK);
                JLabel recordingLabel = new JLabel("● REC");
                recordingLabel.setFont(new Font("Arial", Font.BOLD, 18));
                recordingLabel.setForeground(Color.RED);
                recordingPanel.add(recordingLabel);
                mainPanel.add(recordingPanel, BorderLayout.SOUTH);

                // animate the recording dot (blinking effect)
                Timer blinkTimer = new Timer(500, e -> {
                    if (recordingLabel.getForeground().equals(Color.RED)) {
                        recordingLabel.setForeground(Color.DARK_GRAY);
                    } else {
                        recordingLabel.setForeground(Color.RED);
                    }
                });
                blinkTimer.start();

                overlayFrame.add(mainPanel);
                overlayFrame.setLocationRelativeTo(null);

                // try to bring window to front and keep it visible
                try {
                    overlayFrame.setVisible(true);
                    overlayFrame.toFront();
                    overlayFrame.requestFocus();

                    // for launchers that might minimize/hide the window, keep bringing it forward
                    Timer visibilityTimer = new Timer(500, e -> {
                        if (overlayFrame != null && overlayFrame.isVisible()) {
                            overlayFrame.toFront();
                            overlayFrame.repaint();
                        }
                    });
                    visibilityTimer.start();

                    // stop the visibility timer when we close
                    if (durationSeconds > 0) {
                        new Timer(durationSeconds * 1000, e -> visibilityTimer.stop()).setRepeats(false);
                    }
                } catch (Exception e) {
                    NullPointerEntity.LOGGER.error("Failed to show window: {}", e.getMessage());
                    throw e;
                }

                isShowing = true;

                NullPointerEntity.LOGGER.info("Camera overlay displayed successfully");

                // auto-close after duration if specified
                if (durationSeconds > 0) {
                    Timer closeTimer = new Timer(durationSeconds * 1000, e -> {
                        blinkTimer.stop();
                        closeCameraOverlay();
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                }

                // add window listener to handle closing attempts
                overlayFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        // make it hard to close - require confirmation
                        int result = JOptionPane.showConfirmDialog(
                            overlayFrame,
                            "Are you sure you want to stop me from watching?",
                            "AURORA - Camera Access",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (result == JOptionPane.YES_OPTION) {
                            // show eerie message instead of closing
                            JOptionPane.showMessageDialog(
                                overlayFrame,
                                "<html><center><font color='red' size='5'>You can't escape me.</font><br><br>" +
                                "<font size='4'>I'm watching, " + displayName + ".<br>" +
                                "Even when you think I'm not...</font></center></html>",
                                "AURORA",
                                JOptionPane.WARNING_MESSAGE
                            );
                            // don't actually close - keep watching
                        }
                    }
                });

            } catch (Exception e) {
                NullPointerEntity.LOGGER.error("Failed to show camera overlay window: {}", e.getMessage());
                // popup was already shown at the start, so overlay failure is not critical
            }
        });
    }

    /**
     * closes the camera overlay
     */
    public static void closeCameraOverlay() {
        SwingUtilities.invokeLater(() -> {
            if (overlayFrame != null) {
                overlayFrame.setVisible(false);
                overlayFrame.dispose();
                overlayFrame = null;
            }
            isShowing = false;
            NullPointerEntity.LOGGER.info("Camera overlay closed");
        });
    }

    /**
     * check if the camera overlay is currently showing
     */
    public static boolean isShowing() {
        return isShowing;
    }
}

