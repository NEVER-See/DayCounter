package dev.daycounter.hud;

import dev.daycounter.config.DayCounterConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationStateTest {
    private static final long SECOND = 1_000_000_000L;

    @Test void delayedToastAndSoundUseTheSameStartAndPreserveTheFullDuration() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        config.delaySeconds = 1.25;
        long started = 2 * SECOND;
        long shown = started + 1_250_000_000L;
        assertTrue(state.accept(1, true, started));
        assertEquals(-1.25, state.elapsedSeconds(config, started));
        assertEquals(0, state.opacity(config, shown - 1));
        assertFalse(state.pollSound(config, shown - 1));
        assertEquals(0, state.elapsedSeconds(config, shown));
        assertTrue(state.pollSound(config, shown));
        assertFalse(state.pollSound(config, shown + 1));
        assertEquals(1, state.opacity(config, shown + SECOND));
        assertTrue(state.opacity(config, shown + 8 * SECOND - 100_000_000L) > 0);
        assertEquals(0, state.opacity(config, shown + 8 * SECOND));
    }

    @Test void iconStartsAfterTheInternalDelayAndUsesTheStaticTextureInPersistentMode() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        state.accept(1, true, 0);
        assertEquals(0, state.iconFrame(config, SECOND + 399_999_999L));
        assertEquals(0, state.iconFrame(config, SECOND + 529_999_999L));
        assertEquals(1, state.iconFrame(config, SECOND + 530_000_000L));
        assertEquals(19, state.iconFrame(config, SECOND + 2_900_000_000L));
        config.animationEnabled = false;
        assertEquals(-1, state.iconFrame(config, 4 * SECOND));
    }

    @Test void revisionsTrackChangedPacketsAndResetButNeverDuplicates() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        long emptyRevision = state.revision();
        assertFalse(state.accept(7, false, 0));
        long syncedRevision = state.revision();
        assertNotEquals(emptyRevision, syncedRevision);
        assertEquals(0, state.previousDay());
        assertEquals(0, state.opacity(config, SECOND));
        assertFalse(state.pollSound(config, SECOND));
        assertFalse(state.accept(7, true, SECOND));
        assertEquals(syncedRevision, state.revision());
        assertFalse(state.pollSound(config, 2 * SECOND));
        assertTrue(state.accept(8, true, 3 * SECOND));
        assertEquals(7, state.previousDay());
        long announcedRevision = state.revision();
        state.reset();
        assertNotEquals(announcedRevision, state.revision());
        assertEquals(0, state.previousDay());
        assertFalse(state.pollSound(config, 4 * SECOND));
    }

    @Test void acceptWithConfigCapturesTimingBeforeTheFirstTick() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        state.accept(1, true, 0, config);
        config.delaySeconds = 20;
        config.durationSeconds = 50;

        assertFalse(state.pollSound(config, SECOND - 1));
        assertTrue(state.pollSound(config, SECOND));
        assertEquals(0, state.elapsedSeconds(config, SECOND));
        assertEquals(8, state.visibleDurationSeconds(config));
        assertEquals(1, state.opacity(config, 2 * SECOND));
        assertEquals(0, state.opacity(config, 9 * SECOND));
    }

    @Test void turningAnimationOnDoesNotReplayAnEventAcceptedInPersistentMode() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        config.animationEnabled = false;
        state.accept(1, true, 0, config);
        assertTrue(state.pollSound(config, 0));

        config.animationEnabled = true;
        assertEquals(0, state.opacity(config, 2 * SECOND));
        assertFalse(state.pollSound(config, 2 * SECOND));
    }

    @Test void legacyAcceptCapturesTheWindowAtFirstConfigAwareCall() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        state.accept(1, true, 0);
        assertFalse(state.pollSound(config, SECOND / 2));

        config.durationSeconds = 50;
        assertEquals(0, state.opacity(config, 10 * SECOND));
        assertFalse(state.pollSound(config, 10 * SECOND));
    }

    @Test void switchingToPersistentAfterTheDeadlineCannotPlayTheExpiredSound() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        state.accept(1, true, 0);
        assertFalse(state.pollSound(config, SECOND / 2));

        config.animationEnabled = false;
        assertEquals(1, state.opacity(config, 10 * SECOND));
        assertFalse(state.pollSound(config, 10 * SECOND));
    }

    @Test void switchingToPersistentWhileWaitingKeepsTheOriginalSoundSchedule() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        config.delaySeconds = 20;
        state.accept(1, true, 0);
        assertFalse(state.pollSound(config, SECOND));

        config.animationEnabled = false;
        assertEquals(1, state.opacity(config, SECOND));
        assertFalse(state.pollSound(config, SECOND));
        assertTrue(state.pollSound(config, 20 * SECOND));
        assertFalse(state.pollSound(config, 20 * SECOND + 1));
    }

    @Test void newAnnouncementReplacesThePreviousPendingSound() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        state.accept(1, true, 0);
        assertFalse(state.pollSound(config, SECOND / 2));
        state.accept(2, true, SECOND / 2);
        assertFalse(state.pollSound(config, SECOND));
        assertTrue(state.pollSound(config, SECOND + SECOND / 2));
        assertFalse(state.pollSound(config, 2 * SECOND));
    }

    @Test void persistentNumberChangesUseEachStylesTimingEvenWhenMotionIsDisabled() {
        var state = new NotificationState();
        var config = new DayCounterConfig();
        config.animationEnabled = false;
        config.motionEnabled = false;
        state.accept(1, true, 0);
        assertFalse(state.isDayChanging(config, 0));
        state.accept(2, true, SECOND);
        assertEquals(.5, state.dayChangeAgeSeconds(SECOND + 500_000_000L));

        var styles = DayCounterConfig.Style.values();
        long[] transitionNanos = {700_000_000L, 850_000_000L, 2_200_000_000L, 2_000_000_000L};
        for (int i = 0; i < styles.length; i++) {
            config.style = styles[i];
            long end = SECOND + transitionNanos[i];
            assertTrue(state.isDayChanging(config, end - 1), styles[i].name());
            assertFalse(state.isDayChanging(config, end), styles[i].name());
        }
        state.accept(3, false, 3 * SECOND);
        assertFalse(state.isDayChanging(config, 3 * SECOND));
        assertFalse(state.pollSound(config, 3 * SECOND));
    }

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
    @Test void iconFramesUseAlternatingGifDurationsAndHoldAtTheEnd() {
        var state = new NotificationState();
        long started = 1_000_000_000L;
        state.accept(2, true, started);

        assertEquals(0, state.iconFrame(started - 1));
        assertEquals(0, state.iconFrame(started));
        assertEquals(0, state.iconFrame(started + 129_999_999L));
        assertEquals(1, state.iconFrame(started + 130_000_000L));
        assertEquals(1, state.iconFrame(started + 249_999_999L));
        assertEquals(2, state.iconFrame(started + 250_000_000L));
        assertEquals(18, state.iconFrame(started + 2_379_999_999L));
        assertEquals(19, state.iconFrame(started + 2_380_000_000L));
        assertEquals(19, state.iconFrame(started + 2_499_999_999L));
        assertEquals(19, state.iconFrame(started + 2_500_000_000L));
        assertEquals(19, state.iconFrame(started + 7_000_000_000L));
    }

    @Test void distinctDayRestartsIconAnimation() {
        var state = new NotificationState();
        state.accept(2, true, 1_000_000_000L);
        assertEquals(19, state.iconFrame(8_000_000_000L));

        assertTrue(state.accept(3, true, 8_000_000_000L));
        assertEquals(0, state.iconFrame(8_000_000_000L));
        assertEquals(1, state.iconFrame(8_130_000_000L));
    }

    @Test void duplicatePacketsDoNotRestartSoundOrAnimation() {
        var state = new NotificationState();
        assertTrue(state.accept(3, true, 1_000_000_000L));
        assertFalse(state.accept(3, true, 8_000_000_000L));
        assertEquals(19, state.iconFrame(8_000_000_000L));
        assertEquals(0, state.opacity(true, 8, 9_000_000_000L));
    }
    @Test void permanentModeDoesNotExpireAndDisconnectClearsIt() {
        var state = new NotificationState();
        state.accept(42, true, 1);
        assertEquals(1, state.opacity(false, 5, Long.MAX_VALUE));
        state.reset();
        assertEquals(0, state.opacity(false, 8, 0));
        assertEquals(0, state.day());
        assertEquals(-1, state.iconFrame(0));
    }
    @Test void invalidDayIsNotShown() {
        var state = new NotificationState();
        assertEquals(-1, state.iconFrame(0));
        assertFalse(state.accept(-1, true, 0));
        assertEquals(-1, state.iconFrame(0));
        assertEquals(0, state.opacity(false, 8, 0));
    }
}
