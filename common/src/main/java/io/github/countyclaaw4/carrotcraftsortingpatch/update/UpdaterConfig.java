package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 自動更新に関するユーザー設定を管理します。
 * <p>
 * 設定ファイル: &lt;config-dir&gt;/carrotsortingpatch/updater.properties
 * <ul>
 *   <li>{@code check_for_updates} – 更新を確認してトーストで通知するか（デフォルト: true）</li>
 *   <li>{@code auto_update} – 更新があった際に自動でダウンロード・入れ替えするか（デフォルト: false）</li>
 * </ul>
 */
public final class UpdaterConfig {

    // ---- キー ----
    private static final String KEY_CHECK = "check_for_updates";
    private static final String KEY_AUTO  = "auto_update";

    // ---- デフォルト値 ----
    private static final boolean DEFAULT_CHECK = true;
    private static final boolean DEFAULT_AUTO  = false;

    private boolean checkForUpdates = DEFAULT_CHECK;
    private boolean autoUpdate      = DEFAULT_AUTO;

    /** 設定ファイルが今回新規作成されたか（=初回起動フラグ） */
    private boolean firstLaunch = false;

    private final Path configFile;

    public UpdaterConfig(Path configDir) {
        this.configFile = configDir
                .resolve(CarrotSortingPatch.MOD_ID)
                .resolve("updater.properties");
    }

    // ---- ゲッター ----

    public boolean isCheckForUpdates() { return checkForUpdates; }
    public boolean isAutoUpdate()       { return autoUpdate; }
    public boolean isFirstLaunch()      { return firstLaunch; }

    // ---- セッター ----

    public void setCheckForUpdates(boolean value) {
        this.checkForUpdates = value;
    }

    public void setAutoUpdate(boolean value) {
        this.autoUpdate = value;
    }

    // ---- ファイル I/O ----

    /**
     * 設定ファイルを読み込みます。
     * ファイルが存在しない場合はデフォルト値のまま {@link #firstLaunch} を {@code true} にします。
     */
    public void load() {
        if (!Files.exists(configFile)) {
            firstLaunch = true;
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] 設定ファイルが見つかりません。初回起動として扱います: {}", configFile);
            return;
        }
        try (InputStream in = Files.newInputStream(configFile)) {
            Properties props = new Properties();
            props.load(in);
            checkForUpdates = Boolean.parseBoolean(props.getProperty(KEY_CHECK, String.valueOf(DEFAULT_CHECK)));
            autoUpdate      = Boolean.parseBoolean(props.getProperty(KEY_AUTO,  String.valueOf(DEFAULT_AUTO)));
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] 設定を読み込みました: checkForUpdates={}, autoUpdate={}",
                    checkForUpdates, autoUpdate);
        } catch (IOException e) {
            CarrotSortingPatch.LOGGER.warn("[CarrotSortingPatch] 設定ファイルの読み込みに失敗しました。デフォルト値を使用します", e);
        }
    }

    /**
     * 現在の設定をファイルに保存します。
     */
    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            Properties props = new Properties();
            props.setProperty(KEY_CHECK, String.valueOf(checkForUpdates));
            props.setProperty(KEY_AUTO,  String.valueOf(autoUpdate));
            try (OutputStream out = Files.newOutputStream(configFile)) {
                props.store(out, "CarrotSortingPatch updater settings");
            }
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] 設定を保存しました: {}", configFile);
        } catch (IOException e) {
            CarrotSortingPatch.LOGGER.warn("[CarrotSortingPatch] 設定ファイルの保存に失敗しました", e);
        }
    }
}