package dev.daycounter.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DayCountTest {
    @Test
    void installationDoesNotGuessOldWorldHistory() {
        DayCount counter = new DayCount();
        assertFalse(counter.naturalTick(2_400_000, 2_400_001));
        assertEquals(1, counter.day());
    }

    @Test
    void naturalDawnAddsOneAndDoesNotRepeatAtTheBoundary() {
        DayCount counter = new DayCount();
        assertFalse(counter.naturalTick(23_998, 23_999));
        assertTrue(counter.naturalTick(23_999, 24_000));
        assertFalse(counter.naturalTick(24_000, 24_001));
        assertEquals(2, counter.day());
    }

    @Test
    void commandsBetweenNaturalTicksDoNotBecomeElapsedDays() {
        DayCount counter = new DayCount();
        counter.naturalTick(12_000, 12_001);
        // /time set and /time add are outside the observed natural tick operation.
        assertFalse(counter.naturalTick(2_400_000, 2_400_001));
        assertFalse(counter.naturalTick(1_000, 1_001));
        assertEquals(1, counter.day());
        assertTrue(counter.naturalTick(23_999, 24_000));
        assertEquals(2, counter.day());
    }

    @Test
    void successfulSleepAddsOneWithoutRepeatingOnNextTickOrSecondWake() {
        DayCount counter = new DayCount();
        assertTrue(counter.sleep(13_000, 24_000));
        assertFalse(counter.sleep(24_000, 24_000));
        assertFalse(counter.naturalTick(24_000, 24_001));
        assertEquals(2, counter.day());
    }

    @Test
    void wakingAtTheDawnAlreadyCountedDoesNotAddAnotherDay() {
        DayCount counter = new DayCount();
        assertTrue(counter.naturalTick(23_999, 24_000));
        assertFalse(counter.sleep(24_000, 24_000));
        assertEquals(2, counter.day());
    }

    @Test
    void frozenAndFractionalTicksDoNotInventDays() {
        DayCount counter = new DayCount();
        assertFalse(counter.naturalTick(23_999, 23_999));
        assertFalse(counter.naturalTick(23_999, 23_999));
        assertTrue(counter.naturalTick(23_999, 24_000));
        assertFalse(counter.naturalTick(24_000, 24_000));
        assertEquals(2, counter.day());
    }

    @Test
    void fastNaturalClockCountsEachCrossedDawn() {
        DayCount counter = new DayCount();
        assertTrue(counter.naturalTick(23_999, 72_001));
        assertEquals(4, counter.day());
    }

    @Test
    void sleepIsOneDayEvenWhenACustomSleepMarkerSkipsSeveralCycles() {
        DayCount counter = new DayCount();
        assertTrue(counter.sleep(13_000, 96_000));
        assertEquals(2, counter.day());
    }

    @Test
    void reverseOrUnchangedClockDoesNotAdvanceCount() {
        DayCount counter = new DayCount();
        assertFalse(counter.naturalTick(24_001, 23_999));
        assertFalse(counter.sleep(24_001, 23_999));
        assertEquals(1, counter.day());
    }

    @Test
    void restoredDayResumesWithoutReconstructingHistory() {
        DayCount counter = new DayCount(37);
        assertFalse(counter.naturalTick(9_600_000, 9_600_001));
        assertEquals(37, counter.day());
        assertTrue(counter.naturalTick(9_623_999, 9_624_000));
        assertEquals(38, counter.day());
    }

    @Test
    void invalidSavedValueIsNormalizedToFirstDay() {
        assertEquals(1, new DayCount(0).day());
        assertEquals(1, new DayCount(-20).day());
    }

    @Test
    void countSaturatesInsteadOfOverflowingToNegative() {
        DayCount counter = new DayCount(Long.MAX_VALUE - 1);
        assertTrue(counter.naturalTick(23_999, 72_001));
        assertEquals(Long.MAX_VALUE, counter.day());
        assertFalse(counter.sleep(13_000, 24_000));
        assertEquals(Long.MAX_VALUE, counter.day());
    }
}
