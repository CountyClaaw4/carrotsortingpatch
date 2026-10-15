package io.github.countyclaaw4.carrotcraftsortingpatch.condition;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class VaultGuiDetector {
    public static final int REQUIRED_CONTAINER_SLOTS = 54;
    public static final int SLOT_INDEX_PLAN = 45;
    public static final int SLOT_INDEX_HOWTO = 53;

    public static final int PROTECTED_SLOT_MIN = 45;
    public static final int PROTECTED_SLOT_MAX = 53;

    public static final String NAME_PLAN = "プラン";
    public static final String NAME_HOWTO = "使い方";

    private static final Pattern FORMATTING_PATTERN = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

    private VaultGuiDetector() {}

    /**
     * 指定されたコンテナメニューが play.carrotcraft.net の保管庫GUIであるか判定します。
     * 条件A: コンテナ領域が54スロットであること。
     * 条件B: index 53 が book、「使い方」、carrotgui:item = 1b であること。
     * 条件C: index 45 が clock、「プラン」、carrotgui:item = 1b であること。
     * 判定中に例外が発生した場合は安全に false を返します。
     */
    public static boolean isVaultGui(AbstractContainerMenu menu) {
        if (menu == null) {
            return false;
        }

        try {
            // 条件A: コンテナ領域が54スロットであること
            if (!checkContainerSlotCount(menu)) {
                return false;
            }

            // 条件C: 左下スロット（index 45）が clock / 「プラン」 / carrotgui:item = 1b
            Slot slot45 = menu.getSlot(SLOT_INDEX_PLAN);
            if (!matchesSlotItem(slot45, Items.CLOCK, NAME_PLAN)) {
                return false;
            }

            // 条件B: 最終スロット（index 53）が book / 「使い方」 / carrotgui:item = 1b
            Slot slot53 = menu.getSlot(SLOT_INDEX_HOWTO);
            if (!matchesSlotItem(slot53, Items.BOOK, NAME_HOWTO)) {
                return false;
            }

            return true;
        } catch (Throwable t) {
            // 失敗時は何もしない（安全性のルール1）
            return false;
        }
    }

    /**
     * プレイヤーインベントリを除くコンテナ領域のスロット数がちょうど54スロットか確認します。
     */
    public static boolean checkContainerSlotCount(AbstractContainerMenu menu) {
        if (menu.slots.size() < REQUIRED_CONTAINER_SLOTS) {
            return false;
        }

        int containerSlots = 0;
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory) {
                // プレイヤーインベントリに達したらコンテナ領域終了
                break;
            }
            containerSlots++;
        }

        return containerSlots == REQUIRED_CONTAINER_SLOTS;
    }

    /**
     * スロットのアイテムが指定アイテムID、指定名、carrotgui:item=1bを満たすか判定します。
     */
    public static boolean matchesSlotItem(Slot slot, Item expectedItem, String expectedName) {
        if (slot == null) {
            return false;
        }
        ItemStack stack = slot.getItem();
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // 1. アイテムID完全一致
        if (stack.getItem() != expectedItem) {
            return false;
        }

        // 2. カスタム名判定（装飾コードを除去してトリミング比較）
        Component customNameComp = stack.get(DataComponents.CUSTOM_NAME);
        if (customNameComp == null) {
            customNameComp = stack.getHoverName();
        }
        if (customNameComp == null) {
            return false;
        }
        String cleanName = stripFormatting(customNameComp.getString()).trim();
        if (!expectedName.equals(cleanName)) {
            return false;
        }

        // 3. carrotgui:item = 1b 判定
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag tag = customData.copyTag();
        CompoundTag publicBukkitValues = tag.getCompoundOrEmpty("PublicBukkitValues");
        if (publicBukkitValues.isEmpty() || !publicBukkitValues.contains("carrotgui:item")) {
            return false;
        }

        byte val = publicBukkitValues.getByteOr("carrotgui:item", (byte) 0);
        return val == (byte) 1;
    }

    /**
     * コンテナ内のスロットインデックス（0始まり）が保護対象（45〜53）であるか判定します。
     */
    public static boolean isProtectedContainerSlot(int containerSlotIndex) {
        return containerSlotIndex >= PROTECTED_SLOT_MIN && containerSlotIndex <= PROTECTED_SLOT_MAX;
    }

    /**
     * メニュー全体のスロットIDから、コンテナ領域内インデックス（0始まり）を計算します。
     * プレイヤーインベントリのスロットや無効なスロットの場合は -1 を返します。
     */
    public static int getContainerSlotIndex(AbstractContainerMenu menu, int slotId) {
        if (menu == null || slotId < 0 || slotId >= menu.slots.size()) {
            return -1;
        }

        int containerIndex = 0;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container instanceof Inventory) {
                // プレイヤーインベントリ以降は対象外
                continue;
            }
            if (i == slotId || slot.index == slotId) {
                return containerIndex;
            }
            containerIndex++;
        }

        return -1;
    }

    /**
     * 複数スロットのコンテナ内インデックスを、メニューを一度走査して取得します。
     * メニューに含まれないスロットやプレイヤーインベントリのスロットは -1 になります。
     */
    public static int[] getContainerSlotIndices(AbstractContainerMenu menu, Slot[] slots) {
        if (slots == null) {
            return new int[0];
        }

        int[] indices = new int[slots.length];
        Arrays.fill(indices, -1);
        if (menu == null || slots.length == 0) {
            return indices;
        }

        Map<Slot, Integer> indexBySlot = new IdentityHashMap<>();
        int containerIndex = 0;
        for (Slot slot : menu.slots) {
            if (!(slot.container instanceof Inventory)) {
                indexBySlot.put(slot, containerIndex++);
            }
        }

        for (int i = 0; i < slots.length; i++) {
            Integer index = indexBySlot.get(slots[i]);
            indices[i] = index == null ? -1 : index;
        }
        return indices;
    }

    /**
     * 書式・装飾コード（§a, §l など）を除去します。
     */
    public static String stripFormatting(String input) {
        if (input == null) {
            return "";
        }
        return FORMATTING_PATTERN.matcher(input).replaceAll("");
    }
}
