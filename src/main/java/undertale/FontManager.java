package undertale;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;

import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTAlignedQuad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class FontManager {
    private static FontManager instance;
    private int fontTextureId;
    private STBTTFontinfo fontInfo;
    private ByteBuffer fontData;
    private final int BITMAP_W = 512, BITMAP_H = 512;
    private final int FONT_SIZE = 32;
    private final int FIRST_CHAR = 32, CHAR_COUNT = 96; // ASCII 32~127

    private float[] charWidths = new float[CHAR_COUNT];

    private FontManager() {
        loadFont("PixelOperator-Bold.ttf");
    }

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    private void loadFont(String filePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) throw new IOException("Font file not found: " + filePath);
            byte[] bytes = is.readAllBytes();
            fontData = BufferUtils.createByteBuffer(bytes.length);
            fontData.put(bytes).flip();

            fontInfo = STBTTFontinfo.create();
            if (!stbtt_InitFont(fontInfo, fontData)) throw new RuntimeException("Failed to init font");

            ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(CHAR_COUNT);

            stbtt_BakeFontBitmap(fontData, FONT_SIZE, bitmap, BITMAP_W, BITMAP_H, FIRST_CHAR, charData);

            // 记录每个字符宽度
            for (int i = 0; i < CHAR_COUNT; i++) {
                charWidths[i] = charData.get(i).xadvance();
            }

            fontTextureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, fontTextureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            charData.free();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawText(String text, float x, float y, float r, float g, float b, float a) {
        glBindTexture(GL_TEXTURE_2D, fontTextureId);
        glColor4f(r, g, b, a);

        float xpos = x;
        glBegin(GL_QUADS);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) continue;
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc();
            stbtt_GetBakedQuad(getCharData(), BITMAP_W, BITMAP_H, c - FIRST_CHAR, new float[]{xpos}, new float[]{y}, quad, true);

            glTexCoord2f(quad.s0(), quad.t0());
            glVertex2f(quad.x0(), quad.y0());
            glTexCoord2f(quad.s1(), quad.t0());
            glVertex2f(quad.x1(), quad.y0());
            glTexCoord2f(quad.s1(), quad.t1());
            glVertex2f(quad.x1(), quad.y1());
            glTexCoord2f(quad.s0(), quad.t1());
            glVertex2f(quad.x0(), quad.y1());

            xpos += charWidths[c - FIRST_CHAR];
            quad.free();
        }
        glEnd();
        glBindTexture(GL_TEXTURE_2D, 0);
        glColor4f(1, 1, 1, 1);
    }

    // 获取字符数据（每次都重新bake，建议优化为成员变量缓存）
    private STBTTBakedChar.Buffer getCharData() {
        STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(CHAR_COUNT);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        stbtt_BakeFontBitmap(fontData, FONT_SIZE, bitmap, BITMAP_W, BITMAP_H, FIRST_CHAR, charData);
        return charData;
    }

    public void destroy() {
        glDeleteTextures(fontTextureId);
        fontInfo.free();
    }

    public float getCharWidth(char c) {
        if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) return 0;
        return charWidths[c - FIRST_CHAR];
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