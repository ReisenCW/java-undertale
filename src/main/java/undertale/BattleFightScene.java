package undertale;

public class BattleFightScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private int round;

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
        round = 1;
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

        objectManager.UpdateFightScene(deltaTime);
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