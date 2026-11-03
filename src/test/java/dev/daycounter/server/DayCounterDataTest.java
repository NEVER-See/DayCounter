package dev.daycounter.server;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DayCounterDataTest {
    @Test
    void savedDayRoundTripsAndContinuesFromThatDay() {
        DayCounterData loaded = DayCounterData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"day\":37}")).getOrThrow();
        assertFalse(loaded.naturalTick(9_600_000, 9_600_001));
        assertTrue(loaded.sleep(9_613_000, 9_624_000));
        assertEquals(38, loaded.day());
        assertEquals(JsonParser.parseString("{\"day\":38}"),
                DayCounterData.CODEC.encodeStart(JsonOps.INSTANCE, loaded).getOrThrow());
    }

    @Test
    void ordinaryTicksDoNotRequestDiskWritesButNewDaysDo() {
        DayCounterData data = new DayCounterData(1);
        assertFalse(data.isDirty());
        data.naturalTick(12_000, 12_001);
        assertFalse(data.isDirty());
        data.naturalTick(23_999, 24_000);
        assertTrue(data.isDirty());
        data.setDirty(false);
        data.sleep(24_000, 24_000);
        assertFalse(data.isDirty());
        data.sleep(37_000, 48_000);
        assertTrue(data.isDirty());
    }

    @Test
    void absentWorldDataStartsAtOneAndInvalidStoredCountIsNormalized() {
        assertEquals(1, DayCounterData.TYPE.constructor().get().day());
        DayCounterData loaded = DayCounterData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"day\":-7}")).getOrThrow();
        assertEquals(1, loaded.day());
    }
}
