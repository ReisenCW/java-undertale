package undertale.Texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

public class Texture {
    private int id;
    private int width;
    private int height;

    // Shared rendering resources for textured quads (lazy init)
    private static int quadVao = 0;
    private static int quadVbo = 0;
    private static int quadShaderProgram = 0;
    private static int locScreenSize = -1;
    private static int locColor = -1;
    private static int locTexture = -1;
    private static int whiteTextureId = 0; // 1x1 white texture for color-only draws
    private static boolean glInitialized = false;
    private static int screenWidth = 800;
    private static int screenHeight = 600;

    public Texture(String resourcePath, int filterType) {
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
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterType);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterType);

            STBImage.stbi_image_free(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Texture(String resourcePath) {
        this(resourcePath, GL_NEAREST);
    }

    /**
     * 绘制纹理
     * 颜色为相乘模式
     */
    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation, float r, float g, float b, float a, boolean horizontalReverse, boolean verticalReverse) {
        ensureGLInitialized();

        // build quad (two triangles) into a small buffer
        float cx = x + width / 2.0f;
        float cy = y + height / 2.0f;
        double rad = Math.toRadians(rotation);
        float cos = (float)Math.cos(rad);
        float sin = (float)Math.sin(rad);

        // local corners relative to center
        float lx0 = -width/2f; float ly0 = -height/2f; // top-left
        float lx1 =  width/2f; float ly1 = -height/2f; // top-right
        float lx2 =  width/2f; float ly2 =  height/2f; // bottom-right
        float lx3 = -width/2f; float ly3 =  height/2f; // bottom-left

        float rx0 = lx0 * cos - ly0 * sin; float ry0 = lx0 * sin + ly0 * cos;
        float rx1 = lx1 * cos - ly1 * sin; float ry1 = lx1 * sin + ly1 * cos;
        float rx2 = lx2 * cos - ly2 * sin; float ry2 = lx2 * sin + ly2 * cos;
        float rx3 = lx3 * cos - ly3 * sin; float ry3 = lx3 * sin + ly3 * cos;

        float px0 = cx + rx0; float py0 = cy + ry0;
        float px1 = cx + rx1; float py1 = cy + ry1;
        float px2 = cx + rx2; float py2 = cy + ry2;
        float px3 = cx + rx3; float py3 = cy + ry3;

        float u0 = horizontalReverse ? 1.0f : 0.0f;
        float u1 = horizontalReverse ? 0.0f : 1.0f;
        float v0 = verticalReverse ? 0.0f : 1.0f;
        float v1 = verticalReverse ? 1.0f : 0.0f;

        FloatBuffer buf = org.lwjgl.BufferUtils.createFloatBuffer(6 * 4);
        // tri1
        buf.put(px0); buf.put(py0); buf.put(u0); buf.put(v1);
        buf.put(px1); buf.put(py1); buf.put(u1); buf.put(v1);
        buf.put(px2); buf.put(py2); buf.put(u1); buf.put(v0);
        // tri2
        buf.put(px0); buf.put(py0); buf.put(u0); buf.put(v1);
        buf.put(px2); buf.put(py2); buf.put(u1); buf.put(v0);
        buf.put(px3); buf.put(py3); buf.put(u0); buf.put(v0);
        buf.flip();

        renderBuffer(buf, 1, textureId, r, g, b, a);
    }

    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation, float r, float g, float b, float a){
        drawTexture(textureId, x, y, width, height, rotation, r, g, b, a, false, false);
    }

    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation, boolean horizontalReverse, boolean verticalReverse) {
        drawTexture(textureId, x, y, width, height, rotation, 1.0f, 1.0f, 1.0f, 1.0f, horizontalReverse, verticalReverse);
    }

    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation) { 
        drawTexture(textureId, x, y, width, height, rotation, false, false);
    }

    public static void drawTexture(int textureId, float x, float y, float width, float height) {
        drawTexture(textureId, x, y, width, height, 0);
    }

    public static void drawHollowRect(float x, float y, float width, float height, float r, float g, float b, float a, float lineWidth) {
        // Draw hollow rect using 4 thin quads (core-profile friendly)
        ensureGLInitialized();
        // top
        drawTexture(whiteTextureId, x, y, width, lineWidth, 0, r, g, b, a);
        // bottom
        drawTexture(whiteTextureId, x, y + height - lineWidth, width, lineWidth, 0, r, g, b, a);
        // left
        drawTexture(whiteTextureId, x, y, lineWidth, height, 0, r, g, b, a);
        // right
        drawTexture(whiteTextureId, x + width - lineWidth, y, lineWidth, height, 0, r, g, b, a);
    }

    public static void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
        // Draw filled rect using the white 1x1 texture and quad renderer
        ensureGLInitialized();
        drawTexture(whiteTextureId, x, y, width, height, 0.0f, r, g, b, a);
    }

    // 绘制实心圆
    public static void drawCircle(float x, float y, float radius, float r, float g, float b, float a, int segment) {
        // Build triangle list for filled circle and render via textured shader with white texture
        ensureGLInitialized();
        // each triangle is center, v_i, v_{i+1} -> 3 vertices per segment
        FloatBuffer buf = org.lwjgl.BufferUtils.createFloatBuffer(segment * 3 * 4);
        // center vertex (will be duplicated per triangle)
        for (int i = 0; i < segment; i++) {
            double a1 = 2.0 * Math.PI * i / segment;
            double a2 = 2.0 * Math.PI * (i + 1) / segment;
            float x1 = x + (float)(radius * Math.cos(a1));
            float y1 = y + (float)(radius * Math.sin(a1));
            float x2 = x + (float)(radius * Math.cos(a2));
            float y2 = y + (float)(radius * Math.sin(a2));
            // triangle (center, v1, v2)
            buf.put(x); buf.put(y); buf.put(0.5f); buf.put(0.5f);
            buf.put(x1); buf.put(y1); buf.put(0.5f); buf.put(0.5f);
            buf.put(x2); buf.put(y2); buf.put(0.5f); buf.put(0.5f);
        }
        buf.flip();
        renderTriangles(buf, segment * 3, whiteTextureId, r, g, b, a);
    }

    public static void drawCircle(float x, float y, float radius, float r, float g, float b, float a) {
        drawCircle(x, y, radius, r, g, b, a, 36);
    }

    // 绘制空心圆（线圈）
    public static void drawHollowCircle(float x, float y, float radius, float r, float g, float b, float a, int segment, float lineWidth) {
        // Render a ring between outer radius and inner radius = radius - lineWidth
        ensureGLInitialized();
        float innerR = Math.max(0.0f, radius - lineWidth);
        // each segment produces two triangles => 6 vertices per segment
        FloatBuffer buf = org.lwjgl.BufferUtils.createFloatBuffer(segment * 6 * 4);
        for (int i = 0; i < segment; i++) {
            double a1 = 2.0 * Math.PI * i / segment;
            double a2 = 2.0 * Math.PI * (i + 1) / segment;
            float ox1 = x + (float)(radius * Math.cos(a1));
            float oy1 = y + (float)(radius * Math.sin(a1));
            float ox2 = x + (float)(radius * Math.cos(a2));
            float oy2 = y + (float)(radius * Math.sin(a2));
            float ix1 = x + (float)(innerR * Math.cos(a1));
            float iy1 = y + (float)(innerR * Math.sin(a1));
            float ix2 = x + (float)(innerR * Math.cos(a2));
            float iy2 = y + (float)(innerR * Math.sin(a2));
            // tri 1: ox1, ix1, ox2
            buf.put(ox1); buf.put(oy1); buf.put(0.5f); buf.put(0.5f);
            buf.put(ix1); buf.put(iy1); buf.put(0.5f); buf.put(0.5f);
            buf.put(ox2); buf.put(oy2); buf.put(0.5f); buf.put(0.5f);
            // tri 2: ox2, ix1, ix2
            buf.put(ox2); buf.put(oy2); buf.put(0.5f); buf.put(0.5f);
            buf.put(ix1); buf.put(iy1); buf.put(0.5f); buf.put(0.5f);
            buf.put(ix2); buf.put(iy2); buf.put(0.5f); buf.put(0.5f);
        }
        buf.flip();
        renderTriangles(buf, segment * 6, whiteTextureId, r, g, b, a);
    }

    public static void drawHollowCircle(float x, float y, float radius, float r, float g, float b, float a) {
        drawHollowCircle(x, y, radius, r, g, b, a, 36, 1.0f);
    }

    public static void drawRect(float x, float y, float width, float height) {
        drawRect(x, y, width, height, 1.0f, 1.0f, 1.0f, 1.0f);
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

    public void destroy() {
        glDeleteTextures(id);
    }

    public static void setScreenSize(int w, int h) {
        screenWidth = Math.max(1, w);
        screenHeight = Math.max(1, h);
    }

    private static void ensureGLInitialized() {
        if (glInitialized) return;
        // create VAO/VBO and shader
        quadVao = glGenVertexArrays();
        glBindVertexArray(quadVao);

        quadVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);

        int stride = (2 + 2) * Float.BYTES;
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        quadShaderProgram = createShaderProgram();
        locScreenSize = glGetUniformLocation(quadShaderProgram, "uScreenSize");
        locColor = glGetUniformLocation(quadShaderProgram, "uColor");
        locTexture = glGetUniformLocation(quadShaderProgram, "uTexture");

    // create a 1x1 white texture for color-only drawing (used for shapes)
    whiteTextureId = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, whiteTextureId);
    java.nio.ByteBuffer whitePixel = org.lwjgl.BufferUtils.createByteBuffer(4);
    whitePixel.put((byte)255).put((byte)255).put((byte)255).put((byte)255).flip();
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, whitePixel);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glBindTexture(GL_TEXTURE_2D, 0);

        glInitialized = true;
    }

    private static int createShaderProgram() {
        String vertexSrc = "#version 330 core\n"
                + "layout(location = 0) in vec2 aPos;\n"
                + "layout(location = 1) in vec2 aTex;\n"
                + "out vec2 vTex;\n"
                + "uniform ivec2 uScreenSize;\n"
                + "void main() {\n"
                + "    float x = (aPos.x / float(uScreenSize.x)) * 2.0 - 1.0;\n"
                + "    float y = 1.0 - (aPos.y / float(uScreenSize.y)) * 2.0;\n"
                + "    gl_Position = vec4(x, y, 0.0, 1.0);\n"
                + "    vTex = aTex;\n"
                + "}\n";

        String fragmentSrc = "#version 330 core\n"
                + "in vec2 vTex;\n"
                + "out vec4 fragColor;\n"
                + "uniform sampler2D uTexture;\n"
                + "uniform vec4 uColor;\n"
                + "void main() {\n"
                + "    vec4 tex = texture(uTexture, vTex);\n"
                + "    fragColor = tex * uColor;\n"
                + "}\n";

        int vShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vShader, vertexSrc);
        glCompileShader(vShader);
        if (glGetShaderi(vShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex shader compile error: " + glGetShaderInfoLog(vShader));
        }

        int fShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fShader, fragmentSrc);
        glCompileShader(fShader);
        if (glGetShaderi(fShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment shader compile error: " + glGetShaderInfoLog(fShader));
        }

        int prog = glCreateProgram();
        glAttachShader(prog, vShader);
        glAttachShader(prog, fShader);
        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader program link error: " + glGetProgramInfoLog(prog));
        }

        glDeleteShader(vShader);
        glDeleteShader(fShader);

        return prog;
    }

    /**
     * Render generic triangle list where each vertex is (x,y,u,v) floats.
     */
    private static void renderTriangles(FloatBuffer buf, int vertexCount, int textureId, float r, float g, float b, float a) {
        ensureGLInitialized();

        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);

        glUseProgram(quadShaderProgram);
        glUniform2i(locScreenSize, screenWidth, screenHeight);
        glUniform4f(locColor, r, g, b, a);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(locTexture, 0);

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);

        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static void renderBuffer(FloatBuffer buf, int quadCount, int textureId, float r, float g, float b, float a) {
        ensureGLInitialized();

        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);

        glUseProgram(quadShaderProgram);
        glUniform2i(locScreenSize, screenWidth, screenHeight);
        glUniform4f(locColor, r, g, b, a);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(locTexture, 0);

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, quadCount * 6);
        glBindVertexArray(0);

        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Public wrapper so other systems (eg FontManager) can render batched quads.
     */
    public static void drawQuads(FloatBuffer buf, int quadCount, int textureId, float r, float g, float b, float a) {
        renderBuffer(buf, quadCount, textureId, r, g, b, a);
    }
}
