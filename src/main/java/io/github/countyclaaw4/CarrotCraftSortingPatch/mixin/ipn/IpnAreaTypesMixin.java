package io.github.countyclaaw4.CarrotCraftSortingPatch.mixin.ipn;

import io.github.countyclaaw4.CarrotCraftSortingPatch.CarrotSortingPatch;
import io.github.countyclaaw4.CarrotCraftSortingPatch.condition.ServerCondition;
import io.github.countyclaaw4.CarrotCraftSortingPatch.condition.VaultGuiDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.inventory.AreaTypes", remap = false)
public class IpnAreaTypesMixin {

    @Inject(method = "fillSlots", at = @At("HEAD"), cancellable = true, remap = false)
    private void onFillSlots(Set<?> types, List<Slot> vanillaSlots, List<Integer> slotIndices, CallbackInfo ci) {
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

        // 保管庫GUIの場合: コンテナ領域のスロット45〜53を除外してslotIndicesを構築
        int containerSlotIndex = 0;
        for (int slotIndex = 0; slotIndex < vanillaSlots.size(); slotIndex++) {
            Slot slot = vanillaSlots.get(slotIndex);
            if (slot.container instanceof Inventory) {
                // プレイヤーインベントリはスキップ (IPNの既存処理と同じ)
                continue;
            }

            // コンテナ領域内インデックス 45〜53 を除外
            if (VaultGuiDetector.isProtectedContainerSlot(containerSlotIndex)) {
                containerSlotIndex++;
                continue;
            }

            containerSlotIndex++;
            slotIndices.add(slotIndex);
        }

        CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] IPNの操作対象から最下段9スロット(45〜53)を除外しました (有効スロット数: {})", slotIndices.size());
        ci.cancel();
    }
}
