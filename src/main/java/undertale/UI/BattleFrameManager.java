package undertale.UI;

import undertale.GameObject.Player;
import undertale.Texture.Texture;

public class BattleFrameManager extends UIBase implements UIComponent{
    Player player;
    
    public float batelFrameWidth;
    public float batelFrameHeight;
    public float batelFrameLeft;
    public float batelFrameBottom;

    // battle frame moving
    private boolean bfMoving = false;
    private float bfMoveElapsedMs = 0f;
    private float bfMoveDurationMs = 0f;
    private float bfStartW, bfStartH, bfStartL, bfStartB;
    private float bfTargetW, bfTargetH, bfTargetL, bfTargetB;

    private final float EPS = 0.1f;


    public BattleFrameManager(Player player) {
        super();
        this.player = player;
        batelFrameLeft = (LEFT_MARGIN + RIGHT_MARGIN) / 2.0f;
        batelFrameBottom = BOTTOM_MARGIN - MENU_FRAME_HEIGHT;
        batelFrameWidth = 0;
        batelFrameHeight = 0;
    }

    public void renderBattleFrame() {
        Texture.drawRect(batelFrameLeft, batelFrameBottom - batelFrameHeight, batelFrameWidth, batelFrameHeight, 0.0f, 0.0f, 0.0f, 1.0f);
        Texture.drawHollowRect(batelFrameLeft, batelFrameBottom - batelFrameHeight, batelFrameWidth, batelFrameHeight, 1.0f, 1.0f, 1.0f, 1.0f, BATTLE_FRAME_LINE_WIDTH);
    }

    public void makePlayerInFrame() {
        player.handlePlayerOutBound(batelFrameLeft + BATTLE_FRAME_LINE_WIDTH,
                batelFrameLeft + batelFrameWidth - BATTLE_FRAME_LINE_WIDTH,
                batelFrameBottom - batelFrameHeight + BATTLE_FRAME_LINE_WIDTH,
                batelFrameBottom - BATTLE_FRAME_LINE_WIDTH);
    }

    @Override
    public void update(float deltaTime) {
        // No per-frame update needed for the frame manager in current design
    }

    @Override
    public void render() {
        renderBattleFrame();
    }

    public void moveBattleFrame(float deltaTime, float duration, float targetWidth, float targetHeight, float targetLeft, float targetBottom) {
        if (duration <= 0) {
            batelFrameWidth = targetWidth;
            batelFrameHeight = targetHeight;
            batelFrameLeft = targetLeft;
            batelFrameBottom = targetBottom;
            bfMoving = false;
            return;
        }

        // 如果已经在目标位置，直接设置结束
        if (Math.abs(batelFrameWidth - targetWidth) < EPS
                && Math.abs(batelFrameHeight - targetHeight) < EPS
                && Math.abs(batelFrameLeft - targetLeft) < EPS
                && Math.abs(batelFrameBottom - targetBottom) < EPS) {
            batelFrameWidth = targetWidth;
            batelFrameHeight = targetHeight;
            batelFrameLeft = targetLeft;
            batelFrameBottom = targetBottom;
            bfMoving = false;
            return;
        }

        if (!bfMoving) {
            bfMoving = true;
            bfMoveElapsedMs = 0f;
            bfMoveDurationMs = duration;
            bfStartW = batelFrameWidth;
            bfStartH = batelFrameHeight;
            bfStartL = batelFrameLeft;
            bfStartB = batelFrameBottom;
            bfTargetW = targetWidth;
            bfTargetH = targetHeight;
            bfTargetL = targetLeft;
            bfTargetB = targetBottom;
        }

        bfMoveElapsedMs += deltaTime * 1000.0f;
        float t = Math.min(1.0f, bfMoveElapsedMs / bfMoveDurationMs);
        float smoothT = (float)(0.5f - 0.5f * Math.cos(Math.PI * t));

        batelFrameWidth = bfStartW + (bfTargetW - bfStartW) * smoothT;
        batelFrameHeight = bfStartH + (bfTargetH - bfStartH) * smoothT;
        batelFrameLeft = bfStartL + (bfTargetL - bfStartL) * smoothT;
        batelFrameBottom = bfStartB + (bfTargetB - bfStartB) * smoothT;

        if (t >= 1.0f) bfMoving = false;
    }

    public float getFrameLeft() {
        return batelFrameLeft;
    }

    public float getFrameBottom() {
        return batelFrameBottom;
    }

    public float getFrameWidth() {
        return batelFrameWidth;
    }

    public float getFrameHeight() {
        return batelFrameHeight;
    }

    public boolean isFrameMoving() {
        return bfMoving;
    }
}
