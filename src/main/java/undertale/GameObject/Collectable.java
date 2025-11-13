package undertale.GameObject;

public abstract class Collectable extends GameObject{
    protected boolean canCollect;
    protected Runnable onCollect;
    protected boolean isCollected;

    public Collectable(float x, float y, Runnable onCollect) {
        this.x = x;
        this.y = y;
        this.canCollect = true;
        this.isCollected = false;
        this.onCollect = onCollect;
        this.isColli = true;
    }

    public boolean canCollect() {
        return canCollect;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public abstract void render();
    public abstract void update(float deltaTime);
}
