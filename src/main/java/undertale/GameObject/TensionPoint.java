package undertale.GameObject;

import undertale.GameMain.Game;
import undertale.Texture.Texture;

public class TensionPoint extends Collectable{
    private Texture tpTexture;
    private float currentScale;
    private float targetScale;
    private float scaleSpeed;
    private float rotationSpeed;
    private float shrinkDuration;

    public TensionPoint(float x, float y, float initialScale) {
        super(x, y, () -> {
            // 玩家tp+1
            Game.getPlayer().updateTensionPoints(1);
        });
        this.currentScale = initialScale;
        this.targetScale = 1.0f;
        this.shrinkDuration = 1.5f;
        this.scaleSpeed = (initialScale - targetScale) / shrinkDuration; // shrinkDuration秒内缩放到targetScale
        this.canCollect = true;
        // 随机起始角度
        setSelfAngle((float)(Math.random() * 360));
        // 随机旋转速度 (-180 到 180 度/秒)
        this.rotationSpeed = (float)(Math.random() * 360 - 180);
        init();
    }

    private void init() {
        tpTexture = Game.getTexture("tension_point");
    }
    
    @Override
    public void update(float deltaTime) {
        // 旋转
        setSelfAngle(getSelfAngle() + rotationSpeed * deltaTime);
        // 缩放
        if (currentScale > targetScale) {
            currentScale -= scaleSpeed * deltaTime;
            if (currentScale < targetScale) {
                currentScale = targetScale;
                canCollect = true; // 缩放完毕后可收集
            }
        }
        if(canCollect && !isCollected && checkCollisionWithPlayer(Game.getPlayer())) {
            isCollected = true;
            onCollect.run();
            System.out.println("Collected a Tension Point!");
        }
        updatePosition(deltaTime);
    }

    @Override
    public void render() {
        if (tpTexture != null && currentScale > 0) {
            // 金黄色: RGB(1.0, 1.0, 0.0)
            Texture.drawTexture(tpTexture.getId(), x, y, currentScale * tpTexture.getWidth(), currentScale * tpTexture.getHeight(), getSelfAngle(), 1.0f, 1.0f, 0.0f, 1.0f);
        }
    }

    @Override
    public float getWidth() {
        if (tpTexture != null) {
            return currentScale * tpTexture.getWidth();
        }
        return 0;
    }

    @Override
    public float getHeight() {
        if (tpTexture != null) {
            return currentScale * tpTexture.getHeight();
        }
        return 0;
    }

    public float getCurrentScale() {
        return currentScale;
    }

    public void reset(float x, float y, float initialScale) {
        this.x = x;
        this.y = y;
        this.currentScale = initialScale;
        this.targetScale = 1.0f;
        this.scaleSpeed = (initialScale - targetScale) / 2.0f; // 2秒内缩放到targetScale
        this.canCollect = true;
        // 随机起始角度
        setSelfAngle((float)(Math.random() * 360));
        // 随机旋转速度 (-180 到 180 度/秒)
        this.rotationSpeed = (float)(Math.random() * 360 - 180);
    }
}
