package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import undertale.GameObject.Player;
import undertale.Interfaces.InputObserver;
import undertale.Scene.SceneManager;
import undertale.UI.UIManager;
import undertale.Utils.Timer;

public class InputManager {
    private Window window;
    private boolean[] keyStates = new boolean[GLFW_KEY_LAST + 1];
    private boolean[] wasKeyPressed = new boolean[GLFW_KEY_LAST + 1];
    private List<InputObserver> observers = new ArrayList<>();
    
    // escape
    public final long ESCAPE_HOLD_TIME = 2000; // 按住2秒退出
    private boolean isEscaping = false;
    private Timer escapeTimer = new Timer();
    
    // player
    private Player player;

    // scene
    private SceneManager sceneManager;

    // UI
    private UIManager uiManager;

    InputManager(Window window, Player player) {
        this.window = window;
        this.player = player;
        this.sceneManager = SceneManager.getInstance();
        this.uiManager = UIManager.getInstance();
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

    private void handleEscaping() {
        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!isEscaping) {
                isEscaping = true;
                escapeTimer.setTimerStart();
            }
            // 按住ESCAPE键超过2秒则退出
            if(escapeTimer.isTimeElapsed(ESCAPE_HOLD_TIME)) {
                org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose(window.getWindow(), true);
                return;
            }
        } else {
            if (isEscaping) {
                isEscaping = false;
            }
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

    private void handlePlayerMovement() {
        // if(!player.isMovable || !player.isAlive()) {
        //     player.setDirectionX(0);
        //     player.setDirectionY(0);
        //     return;
        // }
        // // 通过上下左右箭头移动
        // if (isKeyPressed(GLFW_KEY_UP))
        //     player.setDirectionY(-1);
        // else if (isKeyPressed(GLFW_KEY_DOWN))
        //     player.setDirectionY(1);
        // else
        //     player.setDirectionY(0);
        // if (isKeyPressed(GLFW_KEY_LEFT))
        //     player.setDirectionX(-1);
        // else if (isKeyPressed(GLFW_KEY_RIGHT))
        //     player.setDirectionX(1);
        // else
        //     player.setDirectionX(0);
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
        handleEscaping();
        handleDebug();
        for(InputObserver observer : observers) {
            observer.processInput(wasKeyPressed, keyStates);
        }
        switch(sceneManager.getCurrentScene().getCurrentScene()) {
            case BATTLE_FIGHT -> handlePlayerMovement();
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

    public boolean isEscaping() {
        return isEscaping;
    }

    public Timer getEscapeTimer() {
        return escapeTimer;
    }
}