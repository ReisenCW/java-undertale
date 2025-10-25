package undertale.Scene;

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
    }

    @Override
    public void update(float deltaTime) {
        uiManager.updateGameOver(deltaTime);
        // TODO:重返标题界面
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
