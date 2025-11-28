package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import undertale.Scene.SceneManager;
import undertale.Texture.FontManager;
import undertale.UI.ScreenFadeManager;

public class Renderer {
    private SceneManager sceneManager;
    private FontManager fontManager;
    private ScreenFadeManager screenFadeManager;
    private EscapeInputObserver escapeObserver;
    private Window window;

    private final int ESCAPING_X = 50;
    private final int ESCAPING_Y = 50;

    private int width;
    private int height;

    // 使用依赖注入重构构造函数，传入所需依赖
    Renderer(EscapeInputObserver escapeObserver, SceneManager sceneManager, FontManager fontManager, ScreenFadeManager screenFadeManager, Window window, int width, int height) {
        this.escapeObserver = escapeObserver;
        this.sceneManager = sceneManager;
        this.fontManager = fontManager;
        this.screenFadeManager = screenFadeManager;
        this.window = window;
        this.width = width;
        this.height = height;
        init();
    }

    private void init() {
        // 开启混合(透明度)
        glEnable(GL_BLEND);
        // 计算方式: 源颜色的alpha值决定源颜色的贡献度, (1 - 源颜色的alpha)决定目标颜色的贡献度
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // 设置视口为窗口大小, 左下角为(0,0)
        glViewport(0, 0, width, height);
    }

    public void render() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
        // render
        renderEscaping();
        sceneManager.getCurrentScene().render();
        screenFadeManager.render(); // 屏幕淡入淡出覆盖层
        // render ends
        glfwSwapBuffers(window.getWindow());
    }

    private void renderEscaping() {
        if (escapeObserver.isEscaping()) {
            // 按下1/3的结束时间内，透明度从0渐变到1
            float alpha = Math.min(1.0f, escapeObserver.getEscapeAlpha());
            fontManager.drawText("ESCAPING...", ESCAPING_X, ESCAPING_Y, 1.0f, 1.0f, 1.0f, alpha);
        }
    }
}
