package undertale;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager {
    private Window window;
    private boolean[] keyStates = new boolean[GLFW_KEY_LAST + 1];
    
    // escape
    private static final long ESCAPE_HOLD_TIME = 2000; // 按住2秒退出
    private boolean isEscaping = false;
    private Timer escapeTimer = new Timer();
    
    // player
    private Player player;

    InputManager(Window window, Player player) {
        this.window = window;
        this.player = player;
    }

    private void updateKeyState() {
        glfwPollEvents();
        // 从SPACE开始GLFW才允许遍历
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

    private void handlePlayerMovement() {
        // 通过上下左右箭头移动
        if (isKeyPressed(GLFW_KEY_UP))
            player.setDirectionY(-1);
        else if (isKeyPressed(GLFW_KEY_DOWN))
            player.setDirectionY(1);
        else
            player.setDirectionY(0);
        if (isKeyPressed(GLFW_KEY_LEFT))
            player.setDirectionX(-1);
        else if (isKeyPressed(GLFW_KEY_RIGHT))
            player.setDirectionX(1);
        else
            player.setDirectionX(0);
    }

    public void processInput() {
        updateKeyState();
        handleEscaping();
        handlePlayerMovement();
    }

    public boolean isKeyPressed(int key) {
        if (key < GLFW_KEY_SPACE || key > GLFW_KEY_LAST) return false;
        return keyStates[key];
    }

    public boolean isEscaping() {
        return isEscaping;
    }
}