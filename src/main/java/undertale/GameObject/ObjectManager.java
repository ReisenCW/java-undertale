package undertale.GameObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.Game;
import undertale.Texture.Texture;

public class ObjectManager {
    private Player player;
    private ArrayList<Bullet> bullets;
    // 对象池复用子弹, 避免频繁创建和销毁子弹, 减少gc压力和内存分配开销
    private ArrayList<Bullet> bulletPool;
    // 声明为成员变量避免频繁创建临时列表
    private ArrayList<Bullet> toRemove = new ArrayList<>();
    private EnemyManager enemyManager;

    public ObjectManager(Player player){
        init(player);
    }

    private void init(Player player){
        this.player = player;
        bullets = new ArrayList<>();
        bulletPool = new ArrayList<>();
        enemyManager = EnemyManager.getInstance();
    }

    public Bullet createBullet(float x, float y, float selfAngle, float speedAngle,
                            float speed, int damage, Texture texture){
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

        if (CollisionDetector.checkRectCircleCollision(bullet, player)) {
            // 碰撞
            if (!player.isHurt()) {
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
            for (Bullet bullet : bullets) {
                bullet.render();
            }
        }

        //player
        if(renderPlayer) {
            player.render();
        }
    }

    public void renderMenuScene(){
        // player
        player.render();
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
}
