package undertale.GameObject.Bullets;

import undertale.Texture.TextureManager;

public class BallSmall extends Bullet {
    private float waitTimer;
    private float maxSpeed;
    private float accelTime;
    private float accelTimer;
    private float initialScale;
    private float extendedScale;
    private float scaleSwitchInterval;
    private float scaleTimer;
    private boolean isExtended;
    private float speedAngle;

    public BallSmall(float x, float y, float speedAngle, float waitTime, float maxSpeed, float accelTime, float initialScale, float extendedScale, float scaleSwitchInterval, int damage) {
        super(x, y, 0, speedAngle, 0, damage, TextureManager.getInstance().getTexture("ball_small"));
        this.speedAngle = speedAngle;
        this.waitTimer = waitTime;
        this.maxSpeed = maxSpeed;
        this.accelTime = accelTime;
        this.accelTimer = 0;
        this.initialScale = initialScale;
        this.extendedScale = extendedScale;
        this.scaleSwitchInterval = scaleSwitchInterval;
        this.scaleTimer = (float) (Math.random() * 0.3f);
        this.isExtended = false;
        this.hScale = initialScale;
        this.vScale = initialScale;
        this.destroyableOnHit = false;
    }

    @Override
    public void update(float deltaTime) {
        // 等待后开始移动
        if (waitTimer > 0) {
            waitTimer -= deltaTime;
        } else {
            // 加速到 maxSpeed
            if (accelTimer < accelTime) {
                accelTimer += deltaTime;
                float t = accelTimer / accelTime;
                // 平滑曲线，使用 sin
                float speed = maxSpeed * (float) Math.sin(t * Math.PI / 2);
                this.setSpeed(speed);
            } else {
                this.setSpeed(maxSpeed);
            }
            this.setSpeedAngle(this.speedAngle);
            updatePosition(deltaTime);
        }

        // 切换 scale
        scaleTimer += deltaTime;
        if (scaleTimer >= scaleSwitchInterval) {
            scaleTimer -= scaleSwitchInterval;
            isExtended = !isExtended;
            if (isExtended) {
                setHScale(extendedScale);
                setVScale(initialScale);
            } else {
                setHScale(initialScale);
                setVScale(extendedScale);
            }
        }
    }
}