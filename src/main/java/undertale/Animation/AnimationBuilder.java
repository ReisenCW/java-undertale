package undertale.Animation;

import java.util.function.Consumer;

public class AnimationBuilder {
    private Animation anim;
    private float x;
    private float y;
    private float scaleX;
    private float scaleY;
    private float rotation;
    private float rgba[] = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
    private String shaderName = null;
    private Consumer<Integer> uniformSetter = null;
    public AnimationBuilder(Animation anim) {
        this.anim = anim;
        this.x = 0f;
        this.y = 0f;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.rotation = 0f;
        this.rgba = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        this.shaderName = null;
        this.uniformSetter = null;
    }

    public AnimationBuilder position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public AnimationBuilder scale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public AnimationBuilder rotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public AnimationBuilder rgba(float r, float g, float b, float a) {
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
        return this;
    }

    public AnimationBuilder shaderName(String shaderName) {
        this.shaderName = shaderName;
        return this;
    }

    public AnimationBuilder uniformSetter(Consumer<Integer> uniformSetter) {
        this.uniformSetter = uniformSetter;
        return this;
    }

    public void draw() {
        anim.renderCurrentFrame(x, y, scaleX, scaleY, rotation, rgba[0], rgba[1], rgba[2], rgba[3], shaderName, uniformSetter);
    }
}
