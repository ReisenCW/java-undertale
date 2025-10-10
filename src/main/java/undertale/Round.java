package undertale;

public class Round {
    private long roundDuration; // 回合持续时间

    public Round(long duration) {
        this.roundDuration = duration;
    }

    public void updateRound(float deltaTime) {}

    public long getRoundDuration() {
        return roundDuration;
    }
}
