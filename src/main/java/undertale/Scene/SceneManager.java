package undertale.Scene;

import java.util.HashMap;

import undertale.Scene.Scene.SceneEnum;

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
        if(instance == null) {
            synchronized(SceneManager.class) {
                if(instance == null) {
                    instance = new SceneManager();
                }
            }
        }
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

    // 返回指定类型的场景实例（可能为null）
    public Scene getScene(SceneEnum type) {
        return scenes.get(type);
    }

    public void reset() {
        for (Scene scene : scenes.values()) {
            scene.init();
        }
    }

    public Scene getSceneByType(SceneEnum type) {
        return scenes.get(type);
    }
}
