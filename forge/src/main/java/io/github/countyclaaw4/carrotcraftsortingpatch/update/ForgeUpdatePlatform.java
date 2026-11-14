package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import io.github.countyclaaw4.carrotcraftsortingpatch.screen.TitleScreenToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;

public final class ForgeUpdatePlatform implements UpdatePlatform {

    private final ModContainer container;

    public ForgeUpdatePlatform(ModContainer container) {
        this.container = container;
    }

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
        return Optional.of(container.getModInfo().getOwningFile().getFile().getFilePath());
    }

    @Override
    public String getCurrentVersion() {
        return container.getModInfo().getVersion().toString();
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
        UpdateCoordinator.getInstance().onConfigSaved();
    }

}