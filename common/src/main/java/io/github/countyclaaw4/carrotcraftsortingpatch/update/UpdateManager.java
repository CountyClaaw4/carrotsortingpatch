package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import io.github.countyclaaw4.carrotcraftsortingpatch.CarrotSortingPatch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 更新チェック・ダウンロード・終了時 jar 入れ替えを管理します。
 *
 * <h3>入れ替えの仕組み</h3>
 * <ol>
 *   <li>ダウンロードした新 jar を一時ファイルとして mods フォルダに保存。</li>
 *   <li>JVM の {@link Runtime#addShutdownHook(Thread)} でシャットダウンフック登録。</li>
 *   <li>フック内で旧 jar を削除し、一時ファイルをリネーム（{@link StandardCopyOption#ATOMIC_MOVE}）。</li>
 * </ol>
 *
 * シャットダウンフックはクラスローダーが解放された後でも動くため、
 * nio の基本 API のみを使い Minecraft のクラスには依存しません。
 */
public final class UpdateManager {

    // ---- 定数 ----
    private static final String GITHUB_API_LATEST =
            "https://api.github.com/repos/carrot-craft/carrotsortingpatch/releases/latest";
    private static final String GITHUB_ACCEPT = "application/vnd.github+json";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final Pattern CURRENT_ARTIFACT = Pattern.compile(
            "^(carrotsortingpatch-(?:fabric|forge|neoforge)-)[^+]+(\\+mc.+\\.jar)$");
    private static final Pattern CHECKSUM_LINE = Pattern.compile("^([0-9a-fA-F]{64})\\s+\\*?(.+?)\\s*$");

    /** シャットダウン時に実行する「削除すべき旧 jar」「保存先パス（=一時ファイル）のペア」 */
    private record SwapEntry(Path oldJar, Path tempNew) {}

    private static final List<SwapEntry> pendingSwaps = new ArrayList<>();
    private static boolean hookRegistered = false;

    private UpdateManager() {}

    // ---- 公開 API ----

    /**
     * 最新リリース情報を非同期で取得します。
     *
     * @param currentVersion 現在のバージョン文字列
     * @param onResult       新バージョンが存在すれば {@link ReleaseInfo} を渡すコールバック（Minecraft スレッドから呼ばれる想定ではありません）
     */
    public static CompletableFuture<Optional<ReleaseInfo>> checkAsync(String currentVersion) {
        return checkAsync(currentVersion, null);
    }

    public static CompletableFuture<Optional<ReleaseInfo>> checkAsync(String currentVersion, String currentJarName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchLatestRelease(currentVersion, currentJarName);
            } catch (Exception e) {
                CarrotSortingPatch.LOGGER.warn("[CarrotSortingPatch] 更新チェック中にエラーが発生しました", e);
                return Optional.empty();
            }
        });
    }

    /**
     * 指定された {@link ReleaseInfo} の jar を mods フォルダへダウンロードし、
     * シャットダウン時に旧 jar と入れ替えるフックを登録します。
     *
     * @param info           ダウンロードするリリース情報
     * @param currentJarPath 現在実行中の（置き換え対象の）jar パス
     * @param modsDir        mods ディレクトリのパス
     * @param onProgress     ダウンロード進行率（0.0〜1.0）のコールバック
     * @return ダウンロード完了後 {@code true}、失敗時 {@code false}
     */
    public static CompletableFuture<Boolean> downloadAndScheduleSwap(
            ReleaseInfo info,
            Path currentJarPath,
            Path modsDir,
            Consumer<Double> onProgress) {

        return CompletableFuture.supplyAsync(() -> {
            Path tempPath = modsDir.resolve(currentJarPath.getFileName() + ".new_" + info.getTagName());
            try {
                String checksumFile = downloadText(info.getChecksumUrl());
                String expectedChecksum = findChecksum(checksumFile, info.getFileName())
                        .orElseThrow(() -> new IOException("チェックサム一覧に対象JARがありません: " + info.getFileName()));

                downloadFile(info.getDownloadUrl(), tempPath, onProgress);
                String actualChecksum = calculateSha256(tempPath);
                if (!MessageDigest.isEqual(
                        HexFormat.of().parseHex(expectedChecksum),
                        HexFormat.of().parseHex(actualChecksum))) {
                    throw new IOException("SHA-256が一致しません: " + info.getFileName());
                }

                scheduleSwap(currentJarPath, tempPath);
                CarrotSortingPatch.LOGGER.info(
                        "[CarrotSortingPatch] SHA-256を確認しました。ゲーム終了時に jar を入れ替えます: {} -> {}",
                        currentJarPath.getFileName(), info.getFileName());
                return true;
            } catch (Exception e) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException cleanupError) {
                    e.addSuppressed(cleanupError);
                }
                CarrotSortingPatch.LOGGER.error("[CarrotSortingPatch] ダウンロードに失敗しました", e);
                return false;
            }
        });
    }

    // ---- 内部実装 ----

    /**
     * GitHub API から最新リリースを取得し、現在より新しければ {@link ReleaseInfo} を返します。
     */
    private static Optional<ReleaseInfo> fetchLatestRelease(String currentVersion, String currentJarName)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API_LATEST))
                .header("Accept", GITHUB_ACCEPT)
                .header("User-Agent", "carrotsortingpatch/" + currentVersion)
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            CarrotSortingPatch.LOGGER.warn("[CarrotSortingPatch] GitHub API がステータス {} を返しました", response.statusCode());
            return Optional.empty();
        }

        String body = response.body();
        String tagName = extractJsonString(body, "tag_name");
        if (tagName == null || tagName.isBlank()) {
            return Optional.empty();
        }
        String releaseBody = extractReleaseBody(body);

        if (!VersionComparator.isNewer(currentVersion, tagName)) {
            CarrotSortingPatch.LOGGER.info("[CarrotSortingPatch] 最新バージョンを使用中です: {}", currentVersion);
            return Optional.empty();
        }

        // assets 配列から現在のloader/MC構成に合うJARとチェックサム一覧を取得
        String[] assetBlock = extractAssetsBlock(body);
        Matcher currentArtifact = currentJarName == null
                ? null
                : CURRENT_ARTIFACT.matcher(currentJarName);
        boolean hasArtifactVariant = currentArtifact != null && currentArtifact.matches();
        String artifactPrefix = hasArtifactVariant ? currentArtifact.group(1) : null;
        String artifactVariant = hasArtifactVariant ? currentArtifact.group(2) : null;
        String downloadUrl = null;
        String fileName = null;
        String checksumUrl = null;
        for (String asset : assetBlock) {
            String name = extractJsonString(asset, "name");
            String url  = extractJsonString(asset, "browser_download_url");
            if ("SHA256SUMS.txt".equals(name)) {
                checksumUrl = url;
                continue;
            }
            if (hasArtifactVariant && (name == null
                    || !name.startsWith(artifactPrefix)
                    || !name.endsWith(artifactVariant))) {
                continue;
            }
            if (name != null && name.endsWith(".jar") && url != null) {
                downloadUrl = url;
                fileName = name;
            }
        }

        if (downloadUrl != null && fileName != null && checksumUrl != null) {
            CarrotSortingPatch.LOGGER.info(
                    "[CarrotSortingPatch] 新バージョンを検出: {} (現在: {})", tagName, currentVersion);
            return Optional.of(new ReleaseInfo(tagName, downloadUrl, fileName, checksumUrl, releaseBody));
        }

        CarrotSortingPatch.LOGGER.warn(
                "[CarrotSortingPatch] リリース {} に対応JARまたはSHA256SUMS.txtがありません", tagName);
        return Optional.empty();
    }

    static Optional<String> findChecksum(String checksumFile, String fileName) {
        String found = null;
        for (String line : checksumFile.split("\\R")) {
            Matcher matcher = CHECKSUM_LINE.matcher(line);
            if (!matcher.matches() || !fileName.equals(matcher.group(2))) {
                continue;
            }
            if (found != null) {
                return Optional.empty();
            }
            found = matcher.group(1).toLowerCase(java.util.Locale.ROOT);
        }
        return Optional.ofNullable(found);
    }

    static String calculateSha256(Path file) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256が利用できません", e);
        }

        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /** 極小 JSON パーサー: 文字列フィールドを取得します（依存なし）。 */
    private static String extractJsonString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /** "assets" 配列のオブジェクトを文字列配列として返します。 */
    private static String[] extractAssetsBlock(String json) {
        // "assets":[{...},{...}] を単純に { } の対応で分割
        int start = json.indexOf("\"assets\"");
        if (start < 0) return new String[0];
        int arrayStart = json.indexOf('[', start);
        if (arrayStart < 0) return new String[0];

        List<String> results = new ArrayList<>();
        int depth = 0;
        int objStart = -1;
        for (int i = arrayStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    results.add(json.substring(objStart, i + 1));
                    objStart = -1;
                }
            } else if (c == ']' && depth == 0) {
                break;
            }
        }
        return results.toArray(new String[0]);
    }

    /**
     * HTTP(S) でファイルをダウンロードします。
     */
    private static void downloadFile(String url, Path dest, Consumer<Double> onProgress)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(TIMEOUT)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "carrotsortingpatch-updater")
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();

        HttpResponse<InputStream> response =
                client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("ダウンロード失敗: HTTP " + response.statusCode());
        }

        long total = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        Files.createDirectories(dest.getParent());

        try (InputStream raw = response.body();
             BufferedInputStream in = new BufferedInputStream(raw)) {

            byte[] buf = new byte[8192];
            long downloaded = 0L;
            try (var out = Files.newOutputStream(dest)) {
                int read;
                while ((read = in.read(buf)) >= 0) {
                    out.write(buf, 0, read);
                    downloaded += read;
                    if (total > 0 && onProgress != null) {
                        onProgress.accept((double) downloaded / total);
                    }
                }
            }
        }

        if (onProgress != null) onProgress.accept(1.0);
    }

    private static String downloadText(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(TIMEOUT)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "carrotsortingpatch-updater")
                .timeout(TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("チェックサムのダウンロード失敗: HTTP " + response.statusCode());
        }
        return response.body();
    }

    /**
     * シャットダウン時に oldJar を削除して tempNew をリネームするフックを登録します。
     */
    private static synchronized void scheduleSwap(Path oldJar, Path tempNew) {
        pendingSwaps.add(new SwapEntry(oldJar, tempNew));
        if (!hookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(UpdateManager::runSwaps, "carrotsortingpatch-swap"));
            hookRegistered = true;
        }
    }

    /** シャットダウンフック本体: jar の削除とリネームを実行します。 */
    private static void runSwaps() {
        for (SwapEntry entry : pendingSwaps) {
            try {
                Files.deleteIfExists(entry.oldJar());
                Files.move(entry.tempNew(), entry.oldJar().resolveSibling(
                        extractNewFileName(entry.tempNew().getFileName().toString(), entry.oldJar())),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
                System.out.println("[CarrotSortingPatch] jar を入れ替えました: " + entry.oldJar().getFileName());
            } catch (IOException e) {
                System.err.println("[CarrotSortingPatch] jar の入れ替えに失敗しました: " + e.getMessage());
                // 一時ファイルはそのまま残す（次回起動時に手動で対処できるよう）
            }
        }
    }

    /**
     * 一時ファイル名から新 jar のファイル名を決定します。
     * 一時ファイル名: "carrotsortingpatch-xxx.jar.new_1.2.3"
     * 旧 jar のファイル名をそのまま再利用（バージョン番号を維持しない）ことで
     * mods フォルダが汚れないようにします。
     */
    private static Path extractNewFileName(String tempName, Path oldJar) {
        // 旧 jar と同名で上書き（REPLACE_EXISTING を利用）
        return oldJar.getFileName();
    }

    /** トップレベルの "body" 文字列を正規表現なしで取り出す（長いリリースノート対策） */
    private static String extractReleaseBody(String json) {
        // "body": null
        int key = indexOfJsonKey(json, "body");
        if (key < 0) {
            return null;
        }
        int i = key;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }
        if (i + 4 <= json.length() && json.startsWith("null", i)) {
            return null;
        }
        if (i >= json.length() || json.charAt(i) != '"') {
            return null;
        }
        i++; // opening quote
        StringBuilder out = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '"') {
                break; // closing quote
            }
            if (c == '\\' && i < json.length()) {
                char n = json.charAt(i++);
                switch (n) {
                    case 'n'  -> out.append('\n');
                    case 'r'  -> out.append('\r');
                    case 't'  -> out.append('\t');
                    case '"'  -> out.append('"');
                    case '\\' -> out.append('\\');
                    case '/'  -> out.append('/');
                    case 'u'  -> {
                        if (i + 4 <= json.length()) {
                            out.append((char) Integer.parseInt(json.substring(i, i + 4), 16));
                            i += 4;
                        }
                    }
                    default -> out.append(n);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** "key"\s*:\s* の直後のインデックス。無ければ -1 */
    private static int indexOfJsonKey(String json, String key) {
        String needle = "\"" + key + "\"";
        int from = 0;
        while (from < json.length()) {
            int at = json.indexOf(needle, from);
            if (at < 0) {
                return -1;
            }
            int i = at + needle.length();
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                i++;
            }
            if (i < json.length() && json.charAt(i) == ':') {
                i++;
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }
                return i;
            }
            from = at + 1;
        }
        return -1;
    }
}