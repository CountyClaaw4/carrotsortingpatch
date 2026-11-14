package io.github.countyclaaw4.carrotcraftsortingpatch.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class UpdateScreenLayout {

    private static final int SIDE_MARGIN = 16;
    private static final int PANEL_PADDING = 16;
    private static final int MAX_CONTENT_WIDTH = 440;
    private static final int MAX_PANEL_HEIGHT = 300;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 10;
    private static final int BOTTOM_MARGIN = 20;
    private static final int WARNING_PADDING = 10;

    private UpdateScreenLayout() {}

    public static InitialLaunch initialLaunch(
            int screenWidth,
            int screenHeight,
            Font font,
            Component description1,
            Component description2,
            Component warningDescription) {
        int panelWidth = Math.max(1, Math.min(MAX_CONTENT_WIDTH, screenWidth - SIDE_MARGIN * 2));
        int panelHeight = Math.max(1, Math.min(MAX_PANEL_HEIGHT, screenHeight - SIDE_MARGIN * 2));
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = (screenHeight - panelHeight) / 2;
        int contentX = panelX + PANEL_PADDING;
        int contentWidth = Math.max(1, panelWidth - PANEL_PADDING * 2);
        int titleY = panelY + 12;
        int descriptionY = panelY + 34;
        int buttonWidth = Math.min(140, contentWidth);
        int buttonY = panelY + panelHeight - 28;

        List<FormattedCharSequence> allDescription1 = font.split(description1, contentWidth);
        List<FormattedCharSequence> allDescription2 = font.split(description2, contentWidth);
        int maxCheckY = buttonY - 52;
        int desiredCheckY = descriptionY + (allDescription1.size() + allDescription2.size()) * font.lineHeight + 12;
        int checkY = Math.max(panelY + 32, Math.min(desiredCheckY, maxCheckY));
        int descriptionLines = Math.max(0, (checkY - descriptionY - 8) / Math.max(1, font.lineHeight));
        List<FormattedCharSequence> visibleDescription1 = limit(allDescription1, descriptionLines);
        List<FormattedCharSequence> visibleDescription2 = limit(
                allDescription2, Math.max(0, descriptionLines - visibleDescription1.size()));

        int warningY = checkY + 28 + 24;
        Warning warning = warning(font, warningDescription, contentX, warningY, contentWidth,
                Math.max(0, buttonY - warningY - 6));

        return new InitialLaunch(
                new Bounds(panelX, panelY, panelWidth, panelHeight),
                contentX,
                contentWidth,
                titleY,
                descriptionY,
                visibleDescription1,
                visibleDescription2,
                checkY,
                checkY + 28,
                warning,
                new Bounds((screenWidth - buttonWidth) / 2, buttonY, buttonWidth, BUTTON_HEIGHT));
    }

    public static Settings settings(
            int screenWidth,
            int screenHeight,
            Font font,
            Component warningDescription) {
        int contentWidth = Math.max(1, Math.min(MAX_CONTENT_WIDTH, screenWidth - SIDE_MARGIN * 2));
        int contentX = (screenWidth - contentWidth) / 2;
        int checkY = Math.max(32, Math.min(92, (screenHeight - 180) / 3));
        int buttonWidth = Math.max(1, Math.min(240, screenWidth - SIDE_MARGIN * 2));
        int doneY = Math.max(0, screenHeight - BUTTON_HEIGHT - BOTTOM_MARGIN);
        int downloadY = Math.max(0, doneY - BUTTON_HEIGHT - BUTTON_GAP);
        int warningY = checkY + 28 + 24;
        Warning warning = warning(font, warningDescription, contentX, warningY, contentWidth,
                Math.max(0, downloadY - warningY - 6));

        return new Settings(
                contentX,
                contentWidth,
                checkY,
                checkY + 28,
                warning,
                new Bounds((screenWidth - buttonWidth) / 2, downloadY, buttonWidth, BUTTON_HEIGHT),
                new Bounds((screenWidth - buttonWidth) / 2, doneY, buttonWidth, BUTTON_HEIGHT));
    }

    private static Warning warning(
            Font font,
            Component description,
            int x,
            int y,
            int width,
            int availableHeight) {
        int lineHeight = Math.max(1, font.lineHeight);
        int titleHeight = lineHeight;
        int maxLines = (availableHeight - WARNING_PADDING * 2 - titleHeight - 4) / lineHeight;
        List<FormattedCharSequence> allLines = font.split(description, Math.max(1, width - WARNING_PADDING * 2));
        List<FormattedCharSequence> lines = limit(allLines, maxLines);
        int height = Math.min(availableHeight,
                WARNING_PADDING * 2 + titleHeight + 4 + lines.size() * lineHeight);
        return new Warning(
                new Bounds(x, y, width, Math.max(0, height)),
                lines,
                y + WARNING_PADDING,
                y + WARNING_PADDING + titleHeight + 4,
                lineHeight);
    }

    private static List<FormattedCharSequence> limit(List<FormattedCharSequence> lines, int maxLines) {
        return lines.subList(0, Math.min(lines.size(), Math.max(0, maxLines)));
    }

    public record Bounds(int x, int y, int width, int height) {}

    public record Warning(
            Bounds bounds,
            List<FormattedCharSequence> lines,
            int titleY,
            int descriptionY,
            int lineHeight) {}

    public record InitialLaunch(
            Bounds panel,
            int contentX,
            int contentWidth,
            int titleY,
            int descriptionY,
            List<FormattedCharSequence> description1,
            List<FormattedCharSequence> description2,
            int checkY,
            int autoY,
            Warning warning,
            Bounds button) {}

    public record Settings(
            int contentX,
            int contentWidth,
            int checkY,
            int autoY,
            Warning warning,
            Bounds downloadButton,
            Bounds doneButton) {}
}