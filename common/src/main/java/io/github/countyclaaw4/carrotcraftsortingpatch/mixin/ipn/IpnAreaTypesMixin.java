package io.github.countyclaaw4.carrotcraftsortingpatch.mixin.ipn;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.ServerCondition;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.SlotLayout;
import io.github.countyclaaw4.carrotcraftsortingpatch.condition.VaultGuiDetector;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.InterceptorRegistry;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.ipn.IpnInterceptor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Inventory Profiles Next が「コンテナ側の操作対象スロット」を組み立てた結果から、保護スロットを外す。
 * IPN 本来の処理は変えず、できあがった一覧から取り除くだけ（IPN が入っていなければ @Pseudo で何も起きない）。
 */
@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.inventory.AreaTypes", remap = false)
public class IpnAreaTypesMixin {

    @Inject(method = "fillSlots", at = @At("RETURN"), remap = false, require = 0)
    private void carrotSortingPatch$afterFillSlots(CallbackInfo ci, @Local(argsOnly = true, index = 3) List<Integer> slotIndices) {
        try {
            if (slotIndices.isEmpty() || !ServerCondition.isTargetServer()) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return;
            }
            AbstractContainerMenu menu = mc.player.containerMenu;
            if (!VaultGuiDetector.isVaultGui(menu)
                    || slotIndices.stream().anyMatch(position -> position < 0 || position >= menu.slots.size())) {
                return;
            }

            // slotIndices の値はメニュー内の位置。そのコンテナ内番号が保護対象なら外す
            int[] containerIndices = SlotLayout.containerIndices(VaultGuiDetector.playerSlotFlags(menu.slots));
            int before = slotIndices.size();
            slotIndices.removeIf(position -> position >= 0 && position < containerIndices.length
                    && InterceptorRegistry.shouldProtectSlot(IpnInterceptor.MOD_ID, containerIndices[position]));
            if (slotIndices.size() != before) {
                CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] IPN の操作対象から保護スロットを除外しました ({} -> {} スロット)",
                        before, slotIndices.size());
            }
        } catch (Throwable t) {
            CarrotSortingPatch.LOGGER.error("[CarrotSortingPatch] IPN 連携でエラーが発生しました（保護なしで続行します）", t);
        }
    }
}
