package undertale;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {
	private static long window;

    private static int WINDOW_WIDTH = 1280;
    private static int WINDOW_HEIGHT = 720; 

    private static Renderer renderer;
    private static Logic logic;
    private static Timer timer;

	private static Player player;

    public static void run() {
		init();
		loop();
        destroy();
	}

    private static void destroy() {
		glDeleteTextures(player.getHeartTextureId());

		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
    }

	private static void init() {
		GLFWErrorCallback.createPrint(System.err).set();

		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Hello World!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

            // 获取窗口尺寸
			glfwGetWindowSize(window, pWidth, pHeight);
            // 获取显示器属性(此处用到分辨率)
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // 居中
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		}
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();

        timer = new Timer();
		logic = new Logic();
        renderer = new Renderer();
		player = new Player("Frisk");
	}

	private static void loop() {
		while ( !glfwWindowShouldClose(window) ) {
            timer.setTimerStart();
			render();
			update();
			timer.delayIfNeeded();
		}
	}

    public static boolean isKeyPressed(int key) {
        return glfwGetKey(Game.getWindow(), key) == GLFW_PRESS;
    }

    private static void update() {
		logic.update();
	}

    private static void render() {
        renderer.render();
    }
    
    public static long getWindow() {
        return window;
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
}
