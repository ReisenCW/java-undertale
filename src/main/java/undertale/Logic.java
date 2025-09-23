package undertale;

import static org.lwjgl.glfw.GLFW.*;

public class Logic {
    private static final long ESCAPE_HOLD_TIME = 2000; // 按住2秒退出
    private boolean isEscaping;
    private Timer escapeTimer;

    Logic() {
        init();
    }

    private void init() {
        isEscaping = false;
        escapeTimer = new Timer();
    }

    public void update() {
        glfwPollEvents();
        handleEscape();
        Game.getPlayer().update();
    }

    private void handleEscape() {
        if (Game.isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!isEscaping) {
                isEscaping = true;
                escapeTimer.setTimerStart();
            }
            // 按住ESCAPE键超过2秒则退出
            if(escapeTimer.isTimeElapsed(ESCAPE_HOLD_TIME)) {
                glfwSetWindowShouldClose(Game.getWindow(), true);
                return;
            }
        } else {
            if (isEscaping) {
                isEscaping = false;
            }
        }
    }

    public boolean isEscaping() {
        return isEscaping;
    }

}
