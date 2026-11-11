package dev.daycounter.hud;

import dev.daycounter.config.DayCounterConfig;

/** Client-thread state. Time is supplied so animation needs no timer or worker. */
public final class NotificationState {
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;
    private static final long ICON_DELAY_NANOS = 400_000_000L;
    private static final long FIRST_FRAME_IN_PAIR_NANOS = 130_000_000L;
    private static final long FRAME_PAIR_NANOS = 250_000_000L;
    private static final int LAST_ICON_FRAME = 19;

    private long day;
    private long previousDay;
    private long revision;
    private long started;
    private boolean announced;
    private boolean soundPending;
    private boolean timingCaptured;
    private boolean eventAnimated;
    private long eventDelayNanos;
    private double eventDurationSeconds;

    /** Compatibility overload. The first config-aware call supplies the timing. */
    public boolean accept(long nextDay, boolean announce, long nowNanos) {
        return accept(nextDay, announce, nowNanos, null);
    }

    /** Captures timing for this packet so subsequent settings changes only affect future events. */
    public boolean accept(long nextDay, boolean announce, long nowNanos, DayCounterConfig config) {
        if (nextDay < 1 || nextDay == day) return false;
        previousDay = day;
        day = nextDay;
        revision++;
        announced = announce;
        soundPending = announce;
        started = nowNanos;
        timingCaptured = false;
        if (config != null) captureTiming(config);
        return announce;
    }

    public long day() { return day; }
    public long previousDay() { return previousDay; }
    public long revision() { return revision; }

    /** Negative during the event's configured wait, zero at appearance. */
    public double elapsedSeconds(DayCounterConfig config, long nowNanos) {
        if (day == 0) return 0;
        captureTiming(config);
        return visibleElapsedNanos(nowNanos) / NANOS_PER_SECOND;
    }

    public double visibleDurationSeconds(DayCounterConfig config) {
        if (day == 0) return config.durationSeconds;
        captureTiming(config);
        return eventDurationSeconds;
    }

    public double dayChangeAgeSeconds(long nowNanos) {
        return day == 0 ? 0 : (nowNanos - started) / NANOS_PER_SECOND;
    }

    public boolean isDayChanging(DayCounterConfig config, long nowNanos) {
        if (day == 0) return false;
        captureTiming(config);
        if (config.animationEnabled || eventAnimated || !announced || previousDay == 0) return false;
        double age = dayChangeAgeSeconds(nowNanos);
        double duration = switch (config.style) {
            case STANDARD -> .7;
            case CLOUD -> .85;
            case PLAQUE -> 2.2;
            case SCROLL -> 2;
        };
        return age >= 0 && age < duration;
    }

    /** Returns the current icon frame, or -1 for the static texture or an unknown day. */
    public int iconFrame(DayCounterConfig config, long nowNanos) {
        if (day == 0) return -1;
        captureTiming(config);
        if (!config.animationEnabled || !eventAnimated) return -1;
        return frameAt(visibleElapsedNanos(nowNanos) - ICON_DELAY_NANOS);
    }

    /** Legacy icon timing starts with the packet. */
    public int iconFrame(long nowNanos) {
        return day == 0 ? -1 : frameAt(nowNanos - started);
    }

    private static int frameAt(long elapsedNanos) {
        long elapsed = Math.max(0, elapsedNanos);
        long pair = elapsed / FRAME_PAIR_NANOS;
        int frameInPair = elapsed % FRAME_PAIR_NANOS < FIRST_FRAME_IN_PAIR_NANOS ? 0 : 1;
        return (int) Math.min(pair * 2 + frameInPair, LAST_ICON_FRAME);
    }

    public double opacity(DayCounterConfig config, long nowNanos) {
        if (day == 0) return 0;
        captureTiming(config);
        if (config.animationEnabled && !eventAnimated) return 0;
        return opacityAt(config.animationEnabled, eventDurationSeconds, visibleElapsedNanos(nowNanos) / NANOS_PER_SECOND);
    }

    public double opacity(boolean animated, double seconds, long nowNanos) {
        return opacityAt(animated, seconds, (nowNanos - started) / NANOS_PER_SECOND);
    }

    private double opacityAt(boolean animated, double seconds, double elapsed) {
        if (day == 0) return 0;
        if (!animated) return 1;
        if (!announced || elapsed < 0 || elapsed >= seconds) return 0;
        double fade = Math.min(elapsed / .25, (seconds - elapsed) / .35);
        fade = Math.clamp(fade, 0, 1);
        return fade * fade * (3 - 2 * fade);
    }

    /** Consumes one pending sound in the original display window, or discards it after expiry. */
    public boolean pollSound(DayCounterConfig config, long nowNanos) {
        if (!soundPending) return false;
        double elapsed = elapsedSeconds(config, nowNanos);
        if (elapsed < 0) return false;
        soundPending = false;
        return elapsed < eventDurationSeconds;
    }

    private void captureTiming(DayCounterConfig config) {
        if (timingCaptured) return;
        eventAnimated = config.animationEnabled;
        eventDelayNanos = eventAnimated ? Math.round(config.delaySeconds * NANOS_PER_SECOND) : 0;
        eventDurationSeconds = config.durationSeconds;
        timingCaptured = true;
    }

    private long visibleElapsedNanos(long nowNanos) {
        return nowNanos - started - eventDelayNanos;
    }

    public void reset() {
        day = 0;
        previousDay = 0;
        revision++;
        started = 0;
        announced = false;
        soundPending = false;
        timingCaptured = false;
    }
}
