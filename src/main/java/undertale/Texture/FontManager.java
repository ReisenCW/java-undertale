package undertale.Texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;

import undertale.GameMain.Game;

import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTAlignedQuad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
        ByteBuffer fontData;
        STBTTFontinfo fontInfo;
        float[] charWidths;
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

            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            charData.free();

            FontData fd = new FontData();
            fd.textureId = textureId;
            fd.fontData = fontData;
            fd.fontInfo = fontInfo;
            fd.charWidths = charWidths;
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
        glBindTexture(GL_TEXTURE_2D, fd.textureId);
        glColor4f(r, g, b, a);
        float xpos = x;
        glBegin(GL_QUADS);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) continue;
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc();
            stbtt_GetBakedQuad(getCharData(fd), BITMAP_W, BITMAP_H, c - FIRST_CHAR, new float[]{xpos}, new float[]{y}, quad, true);
            float x0 = quad.x0();
            float y0 = quad.y0();
            float x1 = quad.x1();
            float y1 = quad.y1();
            float width = (x1 - x0) * scale;
            float height = (y1 - y0) * scale;
            x1 = x0 + width;
            y1 = y0 + height;
            glTexCoord2f(quad.s0(), quad.t0());
            glVertex2f(x0, y0);
            glTexCoord2f(quad.s1(), quad.t0());
            glVertex2f(x1, y0);
            glTexCoord2f(quad.s1(), quad.t1());
            glVertex2f(x1, y1);
            glTexCoord2f(quad.s0(), quad.t1());
            glVertex2f(x0, y1);
            xpos += fd.charWidths[c - FIRST_CHAR] * scale;
            quad.free();
        }
        glEnd();
        glBindTexture(GL_TEXTURE_2D, 0);
        glColor4f(1, 1, 1, 1);
    }

    // 兼容原接口，使用当前字体
    public void drawText(String text, float x, float y, float scale, float r, float g, float b, float a) {
        drawText(text, x, y, scale, r, g, b, a, currentFontKey);
    }
    public void drawText(String text, float x, float y, float r, float g, float b, float a) {
        drawText(text, x, y, 1.0f, r, g, b, a, currentFontKey);
    }

    // 获取字符数据（每次都重新bake，建议优化为成员变量缓存）
    private STBTTBakedChar.Buffer getCharData(FontData fd) {
        STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(CHAR_COUNT);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        stbtt_BakeFontBitmap(fd.fontData, FONT_SIZE, bitmap, BITMAP_W, BITMAP_H, FIRST_CHAR, charData);
        return charData;
    }

    public void destroy() {
        for (FontData fd : fontCache.values()) {
            glDeleteTextures(fd.textureId);
            fd.fontInfo.free();
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