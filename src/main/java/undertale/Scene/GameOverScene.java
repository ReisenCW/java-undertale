package undertale.Scene;

import undertale.GameMain.Game;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;

public class GameOverScene extends Scene {
    public GameOverScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
    }

    @Override
    public void onEnter() {
        uiManager.resetGameOver();
    }

    @Override
    public void onExit() {
        // 重置全局游戏对象（玩家、敌人、子弹）
        Game.resetGame();
        // 重新初始化战斗场景（回到第一轮等初始状态）
        Scene battleFight = SceneManager.getInstance().getScene(SceneEnum.BATTLE_FIGHT);
        if (battleFight != null) {
            battleFight.init();
        }
    }

    @Override
    public void update(float deltaTime) {
        uiManager.updateGameOver(deltaTime);
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
}
