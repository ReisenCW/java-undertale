package undertale.GameObject;

import undertale.Animation.Animation;
import undertale.Utils.Utilities;
import undertale.GameMain.Game;

public class TitanSpawn extends Bullet{
    private Animation animation;
    private float maxSpeed;
    private float currentAlpha;

    private float cycleTimerSec = 0f;
    private boolean aimedThisCycle = false;
    private int aimTime = 2;

    public TitanSpawn(float x, float y, float maxSpeed, int damage, Animation animation) {
        super(x, y, 0, 0, 0, damage, null);
        setDestroyableOnHit(false);
        setNavi(false);
        this.animation = animation;
        this.maxSpeed = maxSpeed;
        this.currentAlpha = 0.0f;
    }

    private void updateCurrentSpeed(float deltaTime) {
        // 每3.5秒: 周期开始瞄准玩家；前3秒速度按 sin 包络上升再下降；第4秒静止
        cycleTimerSec += deltaTime;
        if (cycleTimerSec >= 3.5f && aimTime > 0) {
            cycleTimerSec -= 3.5f;
            aimedThisCycle = false; // 新周期开始
        }

        // 周期开始时（极短的起始窗口）锁定朝向玩家, 最多锁定2次
        if (!aimedThisCycle && aimTime > 0) {
            Player player = Game.getPlayer();
            if (player != null) {
                float dx = player.getX() - this.getX();
                float dy = player.getY() - this.getY();
                float angleDeg = (float)Math.toDegrees(Math.atan2(dy, dx));
                this.setSpeedAngle(angleDeg);
            }
            aimTime--;
            aimedThisCycle = true;
        }
        // 速度包络：0~3秒为 maxSpeed * sin(pi * t / 3)，3~3.5秒为0
        float t = cycleTimerSec;
        float speed;
        if (t < 3.0f) {
            speed = (float)(maxSpeed * Math.sin(Math.PI * (t / 3.0f)));
        } else {
            if(aimTime > 0) {
                speed = 0.0f;
            }
            else{
                speed = maxSpeed;
            }
        }
        if (speed < 0.0f) speed = 0.0f;
        if (speed > maxSpeed) speed = maxSpeed;
        this.setSpeed(speed);

    }

    @Override
    public void update(float deltaTime) {
        // 在1秒内, alpha从0增加到255
        if (currentAlpha < 1.0f) {
            currentAlpha += Utilities.getChangeStep(0.0f, 1.0f, deltaTime, 1.0f).floatValue();
            if (currentAlpha > 1.0f) {
                currentAlpha = 1.0f;
            }
        }
        else{
            updateCurrentSpeed(deltaTime);
        }
        super.update(deltaTime);
        animation.updateAnimation(deltaTime);
    }
    
    @Override
    public void render() {
        animation.renderCurrentFrame(this.x, this.y, getHScale(), getVScale(), this.getSelfAngle(), 1.0f, 1.0f, 1.0f, currentAlpha);
    }

    @Override
    public float getWidth() {
        return animation.getFrameWidth() * getHScale();
    }

    public float getHeight() {
        return animation.getFrameHeight() * getVScale();
    }
}
