package undertale.Scene;

import java.util.ArrayList;

import undertale.Enemy.EnemyManager;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.Scene.Rounds.*;

public class BattleFightScene extends Scene {
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private int round;
    private ArrayList<Round> rounds;
    private long roundTime;

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
        round = 0;
        rounds = new ArrayList<>();
        rounds.add(new RoundSwarm(10000, 1500));
        roundTime = 0;
    }

    @Override
    public void onEnter() {
        roundTime = 0;
        round = Math.min(round + 1, rounds.size());
        uiManager.setSelected(-1);
        objectManager.initPlayerPosition();
        objectManager.startPlayerLightExpansion();
        objectManager.allowPlayerMovement(true);
    }

    @Override
    public void onExit() {
        objectManager.resetPlayerLight();
        objectManager.allowPlayerMovement(false);
        uiManager.setSelected(0);
        objectManager.clearBullets();
    }

    @Override
    public void update(float deltaTime) {
        if(!objectManager.isPlayerAlive()) {
            SceneManager.getInstance().switchScene(SceneEnum.GAME_OVER, true);
            return;
        }
        roundTime += deltaTime * 1000;
        Round currentRound = rounds.get(round - 1);
        if(roundTime < currentRound.getFrameMoveTime()) {
            currentRound.moveBattleFrame(deltaTime);
        } else {
            currentRound.updateRound(deltaTime);
        }
        if(roundTime >= currentRound.getRoundDuration()) {
            SceneManager.getInstance().shouldSwitch = true;
        }
        objectManager.updateFightScene(deltaTime);
        uiManager.makePlayerInFrame();
        SceneManager.getInstance().switchScene(SceneEnum.BATTLE_MENU);
    }

    @Override
    public void render() {
        enemyManager.render();
        uiManager.renderBattleUI();
        objectManager.renderFightScene();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_FIGHT;
    }
}