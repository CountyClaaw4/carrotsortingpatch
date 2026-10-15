package io.github.countyclaaw4.carrotcraftsortingpatch.interceptor;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InterceptorRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("CarrotSortingPatch");
    private static final List<SortInterceptor> INTERCEPTORS = new ArrayList<>();

    private InterceptorRegistry() {}

    /**
     * ソートMod介入ハンドラを登録します。
     */
    public static synchronized void register(SortInterceptor interceptor) {
        if (interceptor != null) {
            INTERCEPTORS.add(interceptor);
            LOGGER.info("[CarrotSortingPatch] 介入ハンドラを登録しました: {}", interceptor.getModId());
        }
    }

    /**
     * 登録済みハンドラ一覧を取得します。
     */
    public static synchronized List<SortInterceptor> getInterceptors() {
        return Collections.unmodifiableList(new ArrayList<>(INTERCEPTORS));
    }

    /**
     * 指定されたModの保護ポリシーでコンテナ内スロットを判定します。
     */
    public static synchronized boolean shouldProtectSlot(String modId, int containerSlotIndex) {
        for (SortInterceptor interceptor : INTERCEPTORS) {
            if (!interceptor.getModId().equals(modId)) {
                continue;
            }

            try {
                return interceptor.shouldProtectSlot(containerSlotIndex);
            } catch (Throwable t) {
                LOGGER.error("[CarrotSortingPatch] スロット保護判定中にエラーが発生しました: " + modId, t);
                return false;
            }
        }
        return false;
    }

    /**
     * 保管庫GUI検出を各インターセプターへ通知します。
     */
    public static synchronized void notifyVaultGuiDetected(AbstractContainerMenu menu) {
        for (SortInterceptor interceptor : INTERCEPTORS) {
            try {
                if (interceptor.isAvailable()) {
                    interceptor.onVaultGuiDetected(menu);
                }
            } catch (Throwable t) {
                LOGGER.error("[CarrotSortingPatch] インターセプター処理中にエラーが発生しました: " + interceptor.getModId(), t);
            }
        }
    }
}
