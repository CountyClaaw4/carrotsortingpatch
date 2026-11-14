package io.github.countyclaaw4.carrotcraftsortingpatch.screen;

import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdateCoordinator;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdaterConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FirstLaunchScreen extends Screen {

    private static final Component TITLE =
            Component.translatable("screen.carrotsortingpatch.first_launch.title");
    private static final Component DESC_LINE1 =
            Component.translatable("screen.carrotsortingpatch.first_launch.desc1");
    private static final Component DESC_LINE2 =
            Component.translatable("screen.carrotsortingpatch.first_launch.desc2");
    private static final Component LABEL_CHECK =
            Component.translatable("screen.carrotsortingpatch.first_launch.check_for_updates");
    private static final Component LABEL_AUTO =
            Component.translatable("screen.carrotsortingpatch.first_launch.auto_update");
    private static final Component WARNING_TITLE =
            Component.translatable("screen.carrotsortingpatch.first_launch.auto_update_warning.title");
    private static final Component WARNING_DESCRIPTION =
            Component.translatable("screen.carrotsortingpatch.first_launch.auto_update_warning.description");

    private final Screen lastScreen;
    private final UpdaterConfig config;

    private Checkbox checkForUpdatesBox;
    private Checkbox autoUpdateBox;
    private UpdateScreenLayout.InitialLaunch layout;

    public FirstLaunchScreen(Screen lastScreen, UpdaterConfig config) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();
        layout = UpdateScreenLayout.initialLaunch(
                this.width, this.height, this.font, DESC_LINE1, DESC_LINE2, WARNING_DESCRIPTION);

        this.checkForUpdatesBox = Checkbox.builder(LABEL_CHECK, this.font)
                .pos(layout.contentX(), layout.checkY())
                .selected(config.isCheckForUpdates())
                .build();
        this.checkForUpdatesBox.setWidth(layout.contentWidth());
        this.addRenderableWidget(checkForUpdatesBox);

        this.autoUpdateBox = Checkbox.builder(LABEL_AUTO, this.font)
                .pos(layout.contentX(), layout.autoY())
                .selected(config.isAutoUpdate())
                .build();
        this.autoUpdateBox.setWidth(layout.contentWidth());
        this.addRenderableWidget(autoUpdateBox);

        UpdateScreenLayout.Bounds button = layout.button();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, btn -> saveAndClose())
                .bounds(button.x(), button.y(), button.width(), button.height())
                .build());
    }

    private void saveAndClose() {
        config.setCheckForUpdates(checkForUpdatesBox.selected());
        config.setAutoUpdate(autoUpdateBox.selected());
        config.save();

        UpdateCoordinator coordinator = UpdateCoordinator.getInstance();
        if (coordinator != null) {
            coordinator.onConfigSaved();
        }

        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(lastScreen);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
        renderPanel(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphicsExtractor graphics) {
        UpdateScreenLayout.Bounds panel = layout.panel();
        graphics.fill(panel.x(), panel.y(), panel.x() + panel.width(), panel.y() + panel.height(), 0xC0000000);
        graphics.outline(panel.x(), panel.y(), panel.width(), panel.height(), 0xFFFFAA00);
        graphics.centeredText(this.font, this.title, this.width / 2, layout.titleY(), 0xFFFFAA00);

        for (int i = 0; i < layout.description1().size(); i++) {
            graphics.text(this.font, layout.description1().get(i), layout.contentX(),
                    layout.descriptionY() + i * this.font.lineHeight, 0xFFFFFFFF);
        }
        int description2Y = layout.descriptionY() + layout.description1().size() * this.font.lineHeight + 4;
        for (int i = 0; i < layout.description2().size(); i++) {
            graphics.text(this.font, layout.description2().get(i), layout.contentX(),
                    description2Y + i * this.font.lineHeight, 0xFFCCCCCC);
        }

        UpdateScreenLayout.Warning warning = layout.warning();
        UpdateScreenLayout.Bounds warningPanel = warning.bounds();
        if (warningPanel.height() >= 12 + this.font.lineHeight) {
            graphics.fill(warningPanel.x(), warningPanel.y(), warningPanel.x() + warningPanel.width(),
                    warningPanel.y() + warningPanel.height(), 0xCC351F1F);
            graphics.fill(warningPanel.x(), warningPanel.y(), warningPanel.x() + 2,
                    warningPanel.y() + warningPanel.height(), 0xFFFF5555);
            graphics.text(this.font, WARNING_TITLE, warningPanel.x() + 10, warning.titleY(), 0xFFFF7777);
        }
        for (int i = 0; i < warning.lines().size(); i++) {
            graphics.text(this.font, warning.lines().get(i), warningPanel.x() + 10,
                    warning.descriptionY() + i * warning.lineHeight(), 0xFFE0A0A0);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}