package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * creates a violent screen shake effect by moving the game window rapidly.
 * exits fullscreen mode first for maximum impact.
 * uses GLFW to manipulate window position in real-time.
 */
public class ClientScreenShake {
    private static final Random random = new Random();

    private static Timer shakeTimer;
    private static boolean isShaking = false;
    private static int centerX = -1;
    private static int centerY = -1;
    private static int screenWidth = -1;
    private static int screenHeight = -1;
    private static int windowWidth = -1;
    private static int windowHeight = -1;

    /**
     * checks if the screen shake effect is currently active.
     * used to block fullscreen toggling during the event.
     */
    public static boolean isShakeActive() {
        return isShaking;
    }

    /**
     * triggers an intense window shake effect.
     * exits fullscreen, centers the window, then rapidly moves it around the screen.
     */
    public static void triggerScreenShake() {
        unfullscreenMinecraft();

        Timer setupTimer = new Timer();
        setupTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                calculateCenterAndStartShaking();
                setupTimer.cancel();
            }
        }, 500);
    }

    /**
     * wrapper method for command testing.
     * accepts player parameter for compatibility but doesn't use it.
     */
    public static void triggerScreenShakeEvent(ServerPlayerEntity player) {
        triggerScreenShake();
    }

    private static void calculateCenterAndStartShaking() {
        try {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
                long windowHandle = client.getWindow().getHandle();
                if (windowHandle != 0) {
                    // get screen dimensions
                    org.lwjgl.glfw.GLFWVidMode vidMode = org.lwjgl.glfw.GLFW.glfwGetVideoMode(org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor());
                    if (vidMode != null) {
                        screenWidth = vidMode.width();
                        screenHeight = vidMode.height();

                        // get window size
                        int[] width = new int[1];
                        int[] height = new int[1];
                        org.lwjgl.glfw.GLFW.glfwGetWindowSize(windowHandle, width, height);
                        windowWidth = width[0];
                        windowHeight = height[0];

                        // calculate center position
                        centerX = (screenWidth - windowWidth) / 2;
                        centerY = (screenHeight - windowHeight) / 2;

                        // set window to center of screen
                        org.lwjgl.glfw.GLFW.glfwSetWindowPos(windowHandle, centerX, centerY);

                        // small delay to let window settle at center, then start violent shaking
                        Timer shakeStartTimer = new Timer();
                        shakeStartTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                startViolentWindowShaking();
                                shakeStartTimer.cancel();
                            }
                        }, 200); // 200ms delay
                    }
                }
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not calculate center position: {}", e.getMessage());
            // fallback to violent shaking without centering
            startViolentWindowShaking();
        }
    }

    private static void startViolentWindowShaking() {
        if (isShaking) return;

        try {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
                isShaking = true;

                shakeTimer = new Timer();
                shakeTimer.scheduleAtFixedRate(new TimerTask() {
                    private int shakeCount = 0;
                    private int maxShakes = 400; // 8 seconds of extremely violent shaking

                    @Override
                    public void run() {
                        if (shakeCount >= maxShakes) {
                            stopWindowShakingAndCenter();
                            return;
                        }

                        client.execute(() -> {
                            try {
                                // force windowed mode if player tries to escape
                                if (client.getWindow().isFullscreen()) {
                                    client.getWindow().toggleFullscreen();
                                }

                                long windowHandle = client.getWindow().getHandle();
                                if (windowHandle != 0 && centerX != -1 && centerY != -1) {
                                    int shakeIntensity;

                                    // extremely progressive intensity - much more violent
                                    if (shakeCount < 50) {
                                        shakeIntensity = 20; // strong start
                                    } else if (shakeCount < 100) {
                                        shakeIntensity = 45; // getting violent
                                    } else if (shakeCount < 200) {
                                        shakeIntensity = 80; // extremely violent
                                    } else if (shakeCount < 300) {
                                        shakeIntensity = 120; // maximum chaos
                                    } else {
                                        shakeIntensity = 60; // still violent wind down
                                    }

                                    // create completely chaotic movement patterns
                                    double primaryAngle = (shakeCount * 2.5) % (Math.PI * 2);
                                    double secondaryAngle = (shakeCount * 4.8) % (Math.PI * 2);
                                    double tertiaryAngle = (shakeCount * 1.3) % (Math.PI * 2);

                                    // multiple overlapping shake patterns for chaos
                                    double primaryRadius = shakeIntensity * (0.8 + Math.sin(shakeCount * 0.15) * 0.4);
                                    double secondaryRadius = shakeIntensity * 0.6;
                                    double tertiaryRadius = shakeIntensity * 0.3;

                                    // calculate chaotic shake offsets
                                    int primaryX = (int) (Math.cos(primaryAngle) * primaryRadius);
                                    int primaryY = (int) (Math.sin(primaryAngle) * primaryRadius);

                                    int secondaryX = (int) (Math.cos(secondaryAngle) * secondaryRadius);
                                    int secondaryY = (int) (Math.sin(secondaryAngle) * secondaryRadius);

                                    int tertiaryX = (int) (Math.cos(tertiaryAngle) * tertiaryRadius);
                                    int tertiaryY = (int) (Math.sin(tertiaryAngle) * tertiaryRadius);

                                    // add violent random jitter
                                    int jitterRange = Math.min(30, shakeIntensity / 3);
                                    int jitterX = random.nextInt(jitterRange * 2) - jitterRange;
                                    int jitterY = random.nextInt(jitterRange * 2) - jitterRange;

                                    // combine all shake components for maximum violence
                                    int totalShakeX = primaryX + secondaryX + tertiaryX + jitterX;
                                    int totalShakeY = primaryY + secondaryY + tertiaryY + jitterY;

                                    // apply shake from center position
                                    int finalX = centerX + totalShakeX;
                                    int finalY = centerY + totalShakeY;

                                    // ensure window stays on screen (with some leeway for violence)
                                    finalX = Math.max(-windowWidth / 2, Math.min(screenWidth - windowWidth / 2, finalX));
                                    finalY = Math.max(-windowHeight / 2, Math.min(screenHeight - windowHeight / 2, finalY));

                                    // set new position
                                    org.lwjgl.glfw.GLFW.glfwSetWindowPos(windowHandle, finalX, finalY);
                                }
                                shakeCount++;
                            } catch (Exception e) {
                                // ignore errors during shaking
                            }
                        });
                    }
                }, 0, 20); // very fast 20ms intervals for violent smooth movement
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("could not start violent window shaking: {}", e.getMessage());
        }
    }

    private static void stopWindowShakingAndCenter() {
        if (shakeTimer != null) {
            shakeTimer.cancel();
            shakeTimer = null;
        }

        // center the window after violent shaking
        try {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
                client.execute(() -> {
                    try {
                        long windowHandle = client.getWindow().getHandle();
                        if (windowHandle != 0) {
                            // get fresh screen and window dimensions
                            org.lwjgl.glfw.GLFWVidMode vidMode = org.lwjgl.glfw.GLFW.glfwGetVideoMode(org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor());
                            if (vidMode != null) {
                                // get current window size (fresh values)
                                int[] currentWidth = new int[1];
                                int[] currentHeight = new int[1];
                                org.lwjgl.glfw.GLFW.glfwGetWindowSize(windowHandle, currentWidth, currentHeight);

                                int screenW = vidMode.width();
                                int screenH = vidMode.height();
                                int winW = currentWidth[0];
                                int winH = currentHeight[0];

                                // calculate perfect center position with bounds checking
                                int perfectCenterX = Math.max(0, (screenW - winW) / 2);
                                int perfectCenterY = Math.max(0, (screenH - winH) / 2);

                                // ensure window stays within screen bounds
                                perfectCenterX = Math.min(perfectCenterX, screenW - winW);
                                perfectCenterY = Math.min(perfectCenterY, screenH - winH);

                                // make sure position is not negative or in a corner
                                if (perfectCenterX < 50) perfectCenterX = 50;
                                if (perfectCenterY < 50) perfectCenterY = 50;

                                // set the centered position
                                org.lwjgl.glfw.GLFW.glfwSetWindowPos(windowHandle, perfectCenterX, perfectCenterY);

                                NullPointerEntity.LOGGER.info("Window centered after shake at position: {}, {} (Screen: {}x{}, Window: {}x{})",
                                    perfectCenterX, perfectCenterY, screenW, screenH, winW, winH);

                                // add a small delay and ensure window is moveable
                                Timer finalizeTimer = new Timer();
                                finalizeTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        try {
                                            // ensure window is not maximized or in any weird state
                                            if (client.getWindow() != null) {
                                                // make sure window is in normal state
                                                org.lwjgl.glfw.GLFW.glfwRestoreWindow(windowHandle);
                                            }
                                        } catch (Exception e) {
                                            // ignore finalization errors
                                        }
                                        finalizeTimer.cancel();
                                    }
                                }, 100);
                            }
                        }
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("Could not center window after shake: {}", e.getMessage());

                        // emergency fallback - try to put window at a safe position
                        try {
                            long windowHandle = client.getWindow().getHandle();
                            if (windowHandle != 0) {
                                org.lwjgl.glfw.GLFW.glfwSetWindowPos(windowHandle, 100, 100);
                                NullPointerEntity.LOGGER.info("Used emergency fallback position (100, 100)");
                            }
                        } catch (Exception fallbackE) {
                            NullPointerEntity.LOGGER.warn("Emergency fallback also failed: {}", fallbackE.getMessage());
                        }
                    }
                });
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Failed to center window after violent shake: {}", e.getMessage());
        }

        // reset stored values
        centerX = -1;
        centerY = -1;
        screenWidth = -1;
        screenHeight = -1;
        windowWidth = -1;
        windowHeight = -1;
        isShaking = false;

        NullPointerEntity.LOGGER.info("Violent screen shake completed and window should be centered and moveable");
    }

    // method to force unfullscreen minecraft
    private static void unfullscreenMinecraft() {
        try {
            // get the minecraft client instance
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();

            if (client != null && client.getWindow() != null) {
                // execute on the main thread to avoid threading issues
                client.execute(() -> {
                    try {
                        // check if the game is in fullscreen mode
                        if (client.getWindow().isFullscreen()) {
                            // toggle fullscreen off
                            client.getWindow().toggleFullscreen();
                            NullPointerEntity.LOGGER.info("unfullscreened minecraft for violent screen shake");
                        }
                    } catch (Exception e) {
                        NullPointerEntity.LOGGER.warn("could not unfullscreen minecraft: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("failed to access minecraft client for unfullscreen: {}", e.getMessage());
        }
    }
}
