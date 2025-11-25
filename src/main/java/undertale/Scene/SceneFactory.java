package undertale.Scene;

import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.Scene.Scene.SceneEnum;

public class SceneFactory {
    private ObjectManager objectManager;
    private InputManager inputManager;

    public SceneFactory(ObjectManager objectManager, InputManager inputManager) {
        this.objectManager = objectManager;
        this.inputManager = inputManager;
    }

    public Scene creatScene(SceneEnum type) {
        return switch (type) {
            case BATTLE_MENU -> new BattleMenuScene(objectManager, inputManager);
            case BATTLE_FIGHT -> new BattleFightScene(objectManager, inputManager);
            case GAME_OVER -> new GameOverScene(objectManager, inputManager);
            case START_MENU -> new BeginMenuScene(objectManager, inputManager);
        };
    }
}
