package undertale;

public class Timer {
    private final double FRAME_TIME = 1000.0 / 60.0; // 60 FPS
    private long frameStart = 0;
    private long lastFrameTime = 0;

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void setTimerStart() {
        long currentTime = getCurrentTime();
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
        }
        frameStart = currentTime;
    }

    public float getDeltaTime() {
        long currentTime = getCurrentTime();
        float deltaTime = (currentTime - lastFrameTime) / 1000.0f; // 转换为秒
        lastFrameTime = currentTime;
        return deltaTime;
    }

    /**
     * 从setTimerStart开始计时的持续时间
     * @return ms
     */
    public float durationFromStart() {
        return (getCurrentTime() - frameStart);
    }

    public boolean isTimeElapsed(long milliseconds) {
        return (getCurrentTime() - frameStart) >= milliseconds;
    }

    public void delayIfNeeded() {
        long currentTime = getCurrentTime();
        long frameDuration = currentTime - frameStart;
        if (frameDuration < FRAME_TIME) {
            try {
                Thread.sleep((long)(FRAME_TIME - frameDuration));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
