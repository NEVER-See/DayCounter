package dev.daycounter.ui;

import dev.daycounter.config.DayCounterConfig.Language;
import dev.daycounter.config.DayCounterConfig.Style;
import dev.daycounter.hud.ToastRenderer;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

final class StyleChoiceButton extends DayCounterButton {
    private static final int HEIGHT = 44, PREVIEW_WIDTH = 86, TEXT_INSET = 100, CHECK_INSET = 26;
    private final Font font;
    private final Language language;
    private final Style style;
    private final List<FormattedCharSequence> description;

    StyleChoiceButton(int x, int y, int width, Font font, Language language, Style style,
                      Component name, Component description, Runnable choose) {
        super(x, y, width, HEIGHT, name, choose);
        this.font = font;
        this.language = language;
        this.style = style;
        this.description = font.split(description, width - TEXT_INSET - CHECK_INSET - 8);
    }

    @Override protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY();
        boolean highlighted = isHoveredOrFocused();
        int edge = highlighted ? 0xffaba89a : selected ? 0xff9c8456 : 0xff444d57;
        int fill = highlighted ? 0xff343942 : selected ? 0xff343128 : 0xff20252b;
        UiDraw.panel(g, x, y, getWidth(), getHeight(), 5, edge, fill);
        if (isFocused()) g.outline(x + 2, y + 2, getWidth() - 4, getHeight() - 4, UiDraw.GOLD);
        ToastRenderer.drawPreview(g, font, style, x + 7, y + 3, PREVIEW_WIDTH, HEIGHT - 6, language);

        g.text(font, getMessage(), x + TEXT_INSET, y + 6, selected ? UiDraw.GOLD : UiDraw.TEXT);
        for (int i = 0; i < Math.min(description.size(), 2); i++) {
            g.text(font, description.get(i), x + TEXT_INSET, y + 19 + i * 10, UiDraw.MUTED);
        }
        drawCheck(g, x + getWidth() - CHECK_INSET, y + 13);
    }

    private void drawCheck(GuiGraphicsExtractor g, int x, int y) {
        UiDraw.panel(g, x, y, 18, 18, 5, selected ? 0xff648e73 : 0xff52606b, selected ? 0xff284235 : 0xff20272d);
        if (!selected) return;

        int ink = 0xff91dfa9;
        for (int i = 0; i < 3; i++) g.fill(x + 4 + i, y + 8 + i, x + 6 + i, y + 10 + i, ink);
        for (int i = 0; i < 7; i++) g.fill(x + 7 + i, y + 11 - i, x + 9 + i, y + 13 - i, ink);
    }
}
