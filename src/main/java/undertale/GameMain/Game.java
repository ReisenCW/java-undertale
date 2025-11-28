package undertale.GameMain;

import static org.lwjgl.glfw.GLFW.*;

import undertale.Enemy.EnemyManager;
import undertale.Sound.SoundManager;
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
    private static Game instance;

    private boolean allowDebug = true;

    private Window gameWindow;

    private ConfigManager configManager;
    private Renderer renderer;
	private Player player;
    private SceneManager sceneManager;
    private ObjectManager objectManager;
    private InputManager inputManager;
    private UIManager uiManager;
    private EnemyManager enemyManager;
    private TextureManager textureManager;
    private FontManager fontManager;
    private ScreenFadeManager screenFadeManager;
    private ShaderManager shaderManager;

    public Game() {
        // Constructor
    }

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public void run() {
		init();
		loop();
        destroy();
	}

    private void destroy() {
        textureManager.destroyAll();
        fontManager.destroy();
        objectManager.destroy();
        shaderManager.dispose();
		gameWindow.destroyWindow();
    }

	private void init() {
        configManager = ConfigManager.getInstance();
        gameWindow = new Window(configManager.WINDOW_WIDTH, configManager.WINDOW_HEIGHT, "Undertale");
        shaderManager = ShaderManager.getInstance();
        textureManager = TextureManager.getInstance();
        sceneManager = SceneManager.getInstance();
        
        player = new Player("Frisk");
        // Initialize EnemyManager
        enemyManager = new EnemyManager(player);

        objectManager = new ObjectManager(player, enemyManager);
        EscapeInputObserver escapeObserver = new EscapeInputObserver(gameWindow);
        inputManager = new InputManager(gameWindow);
        inputManager.addObserver(escapeObserver);
        inputManager.addObserver(new DebugInputObserver(allowDebug));
        fontManager = FontManager.getInstance();
        
        // Initialize ScreenFadeManager with dimensions
        ScreenFadeManager.init(configManager.WINDOW_WIDTH, configManager.WINDOW_HEIGHT);
        screenFadeManager = ScreenFadeManager.getInstance();

        // Initialize UIManager
        uiManager = new UIManager(player, enemyManager, SoundManager.getInstance(), fontManager);

        SceneFactory sceneFactory = new SceneFactory(objectManager, inputManager, uiManager, enemyManager);

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
        
        // 初始化渲染器 - Inject dependencies
        renderer = new Renderer(escapeObserver, sceneManager, fontManager, screenFadeManager, gameWindow, configManager.WINDOW_WIDTH, configManager.WINDOW_HEIGHT);
	}

	private void loop() {
        Timer timer = new Timer();
		while ( !glfwWindowShouldClose(gameWindow.getWindow()) ) {
            timer.setTimerStart();
			update(timer.getDeltaTime());
			render();
			timer.delayIfNeeded();
		}
	}

    private void update(float deltaTime) {
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

    private void render() {
        renderer.render();
    }
    
    public static Window getWindow() {
        return getInstance().gameWindow;
    }

    public static int getWindowWidth() {
        return getInstance().configManager.WINDOW_WIDTH;
    }

    public static int getWindowHeight() {
        return getInstance().configManager.WINDOW_HEIGHT;
    }

    public static Renderer getRenderer() {
        return getInstance().renderer;
    }

	public static Player getPlayer() {
		return getInstance().player;
	}

    public static ObjectManager getObjectManager() {
        return getInstance().objectManager;
    }

    public static InputManager getInputManager() {
        return getInstance().inputManager;
    }

    public static UIManager getUIManager() {
        return getInstance().uiManager;
    }

    public static Texture getTexture(String name) {
        return getInstance().textureManager.getTexture(name);
    }

    public static float getFrameHeight() {
        return getInstance().uiManager.getFrameHeight();
    }

    public static float getFrameWidth() {
        return getInstance().uiManager.getFrameWidth();
    }

    public static float getFrameLeft() {
        return getInstance().uiManager.getFrameLeft();
    }

    public static float getFrameBottom() {
        return getInstance().uiManager.getFrameBottom();
    }

    public static void resetGame(UIManager.MenuState menuState) {
        getInstance().objectManager.resetGame();
        getInstance().uiManager.resetVars(menuState);
        getInstance().sceneManager.reset();
    }
}
