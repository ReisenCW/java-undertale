package undertale.Scene.Rounds;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameObject.Player;
import undertale.GameObject.TitanSpawn;
import undertale.UI.UIManager;
import undertale.GameObject.ObjectManager;
import undertale.GameMain.Game;

public class RoundSwarm extends Round{
    private ObjectManager objectManager;
    private UIManager uiManager;

    private float spawnTimer = 0f;
    private static final float SPAWN_INTERVAL = 0.3f; // 0.3秒生成一次
    private static final float MIN_RADIUS = 210f;
    private static final float MAX_RADIUS = 260f;
    private Animation titanSpawnAnimation;

    private final float edge;
    private final float centerX;
    private final float centerY;

    public RoundSwarm(long duration, long frameMoveTime) {
        super(duration, frameMoveTime);
        objectManager = Game.getObjectManager();
        uiManager = UIManager.getInstance();
        AnimationManager animationManager = AnimationManager.getInstance();
        titanSpawnAnimation = animationManager.getAnimation("titan_spawn_animation");

        edge = 400.0f;
        centerX = Game.getWindowWidth() / 2.0f;
        centerY = Game.getWindowHeight() / 2.0f;
    }

    @Override
    public void updateRound(float deltaTime) {
        // 每 SPAWN_INTERVAL 生成一个titan spawn, 位置为以player为中心, 半径为 MIN_RADIUS - MAX_RADIUS 的随机位置
        spawnTimer += deltaTime;
        
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnTitanSpawn();
        }
    }

    private void spawnTitanSpawn() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        float angle = (float)(Math.random() * 2 * Math.PI);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        // 计算生成位置（以玩家为中心）
        float spawnX = player.getX() + (float)(Math.cos(angle) * radius);
        float spawnY = player.getY() + (float)(Math.sin(angle) * radius);

        // 创建TitanSpawn
        TitanSpawn spawn = new TitanSpawn(spawnX, spawnY, 110f, 5, titanSpawnAnimation);
        
        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(spawn);
    }

    @Override
    public void moveBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, frameMoveTime, edge, edge, centerX - edge / 2, centerY + edge / 2);
    }
}
