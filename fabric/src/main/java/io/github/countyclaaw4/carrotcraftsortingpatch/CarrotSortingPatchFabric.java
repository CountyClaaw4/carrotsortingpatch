package io.github.countyclaaw4.carrotcraftsortingpatch;

import io.github.countyclaaw4.carrotcraftsortingpatch.platform.ModLoader;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.FabricUpdatePlatform;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdateCoordinator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class CarrotSortingPatchFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModLoader.initialize(modId -> FabricLoader.getInstance().isModLoaded(modId));
        CarrotSortingPatch.initialize();

        // 更新管理を初期化（設定読み込み・初回起動検出・更新チェック開始）
        UpdateCoordinator.init(new FabricUpdatePlatform());
    }
}