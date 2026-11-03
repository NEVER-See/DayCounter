package dev.daycounter;

import dev.daycounter.config.ConfigStore;
import dev.daycounter.hud.DayCounterHud;
import dev.daycounter.hud.NotificationState;
import dev.daycounter.i18n.Translations;
import dev.daycounter.network.DaySyncPayload;
import dev.daycounter.ui.DayCounterSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public final class DayCounterClient implements ClientModInitializer {
    public static final NotificationState NOTIFICATION = new NotificationState();

    @Override public void onInitializeClient() {
        ConfigStore.load();
        Translations.initialize();
        ClientPlayConnectionEvents.INIT.register((handler, client) -> NOTIFICATION.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> NOTIFICATION.reset());
        ClientPlayNetworking.registerGlobalReceiver(DaySyncPayload.TYPE, (payload, context) -> {
            // Fabric invokes play payload handlers on the render/client thread.
            if (NOTIFICATION.accept(payload.day(), payload.announce(), System.nanoTime())) {
                DayCounterSounds.play(ConfigStore.get().soundId);
            }
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("daycounter", "notification"), DayCounterHud::extract);
    }
}
