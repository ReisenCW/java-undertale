package undertale.Scene;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;

public class BattleMenuScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private int round;
    private long roundTime;
    private long battleFrameResetTime = 1000; // 1000ms

    private String[] roundMessages = {
        "* The titan appeared.",
        "* A swarm is coming.",
        "* The titan's hands are moving."
    };

    public BattleMenuScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
        round = 0;
        roundTime = 0;
    }

    @Override
    public void onEnter() {
        objectManager.allowPlayerMovement(false);
        objectManager.clearBullets();
        uiManager.resetVars();
        round = Math.min(++round, roundMessages.length);
        roundTime = 0;
    }

    @Override
    public void onExit() {
        uiManager.menuState = UIManager.MenuState.MAIN;
        uiManager.setSelected(-1);
    }

    @Override
    public void update(float deltaTime) {
        // 开始1s, BattleFrame恢复到原来位置
        roundTime += deltaTime * 1000;
        if (roundTime < 1000) {
            resetBattleFrame(deltaTime);
        }
        uiManager.updatePlayerMenuPosition();
        objectManager.updateMenuScene(deltaTime);
        SceneManager.getInstance().switchScene(SceneEnum.BATTLE_FIGHT);
    }

    @Override
    public void render() {
        enemyManager.render();
        uiManager.renderBattleUI();
        uiManager.renderFrameContents(roundMessages[round - 1]);
        objectManager.renderBattleMenuScene(uiManager.isRenderPlayer());
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_MENU;
    }

    private void resetBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, battleFrameResetTime, uiManager.MENU_FRAME_WIDTH, uiManager.MENU_FRAME_HEIGHT, uiManager.MENU_FRAME_LEFT, uiManager.MENU_FRAME_BOTTOM);
    }
}