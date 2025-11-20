package undertale.GameObject.Bullets;

import undertale.GameObject.CollisionDetector;
import undertale.GameObject.Player;
import undertale.Texture.Texture;
import undertale.Texture.TextureManager;

public class BallBlast extends Bullet {
    private float scaleChangeInterval;
    private float baseScale;
    private float whiteScale;
    private float amp;

    private float scaleTimer = 0f;

    private Texture whiteEdgeTexture;

    public BallBlast(float x, float y, float speed, float speedAngle, float baseScale, float scaleChangeInterval, float amp, int damage) {
        super(x, y, speedAngle, speedAngle, speed, damage, TextureManager.getInstance().getTexture("titan_blast_black"));
        whiteEdgeTexture = TextureManager.getInstance().getTexture("titan_blast");
        this.scaleChangeInterval = scaleChangeInterval;
        this.baseScale = baseScale;
        this.whiteScale = baseScale;
        this.setVScale(baseScale);
        this.setHScale(baseScale);
        this.amp = amp;
        this.destroyableOnHit = false;
        this.bound = false;
    }

    @Override
    public void update(float deltaTime) {
        // 等待后开始移动
        updatePosition(deltaTime);

        scaleTimer += deltaTime;
        if(scaleTimer >= scaleChangeInterval) {
            scaleTimer -= scaleChangeInterval;
            changeScale();
        }
    }

    enum ScaleState {
        LARGE,
        SMALL
    }
    private ScaleState currentScaleState = ScaleState.SMALL;
    private void changeScale() {
        if(currentScaleState == ScaleState.SMALL) {
            // 变大
            whiteScale = baseScale + amp * 2;
            setHScale(baseScale + amp);
            setVScale(baseScale + amp);
            currentScaleState = ScaleState.LARGE;
        } else {
            // 变小
            whiteScale = baseScale;
            setHScale(baseScale);
            setVScale(baseScale);
            currentScaleState = ScaleState.SMALL;
        }
    }

    @Override
    public boolean hasAnimation() {
        return true;
    }

    @Override
    public void render() {
        float whiteX = this.x + this.getWidth() / 2 - whiteEdgeTexture.getWidth() / 2 * whiteScale;
        float whiteY = this.y + this.getHeight() / 2 - whiteEdgeTexture.getHeight() / 2 * whiteScale;
        // 先渲染白色边缘
        Texture.drawTexture(whiteEdgeTexture.getId(), whiteX, whiteY, whiteEdgeTexture.getWidth() * whiteScale, whiteEdgeTexture.getHeight() * whiteScale, this.getSelfAngle(), 1.0f, 1.0f, 1.0f, 1.0f);
        // 再渲染黑色主体
        super.render();
    }

    @Override
    public boolean checkCollisionWithPlayer(Player player) {
        if (!player.isAlive() || !this.isColli) {
            return false;
        }
        return CollisionDetector.checkCircleCollision(this, player, 0);
    }
}