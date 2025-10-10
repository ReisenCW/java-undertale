package undertale;

import java.util.ArrayList;

public class BattleFightScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private int round;
    private ArrayList<Round> rounds;

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
        round = 1;
        rounds = new ArrayList<>();
        Round round1 = new Round(10000); // 10秒
    }

    @Override
    public void onEnter() {
        uiManager.setSelected(-1);
        objectManager.initPlayerPosition();
    }

    @Override
    public void onExit() {
        uiManager.setSelected(0);
        objectManager.clearBullets();
    }

    @Override
    public void update(float deltaTime) {
        // test
        Texture testTexture = textureManager.getTexture("test_bullet");
        int randomX = (int)(Math.random() * (Game.getWindowWidth() - 20));
        objectManager.createBullet(randomX, 0, 
        0, 90, 200, 4, testTexture);

        objectManager.updateFightScene(deltaTime);
        uiManager.updatePlayerInBound();
        SceneManager.getInstance().switchScene(SceneEnum.BATTLE_MENU);
    }

    @Override
    public void render() {
        enemyManager.render();
        uiManager.renderBattleUI(null);
        objectManager.renderFightScene();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_FIGHT;
    }
}