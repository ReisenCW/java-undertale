package undertale.Scene.Rounds;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.GameObject.Bullets.TitanSpawn;

public class RoundSpecial extends Round {
    private float frameLeft = 100;
    private float frameBottom = 100;
    private float frameWidth = Game.getWindowWidth() - frameLeft * 2;
    private float frameHeight = Game.getWindowHeight() / 2 - frameBottom * 2;
    private float spawnTimer;
    private float shootTimer;
    private float cooldownTimer;
    private final float SHOOT_INTERVAL = 0.2f;
    private final float SHOOT_COOLDOWN = 2.5f;
    private final float SPAWN_INTERVAL = 1.2f;
    private final int SHOOT_NUM;
    private final float MIN_RADIUS = 300f;
    private final float MAX_RADIUS = 380f;

    private Animation titanSpawnAnimation;
    private AnimationManager animationManager;

    public RoundSpecial(int intensity, long duration, long frameMoveTime) {
        super(duration, frameMoveTime);
        SHOOT_NUM = intensity;
        animationManager = AnimationManager.getInstance();
        titanSpawnAnimation = animationManager.getAnimation("titan_spawn_animation");
    }

    @Override
    public void onEnter() {
        // 特殊回合进入时的逻辑
        spawnTimer = 0f;
        shootTimer = 0f;
        cooldownTimer = 0f;
    }
    
    @Override
    public void updateRound(float deltaTime) {
        spawnTimer += deltaTime;
        shootTimer += deltaTime;
        cooldownTimer += deltaTime;
        
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnTitanSpawn();
        }
    }

    private void spawnTitanSpawn() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        // angle: -60 ~ 60, 120 ~ 240 度范围内
        float angle = (float)(Math.random() * 120.0f - 60.0f);
        if (Math.random() < 0.5f) {
            angle += 180.0f;
        }
        angle = (float)Math.toRadians(angle);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        // 计算生成位置（以玩家中心为圆心）
        float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(angle) * radius);
        float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(angle) * radius);

        // 创建TitanSpawn
        TitanSpawn spawn = new TitanSpawn(spawnX, spawnY, 80f, 5, titanSpawnAnimation);
        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(spawn);
    }

    @Override
    public void moveBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, frameMoveTime, frameWidth, frameHeight, frameLeft, frameBottom);
    }
}
