package undertale.Scene.Rounds;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameObject.Player;
import undertale.GameObject.Bullets.TitanSpawn;
import undertale.GameObject.Bullets.TitanSwarmRed;
import undertale.UI.UIManager;
import undertale.GameObject.ObjectManager;
import undertale.GameMain.Game;

public class RoundSwarm extends Round{
    private ObjectManager objectManager;
    private UIManager uiManager;

    private float spawnTimer = 0f;
    private float redSpawnTimer = 0f;
    private final float SPAWN_INTERVAL;
    private final float SPAWN_RED_INTERVAL;
    private final float MIN_RADIUS = 260f;
    private final float MAX_RADIUS = 300f;
    private Animation[] titanSpawnAnimation = new Animation[3];
    private final float edge;
    private final float centerX;
    private final float centerY;
    private final int intensity;

    public RoundSwarm(int intensity, long duration, long frameMoveTime) {
        super(duration, frameMoveTime);
        objectManager = Game.getObjectManager();
        uiManager = UIManager.getInstance();
        AnimationManager animationManager = AnimationManager.getInstance();
        titanSpawnAnimation[0] = animationManager.getAnimation("titan_spawn_animation");
        titanSpawnAnimation[1] = animationManager.getAnimation("spawn_evolver_animation");
        titanSpawnAnimation[2] = animationManager.getAnimation("spawn_mouse_animation");

        this.intensity = intensity;
        this.edge = 400.0f;
        this.centerX = Game.getWindowWidth() / 2.0f;
        this.centerY = Game.getWindowHeight() / 2.0f;
        switch(intensity) {
            case 1:
                SPAWN_INTERVAL = 0.5f;
                SPAWN_RED_INTERVAL = 3.0f;
                break;
            case 2:
                SPAWN_INTERVAL = 0.4f;
                SPAWN_RED_INTERVAL = 2.0f;
                break;
            case 3:
                SPAWN_INTERVAL = 0.2f;
                SPAWN_RED_INTERVAL = -1.0f;
                break;
            default:
                SPAWN_INTERVAL = 0.5f;
                SPAWN_RED_INTERVAL = 3.0f;
                break;
        }
    }

    private int type = 0;
    @Override
    public void updateRound(float deltaTime) {
        // 每 SPAWN_INTERVAL 生成一个titan spawn, 位置为以player为中心, 半径为 MIN_RADIUS - MAX_RADIUS 的随机位置
        // 每 SPAWN_RED_INTERVAL 生成一个red spawn
        spawnTimer += deltaTime;
        redSpawnTimer += deltaTime;
        
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnTitanSpawn(type);
            type = (type + 1) % Math.min(intensity, titanSpawnAnimation.length);
        }
        if(SPAWN_RED_INTERVAL > 0.0f && redSpawnTimer >= 0.0f){
            redSpawnTimer += deltaTime;
            if (redSpawnTimer >= SPAWN_RED_INTERVAL) {
                redSpawnTimer -= SPAWN_RED_INTERVAL;
                spawnRed();
            }
        }
    }

    @Override
    public void onEnter() {
        type = 0;
        // 重置计时器
        spawnTimer = 0f;
        redSpawnTimer = 0f;
    }

    private void spawnRed() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        float angle = (float)(Math.random() * 2 * Math.PI);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        // 计算生成位置（以玩家中心为圆心）
        float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(angle) * radius);
        float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(angle) * radius);

        // 创建TitanSwarmRed
        TitanSwarmRed redSwarm = new TitanSwarmRed(spawnX, spawnY, 5);
        objectManager.addBullet(redSwarm);

    }

    private void spawnTitanSpawn(int type) {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        float angle = (float)(Math.random() * 2 * Math.PI);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        // 计算生成位置（以玩家中心为圆心）
        float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(angle) * radius);
        float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(angle) * radius);

        // 创建TitanSpawn
        TitanSpawn spawn = new TitanSpawn(spawnX, spawnY, 120f, 5, titanSpawnAnimation[type]);
        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(spawn);
    }

    @Override
    public void moveBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, frameMoveTime, edge, edge, centerX - edge / 2, centerY + edge / 2);
    }
}
