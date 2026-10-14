package io.github.countyclaaw4.CarrotCraftSortingPatch.mixin.clientsort;

import io.github.countyclaaw4.CarrotCraftSortingPatch.CarrotSortingPatch;
import io.github.countyclaaw4.CarrotCraftSortingPatch.condition.ServerCondition;
import io.github.countyclaaw4.CarrotCraftSortingPatch.condition.VaultGuiDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "dev.terminalmc.clientsort.client.inventory.operator.SingleUseOperator", remap = false)
public class ClientSortOperatorMixin {

    @Inject(method = "collectSlots", at = @At("RETURN"), cancellable = true, remap = false)
    private void onCollectSlots(CallbackInfoReturnable<Slot[]> cir) {
        Slot[] originalSlots = cir.getReturnValue();
        if (originalSlots == null || originalSlots.length == 0) {
            return;
        }

        if (!ServerCondition.isTargetServer()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        if (!VaultGuiDetector.isVaultGui(menu)) {
            return;
        }

        // 保管庫GUIの場合: コンテナ領域内インデックス 45〜53 を除外
        List<Slot> filtered = new ArrayList<>();
        for (Slot slot : originalSlots) {
            int containerSlotIndex = VaultGuiDetector.getContainerSlotIndex(menu, slot.index);
            if (VaultGuiDetector.isProtectedContainerSlot(containerSlotIndex)) {
                // 最下段9スロット(45〜53)を除外
                continue;
            }
            filtered.add(slot);
        }

        if (filtered.size() != originalSlots.length) {
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] ClientSortの操作対象から最下段9スロット(45〜53)を除外しました ({} -> {} スロット)",
                    originalSlots.length, filtered.size());
            cir.setReturnValue(filtered.toArray(new Slot[0]));
        }
    }
}
