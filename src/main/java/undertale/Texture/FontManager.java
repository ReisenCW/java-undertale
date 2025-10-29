package undertale.Texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;

import undertale.GameMain.Game;

import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTAlignedQuad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

import java.util.HashMap;

public class FontManager {
    private static FontManager instance;
    private HashMap<String, String> fonts;
    private final int BITMAP_W = 512, BITMAP_H = 512;
    private final int FONT_SIZE = 32;
    private final int FIRST_CHAR = 32, CHAR_COUNT = 96; // ASCII 32~127

    static {
        instance = new FontManager();
    }

    // 字体缓存结构
    private static class FontData {
        int textureId;
        STBTTFontinfo fontInfo;
        float[] charWidths;
        STBTTBakedChar.Buffer charData; // 缓存字符数据
    }
    private HashMap<String, FontData> fontCache = new HashMap<>();
    private String currentFontKey = "determination";

    private FontManager() {
        fonts = Game.configManager.fonts;
        for (String key : fonts.keySet()) {
            loadFont(key);
        }
        currentFontKey = "determination";
    }

    public static FontManager getInstance() {
        return instance;
    }

    // 加载并缓存字体
    private void loadFont(String fontKey) {
        String filePath = fonts.get(fontKey);
        if (filePath == null) throw new RuntimeException("Font key not found: " + fontKey);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) throw new IOException("Font file not found: " + filePath);
            byte[] bytes = is.readAllBytes();
            ByteBuffer fontData = BufferUtils.createByteBuffer(bytes.length);
            fontData.put(bytes).flip();

            STBTTFontinfo fontInfo = STBTTFontinfo.create();
            if (!stbtt_InitFont(fontInfo, fontData)) throw new RuntimeException("Failed to init font");

            ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(CHAR_COUNT);
            stbtt_BakeFontBitmap(fontData, FONT_SIZE, bitmap, BITMAP_W, BITMAP_H, FIRST_CHAR, charData);

            float[] charWidths = new float[CHAR_COUNT];
            for (int i = 0; i < CHAR_COUNT; i++) {
                charWidths[i] = charData.get(i).xadvance();
            }

            // Convert single-channel bitmap (alpha only) into RGBA so our shader (which samples RGBA) works
            java.nio.ByteBuffer rgba = org.lwjgl.BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < BITMAP_W * BITMAP_H; i++) {
                byte a = bitmap.get(i);
                rgba.put((byte)255);
                rgba.put((byte)255);
                rgba.put((byte)255);
                rgba.put(a);
            }
            rgba.flip();

            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);


            FontData fd = new FontData();
            fd.textureId = textureId;
            fd.fontInfo = fontInfo;
            fd.charWidths = charWidths;
            fd.charData = charData; // 缓存字符数据
            fontCache.put(fontKey, fd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 切换当前字体
    public void setFont(String fontKey) {
        if (!fontCache.containsKey(fontKey)) loadFont(fontKey);
        currentFontKey = fontKey;
    }

    public void drawText(String text, float x, float y, float scale, float r, float g, float b, float a, String fontKey) {
        FontData fd = fontCache.getOrDefault(fontKey, fontCache.get(currentFontKey));
        // Build a batched buffer of quads and render via Texture.drawQuads
        int validChars = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) continue;
            validChars++;
        }
        if (validChars == 0) return;

        FloatBuffer buf = BufferUtils.createFloatBuffer(validChars * 6 * 4);
        float xpos = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) continue;
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc();
            float[] xp = new float[] { xpos };
            float[] yp = new float[] { y };
            stbtt_GetBakedQuad(getCharData(fd), BITMAP_W, BITMAP_H, c - FIRST_CHAR, xp, yp, quad, true);
            float x0 = quad.x0();
            float y0 = quad.y0();
            float x1 = quad.x1();
            float y1 = quad.y1();
            float s0 = quad.s0();
            float t0 = quad.t0();
            float s1 = quad.s1();
            float t1 = quad.t1();

            float width = (x1 - x0) * scale;
            float height = (y1 - y0) * scale;
            x1 = x0 + width;
            y1 = y0 + height;

            // tri1
            buf.put(x0); buf.put(y0); buf.put(s0); buf.put(t0);
            buf.put(x1); buf.put(y0); buf.put(s1); buf.put(t0);
            buf.put(x1); buf.put(y1); buf.put(s1); buf.put(t1);
            // tri2
            buf.put(x0); buf.put(y0); buf.put(s0); buf.put(t0);
            buf.put(x1); buf.put(y1); buf.put(s1); buf.put(t1);
            buf.put(x0); buf.put(y1); buf.put(s0); buf.put(t1);

            xpos += fd.charWidths[c - FIRST_CHAR] * scale;
            quad.free();
        }
        buf.flip();
        Texture.drawQuads(buf, validChars, fd.textureId, r, g, b, a);
    }

    // 兼容原接口，使用当前字体
    public void drawText(String text, float x, float y, float scale, float r, float g, float b, float a) {
        drawText(text, x, y, scale, r, g, b, a, currentFontKey);
    }
    public void drawText(String text, float x, float y, float r, float g, float b, float a) {
        drawText(text, x, y, 1.0f, r, g, b, a, currentFontKey);
    }

    // 获取字符数据
    private STBTTBakedChar.Buffer getCharData(FontData fd) {
        return fd.charData; // 直接返回缓存的字符数据
    }

    public void destroy() {
        for (FontData fd : fontCache.values()) {
            glDeleteTextures(fd.textureId);
            fd.fontInfo.free();
            if (fd.charData != null) {
                fd.charData.free(); // 释放缓存的字符数据
            }
        }
    }

    public float getCharWidth(char c) {
        if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) return 0;
        return fontCache.get(currentFontKey).charWidths[c - FIRST_CHAR];
    }

    public float getTextWidth(String text) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            width += getCharWidth(c);
        }
        return width;
    }

    public float getFontHeight() {
        return FONT_SIZE;
    }
}