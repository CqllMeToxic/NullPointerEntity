package lol.cqllmetoxic.nullpointerentity.privacy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * GUI screen for configuring privacy settings.
 * allows players to choose between real system data or randomized fake data.
 * appears on first launch or when opened via command.
 */
@Environment(EnvType.CLIENT)
public class PrivacyScreen extends Screen {
    private final Screen parent;
    private boolean privacyEnabled;

    /**
     * creates a new privacy configuration screen.
     *
     * @param parent the screen to return to when closed
     */
    public PrivacyScreen(Screen parent) {
        super(Text.literal("NullPointerEntity Privacy Settings"));
        this.parent = parent;
        this.privacyEnabled = PrivacyManager.isPrivacyEnabled();
    }

    @Override
    protected void init() {
        super.init();

        if (this.client != null && this.client.mouse != null) {
            this.client.mouse.unlockCursor();
        }

        int centerX = this.width / 2;
        int buttonWidth = 140;
        int buttonHeight = 24;
        int buttonSpacing = 40;
        int buttonY = this.height - 80;

        if (isMultiplayerWorld()) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("I Understand"),
                button -> {
                    PrivacyManager.setFirstTimeUser(false);

                    String worldName = getCurrentWorldName();
                    if (worldName != null) {
                        PrivacyManager.markWorldAsProcessed(worldName);
                    }

                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                })
                .dimensions(centerX - buttonWidth/2, buttonY, buttonWidth, buttonHeight)
                .build());
        } else {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("I Understand"),
                button -> {
                    PrivacyManager.setPrivacyEnabled(privacyEnabled);
                    PrivacyManager.setFirstTimeUser(false);

                    String worldName = getCurrentWorldName();
                    if (worldName != null) {
                        PrivacyManager.markWorldAsProcessed(worldName);
                    }

                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                })
                .dimensions(centerX - buttonWidth - buttonSpacing/2, buttonY, buttonWidth, buttonHeight)
                .build());

            this.addDrawableChild(ButtonWidget.builder(
                privacyEnabled ?
                    Text.literal("Privacy: ON").formatted(Formatting.GREEN) :
                    Text.literal("Privacy: OFF").formatted(Formatting.RED),
                button -> {
                    privacyEnabled = !privacyEnabled;
                    button.setMessage(privacyEnabled ?
                        Text.literal("Privacy: ON").formatted(Formatting.GREEN) :
                        Text.literal("Privacy: OFF").formatted(Formatting.RED));
                })
                .dimensions(centerX + buttonSpacing/2, buttonY, buttonWidth, buttonHeight)
                .build());
        }
    }

    private String getCurrentWorldName() {
        if (this.client != null && this.client.getServer() != null && this.client.getServer().getSaveProperties() != null) {
            return this.client.getServer().getSaveProperties().getLevelName();
        }
        return "multiplayer_world"; // default for multiplayer
    }

    private boolean isMultiplayerWorld() {
        // use the centralized multiplayer detection utility
        return lol.cqllmetoxic.nullpointerentity.util.MultiplayerDetection.isMultiplayerClient();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // render background with darker overlay for better text readability
        context.fillGradient(0, 0, this.width, this.height, 0xCC000000, 0xDD000000);

        int centerX = this.width / 2;
        int currentY = 40;

        // title with better spacing
        context.drawCenteredTextWithShadow(this.textRenderer, "NullPointerEntity", centerX, currentY, 0xFFFFFF);
        currentY += 25;

        // attribution
        context.drawCenteredTextWithShadow(this.textRenderer, "Inspired by the success of \"Split Self\" by Pryzmm", centerX, currentY, 0x888888);
        currentY += 35;

        if (isMultiplayerWorld()) {
            // multiplayer message with better spacing
            context.drawCenteredTextWithShadow(this.textRenderer, "This mod is DISABLED in multiplayer environments.", centerX, currentY, 0xFF6666);
            currentY += 25;

            context.drawCenteredTextWithShadow(this.textRenderer, "Mod features are disabled and not functional.", centerX, currentY, 0xCCCCCC);
            currentY += 20;
            context.drawCenteredTextWithShadow(this.textRenderer, "Only vanilla features or mods you have are useable.", centerX, currentY, 0xCCCCCC);
            currentY += 25;

            context.drawCenteredTextWithShadow(this.textRenderer, "To use full mod features, play in single player.", centerX, currentY, 0xFFFF55);
        } else {
            // single player message with better spacing
            context.drawCenteredTextWithShadow(this.textRenderer, "This mod may access personal information.", centerX, currentY, 0xFFFFFF);
            currentY += 30;

            // privacy status with prominent display
            String privacyStatus = "Privacy Mode: " + (privacyEnabled ? "ENABLED" : "DISABLED");
            int privacyColor = privacyEnabled ? 0x55FF55 : 0xFF5555;
            context.drawCenteredTextWithShadow(this.textRenderer, privacyStatus, centerX, currentY, privacyColor);
            currentY += 25;

            // explanation of what privacy mode does
            String privacyExplanation = privacyEnabled ?
                "Personal information will be RANDOMIZED and protected." :
                "Real personal information will be displayed.";
            int explanationColor = privacyEnabled ? 0xAAFFAA : 0xFFFF55;
            context.drawCenteredTextWithShadow(this.textRenderer, privacyExplanation, centerX, currentY, explanationColor);
            currentY += 30;

            context.drawCenteredTextWithShadow(this.textRenderer, "Toggle privacy mode and click 'I Understand' to continue.", centerX, currentY, 0xCCCCCC);
        }

        // render the buttons and other child widgets
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // force user to make a choice
    }

    @Override
    public boolean shouldPause() {
        return true; // pause the game when this screen is open
    }

    @Override
    public void tick() {
        super.tick();

        // continuously ensure cursor is unlocked while screen is active
        if (this.client != null && this.client.mouse != null) {
            this.client.mouse.unlockCursor();
        }
    }

    @Override
    public void close() {
        // ensure cursor is unlocked when closing
        if (this.client != null && this.client.mouse != null) {
            this.client.mouse.unlockCursor();
        }
        super.close();
    }
}
