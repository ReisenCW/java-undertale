package undertale.GameObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import undertale.Animation.Animation;
import undertale.Enemy.EnemyManager;
import undertale.GameMain.Game;
import undertale.GameObject.Bullets.Bullet;
import undertale.GameObject.Bullets.BallBlast;
import undertale.GameObject.Bullets.TitanSpawn;
import undertale.GameObject.Bullets.TitanSnake;
import undertale.GameObject.Collectables.Collectable;
import undertale.GameObject.Collectables.TensionPoint;
import undertale.GameObject.Effects.RippleEffect;
import undertale.GameObject.Effects.TitanSpawnParticle;
import undertale.GameObject.Player.LightLevel;
import undertale.Sound.SoundManager;
import undertale.Texture.Texture;

public class ObjectManager {
    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Bullet> pendingBullets;
    // 对象池复用子弹, 避免频繁创建和销毁子弹, 减少gc压力和内存分配开销
    private ArrayList<Bullet> bulletPool;
    // 声明为成员变量避免频繁创建临时列表
    private ArrayList<Bullet> toRemove;

    // 可拾取物
    private ArrayList<Collectable> collectables;
    private ArrayList<Collectable> collectablePool;
    private ArrayList<Collectable> collectablesToRemove;

    // 涟漪效果
    private ArrayList<RippleEffect> rippleEffects;

    // TitanSpawn 粒子
    private ArrayList<TitanSpawnParticle> titanSpawnParticles;

    private EnemyManager enemyManager;
    private BulletRenderer bulletRenderer;
    private CollectableRenderer collectableRenderer;
    private SoundManager soundManager;

    public ObjectManager(Player player){
        init(player);
    }

    private void init(Player player){
        this.player = player;
        bullets = new ArrayList<>();
        pendingBullets = new ArrayList<>();
        bulletPool = new ArrayList<>();
        toRemove = new ArrayList<>();
        collectables = new ArrayList<>();
        collectablePool = new ArrayList<>();
        collectablesToRemove = new ArrayList<>();
        rippleEffects = new ArrayList<>();
        titanSpawnParticles = new ArrayList<>();
        enemyManager = EnemyManager.getInstance();
        bulletRenderer = new BulletRenderer();
        collectableRenderer = new CollectableRenderer();
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

    private void returnCollectableToPool(Collectable collectable) {
        collectablePool.add(collectable);
    }

    private Collectable getCollectableFromPool() {
        if (collectablePool.isEmpty()) {
            return null;
        }
        // 从对象池中获取collectable并移出对象池
        return collectablePool.remove(collectablePool.size() - 1);
    }

    public Collectable createCollectable(float x, float y, float initialScale) {
        TensionPoint tp = (TensionPoint) getCollectableFromPool();
        if (tp == null) {
            // 如果对象池中没有可用TensionPoint，则创建新的
            tp = new TensionPoint(x, y, initialScale);
        } else {
            // 重置TensionPoint属性
            tp.reset(x, y, initialScale);
        }
        collectables.add(tp);
        return tp;
    }

    public void addBullet(Bullet bullet) {
        if (bullet != null) {
            pendingBullets.add(bullet);
        }
    }

    public void addCollectable(Collectable collectable) {
        if (collectable != null) {
            collectables.add(collectable);
        }
    }

    public void addTitanSpawnParticle(TitanSpawnParticle particle) {
        if (particle != null) {
            titanSpawnParticles.add(particle);
        }
    }

    public void updateMenuScene(float deltaTime){
        // enemy
        enemyManager.update(deltaTime);
        // player
        player.update(deltaTime);
    }

    public void updateFightScene(float deltaTime){
        // 先添加 pending bullets
        bullets.addAll(pendingBullets);
        pendingBullets.clear();

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

            // 如果是TitanSpawn或TitanSnake并且已标记为删除，加入移除列表
            if (bullet instanceof TitanSpawn) {
                TitanSpawn ts = (TitanSpawn) bullet;
                if (ts.isMarkedForRemoval()) {
                    toRemove.add(bullet);
                    continue;
                }
            }
            if (bullet instanceof TitanSnake) {
                TitanSnake ts = (TitanSnake) bullet;
                if (ts.isMarkedForRemoval()) {
                    toRemove.add(bullet);
                    continue;
                }
            }

            // 检查TitanSpawn与BallBlast的碰撞
            if (bullet instanceof TitanSpawn && bullet.isColli) {
                for (Bullet other : bullets) {
                    if (other instanceof BallBlast && other != bullet) {
                        if (CollisionDetector.checkCircleCollision(bullet, other)) {
                            ((TitanSpawn) bullet).markForRemovalWithoutTP();
                            toRemove.add(bullet);
                            break;
                        }
                    }
                }
            }

            // 先检查子弹是否超出屏幕边界
            float margin = Math.max(bullet.getWidth(), bullet.getHeight()) / 2.0f;
            if (bullet.bound && isOutOfBounds(bullet, margin)) {
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

        // Collectables
        collectablesToRemove.clear();
        for (Collectable collectable : collectables) {
            collectable.update(deltaTime);
            // 如果超出边界
            float margin = Math.max(collectable.getWidth(), collectable.getHeight()) / 2.0f;
            if (isOutOfBounds(collectable, margin)) {
                collectablesToRemove.add(collectable);
                continue;
            }
            if(collectable.isCollected()) {
                // 如果是TensionPoint，创建涟漪效果
                if (collectable instanceof TensionPoint) {
                    rippleEffects.add(new RippleEffect(collectable.getX(), collectable.getY()));
                }
                collectablesToRemove.add(collectable);
                continue;
            }
        }
        // 移除已收集的collectables
        for (Collectable collectable : collectablesToRemove) {
            collectables.remove(collectable);
            returnCollectableToPool(collectable);
        }

        // 更新涟漪效果
        for (int i = rippleEffects.size() - 1; i >= 0; i--) {
            RippleEffect effect = rippleEffects.get(i);
            effect.update(deltaTime);
            if (!effect.isActive()) {
                rippleEffects.remove(i);
            }
        }

        // 更新 TitanSpawn 粒子
        for (int i = titanSpawnParticles.size() - 1; i >= 0; i--) {
            TitanSpawnParticle particle = titanSpawnParticles.get(i);
            particle.update(deltaTime);
            if (!particle.isActive()) {
                titanSpawnParticles.remove(i);
            }
        }
    }

    private boolean isOutOfBounds(GameObject obj, float margin) {
        return obj.getX() < -margin ||
               obj.getX() > Game.getWindowWidth() + margin ||
               obj.getY() < -margin ||
               obj.getY() > Game.getWindowHeight() + margin;
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
        renderFightScene(true, true, true);
    }

    public void renderFightScene(boolean renderBullets, boolean renderPlayer, boolean renderCollectables){
        // collectables
        if(renderCollectables) {
            collectableRenderer.clearCollectables();
            for (Collectable collectable : collectables) {
                collectableRenderer.addCollectable(collectable);
            }
            collectableRenderer.renderCollectables();
        }
        // 渲染涟漪效果
        for (RippleEffect effect : rippleEffects) {
            effect.render();
        }
        // 渲染 TitanSpawn 粒子
        for (TitanSpawnParticle particle : titanSpawnParticles) {
            particle.render();
        }
        // bullets
        if(renderBullets) {
            bulletRenderer.clearBullets();
            for (Bullet bullet : bullets) {
                bulletRenderer.addBullet(bullet);
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
        bulletPool.clear();
    }

    public void clearRipples() {
        rippleEffects.clear();
    }

    public void clearTitanSpawnParticles() {
        titanSpawnParticles.clear();
    }


    public void clearCollectables() {
        collectables.clear();
        collectablePool.clear();
    }

    public void allowPlayerMovement(boolean allow) {
        player.isMovable = allow;
        if(!allow) {
            player.setSpeedX(0);
            player.setSpeedY(0);
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

    public void destroy() {
        bullets.clear();
        bulletPool.clear();
    }
}
