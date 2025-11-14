package undertale.UI;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.Enemy.Enemy;
import undertale.Enemy.EnemyManager;
import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Scene.SceneManager;
import undertale.Sound.SoundManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class AttackAnimManager extends UIBase {
    private AnimationManager animationManager;
    private FontManager fontManager;
    private SoundManager soundManager;
    private EnemyManager enemyManager;
    private Player player;

    private Texture attack_panel;
    private Texture attack_bar[];
    private Texture miss_text;
    private Animation attack_animation;

    // Attack bar 动画相关变量
    private boolean attackBarStopped = false;
    // 伤害显示
    private boolean showDamage  = false;
    private long damageDisplayDuration = 2000; // 持续时间，单位ms
    private float displayedHealth = 0f; // 造成伤害时显示的血量,动态变化
    private float damagePerMilliSecond = 0f;

    // miss显示
    private boolean showMiss = false;
    private final long MISS_DISPLAY_DURATION = 1500; // ms
    // attackBar位置
    private float attack_bar_offset; // 从左到右
    private int attack_bar_index; // 0白色 1黑色
    private int attack_bar_duration; // 持续时间，单位ms

    // 伤害倍率
    private float attackRate = 1.0f;

    // time elapsed相关变量
    private float attackBarElapsed = 0f;
    private float attackBarBlinkElapsed = 0f;
    private float damageDisplayElapsed = 0f;
    private float missDisplayElapsed = 0f;

    public AttackAnimManager(ConfigManager configManager, FontManager fontManager, Player player) {
        super(configManager);
        this.fontManager = fontManager;
        this.soundManager = SoundManager.getInstance();
        this.enemyManager = EnemyManager.getInstance();
        this.player = player;
        animationManager = AnimationManager.getInstance();
        attack_bar_offset = 0.0f;
        attack_bar_index = 0;
        attack_bar_duration = 2100;
        loadResources();
    }

    private void loadResources() {
        attack_panel = Game.getTexture("attack_panel");
        attack_bar = new Texture[2];
        attack_bar[0] = Game.getTexture("attack_bar_white");
        attack_bar[1] = Game.getTexture("attack_bar_black");

        miss_text = Game.getTexture("miss");

        attack_animation = animationManager.getAnimation("attack_animation");
        attack_animation.disappearAfterEnds = true;
    }

    public void resetStates() {
        attack_animation.setCurrentFrame(0);

        attack_bar_offset = 0.0f;
        attack_bar_index = 0;
        attackRate = 1.0f;

        showDamage = false;
        showMiss = false;
        attackBarStopped = false;
    }

    public void resetTimeVars() {
        attackBarElapsed = 0f;
        attackBarBlinkElapsed = 0f;
        damageDisplayElapsed = 0f;
        missDisplayElapsed = 0f;
    }

    public void renderFightPanel(Enemy enemy) {
        if (!showMiss){
            Texture.drawTexture(attack_panel.getId(),
                                MENU_FRAME_LEFT + BATTLE_FRAME_LINE_WIDTH, 
                                MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + BATTLE_FRAME_LINE_WIDTH,
                                MENU_FRAME_WIDTH, MENU_FRAME_HEIGHT);
            renderAttackBar();
            if(attackBarStopped) {
                renderSlice(enemy);
            }
        }
        else {
            renderMiss(enemy);
        }
    }

    private void renderAttackBar() {
        float scaler = 1.7f;
        float bar_x = MENU_FRAME_LEFT + BATTLE_FRAME_LINE_WIDTH + attack_bar_offset;
        float bar_y = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT / 2 - scaler * attack_bar[attack_bar_index].getHeight() / 2 + BATTLE_FRAME_LINE_WIDTH;
        Texture.drawTexture(attack_bar[attack_bar_index].getId(), bar_x, bar_y, scaler * attack_bar[attack_bar_index].getWidth(), scaler * attack_bar[attack_bar_index].getHeight());
    }

    private void renderSlice(Enemy enemy) {
        // 在选中的敌人上绘制slice动画
        if (enemy == null) return;
        float scaler = 2.0f;
        float x = enemy.getEntryLeft("body") + enemy.getWidth("body") / 2 - scaler * attack_animation.getFrameWidth() / 2 - 50;
        float y = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 - scaler * attack_animation.getFrameHeight() / 2;

        attack_animation.renderCurrentFrame(x, y, scaler, scaler, 0, 1, 1, 1, 1);
        renderDamage(enemy, player.getAttackPower() * attackRate);
    }

    private void renderMiss(Enemy enemy) {
        float missScaler = 0.6f;
        if (enemy == null) return;
        if (showMiss) {
            float missX = (RIGHT_MARGIN + LEFT_MARGIN) / 2 - miss_text.getWidth() / 2 * missScaler;
            float baseMissY = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 - miss_text.getHeight() / 2 * missScaler + 50;
            // sin从0到pi变化，振幅50
            float moveTotalTime = MISS_DISPLAY_DURATION / 2; // 上升和下降共1/2时间
            float theta = (float)(Math.PI * missDisplayElapsed / moveTotalTime);
            float missY = baseMissY - 50 * Math.max(0, (float)Math.sin(theta));
            Texture.drawTexture(miss_text.getId(), missX, missY, miss_text.getWidth() * missScaler, miss_text.getHeight() * missScaler);

            if (missDisplayElapsed >= MISS_DISPLAY_DURATION) {
                showMiss = false;
                missDisplayElapsed = 0f;
                SceneManager.getInstance().shouldSwitch = true;
            }
        }
    }

    private void renderDamage(Enemy enemy, float damage) {
        if (showDamage && enemy != null) {
            // 伤害数字
            String text = String.valueOf((int)damage);
            float dmgTextScaler = 2.5f;
            float textX = (RIGHT_MARGIN + LEFT_MARGIN) / 2 - fontManager.getTextWidth(text) / 2 * dmgTextScaler;
            float textBaseY = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 + 40;
            float moveTotalTime = damageDisplayDuration / 3; // 上升和下降共1/3时间
            float theta = (float)(Math.min(Math.PI,(Math.PI * damageDisplayElapsed) / moveTotalTime));
            float textY = textBaseY - 40 * (float)Math.sin(theta);
            fontManager.drawText(text, textX, textY, dmgTextScaler, 1.0f, 0.0f, 0.0f, 1.0f);

            // 血条
            float maxHealthLength = 900.0f;
            float healthHeight = 20.0f;
            float currentHealthLength = displayedHealth / enemy.maxHealth * maxHealthLength;
            float healthX = enemy.getEntryLeft("body") + enemy.getWidth("body") / 2 - maxHealthLength / 2 - 50;
            float healthY = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 + 80;

            Texture.drawRect(healthX, healthY, maxHealthLength, healthHeight, 1.0f, 0.0f, 0.0f, 1.0f);
            Texture.drawRect(healthX, healthY, currentHealthLength, healthHeight, 0.0f, 1.0f, 0.0f, 1.0f);
        }
    }

    private void updateSliceHpDisplay(float deltaTime, Enemy enemy) {
        if (attack_animation.isFinished() && !showDamage) {
            soundManager.playSE("enemy_hurt");
            int damage = (int)(player.getAttackPower() * attackRate);
            showDamage = true;
            displayedHealth = enemy.currentHealth;
            damageDisplayElapsed = 0f;
            enemy.takeDamage(damage);
            float realDamage = damage * (1 - enemy.getDefenseRate());
            damagePerMilliSecond = (float)realDamage / damageDisplayDuration * 8;
        }
        if(showDamage) {
            displayedHealth -= damagePerMilliSecond * deltaTime * 1000;
            if(displayedHealth < enemy.currentHealth) {
                displayedHealth = enemy.currentHealth;
            }
            
            damageDisplayElapsed += deltaTime * 1000;
            if (damageDisplayElapsed >= damageDisplayDuration) {
                showDamage = false;
                damageDisplayElapsed = 0f;
                // 如果敌人死亡，开始死亡动画
                if (enemy.currentHealth <= 0) {
                    enemy.startDeathAnimation();
                }
                // 切换场景
                if(!enemyManager.isAllEnemiesDefeated()) {
                    SceneManager.getInstance().shouldSwitch = true;
                }
            }
        }
    }

    public void updateAttackBarPosition(float deltaTime) {
        // 在attack_bar_duration内attack_bar_offset从0线性变到MENU_FRAME_WIDTH
        // 需在FIGHT状态下每帧调用
        if (attack_bar_duration <= 0) return;
        attackBarElapsed += deltaTime * 1000f;
        float t = Math.min(1.0f, (float)attackBarElapsed / attack_bar_duration);
        attack_bar_offset = t * MENU_FRAME_WIDTH;
        // 计算attackRate为开口向上的二次函数，最中间为1，两边为0
        float norm = t; // 0~1
        float center = 0.5f;
        attackRate = 1.0f - 4.0f * (norm - center) * (norm - center); // 抛物线
        if (attackRate < 0.0f) attackRate = 0.0f;
        if (t >= 1.0f) {
            attack_bar_offset = MENU_FRAME_WIDTH;
        }

        // 检查attackBar是否到最右且未按Z
        if (attack_bar_offset >= MENU_FRAME_WIDTH && !attackBarStopped && !showMiss) {
            showMiss = true;
            missDisplayElapsed = 0f;
            // attackBar和attackPanel消失
            attackBarStopped = true;
        }
    }

    public void updateAttackBarIndex(float deltaTime) {
        // 攻击后,按下Z,Attack bar会停止, 并且在index = 0和1之间来回切换, 每次切换持续时间为300ms
        if (!attackBarStopped) return;
        attackBarBlinkElapsed += deltaTime * 1000f;
        int period = 300; // ms
        int phase = (int)((attackBarBlinkElapsed / period) % 2);
        attack_bar_index = phase;
    }

    public void resetSliceAnimation() {
        // Attack bar停止 — 开始攻击时重置相关动画以保证slice动画每次都能播放
        if (!attackBarStopped) {
            attackBarStopped = true;
            if (attack_animation != null) {
                attack_animation.reset();
            }
        }
    }

    public void updateMissTime(float deltaTime) {
        if(showMiss) {
            missDisplayElapsed += deltaTime * 1000;
        }
    }

    public void updateAttackAnim(float deltaTime, Enemy enemy) {
        if (!attackBarStopped) {
            updateAttackBarPosition(deltaTime);
        } else {
            updateSliceHpDisplay(deltaTime, enemy);
            updateAttackBarIndex(deltaTime);
            attack_animation.updateAnimation(deltaTime);
        }
    }

    public boolean isAttackAnimFinished() {
        return attack_animation.isFinished();
    }

    public boolean isDamageDisplayFinished() {
        return !showDamage;
    }
}
