package dev.daycounter.hud;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ToastLayoutTest {
    @Test void fractionalFitDoesNotCrashAtSmallWindowBoundary() {
        for (int[] input : new int[][]{{100, 108}, {321, 235}}) {
            var box = assertDoesNotThrow(() -> ToastLayout.calculate(input[0], 100, input[1], 2,
                    ToastLayout.Anchor.TOP, 50, 24));
            assertTrue(box.x() >= 8);
            assertTrue(box.x() + box.width() <= input[0] - 8 + .000001);
        }
    }
    @Test void topIsCenteredWithGenerousMargin() {
        var box = ToastLayout.calculate(640, 360, 45, 1, ToastLayout.Anchor.TOP, 50, 24);
        assertEquals(320, box.x() + box.width() / 2, .01);
        assertEquals(86.4, box.y(), .01);
    }
    @Test void hudPresetLeavesHotbarAndHealthVisible() {
        var box = ToastLayout.calculate(640, 360, 45, 1, ToastLayout.Anchor.HUD, 50, 24);
        assertEquals(308, box.y() + box.height(), .01);
    }
    @Test void manualEdgesCannotPlaceBlockOutsideWindow() {
        for (double percent : new double[]{0, 100}) {
            var box = ToastLayout.calculate(320, 180, 300, 2, ToastLayout.Anchor.MANUAL, percent, percent);
            assertTrue(box.x() >= 8 && box.y() >= 8);
            assertTrue(box.x() + box.width() <= 312.001);
            assertTrue(box.y() + box.height() <= 172.001);
        }
    }
    @Test void longDayNumberAutomaticallyScalesToSmallWindow() {
        var box = ToastLayout.calculate(160, 100, 180, 2, ToastLayout.Anchor.TOP, 50, 24);
        assertTrue(box.scale() < 1);
        assertTrue(box.width() <= 144);
        assertEquals(80, box.x() + box.width() / 2, .01);
    }
}
