package dev.daycounter.server;

import dev.daycounter.network.DaySyncPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DaySyncPayloadTest {
    @Test
    void initialSyncAndNewDayRetainTheirDifferentAnnouncementFlags() {
        assertEquals(new DaySyncPayload(1, false), roundTrip(new DaySyncPayload(1, false)));
        assertEquals(new DaySyncPayload(38, true), roundTrip(new DaySyncPayload(38, true)));
    }

    @Test
    void largeDayCountIsNeverTruncatedToAnInteger() {
        assertEquals(new DaySyncPayload(Long.MAX_VALUE, true),
                roundTrip(new DaySyncPayload(Long.MAX_VALUE, true)));
    }

    private static DaySyncPayload roundTrip(DaySyncPayload payload) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            DaySyncPayload.STREAM_CODEC.encode(buffer, payload);
            assertTrue(buffer.readableBytes() <= 10, "The day payload must stay small");
            DaySyncPayload decoded = DaySyncPayload.STREAM_CODEC.decode(buffer);
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
