package undertale;

public class Timer {
    private final double FRAME_TIME = 1000.0 / 60.0; // 60 FPS
    private long frameStart = 0;

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void setTimerStart() {
        frameStart = getCurrentTime();
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
