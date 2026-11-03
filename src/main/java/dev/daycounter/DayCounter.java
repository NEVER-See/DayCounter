package dev.daycounter;

import dev.daycounter.network.DaySyncPayload;
import dev.daycounter.server.DayCounterData;
import dev.daycounter.server.DayCounterServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class DayCounter implements ModInitializer {
    public static final String MOD_ID = "daycounter";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(DaySyncPayload.TYPE, DaySyncPayload.STREAM_CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DayCounterData.get(server));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                DayCounterServer.syncPlayer(server, handler.player));
    }
}
