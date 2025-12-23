package undertale.Scene;

import undertale.Enemy.EnemyManager;
import undertale.Enemy.Titan;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;
import undertale.UI.state.MenuStateType;
import static org.lwjgl.glfw.GLFW.*;

public class BattleMenuScene extends Scene {
    private EnemyManager enemyManager;
    private long battleFrameResetTime = 1000; // 1000ms
    private boolean isFirstRound = true;
    private String roundMessage = "";

    public BattleMenuScene(ObjectManager objectManager, InputManager inputManager, UIManager uiManager, EnemyManager enemyManager) {
        super(objectManager, inputManager, uiManager);
        this.enemyManager = enemyManager;
        init();
    }

    @Override
    public void init() {
        isFirstRound = true;
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
        roundMessage = getRoundMessage();
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
        uiManager.renderFrameContents(roundMessage);
        objectManager.renderBattleMenuScene(uiManager.isRenderPlayer());
    }

    private String getRoundMessage() {
        // 1) first round
        // 2) titan defense down -> show attack message
        // 3) TP >= 80 && titan not weakened -> suggest UNLEASH
        // 4) based on current round kind

        // First round message
        if (isFirstRound) {
            isFirstRound = false;
            return String.join("\n",
                "* Darkness constricts you...",
                "* TP Gain reduced outside of ???"
            );
        }

        BattleFightScene fight = (BattleFightScene) SceneManager.getInstance().getScene(SceneEnum.BATTLE_FIGHT);
        BattleFightScene.RoundKind kind = BattleFightScene.RoundKind.UNKNOWN;
        if (fight != null) kind = fight.getCurrentRoundKind();

        // Titan weakened
        boolean titanWeakened = false;
        if (enemyManager != null && enemyManager.getCurrentEnemy() instanceof Titan) {
            Titan titan = (Titan) enemyManager.getCurrentEnemy();
            titanWeakened = titan.isWeakened();
            if (titanWeakened) {
                return "* Attack!!! Its defense is down!!!";
            }
        }

        // TP > threshold -> suggest UNLEASH if titan is NOT weakened
        int tp = 0;
        if (objectManager != null && objectManager.getPlayer() != null) {
            tp = objectManager.getPlayer().getTensionPoints();
        }
        if (tp >= 80 && !titanWeakened) {
            return String.join("\n",
                "* The atmosphere feels tense...",
                "* (You can use UNLEASH!)"
            );
        }

        // Round-specific messages
        switch (kind) {
            case SWARM:
                return String.join("\n",
                    "* The dark flows...",
                    "* A swarm is coming."
                );
            case SNAKE:
                return "* Darkness stares at you.";
            case FINGER:
                return String.join("\n",
                    "* For a moment,",
                    "* You felt your heart being gripped"
                );
            default:
                return "* The ground shudders...";
        }
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
