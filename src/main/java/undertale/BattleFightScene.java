package undertale;

public class BattleFightScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
    }

    @Override
    public void onEnter() {
        uiManager.setSelected(-1);
    }

    @Override
    public void onExit() {
        uiManager.setSelected(0);
    }

    @Override
    public void update(float deltaTime) {
        // test
        Texture testTexture = textureManager.getTexture("test_bullet");
        int randomX = (int)(Math.random() * (Game.getWindowWidth() - 20));
        objectManager.createBullet(randomX, 0, 
        0, 90, 200, 4, testTexture);

        objectManager.UpdateFightScene(deltaTime);
    }

    @Override
    public void render() {
        uiManager.renderBattleUI();
        objectManager.renderFightScene();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_FIGHT;
    }
}