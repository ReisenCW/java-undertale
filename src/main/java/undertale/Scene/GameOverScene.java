package undertale.Scene;

import undertale.GameMain.Game;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;

public class GameOverScene extends Scene {
    private boolean bgmStarted = false;
    public GameOverScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
    }

    @Override
    public void onEnter() {
        // 延后播放 BGM: 在心碎动画结束后播放 BGM
        bgmStarted = false;
        soundManager.stopMusic();
        uiManager.resetGameOver();
        sceneManager.reset();
    }

    @Override
    public void onExit() {
        // 重置全局游戏对象（玩家、敌人、子弹）
        Game.resetGame(UIManager.MenuState.MAIN);
        // 重新初始化战斗场景（回到第一轮等初始状态）
        Scene battleFight = SceneManager.getInstance().getScene(SceneEnum.BATTLE_FIGHT);
        if (battleFight != null) {
            battleFight.init();
        }
    }

    @Override
    public void update(float deltaTime) {
        uiManager.updateGameOver(deltaTime);
        // 在心碎动画结束时启动 BGM（只启动一次）
        if (!bgmStarted && uiManager.isGameOverHeartAnimFinished()) {
            soundManager.playMusic("gameover");
            bgmStarted = true;
        }
        sceneManager.switchScene(SceneEnum.BATTLE_MENU);
    }

    @Override
    public void render() {
        uiManager.renderGameOver();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.GAME_OVER;
    }

    @Override
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates) {}
}
