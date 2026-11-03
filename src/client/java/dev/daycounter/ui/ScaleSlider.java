package dev.daycounter.ui;

import java.util.function.DoubleConsumer;
import dev.daycounter.config.DayCounterConfig.Language;
import dev.daycounter.i18n.Translations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

final class ScaleSlider extends AbstractSliderButton {
    private final DoubleConsumer changed;
    private final Language language;

    ScaleSlider(int x, int y, int width, double scale, Language language, DoubleConsumer changed) {
        super(x, y, width, 16, Component.empty(), (scale - .5) / 1.5);
        this.changed = changed;
        this.language = language;
        updateMessage();
    }

    private double scale() { return (50 + Math.round(value * 30) * 5) / 100.0; }
    @Override protected void updateMessage() {
        setMessage(Component.literal(Translations.text(language, "daycounter.scale.value", Math.round(scale() * 100))));
    }
    @Override protected void applyValue() { changed.accept(scale()); }

    @Override public void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int cy = getY() + 8;
        int handle = getX() + 4 + (int) Math.round(value * (getWidth() - 8));
        UiDraw.panel(g, getX(), cy - 2, getWidth(), 5, 2, 0xff71716b, 0xff3b3d3d);
        UiDraw.rounded(g, getX() + 1, cy - 1, Math.max(1, handle - getX()), 3, 1, 0xffdfbd79);
        UiDraw.rounded(g, handle - 5, cy - 5, 10, 10, 5, isHoveredOrFocused() ? 0xffffe4aa : UiDraw.GOLD);
        if (isFocused()) g.outline(handle - 6, cy - 6, 12, 12, 0xffa38b5f);
    }
}
