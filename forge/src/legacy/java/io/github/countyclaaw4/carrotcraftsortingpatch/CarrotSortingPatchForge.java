package io.github.countyclaaw4.carrotcraftsortingpatch;

import io.github.countyclaaw4.carrotcraftsortingpatch.platform.ModLoader;
import io.github.countyclaaw4.carrotcraftsortingpatch.modmenu.ForgeConfigScreenRegistration;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.ForgeUpdatePlatform;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdateCoordinator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * 1.21.11 向け（ModList はインスタンス経由）。クライアント専用 Mod。Forge には Mod クラス単位のサイド指定が無いが、
 * Mixin はすべて "client" 扱いなので、専用サーバーに入っていても何も適用されない。
 */
@Mod(CarrotSortingPatch.MOD_ID)
public final class CarrotSortingPatchForge {
    public CarrotSortingPatchForge(FMLJavaModLoadingContext context) {
        ModLoader.initialize(modId -> ModList.get().isLoaded(modId));
        CarrotSortingPatch.initialize();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            UpdateCoordinator.init(new ForgeUpdatePlatform(context.getContainer()));
            ForgeConfigScreenRegistration.register(context);
        }
    }
}
