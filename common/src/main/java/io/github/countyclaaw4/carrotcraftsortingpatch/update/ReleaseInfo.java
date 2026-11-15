package io.github.countyclaaw4.carrotcraftsortingpatch.update;

/**
 * GitHub Releases の最新リリース情報を保持します。
 */
public final class ReleaseInfo {

    private final String tagName;
    private final String downloadUrl;
    private final String fileName;
    private final String checksumUrl;
    private final String body;

    public ReleaseInfo(String tagName, String downloadUrl, String fileName) {
        this(tagName, downloadUrl, fileName, null, null);
    }

    public ReleaseInfo(String tagName, String downloadUrl, String fileName, String checksumUrl) {
        this(tagName, downloadUrl, fileName, checksumUrl, null);
    }

    public ReleaseInfo(String tagName, String downloadUrl, String fileName,
                       String checksumUrl, String body) {
        this.tagName     = tagName;
        this.downloadUrl = downloadUrl;
        this.fileName    = fileName;
        this.checksumUrl = checksumUrl;
        this.body        = body;
    }

    public String getTagName()     { return tagName; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getFileName()    { return fileName; }
    public String getChecksumUrl() { return checksumUrl; }

    /** リリース本文（Markdown）。無い場合は null */
    public String getBody() { return body; }
}