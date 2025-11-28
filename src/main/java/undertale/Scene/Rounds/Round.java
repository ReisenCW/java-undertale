package undertale.Scene.Rounds;

import undertale.GameMain.Game;
import undertale.GameObject.ObjectManager;
import undertale.UI.UIManager;

public abstract class Round {
    protected long roundDuration; // 回合持续时间
    protected long frameMoveTime;
    protected ObjectManager objectManager;
    protected UIManager uiManager;

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
