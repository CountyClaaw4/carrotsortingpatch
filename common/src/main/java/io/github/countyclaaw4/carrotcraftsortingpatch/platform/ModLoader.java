package io.github.countyclaaw4.carrotcraftsortingpatch.platform;

import java.util.Objects;
import java.util.function.Predicate;

public final class ModLoader {
    private static volatile Predicate<String> modLoadedPredicate;

    private ModLoader() {}

    public static synchronized void initialize(Predicate<String> predicate) {
        if (modLoadedPredicate != null) {
            throw new IllegalStateException("Mod loader services have already been initialized");
        }
        modLoadedPredicate = Objects.requireNonNull(predicate);
    }

    public static boolean isModLoaded(String modId) {
        Predicate<String> predicate = modLoadedPredicate;
        if (predicate == null) {
            throw new IllegalStateException("Mod loader services have not been initialized");
        }
        return predicate.test(modId);
    }
}
