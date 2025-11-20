package undertale.GameObject.Bullets;

import undertale.GameObject.CollisionDetector;
import undertale.GameObject.Player;
import undertale.Texture.Texture;
import undertale.Texture.TextureManager;

import java.util.ArrayList;
import java.util.List;

public class BallBlast extends Bullet {
    private float scaleChangeInterval;
    private float baseScale;
    private float whiteScale;
    private float amp;

    private float scaleTimer = 0f;

    private Texture whiteEdgeTexture;

    private static class BallTrail {
        float x, y, alpha;
        BallTrail(float x, float y, float alpha) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }
    }

    private List<BallTrail> ballTrails = new ArrayList<>();
    private float trailSpawnTimer = 0;
    private float trailSpawnInterval = 0.15f;
    private float trailFadeSpeed = 6.0f;

    public BallBlast(float x, float y, float speed, float speedAngle, float baseScale, float scaleChangeInterval, float amp, int damage) {
        super(x, y, speedAngle, speedAngle, speed, damage, TextureManager.getInstance().getTexture("titan_blast_black"));
        whiteEdgeTexture = TextureManager.getInstance().getTexture("titan_blast");
        this.scaleChangeInterval = scaleChangeInterval;
        this.baseScale = baseScale;
        this.whiteScale = baseScale;
        this.setVScale(baseScale);
        this.setHScale(baseScale);
        this.amp = amp;
        this.destroyableOnHit = false;
        this.bound = false;
    }

    @Override
    public void update(float deltaTime) {
        // 等待后开始移动
        updatePosition(deltaTime);

        scaleTimer += deltaTime;
        if(scaleTimer >= scaleChangeInterval) {
            scaleTimer -= scaleChangeInterval;
            changeScale();
        }

        // 添加残影
        trailSpawnTimer += deltaTime;
        if (trailSpawnTimer >= trailSpawnInterval) {
            trailSpawnTimer -= trailSpawnInterval;
            ballTrails.add(new BallTrail(this.x, this.y, 1.0f));
        }
        // 更新残影
        for (BallTrail trail : ballTrails) {
            trail.alpha -= trailFadeSpeed * deltaTime;
        }
        ballTrails.removeIf(trail -> trail.alpha <= 0);
    }

    enum ScaleState {
        LARGE,
        SMALL
    }
    private ScaleState currentScaleState = ScaleState.SMALL;
    private void changeScale() {
        if(currentScaleState == ScaleState.SMALL) {
            // 变大
            whiteScale = baseScale + amp * 2;
            setHScale(baseScale + amp);
            setVScale(baseScale + amp);
            currentScaleState = ScaleState.LARGE;
        } else {
            // 变小
            whiteScale = baseScale;
            setHScale(baseScale);
            setVScale(baseScale);
            currentScaleState = ScaleState.SMALL;
        }
    }

    @Override
    public boolean hasAnimation() {
        return true;
    }

    @Override
    public void render() {
        // 绘制残影
        for (BallTrail trail : ballTrails) {
            float trailWhiteX = trail.x + this.getWidth() / 2 - whiteEdgeTexture.getWidth() / 2 * whiteScale;
            float trailWhiteY = trail.y + this.getHeight() / 2 - whiteEdgeTexture.getHeight() / 2 * whiteScale;
            // 白色边缘
            Texture.drawTexture(whiteEdgeTexture.getId(), trailWhiteX, trailWhiteY, whiteEdgeTexture.getWidth() * whiteScale, whiteEdgeTexture.getHeight() * whiteScale, this.getSelfAngle(), 1.0f, 1.0f, 1.0f, trail.alpha);
            // 黑色主体
            Texture.drawTexture(getTexture().getId(), trail.x, trail.y, this.getWidth(), this.getHeight(), this.getSelfAngle(), 0.0f, 0.0f, 0.0f, trail.alpha);
        }
        // 绘制当前
        float whiteX = this.x + this.getWidth() / 2 - whiteEdgeTexture.getWidth() / 2 * whiteScale;
        float whiteY = this.y + this.getHeight() / 2 - whiteEdgeTexture.getHeight() / 2 * whiteScale;
        // 先渲染白色边缘
        Texture.drawTexture(whiteEdgeTexture.getId(), whiteX, whiteY, whiteEdgeTexture.getWidth() * whiteScale, whiteEdgeTexture.getHeight() * whiteScale, this.getSelfAngle(), 1.0f, 1.0f, 1.0f, 1.0f);
        // 再渲染黑色主体
        super.render();
    }

    @Override
    public boolean checkCollisionWithPlayer(Player player) {
        if (!player.isAlive() || !this.isColli) {
            return false;
        }
        return CollisionDetector.checkCircleCollision(this, player, 0);
    }
}