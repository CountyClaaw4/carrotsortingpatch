package io.github.countyclaaw4.carrotcraftsortingpatch.mixin;

import io.github.countyclaaw4.carrotcraftsortingpatch.screen.TitleScreenToast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenToastMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void carrotsortingpatch$renderToast(
            GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TitleScreenToast.Message message = TitleScreenToast.current();
        if (message == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int textWidth = Math.max(font.width(message.title()), font.width(message.description()));
        int panelWidth = Math.min(Math.max(180, textWidth + 20), width - 20);
        int x = width - panelWidth - 10;
        int y = 10;

        graphics.fill(x, y, x + panelWidth, y + 44, 0xE010151C);
        graphics.renderOutline(x, y, panelWidth, 44, 0xFFFFAA00);
        graphics.drawString(font, message.title(), x + 10, y + 7, 0xFFFFAA00);
        graphics.drawString(font, message.description(), x + 10, y + 23, 0xFFFFFFFF);
    }
}