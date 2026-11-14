package io.github.countyclaaw4.carrotcraftsortingpatch.update;

/**
 * GitHub Releases の最新リリース情報を保持します。
 */
public final class ReleaseInfo {

    private final String tagName;
    private final String downloadUrl;
    private final String fileName;
    private final String checksumUrl;

    public ReleaseInfo(String tagName, String downloadUrl, String fileName) {
        this(tagName, downloadUrl, fileName, null);
    }

    public ReleaseInfo(String tagName, String downloadUrl, String fileName, String checksumUrl) {
        this.tagName     = tagName;
        this.downloadUrl = downloadUrl;
        this.fileName    = fileName;
        this.checksumUrl = checksumUrl;
    }

    /** タグ名（例: "1.1.2"） */
    public String getTagName() { return tagName; }

    /** jar の直接ダウンロード URL */
    public String getDownloadUrl() { return downloadUrl; }

    /** jar のファイル名 */
    public String getFileName() { return fileName; }

    /** チェックサム一覧の直接ダウンロード URL */
    public String getChecksumUrl() { return checksumUrl; }
}