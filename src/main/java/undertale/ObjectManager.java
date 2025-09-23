package undertale;

import java.util.ArrayList;

public class ObjectManager {
    private Player player;
    private ArrayList<Bullet> bullets;

    ObjectManager(Player player){
        init(player);
    }

    private void init(Player player){
        this.player = player;
        bullets = new ArrayList<>();
    }

    public Bullet createBullet(float x, float y, float selfAngle, float speedAngle, 
                            float speed, int damage, Texture texture){
        Bullet bullet = new Bullet(x, y, selfAngle, speedAngle, speed, damage, texture);
        bullets.add(bullet);
        return bullet;
    }

    public void update(float deltaTime){
        // player
        player.update(deltaTime);

        // bullets（先收集需要删除的子弹，遍历后统一移除）
        ArrayList<Bullet> toRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);
            if (checkPlayerBulletCollisionReturnHit(bullet)) {
                toRemove.add(bullet);
            }
        }
        for (Bullet bullet : toRemove) {
            bullets.remove(bullet);
        }
    }

    private boolean checkPlayerBulletCollisionReturnHit(Bullet bullet){
        float a = player.getWidth() / 3;
        float b = player.getHeight() / 3;
        float playerRadius = Math.min(a, b);
        float centerX = player.getX() + player.getWidth() / 2;
        float centerY = player.getY() + player.getHeight() / 2;

        float ba = bullet.getWidth() / 3;
        float bb = bullet.getHeight() / 3;
        float bulletRadius = Math.min(ba, bb);
        float bulletCenterX = bullet.getX() + bullet.getWidth() / 2;
        float bulletCenterY = bullet.getY() + bullet.getHeight() / 2;

        float dx = bulletCenterX - centerX;
        float dy = bulletCenterY - centerY;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

        if (dist < playerRadius + bulletRadius) {
            // 碰撞
            if (!player.isHurt()) {
                player.takeDamage(bullet.getDamage());
                player.setHurt(true);
                //TEST
                System.out.println("Player is hurt! , Player Health: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
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

    public void render(){
        //player 
        player.render();

        // bullets
        for (Bullet bullet : bullets) {
            bullet.render();
        }
    }
}
