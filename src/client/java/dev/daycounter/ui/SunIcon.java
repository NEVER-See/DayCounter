package dev.daycounter.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class SunIcon {
    public static final int STATIC_FRAME = -1;
    private static final int FRAME_SIZE = 39;
    private static final int ANIMATION_HEIGHT = FRAME_SIZE * 20;
    private static final Identifier STATIC = Identifier.fromNamespaceAndPath("daycounter", "textures/gui/sun_static.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("daycounter", "textures/gui/sun_animation.png");

    private SunIcon() {}

    /** Load the tiny static and animated textures before the first in-world notification. */
    public static void preload() {
        var textures = Minecraft.getInstance().getTextureManager();
        textures.getTexture(STATIC);
        textures.getTexture(ANIMATION);
    }

    public static void draw(GuiGraphicsExtractor g, int x, int y, int size, int frame, int tint) {
        boolean animated = frame >= 0;
        Identifier texture = animated ? ANIMATION : STATIC;
        int frameY = animated ? frame * FRAME_SIZE : 0;
        int textureHeight = animated ? ANIMATION_HEIGHT : FRAME_SIZE;
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, frameY, size, size,
                FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, textureHeight, tint);
    }
}
