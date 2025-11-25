package undertale.Texture;

import java.util.function.Consumer;

public class TextureBuilder {
    private int textureId;
    private float x;
    private float y;
    private float width;
    private float height;
    private float rotation;
    private float[] u = new float[2];
    private float[] v = new float[2];
    private float[] rgba = new float[4];
    private String shaderName;
    private Consumer<Integer> uniformSetter;

    public TextureBuilder() {
        // 设置默认值
        this.textureId = -1;
        this.x = 0f;
        this.y = 0f;
        this.width = 100f;
        this.height = 100f;
        this.rotation = 0f;
        this.u[0] = 0f;
        this.u[1] = 1f;
        this.v[0] = 1f;
        this.v[1] = 0f;
        this.rgba[0] = 1f;
        this.rgba[1] = 1f;
        this.rgba[2] = 1f;
        this.rgba[3] = 1f;
        this.shaderName = null;
        this.uniformSetter = null;
    }

    public TextureBuilder textureId(int textureId) {
        this.textureId = textureId;
        return this;
    }

    public TextureBuilder position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public TextureBuilder size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public TextureBuilder rotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public TextureBuilder uv(float u0, float u1, float v0, float v1) {
        this.u[0] = u0;
        this.u[1] = u1;
        this.v[0] = v0;
        this.v[1] = v1;
        return this;
    }

    public TextureBuilder rgba(float r, float g, float b, float a) {
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
        return this;
    }

    public TextureBuilder shaderName(String shaderName) {
        this.shaderName = shaderName;
        return this;
    }

    public TextureBuilder uniformSetter(Consumer<Integer> uniformSetter) {
        this.uniformSetter = uniformSetter;
        return this;
    }

    public void draw() {
        Texture.drawTexture(textureId, x, y, width, height, rotation, rgba[0], rgba[1], rgba[2], rgba[3], u[0], v[0], u[1], v[1], shaderName, uniformSetter);
    }
}
