package io.github.countyclaaw4.CarrotCraftSortingPatch.condition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class ServerCondition {
    public static final String TARGET_HOST = "play.carrotcraft.net";

    private ServerCondition() {}

    /**
     * 現在接続中のサーバーが play.carrotcraft.net かどうか判定します。
     * シングルプレイ、他サーバー、接続先取得不可の場合は false を返します。
     */
    public static boolean isTargetServer() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.isLocalServer() || client.hasSingleplayerServer()) {
                return false;
            }
            ServerData serverData = client.getCurrentServer();
            if (serverData == null || serverData.ip == null) {
                return false;
            }
            return isTargetHost(serverData.ip);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 与えられたアドレス文字列が対象ホスト名と完全一致するか判定します。
     */
    public static boolean isTargetHost(String address) {
        if (address == null) {
            return false;
        }
        String host = extractHost(address);
        return TARGET_HOST.equalsIgnoreCase(host);
    }

    /**
     * アドレス文字列からポート番号やIPv6ブラケットを取り除き、トリムされたホスト名を抽出します。
     */
    public static String extractHost(String address) {
        String trimmed = address.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        // IPv6形式: [host]:port
        if (trimmed.startsWith("[")) {
            int closingBracket = trimmed.indexOf(']');
            if (closingBracket != -1) {
                return trimmed.substring(1, closingBracket).trim();
            }
        }
        // 通常の host:port 形式
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex != -1) {
            return trimmed.substring(0, colonIndex).trim();
        }
        return trimmed;
    }
}
