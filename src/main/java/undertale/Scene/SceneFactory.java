package undertale.Scene;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.Scene.Scene.SceneEnum;
import undertale.UI.UIManager;

public class SceneFactory {
    private ObjectManager objectManager;
    private InputManager inputManager;
    private UIManager uiManager;
    private EnemyManager enemyManager;

    // 重构内容: 更新了工厂方法，持有 UIManager 和 EnemyManager 实例，并在创建场景时将它们注入。
    // 作用: 负责将依赖传递给具体的场景实例。
    public SceneFactory(ObjectManager objectManager, InputManager inputManager, UIManager uiManager, EnemyManager enemyManager) {
        this.objectManager = objectManager;
        this.inputManager = inputManager;
        this.uiManager = uiManager;
        this.enemyManager = enemyManager;
    }

    public Scene creatScene(SceneEnum type) {
        return switch (type) {
            case BATTLE_MENU -> new BattleMenuScene(objectManager, inputManager, uiManager, enemyManager);
            case BATTLE_FIGHT -> new BattleFightScene(objectManager, inputManager, uiManager, enemyManager);
            case GAME_OVER -> new GameOverScene(objectManager, inputManager, uiManager);
            case START_MENU -> new BeginMenuScene(objectManager, inputManager, uiManager);
        };
    }
}
