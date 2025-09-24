package undertale;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private InputManager inputManager;
    // private final int ESCAPING_X = 100;
    // private final int ESCAPING_Y = 50;

    Renderer(InputManager inputManager) {
        this.inputManager = inputManager;
        init();
    }

    private void init() {
        // 启用纹理映射
        glEnable(GL_TEXTURE_2D);
        // 启用混合（处理透明）
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Game.getWindowWidth(), Game.getWindowHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
    }

    public void render() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        // render
        renderEscaping();
        Game.getObjectManager().render();
        // render ends
        glfwSwapBuffers(Game.getWindow().getWindow()); // swap the color buffers
    }

    private void renderEscaping() {
        if (inputManager.isEscaping()) {
            // 之后可利用在屏幕左上角显示Escaping...
            System.out.println("Escaping...");
        }
    }
}
