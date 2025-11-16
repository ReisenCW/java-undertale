package undertale.GameObject.Bullets;

import undertale.Animation.Animation;

public class TitanSnake extends Bullet{
    public TitanSnake(float x, float y, float angleDeg, float speed, int damage, Animation animation) {
        super(x, y, angleDeg, speed, 0, damage, animation);
        setNavi(false);
        this.destroyableOnHit = false;
        // 创建动画副本，避免多个实例共享同一个动画状态
    }
}
