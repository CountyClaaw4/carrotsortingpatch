package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;
import io.github.countyclaaw4.carrotcraftsortingpatch.screen.FirstLaunchScreen;
import io.github.countyclaaw4.carrotcraftsortingpatch.screen.TitleScreenToast;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;

/**
 * NeoForge 向けの {@link UpdatePlatform} 実装です。
 */
public final class NeoForgeUpdatePlatform implements UpdatePlatform {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path getModsDir() {
        return FMLPaths.MODSDIR.get();
    }

    @Override
    public Optional<Path> getCurrentJarPath() {
        return ModList.get()
                .getModContainerById(CarrotSortingPatch.MOD_ID)
                .map(c -> c.getModInfo().getOwningFile().getFile().getFilePath());
    }

    @Override
    public String getCurrentVersion() {
        return ModList.get()
                .getModContainerById(CarrotSortingPatch.MOD_ID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("0.0.0");
    }

    // ---- トースト通知 ----

    @Override
    public void showUpdateToast(String newVersion) {
        TitleScreenToast.show(
            Component.translatable("toast.carrotsortingpatch.update_available.title"),
            Component.translatable("toast.carrotsortingpatch.update_available.desc", newVersion));
    }

    @Override
    public void showDownloadStartToast() {
        TitleScreenToast.show(
            Component.translatable("toast.carrotsortingpatch.download_start.title"),
            Component.translatable("toast.carrotsortingpatch.download_start.desc"));
    }

    @Override
    public void showDownloadCompleteToast() {
        TitleScreenToast.show(
            Component.translatable("toast.carrotsortingpatch.download_complete.title"),
            Component.translatable("toast.carrotsortingpatch.download_complete.desc"));
    }

    @Override
    public void showDownloadFailedToast() {
        TitleScreenToast.show(
            Component.translatable("toast.carrotsortingpatch.download_failed.title"),
            Component.translatable("toast.carrotsortingpatch.download_failed.desc"));
    }

    // ---- 初回起動ポップアップ ----

    /**
     * NeoForge では {@code ClientLifecycleEvent.CLIENT_STARTED} のタイミングでスケジュールします。
     * {@link NeoForgeFirstLaunchScheduler} 経由でタイトル画面到達時に表示されます。
     */
    @Override
    public void scheduleFirstLaunchPopup(UpdaterConfig config) {
        NeoForgeFirstLaunchScheduler.schedule(config);
    }
}