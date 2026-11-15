package io.github.countyclaaw4.carrotcraftsortingpatch.mixin;

import io.github.countyclaaw4.carrotcraftsortingpatch.screen.FirstLaunchScreen;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.FirstLaunchScreenScheduler;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdaterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void carrotSortingPatch$onInit(CallbackInfo ci) {
        UpdaterConfig config = FirstLaunchScreenScheduler.pollPending();
        if (config == null) return;

        Minecraft mc = Minecraft.getInstance();
        mc.setScreenAndShow(new FirstLaunchScreen(getCurrentScreen(mc), config));
    }

    /* 成果物を増やさないためリフレクションを使う。 */
    private static Screen getCurrentScreen(Minecraft mc) {
        try {
            // Minecraft 26.1.2
            var field = Minecraft.class.getField("screen");
            return (Screen) field.get(mc);
        } catch (NoSuchFieldException ignored) {
            // Minecraft 26.2+
            try {
                var guiField = Minecraft.class.getField("gui");
                Object gui = guiField.get(mc);

                var screenMethod = gui.getClass().getMethod("screen");
                return (Screen) screenMethod.invoke(gui);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("現在の画面を取得できません", e);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("現在の画面を取得できません", e);
        }
    }
}
