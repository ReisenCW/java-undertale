package undertale.Scene;

import java.util.ArrayList;

import undertale.Enemy.Enemy;
import undertale.Enemy.EnemyManager;
import undertale.Enemy.Titan;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.Scene.Rounds.*;
import undertale.Sound.SoundManager;

public class BattleFightScene extends Scene {
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private int phase = 0; // 0-3
    private int phaseRound = 0; // 0-2
    private ArrayList<Round> rounds;
    private long roundTime;

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
        init();
    }

    @Override
    public void init() {
        phase = 0;
        // phaseRound = -1;
        phaseRound = 1; //test
        rounds = new ArrayList<>();
        // 4个阶段, 前三个阶段每个阶段3个round
        for(int p = 0; p < 3; p++) {
            rounds.add(new RoundSwarm(p + 1, 12000, 1500));
            rounds.add(new RoundSnake(p + 1, 17000, 1500));
            rounds.add(new RoundFinger(p + 1, 28000, 1500));
        }
        roundTime = 0;
    }

    @Override
    public void onEnter() {
        roundTime = 0;
        phaseRound++;
        if (phase < 3 && phaseRound > 2) {
            phaseRound = 0;
            phase = (phase + 1) % 4;
        } else if (phase >= 3){
            phaseRound = 0;
        }
        int roundIndex = phase * 3 + phaseRound;
        rounds.get(roundIndex).onEnter();
        uiManager.setSelected(-1);
        objectManager.initPlayerPosition();
        objectManager.startPlayerLightExpansion();
        objectManager.allowPlayerMovement(true);
    }

    /**
     * 返回当前回合编号（1-based）。如果尚未进入回合则返回 0。
     */
    public int getRoundNumber() {
        return phase * 3 + phaseRound;
    }

    public void afterUnleash() {
        // 进入下一个阶段
        phase = phase + 1;
        if(phase > 3) phase = 3;
        phaseRound = 0;
    }

    @Override
    public void onExit() {
        objectManager.resetPlayerLight();
        objectManager.allowPlayerMovement(false);
        uiManager.setSelected(0);
        objectManager.clearBullets();
        objectManager.clearCollectables();
    }

    @Override
    public void update(float deltaTime) {
        if(!objectManager.isPlayerAlive()) {
            SceneManager.getInstance().switchScene(SceneEnum.GAME_OVER, true);
            return;
        }
        roundTime += deltaTime * 1000;
        int roundIndex = phase * 3 + phaseRound;
        Round currentRound = rounds.get(roundIndex);

        currentRound.moveBattleFrame(deltaTime);
        
        if(roundTime > currentRound.getFrameMoveTime()) {
            currentRound.updateRound(deltaTime);
        }
        if(roundTime >= currentRound.getRoundDuration()) {
            SceneManager.getInstance().shouldSwitch = true;
        }
        objectManager.updateFightScene(deltaTime);
        uiManager.makePlayerInFrame();
        // 持续播放 spawn_attack SE
        if (!SoundManager.getInstance().isSePlaying("spawn_attack")) {
            SoundManager.getInstance().playSE("spawn_attack");
        }
        // 回合结束，处理Titan的weaken状态
        Enemy currentEnemy = enemyManager.getCurrentEnemy();
        if (SceneManager.getInstance().shouldSwitch && currentEnemy instanceof Titan) {
            ((Titan) currentEnemy).endTurn();
        }
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