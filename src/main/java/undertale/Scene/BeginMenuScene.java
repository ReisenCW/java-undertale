package undertale.Scene;

import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;
import undertale.UI.state.MenuStateType;
import static org.lwjgl.glfw.GLFW.*;

public class BeginMenuScene extends Scene {
    public BeginMenuScene(ObjectManager objectManager, InputManager inputManager, UIManager uiManager) {
        super(objectManager, inputManager, uiManager);
    }

    @Override
    public void init() {
        uiManager.resetBeginMenu();
    }

    @Override
    public void onEnter() {
        registerAsObserver();
        if(!soundManager.isMusicPlaying("main_menu")) {
            soundManager.playMusic("main_menu");
        }
        // 确保UI处于BEGIN状态并重置Begin Menu相关变量
        objectManager.resetGame();
        uiManager.resetVars(MenuStateType.BEGIN);
        uiManager.resetBeginMenu();
    }

    @Override
    public void onExit() {
        unregisterAsObserver();
    }

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

    @Override
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates) {
        if(uiManager.getMenuStateType() != MenuStateType.BEGIN) return;
        if(currKeyStates[GLFW_KEY_UP] && !preKeyStates[GLFW_KEY_UP]) {
            uiManager.beginMenuSelectUp();
        }
        if(currKeyStates[GLFW_KEY_DOWN] && !preKeyStates[GLFW_KEY_DOWN]) {
            uiManager.beginMenuSelectDown();
        }
        if(currKeyStates[GLFW_KEY_Z] && !preKeyStates[GLFW_KEY_Z]) {
            uiManager.handleBeginMenuSelect();
        }
    }
}
