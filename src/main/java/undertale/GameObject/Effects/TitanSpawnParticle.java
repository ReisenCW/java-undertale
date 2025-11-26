package undertale.GameObject.Effects;

import static org.lwjgl.opengl.GL20.*;

import undertale.GameMain.Game;
import undertale.GameObject.GameObject;
import undertale.Texture.Texture;
import undertale.Texture.TextureBuilder;

public class TitanSpawnParticle extends GameObject {
    private float speed;
    private float speedAngle;
    private float selfAngle;
    private Texture tpTexture;
    private float currentScale;
    private float initialScale = 0.6f;
    private float targetScale = 0.0f;
    private float scaleSpeed;
    private float duration = 0.5f; // 0.5秒内消失
    private float elapsedTime = 0.0f;
    private float rotationSpeed = (float) (Math.random() * 360); // 随机旋转

    public TitanSpawnParticle(float x, float y, float angleDeg) {
        this.x = x;
        this.y = y;
        this.currentScale = initialScale;
        this.scaleSpeed = (initialScale - targetScale) / duration;
        this.speedAngle = angleDeg;
        this.selfAngle = angleDeg;
        this.speed = 100.0f;
        init();
    }

    private void init() {
        try {
            tpTexture = Game.getTexture("tension_point");
        } catch (Exception e) {
            // Game.textureManager may be unavailable during unit tests; leave texture null
            tpTexture = null;
        }
    }

    @Override
    public void update(float deltaTime) {
        elapsedTime += deltaTime;
        if (elapsedTime >= duration) {
            // 标记为移除
            return;
        }

        // 缩放
        currentScale -= scaleSpeed * deltaTime;
        if (currentScale < targetScale) {
            currentScale = targetScale;
        }

        // 旋转
        selfAngle += rotationSpeed * deltaTime;

        // 移动
        float rad = (float) Math.toRadians(speedAngle);
        x += (float) Math.cos(rad) * speed * deltaTime;
        y += (float) Math.sin(rad) * speed * deltaTime;
    }

    @Override
    public void render() {
        if (tpTexture != null && currentScale > 0) {
            // 黄白色: RGB(1.0, 1.0, 0.7)
            new TextureBuilder().textureId(tpTexture.getId())
                .position(x, y)
                .size(currentScale * tpTexture.getWidth(), currentScale * tpTexture.getHeight())
                .rotation(selfAngle)
                .rgba(1.0f, 1.0f, 0.7f, 1.0f)
                .shaderName("tp_shader")
                .uniformSetter(program -> {
                    int locScreenSize = glGetUniformLocation(program, "uScreenSize");
                    int locColor = glGetUniformLocation(program, "uColor");
                    int locTexture = glGetUniformLocation(program, "uTexture");
                    int locWhiteStrength = glGetUniformLocation(program, "uWhiteStrength");
                    glUniform2i(locScreenSize, Game.getWindowWidth(), Game.getWindowHeight());
                    glUniform4f(locColor, 1.0f, 1.0f, 0.7f, 1.0f);
                    glUniform1i(locTexture, 0);
                    glUniform1f(locWhiteStrength, (currentScale - targetScale) / (initialScale - targetScale));
                })
                .draw();
        }
    }

    public boolean isActive() {
        return elapsedTime < duration;
    }

    @Override
    public float getWidth() {
        return tpTexture != null ? currentScale * tpTexture.getWidth() : 0f;
    }

    @Override
    public float getHeight() {
        return tpTexture != null ? currentScale * tpTexture.getHeight() : 0f;
    }
}