package undertale;

public class BattleMenuScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();

    public BattleMenuScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
    }

    @Override
    public void onEnter() {
        objectManager.clearBullets();
        uiManager.setSelected(0);
    }

    @Override
    public void onExit() {
        uiManager.setSelected(-1);
    }

    @Override
    public void update(float deltaTime) {
        objectManager.updateMenuScene(deltaTime);
    }

    @Override
    public void render() {
        uiManager.renderBattleUI();
        objectManager.renderFightScene();
        uiManager.updatePlayerInBound();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_MENU;
    }
}