package io.github.countyclaaw4.CarrotCraftSortingPatch.mixin.nemos;

import io.github.countyclaaw4.CarrotCraftSortingPatch.condition.ServerCondition;
import io.github.countyclaaw4.CarrotCraftSortingPatch.condition.VaultGuiDetector;
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
        if (startIndex != 0 || !VaultGuiDetector.isProtectedContainerSlot(slotIndex) || !ServerCondition.isTargetServer()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.player != null && VaultGuiDetector.isVaultGui(minecraft.player.containerMenu)) {
            cir.setReturnValue(true);
        }
    }
}