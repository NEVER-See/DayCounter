package dev.daycounter.hud;

/** Coordinates are GUI pixels. Manual values refer to the center of the toast. */
public final class ToastLayout {
    public enum Anchor { TOP, HUD, MANUAL }
    public record Box(double x, double y, double width, double height, double scale, int baseWidth, int baseHeight) {
        public Box(double x, double y, double width, double height, double scale, int baseWidth) {
            this(x, y, width, height, scale, baseWidth, HEIGHT);
        }
    }
    public static final int HEIGHT = 28;
    private ToastLayout() {}

    public static Box calculate(int screenWidth, int screenHeight, int textWidth, double requestedScale,
                                Anchor anchor, double xPercent, double yPercent) {
        int baseWidth = (int) Math.min(Integer.MAX_VALUE, Math.max(100, (long) textWidth + 42));
        return calculate(screenWidth, screenHeight, baseWidth, HEIGHT, requestedScale, anchor, xPercent, yPercent, false);
    }

    public static Box calculateDimensions(int screenWidth, int screenHeight, int baseWidth, int baseHeight,
                                          double requestedScale, Anchor anchor, double xPercent, double yPercent) {
        return calculate(screenWidth, screenHeight, baseWidth, baseHeight, requestedScale, anchor, xPercent, yPercent, true);
    }

    private static Box calculate(int screenWidth, int screenHeight, int baseWidth, int baseHeight, double requestedScale,
                                 Anchor anchor, double xPercent, double yPercent, boolean centerTop) {
        int w = Math.max(1, screenWidth), h = Math.max(1, screenHeight);
        int bw = Math.max(1, baseWidth), bh = Math.max(1, baseHeight);
        double margin = Math.min(8, Math.min(w, h) / 4.0);
        // Reserve the lower HUD's space where possible, including in short windows.
        double bottomInset = anchor == Anchor.HUD ? Math.min(52, h / 2.0) : margin;
        double scale = Math.min(bounded(requestedScale, .5, 4, 1.5),
                Math.min((w - 2 * margin) / bw, (h - margin - bottomInset) / bh));
        double width = bw * scale, height = bh * scale;
        double x = (w - width) / 2;
        double y = h * .24 - (centerTop ? height / 2 : 0);
        if (anchor == Anchor.HUD) y = h - bottomInset - height;
        if (anchor == Anchor.MANUAL) {
            x = w * bounded(xPercent, 0, 100, 50) / 100 - width / 2;
            y = h * bounded(yPercent, 0, 100, 24) / 100 - height / 2;
        }
        // A fractional fitted scale can exceed the available extent by one ULP.
        return new Box(Math.clamp(x, margin, Math.max(margin, w - margin - width)),
                Math.clamp(y, margin, Math.max(margin, h - bottomInset - height)), width, height, scale, bw, bh);
    }

    private static double bounded(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}
