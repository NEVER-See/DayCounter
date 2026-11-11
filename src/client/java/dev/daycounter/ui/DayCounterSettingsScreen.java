package dev.daycounter.ui;

import dev.daycounter.config.ConfigStore;
import dev.daycounter.config.DayCounterConfig;
import dev.daycounter.config.SettingsDraft;
import dev.daycounter.config.SoundCatalog;
import dev.daycounter.i18n.Translations;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** A fixed-proportion panel, uniformly fitted to the available GUI area. */
public final class DayCounterSettingsScreen extends Screen {
    private static final int PANEL_WIDTH = 360, PANEL_HEIGHT = 356;
    private static final int LEFT = 14, RIGHT = PANEL_WIDTH - LEFT, CONTENT_WIDTH = RIGHT - LEFT;
    private static final int COLUMN_WIDTH = 106, COLUMN_STEP = 113;
    private static final int FOOTER_Y = 326, STATUS_Y = 307;
    private static final int TIME_FIELD_X = RIGHT - 84;
    private static final int SOUNDS_PER_PAGE = 6, SOUND_ROW_Y = 105, SOUND_ROW_HEIGHT = 23;

    private enum Tab {
        GENERAL("tab.general"), APPEARANCE("tab.appearance"), SOUNDS("tab.sounds");

        private final String label;
        Tab(String label) { this.label = label; }
    }

    private final Screen parent;
    private final SettingsDraft settings = new SettingsDraft(ConfigStore.get());
    private final DayCounterButton[] positions = new DayCounterButton[DayCounterConfig.Position.values().length];
    private final StyleChoiceButton[] styles = new StyleChoiceButton[DayCounterConfig.Style.values().length];
    private Tab tab = Tab.GENERAL;
    private int soundPage;
    private float panelScale = 1, left, top;
    private SettingField delay, duration, xField, yField;
    private DayCounterButton apply;
    private boolean saveError;

    public DayCounterSettingsScreen(Screen parent) {
        super(Component.literal(Translations.text(ConfigStore.get().language, "daycounter.title")));
        this.parent = parent;
        formatTimes();
    }

    private Component text(String key, Object... args) {
        Object[] values = new Object[args.length];
        for (int i = 0; i < args.length; i++) values[i] = args[i] instanceof Component component ? component.getString() : args[i];
        return Component.literal(Translations.text(config().language, "daycounter." + key, values));
    }

    private Component soundName(SoundCatalog.Choice choice) {
        return Component.literal(Translations.text(config().language, choice.key()));
    }

    private DayCounterConfig config() { return settings.value(); }

    @Override public Component getTitle() { return text("title"); }
    @Override public Component getNarrationMessage() { return getTitle(); }

    @Override protected void init() {
        panelScale = Math.max(.01f, Math.min(1, Math.min((width - 12f) / PANEL_WIDTH, (height - 12f) / PANEL_HEIGHT)));
        left = (width - PANEL_WIDTH * panelScale) / 2;
        top = (height - PANEL_HEIGHT * panelScale) / 2;
        delay = duration = xField = yField = null;
        var language = button(RIGHT - 22, 14, 22, 22, Component.literal(config().language.name()), this::switchLanguage);
        language.labelOffsetY = 1;
        language.narration = text("language.switch");
        language.setTooltip(Tooltip.create(language.narration));
        for (Tab choice : Tab.values()) {
            var button = button(columnX(choice.ordinal()), 47, COLUMN_WIDTH, 22, text(choice.label), () -> {
                tab = choice;
                rebuildWidgets();
            });
            button.selected = tab == choice;
            button.active = !button.selected;
        }
        switch (tab) {
            case GENERAL -> general();
            case APPEARANCE -> appearance();
            case SOUNDS -> sounds();
        }
        apply = button(columnX(0), FOOTER_Y, COLUMN_WIDTH, 22, text("apply"), this::apply);
        apply.accent = true;
        button(columnX(1), FOOTER_Y, COLUMN_WIDTH, 22, text("reset"), () -> {
            settings.reset();
            formatTimes();
            saveError = false;
            soundPage = 0;
            rebuildWidgets();
        });
        button(columnX(2), FOOTER_Y, COLUMN_WIDTH, 22, text("cancel"), this::onClose);
        refresh();
    }

    private static int columnX(int index) { return LEFT + index * COLUMN_STEP; }

    private void general() {
        addRenderableWidget(new AnimationToggle(RIGHT - 38, 88, config().animationEnabled, config().language,
                "daycounter.animation.state", enabled -> {
                    config().animationEnabled = enabled;
                    changed();
                }));
        delay = field(TIME_FIELD_X, 119, 84, "delay", settings.delayText(), settings::delayText);
        duration = field(TIME_FIELD_X, 153, 84, "duration", settings.durationText(), settings::durationText);

        addRenderableWidget(new ScaleSlider(LEFT, 206, CONTENT_WIDTH, config().scale, config().language, scale -> {
            config().scale = scale;
            changed();
        }));
        String[] names = {"position.top", "position.hud", "position.manual"};
        for (var position : DayCounterConfig.Position.values()) {
            int i = position.ordinal();
            positions[i] = button(columnX(i), 255, COLUMN_WIDTH, 20, text(names[i]), () -> {
                config().position = position;
                changed();
            });
        }
        xField = field(48, 282, 126, "coordinate.x", settings.xText(), settings::xText);
        yField = field(220, 282, 126, "coordinate.y", settings.yText(), settings::yText);
    }

    private SettingField field(int x, int y, int width, String key, String value, Consumer<String> responder) {
        var field = new SettingField(font, x, y, width, text(key));
        field.setValue(value);
        field.setResponder(next -> {
            responder.accept(next);
            changed();
        });
        return addRenderableWidget(field);
    }

    private void appearance() {
        addRenderableWidget(new AnimationToggle(RIGHT - 38, 88, config().motionEnabled, config().language,
                "daycounter.motion.state", enabled -> {
                    config().motionEnabled = enabled;
                    changed();
                }));
        for (var style : DayCounterConfig.Style.values()) {
            int i = style.ordinal();
            String key = "style." + style.name().toLowerCase(Locale.ROOT);
            styles[i] = addRenderableWidget(new StyleChoiceButton(LEFT, 115 + i * 47, CONTENT_WIDTH, font,
                    config().language, style, text(key), text(key + ".description"), () -> {
                        config().style = style;
                        changed();
                    }));
        }
    }

    private void sounds() {
        int first = soundPage * SOUNDS_PER_PAGE;
        for (int i = 0; i < SOUNDS_PER_PAGE; i++) {
            var choice = SoundCatalog.CHOICES.get(first + i);
            boolean selected = config().soundId.equals(choice.id());
            var button = button(RIGHT - 84, soundRowY(i) + 3, 78, 18, text(selected ? "selected" : "select"), () -> {
                config().soundId = choice.id();
                DayCounterSounds.play(choice.id());
                saveError = false;
                rebuildWidgets();
            });
            button.narration = text(selected ? "sound.current" : "sound.select", soundName(choice));
            button.selected = selected;
            button.active = !selected;
            button.silent = true; // The selected sound is the sole audible response.
        }
        button(columnX(0), 254, COLUMN_WIDTH, 20, text("previous"), () -> {
            soundPage--;
            rebuildWidgets();
        }).active = soundPage > 0;
        button(columnX(2), 254, COLUMN_WIDTH, 20, text("next"), () -> {
            soundPage++;
            rebuildWidgets();
        }).active = soundPage + 1 < soundPageCount();
    }

    private static int soundPageCount() { return SoundCatalog.CHOICES.size() / SOUNDS_PER_PAGE; }

    private static int soundRowY(int index) { return SOUND_ROW_Y + index * SOUND_ROW_HEIGHT; }

    private DayCounterButton button(int x, int y, int w, int h, Component label, Runnable action) {
        return addRenderableWidget(new DayCounterButton(x, y, w, h, label, action));
    }

    private void changed() {
        saveError = false;
        refresh();
    }

    private void refresh() {
        if (apply != null) apply.active = settings.canApply();
        if (tab == Tab.APPEARANCE) {
            for (var style : DayCounterConfig.Style.values()) {
                var card = styles[style.ordinal()];
                card.selected = config().style == style;
                card.narration = card.selected ? text("style.selected", card.getMessage()) : card.getMessage();
            }
        }
        if (delay == null) return;
        setFieldState(delay, config().animationEnabled, settings.delayValid());
        setFieldState(duration, config().animationEnabled, settings.durationValid());
        boolean manual = config().position == DayCounterConfig.Position.MANUAL;
        setFieldState(xField, manual, settings.xValid());
        setFieldState(yField, manual, settings.yValid());
        for (int i = 0; i < positions.length; i++) {
            positions[i].selected = config().position.ordinal() == i;
            positions[i].active = !positions[i].selected;
        }
    }

    private static void setFieldState(SettingField field, boolean enabled, boolean valid) {
        field.active = enabled;
        field.setEditable(enabled);
        field.valid = valid;
    }

    private String formattedSeconds(double seconds) {
        return SettingsDraft.format(seconds) + " " + text("seconds").getString();
    }

    private void formatTimes() {
        settings.delayText(formattedSeconds(config().delaySeconds));
        settings.durationText(formattedSeconds(config().durationSeconds));

    }

    private void switchLanguage() {
        boolean automaticDelay = settings.delayText().equals(formattedSeconds(config().delaySeconds));
        boolean automaticDuration = settings.durationText().equals(formattedSeconds(config().durationSeconds));
        config().language = config().language == DayCounterConfig.Language.EN
                ? DayCounterConfig.Language.RU : DayCounterConfig.Language.EN;
        if (automaticDelay) settings.delayText(formattedSeconds(config().delaySeconds));
        if (automaticDuration) settings.durationText(formattedSeconds(config().durationSeconds));
        saveError = false;
        rebuildWidgets();
    }

    private void apply() {
        if (!settings.canApply()) return;
        if (ConfigStore.save(config())) {
            settings.applied();
            saveError = false;
            formatTimes();
            rebuildWidgets();
        } else saveError = true;
    }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // A simple tint keeps the page light and avoids the vanilla full-screen blur.
        g.fill(0, 0, width, height, 0xb014161a);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.pose().pushMatrix();
        g.pose().translate(left, top);
        g.pose().scale(panelScale, panelScale);
        UiDraw.rounded(g, 1, 3, PANEL_WIDTH, PANEL_HEIGHT, 9, 0x60000000);
        UiDraw.panel(g, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 9, 0xff4c4d50, 0xff25272b);
        SunIcon.draw(g, LEFT, 9, 32, SunIcon.STATIC_FRAME, 0xffffffff);
        g.pose().pushMatrix();
        g.pose().translate(56, 12);
        g.pose().scale(1.35f, 1.35f);
        g.text(font, "DayCounter", 0, 0, UiDraw.TEXT);
        g.pose().popMatrix();
        label(g, text("subtitle"), 56, 28, RIGHT - 88, UiDraw.MUTED);
        g.fill(1, 78, PANEL_WIDTH - 1, 79, 0xff3a3c40);
        g.fill(1, 318, PANEL_WIDTH - 1, 319, 0xff3a3c40);
        switch (tab) {
            case GENERAL -> generalText(g);
            case APPEARANCE -> appearanceText(g);
            case SOUNDS -> soundText(g);
        }
        if (saveError) label(g, text("save.error"), LEFT, STATUS_Y, CONTENT_WIDTH, UiDraw.ERROR);
        super.extractRenderState(g, (int) localX(mouseX), (int) localY(mouseY), delta);
        g.pose().popMatrix();
    }

    private void generalText(GuiGraphicsExtractor g) {
        label(g, text("animation"), LEFT, 88, CONTENT_WIDTH - 48, UiDraw.TEXT);
        label(g, text(config().animationEnabled ? "animation.help" : "animation.persistent"),
                LEFT, 102, CONTENT_WIDTH - 48, UiDraw.MUTED);
        timeText(g, "delay", 119, settings.delayValid());
        timeText(g, "duration", 153, settings.durationValid());

        label(g, text("scale"), LEFT, 193, CONTENT_WIDTH, UiDraw.TEXT);
        String value = Math.round(config().scale * 100) + "%";
        g.text(font, value, RIGHT - font.width(value), 193, UiDraw.GOLD);
        g.text(font, "50%", LEFT, 224, UiDraw.MUTED);
        g.text(font, "400%", RIGHT - font.width("400%"), 224, UiDraw.MUTED);
        label(g, text("position"), LEFT, 241, CONTENT_WIDTH, UiDraw.TEXT);
        g.text(font, "X (%)", LEFT, 289, UiDraw.MUTED);
        g.text(font, "Y (%)", 187, 289, UiDraw.MUTED);
        if (!saveError) {
            boolean valid = settings.xValid() && settings.yValid();
            label(g, text(valid ? "coordinate.help" : "coordinate.error"),
                    LEFT, STATUS_Y, CONTENT_WIDTH, valid ? UiDraw.MUTED : UiDraw.ERROR);
        }
    }

    private void timeText(GuiGraphicsExtractor g, String key, int y, boolean valid) {
        int textWidth = TIME_FIELD_X - LEFT - 10;
        label(g, text(key), LEFT, y, textWidth, UiDraw.TEXT);
        String hint = !config().animationEnabled ? "time.inactive" : key + (valid ? ".help" : ".error");
        label(g, text(hint), LEFT, y + 14, textWidth, valid ? UiDraw.MUTED : UiDraw.ERROR);
    }

    private void appearanceText(GuiGraphicsExtractor g) {
        label(g, text("motion"), LEFT, 88, CONTENT_WIDTH - 48, UiDraw.TEXT);
        label(g, text("motion.help"), LEFT, 102, CONTENT_WIDTH - 48, UiDraw.MUTED);
        if (!saveError) label(g, text("style.note"), LEFT, STATUS_Y, CONTENT_WIDTH, UiDraw.MUTED);
    }

    private void soundText(GuiGraphicsExtractor g) {
        label(g, text("sound.title"), LEFT, 89, 220, UiDraw.TEXT);
        Component count = text("sound.count", SoundCatalog.CHOICES.size());
        g.text(font, count, RIGHT - font.width(count), 89, UiDraw.MUTED);
        UiDraw.panel(g, LEFT, SOUND_ROW_Y - 1, CONTENT_WIDTH, SOUND_ROW_HEIGHT * SOUNDS_PER_PAGE + 2,
                5, 0xff414348, 0xff202226);
        for (int i = 0; i < SOUNDS_PER_PAGE; i++) {
            var choice = SoundCatalog.CHOICES.get(soundPage * SOUNDS_PER_PAGE + i);
            boolean selected = config().soundId.equals(choice.id());
            int y = soundRowY(i);
            if (selected) g.fill(LEFT + 1, y, RIGHT - 1, y + SOUND_ROW_HEIGHT, 0xff333229);
            if (i > 0) g.fill(LEFT + 1, y, RIGHT - 1, y + 1, 0xff34373c);
            label(g, soundName(choice), LEFT + 9, y + 8, CONTENT_WIDTH - 104, selected ? UiDraw.GOLD : UiDraw.TEXT);
        }
        g.centeredText(font, (soundPage + 1) + " / " + soundPageCount(), PANEL_WIDTH / 2, 260, UiDraw.MUTED);
        Component selected = soundName(SoundCatalog.find(config().soundId));
        label(g, text("sound.current", selected), LEFT, 286, CONTENT_WIDTH, 0xffc3b28c);
        if (!saveError) {
            label(g, text(config().animationEnabled ? "sound.help" : "sound.persistent"),
                    LEFT, STATUS_Y, CONTENT_WIDTH, UiDraw.MUTED);
        }
    }

    private void label(GuiGraphicsExtractor g, Component label, int x, int y, int maxWidth, int color) {
        g.text(font, UiDraw.fit(font, label, maxWidth), x, y, color);
    }

    private double localX(double x) { return (x - left) / panelScale; }
    private double localY(double y) { return (y - top) / panelScale; }
    private MouseButtonEvent local(MouseButtonEvent event) {
        return new MouseButtonEvent(localX(event.x()), localY(event.y()), event.buttonInfo());
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return super.mouseClicked(local(event), doubleClick); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(local(event)); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return super.mouseDragged(local(event), dx / panelScale, dy / panelScale);
    }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(localX(x), localY(y)); }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return super.mouseScrolled(localX(x), localY(y), horizontal, vertical);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
