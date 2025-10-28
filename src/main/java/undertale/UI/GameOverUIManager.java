package undertale.UI;


import java.util.ArrayList;

import undertale.GameMain.Game;
import undertale.Sound.SoundManager;
import undertale.GameObject.Player;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class GameOverUIManager extends UIBase{
    private TypeWriter typeWriter;
    private Player player;
    private Texture gameOverBgTexture;
    private Texture heartTexture;
    private Texture brokenHeartTexture;
    private ArrayList<Texture> heartShardTextures;

    // per-shard animation parameters
    private float[] shardAngleDeg;
    private float[] shardSpeed;
    private float[] shardRotSpeed; // degrees per second
    private float[] shardPhase; // phase offset for scale/rotation
    private boolean shardsInitialized = false;

    private SoundManager soundManager;
    private boolean playedHeartBreakSE = false;
    private boolean playedHeartExplodeSE = false;

    private float gameOverBgAlpha;
    private float gameOverTimeElapsed;
    
    private final float TEXT_LEFT = 150.0f;
    private final float TEXT_TOP = BOTTOM_MARGIN / 2 + 100.0f;
    private final float LINE_WIDTH = RIGHT_MARGIN - TEXT_LEFT * 2;
    
    private String gameOverText;
    private boolean printMessage;

    private float heartStaticTime = 1.0f;
    private float heartBreakStaticTime = 0.8f;
    private float heartBreakAnimationTime = 2.0f;
    private float HEART_ANIM_DURATION = heartStaticTime + heartBreakStaticTime + heartBreakAnimationTime;

    private float gameOverAppearDuration = 1.5f; // seconds, 背景淡入时间
    private float printMessageStartTime = 2.0f; // seconds, 开始打印文字的时间

    private String[] gameOverMessages;

    GameOverUIManager(ConfigManager configManager, TypeWriter typeWriter, Player player) {
        super(configManager);
        this.typeWriter = typeWriter;
        this.player = player;
        this.soundManager = SoundManager.getInstance();
        gameOverMessages = new String[]{
            player.getName() + ", stay determined...",
            player.getName() + " don't give up...",
            "Interesting...\nShall we hasten?",
            "The fate of the world lies in your hands...",
        };
        reset();
        loadResources();
    }

    public void reset() {
        gameOverBgAlpha = 0.0f;
        gameOverTimeElapsed = 0.0f;
        printMessage = false;
        selectRandomMessage();
        shardsInitialized = false;
        playedHeartBreakSE = false;
        playedHeartExplodeSE = false;
    }

    private void loadResources() {
        gameOverBgTexture = Game.getTexture("game_over_bg");
        heartTexture = Game.getTexture("heart");
        brokenHeartTexture = Game.getTexture("broken_heart");
        heartShardTextures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            heartShardTextures.add(Game.getTexture("heart_shard_" + i));
        }
    }

    public void update(float deltaTime) {
        // 统一以 gameOverTimeElapsed 记录自进入 GameOver 的时间
        gameOverTimeElapsed += deltaTime;

        // 在心跳破碎动画播放结束前，不开始背景淡入与文字打印
        if (gameOverTimeElapsed < HEART_ANIM_DURATION) {
            // 仍在心跳动画阶段，直接返回
            return;
        }

        // 计算心跳动画结束后的已过时间（相对时间），用于后续的淡入与文字显示
        float postHeartElapsed = gameOverTimeElapsed - HEART_ANIM_DURATION;

        // 控制何时开始打印文字（printMessageStartTime 现在是相对心跳动画后的延迟）
        if (postHeartElapsed >= printMessageStartTime) {
            printMessage = true;
        }

        // 背景淡入以相对时间来计算
        if (gameOverBgAlpha < 1.0f) {
            gameOverBgAlpha = Math.min(postHeartElapsed / gameOverAppearDuration, 1.0f);
        }

    }

    public void render() {
        // 心跳破碎动画始终先渲染
        renderHeartBreakAnimation();

        // 其它 UI 在心跳动画结束后再渲染（基于 gameOverTimeElapsed 与 HEART_ANIM_DURATION）
        if (gameOverTimeElapsed >= HEART_ANIM_DURATION) {
            renderGameOverBackground();
            renderGameOverMessage();
        }
    }

    private void renderHeartBreakAnimation() {
        // 静止阶段
        if (gameOverTimeElapsed < heartStaticTime) {
            Texture.drawTexture(
                heartTexture.getId(),
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
            );
            return;
        }
        // 碎裂静止阶段
        float scaler = player.getHeight() / brokenHeartTexture.getHeight();
        float bhx = player.getX() + (player.getWidth() - brokenHeartTexture.getWidth() * scaler) / 2;
        if(gameOverTimeElapsed < heartStaticTime + heartBreakStaticTime) {
            // 心碎刚发生，播放一次 heart_break 音效
            if (!playedHeartBreakSE) {
                if (soundManager != null) soundManager.playSE("heart_break");
                playedHeartBreakSE = true;
            }
            Texture.drawTexture(
                brokenHeartTexture.getId(),
                bhx,
                player.getY(),
                scaler * brokenHeartTexture.getWidth(),
                scaler * brokenHeartTexture.getHeight()
            );
            return;
        }
        // 碎裂动画阶段（物理运动 + 3D 旋转/缩放效果）
        if(gameOverTimeElapsed < HEART_ANIM_DURATION) {
            float animElapsed = gameOverTimeElapsed - heartStaticTime - heartBreakStaticTime;
            if (animElapsed < 0f) return;
            float animProgress = animElapsed / heartBreakAnimationTime; // 0.0 to 1.0

            // 初始化每个碎片的随机参数（只一次）
            if (!shardsInitialized) {
                int n = heartShardTextures.size();
                shardAngleDeg = new float[n];
                shardSpeed = new float[n];
                shardRotSpeed = new float[n];
                shardPhase = new float[n];
                for (int i = 0; i < n; i++) {
                    // 随机角度在 15 到 165 度之间（向上散开）
                    shardAngleDeg[i] = 15.0f + (float)Math.random() * (165.0f - 15.0f);
                    // 随机初速度（像素/秒），使用适中范围
                    shardSpeed[i] = 200.0f + (float)Math.random() * 400.0f; // 200 - 600 px/s
                    // 旋转速度（度/秒）
                    shardRotSpeed[i] = 180.0f + (float)Math.random() * 720.0f; // 180 - 900 dps
                    // 相位用于 scale 的随机化
                    shardPhase[i] = (float)Math.random() * (float)(Math.PI * 2);
                }
                shardsInitialized = true;
                // 在碎片真正开始爆炸时播放一次 heart_explode 音效
                if (!playedHeartExplodeSE) {
                    if (soundManager != null) soundManager.playSE("heart_explode");
                    playedHeartExplodeSE = true;
                }
            }

            // 重力（像素/秒^2），调整以获得合适的下落曲线
            final float GRAVITY = 900.0f;

            // 计算碎片初始绘制中心（以 broken heart 的左上为基准）
            // 重用前面已计算的 scaler 和 bhx（避免重复声明），只计算宽高与基准 y
            float bhy = player.getY();

            for (int i = 0; i < heartShardTextures.size(); i++) {
                Texture shardTexture = heartShardTextures.get(i);
                float angleRad = (float)Math.toRadians(shardAngleDeg[i]);
                float vx = (float)Math.cos(angleRad) * shardSpeed[i];
                float vy = -(float)Math.sin(angleRad) * shardSpeed[i];

                // 物理运动：x = vx * t, y = vy * t - 0.5 * g * t^2(但以"下"为正方向)
                float xOffset = vx * animElapsed;
                float yOffset = vy * animElapsed + 0.5f * GRAVITY * animElapsed * animElapsed;

                // 旋转（度）
                float rotation = shardRotSpeed[i] * animElapsed + shardPhase[i] * 57.29578f; // phase -> degrees

                // 3D 缩放效果：随时间轻微缩放并做周期抖动以模拟翻转
                float baseScale = 1.0f + 0.6f * (1.0f - animProgress); // 从 ~1.2 缩小到 ~0.6
                float wobble = 0.2f * (float)Math.sin(animElapsed * shardRotSpeed[i] * 0.02f + shardPhase[i]);
                float scale = Math.max(0.2f, baseScale + wobble);

                float drawW = shardTexture.getWidth() * scale;
                float drawH = shardTexture.getHeight() * scale;

                Texture.drawTexture(
                    shardTexture.getId(),
                    bhx + xOffset,
                    bhy + yOffset,
                    drawW,
                    drawH,
                    rotation
                );
            }
            return;
        }

    }

    private void renderGameOverBackground() {
        Texture.drawTexture(
            gameOverBgTexture.getId(),
            TEXT_LEFT,
            20,
            LINE_WIDTH,
            BOTTOM_MARGIN / 2 - 20,
            0.0f,
            1.0f,1.0f,1.0f, gameOverBgAlpha
        );
    }

    private void renderGameOverMessage() {
        if (printMessage && gameOverText != null) {
            typeWriter.renderTexts(gameOverText, TEXT_LEFT, TEXT_TOP, LINE_WIDTH);
        }
    }

    public void selectRandomMessage() {
        int index = (int)(Math.random() * gameOverMessages.length);
        gameOverText = gameOverMessages[index];
    }

    public boolean isMessageAllPrinted() {
        if (printMessage) {
            return typeWriter.isTypewriterAllShown();
        }
        return false;
    }

    public void showAllMessages() {
        typeWriter.showAll();
    }

    public boolean isHeartAnimFinished() {
        return gameOverTimeElapsed >= HEART_ANIM_DURATION;
    }
}
