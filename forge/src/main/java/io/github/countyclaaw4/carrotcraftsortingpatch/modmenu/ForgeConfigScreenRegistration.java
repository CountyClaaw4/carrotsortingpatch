package io.github.countyclaaw4.carrotcraftsortingpatch.modmenu;

import io.github.countyclaaw4.carrotcraftsortingpatch.screen.UpdateConfigScreen;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdateCoordinator;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ForgeConfigScreenRegistration {

    private ForgeConfigScreenRegistration() {}

    public static void register(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> {
                    UpdateCoordinator coordinator = UpdateCoordinator.getInstance();
                    return coordinator == null
                            ? null
                            : new UpdateConfigScreen(parent, coordinator.getConfig());
                }));
    }
}