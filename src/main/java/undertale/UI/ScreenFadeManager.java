package undertale.UI;

import undertale.GameMain.Game;
import undertale.Texture.Texture;

/**
 * 屏幕淡入淡出管理器
 * 提供 淡出-淡入 效果 和 纯淡出 效果
 */
public class ScreenFadeManager{
    public enum State {
        INACTIVE,
        FADING_OUT,
        FADING_IN,
        FADING_OUT_FINAL
    }

    private static ScreenFadeManager instance;

    private State state;
    private float timer;
    private float fadeDuration; // 淡化时间(单段)
    private Runnable onMidpoint; // 在淡出完全变黑时调用
    private Runnable onComplete; // 在淡入完全变亮时调用
    private float WINDOW_WIDTH;
    private float WINDOW_HEIGHT;

    static {
        instance = new ScreenFadeManager();
    }

    private ScreenFadeManager() {
        state = State.INACTIVE;
        timer = 0.0f;
        fadeDuration = 1.0f;
        WINDOW_WIDTH = Game.getWindowWidth();
        WINDOW_HEIGHT = Game.getWindowHeight();
    }

    public static ScreenFadeManager getInstance() {
        return instance;
    }

    /**
     * 开始 淡出 再淡入 效果
     * @param totalDurationSeconds 总持续时间(淡出+淡入)
     * @param onMidpoint 在完全变黑时调用
     * @param onComplete 在淡入完成时调用
     */
    public void startFadeOutIn(float totalDurationSeconds, Runnable onMidpoint, Runnable onComplete) {
        if (totalDurationSeconds <= 0) totalDurationSeconds = 1.0f;
        this.fadeDuration = totalDurationSeconds / 2.0f;
        this.onMidpoint = onMidpoint;
        this.onComplete = onComplete;
        this.timer = 0.0f;
        this.state = State.FADING_OUT;
    }

    /**
     * 开始 淡出 效果
     * @param durationSeconds 持续时间
     * @param onComplete 在完全变黑时调用
     */
    public void startFadeToBlack(float durationSeconds, Runnable onComplete) {
        if (durationSeconds <= 0) durationSeconds = 1.0f;
        this.fadeDuration = durationSeconds;
        this.onMidpoint = null;
        this.onComplete = onComplete;
        this.timer = 0.0f;
        this.state = State.FADING_OUT_FINAL;
    }

    public boolean isActive() {
        return state != State.INACTIVE;
    }

    public void update(float deltaTime) {
        if (state == State.INACTIVE) return;
        timer += deltaTime;
        if (state == State.FADING_OUT) {
            if (timer >= fadeDuration) {
                // 已全黑
                timer = 0.0f;
                if (onMidpoint != null) {
                    try {
                        onMidpoint.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // 开始淡入
                state = State.FADING_IN;
            }
        } else if (state == State.FADING_IN) {
            if (timer >= fadeDuration) {
                // 已全亮
                timer = 0.0f;
                state = State.INACTIVE;
                if (onComplete != null) {
                    try {
                        onComplete.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (state == State.FADING_OUT_FINAL) {
            if (timer >= fadeDuration) {
                timer = 0.0f;
                state = State.INACTIVE;
                if (onComplete != null) {
                    try {
                        onComplete.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void render() {
        float alpha = 0.0f;
        switch (state) {
            case INACTIVE -> alpha = 0.0f;
            case FADING_OUT -> alpha = Math.min(1.0f, timer / fadeDuration);
            case FADING_IN -> alpha = Math.max(0.0f, 1.0f - timer / fadeDuration);
            case FADING_OUT_FINAL -> alpha = Math.min(1.0f, timer / fadeDuration);
        }
        if (alpha > 0.0f) {
            Texture.drawRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 0.0f, 0.0f, 0.0f, alpha);
        }
    }
}
