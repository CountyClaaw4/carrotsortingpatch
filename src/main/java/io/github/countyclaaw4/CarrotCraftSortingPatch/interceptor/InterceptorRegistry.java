package io.github.countyclaaw4.CarrotCraftSortingPatch.interceptor;

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
