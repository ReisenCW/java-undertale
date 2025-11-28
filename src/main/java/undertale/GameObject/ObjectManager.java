package undertale.GameObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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

/**
 * ObjectManager 管理游戏对象生命周期与分层（layers）。
 *
 * 已重构说明（Refactor note）:
 * 本类采用组合层（Composite layers）管理活动对象（bulletsLayer, collectablesLayer, effectsLayer），
 * 并由对象自身负责 render()/update()，不再通过集中式渲染器强制渲染所有对象。
 */
public class ObjectManager {
    private Player player;
    // active bullets are held in bulletsLayer (composite) — do not maintain a separate list
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

    // Composite root and layers (POC)
    private GameObjectComposite root;
    private GameObjectComposite bulletsLayer;
    private GameObjectComposite collectablesLayer;
    private GameObjectComposite effectsLayer;

    private EnemyManager enemyManager;
    // renderers deprecated: objects render themselves now
    private SoundManager soundManager;

    public ObjectManager(Player player, EnemyManager enemyManager){
        init(player, enemyManager);
    }

    /**
     * No-arg constructor for tests that don't need a fully initialized Player.
     * Initializes internal collections and layers only.
     */
    ObjectManager() {
        init(null, null);
    }

    private void init(Player player, EnemyManager enemyManager){
        this.player = player;
        this.enemyManager = enemyManager;
        // bullets list removed; bulletsLayer will hold active bullets
        pendingBullets = new ArrayList<>();
        bulletPool = new ArrayList<>();
        toRemove = new ArrayList<>();
        collectables = new ArrayList<>();
        collectablePool = new ArrayList<>();
        collectablesToRemove = new ArrayList<>();
        rippleEffects = new ArrayList<>();
        titanSpawnParticles = new ArrayList<>();
        // create composite root and layers
        root = new GameObjectComposite();
        bulletsLayer = new GameObjectComposite();
        collectablesLayer = new GameObjectComposite();
        effectsLayer = new GameObjectComposite();
        root.addChild(bulletsLayer);
        root.addChild(collectablesLayer);
        root.addChild(effectsLayer);
        // In test-only construction (player == null) avoid triggering global singletons
        if (player != null) {
            // bulletRenderer / collectableRenderer removed in favor of GameObject.render()
            soundManager = SoundManager.getInstance();
        } else {
            soundManager = null;
        }
    }

    // NOTE: createBullet() factory methods were removed — scenes / rounds directly construct bullets and use addBullet().

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

    // NOTE: createCollectable() factory method removed — rounds/scenes typically construct collectables directly and call addCollectable().

    public void addBullet(Bullet bullet) {
        if (bullet != null) {
            pendingBullets.add(bullet);
        }
    }

    public void addCollectable(Collectable collectable) {
        if (collectable != null) {
            collectables.add(collectable);
            if (collectablesLayer != null) collectablesLayer.addChild(collectable);
        }
    }

    public void addTitanSpawnParticle(TitanSpawnParticle particle) {
        if (particle != null) {
            titanSpawnParticles.add(particle);
            if (effectsLayer != null) effectsLayer.addChild(particle);
        }
    }

    // Test helper: add a ripple effect to the manager and layer
    void addRippleEffect(RippleEffect ripple) {
        if (ripple != null) {
            rippleEffects.add(ripple);
            if (effectsLayer != null) effectsLayer.addChild(ripple);
        }
    }

    // Package-private accessors used by unit tests to verify layer contents
    GameObjectComposite getEffectsLayer() { return effectsLayer; }
    int getTitanSpawnParticlesCount() { return titanSpawnParticles.size(); }
    int getRippleEffectsCount() { return rippleEffects.size(); }
    int getBulletsCount() {
        if (bulletsLayer == null) return 0;
        int count = 0;
        for (GameObject go : bulletsLayer.getChildren()) {
            if (go instanceof Bullet) count++;
        }
        return count;
    }

    public void updateMenuScene(float deltaTime){
        // enemy
        if (enemyManager != null) enemyManager.update(deltaTime);
        // player
        if (player != null) player.update(deltaTime);
    }

    public void updateFightScene(float deltaTime){
        // 先添加 pending bullets -> add each to bulletsLayer (composite authoritative)
        if (bulletsLayer != null) {
            for (Bullet b : pendingBullets) {
                bulletsLayer.addChild(b);
            }
        }
        pendingBullets.clear();

        // enemy
        if (enemyManager != null) enemyManager.update(deltaTime);
        // player
        if (player != null) player.update(deltaTime);

        // bullets: iterate the bullets layer so composite is authoritative for ordering
        toRemove.clear();
        if (bulletsLayer != null) {
            java.util.List<GameObject> bulletChildren = bulletsLayer.getChildren();
            for (int i = 0; i < bulletChildren.size(); i++) {
                GameObject go = bulletChildren.get(i);
                if (!(go instanceof Bullet)) continue;
                Bullet bullet = (Bullet) go;
                bullet.update(deltaTime);
                if (player == null || !player.isAlive())
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
                // check collision against other bullets in the layer
                for (int j = 0; j < bulletChildren.size(); j++) {
                    GameObject otherGo = bulletChildren.get(j);
                    if (!(otherGo instanceof BallBlast)) continue;
                    Bullet other = (Bullet) otherGo;
                    if (other == bullet) continue;
                    if (CollisionDetector.checkCircleCollision(bullet, other)) {
                        ((TitanSpawn) bullet).markForRemovalWithoutTP();
                        toRemove.add(bullet);
                        break;
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
        }
        // 将要移除的子弹回收到对象池
        for (Bullet bullet : toRemove) {
            if (bulletsLayer != null) bulletsLayer.removeChild(bullet);
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
                            RippleEffect ripple = new RippleEffect(collectable.getX(), collectable.getY());
                            rippleEffects.add(ripple);
                            if (effectsLayer != null) effectsLayer.addChild(ripple);
                }
                    collectablesToRemove.add(collectable);
                continue;
            }
        }
        // 移除已收集的collectables
        for (Collectable collectable : collectablesToRemove) {
            collectables.remove(collectable);
            if (collectablesLayer != null) collectablesLayer.removeChild(collectable);
            returnCollectableToPool(collectable);
        }

        // 更新涟漪效果
        for (int i = rippleEffects.size() - 1; i >= 0; i--) {
            RippleEffect effect = rippleEffects.get(i);
            effect.update(deltaTime);
            if (!effect.isActive()) {
                rippleEffects.remove(i);
                if (effectsLayer != null) effectsLayer.removeChild(effect);
            }
        }

        // 更新 TitanSpawn 粒子
        for (int i = titanSpawnParticles.size() - 1; i >= 0; i--) {
            TitanSpawnParticle particle = titanSpawnParticles.get(i);
            particle.update(deltaTime);
            if (!particle.isActive()) {
                titanSpawnParticles.remove(i);
                if (effectsLayer != null) effectsLayer.removeChild(particle);
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
        if (player == null || !player.isAlive())
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
        // Render using composite layers so objects render themselves and groups
        if (renderCollectables && collectablesLayer != null) {
            collectablesLayer.render();
        }
        // effects layer (ripples, titan spawn particles, etc.)
        if (effectsLayer != null) {
            effectsLayer.render();
        }
        // bullets layer
        if (renderBullets && bulletsLayer != null) {
            bulletsLayer.render();
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
        // bullets list removed; clear layer and object pool
        bulletPool.clear();
        if (bulletsLayer != null) bulletsLayer.clearChildren();
    }

    public void clearRipples() {
        // remove ripple objects from effects layer and clear list
        if (effectsLayer != null) {
            for (RippleEffect r : rippleEffects) {
                effectsLayer.removeChild(r);
            }
        }
        rippleEffects.clear();
    }

    public void clearTitanSpawnParticles() {
        if (effectsLayer != null) {
            for (TitanSpawnParticle p : titanSpawnParticles) {
                effectsLayer.removeChild(p);
            }
        }
        titanSpawnParticles.clear();
    }


    public void clearCollectables() {
        collectables.clear();
        collectablePool.clear();
        if (collectablesLayer != null) collectablesLayer.clearChildren();
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
        // remove all active bullets from layer and clear pool
        if (bulletsLayer != null) bulletsLayer.clearChildren();
        bulletPool.clear();
    }

    public Player getPlayer() {
        return player;
    }

    // Package-private accessor for tests
    GameObjectComposite getBulletsLayer() { return bulletsLayer; }
}
