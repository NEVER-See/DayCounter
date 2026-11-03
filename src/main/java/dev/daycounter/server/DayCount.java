package dev.daycounter.server;

/** World-owned count, independent from Minecraft's command-editable clock history. */
public final class DayCount {
    private static final long TICKS_PER_DAY = 24_000;
    private long day;

    public DayCount() {
        this(1);
    }

    public DayCount(long day) {
        this.day = Math.max(1, day);
    }

    public long day() {
        return day;
    }

    /** Observe only the actual clock tick, never the interval between server ticks. */
    public boolean naturalTick(long before, long after) {
        if (after <= before) {
            return false;
        }
        long dawns = Math.floorDiv(after, TICKS_PER_DAY) - Math.floorDiv(before, TICKS_PER_DAY);
        return advance(dawns);
    }

    /** Called only for a successful sleep transition in the server level. */
    public boolean sleep(long before, long after) {
        return after > before && advance(1);
    }

    private boolean advance(long amount) {
        if (amount <= 0 || day == Long.MAX_VALUE) {
            return false;
        }
        day += Math.min(amount, Long.MAX_VALUE - day);
        return true;
    }
}
