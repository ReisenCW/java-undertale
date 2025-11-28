package undertale.Scene.Rounds;

import java.util.ArrayList;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.Enemy.Titan;
import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.GameObject.Bullets.TitanSpawn;
import undertale.Sound.SoundManager;
import undertale.GameObject.Bullets.BallBlast;
import undertale.Texture.Texture;
import undertale.Texture.TextureBuilder;
import undertale.Texture.TextureManager;
import undertale.Enemy.EnemyManager;
import undertale.UI.UIManager;

public class RoundSpecial extends Round {
    private float frameLeft = 400;
    private float frameBottom = Game.getWindowHeight() - 10;
    private float frameWidth = Game.getWindowWidth() - frameLeft * 2;
    private float frameHeight = Game.getWindowHeight() - 310;
    private float spawnTimer;
    private float shootTimer;
    private float cooldownTimer;
    private final float SHOOT_INTERVAL = 0.3f;
    private final float SHOOT_COOLDOWN = 1.0f;
    private final float SPAWN_INTERVAL = 0.5f;
    private final int SHOOT_NUM;
    private final float MIN_RADIUS = 400f;
    private final float MAX_RADIUS = 480f;
    private int shootCount = 0;

    private boolean isCharging = false;
    private float chargeTimer = 0.0f;
    private float chargeDuration = 1.8f;
    private float lineSpawnTimer = 0.0f;
    private ArrayList<ChargeLine> chargeLines = new ArrayList<>();
            
    private float blastSpeed = 750.0f;
    private float blastBaseScale = 1.5f;
    private float scaleChangeInterval = 0.1f;
    private float amp = 0.08f;
    private int blastDamage = 5;
    
    private float ballBlastWidth;
    private float ballBlastHeight;
    private float ballX;
    private float ballY;
    private float blastSpeedAngle;

    private class ChargeLine {
        float startX, startY, endX, endY, progress;
        ChargeLine(float sx, float sy, float ex, float ey) {
            startX = sx; startY = sy; endX = ex; endY = ey; progress = 0;
        }
    }

    private Animation titanSpawnAnimation;
    private AnimationManager animationManager;
    private Texture ballBlastTexture;
    private EnemyManager enemyManager;

    public RoundSpecial(int intensity, long duration, long frameMoveTime, UIManager uiManager, EnemyManager enemyManager) {
        super(duration, frameMoveTime, uiManager);
        this.enemyManager = enemyManager;
        SHOOT_NUM = intensity;
        animationManager = AnimationManager.getInstance();
        titanSpawnAnimation = animationManager.getAnimation("titan_spawn_animation");
        ballBlastTexture = TextureManager.getInstance().getTexture("titan_blast_black");
        ballBlastWidth = ballBlastTexture.getWidth();
        ballBlastHeight = ballBlastTexture.getHeight();
        float pos[] = Titan.getCentralPosition();
        ballX = pos[0] - ballBlastWidth / 2 * blastBaseScale;
        ballY = pos[1] - ballBlastHeight / 2 * blastBaseScale;

    }

    @Override
    public void onEnter() {
        // 特殊回合进入时的逻辑
        spawnTimer = 0f;
        shootTimer = 0f;
        cooldownTimer = 0f;
        shootCount = 0;
        isCharging = false;
        chargeTimer = 0.0f;
        chargeLines.clear();
    }
    
    @Override
    public void updateRound(float deltaTime) {
        spawnTimer += deltaTime;
        cooldownTimer += deltaTime;
        
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnTitanSpawn();
        }
        if (cooldownTimer >= SHOOT_COOLDOWN && shootCount == 0 && !isCharging) {
            isCharging = true;
            chargeTimer = 0.0f;
            lineSpawnTimer = 0.0f;
            SoundManager.getInstance().playSE("blast_charge");
        }
        if (isCharging) {
            chargeTimer += deltaTime;
            lineSpawnTimer += deltaTime;
            // titan震动
            // EnemyManager enemyManager = EnemyManager.getInstance();
            Titan titan = (Titan) enemyManager.getCurrentEnemy();
            if (titan != null) {
                titan.setShakeOffset((float) Math.sin(chargeTimer * 80) * 8);
            }
            // 每0.1秒生成3-5条线条
            if (lineSpawnTimer >= 0.1f) {
                lineSpawnTimer -= 0.1f;
                float[] center = Titan.getCentralPosition();
                int numLines = 3 + (int) (Math.random() * 3); // 3-5条
                for (int i = 0; i < numLines; i++) {
                    float angle = (float) (Math.random() * 2 * Math.PI);
                    float dist = 400 + (float) Math.random() * 200;
                    float sx = center[0] + (float) Math.cos(angle) * dist;
                    float sy = center[1] + (float) Math.sin(angle) * dist;
                    chargeLines.add(new ChargeLine(sx, sy, center[0], center[1]));
                }
            }
            for (ChargeLine line : chargeLines) {
                line.progress += deltaTime * 2;
            }
            chargeLines.removeIf(line -> line.progress > 1);
            if (chargeTimer >= chargeDuration) {
                isCharging = false;
                if (titan != null) {
                    titan.setShakeOffset(0);
                }
                chargeLines.clear();
                shootCount = SHOOT_NUM;
                shootTimer = 0;
                cooldownTimer = 0;
                // blast speed angle 朝向player
                Player player = Game.getPlayer();
                if (player == null) return;
                float dx = (player.getX() + player.getWidth() / 2.0f) - (ballX + ballBlastWidth * blastBaseScale / 2.0f);
                float dy = (player.getY() + player.getHeight() / 2.0f) - (ballY + ballBlastHeight * blastBaseScale / 2.0f);
                blastSpeedAngle = (float)Math.toDegrees(Math.atan2(dy, dx));
            }
        }
        if (shootCount > 0) {
            shootTimer += deltaTime;
            // 发射 ballblast
            if (shootTimer >= SHOOT_INTERVAL) {
                shootTimer -= SHOOT_INTERVAL;
                SoundManager.getInstance().playSE("shot");
                BallBlast blast = new BallBlast(
                    ballX,
                    ballY,
                    blastSpeed, blastSpeedAngle,
                    blastBaseScale,
                    scaleChangeInterval,
                    amp,
                    blastDamage
                );
                objectManager.addBullet(blast);
                shootCount--;
            }
        }
    }

    private void spawnTitanSpawn() {
        Player player = Game.getPlayer();
        if (player == null) return;

        // 随机角度和半径
        // angle: -60 ~ 60, 120 ~ 240 度范围内
        float angle = (float)(Math.random() * 120.0f - 60.0f);
        if (Math.random() < 0.5f) {
            angle += 180.0f;
        }
        angle = (float)Math.toRadians(angle);
        float radius = MIN_RADIUS + (float)(Math.random() * (MAX_RADIUS - MIN_RADIUS));

        // 计算生成位置（以玩家中心为圆心）
        float spawnX = player.getX() + player.getWidth() / 2.0f + (float)(Math.cos(angle) * radius);
        float spawnY = player.getY() + player.getHeight() / 2.0f + (float)(Math.sin(angle) * radius);

        // 创建TitanSpawn
        TitanSpawn spawn = new TitanSpawn(spawnX, spawnY, 120f, 5, titanSpawnAnimation);
        // 将spawn添加到objectManager的bullets列表中
        objectManager.addBullet(spawn);
    }

    @Override
    public void moveBattleFrame(float deltaTime) {
        uiManager.moveBattleFrame(deltaTime, frameMoveTime, frameWidth, frameHeight, frameLeft, frameBottom);
    }

    @Override
    public void render() {
        for (ChargeLine line : chargeLines) {
            // 计算当前线条位置，从start向end移动
            float px = line.startX + (line.endX - line.startX) * line.progress;
            float py = line.startY + (line.endY - line.startY) * line.progress;
            // 方向从当前位置到end
            float dx = line.endX - px;
            float dy = line.endY - py;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                dx /= dist;
                dy /= dist;
            }
            // 线条长度固定为50
            float lineLength = 50.0f;
            // 绘制线条从px向end方向延伸lineLength
            // 计算角度
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            // 绘制, 越接近发射时间, 透明度越低
            new TextureBuilder().textureId(Texture.whiteTextureId)
                .position(px, py)
                .size(lineLength, 3)
                .rotation(angle)
                .rgba(1, 1, 1, (chargeDuration - chargeTimer) / chargeDuration)
                .draw();
        }
    }
}
