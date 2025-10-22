package undertale.Scene;

import undertale.GameMain.InputManager;
import undertale.GameMain.UIManager;
import undertale.GameObject.ObjectManager;
import undertale.Texture.TextureManager;

public abstract class Scene {
    protected SceneManager sceneManager;
    protected ObjectManager objectManager;
    protected InputManager inputManager;
    protected UIManager uiManager;
    protected TextureManager textureManager;

    // 构造函数注入依赖
    public Scene(ObjectManager objectManager, InputManager inputManager) {
        this.sceneManager = SceneManager.getInstance();
        this.uiManager = UIManager.getInstance();
        this.textureManager = TextureManager.getInstance();
        this.objectManager = objectManager;
        this.inputManager = inputManager;
    }

    // 场景进入时调用
    public abstract void onEnter();
    
    // 场景退出时调用
    public abstract void onExit();
    
    // 原有方法
    public abstract SceneEnum getCurrentScene();
    public abstract void init();
    public abstract void update(float deltaTime);
    public abstract void render();
}