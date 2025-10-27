package undertale.GameObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import undertale.Animation.Animation;
import undertale.Enemy.EnemyManager;
import undertale.GameMain.Game;
import undertale.GameObject.Player.LightLevel;
import undertale.Sound.SoundManager;
import undertale.Texture.Texture;

public class ObjectManager {
    private Player player;
    private ArrayList<Bullet> bullets;
    // 对象池复用子弹, 避免频繁创建和销毁子弹, 减少gc压力和内存分配开销
    private ArrayList<Bullet> bulletPool;
    // 声明为成员变量避免频繁创建临时列表
    private ArrayList<Bullet> toRemove;
    private EnemyManager enemyManager;
    private BulletRenderer bulletRenderer;
    private SoundManager soundManager;

    public ObjectManager(Player player){
        init(player);
    }

    private void init(Player player){
        this.player = player;
        bullets = new ArrayList<>();
        bulletPool = new ArrayList<>();
        toRemove = new ArrayList<>();
        enemyManager = EnemyManager.getInstance();
        bulletRenderer = new BulletRenderer();
        soundManager = SoundManager.getInstance();
    }

    public Bullet createBullet(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Texture texture){
        Bullet bullet = getBulletFromPool();
        if (bullet == null) {
            // 如果对象池中没有可用子弹，则创建新的子弹
            bullet = new Bullet(x, y, selfAngle, speedAngle, speed, damage, texture);
        } else {
            // 重置子弹属性
            bullet.reset(x, y, selfAngle, speedAngle, speed, damage, texture);
        }
        bullets.add(bullet);
        return bullet;
    }

    public Bullet createBullet(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Animation animation){
        Bullet bullet = getBulletFromPool();
        if (bullet == null) {
            // 如果对象池中没有可用子弹，则创建新的子弹
            bullet = new Bullet(x, y, selfAngle, speedAngle, speed, damage, animation);
        } else {
            // 重置子弹属性
            bullet.reset(x, y, selfAngle, speedAngle, speed, damage, animation);
        }
        bullets.add(bullet);
        return bullet;
    }

    private Bullet getBulletFromPool() {
        if (bulletPool.isEmpty()) {
            return null;
        }
        // 从对象池中获取子弹并移出对象池
        return bulletPool.remove(bulletPool.size() - 1);
    }

    private void returnBulletToPool(Bullet bullet) {
        bulletPool.add(bullet);
    }

    public void addBullet(Bullet bullet) {
        if (bullet != null) {
            bullets.add(bullet);
        }
    }

    public void updateMenuScene(float deltaTime){
        // enemy
        enemyManager.update(deltaTime);
        // player
        player.update(deltaTime);
    }

    public void updateFightScene(float deltaTime){
        // enemy
        enemyManager.update(deltaTime);
        // player
        player.update(deltaTime);

        // bullets
        toRemove.clear();
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);
            if(!player.isAlive())
                continue;

            // 如果是TitanSpawn并且已标记为删除，加入移除列表
            if (bullet instanceof TitanSpawn) {
                TitanSpawn ts = (TitanSpawn) bullet;
                if (ts.isMarkedForRemoval()) {
                    toRemove.add(bullet);
                    continue;
                }
            }

            // 先检查子弹是否超出屏幕边界
            float margin = Math.max(bullet.getWidth(), bullet.getHeight()) / 2.0f;
            if (bullet.bound && isBulletOutOfBounds(bullet, margin)) {
                toRemove.add(bullet);
                continue;
            }

            // 子弹开启命中销毁 且 子弹与玩家碰撞
            if (checkPlayerBulletCollisionReturnHit(bullet) && bullet.destroyableOnHit) {
                toRemove.add(bullet);
            }
        }
        // 将要移除的子弹回收到对象池
        for (Bullet bullet : toRemove) {
            bullets.remove(bullet);
            returnBulletToPool(bullet);
        }
    }

    private boolean isBulletOutOfBounds(Bullet bullet, float margin) {
        return bullet.getX() < -margin ||
               bullet.getX() > Game.getWindowWidth() + margin ||
               bullet.getY() < -margin ||
               bullet.getY() > Game.getWindowHeight() + margin;
    }

    private boolean checkPlayerBulletCollisionReturnHit(Bullet bullet){
        if(!player.isAlive())
            return false;
        if(!bullet.isColli)
            return false;

        if (bullet.checkCollisionWithPlayer(player)) {
            // 碰撞
            if (!player.isHurt()) {
                // 播放受伤音效
                soundManager.playSE("player_hurt");
                // 玩家受伤
                player.takeDamage(bullet.getDamage());
                player.setHurt(true);
                // 使用定时器恢复
                scheduleHurtRecovery();
            }
            return true;
        }
        return false;
    }

    private void scheduleHurtRecovery() {
        // 使用单线程定时器池
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                player.setHurt(false);
                timer.cancel();
            }
        }, player.getInvisibleTime());
    }

    public void renderFightScene(){
        renderFightScene(true, true);
    }

    public void renderFightScene(boolean renderBullets, boolean renderPlayer){
        // bullets
        if(renderBullets) {
            bulletRenderer.clearBulletRenderData();
            for (Bullet bullet : bullets) {
                Texture currentTexture = bullet.getCurrentTexture();
                if (currentTexture != null) {
                    BulletRenderer.BulletRenderData br = bulletRenderer.new BulletRenderData(
                        bullet.getId(),
                        currentTexture.getId(),
                        bullet.getX(),
                        bullet.getY(),
                        bullet.getSelfAngle(),
                        bullet.getHScale(),
                        bullet.getVScale(),
                        currentTexture.getWidth(),
                        currentTexture.getHeight(),
                        bullet.rgba,
                        bullet.getAnimation() != null
                    );
                    bulletRenderer.addBulletRenderData(br);
                }
            }
            bulletRenderer.renderBullets();
        }

        //player
        if(renderPlayer) {
            player.renderLight();
            player.render();
        }
    }

    // Start player's light expansion effect (call at round start)
    public void startPlayerLightExpansion() {
        if (player != null) {
            player.startLightExpansion();
        }
    }

    public void renderBattleMenuScene(boolean renderPlayer){
        // player
        if(renderPlayer) {
            player.render();
        }
    }

    public void clearBullets() {
        bullets.clear();
    }

    public void allowPlayerMovement(boolean allow) {
        player.isMovable = allow;
        if(!allow) {
            player.setDirection(0.0f, 0.0f);
        }
    }

    public void initPlayerPosition() {
        // 初始化玩家位置到战斗框中央
        float startX = Game.getFrameLeft() + (Game.getFrameWidth() - player.getWidth()) / 2.0f;
        float startY = Game.getFrameBottom() - (Game.getFrameHeight() + player.getHeight()) / 2.0f;
        player.setPosition(startX, startY);
    }

    public void resetPlayerLight() {
        player.setTargetLightRadius(LightLevel.NORMAL);
    }

    public boolean isPlayerAlive() {
        return player.isAlive();
    }

    public void resetGame() {
        // 重置玩家
        player.reset();
        // 重置敌人
        enemyManager.resetEnemies();
        // 清空子弹
        clearBullets();
    }
}
