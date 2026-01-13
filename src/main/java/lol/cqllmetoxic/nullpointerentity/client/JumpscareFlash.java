package lol.cqllmetoxic.nullpointerentity.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Timer;
import java.util.TimerTask;

/**
 * creates a brief white screen flash effect for jumpscares.
 * displays for 200ms with a loud sound.
 * used during various scare events for sudden impact.
 */
public class JumpscareFlash {
    private static boolean isFlashing = false;
    private static long flashStartTime = 0;
    private static final long FLASH_DURATION = 200;
    private static Screen previousScreen = null;

    // you can customize this path to your own image file
    private static final Identifier JUMPSCARE_IMAGE = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/jumpscare.png");

    public static void initialize() {
        // use clienttickevents to handle the jumpscare flash
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isFlashing) {
                handleJumpscareFlash(client);
            }
        });
    }

    public static void triggerJumpscareFlash() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        // start the flash
        isFlashing = true;
        flashStartTime = System.currentTimeMillis();
        previousScreen = client.currentScreen;

        // show the jumpscare overlay screen
        client.setScreen(new JumpscareOverlayScreen(previousScreen));

        // play the jumpscare sound
        playJumpscareSound(client);

        // schedule flash to end
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isFlashing = false;
                // restore the previous screen
                if (client.currentScreen instanceof JumpscareOverlayScreen) {
                    client.setScreen(previousScreen);
                }
                timer.cancel();
            }
        }, FLASH_DURATION);

        NullPointerEntity.LOGGER.info("Jumpscare flash triggered for {}ms", FLASH_DURATION);
    }

    private static void handleJumpscareFlash(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - flashStartTime;

        if (elapsed > FLASH_DURATION) {
            isFlashing = false;
            // ensure we return to the previous screen
            if (client.currentScreen instanceof JumpscareOverlayScreen) {
                client.setScreen(previousScreen);
            }
        }
    }

    private static void playJumpscareSound(MinecraftClient client) {
        try {
            // layer 1: deep, bone-chilling warden death scream
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.ENTITY_WARDEN_DEATH,
                    0.1f, // extremely low pitch for pure demonic terror
                    1.0f  // full volume
                )
            );

            // layer 2: terrifying warden heartbeat - sounds like death approaching
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                    0.05f, // insanely low pitch for earth-shaking dread
                    1.0f  // full volume for maximum terror
                )
            );

            // layer 3: distorted ghast scream - sounds like tortured souls from hell
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.ENTITY_GHAST_SCREAM,
                    0.03f, // horrifically low pitch for otherworldly nightmare
                    1.0f  // full volume for pure terror
                )
            );

            // layer 4: warden sonic boom - reality-shattering horror
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                    0.08f, // extremely low pitch for apocalyptic dread
                    1.0f  // full blast
                )
            );

            // layer 5: sculk shrieker shriek - alien nightmare fuel
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK,
                    0.04f, // terrifyingly low pitch for cosmic horror
                    0.9f  // nearly full volume
                )
            );

            // layer 6: ender dragon death - apocalyptic world-ending terror
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.ENTITY_ENDER_DRAGON_DEATH,
                    0.02f, // nightmarishly low pitch for end-times horror
                    0.8f   // high volume
                )
            );

            // layer 7: wither spawn - the sound of pure evil being born
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.ENTITY_WITHER_SPAWN,
                    0.06f, // horrifically low pitch for demonic birth
                    0.9f   // high volume
                )
            );

            // layer 8: cave ambient sounds - psychological horror
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.AMBIENT_CAVE.value(),
                    0.03f, // extremely low pitch for deep dread
                    0.7f   // moderate volume for unsettling background
                )
            );

            // layer 9: anvil land - sounds like reality breaking
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.BLOCK_ANVIL_LAND,
                    0.01f, // insanely low pitch for earth-shattering impact
                    0.8f   // high volume
                )
            );

            // layer 10: lightning strike - divine wrath
            client.getSoundManager().play(
                PositionedSoundInstance.master(
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                    0.05f, // very low pitch for godly fury
                    0.7f   // strong volume
                )
            );

            NullPointerEntity.LOGGER.info("MAXIMUM TERROR: 10-layer nightmare jumpscare sound triggered");

        } catch (Exception e) {
            NullPointerEntity.LOGGER.error("Failed to play nightmare jumpscare sound: {}", e.getMessage());
        }
    }

    public static boolean isCurrentlyFlashing() {
        return isFlashing;
    }

    // inner class for the jumpscare overlay screen
    private static class JumpscareOverlayScreen extends Screen {
        private final Screen parent;

        protected JumpscareOverlayScreen(Screen parent) {
            super(Text.literal(""));
            this.parent = parent;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            long elapsed = System.currentTimeMillis() - flashStartTime;
            float progress = (float) elapsed / FLASH_DURATION;

            // calculate flash intensity
            float intensity;
            if (progress < 0.3f) {
                intensity = progress / 0.3f;
            } else if (progress < 0.7f) {
                intensity = 1.0f;
            } else {
                intensity = 1.0f - ((progress - 0.7f) / 0.3f);
            }

            intensity = Math.max(0.0f, Math.min(1.0f, intensity));

            // try to render custom jumpscare image, fallback to red screen
            try {
                // attempt to draw the custom jumpscare image with correct method signature
                context.drawTexture(
                    net.minecraft.client.render.RenderLayer::getGuiTextured,
                    JUMPSCARE_IMAGE,
                    0, 0, // position
                    0.0f, 0.0f, // texture uv
                    this.width, this.height, // size
                    this.width, this.height // texture size
                );
            } catch (Exception e) {
                // fallback: red screen flash
                int alpha = (int) (200 * intensity);
                int color = (alpha << 24) | (255 << 16) | (0 << 8) | 0; // red with alpha
                context.fill(0, 0, this.width, this.height, color);

                // add flickering effect
                if (intensity > 0.5f && System.currentTimeMillis() % 100 < 50) {
                    int flickerAlpha = (int) (100 * intensity);
                    int flickerColor = (flickerAlpha << 24) | (255 << 16) | (255 << 8) | 255; // white flicker
                    context.fill(0, 0, this.width, this.height, flickerColor);
                }
            }
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false; // don't allow closing with esc during jumpscare
        }

        @Override
        public boolean shouldPause() {
            return false; // don't pause the game
        }
    }
}
