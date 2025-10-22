package undertale.GameObject;

import undertale.Texture.Texture;

public class Bullet extends GameObject{
    private int damage;

    private float hScale;
    private float vScale;
    private Texture texture;
    
    private boolean destroyableOnHit = true;

    private float[] rgba;


    public Bullet(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Texture texture) {
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // 默认白色不透明
        this.x = x;
        this.y = y;
        setSelfAngle(selfAngle);
        setSpeedAngle(speedAngle);
        this.speed = speed;
        this.damage = damage;
        this.texture = texture;
        this.hScale = 1.0f;
        this.vScale = 1.0f;
    }

    @Override
    public void update(float deltaTime) {
        updatePosition(deltaTime);
    }

    public void setColor(float r, float g, float b, float a) {
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
    }

    public void render(){
        Texture.drawTexture(texture.getId(),
                            this.x, this.y,
                            hScale * texture.getWidth(), vScale * texture.getHeight(), 
                            getSelfAngle(), rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public int getDamage() {
        return damage;
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
        this.hScale = hScale;
    }

    public float getVScale() {
        return vScale;
    }

    public void setVScale(float vScale) {
        this.vScale = vScale;
    }

    @Override
    public float getWidth() {
        return hScale * texture.getWidth();
    }

    @Override
    public float getHeight() {
        return vScale * texture.getHeight();
    }

    public void setDestroyableOnHit(boolean destroyableOnHit) {
        this.destroyableOnHit = destroyableOnHit;
    }

    public boolean isDestroyableOnHit() {
        return destroyableOnHit;
    }
}