package io.github.countyclaaw4.CarrotCraftSortingPatch.condition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VaultGuiDetectorTest {

    @Test
    @DisplayName("装飾コード除去テスト")
    void testStripFormatting() {
        assertEquals("使い方", VaultGuiDetector.stripFormatting("§e使い方"));
        assertEquals("使い方", VaultGuiDetector.stripFormatting("§e§l使い方§r"));
        assertEquals("プラン", VaultGuiDetector.stripFormatting("§b§nプラン§r"));
        assertEquals("test", VaultGuiDetector.stripFormatting("test"));
        assertEquals("", VaultGuiDetector.stripFormatting(""));
        assertEquals("", VaultGuiDetector.stripFormatting(null));
    }

    @Test
    @DisplayName("保護対象スロット判定テスト (コンテナ内 45〜53)")
    void testIsProtectedContainerSlot() {
        // 0〜44 は通常スロット (保護対象外)
        for (int i = 0; i < 45; i++) {
            assertFalse(VaultGuiDetector.isProtectedContainerSlot(i), "Slot " + i + " should not be protected");
        }

        // 45〜53 は最下段9スロット (保護対象)
        for (int i = 45; i <= 53; i++) {
            assertTrue(VaultGuiDetector.isProtectedContainerSlot(i), "Slot " + i + " should be protected");
        }

        // 54以上は範囲外
        assertFalse(VaultGuiDetector.isProtectedContainerSlot(54));
        assertFalse(VaultGuiDetector.isProtectedContainerSlot(-1));
    }
}
