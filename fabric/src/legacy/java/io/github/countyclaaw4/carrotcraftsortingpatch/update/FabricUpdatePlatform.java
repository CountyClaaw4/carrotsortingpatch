package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;
import io.github.countyclaaw4.carrotcraftsortingpatch.screen.FirstLaunchScreen;
import io.github.countyclaaw4.carrotcraftsortingpatch.screen.TitleScreenToast;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.Optional;

public final class FabricUpdatePlatform implements UpdatePlatform {

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getModsDir() {
        return FabricLoader.getInstance().getGameDir().resolve("mods");
    }

    @Override
    public Optional<Path> getCurrentJarPath() {
        return FabricLoader.getInstance()
                .getModContainer(CarrotSortingPatch.MOD_ID)
            .map(ModContainer::getOrigin)
            .map(origin -> origin.getPaths())
            .filter(paths -> !paths.isEmpty())
            .map(paths -> paths.get(0));
    }

    @Override
    public String getCurrentVersion() {
        return FabricLoader.getInstance()
                .getModContainer(CarrotSortingPatch.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("0.0.0");
    }

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

    @Override
    public void scheduleFirstLaunchPopup(UpdaterConfig config) {
        FirstLaunchScreenScheduler.schedule(config);
    }
}