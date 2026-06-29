package lol.cqllmetoxic.nullpointerentity.privacy;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntityClient;
import lol.cqllmetoxic.nullpointerentity.config.VoiceChatConfig;
import lol.cqllmetoxic.nullpointerentity.network.ClientSessionState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
    private boolean pushToTalkEnabled;
    private javax.sound.sampled.Mixer.Info[] availableMicrophones;
    private int selectedMicrophoneIndex = 0;
    private int originalGuiScale = -999;
    private static final int CONTENT_OFFSET_Y = 4;

    /**
     * creates a new privacy configuration screen.
     *
     * @param parent the screen to return to when closed
     */
    public PrivacyScreen(Screen parent) {
        super(Text.translatable("screen.nullpointerentity.privacy.title"));
        this.parent = parent;
        this.privacyEnabled = PrivacyManager.isPrivacyEnabledRaw();
        this.pushToTalkEnabled = VoiceChatConfig.isPushToTalkEnabled();

        // get available microphones
        this.availableMicrophones = lol.cqllmetoxic.nullpointerentity.audio.AudioRecorder.getAvailableMicrophones();

        // find currently selected microphone index
        String currentMic = PrivacyManager.getSelectedMicrophoneName();
        if (currentMic != null && availableMicrophones.length > 0) {
            for (int i = 0; i < availableMicrophones.length; i++) {
                if (availableMicrophones[i].getName().equals(currentMic)) {
                    selectedMicrophoneIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    protected void init() {
        if (this.originalGuiScale == -999 && this.client != null) {
            this.originalGuiScale = this.client.options.getGuiScale().getValue();
        }

        // privacy screen layout is only correct at GUI scale 3, so force it while open
        // (changing the scale triggers onResolutionChanged() -> re-init at the new scale, hence the early return)
        if (this.client != null && this.client.options.getGuiScale().getValue() != 3) {
            this.client.options.getGuiScale().setValue(3);
            return;
        }

        super.init();

        if (this.client != null && this.client.mouse != null) {
            this.client.mouse.unlockCursor();
        }

        int centerX = this.width / 2;
        int buttonWidth = 140;
        int buttonHeight = 24;
        int buttonSpacing = 40;
        int buttonY = this.height - 60;

        if (shouldShowLegacyMultiplayerNotice()) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.nullpointerentity.privacy.confirm"),
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
                .dimensions(centerX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight)
                .build());
            return;
        }

        // microphone selection button (left) + push-to-talk toggle (right) share one row
        int micButtonY = buttonY - 54;
        int rowGap = 6;
        int pttButtonWidth = 120;
        int micButtonWidth = 360 - pttButtonWidth - rowGap;
        this.addDrawableChild(ButtonWidget.builder(
            getMicrophoneButtonText(),
            button -> {
                if (availableMicrophones.length > 0) {
                    selectedMicrophoneIndex = (selectedMicrophoneIndex + 1) % availableMicrophones.length;
                    button.setMessage(getMicrophoneButtonText());
                }
            })
            .dimensions(centerX - 180, micButtonY, micButtonWidth, buttonHeight)
            .build());

        // push-to-talk toggle: when on, AURORA only hears the mic while the voice key is held
        this.addDrawableChild(ButtonWidget.builder(
            getPushToTalkButtonText(),
            button -> {
                pushToTalkEnabled = !pushToTalkEnabled;
                button.setMessage(getPushToTalkButtonText());
            })
            .dimensions(centerX - 180 + micButtonWidth + rowGap, micButtonY, pttButtonWidth, buttonHeight)
            .build());

        if (isRemoteJoiningClient()) {
            // joining (non-host) clients don't control Privacy Mode - the host's session setting drives
            // everyone. show only a centered confirm; mic + push-to-talk above stay editable per client.
            this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("screen.nullpointerentity.privacy.confirm"),
                button -> confirmAndClose(false))
                .dimensions(centerX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight)
                .build());
            return;
        }

        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.nullpointerentity.privacy.confirm"),
            button -> confirmAndClose(true))
            .dimensions(centerX - buttonWidth - buttonSpacing / 2, buttonY, buttonWidth, buttonHeight)
            .build());

        this.addDrawableChild(ButtonWidget.builder(
            getPrivacyToggleText(),
            button -> {
                privacyEnabled = !privacyEnabled;
                button.setMessage(getPrivacyToggleText());
            })
            .dimensions(centerX + buttonSpacing / 2, buttonY, buttonWidth, buttonHeight)
            .build());
    }

    /**
     * applies the chosen settings and returns to the parent screen.
     *
     * @param applyPrivacy whether to write the local Privacy Mode toggle (host/local only; joining
     *                     clients leave Privacy Mode to the host's session setting)
     */
    private void confirmAndClose(boolean applyPrivacy) {
        if (applyPrivacy) {
            PrivacyManager.setPrivacyEnabled(privacyEnabled);
        }
        PrivacyManager.setFirstTimeUser(false);

        // save selected microphone
        if (availableMicrophones.length > 0 && selectedMicrophoneIndex >= 0 && selectedMicrophoneIndex < availableMicrophones.length) {
            PrivacyManager.setSelectedMicrophone(availableMicrophones[selectedMicrophoneIndex].getName());
        }

        // save and apply push-to-talk preference live (Shriek init has already run)
        VoiceChatConfig.setPushToTalkEnabled(pushToTalkEnabled);
        NullPointerEntityClient.applyPushToTalkPreference();

        String worldName = getCurrentWorldName();
        if (worldName != null) {
            PrivacyManager.markWorldAsProcessed(worldName);
        }

        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    /**
     * true when this client is connected to someone else's server (not the integrated host). such
     * clients follow the host's Privacy Mode and shouldn't see the On/Off toggle.
     */
    private boolean isRemoteJoiningClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.world != null && !client.isIntegratedServerRunning();
    }

    private Text getMicrophoneButtonText() {
        if (availableMicrophones == null || availableMicrophones.length == 0) {
            return Text.translatable("screen.nullpointerentity.privacy.microphone.none");
        }

        if (selectedMicrophoneIndex < 0 || selectedMicrophoneIndex >= availableMicrophones.length) {
            return Text.translatable("screen.nullpointerentity.privacy.microphone.invalid");
        }

        // get microphone info
        javax.sound.sampled.Mixer.Info micInfo = availableMicrophones[selectedMicrophoneIndex];
        String micName = micInfo.getName();

        // fallback to description if name is empty
        if (micName == null || micName.trim().isEmpty()) {
            micName = micInfo.getDescription();
        }

        // fallback to vendor if description is also empty
        if (micName == null || micName.trim().isEmpty()) {
            micName = micInfo.getVendor() + " Device";
        }

        // final fallback
        if (micName == null || micName.trim().isEmpty()) {
            micName = Text.translatable("screen.nullpointerentity.privacy.microphone.fallback", selectedMicrophoneIndex + 1).getString();
        }

        // truncate if too long
        if (micName.length() > 45) {
            micName = micName.substring(0, 42) + "...";
        }

        return Text.translatable("screen.nullpointerentity.privacy.microphone.current", micName);
    }

    private Text getPushToTalkButtonText() {
        return pushToTalkEnabled
            ? Text.translatable("screen.nullpointerentity.privacy.pushtotalk.on").formatted(Formatting.GREEN)
            : Text.translatable("screen.nullpointerentity.privacy.pushtotalk.off").formatted(Formatting.RED);
    }

    private Text getPrivacyToggleText() {
        return privacyEnabled
            ? Text.translatable("screen.nullpointerentity.privacy.toggle.on").formatted(Formatting.GREEN)
            : Text.translatable("screen.nullpointerentity.privacy.toggle.off").formatted(Formatting.RED);
    }

    private String getCurrentWorldName() {
        if (this.client != null && this.client.getServer() != null && this.client.getServer().getSaveProperties() != null) {
            return this.client.getServer().getSaveProperties().getLevelName();
        }
        return "multiplayer_world"; // default for multiplayer
    }

    private boolean shouldShowLegacyMultiplayerNotice() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return false;
        }

        boolean isRemoteServer = client.getCurrentServerEntry() != null;
        return isRemoteServer && !ClientSessionState.isServerModPresent();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // render background with darker overlay for better text readability
        context.fillGradient(0, 0, this.width, this.height, 0xCC000000, 0xDD000000);

        int centerX = this.width / 2;
        int lineHeight = this.textRenderer.fontHeight;
        int headerY = 40;
        int currentY = headerY;

        // title with better spacing
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.brand").getString(), centerX, currentY, 0xFFFFFF);
        currentY += lineHeight + 10;

        // attribution
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.attribution").getString(), centerX, currentY, 0x888888);
        int headerBottom = currentY + (lineHeight * 2);
        currentY = headerBottom;

        if (shouldShowLegacyMultiplayerNotice()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.multiplayer.disabled").getString(), centerX, currentY, 0xFF6666);
            currentY += 25;

            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.multiplayer.features_off").getString(), centerX, currentY, 0xCCCCCC);
            currentY += 20;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.multiplayer.vanilla_only").getString(), centerX, currentY, 0xCCCCCC);
            currentY += 25;

            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.multiplayer.singleplayer_hint").getString(), centerX, currentY, 0xFFFF55);

            super.render(context, mouseX, mouseY, delta);
            return;
        }

        int buttonY = this.height - 60;
        int micButtonY = buttonY - 54;
        int topPadding = headerBottom + 16 + CONTENT_OFFSET_Y;
        int footerSpacing = 10;
        int availableHeight = Math.max(0, micButtonY - footerSpacing - topPadding);

        int lines = 6;
        int lineStride = 18; // 18 ensures consistent, clean spacing even with scaling
        int contentHeight = lineStride * lines;

        currentY = topPadding + Math.max(0, (availableHeight - contentHeight) / 2);

        boolean joiningClient = isRemoteJoiningClient();

        if (joiningClient) {
            // joining clients can't change Privacy Mode (the host's session setting drives it), so show
            // a note in place of the singleplayer warning + privacy status/explanation lines.
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.host_controlled.title").getString(), centerX, currentY, 0xFFFFFF);
            currentY += lineStride;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.host_controlled.detail").getString(), centerX, currentY, 0xAAAAAA);
            currentY += lineStride;
            // keep the row count consistent with the host layout
            currentY += lineStride;
        } else {
            // single player message with better spacing
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.singleplayer.warning").getString(), centerX, currentY, 0xFFFFFF);
            currentY += lineStride;

            // privacy status with prominent display
            String privacyStatus = Text.translatable(
                "screen.nullpointerentity.privacy.status",
                privacyEnabled
                    ? Text.translatable("screen.nullpointerentity.privacy.status.enabled")
                    : Text.translatable("screen.nullpointerentity.privacy.status.disabled")
            ).getString();
            int privacyColor = privacyEnabled ? 0x55FF55 : 0xFF5555;
            context.drawCenteredTextWithShadow(this.textRenderer, privacyStatus, centerX, currentY, privacyColor);
            currentY += lineStride;

            // explanation of what privacy mode does
            String privacyExplanation = (privacyEnabled
                ? Text.translatable("screen.nullpointerentity.privacy.explanation.enabled")
                : Text.translatable("screen.nullpointerentity.privacy.explanation.disabled")
            ).getString();
            int explanationColor = privacyEnabled ? 0xAAFFAA : 0xFFFF55;
            context.drawCenteredTextWithShadow(this.textRenderer, privacyExplanation, centerX, currentY, explanationColor);
            currentY += lineStride;
        }

        // microphone selection explanation
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.microphone.select").getString(), centerX, currentY, 0xFFFFFF);
        currentY += lineStride;
        if (availableMicrophones.length == 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.microphone.none_warning").getString(), centerX, currentY, 0xFF5555);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.nullpointerentity.privacy.microphone.cycle_hint").getString(), centerX, currentY, 0xAAFFAA);
        }
        currentY += lineStride;

        String finalInstruction = joiningClient
            ? "screen.nullpointerentity.privacy.joining_instruction"
            : "screen.nullpointerentity.privacy.final_instruction";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable(finalInstruction).getString(), centerX, currentY, 0xCCCCCC);

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
        if (this.originalGuiScale != -999 && this.client != null && this.client.options.getGuiScale().getValue() != this.originalGuiScale) {
            this.client.options.getGuiScale().setValue(this.originalGuiScale);
            this.client.options.write();
        }

        // ensure cursor is unlocked when closing
        if (this.client != null && this.client.mouse != null) {
            this.client.mouse.unlockCursor();
        }
        super.close();
    }
}
