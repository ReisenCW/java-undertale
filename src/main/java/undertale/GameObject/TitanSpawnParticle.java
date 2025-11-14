package undertale.GameObject;

import static org.lwjgl.opengl.GL20.*;

import undertale.GameMain.Game;
import undertale.Texture.Texture;

public class TitanSpawnParticle {
    private float x;
    private float y;
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
        tpTexture = Game.getTexture("tension_point");
    }

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

    public void render() {
        if (tpTexture != null && currentScale > 0) {
            // 黄白色: RGB(1.0, 1.0, 0.7)
            Texture.drawTexture(tpTexture.getId(), x, y, currentScale * tpTexture.getWidth(), currentScale * tpTexture.getHeight(), selfAngle, 1.0f, 1.0f, 0.7f, 1.0f, false, false, "tp_shader", program -> {
                int locScreenSize = glGetUniformLocation(program, "uScreenSize");
                int locColor = glGetUniformLocation(program, "uColor");
                int locTexture = glGetUniformLocation(program, "uTexture");
                int locWhiteStrength = glGetUniformLocation(program, "uWhiteStrength");
                glUniform2i(locScreenSize, Game.getWindowWidth(), Game.getWindowHeight());
                glUniform4f(locColor, 1.0f, 1.0f, 0.7f, 1.0f);
                glUniform1i(locTexture, 0);
                glUniform1f(locWhiteStrength, (currentScale - targetScale) / (initialScale - targetScale));
            });
        }
    }

    public boolean isActive() {
        return elapsedTime < duration;
    }
}