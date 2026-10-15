package io.github.countyclaaw4.carrotcraftsortingpatch.mixin.ipn;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.ServerCondition;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.VaultGuiDetector;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.InterceptorRegistry;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.ipn.IpnInterceptor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.inventory.ContainerClicker", remap = false)
public class IpnContainerClickerMixin {

    @Inject(method = "genericClick(Lnet/minecraft/world/inventory/AbstractContainerMenu;IILnet/minecraft/world/inventory/ContainerInput;Z)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGenericClick(AbstractContainerMenu container, int slotId, int button, ContainerInput actionType, boolean contentUpdates, CallbackInfo ci) {
        if (!ServerCondition.isTargetServer()) {
            return;
        }

        if (!VaultGuiDetector.isVaultGui(container)) {
            return;
        }

        int containerSlotIndex = VaultGuiDetector.getContainerSlotIndex(container, slotId);
        if (InterceptorRegistry.shouldProtectSlot(IpnInterceptor.MOD_ID, containerSlotIndex)) {
            CarrotSortingPatch.LOGGER.warn("[CarrotSortingPatch] IPNによる保護スロットへの自動クリックを遮断しました: slotId={}, containerSlotIndex={}, action={}",
                    slotId, containerSlotIndex, actionType);
            ci.cancel();
        }
    }
}
