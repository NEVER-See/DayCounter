package dev.daycounter.server;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Stored in the overworld's saved data and flushed by Minecraft's normal saves. */
public final class DayCounterData extends SavedData {
    public static final Codec<DayCounterData> CODEC = Codec.LONG.fieldOf("day")
            .xmap(DayCounterData::new, DayCounterData::day).codec();
    public static final SavedDataType<DayCounterData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("daycounter", "days"),
            () -> new DayCounterData(1), CODEC, null);

    private final DayCount count;

    public DayCounterData(long day) {
        count = new DayCount(day);
    }

    public static DayCounterData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public long day() {
        return count.day();
    }

    public boolean naturalTick(long before, long after) {
        return markChanged(count.naturalTick(before, after));
    }

    public boolean sleep(long before, long after) {
        return markChanged(count.sleep(before, after));
    }

    private boolean markChanged(boolean changed) {
        if (changed) {
            setDirty();
        }
        return changed;
    }
}
