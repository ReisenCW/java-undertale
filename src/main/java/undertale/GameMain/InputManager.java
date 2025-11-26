package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import undertale.Interfaces.InputObserver;
public class InputManager {
    private Window window;
    private boolean[] keyStates = new boolean[GLFW_KEY_LAST + 1];
    private boolean[] wasKeyPressed = new boolean[GLFW_KEY_LAST + 1];
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
        // DEBUG: if Z pressed this frame, log observers for troubleshooting
        // input polling - no debug logging

        for(InputObserver observer : observers) {
            observer.processInput(wasKeyPressed, keyStates);
        }
    }

    // keyName helper removed: no longer needed for debug output
}