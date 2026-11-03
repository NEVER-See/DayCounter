package dev.daycounter.ui;

import dev.daycounter.config.ConfigStore;
import dev.daycounter.config.DayCounterConfig;
import dev.daycounter.config.SettingsDraft;
import dev.daycounter.config.SoundCatalog;
import dev.daycounter.i18n.Translations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** A fixed-proportion panel, uniformly fitted to the available GUI area. */
public final class DayCounterSettingsScreen extends Screen {
    private static final int PANEL_WIDTH = 340, PANEL_HEIGHT = 330;
    private final Screen parent;
    private final SettingsDraft settings = new SettingsDraft(ConfigStore.get());
    private int tab, soundPage;
    private float panelScale = 1, left, top;
    private SettingField duration, xField, yField;
    private DayCounterButton apply;
    private final DayCounterButton[] positions = new DayCounterButton[3];
    private boolean saveError;

    public DayCounterSettingsScreen(Screen parent) {
        super(Component.literal(Translations.text(ConfigStore.get().language, "daycounter.title")));
        this.parent = parent;
        formatDuration();
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
        duration = xField = yField = null;
        var language = button(304, 14, 22, 22, Component.literal(config().language.name()), this::switchLanguage);
        language.narration = text("language.switch");
        language.setTooltip(Tooltip.create(language.narration));
        for (int i = 0; i < 2; i++) {
            int chosenTab = i;
            var button = button(14 + i * 160, 47, 152, 22, text(i == 0 ? "tab.appearance" : "tab.sounds"), () -> {
                tab = chosenTab;
                rebuildWidgets();
            });
            button.selected = tab == i;
            button.active = tab != i;
        }
        if (tab == 0) appearance(); else sounds();
        apply = button(14, 300, 99, 22, text("apply"), this::apply);
        apply.accent = true;
        button(120, 300, 100, 22, text("reset"), () -> {
            settings.reset();
            formatDuration();
            saveError = false;
            soundPage = 0;
            rebuildWidgets();
        });
        button(227, 300, 99, 22, text("cancel"), this::onClose);
        refresh();
    }

    private void appearance() {
        addRenderableWidget(new AnimationToggle(288, 86, config().animationEnabled, config().language, enabled -> {
            config().animationEnabled = enabled;
            if (!enabled) {
                formatDuration();
                duration.setValue(settings.durationText());
            }
            changed();
        }));
        duration = new SettingField(font, 246, 116, 80, text("duration"));
        duration.setValue(settings.durationText());
        duration.setResponder(value -> { settings.durationText(value); changed(); });
        addRenderableWidget(duration);

        addRenderableWidget(new ScaleSlider(14, 174, 312, config().scale, config().language, scale -> {
            config().scale = scale;
            changed();
        }));
        String[] names = {"position.top", "position.hud", "position.manual"};
        for (int i = 0; i < 3; i++) {
            var position = DayCounterConfig.Position.values()[i];
            positions[i] = button(14 + i * 106, 220, 100, 20, text(names[i]), () -> {
                config().position = position;
                changed();
            });
        }
        xField = new SettingField(font, 48, 253, 119, text("coordinate.x"));
        xField.setValue(settings.xText());
        xField.setResponder(value -> { settings.xText(value); changed(); });
        addRenderableWidget(xField);
        yField = new SettingField(font, 207, 253, 119, text("coordinate.y"));
        yField.setValue(settings.yText());
        yField.setResponder(value -> { settings.yText(value); changed(); });
        addRenderableWidget(yField);
    }

    private void sounds() {
        int first = soundPage * 6;
        for (int i = 0; i < 6; i++) {
            var choice = SoundCatalog.CHOICES.get(first + i);
            boolean selected = config().soundId.equals(choice.id());
            var button = button(242, 108 + i * 23, 78, 18, text(selected ? "selected" : "select"), () -> {
                config().soundId = choice.id();
                DayCounterSounds.play(choice.id());
                saveError = false;
                rebuildWidgets();
            });
            button.selected = selected;
            button.active = !selected;
            button.silent = true; // The selected sound is the sole audible response.
        }
        button(14, 253, 99, 20, text("previous"), () -> { soundPage--; rebuildWidgets(); }).active = soundPage > 0;
        button(227, 253, 99, 20, text("next"), () -> { soundPage++; rebuildWidgets(); }).active = soundPage < 2;
    }

    private DayCounterButton button(int x, int y, int w, int h, Component label, Runnable action) {
        return addRenderableWidget(new DayCounterButton(x, y, w, h, label, action));
    }

    private void changed() { saveError = false; refresh(); }

    private void refresh() {
        if (apply != null) apply.active = settings.canApply();
        if (duration == null) return;
        duration.active = config().animationEnabled;
        duration.setEditable(duration.active);
        duration.valid = settings.durationValid();
        boolean manual = config().position == DayCounterConfig.Position.MANUAL;
        xField.active = yField.active = manual;
        xField.setEditable(manual);
        yField.setEditable(manual);
        xField.valid = settings.xValid();
        yField.valid = settings.yValid();
        for (int i = 0; i < positions.length; i++) {
            positions[i].selected = config().position.ordinal() == i;
            positions[i].active = !positions[i].selected;
        }
    }

    private void formatDuration() {
        settings.durationText(SettingsDraft.format(config().durationSeconds) + " " + text("seconds").getString());
    }

    private void switchLanguage() {
        String formatted = SettingsDraft.format(config().durationSeconds) + " " + text("seconds").getString();
        boolean automaticDuration = settings.durationText().equals(formatted);
        config().language = config().language == DayCounterConfig.Language.EN
                ? DayCounterConfig.Language.RU : DayCounterConfig.Language.EN;
        if (automaticDuration) formatDuration();
        saveError = false;
        rebuildWidgets();
    }

    private void apply() {
        if (!settings.canApply()) return;
        if (ConfigStore.save(config())) {
            settings.applied();
            saveError = false;
            formatDuration();
            if (duration != null) duration.setValue(settings.durationText());
            refresh();
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
        UiDraw.sun(g, 16, 17);
        g.pose().pushMatrix();
        g.pose().translate(44, 12);
        g.pose().scale(1.35f, 1.35f);
        g.text(font, "DayCounter", 0, 0, UiDraw.TEXT);
        g.pose().popMatrix();
        label(g, text("subtitle"), 44, 28, 250, UiDraw.MUTED);
        g.fill(1, 78, PANEL_WIDTH - 1, 79, 0xff3a3c40);
        g.fill(1, 291, PANEL_WIDTH - 1, 292, 0xff3a3c40);
        if (tab == 0) appearanceText(g); else soundText(g);
        super.extractRenderState(g, (int) localX(mouseX), (int) localY(mouseY), delta);
        g.pose().popMatrix();
    }

    private void appearanceText(GuiGraphicsExtractor g) {
        label(g, text("animation"), 14, 86, 265, UiDraw.TEXT);
        label(g, text(config().animationEnabled ? "animation.help" : "animation.persistent"), 14, 101, 265, UiDraw.MUTED);
        label(g, text("duration"), 14, 118, 220, UiDraw.TEXT);
        label(g, text(config().animationEnabled ? "duration.help" : "duration.inactive"), 14, 132, 220, UiDraw.MUTED);
        if (!settings.durationValid()) label(g, text("duration.error"), 14, 146, 312, UiDraw.ERROR);
        label(g, text("scale"), 14, 160, 230, UiDraw.TEXT);
        String value = Math.round(config().scale * 100) + "%";
        g.text(font, value, 326 - font.width(value), 160, UiDraw.GOLD);
        label(g, text("scale.help"), 14, 192, 312, UiDraw.MUTED);
        label(g, text("position"), 14, 207, 312, UiDraw.TEXT);
        g.text(font, "X (%)", 14, 260, UiDraw.MUTED);
        g.text(font, "Y (%)", 173, 260, UiDraw.MUTED);
        String help = saveError ? "save.error" : !settings.xValid() || !settings.yValid() ? "coordinate.error" : "coordinate.help";
        label(g, text(help), 14, 280, 312, saveError || !settings.valid() ? UiDraw.ERROR : UiDraw.MUTED);
    }

    private void soundText(GuiGraphicsExtractor g) {
        label(g, text("sound.title"), 14, 89, 220, UiDraw.TEXT);
        Component count = text("sound.count", SoundCatalog.CHOICES.size());
        g.text(font, count, 326 - font.width(count), 89, UiDraw.MUTED);
        UiDraw.panel(g, 14, 104, 312, 140, 5, 0xff414348, 0xff202226);
        for (int i = 0; i < 6; i++) {
            var choice = SoundCatalog.CHOICES.get(soundPage * 6 + i);
            boolean selected = config().soundId.equals(choice.id());
            int y = 105 + i * 23;
            if (selected) g.fill(15, y, 325, y + 23, 0xff333229);
            if (i > 0) g.fill(15, y, 325, y + 1, 0xff34373c);
            label(g, soundName(choice), 23, y + 8, 210, selected ? UiDraw.GOLD : 0xffd9dade);
        }
        g.centeredText(font, (soundPage + 1) + " / 3", 170, 259, UiDraw.MUTED);
        Component selected = soundName(SoundCatalog.find(config().soundId));
        label(g, saveError ? text("save.error") : text("sound.current", selected), 14, 280, 312,
                saveError ? UiDraw.ERROR : 0xffc3b28c);
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
