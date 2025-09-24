package undertale;

public class BattleFightScene extends Scene {
    private boolean isPlayerTurn = false;

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
    }

    @Override
    public void onEnter() {
    }

    @Override
    public void onExit() {
    }

    @Override
    public void update(float deltaTime) {
        objectManager.battleSceneUpdate(deltaTime);
    }

    @Override
    public void render() {
        objectManager.renderBattleScene();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_FIGHT;
    }
}