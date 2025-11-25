package undertale.UI;

import undertale.GameObject.Player;
import undertale.Texture.Texture;

public class BattleFrameManager extends UIBase{
    Player player;
    
    public float battle_frame_width;
    public float battle_frame_height;
    public float battle_frame_left;
    public float battle_frame_bottom;

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
    }

    public void renderBattleFrame() {
        Texture.drawRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 0.0f, 0.0f, 0.0f, 1.0f);
        Texture.drawHollowRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 1.0f, 1.0f, 1.0f, 1.0f, BATTLE_FRAME_LINE_WIDTH);
    }

    public void makePlayerInFrame() {
        player.handlePlayerOutBound(battle_frame_left + BATTLE_FRAME_LINE_WIDTH,
                battle_frame_left + battle_frame_width - BATTLE_FRAME_LINE_WIDTH,
                battle_frame_bottom - battle_frame_height + BATTLE_FRAME_LINE_WIDTH,
                battle_frame_bottom - BATTLE_FRAME_LINE_WIDTH);
    }

    public void moveBattleFrame(float deltaTime, float duration, float targetWidth, float targetHeight, float targetLeft, float targetBottom) {
        if (duration <= 0) {
            battle_frame_width = targetWidth;
            battle_frame_height = targetHeight;
            battle_frame_left = targetLeft;
            battle_frame_bottom = targetBottom;
            bfMoving = false;
            return;
        }

        // 如果已经在目标位置，直接设置结束
        if (Math.abs(battle_frame_width - targetWidth) < EPS
                && Math.abs(battle_frame_height - targetHeight) < EPS
                && Math.abs(battle_frame_left - targetLeft) < EPS
                && Math.abs(battle_frame_bottom - targetBottom) < EPS) {
            battle_frame_width = targetWidth;
            battle_frame_height = targetHeight;
            battle_frame_left = targetLeft;
            battle_frame_bottom = targetBottom;
            bfMoving = false;
            return;
        }

        if (!bfMoving) {
            bfMoving = true;
            bfMoveElapsedMs = 0f;
            bfMoveDurationMs = duration;
            bfStartW = battle_frame_width;
            bfStartH = battle_frame_height;
            bfStartL = battle_frame_left;
            bfStartB = battle_frame_bottom;
            bfTargetW = targetWidth;
            bfTargetH = targetHeight;
            bfTargetL = targetLeft;
            bfTargetB = targetBottom;
        }

        bfMoveElapsedMs += deltaTime * 1000.0f;
        float t = Math.min(1.0f, bfMoveElapsedMs / bfMoveDurationMs);
        float smoothT = (float)(0.5f - 0.5f * Math.cos(Math.PI * t));

        battle_frame_width = bfStartW + (bfTargetW - bfStartW) * smoothT;
        battle_frame_height = bfStartH + (bfTargetH - bfStartH) * smoothT;
        battle_frame_left = bfStartL + (bfTargetL - bfStartL) * smoothT;
        battle_frame_bottom = bfStartB + (bfTargetB - bfStartB) * smoothT;

        if (t >= 1.0f) bfMoving = false;
    }

    public float getFrameLeft() {
        return battle_frame_left;
    }

    public float getFrameBottom() {
        return battle_frame_bottom;
    }

    public float getFrameWidth() {
        return battle_frame_width;
    }

    public float getFrameHeight() {
        return battle_frame_height;
    }

    public boolean isFrameMoving() {
        return bfMoving;
    }
}
