package undertale.Scene.Rounds;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameObject.Player;
import undertale.GameObject.Bullets.TitanSnake;
import undertale.GameObject.Bullets.TitanSpawn;
import undertale.UI.UIManager;
import undertale.GameObject.ObjectManager;
import undertale.GameMain.Game;

public class RoundSnake extends Round{
    private ObjectManager objectManager;
    private UIManager uiManager;

    private float spawnTimer = 0f;
    private static final float SPAWN_INTERVAL = 0.9f; // 0.7秒生成一次
    private static final float MIN_RADIUS = 250f;
    private static final float MAX_RADIUS = 300f;
    private Animation titanSpawnAnimation;

    private final float edge;
    private final float centerX;
    private final float centerY;
    private final int intensity;
    private boolean snakeSpawned = false;

    public RoundSnake(int intensity, long duration, long frameMoveTime) {
        super(duration, frameMoveTime);
        objectManager = Game.getObjectManager();
        uiManager = UIManager.getInstance();
        AnimationManager animationManager = AnimationManager.getInstance();
        titanSpawnAnimation = animationManager.getAnimation("titan_snake_body");

        this.intensity = intensity;
        this.edge = 400.0f;
        this.centerX = Game.getWindowWidth() / 2.0f;
        this.centerY = Game.getWindowHeight() / 2.0f;
    }

    @Override
    public void updateRound(float deltaTime) {
        // 每 SPAWN_INTERVAL 生成一个titan spawn, 位置为以player为中心, 半径为 MIN_RADIUS - MAX_RADIUS 的随机位置
        spawnTimer += deltaTime;
        
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnBullets();
        }
    }

    private void spawnBullets() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        float baseAngle = (float)(Math.random() * 2 * Math.PI);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        if(!snakeSpawned){
            int spawnNum = switch(intensity) {
                case 1 , 2 -> 2;
                case 3 -> 3;
                default -> 2;
            };
            for(int i = 0; i < spawnNum; i++) {
                float angle = baseAngle + i * (float)(2 * Math.PI / spawnNum);
                float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(angle) * radius);
                float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(angle) * radius);
                // 创建TitanSnake
                TitanSnake snake = new TitanSnake(spawnX, spawnY, 3, 5);
                // 将spawn添加到objectManager的bullets列表中
                objectManager.addBullet(snake);
            }
            snakeSpawned = true;
        }
        float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(baseAngle) * radius);
        float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(baseAngle) * radius);
        // 创建TitanSpawn
        TitanSpawn spawn = new TitanSpawn(spawnX, spawnY, 80f, 5, titanSpawnAnimation);
        spawn.setNavi(true);

        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(spawn);
    }

    @Override
    public void moveBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, frameMoveTime, edge, edge, centerX - edge / 2, centerY + edge / 2);
    }

    @Override
    public void onEnter() {
        // 重置计时器和状态
        spawnTimer = 0f;
        snakeSpawned = false;
    }
}
