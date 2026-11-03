package dev.daycounter.ui;

import dev.daycounter.config.SoundCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class DayCounterSounds {
    private static SimpleSoundInstance previous;
    private DayCounterSounds() { }

    /** Called only for a new-day announcement or an explicit sound selection. */
    public static void play(String id) {
        var choice = SoundCatalog.find(id);
        var manager = Minecraft.getInstance().getSoundManager();
        if (previous != null) manager.stop(previous);
        previous = SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(Identifier.parse(choice.id())), 1f, choice.volume());
        manager.play(previous);
    }
}
