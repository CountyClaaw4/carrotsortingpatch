package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NeoForgeVersionCheckerBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("CarrotSortingPatch/VersionCheck");
    private static final String MOD_ID = "carrotsortingpatch";
    private static final String RELEASES_URL =
            "https://github.com/carrot-craft/carrotsortingpatch/releases";

    private NeoForgeVersionCheckerBridge() {}

    /** 新バージョン検出時に呼ぶ */
    public static void publishOutdated(String latestVersion, String changelogBody) {
        try {
            Class<?> vcClass = findVersionCheckerClass();
            if (vcClass == null) {
                LOGGER.warn("VersionChecker class not found");
                return;
            }

            // 26.2 ソースでは private static Map<IModInfo, CheckResult> results
            Field resultsField = findResultsField(vcClass);
            if (resultsField == null) {
                LOGGER.warn("VersionChecker results field not found on {}", vcClass.getName());
                return;
            }
            resultsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<Object, Object> results = (Map<Object, Object>) resultsField.get(null);
            if (results == null) {
                // 念のため（通常は ConcurrentHashMap で初期化済み）
                results = new ConcurrentHashMap<>();
                resultsField.set(null, results);
            }

            Object modInfo = findOwnModInfo();
            if (modInfo == null) {
                LOGGER.warn("IModInfo for {} not found", MOD_ID);
                return;
            }

            String ver = latestVersion.replaceFirst("^[vV]", "");
                ComparableVersion target = new ComparableVersion(ver);

                Map<ComparableVersion, String> changes = Map.of();
                if (changelogBody != null && !changelogBody.isBlank()) {
                    changes = Map.of(target, changelogBody.trim());
                }

                Object checkResult = createCheckResult(vcClass, "OUTDATED", target, changes, RELEASES_URL);

            results.put(modInfo, checkResult);
            LOGGER.info("Published OUTDATED to VersionChecker.results (latest={})", ver);
        } catch (Exception e) {
            LOGGER.error("Failed to publish version check result", e);
        }
    }

    private static Object findOwnModInfo() throws Exception {
        Class<?> modListClass = Class.forName("net.neoforged.fml.ModList");
        Object modList = modListClass.getMethod("get").invoke(null);
        Object optional = modListClass
                .getMethod("getModContainerById", String.class)
                .invoke(modList, MOD_ID);
        if (!(boolean) optional.getClass().getMethod("isPresent").invoke(optional)) {
            return null;
        }
        Object container = optional.getClass().getMethod("get").invoke(optional);
        return container.getClass().getMethod("getModInfo").invoke(container);
    }

    private static Field findResultsField(Class<?> vcClass) {
        for (String name : new String[]{"RESULTS", "results"}) {
            try {
                return vcClass.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static Object createCheckResult(
            Class<?> vcClass,
            String statusName,
            ComparableVersion target,
            Map<?, ?> changes,
            String url
    ) throws Exception {
        Class<?> statusClass = Class.forName(vcClass.getName() + "$Status");
        Class<?> checkResultClass = Class.forName(vcClass.getName() + "$CheckResult");

        @SuppressWarnings({"unchecked", "rawtypes"})
        Object status = Enum.valueOf((Class<Enum>) statusClass, statusName);

        // record CheckResult(Status status, ComparableVersion target, Map changes, String url)
        Constructor<?> ctor = checkResultClass.getDeclaredConstructor(
                statusClass,
                ComparableVersion.class,
                Map.class,
                String.class
        );
        ctor.setAccessible(true);
        return ctor.newInstance(status, target, changes, url);
    }

    private static Class<?> findVersionCheckerClass() {
        try {
            return Class.forName("net.neoforged.neoforge.common.VersionChecker"); // 26.3+
        } catch (ClassNotFoundException ignored) {
        }
        try {
            return Class.forName("net.neoforged.fml.VersionChecker"); // 26.1 / 26.2
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}