package undertale.GameObject.Bullets;

import java.util.ArrayList;
import java.util.List;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameMain.Game;
import undertale.GameObject.CollisionDetector;
import undertale.GameObject.Player;
import undertale.GameObject.Collectables.TensionPoint;
import undertale.Utils.GameUtilities;
import undertale.GameObject.Effects.TitanSpawnParticle;

import static org.lwjgl.opengl.GL20.*;

public class TitanSnake extends Bullet {
    private static class SnakePart extends Bullet{
        Animation animation;

        SnakePart(float x, float y, float scale, float angle, float maxSpeed, int damage, Animation animation) {
            super(x, y, angle, angle, 0, damage, animation);
            this.setNavi(true);
            this.animation = new Animation(animation.getFrameDuration(), animation.isLoop(), animation.getFrames());
            setHScale(scale);
            setVScale(scale);
            setMaxSpeed(maxSpeed);
        }

        @Override
        public void update(float deltaTime) {
            updatePosition(deltaTime);
            animation.updateAnimation(deltaTime);
        }

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

        boolean checkContactLight(Player player) {
            float px = player.getX() + player.getWidth() / 2.0f;
            float py = player.getY() + player.getHeight() / 2.0f;
            float dx = px - (this.x + animation.getCurrentFrame().getWidth() / 2.0f);
            float dy = py - (this.y + animation.getCurrentFrame().getHeight() / 2.0f);
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            float outer = player.getCurrentLightRadius();
            float amp = player.getLightOscAmplitude();
            float halfSize = Math.max(animation.getCurrentFrame().getHeight(), animation.getCurrentFrame().getWidth()) / 2.0f;

            return dist <= outer + amp + halfSize;
        }

        private void giveTensionPoints() {
            TensionPoint tp = new TensionPoint(this.x + this.getWidth() / 2.0f, this.y + this.getHeight() / 2.0f, 1.6f);
            Game.getObjectManager().addCollectable(tp);
        }

        @Override
        public boolean checkCollisionWithPlayer(Player player) {
            if (!player.isAlive() || !this.isColli) {
                return false;
            }
            float padding = -Math.min(getWidth(), getHeight()) / 3.5f;
            return CollisionDetector.checkCircleCollision(this, player, padding);
        }
        
    }

    private SnakePart head;
    private List<SnakePart> bodies;
    private SnakePart tail;

    private float fadeInDuration = 0.5f;

    private boolean contacting = false;
    private float contactTimer = 0.0f;
    private float contactDisappearTime = 3.5f;
    private float initialScale = 1.5f;
    private float minScale = 0.35f;

    private float particleEmitTimer = 0.0f;
    private float particleEmitInterval = 0.5f;

    private float initialHeadAcceleration = 250.0f;
    private float maxSpeed = -1.0f;

    private boolean markedForRemoval = false;

    public TitanSnake(float x, float y, int bodyCount, int damage) {
        super(x, y, 0, 0, 0, damage, (Animation) null); // 不使用super的animation
        this.rgba[3] = 0.0f; // 初始透明
        this.isColli = false;
        this.destroyableOnHit = false;

        // 获取动画
        Animation headAnim = AnimationManager.getInstance().getAnimation("titan_snake_head");
        Animation bodyAnim = AnimationManager.getInstance().getAnimation("titan_snake_body");
        Animation tailAnim = AnimationManager.getInstance().getAnimation("titan_snake_tail");

        // 初始化部分
        float angleToPlayer = 0.0f;
        Player player = Game.getPlayer();
        if (player != null) {
            float dx = player.getX() + player.getWidth() / 2.0f - x;
            float dy = player.getY() + player.getHeight() / 2.0f - y;
            angleToPlayer = (float) Math.toDegrees(Math.atan2(dy, dx));
        }
        head = new SnakePart(x, y, initialScale, angleToPlayer, maxSpeed, damage, headAnim);
        float dirX = (float) Math.cos(Math.toRadians(angleToPlayer));
        float dirY = (float) Math.sin(Math.toRadians(angleToPlayer));
        bodies = new ArrayList<>();
        for (int i = 0; i < bodyCount; i++) {
            float offsetX = - (i + 1) * 50 * dirX;
            float offsetY = - (i + 1) * 50 * dirY;
            bodies.add(new SnakePart(x + offsetX, y + offsetY, initialScale, angleToPlayer, maxSpeed, damage, bodyAnim));
        }
        float tailOffsetX = - (bodyCount + 1) * 50 * dirX;
        float tailOffsetY = - (bodyCount + 1) * 50 * dirY;
        tail = new SnakePart(x + tailOffsetX, y + tailOffsetY, initialScale, angleToPlayer, maxSpeed, damage, tailAnim);
        // 共享rgba以实现淡入效果
        head.rgba = this.rgba;
        for (SnakePart body : bodies) {
            body.rgba = this.rgba;
        }
        tail.rgba = this.rgba;
    }

    @Override
    public boolean checkCollisionWithPlayer(Player player) {
        if (head.checkCollisionWithPlayer(player)) return true;
        for (SnakePart body : bodies) {
            if (body.checkCollisionWithPlayer(player)) return true;
        }
        return tail.checkCollisionWithPlayer(player);
    }

    @Override
    public boolean hasAnimation() {
        return true;
    }

    @Override
    public void update(float deltaTime) {
        // 更新head朝向player
        Player player = Game.getPlayer();
        if (player != null) {
            float dx = player.getX() + player.getWidth() / 2.0f - head.getX();
            float dy = player.getY() + player.getHeight() / 2.0f - head.getY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float targetAngle = (float) Math.toDegrees(Math.atan2(dy, dx));

            float acceleration = initialHeadAcceleration + dist; // 距离越远加速越大
            float accelX = (float) Math.cos(Math.toRadians(targetAngle)) * acceleration;
            float accelY = (float) Math.sin(Math.toRadians(targetAngle)) * acceleration;
            head.setAccelerateX(accelX);
            head.setAccelerateY(accelY);
        }

        // 淡入
        if (rgba[3] < 1.0f) {
            rgba[3] += GameUtilities.getChangeStep(0.0f, 1.0f, deltaTime, fadeInDuration).floatValue();
            if (rgba[3] > 1.0f) {
                rgba[3] = 1.0f;
                this.isColli = true;
                head.setColli(true);
                for (SnakePart body : bodies) {
                    body.setColli(true);
                }
                tail.setColli(true);
            }
        }

        // 更新body跟随head
        SnakePart prev = head;
        for (SnakePart body : bodies) {
            follow(prev, body, deltaTime);
            prev = body;
        }
        // tail跟随最后一个body
        follow(prev, tail, deltaTime);

        if (rgba[3] >= 1.0f) {
            // 检查碰撞
            boolean anyContact = false;
            if (head.checkContactLight(player)) anyContact = true;
            for (SnakePart body : bodies) {
                if (body.checkContactLight(player)) anyContact = true;
            }
            if (tail.checkContactLight(player)) anyContact = true;

            if (anyContact) {
                contacting = true;
                contactTimer += deltaTime;
                particleEmitTimer += deltaTime;
                if (particleEmitTimer >= particleEmitInterval) {
                    particleEmitTimer = 0.0f;
                    emitParticles();
                }
            } else {
                contacting = false;
                particleEmitTimer = 0.0f; // 重置timer
            }

            // 缩小
            if (contacting) {
                float scale = initialScale - (initialScale - minScale) * (contactTimer / contactDisappearTime);
                if (scale < minScale) scale = minScale;
                head.setHScale(scale);
                head.setVScale(scale);
                for (SnakePart body : bodies) {
                    body.setHScale(scale);
                    body.setVScale(scale);
                }
                tail.setHScale(scale);
                tail.setVScale(scale);
                if (contactTimer >= contactDisappearTime) {
                    head.giveTensionPoints();
                    for(SnakePart body : bodies) {
                        body.giveTensionPoints();
                    }
                    tail.giveTensionPoints();
                    markedForRemoval = true;
                }
            }
        }

        // 更新动画
        head.update(deltaTime);
        for (SnakePart body : bodies) {
            body.update(deltaTime);
        }
        tail.update(deltaTime);
    }

    private void follow(SnakePart leader, SnakePart follower, float deltaTime) {
        float dx = leader.getX() - follower.getX();
        float dy = leader.getY() - follower.getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float speed = dist * 8.0f;
        float targetSpeedX = (dx / dist) * speed;
        float targetSpeedY = (dy / dist) * speed;
        follower.setSpeedX(targetSpeedX);
        follower.setSpeedY(targetSpeedY);
        follower.setAccelerateX(0);
        follower.setAccelerateY(0);
    }

    private void emitParticles() {
        // 为head散发粒子
        for (int i = 0; i < 3; i++) {
            float angle = (float) (Math.random() * 360);
            TitanSpawnParticle particle = new TitanSpawnParticle(head.getX() + head.getWidth() / 2.0f, head.getY() + head.getHeight() / 2.0f, angle);
            Game.getObjectManager().addTitanSpawnParticle(particle);
        }
        // 为bodies散发粒子
        for (SnakePart body : bodies) {
            for (int i = 0; i < 3; i++) {
                float angle = (float) (Math.random() * 360);
                TitanSpawnParticle particle = new TitanSpawnParticle(body.getX() + body.getWidth() / 2.0f, body.getY() + body.getHeight() / 2.0f, angle);
                Game.getObjectManager().addTitanSpawnParticle(particle);
            }
        }
        // 为tail散发粒子
        for (int i = 0; i < 3; i++) {
            float angle = (float) (Math.random() * 360);
            TitanSpawnParticle particle = new TitanSpawnParticle(tail.getX() + tail.getWidth() / 2.0f, tail.getY() + tail.getHeight() / 2.0f, angle);
            Game.getObjectManager().addTitanSpawnParticle(particle);
        }
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    @Override
    public void render() {
        head.render();
        for (SnakePart body : bodies) {
            body.render();
        }
        tail.render();
    }
}
