package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import java.util.concurrent.atomic.AtomicReference;

/**
 * NeoForge 向けの初回起動ポップアップスケジューラー。
 * Fabric 版の {@code FirstLaunchScreenScheduler} と同等の役割を持ちます。
 */
public final class NeoForgeFirstLaunchScheduler {

    private static final AtomicReference<UpdaterConfig> pendingConfig = new AtomicReference<>(null);

    private NeoForgeFirstLaunchScheduler() {}

    public static void schedule(UpdaterConfig config) {
        pendingConfig.set(config);
    }

    public static UpdaterConfig pollPending() {
        return pendingConfig.getAndSet(null);
    }
}