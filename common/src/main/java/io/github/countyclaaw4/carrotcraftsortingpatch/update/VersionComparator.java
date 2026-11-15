package io.github.countyclaaw4.carrotcraftsortingpatch.update;

/**
 * セマンティックバージョン（x.y.z）の大小を比較します。
 * <p>
 * 先頭の "v" は無視します。数値以外のサフィックス（-beta 等）は
 * 安全側に倒して「古い」と判定します。
 */
public final class VersionComparator {

    private VersionComparator() {}

    /**
     * {@code remote} が {@code current} より新しいか判定します。
     *
     * @param current 現在インストール済みのバージョン文字列
     * @param remote  リモートから取得した最新バージョン文字列
     * @return {@code remote > current} のとき {@code true}
     */
    public static boolean isNewer(String current, String remote) {
        int[] c = parse(current);
        int[] r = parse(remote);
        for (int i = 0; i < Math.max(c.length, r.length); i++) {
            int cv = i < c.length ? c[i] : 0;
            int rv = i < r.length ? r[i] : 0;
            if (rv > cv) return true;
            if (rv < cv) return false;
        }
        return false;
    }

    private static int[] parse(String version) {
        if (version == null || version.isBlank()) return new int[]{0};
        String v = version.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        // "-" 以降（-beta, -rc 等）を切り捨て
        int dash = v.indexOf('-');
        if (dash >= 0) v = v.substring(0, dash);
        String[] parts = v.split("\\.");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                nums[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                nums[i] = 0;
            }
        }
        return nums;
    }
}