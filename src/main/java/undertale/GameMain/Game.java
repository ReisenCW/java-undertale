package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;

import undertale.GameObject.ObjectManager;
import undertale.GameObject.Player;
import undertale.Scene.BattleFightScene;
import undertale.Scene.BattleMenuScene;
import undertale.Scene.Scene;
import undertale.Scene.SceneManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Texture.TextureManager;
import undertale.UI.UIManager;
import undertale.Utils.ConfigManager;
import undertale.Utils.Timer;

public class Game {
    public static boolean DEBUG = true;

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

    public static void run() {
		init();
		loop();
        destroy();
	}

    private static void destroy() {
        textureManager.destroyAll();
        fontManager.destroy();
		gameWindow.destroyWindow();
    }

	private static void init() {
        configManager = new ConfigManager();
        gameWindow = new Window(configManager.WINDOW_WIDTH, configManager.WINDOW_HEIGHT, "Undertale");
        textureManager = TextureManager.getInstance();
        sceneManager = SceneManager.getInstance();
		player = new Player("Frisk");
        objectManager = new ObjectManager(player);
        inputManager = new InputManager(gameWindow, player);
        fontManager = FontManager.getInstance();

        // 初始化场景管理器并注册场景
        sceneManager.registerScene(Scene.SceneEnum.BATTLE_MENU, 
        new BattleMenuScene(objectManager, inputManager));
        sceneManager.registerScene(Scene.SceneEnum.BATTLE_FIGHT,
        new BattleFightScene(objectManager, inputManager));
        
        // 初始场景
        // sceneManager.switchScene(Scene.SceneEnum.BATTLE_FIGHT, true);
        sceneManager.switchScene(Scene.SceneEnum.BATTLE_MENU, true);
        
        // 初始化UI管理器
        uiManager = UIManager.getInstance();
        
        // 初始化渲染器
        renderer = new Renderer(inputManager);
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
        Scene currentScene = SceneManager.getInstance().getCurrentScene();
        if (currentScene != null) {
            currentScene.update(deltaTime);
        }
        // 输入处理
		inputManager.processInput();
        // ui更新
        uiManager.update(deltaTime);
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

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static boolean isKeyPressed(int key) {
        return inputManager.isKeyPressed(key);
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
}
