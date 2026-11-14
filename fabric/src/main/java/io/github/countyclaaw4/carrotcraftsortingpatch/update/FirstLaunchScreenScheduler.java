package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 初回起動ポップアップの表示タイミングを制御します。
 *
 * <p>Fabric の {@code onInitializeClient()} はゲームの起動直後に呼ばれるため、
 * まだ Minecraft の Screen が存在しません。そのため Mixin で
 * {@code TitleScreen.init()} が完了した直後にここを参照してポップアップを表示します。
 */
public final class FirstLaunchScreenScheduler {

    private static final AtomicReference<UpdaterConfig> pendingConfig = new AtomicReference<>(null);

    private FirstLaunchScreenScheduler() {}

    /** ポップアップ表示をスケジュールします。 */
    public static void schedule(UpdaterConfig config) {
        pendingConfig.set(config);
    }

    /**
     * 保留中の {@link UpdaterConfig} を取得して除去します。
     * タイトル画面の Mixin から呼ばれます。
     *
     * @return 保留があれば {@code UpdaterConfig}、なければ {@code null}
     */
    public static UpdaterConfig pollPending() {
        return pendingConfig.getAndSet(null);
    }
}