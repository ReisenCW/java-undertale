package undertale;

import java.util.HashMap;

// 新增场景管理器
public class SceneManager {
    private static SceneManager instance;
    private HashMap<SceneEnum, Scene> scenes = new HashMap<>();
    private Scene currentScene;
    public boolean shouldSwitch = false;

    static {
        instance = new SceneManager();
    }

    private SceneManager() {}

    public static SceneManager getInstance() {
        return instance;
    }

    public void registerScene(SceneEnum type, Scene scene) {
        scenes.put(type, scene);
    }

    public void switchScene(SceneEnum type, boolean force) {
        if(!force && !shouldSwitch) return;
        if (currentScene != null) {
            currentScene.onExit(); // 退出当前场景
        }
        currentScene = scenes.get(type);
        if (currentScene != null) {
            currentScene.onEnter(); // 进入新场景
        }
        shouldSwitch = false;
    }

    public void switchScene(SceneEnum type) {
        switchScene(type, false);
    }

    public Scene getCurrentScene() {
        return currentScene;
    }
}
