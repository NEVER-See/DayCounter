package dev.daycounter.hud;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Bakes the sun and decimal glyphs into one mask per plaque label. */
final class PlaqueEngraving {
    private static final System.Logger LOGGER = System.getLogger("DayCounter");
    private static final int HEIGHT = 38;
    private static final int FIRST_ROW = 5, END_ROW = 32;
    private static final int SUN_LEFT = 11, SUN_RIGHT = 40, NUMBER_LEFT = 48, NUMBER_INSET = 8;
    private static final int SUN_SIZE = 24, SUN_X = 13, SUN_Y = 7;
    private static final double NUMBER_SCALE = 19.0 / 8;
    private static final double NUMBER_Y = 9;
    private static final int ALPHA_THRESHOLD = 90;
    private static final int INK = 0x51311e;
    private static final Identifier ASCII = Identifier.fromNamespaceAndPath("minecraft", "textures/font/ascii.png");
    private static final Identifier SUN = Identifier.fromNamespaceAndPath("daycounter", "textures/gui/sun_static.png");
    private static final Slot PREVIEW = new Slot("preview");
    private static final Slot CURRENT = new Slot("current");
    private static final Slot PREVIOUS = new Slot("previous");

    private static Stencils stencils;
    private static long preparedDay, preparedPreviousDay;
    private static boolean preparedTransition;

    private PlaqueEngraving() {}

    static void initialize(Minecraft client) {
        var resources = (ReloadableResourceManager) client.getResourceManager();
        resources.registerReloadListener(new SimplePreparableReloadListener<Stencils>() {
            @Override protected Stencils prepare(ResourceManager resources, ProfilerFiller profiler) {
                return load(resources);
            }

            @Override protected void apply(Stencils loaded, ResourceManager resources, ProfilerFiller profiler) {
                install(loaded);
            }
        });
        install(load(resources));
    }

    private static void install(Stencils loaded) {
        ToastRenderer.resourcesReloaded();
        PREVIEW.clear();
        CURRENT.clear();
        PREVIOUS.clear();
        stencils = loaded;
        if (stencils == null) return;
        PREVIEW.prepare(42, width(42, 0));
        prepare(preparedDay, preparedPreviousDay, preparedTransition);
    }

    static void prepare(long day, long previousDay, boolean changing) {
        preparedDay = day;
        preparedPreviousDay = previousDay;
        preparedTransition = changing;
        if (stencils == null || day < 1) return;
        int width = width(day, changing ? previousDay : 0);
        CURRENT.prepare(day, width);
        if (changing && previousDay > 0) PREVIOUS.prepare(previousDay, width);
    }

    static boolean isReady() {
        return stencils != null;
    }

    static int width(long currentDay, long previousDay) {
        int digitsWidth = Math.max(advance(currentDay), advance(previousDay));
        return Math.max(112, (int) Math.ceil(digitsWidth * NUMBER_SCALE) + 66);
    }

    private static int advance(long day) {
        int width = 0;
        while (day > 0) {
            width += stencils == null ? 6 : stencils.digits[(int) (day % 10)].advance;
            day /= 10;
        }
        return width;
    }

    private record Digit(byte[] alpha, int width, int height, int advance) {}
    private record Stencils(boolean[] sun, Digit[] digits) {}

    private static Stencils load(ResourceManager resources) {
        try (var sunInput = resources.open(SUN); var asciiInput = resources.open(ASCII);
             var sun = NativeImage.read(sunInput); var ascii = NativeImage.read(asciiInput)) {
            if (ascii.getWidth() % 16 != 0 || ascii.getHeight() % 16 != 0) {
                throw new IOException("Minecraft ASCII font texture does not contain a 16 by 16 grid");
            }
            boolean[] sunMask = new boolean[SUN_SIZE * SUN_SIZE];
            for (int y = 0; y < SUN_SIZE; y++) {
                for (int x = 0; x < SUN_SIZE; x++) {
                    int sx = (2 * x + 1) * sun.getWidth() / (2 * SUN_SIZE);
                    int sy = (2 * y + 1) * sun.getHeight() / (2 * SUN_SIZE);
                    sunMask[y * SUN_SIZE + x] = Byte.toUnsignedInt(sun.getLuminanceOrAlpha(sx, sy)) > ALPHA_THRESHOLD;
                }
            }
            Digit[] digits = new Digit[10];
            int cellWidth = ascii.getWidth() / 16, cellHeight = ascii.getHeight() / 16;
            for (int digit = 0; digit < digits.length; digit++) {
                int cell = '0' + digit;
                int left = (cell % 16) * cellWidth, top = (cell / 16) * cellHeight;
                byte[] alpha = new byte[cellWidth * cellHeight];
                int glyphWidth = 0;
                for (int y = 0; y < cellHeight; y++) {
                    for (int x = 0; x < cellWidth; x++) {
                        byte value = ascii.getLuminanceOrAlpha(left + x, top + y);
                        alpha[y * cellWidth + x] = value;
                        if (value != 0) glyphWidth = Math.max(glyphWidth, x + 1);
                    }
                }
                if (glyphWidth == 0) throw new IOException("Minecraft ASCII font is missing digit " + digit);
                int advance = Math.round(glyphWidth * 8f / cellHeight) + 1;
                digits[digit] = new Digit(alpha, cellWidth, cellHeight, advance);
            }
            return new Stencils(sunMask, digits);
        } catch (IOException | RuntimeException error) {
            LOGGER.log(System.Logger.Level.WARNING, "Could not prepare the plaque engraving", error);
            return null;
        }
    }

    private static boolean[] mask(long day, int width) {
        boolean[] mask = new boolean[width * HEIGHT];
        for (int y = 0; y < SUN_SIZE; y++) {
            System.arraycopy(stencils.sun, y * SUN_SIZE, mask, (SUN_Y + y) * width + SUN_X, SUN_SIZE);
        }
        double x = width / 2.0 + 20 - advance(day) * NUMBER_SCALE / 2;
        String digits = Long.toString(day);
        for (int i = 0; i < digits.length(); i++) {
            Digit digit = stencils.digits[digits.charAt(i) - '0'];
            rasterizeDigit(mask, width, digit, x);
            x += digit.advance * NUMBER_SCALE;
        }
        return mask;
    }

    private static void rasterizeDigit(boolean[] mask, int width, Digit digit, double left) {
        // Measure coverage at the same 19 px per eight source rows as the plaque label.
        double scale = 19.0 / digit.height;
        int right = Math.min(width - NUMBER_INSET, (int) Math.ceil(left + digit.width * scale));
        int bottom = Math.min(END_ROW, (int) Math.ceil(NUMBER_Y + digit.height * scale));
        for (int y = (int) NUMBER_Y; y < bottom; y++) {
            for (int x = Math.max(NUMBER_LEFT, (int) Math.floor(left)); x < right; x++) {
                if (digitCoverage(digit, left, scale, x, y) > ALPHA_THRESHOLD) mask[y * width + x] = true;
            }
        }
    }

    private static double digitCoverage(Digit digit, double left, double scale, int x, int y) {
        double opacity = 0;
        int startX = Math.max(0, (int) Math.floor((x - left) / scale));
        int endX = Math.min(digit.width, (int) Math.ceil((x + 1 - left) / scale));
        int startY = (int) Math.floor((y - NUMBER_Y) / scale);
        int endY = Math.min(digit.height, (int) Math.ceil((y + 1 - NUMBER_Y) / scale));
        for (int sy = startY; sy < endY; sy++) {
            double coveredY = Math.min(y + 1, NUMBER_Y + (sy + 1) * scale) - Math.max(y, NUMBER_Y + sy * scale);
            for (int sx = startX; sx < endX; sx++) {
                double coveredX = Math.min(x + 1, left + (sx + 1) * scale) - Math.max(x, left + sx * scale);
                opacity += coveredX * coveredY * Byte.toUnsignedInt(digit.alpha[sy * digit.width + sx]);
            }
        }
        return opacity;
    }

    private static int appendPath(boolean[] mask, int width, int left, int right, int[] points, int size) {
        for (int y = FIRST_ROW; y < END_ROW; y++) {
            int row = y * width;
            for (int step = 0; step < right - left; step++) {
                int x = (y & 1) == 0 ? left + step : right - 1 - step;
                int point = row + x;
                if (mask[point]) points[size++] = point;
            }
        }
        return size;
    }

    private static final class Slot {
        private final Identifier id;
        private DynamicTexture texture;
        private long day;
        private int width;
        private int sunPoints;
        private int[] path;

        Slot(String name) {
            id = Identifier.fromNamespaceAndPath("daycounter", "runtime/plaque_" + name);
        }

        void clear() {
            if (texture != null) Minecraft.getInstance().getTextureManager().release(id);
            texture = null;
            path = null;
        }

        void prepare(long day, int width) {
            if (texture != null && this.day == day && this.width == width) return;
            boolean[] mask = mask(day, width);
            int[] points = new int[mask.length];
            int sunPoints = appendPath(mask, width, SUN_LEFT, SUN_RIGHT, points, 0);
            int count = appendPath(mask, width, NUMBER_LEFT, width - NUMBER_INSET, points, sunPoints);

            boolean reuse = texture != null && this.width == width;
            NativeImage image = reuse ? texture.getPixels() : new NativeImage(width, HEIGHT, true);
            if (reuse) image.fillRect(0, 0, width, HEIGHT, 0);
            for (int i = 0; i < count; i++) {
                int point = points[i];
                image.setPixel(point % width, point / width, 0xffffffff);
            }
            if (reuse) {
                texture.upload();
            } else {
                texture = new DynamicTexture(() -> "DayCounter plaque engraving", image);
                // Registering a replacement closes the old texture under the same identifier.
                Minecraft.getInstance().getTextureManager().register(id, texture);
            }

            this.day = day;
            this.width = width;
            this.sunPoints = sunPoints;
            this.path = Arrays.copyOf(points, count);
        }
    }

    static void draw(GuiGraphicsExtractor g, long day, long previousDay, int width, double opacity,
                     boolean animated, double detailAge, boolean changing, double changeAge, boolean preview) {
        if (animated && detailAge <= 0) return;
        Slot slot = preview ? PREVIEW : CURRENT;
        if (changing && changeAge < .2) {
            PREVIOUS.prepare(previousDay, width);
            drawSun(g, PREVIOUS, opacity);
            double fade = changeAge / .2;
            double previousOpacity = 1 - fade * fade * (3 - 2 * fade);
            blit(g, PREVIOUS, NUMBER_LEFT, FIRST_ROW, width - NUMBER_INSET, END_ROW, opacity * previousOpacity);
            return;
        }
        slot.prepare(day, width);
        if (animated && detailAge < 2.8) {
            paint(g, slot, 0, detailAge / 2.8, detailAge + .4, opacity);
        } else if (changing) {
            drawSun(g, slot, opacity);
            paint(g, slot, slot.sunPoints, (changeAge - .2) / 2, changeAge, opacity);
        } else {
            blit(g, slot, 0, 0, width, HEIGHT, opacity);
        }
    }

    private static void drawSun(GuiGraphicsExtractor g, Slot slot, double opacity) {
        blit(g, slot, SUN_LEFT, FIRST_ROW, SUN_RIGHT, END_ROW, opacity);
    }

    private static void paint(GuiGraphicsExtractor g, Slot slot, int start, double progress, double time,
                              double opacity) {
        int count = (int) Math.floor((slot.path.length - start) * Math.clamp(progress, 0, 1));
        if (count == 0) return;

        int end = start + count;
        int tail = progress < 1 ? Math.min(24, count) : 0;
        int coolEnd = end - tail;
        if (start == 0 && coolEnd > 0) {
            prefix(g, slot, Math.min(coolEnd, slot.sunPoints), SUN_LEFT, SUN_RIGHT, opacity);
        }
        if (coolEnd > slot.sunPoints) {
            prefix(g, slot, coolEnd, NUMBER_LEFT, slot.width - NUMBER_INSET, opacity);
        }
        if (tail == 0) return;
        for (int i = coolEnd; i < end; i++) {
            int point = slot.path[i];
            int behind = end - i;
            int glow = behind > 15 ? 0x9a3223 : behind > 7 ? 0xe34a2b : behind > 2 ? 0xff8b44 : 0xffe2a0;
            pixel(g, point % slot.width, point / slot.width, glow, opacity);
        }

        int point = slot.path[end - 1];
        int x = point % slot.width, y = point / slot.width;
        int flare = color(0xff7950, opacity);
        g.fill(x - 1, y, x + 2, y + 1, flare);
        g.fill(x, y - 1, x + 1, y + 2, flare);
        pixel(g, x, y, 0xfff0c8, opacity);
        int flicker = (int) Math.floor(time * 22) % 3;
        pixel(g, x + (flicker == 0 ? 3 : 2), y - 3, 0xd65435, opacity);
        pixel(g, x - 2, y - 2 - flicker, 0xd65435, opacity);
    }

    private static void prefix(GuiGraphicsExtractor g, Slot slot, int end, int left, int right, double opacity) {
        if (end == 0) return;
        int point = slot.path[end - 1];
        int x = point % slot.width, y = point / slot.width;
        blit(g, slot, left, FIRST_ROW, right, y, opacity);
        if ((y & 1) == 0) {
            blit(g, slot, left, y, x + 1, y + 1, opacity);
        } else {
            blit(g, slot, x, y, right, y + 1, opacity);
        }
    }

    private static void blit(GuiGraphicsExtractor g, Slot slot, int x, int y, int right, int bottom, double opacity) {
        int width = right - x, height = bottom - y;
        if (width <= 0 || height <= 0 || opacity < 1.0 / 255) return;
        blitTexture(g, slot.id, x, y, width, height, slot.width, color(INK, opacity));
    }

    private static void blitTexture(GuiGraphicsExtractor g, Identifier texture, int x, int y,
                                    int width, int height, int textureWidth, int tint) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) x, (float) y, width, height,
                width, height, textureWidth, HEIGHT, tint);
    }

    private static void pixel(GuiGraphicsExtractor g, int x, int y, int rgb, double opacity) {
        g.fill(x, y, x + 1, y + 1, color(rgb, opacity));
    }

    private static int color(int rgb, double opacity) {
        return ((int) Math.round(255 * opacity) << 24) | rgb;
    }
}
