package undertale.GameMain;

import undertale.Interfaces.InputObserver;
import static org.lwjgl.glfw.GLFW.*;

public class DebugInputObserver implements InputObserver {
    private boolean DEBUG = false;
    private boolean allowDebug;
    public DebugInputObserver(boolean allowDebug) {
        this.allowDebug = allowDebug;
    }
    @Override
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates) {
        if(!allowDebug) return;
        // 按下f12切换debug模式
        if(DEBUG && currKeyStates[GLFW_KEY_F12] && !preKeyStates[GLFW_KEY_F12]) {
            DEBUG = false;
            System.out.println("Debug mode OFF");
        } else if(!DEBUG && currKeyStates[GLFW_KEY_F12] && !preKeyStates[GLFW_KEY_F12]) {
            DEBUG = true;
            System.out.println("Debug mode ON");
        }
    }
}
