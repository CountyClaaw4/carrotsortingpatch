package io.github.countyclaaw4.carrotcraftsortingpatch.interceptor;

import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.clientsort.ClientSortInterceptor;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.ipn.IpnInterceptor;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.nemos.NemosInterceptor;
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

        InterceptorRegistry.register(ipn);
        InterceptorRegistry.register(clientSort);
        InterceptorRegistry.register(nemos);

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
        assertTrue(InterceptorRegistry.shouldProtectSlot(nemos.getModId(), 45));
        assertFalse(InterceptorRegistry.shouldProtectSlot(nemos.getModId(), 54));
    }
}
