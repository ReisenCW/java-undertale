package undertale.Scene.Rounds;

import undertale.GameMain.Game;
import undertale.GameObject.ObjectManager;
import undertale.GameObject.Bullets.TitanFingers;
import undertale.Texture.TextureManager;
import undertale.UI.UIManager;

public class RoundFinger extends Round {
    private ObjectManager objectManager;
    private UIManager uiManager;
    private int intensity;
    private boolean spawnedFinger = false;
    private final float edge;
    private final float centerX;
    private final float centerY;
    // 0: 朝右, 1: 朝左
    private TitanFingers[] titanFingers = new TitanFingers[2];

    public RoundFinger(int intensity, long duration, long frameMoveTime) {
        super(duration, frameMoveTime);
        this.objectManager = Game.getObjectManager();
        this.uiManager = UIManager.getInstance();
        this.intensity = intensity;
        this.edge = 400.0f;
        this.centerX = Game.getWindowWidth() / 2.0f;
        this.centerY = Game.getWindowHeight() / 2.0f;
    }

    public void onEnter() {
        spawnedFinger = false;
    }

    @Override
    public void updateRound(float deltaTime) {
        if(!spawnedFinger){
            spawnedFinger = true;
            // spawn titan fingers
            float scale = 3.4f;
            float randomTime = 0.3f + (float)(Math.random() * 0.6f); // +-0.3秒随机时间
            float palmHeight = TextureManager.getInstance().getTexture("titan_palm").getHeight() * scale;
            float palmY = Game.getWindowHeight() / 2 - palmHeight / 2;
            float randomY = (float)(Math.random() * 6.0f) * scale;
            titanFingers[0] = new TitanFingers(0.0f, palmY + randomY, intensity, 5, 1, scale, randomTime);
            randomY = (float)(Math.random() * 6.0f) * scale;
            titanFingers[1] = new TitanFingers(Game.getWindowWidth() - 250.0f, palmY + randomY, intensity, 5, -1, scale, randomTime);
            objectManager.addBullet(titanFingers[0]);
            objectManager.addBullet(titanFingers[1]);
        }
    }

    @Override
    public void moveBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, frameMoveTime, edge, edge, centerX - edge / 2, centerY + edge / 2);
    }
}
