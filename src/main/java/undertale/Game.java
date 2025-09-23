package undertale;

import static org.lwjgl.glfw.GLFW.*;


public class Game {
    private static Window gameWindow;

    private static int WINDOW_WIDTH = 1280;
    private static int WINDOW_HEIGHT = 720; 

    private static Renderer renderer;
    private static Logic logic;
    private static Timer timer;
	private static Player player;
    private static ObjectManager objectManager;

    public static void run() {
		init();
		loop();
        destroy();
	}

    private static void destroy() {
        player.destroyTexture();
		gameWindow.destroyWindow();
    }

	private static void init() {
		gameWindow = new Window(WINDOW_WIDTH, WINDOW_HEIGHT, "Undertale");
        timer = new Timer();
		logic = new Logic();
        renderer = new Renderer();
		player = new Player("Frisk");
        objectManager = new ObjectManager(player);
	}

	private static void loop() {
		while ( !glfwWindowShouldClose(gameWindow.getWindow()) ) {
            timer.setTimerStart();

            // test
            Texture testTexture = new Texture("img_ball_bullet.png");
            int randomX = (int)(Math.random() * (Game.WINDOW_WIDTH - 20));
            objectManager.createBullet(randomX, 0, 
            0, 90, 200, 1, testTexture);

			update(timer.getDeltaTime());
			render();
			timer.delayIfNeeded();
		}
	}

    public static boolean isKeyPressed(int key) {
        return glfwGetKey(Game.getWindow().getWindow(), key) == GLFW_PRESS;
    }

    private static void update(float deltaTime) {
		logic.update(deltaTime);
	}

    private static void render() {
        renderer.render();
    }
    
    public static Window getWindow() {
        return gameWindow;
    }

    public static int getWindowWidth() {
        return WINDOW_WIDTH;
    }

    public static int getWindowHeight() {
        return WINDOW_HEIGHT;
    }

    public static Renderer getRenderer() {
        return renderer;
    }

    public static Logic getLogic() {
        return logic;
    }

	public static Player getPlayer() {
		return player;
	}

    public static ObjectManager getObjectManager() {
        return objectManager;
    }
}
