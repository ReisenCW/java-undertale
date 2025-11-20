package undertale.Scene.Rounds;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameObject.Player;
import undertale.GameObject.Bullets.TitanSnake;
import undertale.GameObject.Bullets.TitanSpawn;
import undertale.GameMain.Game;

public class RoundSnake extends Round{
    private float spawnTimer = 0f;
    private float snakeSpawnTimer = 0f;
    private final float SPAWN_INTERVAL;
    private final float SNAKE_SPAWN_INTERVAL;
    private final int SNAKE_NUM;
    private final int SNAKE_BODY_NUM;
    private int snakeSpawnedCount = 0;
    private final float MIN_RADIUS = 250f;
    private final float MAX_RADIUS = 300f;
    private Animation titanSpawnAnimation;

    private final float edge;
    private final float centerX;
    private final float centerY;

    public RoundSnake(int intensity, long duration, long frameMoveTime) {
        super(duration, frameMoveTime);
        AnimationManager animationManager = AnimationManager.getInstance();
        titanSpawnAnimation = animationManager.getAnimation("titan_snake_body");

        this.edge = 400.0f;
        this.centerX = Game.getWindowWidth() / 2.0f;
        this.centerY = Game.getWindowHeight() / 2.0f;
        switch(intensity) {
            case 1:
                SPAWN_INTERVAL = 0.9f;
                SNAKE_NUM = 2;
                SNAKE_BODY_NUM = 2;
                SNAKE_SPAWN_INTERVAL = 1.5f;
                break;
            case 2:
                SPAWN_INTERVAL = 0.7f;
                SNAKE_NUM = 2;
                SNAKE_BODY_NUM = 4;
                SNAKE_SPAWN_INTERVAL = 1.5f;
                break;
            case 3:
                SPAWN_INTERVAL = 0.5f;
                SNAKE_NUM = 3;
                SNAKE_BODY_NUM = 1;
                SNAKE_SPAWN_INTERVAL = 0.0f;
                break;
            default:
                SPAWN_INTERVAL = 0.9f;
                SNAKE_NUM = 2;
                SNAKE_BODY_NUM = 2;
                SNAKE_SPAWN_INTERVAL = 1.5f;
                break;
        }
    }

    @Override
    public void updateRound(float deltaTime) {
        // 每 SPAWN_INTERVAL 生成一个titan spawn, 位置为以player为中心, 半径为 MIN_RADIUS - MAX_RADIUS 的随机位置
        spawnTimer += deltaTime;
        snakeSpawnTimer += deltaTime;
        
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnTitanSpawn();
        }

        if(snakeSpawnTimer >= SNAKE_SPAWN_INTERVAL && snakeSpawnedCount < SNAKE_NUM) {
            snakeSpawnTimer -= SNAKE_SPAWN_INTERVAL;
            if(SNAKE_SPAWN_INTERVAL == 0.0f) {
                spawnAllSnakes();
            } else {
                spawnSnake();
            }
        }
    }

    private void spawnTitanSpawn() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        float baseAngle = (float)(Math.random() * 2 * Math.PI);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(baseAngle) * radius);
        float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(baseAngle) * radius);
        // 创建TitanSpawn
        TitanSpawn spawn = new TitanSpawn(spawnX, spawnY, 110f, 0.6f, 5, titanSpawnAnimation);
        spawn.setNavi(true);

        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(spawn);
    }

    private void spawnAllSnakes() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        float baseAngle = (float)(Math.random() * 2 * Math.PI);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        for(int i = 0; i < SNAKE_NUM; i++) {
            float angle = baseAngle + i * (float)(2 * Math.PI / SNAKE_NUM);
            float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(angle) * radius);
            float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(angle) * radius);
            // 创建TitanSnake
            TitanSnake snake = new TitanSnake(spawnX, spawnY, SNAKE_BODY_NUM, 5);
            // 将spawn添加到objectManager的bullets列表中
            objectManager.addBullet(snake);
            snakeSpawnedCount++;
        }
    }

    public void spawnSnake() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        float angle = (float)(Math.random() * 2 * Math.PI);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        // 计算生成位置（以玩家中心为圆心）
        float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(angle) * radius);
        float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(angle) * radius);

        // 创建TitanSnake
        TitanSnake snake = new TitanSnake(spawnX, spawnY, SNAKE_BODY_NUM, 5);
        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(snake);
        snakeSpawnedCount++;
    }

    @Override
    public void moveBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, frameMoveTime, edge, edge, centerX - edge / 2, centerY + edge / 2);
    }

    @Override
    public void onEnter() {
        // 重置计时器和状态
        spawnTimer = 0f;
        snakeSpawnedCount = 0;
    }

    
    @Override
    public void render() {}
}
