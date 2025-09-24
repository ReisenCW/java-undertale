package undertale;

public class Bullet extends GameObject{
    private int damage;
    private float hScale;
    private float vScale;
    private Texture texture;
    private float[] rgba;

    private boolean destroyableOnHit = true;

    public Bullet(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Texture texture) {
        rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // 默认白色不透明
        init(x, y, selfAngle, speedAngle, speed, damage, texture);
    }
    private void init(float x, float y, float selfAngle, float speedAngle, float speed, int damage, Texture texture){
        setPosition(x, y);
        setAngle(speedAngle);
        setSelfAngle(selfAngle);
        setSpeed(speed);
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
                            getX(), getY(), 
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

    public float getWidth() {
        return hScale * texture.getWidth();
    }

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