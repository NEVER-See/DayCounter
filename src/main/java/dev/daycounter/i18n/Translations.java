package dev.daycounter.i18n;

import dev.daycounter.config.DayCounterConfig.Language;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.gson.JsonParser;

/** Bundled translations selected independently of the Minecraft language. */
public final class Translations {
    private static final Map<Language, Map<String, String>> LANGUAGES = Map.of(
            Language.EN, load("en_us"), Language.RU, load("ru_ru"));

    private Translations() { }

    /** Call during client initialization so resource reads finish before rendering starts. */
    public static void initialize() { }

    public static String text(Language language, String key, Object... arguments) {
        var selected = LANGUAGES.get(language == null ? Language.EN : language);
        String pattern = selected.getOrDefault(key, LANGUAGES.get(Language.EN).getOrDefault(key, key));
        return arguments.length == 0 ? pattern : String.format(Locale.ROOT, pattern, arguments);
    }

    private static Map<String, String> load(String language) {
        String path = "assets/daycounter/lang/" + language + ".json";
        try (var stream = Translations.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new IOException("Missing translation resource: " + path);
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            var entries = new HashMap<String, String>();
            json.entrySet().forEach(entry -> entries.put(entry.getKey(), entry.getValue().getAsString()));
            return Map.copyOf(entries);
        } catch (IOException | RuntimeException error) {
            System.getLogger("DayCounter").log(System.Logger.Level.WARNING, "Could not load DayCounter translations: " + path, error);
            return Map.of();
        }
    }
}
