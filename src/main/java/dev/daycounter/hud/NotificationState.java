package dev.daycounter.hud;

/** Client-thread state. Time is supplied so animation needs no timer or worker. */
public final class NotificationState {
    private long day;
    private long started;
    private boolean announced;

    public boolean accept(long nextDay, boolean announce, long nowNanos) {
        if (nextDay < 1 || nextDay == day) return false;
        day = nextDay;
        announced = announce;
        started = nowNanos;
        return announce;
    }

    public long day() { return day; }

    public double opacity(boolean animated, double seconds, long nowNanos) {
        if (day == 0) return 0;
        if (!animated) return 1;
        if (!announced) return 0;
        double elapsed = (nowNanos - started) / 1_000_000_000.0;
        if (elapsed < 0 || elapsed >= seconds) return 0;
        double fade = Math.min(elapsed / .25, (seconds - elapsed) / .35);
        fade = Math.clamp(fade, 0, 1);
        return fade * fade * (3 - 2 * fade);
    }

    public void reset() { day = 0; announced = false; started = 0; }
}
