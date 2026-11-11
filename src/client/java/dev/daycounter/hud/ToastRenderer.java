package dev.daycounter.hud;

import dev.daycounter.config.DayCounterConfig.Language;
import dev.daycounter.config.DayCounterConfig.Style;
import dev.daycounter.i18n.Translations;
import dev.daycounter.ui.SunIcon;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Pixel shapes and native text shared by the in-game toast and settings samples. */
public final class ToastRenderer {
    private static final double DETAIL_DELAY = .4;
    private static final float STANDARD_TEXT = 1.25f;
    private static final float CLOUD_TEXT = 1.5f;
    private static final float PLAQUE_TEXT = 2.125f;
    private static final float SCROLL_TEXT = 2f;
    private static final int ENGRAVED_INK = 0x51311e;
    private static final int SCROLL_INK = 0x695153;
    private static final int[] ROUND_INSETS = {3, 2, 1, 1};
    private static final int[] MAGIC_COLORS = {
            0x91dfb1, 0x67dfcb, 0x6ad4ed, 0x78b7ff, 0x8d9dff, 0xb197ff, 0xcd9af3, 0xe197ed,
            0xf19ed1, 0xf6a4b1, 0xf7af97, 0xf1bd85, 0xedce82, 0xdde28a, 0xbee49a, 0x9ad9bb,
            0x88d0d6, 0xa8c4f7, 0xd0b4e9, 0xe9bdc8
    };
    private static final Content[][] PREVIEWS = new Content[Language.values().length][Style.values().length];

    private static long fontGeneration;

    private ToastRenderer() {}

    private static int fontOptions() {
        var options = Minecraft.getInstance().options;
        return (options.forceUnicodeFont().get() ? 1 : 0) | (options.japaneseGlyphVariants().get() ? 2 : 0);
    }

    static void resourcesReloaded() {
        fontGeneration++;
    }

    public static void initializePlaque(Minecraft client) {
        PlaqueEngraving.initialize(client);
    }

    public static void preparePlaque(long day, long previousDay, boolean changing) {
        PlaqueEngraving.prepare(day, previousDay, changing);
    }

    record Content(Style style, Component prefix, int prefixWidth, Component current, int currentWidth,
                   Component previous, int previousWidth, long currentDay, long previousDay, long metricsGeneration, int metricsOptions) {
        static Content measure(Font font, Style style, Language language, long currentDay, long previousDay) {
            Component prefix = style == Style.STANDARD
                    ? Component.literal(Translations.text(language, "daycounter.hud.day", ""))
                    : Component.empty();
            Component current = Component.literal(Long.toString(currentDay));
            Component previous = previousDay > 0 ? Component.literal(Long.toString(previousDay)) : current;
            int prefixWidth = style == Style.STANDARD ? font.width(prefix.getVisualOrderText()) : 0;
            int currentWidth = font.width(current.getVisualOrderText());
            int previousWidth = previous == current ? currentWidth : font.width(previous.getVisualOrderText());
            return new Content(style, prefix, prefixWidth, current, currentWidth, previous, previousWidth,
                    currentDay, previousDay, fontGeneration, fontOptions());
        }

        Content refreshMetrics(Font font) {
            if (style == Style.PLAQUE && PlaqueEngraving.isReady()) return this;
            if (metricsGeneration == fontGeneration && metricsOptions == fontOptions()) return this;
            // A resource reload can replace glyph providers in the same Font instance.
            int newPrefixWidth = style == Style.STANDARD ? font.width(prefix.getVisualOrderText()) : 0;
            int newCurrentWidth = font.width(current.getVisualOrderText());
            int newPreviousWidth = previous == current ? newCurrentWidth : font.width(previous.getVisualOrderText());

            return new Content(style, prefix, newPrefixWidth, current, newCurrentWidth, previous, newPreviousWidth,
                    currentDay, previousDay, fontGeneration, fontOptions());
        }

        private int numberWidth(boolean changing) {
            return changing ? Math.max(currentWidth, previousWidth) : currentWidth;
        }

        int frontWidth(boolean changing) {
            return Math.max(64, (int) Math.ceil(numberWidth(changing) * CLOUD_TEXT) + 24);
        }

        int width(boolean changing) {
            int number = numberWidth(changing);
            return switch (style) {
                case STANDARD -> Math.max(104, (int) Math.ceil((prefixWidth + number) * STANDARD_TEXT) + 43);
                case CLOUD -> Math.max(144, frontWidth(changing) + 52);
                case PLAQUE -> PlaqueEngraving.isReady() ? PlaqueEngraving.width(currentDay, changing ? previousDay : 0)
                        : Math.max(112, (int) Math.ceil(number * PLAQUE_TEXT) + 66);
                case SCROLL -> Math.max(126, (int) Math.ceil(number * SCROLL_TEXT) + 82);
            };
        }

        int height() {
            return switch (style) {
                case STANDARD -> ToastLayout.HEIGHT;
                case CLOUD -> 69;
                case PLAQUE -> 38;
                case SCROLL -> 46;
            };
        }

        int verticalPadding() {
            return switch (style) {
                case STANDARD -> 6;
                case CLOUD -> 3;
                case PLAQUE -> 11;
                case SCROLL -> 7;
            };
        }
    }

    public static void drawPreview(GuiGraphicsExtractor g, Font font, Style style, int x, int y,
                                   int width, int height, Language language) {
        if (width <= 0 || height <= 0) return;
        int row = language.ordinal(), column = style.ordinal();
        Content content = PREVIEWS[row][column];
        content = content == null ? Content.measure(font, style, language, 42, 0) : content.refreshMetrics(font);
        PREVIEWS[row][column] = content;
        int baseWidth = content.width(false), baseHeight = content.height();
        float scale = Math.min(1.7f, Math.min(width / (baseWidth + 6f), height / (baseHeight + 6f)));
        clip(g, x, y, x + width, y + height);
        try {
            g.pose().pushMatrix();
            try {
                g.pose().translate(x + (width - baseWidth * scale) / 2, y + (height - baseHeight * scale) / 2);
                g.pose().scale(scale, scale);
                if (style == Style.PLAQUE) {
                    drawPlaque(g, font, content, baseWidth, 1, 1, false, 0, false, 0, true);
                } else {
                    draw(g, font, content, false, 0, 0, SunIcon.STATIC_FRAME, false, 0, false, 0, null);
                }
            } finally {
                g.pose().popMatrix();
            }
        } finally {
            g.disableScissor();
        }
    }

    /** Refresh one palette per event. Adjacent samples and the preceding opening color differ. */
    static void samplePalette(int[] sequence) {
        var random = ThreadLocalRandom.current();
        int excluded = sequence[0];
        for (int i = 0; i < sequence.length; i++) {
            int color = random.nextInt(MAGIC_COLORS.length - 1);
            if (color >= excluded) color++;
            sequence[i] = color;
            excluded = color;
        }
    }

    static void draw(GuiGraphicsExtractor g, Font font, Content content, boolean animated, double elapsed,
                     double duration, int frame, boolean changing, double changeAge, boolean motion,
                     double motionTime, int[] palette) {
        Style style = content.style;
        double opening = style == Style.STANDARD ? .45 : .85;
        double closing = style == Style.STANDARD ? .45 : .8;
        double enter = animated ? ease(elapsed / opening) : 1;
        double exit = animated ? ease((elapsed - (duration - closing)) / closing) : 0;
        double opacity = enter * (1 - exit);
        if (opacity < 1.0 / 255) return;
        int width = content.width(changing);
        float bob = motion && style != Style.CLOUD
                ? wave(motionTime, style == Style.PLAQUE ? 5.4 : 4.8, 0, 2.3) : 0;
        g.pose().pushMatrix();
        try {
            g.pose().translate(0, bob);
            switch (style) {
                case STANDARD -> drawStandard(g, font, content, width, enter, opacity, animated, elapsed, frame,
                        changing, changeAge);
                case CLOUD -> drawClouds(g, font, content, width, opacity, animated, elapsed, duration, frame,
                        changing, changeAge, motion, motionTime);
                case PLAQUE -> drawPlaque(g, font, content, width, enter, opacity, animated, elapsed,
                        changing, changeAge, false);
                case SCROLL -> drawScroll(g, font, content, width, enter, opacity, animated, elapsed, frame,
                        changing, changeAge, palette);
            }
        } finally {
            g.pose().popMatrix();
        }
    }

    private static void drawStandard(GuiGraphicsExtractor g, Font font, Content content, int width,
                                     double enter, double opacity, boolean animated, double elapsed, int frame,
                                     boolean changing, double changeAge) {
        g.pose().translate(0, (float) ((1 - enter) * -3));
        int height = ToastLayout.HEIGHT;
        int background = color(0x26282a, opacity * .91);
        rect(g, 0, 4, width, height - 8, background);
        for (int row = 0; row < ROUND_INSETS.length; row++) {
            int inset = ROUND_INSETS[row];
            rect(g, inset, row, width - inset * 2, 1, background);
            rect(g, inset, height - row - 1, width - inset * 2, 1, background);
        }
        rect(g, 3, 1, width - 6, 1, color(0xffffff, .08 * opacity));
        double sunOpacity = animated ? opacity * ease((elapsed - DETAIL_DELAY) / .25) : opacity;
        if (sunOpacity >= 1.0 / 255) {
            SunIcon.draw(g, 6, 4, 20, frame, color(0xffffff, sunOpacity));
        }
        int ink = color(0xf4dc9a, opacity);
        text(g, font, content.prefix, 34, 15, STANDARD_TEXT, ink);
        float numberX = 34 + content.prefixWidth * STANDARD_TEXT;
        if (!changing) {
            text(g, font, content.current, numberX, 15, STANDARD_TEXT, ink);
            return;
        }
        float offset = (float) (18 * ease(changeAge / .7));
        clip(g, (int) numberX, 6, width - 7, 24);
        try {
            text(g, font, content.previous, numberX, 15 + offset, STANDARD_TEXT, ink);
            text(g, font, content.current, numberX, 15 - 18 + offset, STANDARD_TEXT, ink);
        } finally {
            g.disableScissor();
        }
    }

    private static void drawClouds(GuiGraphicsExtractor g, Font font, Content content, int width,
                                   double opacity, boolean animated, double elapsed, double duration, int frame,
                                   boolean changing, double changeAge, boolean motion, double motionTime) {
        float center = width / 2f;
        float groupScale = .9f + .1f * (float) opacity;
        g.pose().translate(center, 34.5f);
        g.pose().scale(groupScale, groupScale);
        g.pose().translate(-center, -34.5f);

        double closingAge = elapsed - (duration - .8);
        double shrink = animated ? ease(closingAge / .65) : 0;
        double grow = animated ? ease((elapsed - DETAIL_DELAY) / .9) : 1;
        double sunOpacity = animated ? opacity * ease((elapsed - DETAIL_DELAY) / .45) * (1 - shrink) : opacity;
        if (sunOpacity >= 1.0 / 255) {
            float size = (float) ((10 + 37 * grow) * (1 - shrink));
            g.pose().pushMatrix();
            try {
                g.pose().translate(center - size / 2, 27 - size / 2);
                g.pose().scale(size / 47, size / 47);
                SunIcon.draw(g, 0, 0, 47, frame, color(0xffffff, sunOpacity));
            } finally {
                g.pose().popMatrix();
            }
        }

        float leftX = motion ? wave(motionTime, 5.3, 0, 2) : 0;
        float leftY = motion ? wave(motionTime, 4.1, .6, 1.8) : 0;
        float rightX = motion ? wave(motionTime, 6.1, 2.1, 2.2) : 0;
        float rightY = motion ? wave(motionTime, 5.4, 3.2, 2.1) : 0;
        float frontX = motion ? wave(motionTime, 4.7, 4.3, 1.3) : 0;
        float frontY = motion ? wave(motionTime, 6.3, 4.7, 1.6) : 0;
        float openness = (float) opacity;
        cloud(g, center - 26 - 38 * openness + leftX, 27 + 4 * (1 - openness) + leftY, 49, 25, opacity);
        cloud(g, center - 23 + 38 * openness + rightX, 25 + 5 * (1 - openness) + rightY, 49, 27, opacity);
        int frontWidth = content.frontWidth(changing);
        cloud(g, center - frontWidth / 2f + frontX, 42 + frontY, frontWidth, 27, opacity);
        double inkOpacity = animated ? opacity * (1 - ease((elapsed - (duration - .95)) / .42)) : opacity;
        float numberX = center + frontX, numberY = 56 + frontY;
        if (!changing) {
            centeredNumber(g, font, content, false, numberX, numberY, CLOUD_TEXT, color(0x34424c, inkOpacity));
            return;
        }
        double progress = changeAge / .85;
        if (progress < .45) {
            centeredNumber(g, font, content, true, numberX, numberY, CLOUD_TEXT,
                    color(0x34424c, inkOpacity * (1 - ease(progress / .45))));
        } else if (progress > .55) {
            centeredNumber(g, font, content, false, numberX, numberY, CLOUD_TEXT,
                    color(0x34424c, inkOpacity * ease((progress - .55) / .45)));
        }
    }

    private static void cloud(GuiGraphicsExtractor g, float x, float y, int w, int h, double opacity) {
        int body = color(0xe1ebee, opacity);
        int highlight = color(0xf5f6ef, opacity);
        int shade = color(0xd1dfe6, opacity);
        int edge = color(0xa7bdca, opacity);
        g.pose().pushMatrix();
        try {
            g.pose().translate(x, y);
            rect(g, 13, 0, w - 28, 3, body);
            rect(g, 13, 3, w - 28, 1, highlight);
            rect(g, 13, 4, w - 19, 2, highlight);
            rect(g, 6, 6, w - 12, 1, highlight);
            rect(g, 6, 7, w - 12, 1, body);
            rect(g, 6, 8, w - 6, 1, body);
            rect(g, 0, 9, w, h - 17, body);
            rect(g, 0, h - 8, w, 3, shade);
            rect(g, 0, h - 5, w, 1, edge);
            rect(g, 4, h - 4, w - 9, 4, edge);
        } finally {
            g.pose().popMatrix();
        }
    }

    private static void drawPlaque(GuiGraphicsExtractor g, Font font, Content content, int w, double enter,
                                   double opacity, boolean animated, double elapsed, boolean changing,
                                   double changeAge, boolean preview) {
        float scale = .91f + .09f * (float) enter;
        g.pose().translate(w / 2f, 19f - (float) ((1 - enter) * 8));
        g.pose().scale(scale, scale);
        g.pose().translate(-w / 2f, -19);
        rect(g, 2, 3, w - 4, 35, color(0x37281e, opacity));
        rect(g, 0, 6, w, 29, color(0x37281e, opacity));
        rect(g, 2, 1, w - 4, 34, color(0x85603a, opacity));
        rect(g, 0, 5, w, 27, color(0x85603a, opacity));
        rect(g, 3, 2, w - 6, 3, color(0xbd935d, opacity));
        rect(g, 3, 3, 3, 28, color(0xbd935d, opacity));
        rect(g, 6, 5, w - 10, 25, color(0x9e774a, opacity));
        rect(g, 7, 6, w - 13, 11, color(0xaa8251, opacity));
        rect(g, 7, 18, w - 13, 1, color(0x6d4d2e, opacity));
        rect(g, 7, 31, w - 13, 2, color(0x6d4d2e, opacity));
        for (int x = 10; x < w - 14; x += 31) {
            rect(g, x, 8 + x % 3, Math.min(15, w - x - 9), 1, color(0xbc955f, opacity));
            rect(g, x + 3, 25, Math.min(9, w - x - 12), 1, color(0xbc955f, opacity));
            rect(g, x - 2, 13, 8, 1, color(0x886139, opacity));
        }
        int nail = color(0x41362b, opacity);
        rect(g, 6, 6, 2, 2, nail);
        rect(g, w - 8, 6, 2, 2, nail);
        rect(g, 6, 28, 2, 2, nail);
        rect(g, w - 8, 28, 2, 2, nail);

        if (PlaqueEngraving.isReady()) {
            PlaqueEngraving.draw(g, content.currentDay, content.previousDay, w, opacity,
                    animated, elapsed - DETAIL_DELAY, changing, changeAge, preview);
            return;
        }

        // Keep the number readable if a resource pack has an invalid font atlas.
        float center = w / 2f + 20;
        double sunOpacity = animated ? opacity * ease((elapsed - DETAIL_DELAY) / .3) : opacity;
        if (sunOpacity >= 1.0 / 255) {
            SunIcon.draw(g, 13, 7, 24, SunIcon.STATIC_FRAME, color(ENGRAVED_INK, sunOpacity));
        }
        boolean previous = changing && changeAge < .2;
        double inkOpacity = changing
                ? opacity * (previous ? 1 - ease(changeAge / .2) : ease((changeAge - .2) / 2))
                : sunOpacity;
        centeredNumber(g, font, content, previous, center, 20, PLAQUE_TEXT, color(ENGRAVED_INK, inkOpacity));
    }

    private static void drawScroll(GuiGraphicsExtractor g, Font font, Content content, int w, double enter,
                                   double opacity, boolean animated, double elapsed, int frame, boolean changing,
                                   double changeAge, int[] palette) {
        g.pose().translate(0, (float) ((1 - enter) * -4));
        scrollPaper(g, w, opacity);
        float center = w / 2f + 19;
        int ink = color(SCROLL_INK, opacity);
        int tint = color(0xffffff, opacity);
        if (animated) {
            revealScrollInk(g, font, content, w, center, (elapsed - DETAIL_DELAY) / 1.8,
                    opacity, frame, true, palette);
            return;
        }
        SunIcon.draw(g, 22, 11, 24, SunIcon.STATIC_FRAME, tint);
        if (!changing) {
            centeredNumber(g, font, content, false, center, 25, SCROLL_TEXT, ink);
        } else if (changeAge < .2) {
            centeredNumber(g, font, content, true, center, 25, SCROLL_TEXT,
                    color(SCROLL_INK, opacity * (1 - ease(changeAge / .2))));
        } else {
            revealScrollInk(g, font, content, w, center, (changeAge - .2) / 1.8,
                    opacity, frame, false, palette);
        }
    }

    private static void scrollPaper(GuiGraphicsExtractor g, int w, double opacity) {
        rect(g, 7, 7, w - 12, 37, color(0x48372d, opacity * .22));
        rect(g, 12, 4, w - 24, 37, color(0xc3a28e, opacity));
        rect(g, 13, 6, w - 26, 32, color(0xefd7c8, opacity));
        rect(g, 13, 6, w - 26, 3, color(0xfff7ee, opacity));
        rect(g, 15, 9, w - 30, 25, color(0xf7e5d9, opacity));
        rect(g, 15, 32, w - 30, 5, color(0xecd0be, opacity));
        rect(g, 13, 38, w - 26, 2, color(0xd3b39e, opacity));
        rect(g, 18, 10, w - 36, 1, color(0xfff9f1, opacity));
        rect(g, 20, 35, w - 40, 1, color(0xdfc1ad, opacity));
        scrollRoll(g, 3, opacity);
        scrollRoll(g, w - 19, opacity);
    }

    private static void scrollRoll(GuiGraphicsExtractor g, int x, double opacity) {
        rect(g, x + 3, 0, 10, 44, color(0xa98d7b, opacity));
        rect(g, x, 4, 16, 35, color(0xa98d7b, opacity));
        rect(g, x + 1, 5, 14, 34, color(0xdfbfaa, opacity));
        rect(g, x + 3, 2, 10, 40, color(0xdfbfaa, opacity));
        rect(g, x + 3, 5, 10, 33, color(0xf7e7dc, opacity));
        rect(g, x + 4, 6, 4, 31, color(0xfffaf4, opacity));
        rect(g, x + 11, 7, 2, 28, color(0xe7cbb8, opacity));
        rect(g, x + 3, 38, 10, 3, color(0xc9a88f, opacity));
        rect(g, x + 3, 2, 10, 3, color(0xfff5e9, opacity));
        rect(g, x + 5, 3, 5, 1, color(0xb3947d, opacity));
        rect(g, x + 4, 40, 8, 2, color(0xf7e6d7, opacity));
    }

    private static void revealScrollInk(GuiGraphicsExtractor g, Font font, Content content, int width, float center,
                                        double progress, double opacity, int frame, boolean includeSun, int[] palette) {
        if (progress <= 0) return;
        int ink = color(SCROLL_INK, opacity), tint = color(0xffffff, opacity);
        if (progress >= 1) {
            if (includeSun) SunIcon.draw(g, 22, 11, 24, frame, tint);
            centeredNumber(g, font, content, false, center, 25, SCROLL_TEXT, ink);
            return;
        }
        int left = includeSun ? 20 : 55;
        int right = width - 21;
        int edge = left + (int) Math.round((right - left) * progress);
        if (edge > left) {
            clip(g, left, 8, edge, 37);
            try {
                if (includeSun) SunIcon.draw(g, 22, 11, 24, frame, tint);
                centeredNumber(g, font, content, false, center, 25, SCROLL_TEXT, ink);
            } finally {
                g.disableScissor();
            }
        }
        magicSweep(g, includeSun ? 18 : 49, width, edge, progress, opacity, palette);
    }

    private static void magicSweep(GuiGraphicsExtractor g, int left, int width, int x, double progress,
                                   double opacity, int[] palette) {
        double position = progress * 1.8 / .32;
        int index = (int) position;
        int from = MAGIC_COLORS[palette[index]];
        int to = MAGIC_COLORS[palette[index + 1]];
        double mix = ease(position - index);
        int rgb = (mixChannel(from, to, 16, mix) << 16) | (mixChannel(from, to, 8, mix) << 8)
                | mixChannel(from, to, 0, mix);
        clip(g, left, 8, width - 18, 37);
        try {
            rect(g, x - 6, 9, 12, 27, color(rgb, opacity * .19));
            rect(g, x - 3, 10, 6, 25, color(rgb, opacity * .46));
            rect(g, x - 1, 12, 3, 20, color(rgb, opacity));
            rect(g, x, 11, 1, 22, color(0xfdf4ff, opacity));
            int step = (int) (progress * 18);
            for (int i = 0; i < 3; i++) {
                int sx = x - 4 - i * 3, sy = 13 + (step * 3 + i * 7) % 19;
                int spark = color(i == 1 ? 0xfaffff : rgb, opacity);
                rect(g, sx, sy, 3, 1, spark);
                rect(g, sx + 1, sy - 1, 1, 3, spark);
            }
            rect(g, x - 1, 10, 3, 2, color(0xffffff, opacity));
            rect(g, x - 1, 32, 3, 2, color(0xffffff, opacity));
        } finally {
            g.disableScissor();
        }
    }

    private static void centeredNumber(GuiGraphicsExtractor g, Font font, Content content, boolean previous,
                                       float center, float y, float scale, int ink) {
        Component number = previous ? content.previous : content.current;
        int width = previous ? content.previousWidth : content.currentWidth;
        text(g, font, number, center - width * scale / 2, y, scale, ink);
    }

    private static void text(GuiGraphicsExtractor g, Font font, Component label, float x, float centerY,
                             float scale, int ink) {
        // The old font renderer interprets alpha 0–3 as opaque.
        if ((ink >>> 24) < 4) return;
        g.pose().pushMatrix();
        try {
            g.pose().translate(x, centerY - font.lineHeight * scale / 2);
            g.pose().scale(scale, scale);
            g.text(font, label, 0, 0, ink, false);
        } finally {
            g.pose().popMatrix();
        }
    }

    private static void clip(GuiGraphicsExtractor g, int x, int y, int right, int bottom) {
        g.enableScissor(x, y, right, bottom);
    }

    private static void rect(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        g.fill(x, y, x + width, y + height, color);
    }

    private static int color(int rgb, double opacity) {
        return ((int) Math.round(255 * opacity) << 24) | rgb;
    }

    private static int mixChannel(int from, int to, int shift, double mix) {
        int first = (from >>> shift) & 255;
        int second = (to >>> shift) & 255;
        return first + (int) Math.round((second - first) * mix);
    }

    private static float wave(double time, double period, double phase, double amplitude) {
        return (float) (Math.sin(time * 2 * Math.PI / period + phase) * amplitude);
    }

    private static double ease(double value) {
        double clamped = Math.clamp(value, 0, 1);
        return clamped * clamped * (3 - 2 * clamped);
    }
}
