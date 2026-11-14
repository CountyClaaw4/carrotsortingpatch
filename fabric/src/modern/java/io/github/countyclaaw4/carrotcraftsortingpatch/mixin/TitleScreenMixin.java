package io.github.countyclaaw4.carrotcraftsortingpatch.mixin;

import io.github.countyclaaw4.carrotcraftsortingpatch.screen.FirstLaunchScreen;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.FirstLaunchScreenScheduler;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdaterConfig;
import net.minecraft.client.Minecraft;
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
        mc.setScreenAndShow(new FirstLaunchScreen(mc.screen, config));
    }
}