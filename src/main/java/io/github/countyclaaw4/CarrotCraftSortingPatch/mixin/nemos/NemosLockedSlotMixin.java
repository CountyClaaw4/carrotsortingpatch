package io.github.countyclaaw4.carrotcraftsortingpatch.mixin.nemos;

import io.github.countyclaaw4.carrotcraftsortingpatch.condition.ServerCondition;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.VaultGuiDetector;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.InterceptorRegistry;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.nemos.NemosInterceptor;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.nemonotfound.nemos.inventory.sorting.service.LockedSlotService", remap = false)
public class NemosLockedSlotMixin {

    @Inject(method = "isLocked(II)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void onIsLocked(int slotIndex, int startIndex, CallbackInfoReturnable<Boolean> cir) {
        if (startIndex != 0
                || !InterceptorRegistry.shouldProtectSlot(NemosInterceptor.MOD_ID, slotIndex)
                || !ServerCondition.isTargetServer()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null
            && minecraft.player != null
            && VaultGuiDetector.isVaultGui(minecraft.player.containerMenu)
            && InterceptorRegistry.shouldProtectSlot(NemosInterceptor.MOD_ID, slotIndex)) {
            cir.setReturnValue(true);
        }
    }
}