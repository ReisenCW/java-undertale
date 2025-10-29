package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import undertale.Scene.SceneManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.UI.ScreenFadeManager;

public class Renderer {
    private InputManager inputManager;
    private SceneManager sceneManager;
    private FontManager fontManager;
    private ScreenFadeManager screenFadeManager;

    private final int ESCAPING_X = 50;
    private final int ESCAPING_Y = 50;

    Renderer(InputManager inputManager) {
        this.inputManager = inputManager;
        this.sceneManager = SceneManager.getInstance();
        this.fontManager = FontManager.getInstance();
        this.screenFadeManager = ScreenFadeManager.getInstance();
        init();
    }

    private void init() {
        // Core-profile: enable blending, set viewport and inform Texture system of screen size.
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Ensure the OpenGL viewport matches our window size
        glViewport(0, 0, Game.getWindowWidth(), Game.getWindowHeight());

        // Let Texture (and its quad renderer) convert pixel coords -> NDC using screen size
        Texture.setScreenSize(Game.getWindowWidth(), Game.getWindowHeight());
    }

    public void render() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        // render
        renderEscaping();
        // enemyManager.render();
        sceneManager.getCurrentScene().render();
        // 屏幕淡入淡出覆盖层（在场景渲染后绘制）
        screenFadeManager.render();
        // render ends
        glfwSwapBuffers(Game.getWindow().getWindow()); // swap the color buffers
    }

    private void renderEscaping() {
        if (inputManager.isEscaping()) {
            // 按下1/3的结束时间内，透明度从0渐变到1
            float alpha = Math.min(1.0f, 3 * inputManager.getEscapeTimer().durationFromStart() / inputManager.ESCAPE_HOLD_TIME);
            fontManager.drawText("ESCAPING...", ESCAPING_X, ESCAPING_Y, 1.0f, 1.0f, 1.0f, alpha);
        }
    }
}
