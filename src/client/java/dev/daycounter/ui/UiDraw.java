package dev.daycounter.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

final class UiDraw {
    static final int TEXT = 0xffefefeb, MUTED = 0xffa6a9af, GOLD = 0xffefcd86, ERROR = 0xfff0aaa4;
    private UiDraw() { }

    static void rounded(GuiGraphicsExtractor g, int x, int y, int width, int height, int radius, int color) {
        int r = Math.min(radius, Math.min(width, height) / 2);
        g.fill(x, y + r, x + width, y + height - r, color);
        for (int row = 0; row < r; row++) {
            double dy = r - row - .5;
            int inset = (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
            g.fill(x + inset, y + height - row - 1, x + width - inset, y + height - row, color);
        }
    }

    static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int edge, int fill) {
        rounded(g, x, y, w, h, r, edge);
        rounded(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    static String fit(Font font, Component text, int width) {
        String value = text.getString();
        return font.width(value) <= width ? value
                : font.plainSubstrByWidth(value, Math.max(0, width - font.width("…"))) + "…";
    }
}
