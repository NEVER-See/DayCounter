package dev.daycounter.hud;

/** Coordinates are GUI pixels. Manual values refer to the center of the toast. */
public final class ToastLayout {
    public enum Anchor { TOP, HUD, MANUAL }
    public record Box(double x, double y, double width, double height, double scale, int baseWidth) {}
    public static final int HEIGHT = 28;
    private ToastLayout() {}

    public static Box calculate(int screenWidth, int screenHeight, int textWidth, double requestedScale,
                                Anchor anchor, double xPercent, double yPercent) {
        int w = Math.max(1, screenWidth), h = Math.max(1, screenHeight);
        double margin = Math.min(8, Math.min(w, h) / 4.0);
        int baseWidth = Math.max(100, Math.max(0, textWidth) + 42);
        double scale = Math.min(Math.clamp(requestedScale, .5, 2),
                Math.min((w - 2 * margin) / baseWidth, (h - 2 * margin) / HEIGHT));
        double width = baseWidth * scale, height = HEIGHT * scale;
        double x = (w - width) / 2, y = h * .24;
        if (anchor == Anchor.HUD) y = h - 52 - height;
        if (anchor == Anchor.MANUAL) {
            x = w * Math.clamp(xPercent, 0, 100) / 100 - width / 2;
            y = h * Math.clamp(yPercent, 0, 100) / 100 - height / 2;
        }
        // A fractional fitted scale can exceed the available extent by one ULP.
        // Keep clamp bounds ordered even at the exact fitting boundary.
        return new Box(Math.clamp(x, margin, Math.max(margin, w - margin - width)),
                Math.clamp(y, margin, Math.max(margin, h - margin - height)), width, height, scale, baseWidth);
    }
}
