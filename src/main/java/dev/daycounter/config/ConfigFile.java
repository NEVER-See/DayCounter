package dev.daycounter.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

final class ConfigFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final System.Logger LOGGER = System.getLogger("DayCounter");

    static DayCounterConfig read(Path file) {
        var config = new DayCounterConfig();
        if (Files.notExists(file)) {
            try {
                Files.createDirectories(file.toAbsolutePath().getParent());
                Files.writeString(file, GSON.toJson(config) + System.lineSeparator(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } catch (FileAlreadyExistsException ignored) {
                // Another caller created the configuration; read that version below.
            } catch (IOException | RuntimeException error) {
                LOGGER.log(System.Logger.Level.WARNING, "Could not create DayCounter settings; using defaults: " + error.getMessage());
                return config;
            }
        }
        try {
            if (Files.size(file) > 65_536) throw new IOException("Configuration exceeds 64 KiB");
            var element = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) throw new IOException("Configuration is not an object");
            JsonObject json = element.getAsJsonObject();
            String language = string(json, "language", "EN");
            try { config.language = DayCounterConfig.Language.valueOf(language.toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ignored) { config.language = DayCounterConfig.Language.EN; }
            config.durationSeconds = number(json, "durationSeconds", config.durationSeconds);
            config.scale = number(json, "scale", config.scale);
            config.xPercent = number(json, "xPercent", config.xPercent);
            config.yPercent = number(json, "yPercent", config.yPercent);
            var animated = json.get("animationEnabled");
            if (animated != null && animated.isJsonPrimitive() && animated.getAsJsonPrimitive().isBoolean())
                config.animationEnabled = animated.getAsBoolean();
            String position = string(json, "position", "TOP");
            try { config.position = DayCounterConfig.Position.valueOf(position.toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ignored) { config.position = DayCounterConfig.Position.TOP; }
            config.soundId = string(json, "soundId", config.soundId);
            return config.normalized();
        } catch (IOException | RuntimeException error) {
            // Keep the original file for diagnosis. Defaults are only persisted on an explicit Apply.
            LOGGER.log(System.Logger.Level.WARNING, "Could not read DayCounter settings; using defaults: " + error.getMessage());
            return config.normalized();
        }
    }

    static void write(Path file, DayCounterConfig config) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "daycounter-", ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(config.normalized()) + System.lineSeparator(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static double number(JsonObject json, String key, double fallback) {
        var value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) return fallback;
        try { return value.getAsDouble(); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static String string(JsonObject json, String key, String fallback) {
        var value = json.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString() : fallback;
    }
}
