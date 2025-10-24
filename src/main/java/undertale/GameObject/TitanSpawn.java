package undertale.GameObject;

import undertale.Animation.Animation;
import undertale.Utils.GameUtilities;
import undertale.GameMain.Game;

public class TitanSpawn extends Bullet{
    private float maxSpeed;

    private float cycleTimerSec = 0f;
    private boolean aimedThisCycle = false;
    private int aimTime = 2;

    public TitanSpawn(float x, float y, float maxSpeed, int damage, Animation animation) {
        super(x, y, 0, 0, 0, damage, animation);
        setNavi(false);
        this.destroyableOnHit = false;
        this.maxSpeed = maxSpeed;
        // 创建动画副本，避免多个实例共享同一个动画状态
        this.animation = new Animation(animation.getFrameDuration(), animation.isLoop(), animation.getFrames());

        this.rgba[3] = 0.0f; // 初始透明
        this.isColli = false; // 初始无判定
    }

    private void updateCurrentSpeed(float deltaTime) {
        // 每3秒: 周期开始瞄准玩家；前2.5秒速度按 sin 包络上升再下降；第3秒静止
        cycleTimerSec += deltaTime;
        if (cycleTimerSec >= 3.0f && aimTime > 0) {
            cycleTimerSec -= 3.0f;
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
        // 速度包络：0~2.5秒为 maxSpeed * sin(pi * t / 2.5)，2.5~3秒为0
        float t = cycleTimerSec;
        float speed;
        if (t < 2.5f) {
            speed = (float)(maxSpeed * Math.sin(Math.PI * (t / 2.5f)));
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
        // 在1秒内, alpha从0增加到1
        if (rgba[3] < 1.0f) {
            rgba[3] += GameUtilities.getChangeStep(0.0f, 1.0f, deltaTime, 1.0f).floatValue();
            if (rgba[3] > 1.0f) {
                rgba[3] = 1.0f;
            }
        } else {
            // 完全显现, 恢复判定
            if(!this.isColli){
                this.isColli = true;
            }
            updateCurrentSpeed(deltaTime);
        }
        super.update(deltaTime);
        animation.updateAnimation(deltaTime);
    }
    
    @Override
    public void render() {
        animation.renderCurrentFrame(this.x, this.y, getHScale(), getVScale(), this.getSelfAngle(), rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    @Override
    public float getWidth() {
        return animation.getFrameWidth() * getHScale();
    }

    public float getHeight() {
        return animation.getFrameHeight() * getVScale();
    }
}
