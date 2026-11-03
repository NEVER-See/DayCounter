package dev.daycounter.mixin;

import dev.daycounter.server.DayCounterServer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerClockManager.class)
public abstract class ServerClockManagerMixin {
    @Shadow private MinecraftServer server;

    @Shadow
    public abstract ServerClockManager.ServerClockInstance getInstance(Holder<WorldClock> clock);

    @Unique private Holder<WorldClock> daycounter$overworldClock;
    @Unique private long daycounter$beforeTick;

    @Inject(method = "init", at = @At("RETURN"))
    private void daycounter$findClock(MinecraftServer minecraftServer, CallbackInfo ci) {
        daycounter$overworldClock = minecraftServer.registryAccess()
                .lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void daycounter$beforeNaturalTick(CallbackInfo ci) {
        daycounter$beforeTick = getInstance(daycounter$overworldClock).totalTicks();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void daycounter$afterNaturalTick(CallbackInfo ci) {
        // Command mutations and sleep transitions are outside this method.
        DayCounterServer.naturalTick(server, daycounter$beforeTick,
                getInstance(daycounter$overworldClock).totalTicks());
    }
}
