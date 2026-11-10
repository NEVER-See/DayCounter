package dev.daycounter.config;

import java.util.Objects;

public final class DayCounterConfig {
    public enum Position { TOP, HUD, MANUAL }
    public enum Language { EN, RU }

    private static final double DEFAULT_SCALE = 1.5;

    public Language language = Language.EN;
    public double durationSeconds = 8;
    public boolean animationEnabled = true;
    public double scale = DEFAULT_SCALE;
    public Position position = Position.TOP;
    public double xPercent = 50, yPercent = 24;
    public String soundId = "minecraft:block.note_block.chime";
    public DayCounterConfig copy() {
        var copy = new DayCounterConfig();
        copy.language = language;
        copy.durationSeconds = durationSeconds;
        copy.animationEnabled = animationEnabled;
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
        result.durationSeconds = bounded(durationSeconds, 5, 50, 8);
        result.scale = bounded(scale, .5, 2, DEFAULT_SCALE);
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
        return language == c.language && Double.compare(durationSeconds, c.durationSeconds) == 0
                && animationEnabled == c.animationEnabled && Double.compare(scale, c.scale) == 0
                && position == c.position && Double.compare(xPercent, c.xPercent) == 0
                && Double.compare(yPercent, c.yPercent) == 0 && Objects.equals(soundId, c.soundId);
    }

    @Override public int hashCode() {
        return Objects.hash(language, durationSeconds, animationEnabled, scale, position, xPercent, yPercent, soundId);
    }
}
