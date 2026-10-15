package io.github.countyclaaw4.carrotcraftsortingpatch.condition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConditionTest {

    @Test
    @DisplayName("ホスト名抽出テスト")
    void testExtractHost() {
        assertEquals("play.carrotcraft.net", ServerCondition.extractHost("play.carrotcraft.net"));
        assertEquals("play.carrotcraft.net", ServerCondition.extractHost("  play.carrotcraft.net  "));
        assertEquals("play.carrotcraft.net", ServerCondition.extractHost("play.carrotcraft.net:25565"));
        assertEquals("play.carrotcraft.net", ServerCondition.extractHost("  play.carrotcraft.net:19132  "));
        assertEquals("play.carrotcraft.net", ServerCondition.extractHost("[play.carrotcraft.net]:25565"));
        assertEquals("", ServerCondition.extractHost(""));
        assertEquals("", ServerCondition.extractHost("   "));
    }

    @Test
    @DisplayName("サーバー判定仕様テスト (play.carrotcraft.net のみ完全一致)")
    void testIsTargetHost() {
        // 許可ホスト名（大文字小文字区別なし・空白・ポート対応）
        assertTrue(ServerCondition.isTargetHost("play.carrotcraft.net"));
        assertTrue(ServerCondition.isTargetHost("PLAY.CARROTCRAFT.NET"));
        assertTrue(ServerCondition.isTargetHost("Play.Carrotcraft.Net"));
        assertTrue(ServerCondition.isTargetHost("  play.carrotcraft.net  "));
        assertTrue(ServerCondition.isTargetHost("play.carrotcraft.net:25565"));
        assertTrue(ServerCondition.isTargetHost("  PLAY.CARROTCRAFT.NET:25565  "));

        // 対象外（部分一致や他ドメインは不許可）
        assertFalse(ServerCondition.isTargetHost("carrotcraft.net"));
        assertFalse(ServerCondition.isTargetHost("sub.play.carrotcraft.net"));
        assertFalse(ServerCondition.isTargetHost("play.carrotcraft.net.example.com"));
        assertFalse(ServerCondition.isTargetHost("other.server.net"));
        assertFalse(ServerCondition.isTargetHost(null));
        assertFalse(ServerCondition.isTargetHost(""));
    }
}
