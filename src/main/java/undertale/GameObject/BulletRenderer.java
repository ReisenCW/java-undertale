package undertale.GameObject;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import undertale.GameMain.Game;

// 子弹渲染器, 用于批量渲染子弹以提升性能
public class BulletRenderer {
    public class BulletRenderData {
        public int id;
        public int textureId;
        public float x;
        public float y;
        public float angle;
        public float hScale;
        public float vScale;
        public float width;
        public float height;
        public float[] rgba;
        public boolean isAnimation;

        public BulletRenderData(int id, int textureId, float x, float y, float angle, float hScale, float vScale, float width, float height, float[] rgba, boolean isAnimation) {
            this.id = id;
            this.textureId = textureId;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.hScale = hScale;
            this.vScale = vScale;
            this.width = width;
            this.height = height;
            this.rgba = rgba;
            this.isAnimation = isAnimation;
        }
    }

    private ArrayList<BulletRenderData> renderDataList;
    // GL objects
    private int vaoId;
    private int vboId;
    private int shaderProgram;
    private int locScreenSize;
    private int locColor;
    private int locTexture;

    public BulletRenderer() {
        renderDataList = new ArrayList<>();
        initGLResources();
    }
    public void addBulletRenderData(BulletRenderData data) {
        renderDataList.add(data);
    }

    public void dispose() {
        if (shaderProgram != 0) {
            glDeleteProgram(shaderProgram);
            shaderProgram = 0;
        }
        if (vboId != 0) {
            glDeleteBuffers(vboId);
            vboId = 0;
        }
        if (vaoId != 0) {
            glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }
    }

    private void initGLResources() {
        // Create VAO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create VBO
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        // layout: vec2 aPos (pixels), vec2 aTex
        int stride = (2 + 2) * Float.BYTES;
        // position attrib
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        // texcoord attrib
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        shaderProgram = createShaderProgram();
        // cache uniform locations
        locScreenSize = glGetUniformLocation(shaderProgram, "uScreenSize");
        locColor = glGetUniformLocation(shaderProgram, "uColor");
        locTexture = glGetUniformLocation(shaderProgram, "uTexture");
    }

    private int createShaderProgram() {
        // Vertex shader: convert pixel-space positions to NDC using screen size uniform
        String vertexSrc = "#version 330 core\n"
                + "layout(location = 0) in vec2 aPos;\n"
                + "layout(location = 1) in vec2 aTex;\n"
                + "out vec2 vTex;\n"
                + "uniform ivec2 uScreenSize;\n"
                + "void main() {\n"
                + "    // convert from pixel coords (origin top-left) to NDC (-1..1)\n"
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

    public void clearBulletRenderData() {
        renderDataList.clear();
    }

    public void renderBullets() {
        // 分离Animation和非Animation的bullet
        ArrayList<BulletRenderData> staticBullets = new ArrayList<>();
        ArrayList<BulletRenderData> animatedBullets = new ArrayList<>();
        
        for (BulletRenderData data : renderDataList) {
            if (data.isAnimation) {
                animatedBullets.add(data);
            } else {
                staticBullets.add(data);
            }
        }

        // 批量渲染静态texture的bullet
        renderStaticBullets(staticBullets);
        
        // 单独渲染Animation的bullet
        renderAnimatedBullets(animatedBullets);
    }

    private void renderStaticBullets(ArrayList<BulletRenderData> staticBullets) {
        // 按纹理ID分组渲染数据
        ArrayList<ArrayList<BulletRenderData>> groupedData = new ArrayList<>();
        for (BulletRenderData data : staticBullets) {
            int textureId = data.textureId;
            while (groupedData.size() <= textureId) {
                groupedData.add(new ArrayList<>());
            }
            groupedData.get(textureId).add(data);
        }

        // 对每个纹理组进行渲染
        for (int textureId = 0; textureId < groupedData.size(); textureId++) {
            ArrayList<BulletRenderData> dataList = groupedData.get(textureId);
            if (dataList.isEmpty()) continue;

            // 按ID排序以确保渲染顺序稳定
            // dataList.sort(Comparator.comparingInt(data -> data.id));
            dataList.sort((data1, data2) -> Float.compare(data2.y, data1.y));

            // Batch upload all quads for this texture into the VBO and draw once
            int quadCount = dataList.size();
            // each quad -> 6 vertices, each vertex -> 4 floats (x,y,u,v)
            FloatBuffer buf = BufferUtils.createFloatBuffer(quadCount * 6 * 4);

            for (int i = 0; i < dataList.size(); i++) {
                BulletRenderData data = dataList.get(i);
                float width = data.hScale * data.width;
                float height = data.vScale * data.height;
                float cx = data.x + width / 2.0f;
                float cy = data.y + height / 2.0f;
                double rad = Math.toRadians(data.angle);
                float cos = (float)Math.cos(rad);
                float sin = (float)Math.sin(rad);

                // local corners relative to center
                float lx0 = -width/2f; float ly0 = -height/2f; // top-left
                float lx1 =  width/2f; float ly1 = -height/2f; // top-right
                float lx2 =  width/2f; float ly2 =  height/2f; // bottom-right
                float lx3 = -width/2f; float ly3 =  height/2f; // bottom-left

                float[] px = new float[4];
                float[] py = new float[4];
                // rotate and translate
                float rx0 = lx0 * cos - ly0 * sin; float ry0 = lx0 * sin + ly0 * cos;
                float rx1 = lx1 * cos - ly1 * sin; float ry1 = lx1 * sin + ly1 * cos;
                float rx2 = lx2 * cos - ly2 * sin; float ry2 = lx2 * sin + ly2 * cos;
                float rx3 = lx3 * cos - ly3 * sin; float ry3 = lx3 * sin + ly3 * cos;
                px[0] = cx + rx0; py[0] = cy + ry0; // top-left
                px[1] = cx + rx1; py[1] = cy + ry1; // top-right
                px[2] = cx + rx2; py[2] = cy + ry2; // bottom-right
                px[3] = cx + rx3; py[3] = cy + ry3; // bottom-left

                // two triangles: (0,1,2) and (0,2,3)
                // tri 1
                buf.put(px[0]); buf.put(py[0]); buf.put(0.0f); buf.put(0.0f);
                buf.put(px[1]); buf.put(py[1]); buf.put(1.0f); buf.put(0.0f);
                buf.put(px[2]); buf.put(py[2]); buf.put(1.0f); buf.put(1.0f);
                // tri 2
                buf.put(px[0]); buf.put(py[0]); buf.put(0.0f); buf.put(0.0f);
                buf.put(px[2]); buf.put(py[2]); buf.put(1.0f); buf.put(1.0f);
                buf.put(px[3]); buf.put(py[3]); buf.put(0.0f); buf.put(1.0f);
            }

            buf.flip();

            // upload
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);

            // use shader
            glUseProgram(shaderProgram);
            // set screen size uniform using actual window size to match Texture system
            glUniform2i(locScreenSize, Game.getWindowWidth(), Game.getWindowHeight());
            // set color uniform (white, tint will come from uColor per-batch if needed)
            glUniform4f(locColor, 1f, 1f, 1f, 1f);
            // bind texture to unit 0
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);
            glUniform1i(locTexture, 0);

            glBindVertexArray(vaoId);
            // draw
            glDrawArrays(GL_TRIANGLES, 0, quadCount * 6);
            glBindVertexArray(0);

            glUseProgram(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        // nothing to reset in core pipeline; shader-based color reset not required here
    }

    private void renderAnimatedBullets(ArrayList<BulletRenderData> animatedBullets) {
        // 按id排序以确保渲染顺序稳定
        animatedBullets.sort(Comparator.comparingInt(data -> data.id));

        // For animated bullets we draw them individually (they are likely few)
        for (BulletRenderData data : animatedBullets) {
            float width = data.hScale * data.width;
            float height = data.vScale * data.height;
            float cx = data.x + width / 2.0f;
            float cy = data.y + height / 2.0f;
            double rad = Math.toRadians(data.angle);
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

            FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 4);
            // tri1
            buf.put(px0); buf.put(py0); buf.put(0f); buf.put(0f);
            buf.put(px1); buf.put(py1); buf.put(1f); buf.put(0f);
            buf.put(px2); buf.put(py2); buf.put(1f); buf.put(1f);
            // tri2
            buf.put(px0); buf.put(py0); buf.put(0f); buf.put(0f);
            buf.put(px2); buf.put(py2); buf.put(1f); buf.put(1f);
            buf.put(px3); buf.put(py3); buf.put(0f); buf.put(1f);
            buf.flip();

            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);

            glUseProgram(shaderProgram);
            // use actual window size so pixel -> NDC conversion matches the rest of the renderer
            glUniform2i(locScreenSize, Game.getWindowWidth(), Game.getWindowHeight());
            glUniform4f(locColor, data.rgba[0], data.rgba[1], data.rgba[2], data.rgba[3]);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, data.textureId);
            glUniform1i(locTexture, 0);

            glBindVertexArray(vaoId);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);

            glUseProgram(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }
}
