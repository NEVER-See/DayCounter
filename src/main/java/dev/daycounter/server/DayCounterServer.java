package dev.daycounter.server;

import dev.daycounter.network.DaySyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class DayCounterServer {
    private DayCounterServer() {
    }

    public static void naturalTick(MinecraftServer server, long before, long after) {
        // Avoid looking up saved data on every ordinary tick. The model independently
        // verifies the transition; this fast path creates no saved clock baseline.
        if (after <= before || Math.floorDiv(before, 24_000) == Math.floorDiv(after, 24_000)) {
            return;
        }
        DayCounterData data = DayCounterData.get(server);
        if (data.naturalTick(before, after)) {
            announce(server, data.day());
        }
    }

    public static void sleep(MinecraftServer server, long before, long after) {
        DayCounterData data = DayCounterData.get(server);
        if (data.sleep(before, after)) {
            announce(server, data.day());
        }
    }

    public static void syncPlayer(MinecraftServer server, ServerPlayer player) {
        send(player, new DaySyncPayload(DayCounterData.get(server).day(), false));
    }

    private static void announce(MinecraftServer server, long day) {
        DaySyncPayload payload = new DaySyncPayload(day, true);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(player, payload);
        }
    }

    private static void send(ServerPlayer player, DaySyncPayload payload) {
        // The mod is optional on clients: vanilla clients receive no custom payload.
        if (ServerPlayNetworking.canSend(player, DaySyncPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
