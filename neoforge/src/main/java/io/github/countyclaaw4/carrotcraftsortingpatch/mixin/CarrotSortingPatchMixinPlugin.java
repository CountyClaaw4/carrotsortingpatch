package io.github.countyclaaw4.carrotcraftsortingpatch.mixin;

import net.neoforged.fml.loading.FMLLoader;
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
        String modId = null;
        if (mixinClassName.contains(".mixin.ipn.")) {
            modId = "inventoryprofilesnext";
        } else if (mixinClassName.contains(".mixin.clientsort.")) {
            modId = "clientsort";
        }

        if (modId == null) {
            return true;
        }

        boolean modLoaded = FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        if (modLoaded) {
            LOGGER.info("[CarrotSortingPatch] {}が検出されたため、連携Mixinを適用します: {}", modId, mixinClassName);
        }
        return modLoaded;
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
