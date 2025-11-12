package undertale.GameObject;

import static org.lwjgl.opengl.GL20.*;

import undertale.Animation.Animation;
import undertale.Utils.GameUtilities;
import undertale.GameMain.Game;

public class TitanSpawn extends Bullet{
    private float maxSpeed;

    private float cycleTimerSec = 0f;
    private boolean aimedThisCycle = false;
    private int aimTime = 4;
    // 接触光圈缩小相关变量
    private float contactTimer = 0f; // seconds
    private float contactDisapperTime = 1.5f; // seconds, 接触光圈1.5s后消失
    private boolean contacting = false;
    private boolean markedForRemoval = false;
    private float initialHScale;
    private float initialVScale;

    private float cycleDuration = 2.5f; // 每个周期持续时间
    private float speedDuration = 2.0f; // 速度变化持续时间
    
    private final float MIN_VISIBLE_SCALE = 0.35f;

    public TitanSpawn(float x, float y, float maxSpeed, int damage, Animation animation) {
        super(x, y, 0, 0, 0, damage, animation);
        setNavi(false);
        this.destroyableOnHit = false;
        this.maxSpeed = maxSpeed;
        // 创建动画副本，避免多个实例共享同一个动画状态
        this.animation = new Animation(animation.getFrameDuration(), animation.isLoop(), animation.getFrames());

        this.rgba[3] = 0.0f; // 初始透明
        this.isColli = false; // 初始无判定
        this.initialHScale = getHScale();
        this.initialVScale = getVScale();
    }

    private void updateCurrentSpeed(float deltaTime) {
        // 每cycleDuration秒: 周期开始瞄准玩家
        // 前speedDuration秒速度按 sin 包络上升再下降, 之后速度为0
        cycleTimerSec += deltaTime;
        if (cycleTimerSec >= cycleDuration && aimTime > 0) {
            cycleTimerSec -= cycleDuration;
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
        // 速度包络:0~speedDuration秒为 maxSpeed * sin(pi * t / speedDuration), speedDuration~cycleDuration秒为0
        float t = cycleTimerSec;
        float speed;
        if (t < speedDuration) {
            speed = (float)(maxSpeed * Math.sin(Math.PI * (t / speedDuration)));
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

        // 如果已经标记为删除则直接返回
        if (markedForRemoval) return;

        // 检测是否与player任意光圈环接触（renderLight绘制了多圈）
        Player player = Game.getPlayer();
        if (player != null && player.isAlive()) {
            float px = player.getX() + player.getWidth() / 2.0f;
            float py = player.getY() + player.getHeight() / 2.0f;
            float dx = px - (this.x + this.getWidth() / 2.0f);
            float dy = py - (this.y + this.getHeight() / 2.0f);
            float dist = (float)Math.sqrt(dx * dx + dy * dy);

            float outer = player.getCurrentLightRadius();
            float amp = player.getLightOscAmplitude();

            boolean touch = false;
            float halfSize = Math.max(this.getHeight(), this.getWidth()) / 2.0f;

            // player与titan_spawn的中心距离 <= 外光圈半径 + 震动幅度 + titan_spawn半径
            if (dist <= outer + amp + halfSize) {
                touch = true;
            }

            if (touch) {
                contactTimer += deltaTime;
                contacting = true;
            } else {
                contacting = false;
            }

            // 接触光圈时缩小
            if (contacting) {
                float t = Math.min(1.0f, contactTimer / contactDisapperTime);
                // 不将scale降为0, 保留最小可见比例以保持良好体验
                float scale = Math.max(MIN_VISIBLE_SCALE, 1.0f - t);
                setHScale(initialHScale * scale);
                setVScale(initialVScale * scale);
                // 接触超过contactDisapperTime后消失
                if (Math.abs(scale - MIN_VISIBLE_SCALE) < 0.01f) {
                    this.rgba[3] = 0.0f;
                    this.isColli = false;
                    markedForRemoval = true;
                }
            }
        }
    }
    
    @Override
    public void render() {
        animation.renderCurrentFrame(this.x, this.y, getHScale(), getVScale(), this.getSelfAngle(), rgba[0], rgba[1], rgba[2], rgba[3], "titan_spawn_shader", program -> {
            int locScreenSize = glGetUniformLocation(program, "uScreenSize");
            int locColor = glGetUniformLocation(program, "uColor");
            int locTexture = glGetUniformLocation(program, "uTexture");
            int locScale = glGetUniformLocation(program, "uScale");
            glUniform2i(locScreenSize, Game.getWindowWidth(), Game.getWindowHeight());
            glUniform4f(locColor, rgba[0], rgba[1], rgba[2], rgba[3]);
            glUniform1i(locTexture, 0);
            glUniform1f(locScale, (getHScale() + getVScale()) / 2.0f);
        });
    }

    @Override
    public float getWidth() {
        return animation.getFrameWidth() * getHScale();
    }

    public float getHeight() {
        return animation.getFrameHeight() * getVScale();
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }
}
