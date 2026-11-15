package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public final class ForgeVersionCheckerBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("CarrotSortingPatch/VersionCheck");
    private static final String MOD_ID = "carrotsortingpatch";
    private static final String RELEASES_URL =
            "https://github.com/carrot-craft/carrotsortingpatch/releases";

    private ForgeVersionCheckerBridge() {}

    public static void publishOutdated(String latestVersion, String changelogBody) {
        try {
            Class<?> vcClass = Class.forName("net.minecraftforge.fml.VersionChecker");

            Field resultsField = findResultsField(vcClass);
            if (resultsField == null) {
                LOGGER.warn("VersionChecker results field not found");
                return;
            }
            resultsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<Object, Object> results = (Map<Object, Object>) resultsField.get(null);
            if (results == null) {
                LOGGER.warn("VersionChecker results map is null");
                return;
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
            if (checkResult == null) {
                LOGGER.warn("Failed to create CheckResult");
                return;
            }

            results.put(modInfo, checkResult);
            LOGGER.info("Published OUTDATED to Forge VersionChecker (latest={})", ver);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Forge VersionChecker not found");
        } catch (Exception e) {
            LOGGER.error("Failed to publish version check result", e);
        }
    }

    private static Field findResultsField(Class<?> vcClass) {
        for (String name : new String[]{"results", "RESULTS"}) {
            try {
                return vcClass.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        for (Field f : vcClass.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Map.class.isAssignableFrom(f.getType())) {
                return f;
            }
        }
        return null;
    }

    private static Object findOwnModInfo() throws Exception {
        Class<?> modListClass = Class.forName("net.minecraftforge.fml.ModList");
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

        Constructor<?> ctor = checkResultClass.getDeclaredConstructor(
                statusClass,
                ComparableVersion.class,
                Map.class,
                String.class
        );
        ctor.setAccessible(true);
        return ctor.newInstance(status, target, changes, url);
    }
}