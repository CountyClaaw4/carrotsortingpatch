package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 更新チェック〜通知〜ダウンロードのフローを統括します。
 *
 * <h3>フロー</h3>
 * <ol>
 *   <li>{@link UpdaterConfig} を読み込む。</li>
 *   <li>初回起動（設定ファイルなし）なら {@link UpdatePlatform#scheduleFirstLaunchPopup} を呼ぶ。</li>
 *   <li>設定で「更新チェック」が ON なら GitHub API を非同期でポーリング。</li>
 *   <li>新バージョンを発見したらトーストで通知。</li>
 *   <li>設定で「自動更新」が ON なら非同期でダウンロードし、シャットダウン時に入れ替えスケジュール。</li>
 * </ol>
 */
public final class UpdateCoordinator {

    private static volatile UpdateCoordinator instance;

    private final UpdatePlatform platform;
    private final UpdaterConfig  config;

    // 外部から参照できるようにキャッシュ
    private volatile ReleaseInfo latestRelease;

    private UpdateCoordinator(UpdatePlatform platform) {
        this.platform = platform;
        this.config   = new UpdaterConfig(platform.getConfigDir());
    }

    // ---- シングルトン ----

    public static UpdateCoordinator getInstance() {
        return instance;
    }

    public static void init(UpdatePlatform platform) {
        if (instance != null) {
            throw new IllegalStateException("UpdateCoordinator はすでに初期化されています");
        }
        instance = new UpdateCoordinator(platform);
        instance.start();
    }

    // ---- 設定ゲッター ----

    public UpdaterConfig getConfig() {
        return config;
    }

    public Optional<ReleaseInfo> getLatestRelease() {
        return Optional.ofNullable(latestRelease);
    }

    public boolean downloadLatestUpdate() {
        ReleaseInfo release = latestRelease;
        if (release == null) {
            return false;
        }
        startAutoDownload(release);
        return true;
    }

    // ---- 起動処理 ----

    private void start() {
        config.load();

        // 初回起動: ポップアップをスケジュール（Minecraft 起動後に表示）
        if (config.isFirstLaunch()) {
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] 初回起動を検出しました。設定ポップアップを表示します。");
            platform.scheduleFirstLaunchPopup(config);
            return; // 初回は設定保存まで更新チェックしない
        }

        if (config.isCheckForUpdates()) {
            startUpdateCheck();
        }
    }

    /** 設定保存後（ポップアップ確定時）に呼ぶ: 必要であれば更新チェックを開始します。 */
    public void onConfigSaved() {
        if (config.isCheckForUpdates()) {
            startUpdateCheck();
        }
    }

    // ---- 更新チェック ----

    private void startUpdateCheck() {
        String current = platform.getCurrentVersion();
        String currentJarName = platform.getCurrentJarPath()
            .map(Path::getFileName)
            .map(Path::toString)
            .orElse(null);
        CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] 更新チェックを開始します（現在: {}）", current);

        UpdateManager.checkAsync(current, currentJarName)
                .thenAccept(optInfo -> {
                    if (optInfo.isEmpty()) return;
                    ReleaseInfo info = optInfo.get();
                    latestRelease = info;

                    // Minecraft スレッドへのマーシャリングはプラットフォーム実装側で行う
                    platform.showUpdateToast(info.getTagName());

                    if (config.isAutoUpdate()) {
                        startAutoDownload(info);
                    }
                })
                .exceptionally(e -> {
                    CarrotSortingPatch.LOGGER.warn("[CarrotSortingPatch] 更新チェックに失敗しました", e);
                    return null;
                });
    }

    private void startAutoDownload(ReleaseInfo info) {
        Optional<Path> jarOpt = platform.getCurrentJarPath();
        if (jarOpt.isEmpty()) {
            CarrotSortingPatch.LOGGER.warn("[CarrotSortingPatch] 現在の jar パスを取得できません。自動更新をスキップします。");
            return;
        }

        platform.showDownloadStartToast();

        Path currentJar = jarOpt.get();
        Path modsDir    = platform.getModsDir();

        UpdateManager.downloadAndScheduleSwap(info, currentJar, modsDir, progress -> {
                    // 進捗ログ（10% 刻み）
                    int pct = (int) (progress * 100);
                    if (pct % 10 == 0) {
                        CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] ダウンロード進行: {}%", pct);
                    }
                })
                .thenAccept(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        platform.showDownloadCompleteToast();
                    } else {
                        platform.showDownloadFailedToast();
                    }
                })
                .exceptionally(e -> {
                    CarrotSortingPatch.LOGGER.error("[CarrotSortingPatch] 自動ダウンロードで予期しないエラー", e);
                    platform.showDownloadFailedToast();
                    return null;
                });
    }
}