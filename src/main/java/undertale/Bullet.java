package undertale;

import static org.lwjgl.opengl.GL11.*;

public class Bullet extends GameObject{
    private int damage;
    private float hScale;
    private float vScale;
    private Texture texture;

    public Bullet(float x, float y, float selfAngle, float speedAngle, float speed, int damage, String texturePath) {
        init(x, y, selfAngle, speedAngle, speed, damage, texturePath);
    }
    private void init(float x, float y, float selfAngle, float speedAngle, float speed, int damage, String texturePath){
        setPosition(x, y);
        setAngle(speedAngle);
        setSelfAngle(selfAngle);
        setSpeed(speed);
        this.damage = damage;
        this.texture = new Texture(texturePath);
    }

    @Override
    public void update(float deltaTime) {
        updatePosition(deltaTime);
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

    public void destroyTexture() {
        glDeleteTextures(texture.getId());
    }
}
