package undertale.GameObject.Bullets;

import static org.lwjgl.opengl.GL20.*;

import java.util.ArrayList;
import java.util.List;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.GameObject.Collectables.TensionPoint;
import undertale.GameObject.Effects.TitanSpawnParticle;
import undertale.Sound.SoundManager;
import undertale.Texture.Texture;
import undertale.Texture.TextureManager;

public class TitanFingers extends Bullet{
    class TitanSingleFinger extends Bullet{
        private boolean isContacting = false;
        private float contactTimer = 0.0f;
        private float contactDisappearTime = 1.6f;
        private boolean markedForRemoval = false;
        private float particleSpawnTimer = 0.0f;
        private float particleSpawnInterval = 0.3f;
        private boolean generatedTP = false;
        public float resetStartX;
        private float pauseBaseX, pauseBaseY;
        TitanSingleFinger(float x, float y, int direction, int intensity, int damage, float scale) {
            super(x, y, 180.0f + direction * 180.0f, 180.0f + direction * 180.0f, 0, damage, AnimationManager.getInstance().getAnimation("titan_finger"));
            setNavi(false);
            this.destroyableOnHit = false;
            this.hScale = scale;
            this.vScale = scale;
            this.animation = new Animation(animation.getFrameDuration(), animation.isLoop(), animation.getFrames());
            this.animation.setHorizontalReverse(direction == -1);
            this.animation.setInterval(0.1f + (4 -intensity) * 0.02f);
            this.isColli = false;
            this.bound = false;
        }

        @Override
        public void update(float deltaTime) {
            if(markedForRemoval) return;

            updatePosition(deltaTime);
            if(state == State.MOVING_OUT || state == State.MOVING_UP_DOWN) {
                animation.updateAnimation(deltaTime);
            }

            // 接触检测和粒子生成，只在特定状态
            if (TitanFingers.this.state == State.STABBING_IN || TitanFingers.this.state == State.STAB_PAUSE || TitanFingers.this.state == State.RESETTING) {
                Player player = Game.getPlayer();
                if (player != null && player.isAlive()) {
                    if (checkContactWithPlayer(player)) {
                        isContacting = true;
                        spawnParticle(deltaTime, player);
                    } else {
                        isContacting = false;
                    }

                    if (isContacting) {
                        contactTimer += deltaTime;
                    }

                    if (contactTimer > contactDisappearTime) {
                        markedForRemoval = true;
                        SoundManager.getInstance().playSE("explode");
                        generateTP();
                    }
                }
            }
        }

        @Override
        public void render() {
            if (markedForRemoval) return;
            animation.renderCurrentFrame(this.x, this.y, getHScale(), getVScale(), this.getSelfAngle(), rgba[0], rgba[1], rgba[2], rgba[3], "titan_spawn_shader", program -> {
                int locScreenSize = glGetUniformLocation(program, "uScreenSize");
                int locColor = glGetUniformLocation(program, "uColor");
                int locTexture = glGetUniformLocation(program, "uTexture");
                int locScale = glGetUniformLocation(program, "uScale");
                glUniform2i(locScreenSize, Game.getWindowWidth(), Game.getWindowHeight());
                glUniform4f(locColor, rgba[0], rgba[1], rgba[2], rgba[3]);
                glUniform1i(locTexture, 0);
                glUniform1f(locScale, (contactDisappearTime - contactTimer) / contactDisappearTime);
            });
        }

        @Override
        public boolean checkCollisionWithPlayer(Player player) {
            if (!isColli || markedForRemoval) return false;
            // 三角形碰撞检测
            float px = player.getX() + player.getWidth() / 2.0f;
            float py = player.getY() + player.getHeight() / 2.0f;
            return isPointInTriangle(px, py, getTriangleVertices());
        }

        private float[][] getTriangleVertices() {
            // 假设三角形：尖端朝向 direction
            float halfH = getHeight() / 2.0f;
            float[][] vertices = new float[3][2];
            if (direction == 1) { // 朝右
                // 尖端右
                vertices[0][0] = x + getWidth(); 
                vertices[0][1] = y + halfH;
                // 左上
                vertices[1][0] = x;
                vertices[1][1] = y;
                // 左下
                vertices[2][0] = x;
                vertices[2][1] = y + getHeight();
            } else { // 朝左
                // 尖端左
                vertices[0][0] = x;
                vertices[0][1] = y + halfH;
                // 右上
                vertices[1][0] = x + getWidth();
                vertices[1][1] = y;
                // 右下
                vertices[2][0] = x + getWidth();
                vertices[2][1] = y + getHeight();
            }
            return vertices;
        }

        private boolean isPointInTriangle(float px, float py, float[][] vertices) {
            float x1 = vertices[0][0], y1 = vertices[0][1];
            float x2 = vertices[1][0], y2 = vertices[1][1];
            float x3 = vertices[2][0], y3 = vertices[2][1];

            // 计算三个顶点的权重(任意一点坐标可以表示为三个顶点坐标的加权和)
            float denom = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
            float a = ((y2 - y3) * (px - x3) + (x3 - x2) * (py - y3)) / denom;
            float b = ((y3 - y1) * (px - x3) + (x1 - x3) * (py - y3)) / denom;
            float c = 1 - a - b;

            // 如果权重在[0,1], 说明点在三角形内
            return 0 <= a && a <= 1 && 0 <= b && b <= 1 && 0 <= c && c <= 1;
        }

        private boolean checkContactWithPlayer(Player player) {
            float px = player.getX() + player.getWidth() / 2.0f;
            float py = player.getY() + player.getHeight() / 2.0f;
            float dx = px - (this.x + this.getWidth() / 2.0f);
            float dy = py - (this.y + this.getHeight() / 2.0f);
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float outer = player.getCurrentLightRadius();
            float amp = player.getLightOscAmplitude();
            float semiHalfSize = Math.max(this.getHeight(), this.getWidth()) / 4.0f;
            if (dist <= outer + amp + semiHalfSize) {
                return true;
            }
            return false;
        }

        private void spawnParticle(float deltaTime, Player player) {
            particleSpawnTimer += deltaTime;
            if (particleSpawnTimer >= particleSpawnInterval) {
                particleSpawnTimer -= particleSpawnInterval;
                float ppx = player.getX() + player.getWidth() / 2.0f;
                float ppy = player.getY() + player.getHeight() / 2.0f;
                float cx = this.x + this.getWidth() / 2.0f;
                float cy = this.y + this.getHeight() / 2.0f;
                float ddx = cx - ppx;
                float ddy = cy - ppy;
                float distance = (float) Math.sqrt(ddx * ddx + ddy * ddy);
                if (distance > 0) {
                    float dirX = ddx / distance;
                    float dirY = ddy / distance;
                    float awayAngle = (float) Math.toDegrees(Math.atan2(ddy, ddx));
                    float randomOffset = (float) ((Math.random() - 0.5) * 20);
                    float spawnAngle = awayAngle + randomOffset;
                    float halfW = this.getWidth() / 2.0f;
                    float halfH = this.getHeight() / 2.0f;
                    float randomDist = (float) Math.random() * Math.max(halfW, halfH);
                    float spawnX = cx - dirX * randomDist;
                    float spawnY = cy - dirY * randomDist;
                    spawnX = Math.max(cx - halfW, Math.min(cx + halfW, spawnX));
                    spawnY = Math.max(cy - halfH, Math.min(cy + halfH, spawnY));
                    TitanSpawnParticle particle = new TitanSpawnParticle(spawnX, spawnY, spawnAngle);
                    Game.getObjectManager().addTitanSpawnParticle(particle);
                }
            }
        }

        private void generateTP() {
            if (!generatedTP) {
                generatedTP = true;
                float width = getWidth();
                float height = getHeight();
                for (int i = 0; i < 4; i++) {
                    float tx = x + (i + 0.5f) * width / 4;
                    float ty = y + height / 2;
                    TensionPoint tp = new TensionPoint(tx, ty, 1.6f);
                    Game.getObjectManager().addCollectable(tp);
                }
            }
        }
    }

    private enum State { 
        MOVING_UP_DOWN, 
        MOVING_OUT, 
        MOVING_OUT_PAUSE,
        STABBING_IN,
        STAB_PAUSE,
        RESETTING,
        RESET_ANIMATION
    }

    private class PalmTrail {
        float x, y, alpha;
        PalmTrail(float x, float y, float alpha) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }
    }

    private State state = State.MOVING_UP_DOWN;

    private List<PalmTrail> palmTrails = new ArrayList<>();
    private float trailSpawnTimer = 0;
    private float trailSpawnInterval = 0.05f;
    private float trailFadeSpeed = 2.0f;

    private TitanSingleFinger[] fingers = new TitanSingleFinger[4];
    private int direction;
    private float randomTime;
    private float fadeTimer;
    private float moveTimer;
    private boolean moving;
    private float resetStartX;
    private float baseY;
    private float[] baseFingerY = new float[4];
    private float initialX;
    private float[] initialFingerX = new float[4];

    private float outMoveTimer = 0;
    private float stabMoveTimer = 0;
    private float fingerAnimationDuration;
    private float stabDuration = 0.5f;
    private float resetDuration = 1.0f;
    private float pauseDuration = 1.0f;
    private boolean animationStarted = false;
    private float resetTimer = 0;
    private float pauseTimer = 0;

    private float currentTheta = 0.0f;
    private float pauseBaseX, pauseBaseY;


    /**
     * @param speed      finger的速度
     * @param direction  朝向, 1代表右, -1代表左
     * @param scale      缩放
     * @param randomTime 随机时间，用于停止移动
     */
    public TitanFingers(float x, float y, int intensity, int damage, int direction, float scale, float randomTime) {
        super(x, y, 0, 0, 0, damage, TextureManager.getInstance().getTexture("titan_palm"));
        setNavi(false);
        this.direction = direction;
        this.bound = false;
        this.randomTime = randomTime;
        this.vScale = scale;
        this.hScale = scale;
        float fingerHeight = AnimationManager.getInstance().getAnimation("titan_finger").getFrameHeight();
        float fingerWidth = AnimationManager.getInstance().getAnimation("titan_finger").getFrameWidth();
        for (int i = 0; i < fingers.length; i++) {
            float offsetX = (this.getWidth() / 2 + fingerWidth / 2 * scale / 5) * direction;
            if(direction == -1) {
                offsetX -= fingerWidth * scale / 5;
            }
            float offsetY = -25 * 2.5f + i * 25 + fingerHeight / 2;
            fingers[i] = new TitanSingleFinger(x + offsetX, this.getHeight() / 2 + y + offsetY * scale, direction, intensity, damage, scale);
        }
        this.destroyableOnHit = false;
        // 初始淡入
        this.rgba[3] = 0.0f;
        this.isColli = false;
        fadeTimer = 0.0f;
        moveTimer = 0.0f;
        moving = true;
        baseY = y;
        initialX = x;
        for (int i = 0; i < fingers.length; i++) {
            baseFingerY[i] = fingers[i].getY();
            initialFingerX[i] = fingers[i].getX();
        }
        state = State.MOVING_UP_DOWN;
        outMoveTimer = 0;
        stabMoveTimer = 0;
        animationStarted = false;
        resetTimer = 0;
        fingerAnimationDuration = fingers[0].getAnimation().getTotalDuration();
    }

    @Override
    public void update(float deltaTime) {
        // 淡入效果
        fadeTimer += deltaTime;
        float fadeInTime = 1.0f; // 1秒淡入
        if (fadeTimer < fadeInTime) {
            float alpha = fadeTimer / fadeInTime;
            this.rgba[3] = alpha;
            for (TitanSingleFinger finger : fingers) {
                finger.rgba[3] = alpha;
            }
        } else {
            this.rgba[3] = 1.0f;
            for (TitanSingleFinger finger : fingers) {
                finger.rgba[3] = 1.0f;
            }
        }

        // 状态机
        if (state == State.MOVING_UP_DOWN) {
            float baseTime = 3.8f;
            float totalTime = baseTime + randomTime;
            float amp = 60.0f;
            float cycleTime = 2.8f;
            // 上下移动
            if (moving) {
                moveTimer += deltaTime;
                if (moveTimer < totalTime) {
                    // sin 移动，周期 cycleTime秒，幅度 amp
                    float sinValue = (float) Math.sin(currentTheta + moveTimer * 2 * Math.PI / cycleTime) * amp;
                    // 移动 palm
                    this.y = baseY + sinValue;
                    // 移动 fingers
                    for (int i = 0; i < fingers.length; i++) {
                        fingers[i].setY(baseFingerY[i] + sinValue);
                    }
                } else {
                    currentTheta = (currentTheta + (float)(moveTimer * 2 * Math.PI / cycleTime)) % (2 * (float)Math.PI);
                    moving = false;
                    state = State.MOVING_OUT;
                    outMoveTimer = 0;
                    animationStarted = false;
                    for (TitanSingleFinger finger : fingers) {
                        finger.animation.reset();
                    }
                }
            }
        } else if (state == State.MOVING_OUT) {
            outMoveTimer += deltaTime;
            // 向外 sin 移动
            float amp = 200.0f;
            float cycleTime = 2 * fingerAnimationDuration;
            float outSin = (float) Math.sin(outMoveTimer * 2 * Math.PI / cycleTime) * amp;
            float t = outMoveTimer / cycleTime;
            if (t >= 0.25f && !animationStarted) {
                animationStarted = true;
                // 动画开始播放
            }
            if (direction == 1) {
                this.x -= outSin * deltaTime;
                for (TitanSingleFinger finger : fingers) {
                    finger.setX(finger.getX() - outSin * deltaTime);
                }
            } else {
                this.x += outSin * deltaTime;
                for (TitanSingleFinger finger : fingers) {
                    finger.setX(finger.getX() + outSin * deltaTime);
                }
            }
            // 检查动画是否结束
            boolean allFinished = true;
            for (TitanSingleFinger finger : fingers) {
                if (!finger.markedForRemoval && !finger.animation.isFinished()) {
                    allFinished = false;
                    break;
                }
            }
            if (allFinished) {
                state = State.MOVING_OUT_PAUSE;
                pauseTimer = 0;
                pauseBaseX = this.x;
                pauseBaseY = this.y;
                for (TitanSingleFinger finger : fingers) {
                    finger.pauseBaseX = finger.getX();
                    finger.pauseBaseY = finger.getY();
                }
            }
        } else if (state == State.MOVING_OUT_PAUSE) {
            pauseTimer += deltaTime;
            if (pauseTimer < 0.5f) {
                // 抖动
                float shakeAmp = 5.0f;
                float shakeX = (float) ((Math.random() - 0.5) * 2 * shakeAmp);
                float shakeY = (float) ((Math.random() - 0.5) * 2 * shakeAmp);
                this.x = pauseBaseX + shakeX;
                this.y = pauseBaseY + shakeY;
                for (TitanSingleFinger finger : fingers) {
                    finger.setX(finger.pauseBaseX + shakeX);
                    finger.setY(finger.pauseBaseY + shakeY);
                }
            } else {
                state = State.STABBING_IN;
                stabMoveTimer = 0;
                SoundManager.getInstance().playSE("finger_charge");
                // 开启判定
                this.isColli = true;
                for (TitanSingleFinger finger : fingers) {
                    finger.setColli(true);
                }
            }
        } else if (state == State.STABBING_IN) {
            stabMoveTimer += deltaTime;
            // 高速突刺
            float stabSpeed = 860.0f;
            if (direction == 1) {
                this.x += stabSpeed * deltaTime;
                for (TitanSingleFinger finger : fingers) {
                    finger.setX(finger.getX() + stabSpeed * deltaTime);
                }
            } else {
                this.x -= stabSpeed * deltaTime;
                for (TitanSingleFinger finger : fingers) {
                    finger.setX(finger.getX() - stabSpeed * deltaTime);
                }
            }
            // 添加残影
            trailSpawnTimer += deltaTime;
            if (trailSpawnTimer >= trailSpawnInterval) {
                trailSpawnTimer -= trailSpawnInterval;
                palmTrails.add(new PalmTrail(this.x, this.y, 1.0f));
            }
            // 突刺后停顿
            if (stabMoveTimer > stabDuration) {
                state = State.STAB_PAUSE;
                pauseTimer = 0;
            }
        } else if (state == State.STAB_PAUSE) {
            pauseTimer += deltaTime;
            if (pauseTimer > pauseDuration) {
                state = State.RESETTING;
                resetTimer = 0;
                resetStartX = this.x;
                for (TitanSingleFinger finger : fingers) {
                    finger.resetStartX = finger.getX();
                }
            }
        } else if (state == State.RESETTING) {
            resetTimer += deltaTime;
            if (resetTimer < resetDuration) {
                float t = resetTimer / resetDuration;
                // 平滑插值到初始位置
                this.x = lerp(resetStartX, initialX, t);
                for (int i = 0; i < fingers.length; i++) {
                    fingers[i].setX(lerp(fingers[i].resetStartX, initialFingerX[i], t));
                }
            } else {
                // 确保精确位置
                this.x = initialX;
                for (int i = 0; i < fingers.length; i++) {
                    fingers[i].setX(initialFingerX[i]);
                }
                state = State.MOVING_UP_DOWN;
                moveTimer = 0;
                moving = true;
                // 重置判定
                this.isColli = false;
                for (TitanSingleFinger finger : fingers) {
                    finger.setColli(false);
                    finger.animation.reset();
                }
            }
        }

        for (TitanSingleFinger finger : fingers) {
            finger.update(deltaTime);
        }
        super.update(deltaTime);

        // 更新残影
        for (PalmTrail trail : palmTrails) {
            trail.alpha -= trailFadeSpeed * deltaTime;
        }
        palmTrails.removeIf(trail -> trail.alpha <= 0);
    }

    @Override 
    public void render() {
        Texture currentTexture = getCurrentTexture();
        if (currentTexture != null) {
            // 绘制残影
            for (PalmTrail trail : palmTrails) {
                Texture.drawTexture(currentTexture.getId(),
                    trail.x, trail.y,
                    hScale * currentTexture.getWidth(), vScale * currentTexture.getHeight(),
                    getSelfAngle(), rgba[0], rgba[1], rgba[2], trail.alpha, direction != 1, false
                );
            }
            // 绘制当前 palm
            Texture.drawTexture(currentTexture.getId(),
                this.x, this.y,
                hScale * currentTexture.getWidth(), vScale * currentTexture.getHeight(),
                getSelfAngle(), rgba[0], rgba[1], rgba[2], rgba[3], direction != 1, false
            );
        }
        for (TitanSingleFinger finger : fingers) {
            if (!finger.markedForRemoval) {
                finger.render();
            }
        }
    }

    public boolean checkCollisionWithPlayer(Player player) {
        for (TitanSingleFinger finger : fingers) {
            if (finger.checkCollisionWithPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAnimation() {
        return true;
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}
