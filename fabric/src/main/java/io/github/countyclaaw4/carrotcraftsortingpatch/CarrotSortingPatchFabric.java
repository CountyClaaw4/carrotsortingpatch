package io.github.countyclaaw4.carrotcraftsortingpatch;

import io.github.countyclaaw4.carrotcraftsortingpatch.platform.ModLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class CarrotSortingPatchFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModLoader.initialize(modId -> FabricLoader.getInstance().isModLoaded(modId));
        CarrotSortingPatch.initialize();
    }
}
