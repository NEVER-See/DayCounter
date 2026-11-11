package dev.daycounter;

import dev.daycounter.config.ConfigStore;
import dev.daycounter.hud.DayCounterHud;
import dev.daycounter.hud.NotificationState;
import dev.daycounter.hud.ToastRenderer;
import dev.daycounter.ui.SunIcon;
import dev.daycounter.i18n.Translations;
import dev.daycounter.network.DaySyncPayload;
import dev.daycounter.ui.DayCounterSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public final class DayCounterClient implements ClientModInitializer {
    public static final NotificationState NOTIFICATION = new NotificationState();

    @Override public void onInitializeClient() {
        ConfigStore.load();
        Translations.initialize();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            SunIcon.preload();
            ToastRenderer.initializePlaque(client);
        });
        ClientPlayConnectionEvents.INIT.register((handler, client) -> NOTIFICATION.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> NOTIFICATION.reset());
        ClientPlayNetworking.registerGlobalReceiver(DaySyncPayload.TYPE, (payload, context) -> {
            // Fabric invokes play payload handlers on the render/client thread.
            var config = ConfigStore.get();
            long now = System.nanoTime();
            NOTIFICATION.accept(payload.day(), payload.announce(), now, config);
            if (config.style == dev.daycounter.config.DayCounterConfig.Style.PLAQUE) {
                ToastRenderer.preparePlaque(NOTIFICATION.day(), NOTIFICATION.previousDay(),
                        NOTIFICATION.isDayChanging(config, now));
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            var config = ConfigStore.get();
            if (NOTIFICATION.pollSound(config, System.nanoTime())) {
                DayCounterSounds.play(config.soundId);
            }
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("daycounter", "notification"), DayCounterHud::extract);
    }
}
