package undertale.GameObject.Effects;

import undertale.Texture.Texture;

public class RippleEffect {
    private float centerX;
    private float centerY;
    private float maxRadius = 20.0f;
    private float duration = 0.4f; // 0.4秒
    private float elapsedTime = 0.0f;
    private boolean isActive = true;

    public RippleEffect(float x, float y) {
        this.centerX = x;
        this.centerY = y;
    }

    public void update(float deltaTime) {
        elapsedTime += deltaTime;
        if (elapsedTime >= duration) {
            isActive = false;
        }
    }

    public void render() {
        if (!isActive) return;

        float progress = elapsedTime / duration; // 0.0 到 1.0
        float currentRadius = maxRadius * progress;
        float alpha = 1.0f - progress; // 从1.0渐变到0.0

        // 金黄色涟漪，线宽根据半径调整
        float lineWidth = Math.max(1.0f, currentRadius * 0.1f);
        Texture.drawHollowCircle(centerX, centerY, currentRadius, 1.0f, 1.0f, 0.0f, alpha, 32, lineWidth);
    }

    public boolean isActive() {
        return isActive;
    }
}