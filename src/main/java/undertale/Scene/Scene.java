package undertale.Scene;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.Interfaces.InputObserver;
import undertale.Sound.SoundManager;
import undertale.Texture.TextureManager;
import undertale.UI.UIManager;

public abstract class Scene implements InputObserver {
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

    // 重构内容: Scene 基类和所有子类的构造函数增加了 UIManager 参数。BattleFightScene 和 BattleMenuScene 还增加了 EnemyManager 参数。
    // 作用: 场景不再依赖全局单例来获取 UI 和 敌人管理器，而是通过构造函数注入。
    // 构造函数注入依赖
    public Scene(ObjectManager objectManager, InputManager inputManager, UIManager uiManager) {
        this.sceneManager = SceneManager.getInstance();
        this.uiManager = uiManager;
        this.textureManager = TextureManager.getInstance();
        this.objectManager = objectManager;
        this.inputManager = inputManager;
        this.soundManager = SoundManager.getInstance();
    }

    // 场景进入时调用
    public abstract void onEnter();
    
    // 场景退出时调用
    public abstract void onExit();
    
    public abstract SceneEnum getCurrentScene();
    public abstract void init();
    public abstract void update(float deltaTime);
    public abstract void render();

    protected void registerAsObserver() {
        inputManager.addObserver(this);
    }
    protected void unregisterAsObserver() {
        inputManager.removeObserver(this);
    }
}