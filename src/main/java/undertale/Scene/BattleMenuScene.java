package undertale.Scene;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;
import undertale.UI.state.MenuStateType;
import static org.lwjgl.glfw.GLFW.*;

public class BattleMenuScene extends Scene {
    private EnemyManager enemyManager;
    private int round;
    private long battleFrameResetTime = 1000; // 1000ms

    private String[] roundMessages = {
        "* The titan appeared.",
        "* A swarm is coming.",
        "* The titan's hands are moving."
    };

    public BattleMenuScene(ObjectManager objectManager, InputManager inputManager, UIManager uiManager, EnemyManager enemyManager) {
        super(objectManager, inputManager, uiManager);
        this.enemyManager = enemyManager;
        init();
    }

    @Override
    public void init() {
        round = 0;
    }

    @Override
    public void onEnter() {
        registerAsObserver();
        if(!soundManager.isMusicPlaying("titan_battle")) {
            soundManager.playMusic("titan_battle");
        }
        objectManager.allowPlayerMovement(false);
        objectManager.clearBullets();
        uiManager.resetVars(MenuStateType.MAIN);
        round = Math.min(++round, roundMessages.length);
    }

    @Override
    public void onExit() {
        unregisterAsObserver();
        uiManager.setMenuState(MenuStateType.MAIN);
        uiManager.setSelected(-1);
    }

    @Override
    public void update(float deltaTime) {
        if(!objectManager.isPlayerAlive()) {
            SceneManager.getInstance().switchScene(SceneEnum.GAME_OVER, true);
            return;
        }
        // 开始1s, BattleFrame恢复到原来位置
        resetBattleFrame(deltaTime);
        uiManager.updatePlayerMenuPosition();
        objectManager.updateMenuScene(deltaTime);
        SceneEnum nextScene = SceneEnum.BATTLE_FIGHT;
        if(enemyManager.isAllEnemiesDefeated()) {
            nextScene = SceneEnum.START_MENU;
        }
        SceneManager.getInstance().switchScene(nextScene);
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

    @Override
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates) {
        if(currKeyStates[GLFW_KEY_RIGHT] && !preKeyStates[GLFW_KEY_RIGHT]) {
            uiManager.selectMoveRight();
        }
        if(currKeyStates[GLFW_KEY_LEFT] && !preKeyStates[GLFW_KEY_LEFT]) {
            uiManager.selectMoveLeft();
        }
        if(currKeyStates[GLFW_KEY_Z] && !preKeyStates[GLFW_KEY_Z]) {
            uiManager.handleMenuSelect();
        }
        if(currKeyStates[GLFW_KEY_X] && !preKeyStates[GLFW_KEY_X]) {
            uiManager.handleMenuCancel();
        }
        if(currKeyStates[GLFW_KEY_UP] && !preKeyStates[GLFW_KEY_UP]) {
            uiManager.menuSelectUp();
        }
        if(currKeyStates[GLFW_KEY_DOWN] && !preKeyStates[GLFW_KEY_DOWN]) {
            uiManager.menuSelectDown();
        }
    }
}
