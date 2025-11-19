package undertale.GameObject.Bullets;

import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Texture.Texture;
import undertale.Texture.TextureManager;
import java.util.ArrayList;
import java.util.List;

public class TitanSwarmRed extends Bullet{
    private Texture redEye;
    private float maxSpeed = 200.0f;
    private float rotateDir = Math.random() < 0.5 ? -1.0f : 1.0f;
    private float rotationSpeed = 150.0f; // degrees per second
    private float appearTime = 1.0f;
    private float appearTimer = 0.0f;
    private float homingTime = 1.0f;
    private float targetScale = 1.5f;

    private float trailSpawnTimer = 0;
    private float trailSpawnInterval = 0.2f;
    private float trailFadeSpeed = 4.0f;

    private class Trail {
        float x, y, angle, alpha;
        Trail(float x, float y, float angle, float alpha) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.alpha = alpha;
        }
    }
    private List<Trail> trails = new ArrayList<>();

    public TitanSwarmRed(float x, float y, int damage) {
        super(x, y, 360 * (float)Math.random(), 0, 0, damage, TextureManager.getInstance().getTexture("spawn_red"));

        redEye = TextureManager.getInstance().getTexture("spawn_red_eye");
        setNavi(false);

        this.destroyableOnHit = false;

        // 初始透明和无缩放
        this.rgba[3] = 0.0f;
        this.isColli = false;
        setHScale(0.0f);
        setVScale(0.0f);

        this.setSpeed(0); // 初始速度 0
    }

    @Override
    public boolean hasAnimation() {
        return true;
    }

    @Override
    public void update(float deltaTime) {
        appearTimer += deltaTime;
        if (appearTimer < appearTime) {
            float t = appearTimer / appearTime;
            rgba[3] = t;
            setHScale(targetScale * t);
            setVScale(targetScale * t);
        } else {
            rgba[3] = 1.0f;
            setHScale(targetScale);
            setVScale(targetScale);
            if (!this.isColli) {
                this.isColli = true;
            }
            if(appearTimer - appearTime < homingTime) {
                // 跟踪 player
                Player player = Game.getPlayer();
                if (player != null) {
                    float px = player.getX() + player.getWidth() / 2.0f;
                    float py = player.getY() + player.getHeight() / 2.0f;
                    float cx = this.x + this.getWidth() / 2.0f;
                    float cy = this.y + this.getHeight() / 2.0f;
                    float dx = px - cx;
                    float dy = py - cy;
                    float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
                    this.setSpeedAngle(angleDeg);
                }
            }
            // 速度变化
            float accelTime = 2.0f;
            float speedT = Math.min(1.0f, (appearTimer - appearTime) / accelTime);
            float currentSpeed = (float) (maxSpeed * Math.sin(Math.PI / 2 * speedT));
            this.setSpeed(currentSpeed);
        }

        updatePosition(deltaTime);

        // 旋转角度
        this.setSelfAngle(this.getSelfAngle() + rotateDir * rotationSpeed * deltaTime);

        // 添加残影
        trailSpawnTimer += deltaTime;
        if (trailSpawnTimer >= trailSpawnInterval) {
            trailSpawnTimer -= trailSpawnInterval;
            trails.add(new Trail(this.x, this.y, this.getSelfAngle(), 1.0f));
        }
        for (Trail t : trails) {
            t.alpha -= deltaTime * trailFadeSpeed;
        }
        trails.removeIf(trail -> trail.alpha <= 0);

    }

    @Override
    public void render() {
        // render trails
        for (Trail t : trails) {
            // render body trail
            Texture.drawTexture(getTexture().getId(),
                t.x, t.y,
                getHScale() * getTexture().getWidth(), getVScale() * getTexture().getHeight(),
                t.angle, rgba[0], rgba[1], rgba[2], t.alpha);

            // render eye trail
            float eyeScale = 1.5f;
            float eyeX = t.x + this.getWidth() / 2 - redEye.getWidth() / 2 * eyeScale * targetScale;
            float eyeY = t.y + this.getHeight() / 2 - redEye.getHeight() / 2 * eyeScale * targetScale;
            Texture.drawTexture(redEye.getId(),
                eyeX, eyeY,
                targetScale * eyeScale * redEye.getWidth(), targetScale * eyeScale * redEye.getHeight(),
                0, 1, 0, 0, t.alpha);
        }

        // render body
        super.render();

        // render eye
        float eyeScale = 1.5f;
        float eyeX = this.x + this.getWidth() / 2 - redEye.getWidth() / 2 * eyeScale * targetScale;
        float eyeY = this.y + this.getHeight() / 2 - redEye.getHeight() / 2 * eyeScale * targetScale;
        Texture.drawTexture(redEye.getId(),
            eyeX, eyeY,
            targetScale * eyeScale * redEye.getWidth(), targetScale * eyeScale * redEye.getHeight(),
            0, 1, 0, 0, rgba[3]
        );
    }
}
