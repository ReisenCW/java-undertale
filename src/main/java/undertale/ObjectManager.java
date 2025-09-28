package undertale;

import java.util.ArrayList;

public class ObjectManager {
    private Player player;
    private ArrayList<Bullet> bullets;
    private EnemyManager enemyManager;

    ObjectManager(Player player){
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

    public void updateMenuScene(float deltaTime){
        // enemy
        enemyManager.update(deltaTime);
        // player
        player.update(deltaTime);
    }

    public void UpdateFightScene(float deltaTime){
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
        }
        for (Bullet bullet : toRemove) {
            bullets.remove(bullet);
        }
    }

    private boolean checkPlayerBulletCollisionReturnHit(Bullet bullet){
        if(!player.isAlive()) 
            return false;
        // 椭圆子弹判定：仿射变换到单位圆
        float a = bullet.getWidth() / 2; // 长轴
        float b = bullet.getHeight() / 2; // 短轴
        float theta = (float)Math.toRadians(bullet.getSelfAngle()); // 椭圆旋转角度
        float bulletCenterX = bullet.getX() + a;
        float bulletCenterY = bullet.getY() + b;

        float playerRadius = Math.min(player.getWidth(), player.getHeight()) / 3;
        float playerCenterX = player.getX() + player.getWidth() / 2;
        float playerCenterY = player.getY() + player.getHeight() / 2;

        // 步骤1：平移
        float px = playerCenterX - bulletCenterX;
        float py = playerCenterY - bulletCenterY;
        // 步骤2：旋转（逆时针-θ）
        float cosT = (float)Math.cos(-theta);
        float sinT = (float)Math.sin(-theta);
        float rx = px * cosT - py * sinT;
        float ry = px * sinT + py * cosT;
        // 步骤3：缩放
        float sx = rx / a;
        float sy = ry / b;
        // 玩家半径也缩放，取均值
        float rScaled = playerRadius / ((a + b) / 2);

        // 判定：距离是否小于rScaled + 1（单位圆半径）
        float dist = (float)Math.sqrt(sx * sx + sy * sy);
        if (dist < rScaled + 1) {
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
