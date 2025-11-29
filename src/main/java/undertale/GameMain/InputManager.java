package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import undertale.Interfaces.InputObserver;

// 使用observer模式重构输入管理
public class InputManager {
    private Window window;
    private boolean[] keyStates = new boolean[GLFW_KEY_LAST + 1];
    private boolean[] wasKeyPressed = new boolean[GLFW_KEY_LAST + 1];
    // 观察者列表
    private List<InputObserver> observers = new ArrayList<>();
    
    InputManager(Window window) {
        this.window = window;
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

    public void processInput() {
        updateKeyState();

        // 当前帧和上一帧的按键状态传递给观察者
        for(InputObserver observer : observers) {
            observer.processInput(wasKeyPressed, keyStates);
        }
    }

}