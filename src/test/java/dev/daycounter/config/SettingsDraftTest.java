package dev.daycounter.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SettingsDraftTest {
    @Test void languageChangeIsDraftOnlyAndRevertingRemovesDirtyState() {
        var opening = new DayCounterConfig();
        var draft = new SettingsDraft(opening);
        draft.value().language = DayCounterConfig.Language.RU;
        assertTrue(draft.canApply());
        assertEquals(DayCounterConfig.Language.EN, opening.language);
        assertEquals(DayCounterConfig.Language.RU, draft.value().normalized().language);
        draft.value().language = DayCounterConfig.Language.EN;
        assertFalse(draft.canApply());
        draft.value().language = DayCounterConfig.Language.RU;
        draft.applied();
        assertFalse(draft.canApply());
        draft.reset();
        assertTrue(draft.canApply());
        assertEquals(DayCounterConfig.Language.EN, draft.value().language);
    }

    @Test void languageChangePreservesRawInvalidFieldsAndOtherDraftChoices() {
        var draft = new SettingsDraft(new DayCounterConfig());
        draft.durationText("8,, сек");
        draft.xText("bad X");
        draft.yText("101");
        draft.value().scale = 1.45;
        draft.value().soundId = "minecraft:ui.toast.in";
        draft.value().position = DayCounterConfig.Position.MANUAL;
        draft.value().language = DayCounterConfig.Language.RU;
        assertEquals("8,, сек", draft.durationText());
        assertEquals("bad X", draft.xText());
        assertEquals("101", draft.yText());
        assertEquals(1.45, draft.value().scale);
        assertEquals("minecraft:ui.toast.in", draft.value().soundId);
        assertFalse(draft.canApply());
    }

    @ParameterizedTest
    @ValueSource(strings = {"5", "8.5", "8,5 сек", "50 s", "12.25 сек.", " 7,25 seconds "})
    void acceptsBoundedFractionalDuration(String value) {
        var draft = new SettingsDraft(new DayCounterConfig());
        draft.durationText(value);
        assertTrue(draft.valid());
        assertTrue(draft.value().durationSeconds >= 5 && draft.value().durationSeconds <= 50);
        if (value.contains("8")) assertEquals(8.5, draft.value().durationSeconds);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "4.99", "50.01", "NaN", "Infinity", "1e1", "-8", "8,5,5", "9 frogs"})
    void rejectsInvalidDurationWithoutOverwritingLastValue(String value) {
        var draft = new SettingsDraft(new DayCounterConfig());
        draft.durationText(value);
        assertFalse(draft.valid());
        assertFalse(draft.canApply());
        assertEquals(8, draft.value().durationSeconds);
    }

    @Test void ignoredFieldsCannotBlockPersistentOrPresetMode() {
        var draft = new SettingsDraft(new DayCounterConfig());
        draft.durationText("bad");
        draft.xText("wrong");
        draft.yText("101");
        draft.value().animationEnabled = false;
        assertTrue(draft.canApply());
        draft.value().position = DayCounterConfig.Position.MANUAL;
        assertFalse(draft.canApply());
        draft.xText("0");
        draft.yText("100");
        assertTrue(draft.canApply());
    }

    @Test void draftDoesNotChangeOpeningValueAndResetStillNeedsApply() {
        var opening = new DayCounterConfig();
        opening.scale = 1.25;
        var draft = new SettingsDraft(opening);
        assertFalse(draft.canApply());
        draft.durationText("9,5 сек");
        assertEquals(8, opening.durationSeconds);
        assertTrue(draft.canApply());
        draft.reset();
        assertEquals(1.5, draft.value().scale);
        assertEquals(1.25, opening.scale);
        assertTrue(draft.canApply());
        draft.applied();
        assertFalse(draft.canApply());
    }

    @Test void formattingOnlyIsNotAChangeAndBadFieldsSurviveOtherControls() {
        var draft = new SettingsDraft(new DayCounterConfig());
        draft.durationText("8,0 сек");
        assertFalse(draft.canApply());
        draft.durationText("invalid");
        draft.value().scale = 1.5;
        assertEquals("invalid", draft.durationText());
        assertFalse(draft.canApply());
    }

    @Test void normalizationHandlesCorruptedAndNonFiniteValues() {
        var c = new DayCounterConfig();
        c.durationSeconds = Double.NaN;
        c.scale = 9;
        c.xPercent = -20;
        c.yPercent = Double.POSITIVE_INFINITY;
        c.position = null;
        c.language = null;
        c.soundId = "unknown:bad";
        var n = c.normalized();
        assertEquals(8, n.durationSeconds);
        assertEquals(2, n.scale);
        assertEquals(0, n.xPercent);
        assertEquals(24, n.yPercent);
        assertEquals(DayCounterConfig.Position.TOP, n.position);
        assertEquals(DayCounterConfig.Language.EN, n.language);
        assertEquals("minecraft:block.note_block.chime", n.soundId);
        assertEquals(n, n.copy());
        assertEquals(n.hashCode(), n.copy().hashCode());
        n.copy().scale = 1;
        assertEquals(2, n.scale);
    }
}
