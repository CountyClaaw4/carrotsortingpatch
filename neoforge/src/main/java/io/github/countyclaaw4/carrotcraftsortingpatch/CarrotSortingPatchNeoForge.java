package io.github.countyclaaw4.carrotcraftsortingpatch;

import io.github.countyclaaw4.carrotcraftsortingpatch.platform.ModLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(value = CarrotSortingPatch.MOD_ID, dist = Dist.CLIENT)
public final class CarrotSortingPatchNeoForge {
    public CarrotSortingPatchNeoForge() {
        ModLoader.initialize(modId ->
                FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null);
        CarrotSortingPatch.initialize();
    }
}
