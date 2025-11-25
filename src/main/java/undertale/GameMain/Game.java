package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;

import undertale.GameObject.ObjectManager;
import undertale.GameObject.Player;
import undertale.Scene.Scene;
import undertale.Scene.Scene.SceneEnum;
import undertale.Scene.SceneFactory;
import undertale.Scene.SceneManager;
import undertale.Shaders.ShaderManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Texture.TextureManager;
import undertale.UI.UIManager;
import undertale.Utils.ConfigManager;
import undertale.Utils.Timer;
import undertale.UI.ScreenFadeManager;

public class Game {
    private static boolean allowDebug = true;

    private static Window gameWindow;

    public static ConfigManager configManager;
    private static Renderer renderer;
	private static Player player;
    private static SceneManager sceneManager;
    private static ObjectManager objectManager;
    private static InputManager inputManager;
    private static UIManager uiManager;
    private static TextureManager textureManager;
    private static FontManager fontManager;
    private static ScreenFadeManager screenFadeManager;
    private static ShaderManager shaderManager;

    public static void run() {
		init();
		loop();
        destroy();
	}

    private static void destroy() {
        textureManager.destroyAll();
        fontManager.destroy();
        objectManager.destroy();
        shaderManager.dispose();
		gameWindow.destroyWindow();
    }

	private static void init() {
        configManager = ConfigManager.getInstance();
        gameWindow = new Window(configManager.WINDOW_WIDTH, configManager.WINDOW_HEIGHT, "Undertale");
        shaderManager = ShaderManager.getInstance();
        textureManager = TextureManager.getInstance();
        sceneManager = SceneManager.getInstance();
		player = new Player("Frisk");
        objectManager = new ObjectManager(player);
        EscapeInputObserver escapeObserver = new EscapeInputObserver(gameWindow);
        inputManager = new InputManager(gameWindow);
        inputManager.addObserver(escapeObserver);
        inputManager.addObserver(new DebugInputObserver(allowDebug));
        fontManager = FontManager.getInstance();
        screenFadeManager = ScreenFadeManager.getInstance();

        SceneFactory sceneFactory = new SceneFactory(objectManager, inputManager);

        // 初始化场景管理器并注册场景
        sceneManager.registerScene(SceneEnum.START_MENU,
        sceneFactory.creatScene(SceneEnum.START_MENU));
        sceneManager.registerScene(SceneEnum.BATTLE_MENU,
        sceneFactory.creatScene(SceneEnum.BATTLE_MENU));
        sceneManager.registerScene(SceneEnum.BATTLE_FIGHT,
        sceneFactory.creatScene(SceneEnum.BATTLE_FIGHT));
        sceneManager.registerScene(SceneEnum.GAME_OVER,
        sceneFactory.creatScene(SceneEnum.GAME_OVER));
        
        // 初始场景
        sceneManager.switchScene(SceneEnum.START_MENU, true);
        
        // 初始化UI管理器
        uiManager = UIManager.getInstance();
        
        // 初始化渲染器
        renderer = new Renderer(escapeObserver);
	}

	private static void loop() {
        Timer timer = new Timer();
		while ( !glfwWindowShouldClose(gameWindow.getWindow()) ) {
            timer.setTimerStart();
			update(timer.getDeltaTime());
			render();
			timer.delayIfNeeded();
		}
	}

    private static void update(float deltaTime) {
        // 场景更新
        Scene currentScene = sceneManager.getCurrentScene();
        if (currentScene != null) {
            currentScene.update(deltaTime);
        }
        // 输入处理
		inputManager.processInput();
        // ui更新
        uiManager.update(deltaTime);
        // 屏幕淡入淡出更新
        screenFadeManager.update(deltaTime);
    }

    private static void render() {
        renderer.render();
    }
    
    public static Window getWindow() {
        return gameWindow;
    }

    public static int getWindowWidth() {
        return configManager.WINDOW_WIDTH;
    }

    public static int getWindowHeight() {
        return configManager.WINDOW_HEIGHT;
    }

    public static Renderer getRenderer() {
        return renderer;
    }

	public static Player getPlayer() {
		return player;
	}

    public static ObjectManager getObjectManager() {
        return objectManager;
    }

    public static InputManager getInputManager() {
        return inputManager;
    }

    public static UIManager getUIManager() {
        return uiManager;
    }

    public static Texture getTexture(String name) {
        return textureManager.getTexture(name);
    }

    public static float getFrameHeight() {
        return uiManager.getFrameHeight();
    }

    public static float getFrameWidth() {
        return uiManager.getFrameWidth();
    }

    public static float getFrameLeft() {
        return uiManager.getFrameLeft();
    }

    public static float getFrameBottom() {
        return uiManager.getFrameBottom();
    }

    public static void resetGame(UIManager.MenuState menuState) {
        objectManager.resetGame();
        uiManager.resetVars(menuState);
        sceneManager.reset();
    }
}
