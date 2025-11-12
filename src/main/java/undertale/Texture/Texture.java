package undertale.Texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import undertale.GameMain.Game;
import undertale.Shaders.ShaderManager;

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
import java.util.function.Consumer;

public class Texture {
    private int id;
    private int width;
    private int height;

    // Shared rendering resources for textured quads (lazy init)
    private static int quadVao = 0;
    private static int quadVbo = 0;
    private static int whiteTextureId = 0; // 1x1 white texture for color-only draws
    private static boolean glInitialized = false;
    private static int screenWidth = Game.getWindowWidth();
    private static int screenHeight = Game.getWindowHeight();
    private static ShaderManager shaderManager = ShaderManager.getInstance();

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
    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation, float r, float g, float b, float a, boolean horizontalReverse, boolean verticalReverse, String shaderName, Consumer<Integer> uniformSetter) {
        ensureGLInitialized();

        // 中心点坐标
        float cx = x + width / 2.0f;
        float cy = y + height / 2.0f;
        // 旋转角对应sin和cos值
        double rad = Math.toRadians(rotation);
        float cos = (float)Math.cos(rad);
        float sin = (float)Math.sin(rad);

        // 四角相对于中心点的相对坐标
        float lx0 = -width/2f; float ly0 = -height/2f; // top-left
        float lx1 =  width/2f; float ly1 = -height/2f; // top-right
        float lx2 =  width/2f; float ly2 =  height/2f; // bottom-right
        float lx3 = -width/2f; float ly3 =  height/2f; // bottom-left

        // 旋转后的相对坐标
        float rx0 = lx0 * cos - ly0 * sin; float ry0 = lx0 * sin + ly0 * cos;
        float rx1 = lx1 * cos - ly1 * sin; float ry1 = lx1 * sin + ly1 * cos;
        float rx2 = lx2 * cos - ly2 * sin; float ry2 = lx2 * sin + ly2 * cos;
        float rx3 = lx3 * cos - ly3 * sin; float ry3 = lx3 * sin + ly3 * cos;

        // 最终顶点坐标
        float px0 = cx + rx0; float py0 = cy + ry0;
        float px1 = cx + rx1; float py1 = cy + ry1;
        float px2 = cx + rx2; float py2 = cy + ry2;
        float px3 = cx + rx3; float py3 = cy + ry3;

        // 水平方向起点
        float u0 = horizontalReverse ? 1.0f : 0.0f;
        // 水平方向终点
        float u1 = horizontalReverse ? 0.0f : 1.0f;
        // 垂直方向起点
        float v0 = verticalReverse ? 0.0f : 1.0f;
        // 垂直方向终点
        float v1 = verticalReverse ? 1.0f : 0.0f;

        // 存储6个顶点(2个三角形, 每个三角形3个顶点), 每个顶点4个数据(x,y,u,v)
        FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 4);
        // triangle1
        buf.put(px0); buf.put(py0); buf.put(u0); buf.put(v1); // 左上
        buf.put(px1); buf.put(py1); buf.put(u1); buf.put(v1); // 右上
        buf.put(px2); buf.put(py2); buf.put(u1); buf.put(v0); // 右下
        // triangle2
        buf.put(px0); buf.put(py0); buf.put(u0); buf.put(v1); // 左上
        buf.put(px2); buf.put(py2); buf.put(u1); buf.put(v0); // 右下
        buf.put(px3); buf.put(py3); buf.put(u0); buf.put(v0); // 左下
        // 切换缓冲区为读模式
        buf.flip();

        renderBuffer(buf, 1, textureId, r, g, b, a, shaderName, uniformSetter);
    }

    public static void drawTexture(int textureId, float x, float y, float width, float height, float rotation, float r, float g, float b, float a, boolean horizontalReverse, boolean verticalReverse) {
        drawTexture(textureId, x, y, width, height, rotation, r, g, b, a, horizontalReverse, verticalReverse, "texture_shader", program -> {
            int locScreenSize = glGetUniformLocation(program, "uScreenSize");
            int locColor = glGetUniformLocation(program, "uColor");
            int locTexture = glGetUniformLocation(program, "uTexture");
            glUniform2i(locScreenSize, screenWidth, screenHeight);
            glUniform4f(locColor, r, g, b, a);
            glUniform1i(locTexture, 0);
        });
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
        // 用白色1x1纹理绘制四条边
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
        renderTriangles(buf, segment * 3, whiteTextureId, r, g, b, a, "texture_shader", program -> {
            int locScreenSize = glGetUniformLocation(program, "uScreenSize");
            int locColor = glGetUniformLocation(program, "uColor");
            int locTexture = glGetUniformLocation(program, "uTexture");
            glUniform2i(locScreenSize, screenWidth, screenHeight);
            glUniform4f(locColor, r, g, b, a);
            glUniform1i(locTexture, 0);
        });
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
        renderTriangles(buf, segment * 6, whiteTextureId, r, g, b, a, "texture_shader", program -> {
            int locScreenSize = glGetUniformLocation(program, "uScreenSize");
            int locColor = glGetUniformLocation(program, "uColor");
            int locTexture = glGetUniformLocation(program, "uTexture");
            glUniform2i(locScreenSize, screenWidth, screenHeight);
            glUniform4f(locColor, r, g, b, a);
            glUniform1i(locTexture, 0);
        });
    }

    public static void drawHollowCircle(float x, float y, float radius, float r, float g, float b, float a) {
        drawHollowCircle(x, y, radius, r, g, b, a, 36, 1.0f);
    }

    public static void drawRect(float x, float y, float width, float height) {
        drawRect(x, y, width, height, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 追加一个旋转矩形的顶点数据到 buf 中
     * @param buf 目标缓冲区
     * @param x 矩形左上角X坐标
     * @param y 矩形左上角Y坐标
     * @param width 矩形宽度
     * @param height 矩形高度
     * @param rotation 矩形旋转角度（度）
     * @param u0 纹理坐标左上角U(u = 0时为左边, u = 1时为右边)
     * @param v0 纹理坐标左上角V(v = 0时为上边, v = 1时为下边)
     * @param u1 纹理坐标右下角U
     * @param v1 纹理坐标右下角V
     */
    public static void appendQuad(FloatBuffer buf, float x, float y, float width, float height, float rotation, float u0, float v0, float u1, float v1) {
        // center
        float cx = x + width / 2.0f;
        float cy = y + height / 2.0f;
        double rad = Math.toRadians(rotation);
        float cos = (float)Math.cos(rad);
        float sin = (float)Math.sin(rad);

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

        // tri1: 0,1,2
        buf.put(px0); buf.put(py0); buf.put(u0); buf.put(v0);
        buf.put(px1); buf.put(py1); buf.put(u1); buf.put(v0);
        buf.put(px2); buf.put(py2); buf.put(u1); buf.put(v1);
        // tri2: 0,2,3
        buf.put(px0); buf.put(py0); buf.put(u0); buf.put(v0);
        buf.put(px2); buf.put(py2); buf.put(u1); buf.put(v1);
        buf.put(px3); buf.put(py3); buf.put(u0); buf.put(v1);
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

        // create a 1x1 white texture for color-only drawing (used for shapes)
        whiteTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, whiteTextureId);
        ByteBuffer whitePixel = BufferUtils.createByteBuffer(4);
        whitePixel.put((byte)255).put((byte)255).put((byte)255).put((byte)255).flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, whitePixel);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        glInitialized = true;
    }

    /**
     * 绘制三角形列表
     * @param buf 包含三角形顶点数据的缓冲区
     * @param vertexCount 顶点数量
     */
    private static void renderTriangles(FloatBuffer buf, int vertexCount, int textureId, float r, float g, float b, float a, String shaderName, Consumer<Integer> uniformSetter) {
        ensureGLInitialized();

        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);

        int program = shaderManager.getProgram(shaderName);
        glUseProgram(program);
        uniformSetter.accept(program);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);

        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * 渲染多个四边形
     * @param buf 包含多个四边形顶点数据的缓冲区
     * @param quadCount 四边形数量
     */
    private static void renderBuffer(FloatBuffer buf, int quadCount, int textureId, float r, float g, float b, float a, String shaderName, Consumer<Integer> uniformSetter) {
        ensureGLInitialized();

        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);

        int program = shaderManager.getProgram(shaderName);
        glUseProgram(program);
        uniformSetter.accept(program);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, quadCount * 6);
        glBindVertexArray(0);

        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * 绘制多个四边形, 等同于私有方法 renderBuffer
     * @param buf 包含多个四边形顶点数据的缓冲区
     * @param quadCount 四边形数量
     */
    public static void drawQuads(FloatBuffer buf, int quadCount, int textureId, float r, float g, float b, float a) {
        renderBuffer(buf, quadCount, textureId, r, g, b, a, "texture_shader", program -> {
            int locScreenSize = glGetUniformLocation(program, "uScreenSize");
            int locColor = glGetUniformLocation(program, "uColor");
            int locTexture = glGetUniformLocation(program, "uTexture");
            glUniform2i(locScreenSize, screenWidth, screenHeight);
            glUniform4f(locColor, r, g, b, a);
            glUniform1i(locTexture, 0);
        });
    }
}
