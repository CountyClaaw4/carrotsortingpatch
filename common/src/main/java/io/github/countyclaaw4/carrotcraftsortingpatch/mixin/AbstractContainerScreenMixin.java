package io.github.countyclaaw4.carrotcraftsortingpatch.mixin;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.ServerCondition;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.VaultGuiDetector;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.InterceptorRegistry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

    @Shadow
    @Final
    protected T menu;

    @Unique
    private boolean carrotSortingPatch$detected = false;

    @Inject(method = "containerTick", at = @At("HEAD"))
    private void onContainerTick(CallbackInfo ci) {
        if (this.carrotSortingPatch$detected) {
            return;
        }

        // 1. サーバー判定 (play.carrotcraft.net)
        if (!ServerCondition.isTargetServer()) {
            return;
        }

        // 2. エンダーチェストGUI識別判定 (条件A, B, C)
        if (VaultGuiDetector.isVaultGui(this.menu)) {
            this.carrotSortingPatch$detected = true;
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] 対象のエンダーチェストGUIを検出しました (ホスト: play.carrotcraft.net, コンテナスロット数: 54, 該当スロット: 45〜53)");
            InterceptorRegistry.notifyVaultGuiDetected(this.menu);
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void onScreenClose(CallbackInfo ci) {
        this.carrotSortingPatch$detected = false;
    }
}
