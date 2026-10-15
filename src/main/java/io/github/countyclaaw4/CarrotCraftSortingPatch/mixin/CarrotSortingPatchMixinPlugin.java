package io.github.countyclaaw4.carrotcraftsortingpatch.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CarrotSortingPatchMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("CarrotSortingPatch");

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".mixin.ipn.")) {
            boolean ipnLoaded = FabricLoader.getInstance().isModLoaded("inventoryprofilesnext");
            if (ipnLoaded) {
                LOGGER.info("[CarrotSortingPatch] IPNが検出されたため、IPN連携Mixinを適用します: {}", mixinClassName);
            }
            return ipnLoaded;
        }

        if (mixinClassName.contains(".mixin.clientsort.")) {
            boolean clientSortLoaded = FabricLoader.getInstance().isModLoaded("clientsort");
            if (clientSortLoaded) {
                LOGGER.info("[CarrotSortingPatch] ClientSortが検出されたため、ClientSort連携Mixinを適用します: {}", mixinClassName);
            }
            return clientSortLoaded;
        }

        if (mixinClassName.contains(".mixin.nemos.")) {
            boolean nemosLoaded = FabricLoader.getInstance().isModLoaded("nemos_inventory_sorting");
            if (nemosLoaded) {
                LOGGER.info("[CarrotSortingPatch] NemosInventorySortingが検出されたため、連携Mixinを適用します: {}", mixinClassName);
            }
            return nemosLoaded;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
