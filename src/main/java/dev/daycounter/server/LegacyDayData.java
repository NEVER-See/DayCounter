package dev.daycounter.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Import the pre-26.1 saved-data file once, without modifying the original. */
final class LegacyDayData {
    private static final Logger LOGGER = LoggerFactory.getLogger("daycounter");
    private static final long MAX_LEGACY_NBT_BYTES = 65_536;

    private LegacyDayData() {
    }

    static DayCounterData getOrCreate(MinecraftServer server) {
        return getOrCreate(server.overworld().getDataStorage(), server.getWorldPath(LevelResource.ROOT));
    }

    static DayCounterData getOrCreate(SavedDataStorage storage, Path worldRoot) {
        DayCounterData existing = storage.get(DayCounterData.TYPE);
        if (existing != null) {
            return existing;
        }
        DayCounterData data = readLegacy(worldRoot);
        // set also fills Minecraft's cache, so ordinary ticks never probe the file again.
        storage.set(DayCounterData.TYPE, data);
        data.setDirty();
        return data;
    }

    private static DayCounterData readLegacy(Path worldRoot) {
        Path legacyFile = worldRoot.resolve("data").resolve("daycounter_days.dat");
        try {
            if (!Files.isRegularFile(legacyFile)) {
                return new DayCounterData(1);
            }
            CompoundTag root = NbtIo.readCompressed(legacyFile, NbtAccounter.create(MAX_LEGACY_NBT_BYTES));
            long day = root.getCompoundOrEmpty("data").getLongOr("day", 0);
            if (day < 1) {
                throw new IOException("Legacy day must be a positive number");
            }
            return new DayCounterData(day);
        } catch (IOException | RuntimeException error) {
            LOGGER.error("Could not import legacy DayCounter data from {}; starting at day 1", legacyFile, error);
            return new DayCounterData(1);
        }
    }
}
