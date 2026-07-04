package lol.cqllmetoxic.nullpointerentity.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Random;

/**
 * displays a convincing fake death screen overlay.
 * mimics minecraft's real death screen with working buttons.
 * automatically closes after the specified duration.
 */
public class FakeDeathScreen extends Screen {
    private final long startTime;
    private final long duration;
    private final Random random = new Random();
    private ButtonWidget respawnButton;
    private ButtonWidget titleButton;

    /**
     * creates a fake death screen that lasts for the specified duration.
     *
     * @param durationMs how long to show the fake death screen in milliseconds
     */
    public FakeDeathScreen(long durationMs) {
        super(Text.literal(""));
        this.startTime = System.currentTimeMillis();
        this.duration = durationMs;
    }

    @Override
    protected void init() {
        // add fake "respawn" button
        this.respawnButton = ButtonWidget.builder(
            Text.translatable("deathScreen.respawn"),
            button -> onRespawnClick()
        ).dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build();
        this.addDrawableChild(respawnButton);

        // add fake "title screen" button
        this.titleButton = ButtonWidget.builder(
            Text.translatable("deathScreen.titleScreen"),
            button -> onTitleScreenClick()
        ).dimensions(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build();
        this.addDrawableChild(titleButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long elapsed = System.currentTimeMillis() - startTime;

        // check if duration expired
        if (elapsed > duration) {
            this.close();
            return;
        }

        // render the actual game world in the background (dimmed)
        if (this.client != null && this.client.world != null) {
            // this renders the world behind the overlay, just like real death screen
            this.renderBackground(context, mouseX, mouseY, delta);
        }

        // dark red tinted overlay (matching authentic minecraft death screen)
        // darker overlay to match reference image
        context.fillGradient(0, 0, this.width, this.height,
            0xCC200000, 0xCC500000);

        // "you died!" or "you died!" text (death screen title)
        boolean isGlitching = elapsed > 2000 && elapsed % 1000 < 500;
        String deathCause = "NullPointerEntity";

        if (isGlitching) {
            // glitch to creepy messages periodically - render without scaling to avoid overlap
            Text glitchMessage = Text.translatable(
                "screen.nullpointerentity.death.glitch." + (1 + random.nextInt(9)));
            context.drawCenteredTextWithShadow(this.textRenderer, glitchMessage,
                this.width / 2, 70, 0xFFFFFF);
            deathCause = "";
        } else {
            // draw normal "you died!" centered at the top (scaled up like real death screen)
            Text deathMessage = Text.translatable("deathScreen.title");
            context.getMatrices().push();
            context.getMatrices().translate(this.width / 2.0, 70, 0);
            context.getMatrices().scale(2.0f, 2.0f, 1.0f);
            context.drawText(this.textRenderer, deathMessage, -this.textRenderer.getWidth(deathMessage) / 2, 0, 0xFFFFFF, true);
            context.getMatrices().pop();
        }

        // draw death cause below title (like real death screen)
        if (!deathCause.isEmpty() && elapsed < 2000) {
            String playerName = this.client != null && this.client.player != null
                ? this.client.player.getName().getString()
                : "Player";
            Text causeText = Text.translatable("screen.nullpointerentity.death.cause", playerName);
            context.drawCenteredTextWithShadow(this.textRenderer, causeText,
                this.width / 2, this.height / 4 + 10, 0xFFFFFF);

            // draw score (always 0 for creepiness)
            Text scoreText = Text.translatable("screen.nullpointerentity.death.score");
            context.drawCenteredTextWithShadow(this.textRenderer, scoreText,
                this.width / 2, this.height / 4 + 30, 0xFFFFFF);
        }

        // fake respawn countdown or error messages
        if (elapsed < 3000) {
            int countdown = 3 - (int)(elapsed / 1000);
            if (countdown > 0) {
                context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("screen.nullpointerentity.death.respawning", countdown),
                    this.width / 2, this.height / 4 + 125, 0xAAAAAA);
            }
        } else if (elapsed >= 3000 && elapsed < 3500) {
            // show error message after countdown
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("screen.nullpointerentity.death.error.respawn_failed"),
                this.width / 2, this.height / 4 + 125, 0xFF0000);
        }

        // corrupt buttons after 2 seconds
        if (elapsed > 2000) {
            if (elapsed % 800 < 400) {
                respawnButton.setMessage(Text.translatable("screen.nullpointerentity.death.error.glitch"));
                titleButton.setMessage(Text.translatable("screen.nullpointerentity.death.error.glitch"));
            } else {
                respawnButton.setMessage(Text.translatable("deathScreen.respawn"));
                titleButton.setMessage(Text.translatable("deathScreen.titleScreen"));
            }
        }

        // render buttons (they'll appear in correct positions due to init())
        super.render(context, mouseX, mouseY, delta);
    }

    private void onRespawnClick() {
        // fake button - show error message
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.translatable("screen.nullpointerentity.death.error.death_not_found"), false);
        }
    }

    private void onTitleScreenClick() {
        // fake button - show error message
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.translatable("screen.nullpointerentity.death.error.connection_lost"), false);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // can't escape until duration is over
        return false;
    }

    @Override
    public boolean shouldPause() {
        // don't actually pause the game
        return false;
    }
}

