package io.github.countyclaaw4.CarrotCraftSortingPatch.interceptor;

import io.github.countyclaaw4.CarrotCraftSortingPatch.interceptor.clientsort.ClientSortInterceptor;
import io.github.countyclaaw4.CarrotCraftSortingPatch.interceptor.ipn.IpnInterceptor;
import io.github.countyclaaw4.CarrotCraftSortingPatch.interceptor.nemos.NemosInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterceptorRegistryTest {

    @Test
    @DisplayName("インターセプター登録および保護スロット範囲テスト")
    void testInterceptors() {
        IpnInterceptor ipn = new IpnInterceptor();
        assertEquals("inventoryprofilesnext", ipn.getModId());

        ClientSortInterceptor clientSort = new ClientSortInterceptor();
        assertEquals("clientsort", clientSort.getModId());

        NemosInterceptor nemos = new NemosInterceptor();
        assertEquals("nemos_inventory_sorting", nemos.getModId());

        // 保護対象範囲: 45〜53
        for (int i = 0; i < 45; i++) {
            assertFalse(ipn.shouldProtectSlot(i));
            assertFalse(clientSort.shouldProtectSlot(i));
        }
        for (int i = 45; i <= 53; i++) {
            assertTrue(ipn.shouldProtectSlot(i));
            assertTrue(clientSort.shouldProtectSlot(i));
            assertTrue(nemos.shouldProtectSlot(i));
        }
        assertFalse(ipn.shouldProtectSlot(54));
        assertFalse(clientSort.shouldProtectSlot(54));
        assertFalse(nemos.shouldProtectSlot(54));
    }
}
