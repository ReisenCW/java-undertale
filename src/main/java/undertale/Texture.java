package undertale;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Texture {
    private int id;
    private int width;
    private int height;

    public Texture(String resourcePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer image;
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new IOException("Resource not found: " + resourcePath);
                }
                byte[] bytes = in.readAllBytes();
                ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                image = STBImage.stbi_load_from_memory(buffer, w, h, comp, 4);
            }

            if (image == null) {
                throw new RuntimeException("Failed to load a texture file! " + STBImage.stbi_failure_reason());
            }

            width = w.get();
            height = h.get();

            id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            STBImage.stbi_image_free(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 绘制纹理
    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation, float r, float g, float b, float a) {

        // 绑定纹理
        glBindTexture(GL_TEXTURE_2D, textureId);
        // 设置颜色和透明度
        glColor4f(r, g, b, a);

        // 以纹理中心为旋转中心
        float cx = x + width / 2.0f;
        float cy = y + height / 2.0f;

        glPushMatrix();
        glTranslatef(cx, cy, 0);
        glRotatef(rotation, 0, 0, 1);
        glTranslatef(-cx, -cy, 0);

        glBegin(GL_QUADS);
        // 左下角
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        // 右下角
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        // 右上角
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        // 左上角
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glEnd();

        glPopMatrix();
    }

    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation) {
        drawTexture(textureId, x, y, width, height, rotation, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawTexture(int textureId, float x, float y, float width, float height) {
        drawTexture(textureId, x, y, width, height, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getId() {
        return id;
    }
}
