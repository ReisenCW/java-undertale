package undertale.GameObject;

import undertale.Animation.Animation;
import undertale.Utils.Utilities;
import undertale.GameMain.Game;

public class TitanSpawn extends Bullet{
    private Animation animation;
    private float scaler;
    private float maxSpeed;
    private int currentAlpha;

    private float cycleTimerSec = 0f;
    private boolean aimedThisCycle = false;
    public TitanSpawn(float x, float y, float maxSpeed, int damage, Animation animation) {
        super(x, y, 0, 0, 0, damage, null);
        this.animation = animation;
        this.maxSpeed = maxSpeed;
        this.scaler = 1.0f;
        currentAlpha = 0;
    }

    public void setScaler(float scaler) {
        if(scaler >= 0.0f){
            this.scaler = scaler;
        }
    }

    private void updateCurrentSpeed(float deltaTime) {
        // 每4秒: 周期开始瞄准玩家；前3秒速度按 sin 包络上升再下降；第4秒静止
        cycleTimerSec += deltaTime;
        if (cycleTimerSec >= 4.0f) {
            cycleTimerSec -= 4.0f;
            aimedThisCycle = false; // 新周期开始
        }

        // 周期开始时（极短的起始窗口）锁定朝向玩家
        if (!aimedThisCycle && cycleTimerSec < 0.0001f) {
            Player player = Game.getPlayer();
            if (player != null) {
                float dx = player.getX() - this.getX();
                float dy = player.getY() - this.getY();
                float angleDeg = (float)Math.toDegrees(Math.atan2(dy, dx));
                this.setSpeedAngle(angleDeg);
            }
            aimedThisCycle = true;
        }

        // 速度包络：0~3秒为 maxSpeed * sin(pi * t / 3)，3~4秒为0
        float t = cycleTimerSec;
        float speed;
        if (t < 3.0f) {
            speed = (float)(maxSpeed * Math.sin(Math.PI * (t / 3.0f)));
        } else {
            speed = 0.0f;
        }
        if (speed < 0.0f) speed = 0.0f; // 数值稳定
        this.setSpeed(speed);
    }

    @Override
    public void update(float deltaTime) {
        // 在第一秒内, alpha从0增加到255
        if (currentAlpha < 255) {
            currentAlpha += Utilities.getChangeStep(0, 255, deltaTime, 1.0f).intValue();
            if (currentAlpha > 255) {
                currentAlpha = 255;
            }
        }
        updateCurrentSpeed(deltaTime);
        super.update(deltaTime);
        animation.updateAnimation(deltaTime);
    }
    
    @Override
    public void render() {
        animation.renderCurrentFrame(this.x, this.y, scaler, scaler, this.getSpeedAngle(), 255, 255, 255, currentAlpha);
    }
}
