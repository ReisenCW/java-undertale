package undertale.GameObject;

import undertale.Animation.Animation;
import undertale.Texture.Texture;

public class Bullet extends GameObject{
    private static int nextId = 0;
    private int id;
    private int damage;
    private float hScale;
    private float vScale;
    protected float[] rgba;
    public boolean bound;
    public boolean destroyableOnHit;
    public boolean isColli;
    protected Texture texture;
    protected Animation animation;


    public Bullet(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Texture texture) {
        this.id = nextId++;
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // 默认白色不透明
        this.x = x;
        this.y = y;
        setSelfAngle(selfAngle);
        setSpeedAngle(speedAngle);
        this.speed = speed;
        this.damage = damage;
        this.texture = texture;
        this.animation = null;
        this.hScale = 1.0f;
        this.vScale = 1.0f;
        this.bound = true;
        this.destroyableOnHit = true;
        this.isColli = true;
    }

    public Bullet(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Animation animation) {
        this.id = nextId++;
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // 默认白色不透明
        this.x = x;
        this.y = y;
        setSelfAngle(selfAngle);
        setSpeedAngle(speedAngle);
        this.speed = speed;
        this.damage = damage;
        this.texture = null;
        this.animation = animation;
        this.hScale = 1.0f;
        this.vScale = 1.0f;
        this.bound = true;
        this.destroyableOnHit = true;
    }

    @Override
    public void update(float deltaTime) {
        updatePosition(deltaTime);
        if (animation != null) {
            animation.updateAnimation(deltaTime);
        }
    }

    public void setColor(float r, float g, float b, float a) {
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
    }

    public int getId() {
        return id;
    }

    public void render(){
        Texture currentTexture = getCurrentTexture();
        if (currentTexture != null) {
            Texture.drawTexture(currentTexture.getId(),
                                this.x, this.y,
                                hScale * currentTexture.getWidth(), vScale * currentTexture.getHeight(),
                                getSelfAngle(), rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }

    public Texture getCurrentTexture() {
        if (animation != null) {
            return animation.getCurrentFrame();
        }
        return texture;
    }

    public int getDamage() {
        return damage;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getHScale() {
        return hScale;
    }

    public void setHScale(float hScale) {
        // 调整x以保持中心位置不变
        this.x -= (hScale - this.hScale) * getWidth() / 2.0f;
        this.hScale = hScale;
    }

    public float getVScale() {
        return vScale;
    }

    public void setVScale(float vScale) {
        // 调整y以保持中心位置不变
        this.y -= (vScale - this.vScale) * getHeight() / 2.0f;
        this.vScale = vScale;
    }

    @Override
    public float getWidth() {
        Texture currentTexture = getCurrentTexture();
        return currentTexture != null ? hScale * currentTexture.getWidth() : 0;
    }

    @Override
    public float getHeight() {
        Texture currentTexture = getCurrentTexture();
        return currentTexture != null ? vScale * currentTexture.getHeight() : 0;
    }

    public void reset(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Texture texture) {
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // 默认白色不透明
        this.x = x;
        this.y = y;
        setSelfAngle(selfAngle);
        setSpeedAngle(speedAngle);
        this.speed = speed;
        this.damage = damage;
        this.texture = texture;
        this.animation = null;
        this.hScale = 1.0f;
        this.vScale = 1.0f;
        this.destroyableOnHit = true;
        this.bound = true;
    }

    public void reset(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Animation animation) {
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // 默认白色不透明
        this.x = x;
        this.y = y;
        setSelfAngle(selfAngle);
        setSpeedAngle(speedAngle);
        this.speed = speed;
        this.damage = damage;
        this.texture = null;
        this.animation = animation;
        this.hScale = 1.0f;
        this.vScale = 1.0f;
        this.destroyableOnHit = true;
        this.bound = true;
    }

    public boolean checkCollisionWithPlayer(Player player) {
        if (!player.isAlive() || !this.isColli) {
            return false;
        }
        return CollisionDetector.checkRectCircleCollision(this, player);
    }
}