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
