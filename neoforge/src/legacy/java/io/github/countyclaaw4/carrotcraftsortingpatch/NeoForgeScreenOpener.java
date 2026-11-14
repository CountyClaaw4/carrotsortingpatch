package io.github.countyclaaw4.carrotcraftsortingpatch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class NeoForgeScreenOpener {

    private NeoForgeScreenOpener() {}

    public static void open(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }
}