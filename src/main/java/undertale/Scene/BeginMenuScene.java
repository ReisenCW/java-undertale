package undertale.Scene;

import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;

public class BeginMenuScene extends Scene {
    public BeginMenuScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
    }

    @Override
    public void init() {
        uiManager.resetBeginMenu();
    }

    @Override
    public void onEnter() {
        if(!soundManager.isMusicPlaying("main_menu")) {
            soundManager.playMusic("main_menu");
        }
        uiManager.resetBeginMenu();
    }

    @Override
    public void onExit() {}

    @Override
    public void update(float deltaTime) {
        sceneManager.switchScene(SceneEnum.BATTLE_MENU);
    }

    @Override
    public void render() {
        uiManager.renderBeginMenu();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.START_MENU;
    }
}
