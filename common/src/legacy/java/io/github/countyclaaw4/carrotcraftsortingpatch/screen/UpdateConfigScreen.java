
package io.github.countyclaaw4.carrotcraftsortingpatch.screen;

import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdaterConfig;
import io.github.countyclaaw4.carrotcraftsortingpatch.update.UpdateCoordinator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class UpdateConfigScreen extends Screen {

    private static final Component TITLE =
            Component.translatable("screen.carrotsortingpatch.first_launch.title");

    private static final Component LABEL_CHECK =
            Component.translatable("screen.carrotsortingpatch.first_launch.check_for_updates");

    private static final Component LABEL_AUTO =
            Component.translatable("screen.carrotsortingpatch.first_launch.auto_update");

    private static final Component WARNING_TITLE =
            Component.translatable("screen.carrotsortingpatch.first_launch.auto_update_warning.title");

    private static final Component WARNING_DESCRIPTION =
            Component.translatable("screen.carrotsortingpatch.first_launch.auto_update_warning.description");

    private static final int WARNING_PADDING = 10;

    private final Screen lastScreen;
    private final UpdaterConfig config;

    private Checkbox checkForUpdatesBox;
    private Checkbox autoUpdateBox;
    private Button downloadUpdateButton;
        private UpdateScreenLayout.Settings layout;

    private boolean downloadStarted;

    public UpdateConfigScreen(Screen lastScreen, UpdaterConfig config) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        layout = UpdateScreenLayout.settings(this.width, this.height, this.font, WARNING_DESCRIPTION);

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

        this.downloadUpdateButton = Button.builder(
                Component.translatable("screen.carrotsortingpatch.download_update"),
                button -> downloadLatestUpdate()
        ).bounds(layout.downloadButton().x(), layout.downloadButton().y(),
                layout.downloadButton().width(), layout.downloadButton().height()).build();

        this.addRenderableWidget(downloadUpdateButton);

        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> saveAndClose()
        ).bounds(layout.doneButton().x(), layout.doneButton().y(),
                layout.doneButton().width(), layout.doneButton().height()).build());

        refreshDownloadButton();
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
            this.minecraft.setScreen(lastScreen);
        }
    }

    @Override
    public void tick() {
        super.tick();
        refreshDownloadButton();
    }

    private void refreshDownloadButton() {
        if (downloadUpdateButton == null) {
            return;
        }

        UpdateCoordinator coordinator = UpdateCoordinator.getInstance();

        downloadUpdateButton.visible = !downloadStarted
                && checkForUpdatesBox.selected()
                && !autoUpdateBox.selected()
                && coordinator != null
                && coordinator.getLatestRelease().isPresent();
    }

    private void downloadLatestUpdate() {
        config.setCheckForUpdates(checkForUpdatesBox.selected());
        config.setAutoUpdate(autoUpdateBox.selected());
        config.save();

        UpdateCoordinator coordinator = UpdateCoordinator.getInstance();

        if (coordinator != null && coordinator.downloadLatestUpdate()) {
            downloadStarted = true;
            refreshDownloadButton();
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                0, 0,
                this.width, this.height,
                0xA0000000
        );

        graphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );

        if (autoUpdateBox != null) {
            UpdateScreenLayout.Warning warning = layout.warning();
            UpdateScreenLayout.Bounds panel = warning.bounds();

            // 注意書きパネル
            graphics.fill(
                    panel.x(),
                    panel.y(),
                    panel.x() + panel.width(),
                    panel.y() + panel.height(),
                    0xCC351F1F
            );

            // 左側のアクセント
            graphics.fill(
                    panel.x(),
                    panel.y(),
                    panel.x() + 2,
                    panel.y() + panel.height(),
                    0xFFFF5555
            );

            // 見出し
            if (panel.height() >= WARNING_PADDING * 2 + warning.lineHeight()) {
                graphics.drawString(
                        this.font,
                        WARNING_TITLE,
                        panel.x() + WARNING_PADDING,
                        warning.titleY(),
                        0xFFFF7777
                );
            }

            // 説明文
            for (int i = 0; i < warning.lines().size(); i++) {
                graphics.drawString(
                        this.font,
                        warning.lines().get(i),
                        panel.x() + WARNING_PADDING,
                        warning.descriptionY() + i * warning.lineHeight(),
                        0xFFE0E0E0
                );
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(lastScreen);
        }
    }
}
