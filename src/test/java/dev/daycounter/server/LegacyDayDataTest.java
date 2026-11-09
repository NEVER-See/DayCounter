package dev.daycounter.server;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LegacyDayDataTest {
    private static final DataFixer IDENTITY_FIXER = new DataFixer() {
        @Override
        public <T> Dynamic<T> update(DSL.TypeReference type, Dynamic<T> input, int oldVersion, int newVersion) {
            return input;
        }

        @Override
        public Schema getSchema(int versionKey) {
            throw new UnsupportedOperationException("No schemas needed for this saved-data round trip");
        }
    };

    @BeforeAll
    static void initializeVersion() {
        SharedConstants.tryDetectVersion();
    }

    @Test
    void importsLegacyCountAndPersistsItWithoutChangingTheOriginal(@TempDir Path root) throws IOException {
        Path original = writeLegacy(root, Long.MAX_VALUE - 1);
        byte[] originalBytes = Files.readAllBytes(original);
        try (SavedDataStorage storage = storage(root)) {
            DayCounterData data = LegacyDayData.getOrCreate(storage, root);
            assertEquals(Long.MAX_VALUE - 1, data.day());
            assertTrue(data.isDirty());
            storage.saveAndJoin();
        }
        try (SavedDataStorage reopened = storage(root)) {
            DayCounterData data = LegacyDayData.getOrCreate(reopened, root);
            assertEquals(Long.MAX_VALUE - 1, data.day());
            assertTrue(data.sleep(13_000, 24_000));
            assertEquals(Long.MAX_VALUE, data.day());
        }
        assertArrayEquals(originalBytes, Files.readAllBytes(original));
    }

    @Test
    void currentSavedDataTakesPriorityOverTheLegacyFile(@TempDir Path root) throws IOException {
        try (SavedDataStorage storage = storage(root)) {
            DayCounterData current = new DayCounterData(7);
            current.setDirty();
            storage.set(DayCounterData.TYPE, current);
        }
        writeLegacy(root, 99);
        try (SavedDataStorage reopened = storage(root)) {
            assertEquals(7, LegacyDayData.getOrCreate(reopened, root).day());
        }
    }

    @Test
    void absentLegacyFileStartsAtOneAndIsNotCheckedAgain(@TempDir Path root) throws IOException {
        try (SavedDataStorage storage = storage(root)) {
            DayCounterData first = LegacyDayData.getOrCreate(storage, root);
            assertEquals(1, first.day());
            writeLegacy(root, 99);
            assertSame(first, LegacyDayData.getOrCreate(storage, root));
            assertEquals(1, first.day());
        }
    }

    @Test
    void invalidLegacyCountUsesFirstDayAndKeepsTheOriginal(@TempDir Path root) throws IOException {
        Path original = writeLegacy(root, -7);
        byte[] originalBytes = Files.readAllBytes(original);
        try (SavedDataStorage storage = storage(root)) {
            assertEquals(1, LegacyDayData.getOrCreate(storage, root).day());
        }
        assertArrayEquals(originalBytes, Files.readAllBytes(original));
    }

    @Test
    void malformedLegacyFileUsesFirstDay(@TempDir Path root) throws IOException {
        Path original = root.resolve("data/daycounter_days.dat");
        Files.createDirectories(original.getParent());
        Files.write(original, new byte[] {1, 2, 3});
        try (SavedDataStorage storage = storage(root)) {
            assertEquals(1, LegacyDayData.getOrCreate(storage, root).day());
        }
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(original));
    }

    @Test
    void oversizedLegacyNbtIsRejectedBeforeItsCountIsImported(@TempDir Path root) throws IOException {
        CompoundTag data = new CompoundTag();
        data.putLong("day", 37);
        data.putByteArray("oversized", new byte[70_000]);
        CompoundTag file = new CompoundTag();
        file.put("data", data);
        Path original = root.resolve("data/daycounter_days.dat");
        Files.createDirectories(original.getParent());
        NbtIo.writeCompressed(file, original);
        try (SavedDataStorage storage = storage(root)) {
            assertEquals(1, LegacyDayData.getOrCreate(storage, root).day());
        }
        assertTrue(Files.exists(original));
    }

    private static SavedDataStorage storage(Path root) {
        // Fabric's object-builder mixin accepts null data-fix types. Plain JUnit
        // has no mixins; our identity fixer supplies the same unchanged NBT.
        return new SavedDataStorage(root.resolve("modern-data"), IDENTITY_FIXER, RegistryAccess.EMPTY) {
            @Override
            public CompoundTag readTagFromDisk(Path file, net.minecraft.util.datafix.DataFixTypes type,
                                              int version) throws IOException {
                return super.readTagFromDisk(file,
                        type == null ? net.minecraft.util.datafix.DataFixTypes.LEVEL : type, version);
            }
        };
    }

    private static Path writeLegacy(Path root, long day) throws IOException {
        CompoundTag data = new CompoundTag();
        data.putLong("day", day);
        CompoundTag file = new CompoundTag();
        file.put("data", data);
        Path path = root.resolve("data/daycounter_days.dat");
        Files.createDirectories(path.getParent());
        NbtIo.writeCompressed(file, path);
        return path;
    }
}
