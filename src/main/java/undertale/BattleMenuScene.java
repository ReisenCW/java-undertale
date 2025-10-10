package undertale;

public class BattleMenuScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private int round;

    private String[] roundMessages = {
        "The titan appeared.",
        "A swarm is coming.",
        "The titan's hands are moving."
    };

    public BattleMenuScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
        round = 0;
    }

    @Override
    public void onEnter() {
        objectManager.clearBullets();
        uiManager.resetVars();
        round = Math.min(++round, roundMessages.length);
    }

    @Override
    public void onExit() {
        uiManager.menuState = UIManager.MenuState.MAIN;
        uiManager.setSelected(-1);
    }

    @Override
    public void update(float deltaTime) {
        uiManager.updatePlayerMenuPosition();
        objectManager.updateMenuScene(deltaTime);
        SceneManager.getInstance().switchScene(SceneEnum.BATTLE_FIGHT);
    }

    @Override
    public void render() {
        enemyManager.render();
        uiManager.renderBattleUI(roundMessages[round - 1]);
        objectManager.renderFightScene(false, uiManager.isRenderPlayer());
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_MENU;
    }
}