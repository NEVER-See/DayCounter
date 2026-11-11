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
        var defaults = ConfigFile.read(file);
        assertEquals(new DayCounterConfig(), defaults);
        assertEquals(1.5, defaults.scale);
        assertTrue(Files.isRegularFile(file));
        var json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertEquals(new JsonPrimitive("EN"), json.get("language"));
        assertEquals(1.5, json.get("scale").getAsDouble());

        String saved = "{\"scale\":1.0,\"futureOption\":true}";
        Files.writeString(file, saved);
        assertEquals(1.0, ConfigFile.read(file).scale);
        assertEquals(saved, Files.readString(file));
    }

    @Test void appearanceDefaultsMigrateWithoutRewritingExistingFile() throws Exception {
        Path file = directory.resolve("daycounter.json");
        String oldJson = "{\"language\":\"RU\",\"scale\":1.25,\"durationSeconds\":14,\"animationEnabled\":false}";
        Files.writeString(file, oldJson);
        var config = ConfigFile.read(file);
        assertEquals(DayCounterConfig.Style.STANDARD, config.style);
        assertTrue(config.motionEnabled);
        assertEquals(1, config.delaySeconds);
        assertEquals(DayCounterConfig.Language.RU, config.language);
        assertEquals(1.25, config.scale);
        assertEquals(14, config.durationSeconds);
        assertFalse(config.animationEnabled);
        assertEquals(oldJson, Files.readString(file));
    }

    @Test void malformedAppearanceFieldsUseDefaultsAndPreserveValidSettings() throws Exception {
        Path file = directory.resolve("daycounter.json");
        Files.writeString(file, """
                {"style":"unknown","motionEnabled":"false","delaySeconds":1e999,"scale":3.75}
                """);
        var config = ConfigFile.read(file);
        assertEquals(DayCounterConfig.Style.STANDARD, config.style);
        assertTrue(config.motionEnabled);
        assertEquals(1, config.delaySeconds);
        assertEquals(3.75, config.scale);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"scale\":null}", "{\"scale\":\"bad\"}", "{\"scale\":1e999}"})
    void missingOrInvalidScaleUses150Percent(String json) throws Exception {
        Path file = directory.resolve("daycounter.json");
        Files.writeString(file, json);
        assertEquals(1.5, ConfigFile.read(file).scale);
        assertEquals(json, Files.readString(file));
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
        c.style = DayCounterConfig.Style.SCROLL;
        c.motionEnabled = false;
        c.delaySeconds = 19.75;
        c.scale = 4;
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
