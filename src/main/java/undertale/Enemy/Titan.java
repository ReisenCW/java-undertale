package undertale.Enemy;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Scene.SceneManager;
import undertale.Scene.BattleFightScene;
import undertale.Scene.Scene;
import undertale.Sound.SoundManager;
import undertale.Utils.ConfigManager;

public class Titan extends Enemy {
    private boolean weakened;
    private int weakenTurns;
    private SoundManager soundManager;
    private int roundsPerWeaken;
    private float initialDefenseRate = 0.8f;

    public Titan() {
        super("Titan", 10000, 10000, 114514, 19198);
        this.weakened = false;
        init();
    }

    private void init() {
        ConfigManager configManager = Game.getConfigManager();
        AnimationManager animationManager = AnimationManager.getInstance();
        soundManager = SoundManager.getInstance();
        Player player = Game.getPlayer();
        roundsPerWeaken = 2;
        defenseRate = initialDefenseRate;
        // 添加Act行为
        addAct(
            "check",
            "* Dark element boss.\n* Emit light, gather courage and use unleash to weaken it.",
            "check enemy info",
            () -> true,
            () -> {}
        );
        addAct(
            "light",
            "* Your soul emits a greater light.",
            "costs 4 tp, player emits greater light",
            () -> (player.getTensionPoints() >= 4),
            () -> {
                player.updateTensionPoints(-4);
                player.setTargetLightRadius(Player.LightLevel.ENHANCED);
            }
        );
        addAct(
            "unleash",
            "* Your soul emits a gentle light.\n* The titan's defense dropped to zero.",
            "costs 80 tp, titan gets weakened for 1 turn",
            () -> (player.getTensionPoints() >= 80 && !weakened),
            () -> {
                player.updateTensionPoints(-80);
                defenseWeaken(roundsPerWeaken); // 2回合
                // 执行特殊攻击，进入下一个阶段
                Scene fightScene = SceneManager.getInstance().getSceneByType(Scene.SceneEnum.BATTLE_FIGHT);
                if (fightScene instanceof BattleFightScene) {
                    ((BattleFightScene) fightScene).afterUnleash();
                }
            }
        );
        addAct(
            "single heal",
            "* You healed a small amount of HP.",
            "costs 4 tp, heals a small amount of hp",
            () -> (player.getTensionPoints() >= 4),
            () -> {
                int healAmount = 8 + (int)(Math.random() * 16);
                player.heal(healAmount);
                player.updateTensionPoints(-4);
            }
        );

        // 初始化动画
        initAnimations(configManager, animationManager);
    }

    private void initAnimations(ConfigManager configManager, AnimationManager animationManager) {
        float windowWidth = Game.getWindowWidth();
        float windowCenterX = windowWidth / 2;
        float bodyBottom = configManager.MENU_FRAME_BOTTOM - configManager.MENU_FRAME_HEIGHT;
        float starBottom = bodyBottom - 45;

        Animation bodyAnimation = animationManager.getAnimation("titan_body");
        Animation starAnimation = animationManager.getAnimation("titan_star");

        // 给所有backwing和frontwing添加动画
        Animation[] backwingAnimations = new Animation[4];
        for (int i = 0; i < backwingAnimations.length; i++) {
            backwingAnimations[i] = animationManager.getAnimation("titan_backwing_" + i);
        }
        Animation[] frontwingAnimations = new Animation[3];
        for (int i = 0; i < frontwingAnimations.length; i++) {
            frontwingAnimations[i] = animationManager.getAnimation("titan_frontwing_" + i);
        }

        // name, animation, left, bottom, priority, scaler
        Object[][] titanAnimations = {
            {"frontwing_0", frontwingAnimations[0], windowCenterX - frontwingAnimations[0].getFrameWidth() / 2 - 40, bodyBottom, 4, 1.5f},
            {"frontwing_1", frontwingAnimations[1], windowCenterX - frontwingAnimations[1].getFrameWidth() / 2 - 120, bodyBottom + 20, 4, 1.7f},
            {"frontwing_2", frontwingAnimations[2], windowCenterX - frontwingAnimations[2].getFrameWidth() / 2 - 80, bodyBottom - 20, 4, 1.5f},
            {"backwing_0", backwingAnimations[0], windowCenterX - backwingAnimations[0].getFrameWidth() / 2 - 100, bodyBottom, 1, 1.6f},
            {"backwing_1", backwingAnimations[1], windowCenterX - backwingAnimations[1].getFrameWidth() / 2 - 120, bodyBottom + 20, 1, 1.7f},
            {"backwing_2", backwingAnimations[2], windowCenterX - backwingAnimations[2].getFrameWidth() / 2 - 80, bodyBottom - 20, 0, 1.5f},
            {"backwing_3", backwingAnimations[3], windowCenterX - backwingAnimations[3].getFrameWidth() / 2 - 40, bodyBottom - 20, 0, 1.6f},
            {"body", bodyAnimation, windowCenterX - bodyAnimation.getFrameWidth() / 2, bodyBottom, 2, 1.5f},
            {"star", starAnimation, windowCenterX - starAnimation.getFrameWidth() / 2 - 10 , starBottom, 3, 1.5f}
        };

        for (Object[] anim : titanAnimations) {
            addAnimation(
                (String)anim[0],
                (Animation)anim[1],
                (Float)anim[2],
                (Float)anim[3],
                (Integer)anim[4],
                (Float)anim[5]
            );
        }
    }

    public void defenseWeaken(int turns) {
        // 进入防御下降状态
        setDefenseRate(0.0f);
        weakened = true;
        weakenTurns = turns;
        // 播放音效
        soundManager.playSE("titan_weakened");
        // star逐渐变暗消失
        setAnimationAlpha("star", 0.0f, 0.5f); // 0.5秒内淡出
    }

    public boolean isWeakened() {
        return weakened;
    }

    public void endTurn() {
        // 如果 roundsPerWeaken 设置为 -1, 则永久保持防御下降状态
        if(weakenTurns == -1) return;
        if (weakened) {
            weakenTurns--;
            if (weakenTurns <= 0) {
                // 恢复防御
                setDefenseRate(initialDefenseRate);
                weakened = false;
                // star淡入显示
                setAnimationAlpha("star", 1.0f, 0.5f); // 0.5秒内淡入
            }
        }
    }

    public void setRoundsPerWeaken(int rounds) {
        this.roundsPerWeaken = rounds;
        if(rounds != -1 && rounds < 2)
            this.roundsPerWeaken = 2;
    }

    @Override
    public void reset() {
        super.reset();
        this.weakened = false;
        this.weakenTurns = 0;
        this.roundsPerWeaken = 2;
        this.defenseRate = initialDefenseRate;
        // star显示
        setAnimationAlpha("star", 1.0f, 0.0f);
    }
}