package undertale;

public class BattleMenuScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();

    public BattleMenuScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
    }

    @Override
    public void onEnter() {
        uiManager.menuState = UIManager.MenuState.MAIN;
        uiManager.selectedEnemy = 0;
        uiManager.selectedAct = 0;
        uiManager.selectedItem = 0;
        uiManager.selectedAction = 0;
        objectManager.clearBullets();
        uiManager.setSelected(0);
    }

    @Override
    public void onExit() {
        uiManager.menuState = UIManager.MenuState.MAIN;
        uiManager.setSelected(-1);
    }

    @Override
    public void update(float deltaTime) {
        objectManager.updateMenuScene(deltaTime);
    }

    @Override
    public void render() {
        enemyManager.render();
        uiManager.renderBattleUI();
        objectManager.renderFightScene();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_MENU;
    }
}