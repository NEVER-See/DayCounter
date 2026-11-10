package dev.daycounter.hud;

import dev.daycounter.DayCounterClient;
import dev.daycounter.config.ConfigStore;
import dev.daycounter.config.DayCounterConfig;
import dev.daycounter.i18n.Translations;
import dev.daycounter.ui.SunIcon;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** One small HUD layer; no offscreen render target, blur, hardware polling or tick listener. */
public final class DayCounterHud {
    private static DayCounterConfig cachedConfig;
    private static int cachedWidth, cachedHeight, cachedTextWidth;
    private static long cachedDay;
    private static DayCounterConfig.Language cachedLanguage;
    private static Component label = Component.empty();
    private static ToastLayout.Box box;
    private DayCounterHud() {}

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.gui.screen() != null || mc.gui.hud.isHidden()) return;
        var config = ConfigStore.get();
        var state = DayCounterClient.NOTIFICATION;
        long now = System.nanoTime();
        double opacity = state.opacity(config.animationEnabled, config.durationSeconds, now);
        if (opacity <= .003) return;
        if (state.day() != cachedDay || config.language != cachedLanguage) {
            cachedDay = state.day();
            cachedLanguage = config.language;
            label = Component.literal(Translations.text(config.language, "daycounter.hud.day", cachedDay));
            box = null;
        }
        int w = graphics.guiWidth(), h = graphics.guiHeight();
        int labelWidth = mc.font.width(label);
        if (box == null || config != cachedConfig || w != cachedWidth || h != cachedHeight || labelWidth != cachedTextWidth) {
            cachedConfig = config; cachedWidth = w; cachedHeight = h; cachedTextWidth = labelWidth;
            box = ToastLayout.calculate(w, h, labelWidth, config.scale,
                    ToastLayout.Anchor.valueOf(config.position.name()), config.xPercent, config.yPercent);
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) box.x(), (float) (box.y() - (1 - opacity) * 1.5));
        graphics.pose().scale((float) box.scale(), (float) box.scale());
        int width = box.baseWidth();
        rounded(graphics, 0, 0, width, ToastLayout.HEIGHT, 6, color(0x77756e, .55 * opacity));
        rounded(graphics, 1, 1, width - 2, ToastLayout.HEIGHT - 2, 5, color(0x232528, .92 * opacity));
        int frame = config.animationEnabled ? state.iconFrame(now) : SunIcon.STATIC_FRAME;
        SunIcon.draw(graphics, 11, 7, 15, frame, color(0xffffff, opacity));
        graphics.text(mc.font, label, 32 + Math.max(0, (width - 42 - labelWidth) / 2), 10, color(0xf5f2e9, opacity), true);
        graphics.pose().popMatrix();
    }

    private static int color(int rgb, double opacity) { return ((int) Math.round(255 * opacity) << 24) | rgb; }

    private static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        g.fill(x, y + radius, x + w, y + h - radius, color);
        for (int row = 0; row < radius; row++) {
            double dy = radius - row - .5;
            int inset = (int) Math.ceil(radius - Math.sqrt(radius * radius - dy * dy));
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }
}
