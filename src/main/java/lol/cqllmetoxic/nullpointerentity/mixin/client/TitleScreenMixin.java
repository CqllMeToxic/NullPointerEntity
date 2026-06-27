package lol.cqllmetoxic.nullpointerentity.mixin.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * adds a Shriek download button to the title screen when the mod is missing.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private static final String SHRIEK_MOD_ID = "shriek";
    private static final String SHRIEK_URL = "https://modrinth.com/mod/shriek";
    private static final Identifier SHRIEK_ICON = Identifier.of(NullPointerEntity.MOD_ID, "textures/gui/shriek_logo.png");
    private static final int ICON_SIZE = 32;
    private static final int ICON_PADDING = 8;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addShriekDownloadButton(CallbackInfo ci) {
        if (FabricLoader.getInstance().isModLoaded(SHRIEK_MOD_ID)) {
            return;
        }

        int x = ICON_PADDING;
        int y = ICON_PADDING;

        ShriekIconButton button = new ShriekIconButton(
            x,
            y,
            ICON_SIZE,
            ICON_SIZE,
            Text.translatable("screen.nullpointerentity.shriek_download"),
            this::openShriekLink
        );
        button.setTooltip(Tooltip.of(Text.translatable("screen.nullpointerentity.shriek_download.tooltip")));
        this.addDrawableChild(button);
    }

    private void openShriekLink() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new ConfirmLinkScreen(confirmed -> {
            if (confirmed) {
                Util.getOperatingSystem().open(SHRIEK_URL);
            }
            client.setScreen(this);
        }, SHRIEK_URL, true));
    }

    private static final class ShriekIconButton extends ClickableWidget {
        private final Runnable onPress;

        private ShriekIconButton(int x, int y, int width, int height, Text message, Runnable onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.onPress.run();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            if (this.isHovered()) {
                context.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, 0x80FFFFFF);
            }

            context.drawTexture(
                RenderLayer::getGuiTextured,
                SHRIEK_ICON,
                this.getX(),
                this.getY(),
                0.0f,
                0.0f,
                this.width,
                this.height,
                this.width,
                this.height
            );
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(NarrationPart.TITLE, this.getMessage());
        }
    }
}
