package undertale.GameObject.Collectables;

import static org.lwjgl.opengl.GL20.*;

import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Texture.Texture;

public class TensionPoint extends Collectable{
    private boolean turn = false;
    private Texture tpTexture;
    private float currentScale;
    private float initialScale;
    private float targetScale = 0.7f;
    private float scaleSpeed;
    private float rotationSpeed = (Math.random() >= 0.5 ? -1 : 1) * (float) (580 + Math.random() * 20);
    private float shrinkDuration = 0.8f;
    
    // 移动相关
    private float initialSpeed = 120f;
    private float maxSpeed = 150f;
    private float initialAngle = 0.0f;

    public TensionPoint(float x, float y, float initialScale) {
        super(x, y, () -> {
            // 玩家tp+1
            Game.getPlayer().updateTensionPoints(1);
        });
        this.currentScale = initialScale;
        this.initialScale = initialScale;
        this.scaleSpeed = (initialScale - targetScale) / shrinkDuration; // shrinkDuration秒内缩放到targetScale
        this.canCollect = false;
        this.isNavi = false;
        this.setSpeed(initialSpeed); // 初始速度
        // 随机起始角度
        setSelfAngle((float)(Math.random() * 360));
        init();
    }

    private void init() {
        tpTexture = Game.getTexture("tension_point");

        // 获取玩家位置
        Player player = Game.getPlayer();
        float playerX = player.getX();
        float playerY = player.getY();
        
        // 计算到玩家的向量
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if(dist > 0.0f){
            initialAngle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 180.0f;
        } else {
            initialAngle = 0.0f;
        }
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
                turn = true;
                canCollect = true;
                setSpeed(0); // 缩放结束时速度为0
            } else {
                // 缩放期间：远离玩家并减速
                // 远离玩家的方向（取反）
                setSpeedAngle(initialAngle);
                // 减速到0
                setSpeed(Math.max(0, getSpeed() - (initialSpeed / shrinkDuration) * deltaTime));
            }
        } else if (turn) {
            // 缩放结束后：朝向玩家加速
            // 获取玩家位置
            Player player = Game.getPlayer();
            float playerX = player.getX();
            float playerY = player.getY();
            
            // 计算到玩家的向量
            float dx = playerX - x;
            float dy = playerY - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                float moveAngle = (float) Math.atan2(dy, dx);
                setSpeedAngle((float) Math.toDegrees(moveAngle));
                // 加速朝向玩家，速度上限为maxSpeed
                setSpeed(Math.min(maxSpeed, getSpeed() + maxSpeed * deltaTime * 1)); // 1秒内加速到maxSpeed
            }
        }
        
        if(canCollect && !isCollected && checkCollisionWithPlayer(Game.getPlayer())) {
            isCollected = true;
            onCollect.run();
        }
        updatePosition(deltaTime);
    }

    @Override
    public void render() {
        if (tpTexture != null && currentScale > 0) {
            // 金黄色: RGB(1.0, 1.0, 0.0)
            Texture.drawTexture(tpTexture.getId(), x, y, currentScale * tpTexture.getWidth(), currentScale * tpTexture.getHeight(), getSelfAngle(), 1.0f, 1.0f, 0.0f, 1.0f, false, false, "tp_shader", program -> {
                int locScreenSize = glGetUniformLocation(program, "uScreenSize");
                int locColor = glGetUniformLocation(program, "uColor");
                int locTexture = glGetUniformLocation(program, "uTexture");
                int locWhiteStrength = glGetUniformLocation(program, "uWhiteStrength");
                glUniform2i(locScreenSize, Game.getWindowWidth(), Game.getWindowHeight());
                glUniform4f(locColor, 1.0f , 1.0f, 0.0f, 1.0f);
                glUniform1i(locTexture, 0);
                glUniform1f(locWhiteStrength, (currentScale - targetScale) / (initialScale - targetScale));
            });
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
        this.canCollect = false;
        this.turn = false;
        this.setSpeed(initialSpeed); // 重置初始速度
        // 随机起始角度
        setSelfAngle((float)(Math.random() * 360));
        // 随机旋转速度 (-180 到 180 度/秒)
        rotationSpeed = (Math.random() >= 0.5 ? -1 : 1) * (float) (160 + Math.random() * 20);

    }
}
