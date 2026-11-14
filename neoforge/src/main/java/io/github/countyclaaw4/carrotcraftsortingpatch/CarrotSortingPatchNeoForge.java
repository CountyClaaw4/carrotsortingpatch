package io.github.countyclaaw4.carrotcraftsortingpatch;

import io.github.countyclaaw4.carrotcraftsortingpatch.platform.ModLoader;
import io.github.countyclaaw4.carrotcraftsortingpatch.screen.FirstLaunchScreen;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.NeoForgeFirstLaunchScheduler;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.NeoForgeUpdatePlatform;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdateCoordinator;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdaterConfig;
import io.github.countyclaaw4.carrotcraftsortingpatch.screen.UpdateConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CarrotSortingPatch.MOD_ID, dist = Dist.CLIENT)
public final class CarrotSortingPatchNeoForge {

    public CarrotSortingPatchNeoForge() {
        ModLoader.initialize(modId -> ModList.get().isLoaded(modId));
        CarrotSortingPatch.initialize();

        // 更新管理を初期化
        UpdateCoordinator.init(new NeoForgeUpdatePlatform());

        ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (container, parent) -> new UpdateConfigScreen(
                parent, UpdateCoordinator.getInstance().getConfig()));

        // TitleScreen 表示後のポップアップ挿入イベントを登録
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(this);
    }

    /**
     * TitleScreen（またはすべての Screen の init 後）に初回起動ポップアップを挿入します。
     */
    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen)) {
            return;
        }
        UpdaterConfig config = NeoForgeFirstLaunchScheduler.pollPending();
        if (config == null) return;

        NeoForgeScreenOpener.open(new FirstLaunchScreen(event.getScreen(), config));
    }
}