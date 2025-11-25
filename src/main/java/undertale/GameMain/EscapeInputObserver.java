package undertale.GameMain;
import static org.lwjgl.glfw.GLFW.*;
import undertale.Interfaces.InputObserver;
import undertale.Utils.Timer;

public class EscapeInputObserver implements InputObserver {
    private boolean isEscaping = false;
    private Timer escapeTimer = new Timer();
    private Window window;
    private final long ESCAPE_HOLD_TIME = 2000; // 按住2秒退出

    public EscapeInputObserver(Window window) {
        this.window = window;
    }

    @Override
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates) {
        if (currKeyStates[GLFW_KEY_ESCAPE]) {
            if (!isEscaping) {
                isEscaping = true;
                escapeTimer.setTimerStart();
            }
            // 按住ESCAPE键超过2秒则退出
            if(escapeTimer.isTimeElapsed(ESCAPE_HOLD_TIME)) {
                glfwSetWindowShouldClose(window.getWindow(), true);
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

    public float getEscapeAlpha() {
        if (!isEscaping) {
            return 0.0f;
        }
        return Math.min(1.0f, 3.0f * (float)escapeTimer.durationFromStart() / ESCAPE_HOLD_TIME);
    }
}
