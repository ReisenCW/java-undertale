package undertale.Scene;

import java.util.ArrayList;

import undertale.Enemy.Enemy;
import undertale.Enemy.EnemyManager;
import undertale.Enemy.Titan;
import undertale.GameMain.InputManager;
import undertale.GameObject.ObjectManager;
import undertale.Scene.Rounds.*;
import undertale.Sound.SoundManager;
import undertale.UI.UIManager;

public class BattleFightScene extends Scene {
    private EnemyManager enemyManager;
    private int phase = 0; // 0-3
    private int phaseRound = 0; // 0-2
    private ArrayList<Round> rounds;
    private long roundTime;
    private boolean isSpecial = false;

    public BattleFightScene(ObjectManager objectManager, InputManager inputManager, UIManager uiManager, EnemyManager enemyManager) {
        super(objectManager, inputManager, uiManager);
        this.enemyManager = enemyManager;
        init();
    }

    @Override
    public void init() {
        phase = 0;
        phaseRound = -1;
        // phase = 1; // test
        // phaseRound = 1; //test
        rounds = new ArrayList<>();
        // 4个阶段, 前三个阶段每个阶段3个round
        // 0-2, 4-6, 8-10
        // 3,7,11为special round, 只有使用了unleash之后才会进入该round
        for(int p = 0; p < 3; p++) {
            rounds.add(new RoundSwarm(p + 1, 16000, 1500, uiManager));
            rounds.add(new RoundSnake(p + 1, 17000, 1500, uiManager));
            rounds.add(new RoundFinger(p + 1, 22000, 1500, uiManager));
            rounds.add(new RoundSpecial(p + 1, 17000, 1500, uiManager, enemyManager));
        }
        rounds.add(new RoundSpecial(3, 17000, 1500, uiManager, enemyManager)); // 循环最后一个攻击
        roundTime = 0;
    }

    @Override
    public void onEnter() {
        registerAsObserver();
        roundTime = 0;
        if(isSpecial) {
            System.out.println("special round");
            phaseRound = -1;
            isSpecial = false;
        } else {
            phaseRound++;
            System.out.println("nromal round " + phaseRound);
            if (phase >= 3 || phaseRound > 2) {
                System.out.println("reset phase round to 0");
                phaseRound = 0;
            }
        }
        int roundIndex = phase * 4 + phaseRound;
        rounds.get(roundIndex).onEnter();
        uiManager.setSelected(-1);
        objectManager.initPlayerPosition();
        objectManager.startPlayerLightExpansion();
        objectManager.allowPlayerMovement(true);
        System.out.println("current phase: " + phase + ", round: " + phaseRound + ", roundIndex: " + roundIndex);
    }

    /**
     * 返回当前回合编号（0-based）
     */
    public int getRoundNumber() {
        return phase * 4 + phaseRound;
    }

    public void afterUnleash() {
        // 进入下一个阶段
        phase = phase + 1;
        if(phase > 3) {
            phase = 3;
        } else {
            isSpecial = true;
        }
    }

    @Override
    public void onExit() {
        unregisterAsObserver();
        objectManager.resetPlayerLight();
        objectManager.allowPlayerMovement(false);
        uiManager.setSelected(0);
        objectManager.clearBullets();
        objectManager.clearCollectables();
        objectManager.clearRipples();
        objectManager.clearTitanSpawnParticles();
    }

    @Override
    public void update(float deltaTime) {
        if(!objectManager.isPlayerAlive()) {
            SceneManager.getInstance().switchScene(SceneEnum.GAME_OVER, true);
            return;
        }
        roundTime += deltaTime * 1000;
        int roundIndex = phase * 4 + phaseRound;
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
        int roundIndex = phase * 4 + phaseRound;
        Round currentRound = rounds.get(roundIndex);
        currentRound.render();
    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_FIGHT;
    }

    public int getPhase() {
        return phase;
    }

    @Override
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates) {}

    @Override
    protected void registerAsObserver() {
        inputManager.addObserver(objectManager.getPlayer());
    }

    @Override
    protected void unregisterAsObserver() {
        inputManager.removeObserver(objectManager.getPlayer());
    }
}