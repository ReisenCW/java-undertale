package undertale.Scene;

import java.util.ArrayList;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.InputManager;
import undertale.GameMain.UIManager;
import undertale.GameObject.ObjectManager;

public class BattleFightScene extends Scene {
    private UIManager uiManager = UIManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private int round;
    private ArrayList<Round> rounds;

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
        round = 1;
        rounds = new ArrayList<>();
        rounds.add(new Round(10000));
    }

    @Override
    public void onEnter() {
        uiManager.setSelected(-1);
        objectManager.initPlayerPosition();
    }

    @Override
    public void onExit() {
        uiManager.setSelected(0);
        objectManager.clearBullets();
    }

    @Override
    public void update(float deltaTime) {

        rounds.get(round - 1).updateRound(deltaTime);
        objectManager.updateFightScene(deltaTime);
        uiManager.updatePlayerInBound();
        SceneManager.getInstance().switchScene(SceneEnum.BATTLE_MENU);
    }

    @Override
    public void render() {
        enemyManager.render();
        uiManager.renderBattleUI(null);
        objectManager.renderFightScene();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_FIGHT;
    }
}