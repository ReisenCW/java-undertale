package undertale.Scene;

import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.Sound.SoundManager;
import undertale.Texture.TextureManager;
import undertale.UI.UIManager;

public abstract class Scene {
    public enum SceneEnum {
        BATTLE_MENU,
        BATTLE_FIGHT,
        GAME_OVER,
        START_MENU
    }
    protected SceneManager sceneManager;
    protected ObjectManager objectManager;
    protected InputManager inputManager;
    protected UIManager uiManager;
    protected TextureManager textureManager;
    protected SoundManager soundManager;

    // 构造函数注入依赖
    public Scene(ObjectManager objectManager, InputManager inputManager) {
        this.sceneManager = SceneManager.getInstance();
        this.uiManager = UIManager.getInstance();
        this.textureManager = TextureManager.getInstance();
        this.objectManager = objectManager;
        this.inputManager = inputManager;
        this.soundManager = SoundManager.getInstance();
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