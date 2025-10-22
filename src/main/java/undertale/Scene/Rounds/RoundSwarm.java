package undertale.Scene.Rounds;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameObject.Player;
import undertale.GameObject.TitanSpawn;
import undertale.GameObject.ObjectManager;
import undertale.GameMain.Game;

public class RoundSwarm extends Round{
    private ObjectManager objectManager;

    private float spawnTimer = 0f;
    private static final float SPAWN_INTERVAL = 0.5f; // 0.5秒生成一次
    private static final float MIN_RADIUS = 120f;
    private static final float MAX_RADIUS = 200f;
    private Animation titanSpawnAnimation;

    public RoundSwarm(long duration) {
        super(duration);
        objectManager = Game.getObjectManager();
        AnimationManager animationManager = AnimationManager.getInstance();
        titanSpawnAnimation = animationManager.getAnimation("titan_spawn_animation");
    }

    @Override
    public void updateRound(float deltaTime) {
        // 每0.5s生成一个titan spawn, 位置为以player为中心, 半径为80-100的随机位置
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
        TitanSpawn spawn = new TitanSpawn(spawnX, spawnY, 100f, 5, titanSpawnAnimation);
        
        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(spawn);
    }
}
