package undertale;

import java.util.ArrayList;

public class Animation {
    private int frameCount;
    private int currentFrame;
    private float frameDuration; // Duration of each frame in seconds
    private float elapsedTime; // Time since the last frame change
    private boolean loop;
    private boolean isEnd;
    public boolean disappearAfterEnds;
    private boolean horizontalReverse;
    private boolean verticalReverse;

    private ArrayList<Texture> frames;

    public Animation(float frameDuration, boolean loop, ArrayList<Texture> frames, boolean horizontalReverse, boolean verticalReverse) {
        this.frameDuration = frameDuration;
        this.loop = loop;
        this.isEnd = false;
        this.disappearAfterEnds = false;
        this.frames = frames;
        this.currentFrame = 0;
        this.elapsedTime = 0.0f;
        this.horizontalReverse = horizontalReverse;
        this.verticalReverse = verticalReverse;
        this.frameCount = frames.size();
    }

    public Animation(float frameDuration, boolean loop, ArrayList<Texture> frames) {
        this(frameDuration, loop, frames, false, false);
    }

    public Animation(float frameDuration, boolean loop, Texture... frames) {
        this(frameDuration, loop, new ArrayList<Texture>(), false, false);
        for (Texture frame : frames) {
            addFrame(frame);
        }
    }

    public Animation(float frameDuration, boolean loop, boolean horizontalReverse, boolean verticalReverse, Texture... frames) {
        this(frameDuration, loop, new ArrayList<Texture>(), horizontalReverse, verticalReverse);
        for (Texture frame : frames) {
            addFrame(frame);
        }
    }

    public Animation(float frameDuration, boolean loop) {
        this(frameDuration, loop, new ArrayList<Texture>(), false, false);
    }

    public Texture getCurrentFrame() {
        if (frameCount == 0) return null;
        return frames.get(currentFrame);
    }

    public int getCurrentFrameIndex() {
        return currentFrame;
    }

    public void setCurrentFrame(int index) {
        if (index >= 0 && index < frameCount) {
            currentFrame = index;
            elapsedTime = 0.0f; // Reset elapsed time when manually setting frame
        }
    }

    public void addFrame(Texture frame) {
        frames.add(frame);
        frameCount++;
    }

    public void updateAnimation(float deltaTime) {
        if (frameCount == 0) return;

        elapsedTime += deltaTime;
        if (elapsedTime >= frameDuration) {
            elapsedTime -= frameDuration;
            currentFrame++;
            if (currentFrame >= frameCount) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = frameCount - 1; // Stay on the last frame
                }
            }
        }
    }

    public float getFrameWidth() {
        if (frameCount == 0) return 0;
        return frames.get(0).getWidth();
    }

    public float getFrameHeight() {
        if (frameCount == 0) return 0;
        return frames.get(0).getHeight();
    }

    public void renderCurrentFrame(float x, float y, float scaleX, float scaleY, float angle, float r, float g, float b, float a) {
        if(isEnd && disappearAfterEnds) return;
        Texture currentTexture = getCurrentFrame();
        if (currentTexture != null) {
            Texture.drawTexture(currentTexture.getId(), x, y, scaleX * currentTexture.getWidth(), scaleY * currentTexture.getHeight(), angle, r, g, b, a, horizontalReverse, verticalReverse);
            if (!loop && currentFrame == frameCount - 1) {
                isEnd = true;
            } else {
                isEnd = false;
            }
        }
    }

    public boolean isFinished() {
        return isEnd;
    }
}
