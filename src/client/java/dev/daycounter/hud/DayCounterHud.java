package dev.daycounter.hud;

import dev.daycounter.DayCounterClient;
import dev.daycounter.config.ConfigStore;
import dev.daycounter.config.DayCounterConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A single HUD layer with cached text, palette, and viewport fit. */
public final class DayCounterHud {
    private static final int HORIZONTAL_PADDING = 3;
    private static final int[] MAGIC_PALETTE = new int[8];
    private static NotificationState cachedState;
    private static long cachedRevision;
    private static DayCounterConfig.Language cachedLanguage;
    private static ToastRenderer.Content content;
    private static DayCounterConfig cachedConfig;
    private static int cachedWidth, cachedHeight;
    private static ToastLayout.Box box;

    private DayCounterHud() {}

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.gui.screen() != null || mc.gui.hud.isHidden()) return;
        draw(graphics, mc.font, DayCounterClient.NOTIFICATION, ConfigStore.get(), System.nanoTime());
    }

    public static void draw(GuiGraphicsExtractor graphics, Font font, NotificationState state,
                            DayCounterConfig config, long now) {
        if (state.opacity(config, now) <= 0) return;
        boolean newEvent = state != cachedState || state.revision() != cachedRevision;
        if (newEvent) {
            cachedState = state;
            cachedRevision = state.revision();
            ToastRenderer.samplePalette(MAGIC_PALETTE);
        }
        if (newEvent || config.language != cachedLanguage || content.style() != config.style) {
            cachedLanguage = config.language;
            content = ToastRenderer.Content.measure(font, config.style, config.language, state.day(), state.previousDay());
        } else {
            content = content.refreshMetrics(font);
        }

        boolean changing = state.isDayChanging(config, now);
        int width = graphics.guiWidth(), height = graphics.guiHeight();
        int baseWidth = content.width(changing) + 2 * HORIZONTAL_PADDING;
        int baseHeight = content.height() + 2 * content.verticalPadding();
        if (box == null || config != cachedConfig || width != cachedWidth || height != cachedHeight
                || box.baseWidth() != baseWidth || box.baseHeight() != baseHeight) {
            cachedConfig = config;
            cachedWidth = width;
            cachedHeight = height;
            box = ToastLayout.calculateDimensions(width, height, baseWidth, baseHeight, config.scale,
                    ToastLayout.Anchor.valueOf(config.position.name()), config.xPercent, config.yPercent);
        }

        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate((float) box.x(), (float) box.y());
            graphics.pose().scale((float) box.scale(), (float) box.scale());
            graphics.pose().translate(HORIZONTAL_PADDING, content.verticalPadding());
            ToastRenderer.draw(graphics, font, content, config.animationEnabled, state.elapsedSeconds(config, now),
                    state.visibleDurationSeconds(config), state.iconFrame(config, now), changing,
                    changing ? state.dayChangeAgeSeconds(now) : 0, config.motionEnabled, now / 1_000_000_000.0,
                    MAGIC_PALETTE);
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
