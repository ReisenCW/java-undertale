package undertale.UI;

import undertale.GameMain.Game;
import undertale.Texture.Texture;

/**
 * Singleton manager for screen fade effects.
 * Supports fade-out -> callback -> fade-in (useful for scene switches)
 * and fade-to-black with final callback (useful for quitting).
 */
public class ScreenFadeManager {
    public enum State { INACTIVE, FADING_OUT, FADING_IN, FADING_OUT_FINAL }

    private static ScreenFadeManager instance = new ScreenFadeManager();

    private State state = State.INACTIVE;
    private float timer = 0.0f;
    private float fadeDuration = 1.0f; // seconds for one phase (out or in)
    private Runnable onMidpoint; // called when fully black (for out->in)
    private Runnable onComplete; // called when fully finished (after in or after final out)

    private ScreenFadeManager() {}

    public static ScreenFadeManager getInstance() {
        return instance;
    }

    /**
     * Start a fade-out then call onMidpoint when fully black, then fade-in and call onComplete when finished.
     * totalDuration is the whole roundtrip time (out + in).
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
     * Start a fade-to-black (no fade-in). onComplete called when fully black.
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
                // reached full black
                timer = 0.0f;
                if (onMidpoint != null) {
                    try { onMidpoint.run(); } catch (Exception e) { e.printStackTrace(); }
                }
                state = State.FADING_IN;
            }
        } else if (state == State.FADING_IN) {
            if (timer >= fadeDuration) {
                timer = 0.0f;
                state = State.INACTIVE;
                if (onComplete != null) {
                    try { onComplete.run(); } catch (Exception e) { e.printStackTrace(); }
                }
            }
        } else if (state == State.FADING_OUT_FINAL) {
            if (timer >= fadeDuration) {
                timer = 0.0f;
                state = State.INACTIVE;
                if (onComplete != null) {
                    try { onComplete.run(); } catch (Exception e) { e.printStackTrace(); }
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
            Texture.drawRect(0, 0, Game.getWindowWidth(), Game.getWindowHeight(), 0.0f, 0.0f, 0.0f, alpha);
        }
    }
}
