package io.github.countyclaaw4.carrotcraftsortingpatch.mixin;

import io.github.countyclaaw4.carrotcraftsortingpatch.update.ReleaseInfo;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdateCoordinator;
import net.neoforged.fml.VersionChecker;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = VersionChecker.class, remap = false)
public abstract class NeoForgeVersionCheckerMixin {

    private static final String MOD_ID = "carrotsortingpatch";
    private static final String RELEASES_URL = "https://github.com/carrot-craft/carrotsortingpatch/releases";

    @Inject(method = "getResult", at = @At("HEAD"), cancellable = true, remap = false)
    private static void carrotsortingpatch$useGitHubRelease(
            IModInfo mod, CallbackInfoReturnable<VersionChecker.CheckResult> cir) {
        if (!MOD_ID.equals(mod.getModId())) {
            return;
        }

        UpdateCoordinator coordinator = UpdateCoordinator.getInstance();
        if (coordinator == null) {
            return;
        }

        ReleaseInfo release = coordinator.getLatestRelease().orElse(null);
        if (release == null) {
            return;
        }

        String version = release.getTagName().replaceFirst("^[vV]", "");
        cir.setReturnValue(new VersionChecker.CheckResult(
                VersionChecker.Status.OUTDATED,
                new ComparableVersion(version),
                Map.of(),
                RELEASES_URL));
    }
}