package dev.daycounter.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

final class SettingField extends EditBox {
    boolean valid = true;

    SettingField(Font font, int x, int y, int width, Component label) {
        super(font, x, y, width, 22, label);
        setBordered(false);
        setCentered(true);
        setTextColor(0xfff3e6c8);
        setTextColorUneditable(0xff797e85);
        setTextShadow(false);
        setMaxLength(16);
    }

    @Override public void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int edge = !active ? 0xff414348 : !valid ? 0xffdc7a7a : isFocused() ? UiDraw.GOLD : 0xff6b6a60;
        UiDraw.panel(g, getX(), getY(), getWidth(), getHeight(), 4, edge, active ? 0xff1a1c20 : 0xff222429);
        g.pose().pushMatrix();
        g.pose().translate(0, (getHeight() - 9) / 2f);
        super.extractWidgetRenderState(g, mouseX, mouseY, delta);
        g.pose().popMatrix();
    }
}
