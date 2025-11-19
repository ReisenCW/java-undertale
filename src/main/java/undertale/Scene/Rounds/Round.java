package undertale.Scene.Rounds;

import undertale.GameMain.Game;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;

public abstract class Round {
    protected long roundDuration; // 回合持续时间
    protected long frameMoveTime;
    protected ObjectManager objectManager;
    protected UIManager uiManager;

    public Round(long duration, long frameMoveTime) {
        this.roundDuration = duration;
        this.frameMoveTime = frameMoveTime;
        objectManager = Game.getObjectManager();
        uiManager = UIManager.getInstance();
    }
    
    public void onEnter() {}

    public abstract void updateRound(float deltaTime);

    public long getRoundDuration() {
        return roundDuration;
    }

    public long getFrameMoveTime() {
        return frameMoveTime;
    }

    public void moveBattleFrame(float deltaTime) {}
}
