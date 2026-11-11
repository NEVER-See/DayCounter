package dev.daycounter.config;

import java.util.Objects;

public final class DayCounterConfig {
    public enum Position { TOP, HUD, MANUAL }
    public enum Language { EN, RU }
    public enum Style { STANDARD, CLOUD, PLAQUE, SCROLL }

    private static final double DEFAULT_SCALE = 1.5;

    public Language language = Language.EN;
    public Style style = Style.STANDARD;
    public double durationSeconds = 8;
    public double delaySeconds = 1;
    public boolean animationEnabled = true;
    public boolean motionEnabled = true;
    public double scale = DEFAULT_SCALE;
    public Position position = Position.TOP;
    public double xPercent = 50, yPercent = 24;
    public String soundId = "minecraft:block.note_block.chime";
    public DayCounterConfig copy() {
        var copy = new DayCounterConfig();
        copy.language = language;
        copy.style = style;
        copy.durationSeconds = durationSeconds;
        copy.delaySeconds = delaySeconds;
        copy.animationEnabled = animationEnabled;
        copy.motionEnabled = motionEnabled;
        copy.scale = scale;
        copy.position = position;
        copy.xPercent = xPercent;
        copy.yPercent = yPercent;
        copy.soundId = soundId;
        return copy;
    }

    /** Returns a safe independent value; never repairs a live draft in place. */
    public DayCounterConfig normalized() {
        var result = copy();
        result.language = language == null ? Language.EN : language;
        result.style = style == null ? Style.STANDARD : style;
        result.durationSeconds = bounded(durationSeconds, 5, 50, 8);
        result.delaySeconds = bounded(delaySeconds, 1, 20, 1);
        result.scale = bounded(scale, .5, 4, DEFAULT_SCALE);
        result.xPercent = bounded(xPercent, 0, 100, 50);
        result.yPercent = bounded(yPercent, 0, 100, 24);
        result.position = position == null ? Position.TOP : position;
        result.soundId = SoundCatalog.contains(soundId) ? soundId : SoundCatalog.DEFAULT_ID;
        return result;
    }

    private static double bounded(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof DayCounterConfig c)) return false;
        return language == c.language && style == c.style
                && Double.compare(durationSeconds, c.durationSeconds) == 0
                && Double.compare(delaySeconds, c.delaySeconds) == 0
                && animationEnabled == c.animationEnabled && motionEnabled == c.motionEnabled
                && Double.compare(scale, c.scale) == 0
                && position == c.position && Double.compare(xPercent, c.xPercent) == 0
                && Double.compare(yPercent, c.yPercent) == 0 && Objects.equals(soundId, c.soundId);
    }

    @Override public int hashCode() {
        return Objects.hash(language, style, durationSeconds, delaySeconds, animationEnabled, motionEnabled,
                scale, position, xPercent, yPercent, soundId);
    }
}
