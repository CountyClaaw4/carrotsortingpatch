package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import java.nio.file.Path;
import java.util.Optional;

/**
 * 更新機能のうちローダー依存の処理（通知・ファイルシステムパス・jar パス解決）を
 * ローダーごとに実装するためのインターフェースです。
 */
public interface UpdatePlatform {

    /**
     * このゲームインスタンスの設定ディレクトリのパスを返します。
     * 通常は {@code .minecraft/config} です。
     */
    Path getConfigDir();

    /**
     * mods ディレクトリのパスを返します。
     */
    Path getModsDir();

    /**
     * このModの jar ファイルのパスを返します。
     * 見つからない場合は {@link Optional#empty()} を返します。
     */
    Optional<Path> getCurrentJarPath();

    /**
     * このModの現在バージョン文字列を返します（例: "1.1.1"）。
     */
    String getCurrentVersion();

    /**
     * 更新通知をトースト（画面右上の通知）として表示します。
     * Minecraft のメインスレッドから呼ばれます。
     *
     * @param newVersion 新バージョン文字列
     */
    void showUpdateToast(String newVersion);

    /**
     * 自動ダウンロード・入れ替えの開始を通知するトーストを表示します。
     * Minecraft のメインスレッドから呼ばれます。
     */
    void showDownloadStartToast();

    /**
     * ダウンロード完了（次回起動時に入れ替え）を通知するトーストを表示します。
     * Minecraft のメインスレッドから呼ばれます。
     */
    void showDownloadCompleteToast();

    /**
     * ダウンロード失敗を通知するトーストを表示します。
     * Minecraft のメインスレッドから呼ばれます。
     */
    void showDownloadFailedToast();

    /**
     * 初回起動時に設定ポップアップ画面を表示する処理をスケジュールします。
     * Minecraft のメインスレッドから呼ばれます（タイトル画面描画後など）。
     *
     * @param config 設定オブジェクト（ポップアップ内で編集・保存する）
     */
    void scheduleFirstLaunchPopup(UpdaterConfig config);
}