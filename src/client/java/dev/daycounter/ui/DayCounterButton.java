package dev.daycounter.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

class DayCounterButton extends Button {
    boolean selected, accent, silent;
    Component narration;

    DayCounterButton(int x, int y, int width, int height, Component label, Runnable action) {
        super(x, y, width, height, label, button -> action.run(), DEFAULT_NARRATION);
    }

    @Override protected MutableComponent createNarrationMessage() {
        return (narration == null ? getMessage() : narration).copy();
    }

    @Override protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        boolean highlighted = active && isHoveredOrFocused();
        int edge = selected ? 0xff75674a : highlighted ? 0xffaaa9a2 : active ? 0xff5b5d61 : 0xff424449;
        int background = selected ? 0xff3b3930 : highlighted ? 0xff45484c : active ? 0xff35383c : 0xff2a2c30;
        if (accent && active) { edge = highlighted ? 0xffd0b57d : 0xffa38b5f; background = 0xff514a38; }
        if (active) UiDraw.rounded(g, getX(), getY() + 2, getWidth(), getHeight(), 4, 0xff141618);
        UiDraw.panel(g, getX(), getY(), getWidth(), getHeight(), 4, edge, background);
        if (isFocused() && active) g.outline(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4, UiDraw.GOLD);
        var font = Minecraft.getInstance().font;
        String label = UiDraw.fit(font, getMessage(), getWidth() - 10);
        int color = selected || accent && active ? UiDraw.GOLD : active ? UiDraw.TEXT : 0xff8d9093;
        g.centeredText(font, label, getX() + getWidth() / 2, getY() + (getHeight() - 9) / 2, color);
    }

    @Override public void playDownSound(SoundManager sounds) { if (!silent) super.playDownSound(sounds); }
}
