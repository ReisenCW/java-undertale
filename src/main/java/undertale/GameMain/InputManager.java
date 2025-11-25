package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import undertale.Interfaces.InputObserver;
import undertale.Scene.SceneManager;
import undertale.UI.UIManager;

public class InputManager {
    private Window window;
    private boolean[] keyStates = new boolean[GLFW_KEY_LAST + 1];
    private boolean[] wasKeyPressed = new boolean[GLFW_KEY_LAST + 1];
    private List<InputObserver> observers = new ArrayList<>();
    
    // scene
    private SceneManager sceneManager;

    // UI
    private UIManager uiManager;

    InputManager(Window window, EscapeInputObserver escapeObserver) {
        this.window = window;
        this.sceneManager = SceneManager.getInstance();
        this.uiManager = UIManager.getInstance();
        addObserver(escapeObserver);
    }

    public void addObserver(InputObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(InputObserver observer) {
        observers.remove(observer);
    }

    private void updateKeyState() {
        glfwPollEvents();
        // 先保存上一帧状态
        for (int key = GLFW_KEY_SPACE; key <= GLFW_KEY_LAST; key++) {
            wasKeyPressed[key] = keyStates[key];
        }
        // 再更新当前帧状态
        for (int key = GLFW_KEY_SPACE; key <= GLFW_KEY_LAST; key++) {
            keyStates[key] = (glfwGetKey(window.getWindow(), key) == GLFW_PRESS);
        }
    }

    private void handleDebug(){
        // 按下f12切换debug模式
        if(Game.DEBUG && isKeyTriggered(GLFW_KEY_F12)) {
            Game.DEBUG = false;
            System.out.println("Debug mode OFF");
        } else if(!Game.DEBUG && isKeyTriggered(GLFW_KEY_F12)) {
            Game.DEBUG = true;
            System.out.println("Debug mode ON");
        }
    }

    private void handleMenuChoose() {
        if(isKeyTriggered(GLFW_KEY_RIGHT)) {
            uiManager.selectMoveRight();
        }
        if(isKeyTriggered(GLFW_KEY_LEFT)) {
            uiManager.selectMoveLeft();
        }
        if(isKeyTriggered(GLFW_KEY_Z)) {
            uiManager.handleMenuSelect();
        }
        if(isKeyTriggered(GLFW_KEY_X)) {
            uiManager.handleMenuCancel();
        }
        if(isKeyTriggered(GLFW_KEY_UP)) {
            uiManager.menuSelectUp();
        }
        if(isKeyTriggered(GLFW_KEY_DOWN)) {
            uiManager.menuSelectDown();
        }
    }

    private void processGameOverInput() {
        // 当文字输出完毕后按下Z键确认
        if(isKeyTriggered(GLFW_KEY_Z)) {
            uiManager.handleGameOverConfirm();
        }
        // 按下X键全部显示
        if(isKeyTriggered(GLFW_KEY_X)) {
            uiManager.handleGameOverSkip();
        }
    }

    private void processBeginMenuInput() {
        if(uiManager.menuState != UIManager.MenuState.BEGIN) return;
        if(isKeyTriggered(GLFW_KEY_UP)) {
            uiManager.beginMenuSelectUp();
        }
        if(isKeyTriggered(GLFW_KEY_DOWN)) {
            uiManager.beginMenuSelectDown();
        }
        if(isKeyTriggered(GLFW_KEY_Z)) {
            uiManager.handleBeginMenuSelect();
        }
    }

    public void processInput() {
        updateKeyState();
        handleDebug();
        for(InputObserver observer : observers) {
            observer.processInput(wasKeyPressed, keyStates);
        }
        switch(sceneManager.getCurrentScene().getCurrentScene()) {
            case BATTLE_MENU -> handleMenuChoose();
            case GAME_OVER -> processGameOverInput();
            case START_MENU -> processBeginMenuInput();
            default -> {
            }
        }
    }

    /**
     * 检查某个键当前是否被按下
     */
    public boolean isKeyPressed(int key) {
        if (key < GLFW_KEY_SPACE || key > GLFW_KEY_LAST) return false;
        return keyStates[key];
    }

    /**
     * 检查某个键是否被触发（从未按下到按下）
     */
    public boolean isKeyTriggered(int key){
        if (key < GLFW_KEY_SPACE || key > GLFW_KEY_LAST) return false;
        return keyStates[key] && !wasKeyPressed[key];
    }
}