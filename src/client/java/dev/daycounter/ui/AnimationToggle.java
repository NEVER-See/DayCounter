package dev.daycounter.ui;

import java.util.function.Consumer;
import dev.daycounter.config.DayCounterConfig.Language;
import dev.daycounter.i18n.Translations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

final class AnimationToggle extends DayCounterButton {
    private boolean enabled;
    private double from, target;
    private long changedAt;
    private final Language language;
    private final String stateKey;

    AnimationToggle(int x, int y, boolean enabled, Language language,
                    String stateKey, Consumer<Boolean> changed) {
        super(x, y, 38, 18, message(language, stateKey, enabled), () -> { });
        this.language = language;
        this.stateKey = stateKey;
        this.enabled = enabled;
        from = target = enabled ? 1 : 0;
        changedAt = System.nanoTime();
        this.changed = changed;
    }

    private final Consumer<Boolean> changed;
    private static Component message(Language language, String stateKey, boolean enabled) {
        return Component.literal(Translations.text(language, stateKey,
                Translations.text(language, enabled ? "daycounter.on" : "daycounter.off")));
    }

    @Override public void onPress(net.minecraft.client.input.InputWithModifiers input) {
        from = progress();
        enabled = !enabled;
        target = enabled ? 1 : 0;
        changedAt = System.nanoTime();
        setMessage(message(language, stateKey, enabled));
        changed.accept(enabled);
    }

    private double progress() {
        double t = Math.min(1, (System.nanoTime() - changedAt) / 180_000_000.0);
        t = t * t * (3 - 2 * t);
        return from + (target - from) * t;
    }

    private static int blend(int off, int on, double t) {
        int red = (int) (((off >> 16) & 255) * (1 - t) + ((on >> 16) & 255) * t);
        int green = (int) (((off >> 8) & 255) * (1 - t) + ((on >> 8) & 255) * t);
        int blue = (int) ((off & 255) * (1 - t) + (on & 255) * t);
        return 0xff000000 | red << 16 | green << 8 | blue;
    }

    @Override protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        double t = progress();
        int edge = isHoveredOrFocused() ? 0xffb5c1a9 : blend(0x915b58, 0x69956f, t);
        UiDraw.panel(g, getX(), getY(), getWidth(), getHeight(), 9, edge, blend(0x7f4140, 0x437953, t));
        int knob = getX() + 3 + (int) Math.round(20 * t);
        UiDraw.rounded(g, knob, getY() + 3, 12, 12, 6, 0xfff2f2ed);
    }
}
