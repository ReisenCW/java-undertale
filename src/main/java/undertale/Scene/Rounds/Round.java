package undertale.Scene.Rounds;

public abstract class Round {
    protected long roundDuration; // 回合持续时间
    protected long frameMoveTime;

    public Round(long duration, long frameMoveTime) {
        this.roundDuration = duration;
        this.frameMoveTime = frameMoveTime;
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
