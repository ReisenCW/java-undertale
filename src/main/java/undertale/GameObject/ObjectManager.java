package undertale.GameObject;

import java.util.ArrayList;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.Game;
import undertale.Texture.Texture;

public class ObjectManager {
    private Player player;
    private ArrayList<Bullet> bullets;
    private EnemyManager enemyManager;

    public ObjectManager(Player player){
        init(player);
    }

    private void init(Player player){
        this.player = player;
        bullets = new ArrayList<>();
        enemyManager = EnemyManager.getInstance();
    }

    public Bullet createBullet(float x, float y, float selfAngle, float speedAngle, 
                            float speed, int damage, Texture texture){
        Bullet bullet = new Bullet(x, y, selfAngle, speedAngle, speed, damage, texture);
        bullets.add(bullet);
        return bullet;
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

        // bullets（先收集需要删除的子弹，遍历后统一移除）
        ArrayList<Bullet> toRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);
            if(!player.isAlive())
                continue;
            if (checkPlayerBulletCollisionReturnHit(bullet)) {
                toRemove.add(bullet);
            }
            // 判断子弹是否超出窗口
        }
        for (Bullet bullet : toRemove) {
            bullets.remove(bullet);
        }
    }

    private boolean checkPlayerBulletCollisionReturnHit(Bullet bullet){
        if(!player.isAlive()) 
            return false;
        if (CollisionDetector.checkRectCircleCollision(bullet, player)) {
            // 碰撞
            if (!player.isHurt()) {
                player.takeDamage(bullet.getDamage());
                player.setHurt(true);
                new Thread(() -> {
                    try {
                        Thread.sleep(player.getInvisibleTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player.setHurt(false);
                }).start();
            }
            return bullet.isDestroyableOnHit();
        }
        return false;
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

    public void initPlayerPosition() {
        // 初始化玩家位置到战斗框中央
        float startX = Game.getFrameLeft() + (Game.getFrameWidth() - player.getWidth()) / 2.0f;
        float startY = Game.getFrameBottom() - (Game.getFrameHeight() + player.getHeight()) / 2.0f;
        player.setPosition(startX, startY);
    }
}
