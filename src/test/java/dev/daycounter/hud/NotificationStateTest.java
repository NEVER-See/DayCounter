package dev.daycounter.hud;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationStateTest {
    @Test void initialSyncIsSilentButAvailableForPersistentMode() {
        var state = new NotificationState();
        assertFalse(state.accept(27, false, 1000));
        assertEquals(27, state.day());
        assertEquals(0, state.opacity(true, 8, 1100));
        assertEquals(1, state.opacity(false, 8, 1100));
    }
    @Test void realNewDayFadesInHoldsThenFadesOut() {
        var state = new NotificationState();
        state.accept(1, false, 0);
        assertTrue(state.accept(2, true, 1_000_000_000L));
        assertEquals(0, state.opacity(true, 8, 1_000_000_000L));
        assertTrue(state.opacity(true, 8, 1_125_000_000L) > 0);
        assertEquals(1, state.opacity(true, 8, 2_000_000_000L));
        assertEquals(0, state.opacity(true, 8, 9_000_000_000L));
    }
    @Test void duplicatePacketsDoNotRestartSoundOrAnimation() {
        var state = new NotificationState();
        assertTrue(state.accept(3, true, 1_000_000_000L));
        assertFalse(state.accept(3, true, 8_000_000_000L));
        assertEquals(0, state.opacity(true, 8, 9_000_000_000L));
    }
    @Test void permanentModeDoesNotExpireAndDisconnectClearsIt() {
        var state = new NotificationState();
        state.accept(42, true, 1);
        assertEquals(1, state.opacity(false, 5, Long.MAX_VALUE));
        state.reset();
        assertEquals(0, state.opacity(false, 8, 0));
        assertEquals(0, state.day());
    }
    @Test void invalidDayIsNotShown() {
        var state = new NotificationState();
        assertFalse(state.accept(-1, true, 0));
        assertEquals(0, state.opacity(false, 8, 0));
    }
}
