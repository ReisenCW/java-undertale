package undertale.Scene.Rounds;

import undertale.GameMain.Game;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;

public abstract class Round {
    protected long roundDuration; // 回合持续时间
    protected long frameMoveTime;
    protected ObjectManager objectManager;
    protected UIManager uiManager;

    // 重构内容: 构造函数增加了 UIManager 参数（RoundSpecial 还增加了 EnemyManager）。
    // 作用: 战斗回合逻辑（Round）也不再依赖全局单例。
    public Round(long duration, long frameMoveTime, UIManager uiManager) {
        this.roundDuration = duration;
        this.frameMoveTime = frameMoveTime;
        objectManager = Game.getObjectManager();
        this.uiManager = uiManager;
    }
    
    public void onEnter() {}

    public abstract void updateRound(float deltaTime);

    public abstract void render();

    public long getRoundDuration() {
        return roundDuration;
    }

    public long getFrameMoveTime() {
        return frameMoveTime;
    }

    public void moveBattleFrame(float deltaTime) {}
}
