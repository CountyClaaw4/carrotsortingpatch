package io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.nemos;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.SortInterceptor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class NemosInterceptor implements SortInterceptor {
    public static final String MOD_ID = "nemos_inventory_sorting";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    @Override
    public void onVaultGuiDetected(AbstractContainerMenu menu) {
        if (isAvailable()) {
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] NemosInventorySortingのソート対象から保護スロット45〜53を除外します");
        }
    }
}