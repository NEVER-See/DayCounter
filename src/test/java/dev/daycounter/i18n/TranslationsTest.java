package dev.daycounter.i18n;

import static org.junit.jupiter.api.Assertions.*;
import static dev.daycounter.config.DayCounterConfig.Language.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daycounter.config.DayCounterConfig;
import dev.daycounter.config.SoundCatalog;
import org.junit.jupiter.api.Test;

class TranslationsTest {
    @Test void defaultHudRemainsEnglishInRussianSystemLocale() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ru-RU"));
            assertEquals("DAY 125", Translations.text(new DayCounterConfig().language, "daycounter.hud.day", 125));
            assertEquals("ДЕНЬ 125", Translations.text(RU, "daycounter.hud.day", 125));
        } finally { Locale.setDefault(before); }
    }

    @Test void soundNamesAndFormattedStatusUseSelectedLanguage() {
        String soundKey = SoundCatalog.find(SoundCatalog.DEFAULT_ID).key();
        assertEquals("Selected: Chime", Translations.text(EN, "daycounter.sound.current", Translations.text(EN, soundKey)));
        assertEquals("Выбран: Перезвон", Translations.text(RU, "daycounter.sound.current", Translations.text(RU, soundKey)));
        assertEquals("Notification size: 145%", Translations.text(EN, "daycounter.scale.value", 145));
        assertEquals("Размер блока: 145%", Translations.text(RU, "daycounter.scale.value", 145));
    }

    @Test void resourcesHaveMatchingNonemptyKeysAndEverySoundIsTranslated() throws Exception {
        JsonObject english = resource("en_us"), russian = resource("ru_ru");
        assertEquals(english.keySet(), russian.keySet());
        for (String key : english.keySet()) {
            assertFalse(Translations.text(EN, key).isBlank(), key);
            assertFalse(Translations.text(RU, key).isBlank(), key);
            assertNotEquals(key, Translations.text(EN, key));
            assertNotEquals(key, Translations.text(RU, key));
        }
        for (var sound : SoundCatalog.CHOICES) {
            assertNotEquals(sound.key(), Translations.text(EN, sound.key()));
            assertNotEquals(sound.key(), Translations.text(RU, sound.key()));
        }
    }

    @Test void independentAccessibilityTextCoversLanguageAndToggle() {
        assertEquals("Language: English. Switch to Russian", Translations.text(EN, "daycounter.language.switch"));
        assertEquals("Язык: русский. Переключить на английский", Translations.text(RU, "daycounter.language.switch"));
        assertEquals("Enable animation: On", Translations.text(EN, "daycounter.animation.state", Translations.text(EN, "daycounter.on")));
        assertEquals("Включение анимации: Выкл", Translations.text(RU, "daycounter.animation.state", Translations.text(RU, "daycounter.off")));
    }

    @Test void appearanceSettingsAreAvailableInBothLanguages() throws Exception {
        JsonObject english = resource("en_us"), russian = resource("ru_ru");
        for (var style : DayCounterConfig.Style.values()) {
            String key = "daycounter.style." + style.name().toLowerCase(Locale.ROOT);
            assertTrue(english.has(key), key);
            assertTrue(russian.has(key), key);
        }
        assertEquals("Motion: On", Translations.text(EN, "daycounter.motion.state",
                Translations.text(EN, "daycounter.on")));
        assertEquals("Подвижность: Выкл", Translations.text(RU, "daycounter.motion.state",
                Translations.text(RU, "daycounter.off")));
    }

    private static JsonObject resource(String language) throws Exception {
        try (var input = TranslationsTest.class.getClassLoader().getResourceAsStream("assets/daycounter/lang/" + language + ".json")) {
            assertNotNull(input);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
