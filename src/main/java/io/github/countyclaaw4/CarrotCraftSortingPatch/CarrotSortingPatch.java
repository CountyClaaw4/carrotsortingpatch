package io.github.countyclaaw4.carrotcraftsortingpatch;

import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.InterceptorRegistry;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.clientsort.ClientSortInterceptor;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.ipn.IpnInterceptor;
import io.github.countyclaaw4.carrotcraftsortingpatch.interceptor.nemos.NemosInterceptor;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarrotSortingPatch implements ClientModInitializer {
    public static final String MOD_ID = "carrotsortingpatch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[CarrotSortingPatch] 初期化開始 (対象サーバー: play.carrotcraft.net)");

        // 各ソートMod向けインターセプターの登録
        InterceptorRegistry.register(new IpnInterceptor());
        InterceptorRegistry.register(new ClientSortInterceptor());
        InterceptorRegistry.register(new NemosInterceptor());

        LOGGER.info("[CarrotSortingPatch] 初期化完了");
    }
}
