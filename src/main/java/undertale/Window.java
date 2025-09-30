package undertale;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class Window {
	private long window;

    Window(int width, int height, String title){
		init(width, height, title);
    }

    public void init(int width, int height, String title){
		GLFWErrorCallback.createPrint(System.err).set();
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
        		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        createWindow(width, height, title);
    }

    private void createWindow(int width, int height, String title){
        window = glfwCreateWindow(width, height, title, NULL, NULL);
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
    }

    public void destroyWindow(){
        glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
        glfwTerminate();
		glfwSetErrorCallback(null).free();
    }

    public long getWindow() {
        return window;
    }
}
