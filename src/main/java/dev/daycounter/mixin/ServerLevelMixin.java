package dev.daycounter.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.daycounter.server.DayCounterServer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/clock/ServerClockManager;moveToTimeMarker(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/clock/ServerClockManager$MoveResult;"))
    private ServerClockManager.MoveResult daycounter$afterSleep(
            ServerClockManager clocks, Holder<WorldClock> clock, ResourceKey<ClockTimeMarker> marker,
            Operation<ServerClockManager.MoveResult> original) {
        long before = clocks.getInstance(clock).totalTicks();
        ServerClockManager.MoveResult result = original.call(clocks, clock, marker);
        if (result == ServerClockManager.MoveResult.MOVED
                && clock.is(WorldClocks.OVERWORLD) && marker.equals(ClockTimeMarkers.WAKE_UP_FROM_SLEEP)) {
            DayCounterServer.sleep(((ServerLevel) (Object) this).getServer(), before,
                    clocks.getInstance(clock).totalTicks());
        }
        return result;
    }
}
