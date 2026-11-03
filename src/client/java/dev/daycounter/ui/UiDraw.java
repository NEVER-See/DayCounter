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

    static void sun(GuiGraphicsExtractor g, int x, int y) {
        int ray = 0xffd6973f;
        g.fill(x + 7, y, x + 9, y + 3, ray); g.fill(x + 7, y + 13, x + 9, y + 16, ray);
        g.fill(x, y + 7, x + 3, y + 9, ray); g.fill(x + 13, y + 7, x + 16, y + 9, ray);
        g.fill(x + 2, y + 2, x + 4, y + 4, ray); g.fill(x + 12, y + 2, x + 14, y + 4, ray);
        g.fill(x + 2, y + 12, x + 4, y + 14, ray); g.fill(x + 12, y + 12, x + 14, y + 14, ray);
        g.fill(x + 4, y + 5, x + 12, y + 11, 0xffe8ab45);
        g.fill(x + 5, y + 4, x + 11, y + 12, 0xffe8ab45);
        g.fill(x + 5, y + 5, x + 11, y + 10, 0xffffd985);
        g.fill(x + 6, y + 5, x + 10, y + 7, 0xfffff0b7);
    }
}
