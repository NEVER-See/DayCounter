package dev.daycounter.config;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigFileTest {
    @TempDir Path directory;

    @Test void missingFileCreatesDiscoverableDefaultsOnlyOnce() throws Exception {
        Path file = directory.resolve("config/daycounter.json");
        assertEquals(new DayCounterConfig(), ConfigFile.read(file));
        assertTrue(Files.isRegularFile(file));
        assertEquals(new JsonPrimitive("EN"), JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("language"));
        Files.writeString(file, "{\"scale\":1.4,\"futureOption\":true}");
        assertEquals(1.4, ConfigFile.read(file).scale);
        assertEquals("{\"scale\":1.4,\"futureOption\":true}", Files.readString(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"language\":null}", "{\"language\":\"unknown\"}", "{\"language\":false}", "{\"language\":{}}"})
    void oldOrInvalidLanguageIsSavedAsEnglish(String json) throws Exception {
        Path file = directory.resolve("daycounter.json");
        Files.writeString(file, json);
        ConfigFile.write(file, ConfigFile.read(file));
        assertEquals(new JsonPrimitive("EN"), JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("language"));
    }

    @Test void russianSelectionSurvivesReloadAndDoesNotResetOtherOptions() throws Exception {
        Path file = directory.resolve("daycounter.json");
        Files.writeString(file, "{\"language\":\"RU\",\"scale\":1.45,\"animationEnabled\":false}");
        var config = ConfigFile.read(file);
        assertEquals(1.45, config.scale);
        assertFalse(config.animationEnabled);
        ConfigFile.write(file, config);
        assertEquals(new JsonPrimitive("RU"), JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("language"));
    }

    @Test void unavailableConfigDirectoryKeepsDefaultsRunning() throws Exception {
        Path blocker = directory.resolve("file-not-directory");
        Files.writeString(blocker, "untouched");
        assertEquals(new DayCounterConfig(), ConfigFile.read(blocker.resolve("daycounter.json")));
        assertEquals("untouched", Files.readString(blocker));
    }

    @Test void malformedFileIsPreservedForRecovery() throws Exception {
        Path file = directory.resolve("daycounter.json");
        Files.writeString(file, "{ broken");
        assertEquals(new DayCounterConfig(), ConfigFile.read(file));
        assertEquals("{ broken", Files.readString(file));
    }

    @Test void validFieldsSurviveCorruptFieldsAndUnknownOptions() throws Exception {
        Path file = directory.resolve("daycounter.json");
        Files.writeString(file, """
                {"durationSeconds":"bad", "animationEnabled":false, "scale":1.4,
                 "xPercent":-50, "yPercent":500, "position":"manual", "soundId":null,
                 "futureOption":{"nested":true}}
                """);
        var config = ConfigFile.read(file);
        assertEquals(8, config.durationSeconds);
        assertFalse(config.animationEnabled);
        assertEquals(1.4, config.scale);
        assertEquals(0, config.xPercent);
        assertEquals(100, config.yPercent);
        assertEquals(DayCounterConfig.Position.MANUAL, config.position);
        assertEquals("minecraft:block.note_block.chime", config.soundId);
    }

    @Test void savedConfigRoundTripsAndLeavesNoTemporaryFiles() throws Exception {
        var c = new DayCounterConfig();
        c.durationSeconds = 12.75;
        c.position = DayCounterConfig.Position.HUD;
        c.soundId = "minecraft:ui.toast.in";
        Path file = directory.resolve("nested/daycounter.json");
        ConfigFile.write(file, c);
        assertEquals(c, ConfigFile.read(file));
        c.scale = .75;
        ConfigFile.write(file, c);
        assertEquals(c, ConfigFile.read(file));
        try (var files = Files.list(file.getParent())) { assertEquals(1, files.count()); }
    }

    @Test void failedWriteIsReportedRatherThanPretendingItSaved() throws Exception {
        Path blocker = directory.resolve("file-not-directory");
        Files.writeString(blocker, "untouched");
        assertThrows(java.io.IOException.class, () -> ConfigFile.write(blocker.resolve("daycounter.json"), new DayCounterConfig()));
        assertEquals("untouched", Files.readString(blocker));
    }
}
