package dev.daycounter.config;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

/** UI editing rules independent of the Minecraft client. */
public final class SettingsDraft {
    private static final Pattern DECIMAL = Pattern.compile("[0-9]+(?:[.,][0-9]+)?");
    private static final Pattern SECONDS = Pattern.compile("\\s*(?:сек\\.?|s|sec|seconds?)\\s*$");
    private DayCounterConfig opening, config;
    private String durationText, xText, yText;

    public SettingsDraft(DayCounterConfig config) {
        opening = config.normalized();
        this.config = opening.copy();
        syncText();
    }
    public DayCounterConfig value() { return config; }
    public void durationText(String text) {
        durationText = text;
        Double value = parse(text, 5, 50, true);
        if (value != null) config.durationSeconds = value;
    }
    public String durationText() { return durationText; }
    public void xText(String text) {
        xText = text;
        Double value = parse(text, 0, 100, false);
        if (value != null) config.xPercent = value;
    }
    public String xText() { return xText; }
    public void yText(String text) {
        yText = text;
        Double value = parse(text, 0, 100, false);
        if (value != null) config.yPercent = value;
    }
    public String yText() { return yText; }
    public boolean durationValid() { return !config.animationEnabled || parse(durationText, 5, 50, true) != null; }
    public boolean xValid() { return config.position != DayCounterConfig.Position.MANUAL || parse(xText, 0, 100, false) != null; }
    public boolean yValid() { return config.position != DayCounterConfig.Position.MANUAL || parse(yText, 0, 100, false) != null; }
    public boolean valid() { return durationValid() && xValid() && yValid(); }
    public boolean canApply() { return valid() && !opening.equals(config); }
    public void reset() { config = new DayCounterConfig(); syncText(); }
    public void applied() { opening = config.copy(); }
    public void syncText() {
        durationText = format(config.durationSeconds);
        xText = format(config.xPercent);
        yText = format(config.yPercent);
    }
    public static String format(double value) { return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString(); }

    private static Double parse(String text, double min, double max, boolean seconds) {
        if (text == null) return null;
        String value = text.trim().toLowerCase(Locale.ROOT);
        if (seconds) value = SECONDS.matcher(value).replaceFirst("").trim();
        if (!DECIMAL.matcher(value).matches()) return null;
        try {
            double number = Double.parseDouble(value.replace(',', '.'));
            return Double.isFinite(number) && number >= min && number <= max ? number : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
