package io.github.countyclaaw4.CarrotCraftSortingPatch.interceptor.clientsort;

import io.github.countyclaaw4.CarrotCraftSortingPatch.CarrotSortingPatch;
import io.github.countyclaaw4.CarrotCraftSortingPatch.interceptor.SortInterceptor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ClientSortInterceptor implements SortInterceptor {
    public static final String MOD_ID = "clientsort";

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
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] ClientSort用の保護インターセプターが有効化されています (スロット45〜53を除外)");
        }
    }
}
