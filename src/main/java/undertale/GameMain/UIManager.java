package undertale.GameMain;

import java.util.ArrayList;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.Enemy.Enemy;
import undertale.Enemy.EnemyManager;
import undertale.GameObject.Player;
import undertale.Item.Item;
import undertale.Scene.SceneManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class UIManager {

    private static UIManager instance;
    private Texture attack_normal;
    private Texture attack_chosen;
    private Texture act_normal;
    private Texture act_chosen;
    private Texture item_normal;
    private Texture item_chosen;
    private Texture mercy_normal;
    private Texture mercy_chosen;
    private Texture[] buttons;

    private Texture attack_panel;
    private Texture attack_bar[];
    private Texture hp_text;
    private Texture miss_text;
    private Animation attack_animation;

    public final int TOP_MARGIN = 0;
    public final int BOTTOM_MARGIN = Game.getWindowHeight();
    public final int LEFT_MARGIN = 0;
    public final int RIGHT_MARGIN = Game.getWindowWidth();

    public final float BOTTOM_OFFSET;
    public final float SCALER;
    public final float BTN_WIDTH;
    public final float BTN_HEIGHT;
    public final float BTN_MARGIN;

    public final float BATTLE_FRAME_LINE_WIDTH;
    public final float MENU_FRAME_WIDTH;
    public final float MENU_FRAME_HEIGHT;
    public final float MENU_FRAME_LEFT;
    public final float MENU_FRAME_BOTTOM;
    public float battle_frame_width;
    public float battle_frame_height;
    public float battle_frame_left;
    public float battle_frame_bottom;

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
    private float typewriterElapsed = 0f;
    private float missDisplayElapsed = 0f;


    private Player player;

    private FontManager fontManager;
    private AnimationManager uiAnimationManager;

    public enum MenuState { 
        MAIN, 
        FIGHT_SELECT_ENEMY,
        FIGHT,
        ACT_SELECT_ENEMY,
        ACT_SELECT_ACT,
        ACT,
        ITEM_SELECT_ITEM, 
        ITEM,
        MERCY_SELECT_ENEMY, 
        MERCY_SELECT_SPARE,
        MERCY
    }
    public MenuState menuState = MenuState.MAIN;

    public int selectedEnemy = 0;
    public int selectedAct = 0;
    public int selectedItem = 0;
    public int itemListFirstIndex = 0;
    public int selectedAction = -1;

    // 打字机效果相关变量
    private String lastText = null;
    private ArrayList<String> displayLines = new ArrayList<>();
    private ArrayList<Boolean> isRawNewline = new ArrayList<>();
    private int totalCharsToShow = 0;
    private boolean typewriterAllShown = false;
    private final int TYPEWRITER_SPEED = 30; // 每秒显示字符数

    static {
        instance = new UIManager();
    }

    private UIManager() {
        fontManager = FontManager.getInstance();
        uiAnimationManager = AnimationManager.getInstance();
        ConfigManager configManager = Game.getConfigManager();
        player = Game.getPlayer();
        
        loadResources();

        BOTTOM_OFFSET = configManager.BOTTOM_OFFSET;
        SCALER = configManager.BUTTON_SCALER;
        BTN_WIDTH = configManager.BUTTON_WIDTH;
        BTN_HEIGHT = configManager.BUTTON_HEIGHT;
        BTN_MARGIN = configManager.BUTTON_MARGIN;
        BATTLE_FRAME_LINE_WIDTH = configManager.BATTLE_FRAME_LINE_WIDTH;

        battle_frame_width = MENU_FRAME_WIDTH = configManager.MENU_FRAME_WIDTH;
        battle_frame_height = MENU_FRAME_HEIGHT = configManager.MENU_FRAME_HEIGHT;
        battle_frame_left = MENU_FRAME_LEFT = configManager.MENU_FRAME_LEFT;
        battle_frame_bottom = MENU_FRAME_BOTTOM = configManager.MENU_FRAME_BOTTOM;

        attack_bar_offset = 0.0f;
        attack_bar_index = 0;
        attack_bar_duration = 2100;
    }

    private void loadResources() {
        attack_panel = Game.getTexture("attack_panel");
        attack_bar = new Texture[2];
        attack_bar[0] = Game.getTexture("attack_bar_white");
        attack_bar[1] = Game.getTexture("attack_bar_black");

        hp_text = Game.getTexture("hp_text");
        miss_text = Game.getTexture("miss");

        attack_normal = Game.getTexture("attack_normal");
        attack_chosen = Game.getTexture("attack_chosen");
        act_normal = Game.getTexture("act_normal");
        act_chosen = Game.getTexture("act_chosen");
        item_normal = Game.getTexture("item_normal");
        item_chosen = Game.getTexture("item_chosen");
        mercy_normal = Game.getTexture("mercy_normal");
        mercy_chosen = Game.getTexture("mercy_chosen");
        buttons = new Texture[]{attack_normal, act_normal, item_normal, mercy_normal,
                                attack_chosen, act_chosen, item_chosen, mercy_chosen};
        attack_animation = uiAnimationManager.getAnimation("attack_animation");
        attack_animation.disappearAfterEnds = true;
    }

    public static UIManager getInstance() {
        return instance;
    }

    public void resetVars() {
        resetStates();
        resetTimeVars();
        resetFrameSize();
    }

    private void resetStates() {
        menuState = MenuState.MAIN;
        selectedEnemy = 0;
        selectedAct = 0;
        selectedItem = 0;
        selectedAction = 0;
        itemListFirstIndex = 0;
        attack_animation.setCurrentFrame(0);
        setSelected(0);

        attack_bar_offset = 0.0f;
        attackRate = 1.0f;

        showDamage = false;
        attackBarStopped = false;
    }

    private void resetFrameSize() {
        battle_frame_bottom = MENU_FRAME_BOTTOM;
        battle_frame_height = MENU_FRAME_HEIGHT;
        battle_frame_left = MENU_FRAME_LEFT;
        battle_frame_width = MENU_FRAME_WIDTH;
    }

    private void resetTimeVars() {
        attackBarElapsed = 0f;
        attackBarBlinkElapsed = 0f;
        damageDisplayElapsed = 0f;
        typewriterElapsed = 0f;
        missDisplayElapsed = 0f;
    }

    public void renderBattleUI(String roundText) {
        // 渲染按钮
        renderButtons();
        renderPlayerInfo();
        renderBattleFrame();
        if(roundText == null) return;
        switch(menuState) {
            case FIGHT_SELECT_ENEMY, ACT_SELECT_ENEMY, MERCY_SELECT_ENEMY -> 
                renderEnemyList();
            case ACT_SELECT_ACT -> 
                renderActList(EnemyManager.getInstance().getEnemy(selectedEnemy));
            case ITEM_SELECT_ITEM -> 
                renderItemList();
            case MERCY_SELECT_SPARE -> 
                renderMercyList();
            case ACT ->{
                Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy); 
                renderTextsInMenu(enemy.getDescriptionByIndex(selectedAct));
            }
            case ITEM -> {
                Item item = player.getItemByIndex(selectedItem);
                int healAmount = item.getHealingAmount();
                player.heal(healAmount);
                String description = "* You ate the " + item.getName() + ", healed " + healAmount + " HP.\n" + item.getAdditionalDescription();
                renderTextsInMenu(description);
            }
            case FIGHT -> {
                renderFightPanel();
            }
            case MAIN ->
                renderTextsInMenu(roundText);
            case MERCY -> {
                String text = "* You spared the enemy.";
                Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
                if(enemy != null){
                    if(!enemy.isYellow) {
                        text += "\n* But the enemy's name isn't yellow.";
                    }
                    else{
                        text += "\n* You won!\n* You earned " + enemy.getDropExp() + " EXP and " + enemy.getDropGold() + " gold.";
                    }
                }
                renderTextsInMenu(text);
            }
        }
    }

    private void renderEnemyList() {
        int enemyCnt = EnemyManager.getInstance().getEnemyCount();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        for (int i = 0; i < enemyCnt; i++) {
            Enemy enemy = EnemyManager.getInstance().getEnemy(i);
            float blue = enemy.isYellow ? 0.0f : 1.0f;
            fontManager.drawText(enemy.getName(), left, top + i * (fontManager.getFontHeight() + 20), 1.0f, 1.0f, blue, 1.0f);
        }
    }

    private void renderActList(Enemy enemy) {
        int actCnt = enemy.getActs().size();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        for (int i = 0; i < actCnt; i++) {
            String act = enemy.getActs().get(i);
            fontManager.drawText(act, left, top + i * (fontManager.getFontHeight() + 20), 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderItemList() {
        // 一页显示4个，支持滚动
        int itemCnt = player.getItemNumber();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        int itemsPerPage = 4;
        float infoLeft = MENU_FRAME_LEFT + MENU_FRAME_WIDTH - 200; // 右侧回血信息位置
        for (int i = 0; i < itemsPerPage; i++) {
            int idx = itemListFirstIndex + i;
            if (idx >= itemCnt) break;
            Item item = player.getItemByIndex(idx);
            float y = top + i * (fontManager.getFontHeight() + 20);
            // 高亮当前选中项
            if (idx == selectedItem) {
                fontManager.drawText("> " + item.getName(), left, y, 1.0f, 1.0f, 0.5f, 1.0f);
                // 显示回血信息
                String healInfo = "+" + item.getHealingAmount() + " HP";
                fontManager.drawText(healInfo, infoLeft, y, 1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                fontManager.drawText(item.getName(), left, y, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        // 分页指示器：竖直小方块，当前页为大实心，其余为小空心
        int totalPages = Math.max(itemCnt - 3, 1);
        int currentPage = itemListFirstIndex;
        float indicatorX = MENU_FRAME_LEFT + MENU_FRAME_WIDTH - 30; // 靠近对话框右边框左侧
        float indicatorTop = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 30;
        float gap = 40;
        for (int p = 0; p < totalPages; p++) {
            float cx = indicatorX;
            float cy = indicatorTop + p * gap;
            if (p == currentPage) {
                // 大实心方块
                Texture.drawRect(cx, cy, 14, 14, 1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                // 小空心方块
                Texture.drawHollowRect(cx + 2, cy + 2, 10, 10, 1.0f, 1.0f, 1.0f, 1.0f, 2.0f);
            }
        }
        if (itemListFirstIndex > 0) {
            fontManager.drawText("↑", left - 40, top, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        if (itemListFirstIndex + itemsPerPage < itemCnt) {
            fontManager.drawText("↓", left - 40, top + (itemsPerPage - 1) * (fontManager.getFontHeight() + 20), 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderMercyList() {
        // spare单项
        fontManager.drawText("spare", MENU_FRAME_LEFT + 100, MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderButtons(){

        for (int i = 0; i < 4; i++) {
            Texture.drawTexture(buttons[i + (i == selectedAction ? 4 : 0)].getId(),
                LEFT_MARGIN + BTN_MARGIN + i * (BTN_WIDTH + BTN_MARGIN), BOTTOM_MARGIN - BTN_HEIGHT - BOTTOM_OFFSET,
                BTN_WIDTH, BTN_HEIGHT);
        }
    }

    private void renderPlayerInfo() {
        // 绘制 name, LV, HP/MaxHP信息
        float HEIGHT = BOTTOM_MARGIN - BOTTOM_OFFSET - BTN_HEIGHT - 20;
        float OFFSET = LEFT_MARGIN + BTN_MARGIN;
        // 绘制name
        fontManager.drawText(player.getName(), OFFSET , HEIGHT, 1.0f, 1.0f, 1.0f, 1.0f);
        // 绘制LV
        fontManager.drawText("LV " + player.getLevel(), OFFSET + BTN_WIDTH / 4 * 3, HEIGHT, 1.0f, 1.0f, 1.0f, 1.0f);
        // 绘制HP
        float hpLeft = OFFSET + BTN_WIDTH * 3 / 2 + BTN_MARGIN - hp_text.getWidth();
        float hpTop = HEIGHT - hp_text.getHeight() * 2;
        Texture.drawTexture(hp_text.getId(), hpLeft, hpTop, hp_text.getWidth() * 2, hp_text.getHeight() * 2);
        // 绘制HP条，用红色绘制maxHealth长度，再用黄色覆盖currentHealth长度
        float HP_BAR_WIDTH = player.getMaxHealth() * 3;
        float HP_BAR_CURRENT_WIDTH = player.getCurrentHealth() * 3;
        float HP_BAR_HEIGHT = fontManager.getFontHeight();
        float HP_BAR_X = OFFSET + BTN_WIDTH * 3 / 2 + BTN_MARGIN + fontManager.getTextWidth("HP ") + 20;
        float HP_BAR_Y = HEIGHT - HP_BAR_HEIGHT / 2 - 8;
        // 绘制maxHealth
        Texture.drawRect(HP_BAR_X, HP_BAR_Y, HP_BAR_WIDTH, HP_BAR_HEIGHT, 1.0f, 0.0f, 0.0f, 1.0f);
        // 绘制currentHealth
        Texture.drawRect(HP_BAR_X, HP_BAR_Y, HP_BAR_CURRENT_WIDTH, HP_BAR_HEIGHT, 1.0f, 1.0f, 0.0f, 1.0f);

        // 绘制currentHealth/maxHealth
        String hpText = player.getCurrentHealth() + "  /  " + player.getMaxHealth();
        fontManager.drawText(hpText, HP_BAR_X + HP_BAR_WIDTH + 20, HEIGHT, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderFightPanel() {
        if (!showMiss){
            Texture.drawTexture(attack_panel.getId(),
                                MENU_FRAME_LEFT + BATTLE_FRAME_LINE_WIDTH, 
                                MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + BATTLE_FRAME_LINE_WIDTH,
                                MENU_FRAME_WIDTH, MENU_FRAME_HEIGHT);
            renderAttackBar();
            if(attackBarStopped) {
                renderSlice();
            }
        }
        else {
            renderMiss();
        }

    }

    private void renderAttackBar() {
        float scaler = 1.7f;
        float bar_x = MENU_FRAME_LEFT + BATTLE_FRAME_LINE_WIDTH + attack_bar_offset;
        float bar_y = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT / 2 - scaler * attack_bar[attack_bar_index].getHeight() / 2 + BATTLE_FRAME_LINE_WIDTH;
        Texture.drawTexture(attack_bar[attack_bar_index].getId(), bar_x, bar_y, scaler * attack_bar[attack_bar_index].getWidth(), scaler * attack_bar[attack_bar_index].getHeight());
    }

    private void renderSlice() {
        // 在选中的敌人上绘制slice动画
        Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
        if (enemy == null) return;
        float scaler = 2.0f;
        float x = enemy.getEntryLeft("body") + enemy.getWidth("body") / 2 - scaler * attack_animation.getFrameWidth() / 2 - 50;
        float y = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 - scaler * attack_animation.getFrameHeight() / 2;

        attack_animation.renderCurrentFrame(x, y, scaler, scaler, 0, 1, 1, 1, 1);
        renderDamage(enemy, player.getAttackPower() * attackRate);
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

    private void renderMiss() {
        Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
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

    private void renderBattleFrame() {
        Texture.drawRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 0.0f, 0.0f, 0.0f, 1.0f);
        Texture.drawHollowRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 1.0f, 1.0f, 1.0f, 1.0f, BATTLE_FRAME_LINE_WIDTH);
    }

    public void renderTextsInMenu(String text) {
        // 打字机效果，X跳过全部显示，全部显示后Z才可继续
        float left = MENU_FRAME_LEFT + 50;
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float maxWidth = MENU_FRAME_WIDTH - 40;
        float fontHeight = fontManager.getFontHeight() + 5;

        // 若文本变化，重置打字机状态
        if (lastText == null || !lastText.equals(text)) {
            lastText = text;
            displayLines.clear();
            isRawNewline.clear();
            // 先按\n分割，再对每行做自动换行
            String[] lines = text.split("\\n");
            for (String rawLine : lines) {
                int start = 0;
                int len = rawLine.length();
                boolean first = true;
                while (start < len) {
                    int end = start;
                    while (end < len) {
                        int nextSpace = rawLine.indexOf(' ', end);
                        String sub = rawLine.substring(start, nextSpace == -1 ? len : nextSpace);
                        if (fontManager.getTextWidth(sub) > maxWidth) break;
                        end = nextSpace == -1 ? len : nextSpace + 1;
                    }
                    if (end == start) end++;
                    String line = rawLine.substring(start, end);
                    displayLines.add(line);
                    isRawNewline.add(first); // 只有原始\n的第一行才true
                    first = false;
                    start = end;
                }
            }
            totalCharsToShow = 0;
            typewriterElapsed = 0f;
            typewriterAllShown = false;
        }

        // 计算当前应显示的字符数（仅原始\n换行才停顿）
        if (!typewriterAllShown) {
            int total = 0;
            int charsToShow = 0;
            for (int i = 0; i < displayLines.size(); i++) {
                String line = displayLines.get(i);
                boolean pause = isRawNewline != null && isRawNewline.size() > i && isRawNewline.get(i);
                float lineStart = (float)total / TYPEWRITER_SPEED + (pause ? i * 0.25f : 0); // 0.25秒行间停顿
                float lineElapsed = typewriterElapsed - lineStart;
                if (lineElapsed > 0) {
                    int lineChars = Math.min(line.length(), (int)(lineElapsed * TYPEWRITER_SPEED));
                    charsToShow += lineChars;
                }
                // 若本行未全部显示，后续行不显示
                if (lineElapsed < ((float)line.length() / TYPEWRITER_SPEED)) {
                    break;
                }
                total += line.length();
            }
            // 限制最大
            int allChars = 0;
            for (String l : displayLines) allChars += l.length();
            totalCharsToShow = Math.min(charsToShow, allChars);
            if (totalCharsToShow >= allChars) {
                typewriterAllShown = true;
            }
        }

        // 绘制文本
        int shown = 0;
        int rowIdx = 0;
        for (String line : displayLines) {
            int remain = totalCharsToShow - shown;
            if (remain <= 0) break;
            int toShow = Math.min(remain, line.length());
            fontManager.drawText(line.substring(0, toShow), left, top + rowIdx * fontHeight, 1.0f, 1.0f, 1.0f, 1.0f);
            shown += toShow;
            rowIdx++;
        }
    }

    private void updateSliceHpDisplay(float deltaTime) {
        Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
        if (attack_animation.isFinished() && !showDamage) {
            int damage = (int)(player.getAttackPower() * attackRate);
            showDamage = true;
            displayedHealth = enemy.currentHealth;
            damageDisplayElapsed = 0f;
            enemy.takeDamage(damage);
            damagePerMilliSecond = (float)damage / damageDisplayDuration * 8;
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
                // 切换场景
                SceneManager.getInstance().shouldSwitch = true;
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

    public void updatePlayerMenuPosition() {
        if(menuState == MenuState.FIGHT || menuState == MenuState.ACT || menuState == MenuState.ITEM || menuState == MenuState.MERCY) {
            return;
        }
        else if(menuState == MenuState.MAIN) {
            int LEFT_OFFSET = 25;
            player.setPosition(LEFT_MARGIN + BTN_MARGIN + selectedAction * (BTN_WIDTH + BTN_MARGIN) + LEFT_OFFSET - player.getWidth() / 2, BOTTOM_MARGIN - BOTTOM_OFFSET  - BTN_HEIGHT/2 - player.getHeight()/2);
        }
        else {
            int row = switch(menuState) {
                case FIGHT_SELECT_ENEMY, MERCY_SELECT_ENEMY -> selectedEnemy;
                case ACT_SELECT_ACT -> selectedAct;
                case ITEM_SELECT_ITEM -> selectedItem - itemListFirstIndex;
                case MERCY_SELECT_SPARE -> 0;
                default -> 0;
            };
            // 渲染在list
            player.setPosition(MENU_FRAME_LEFT + 60 - player.getWidth() / 2, MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 40 + row * (fontManager.getFontHeight() + 20) - player.getHeight() / 2);
        }
    }

    public void updatePlayerInBound() {
        player.handlePlayerOutBound(battle_frame_left + BATTLE_FRAME_LINE_WIDTH, 
                                    battle_frame_left + battle_frame_width - BATTLE_FRAME_LINE_WIDTH, 
                                    battle_frame_bottom - battle_frame_height + BATTLE_FRAME_LINE_WIDTH, 
                                    battle_frame_bottom - BATTLE_FRAME_LINE_WIDTH);
    }

    // 菜单“确定”操作
    public void handleMenuSelect() {
        if (menuState == MenuState.MAIN) {
            menuState = switch(selectedAction) {
                case 0 -> MenuState.FIGHT_SELECT_ENEMY;
                case 1 -> {
                    // 若没有act，则不进入act菜单
                    if(EnemyManager.getInstance().getEnemy(selectedEnemy).getActs().isEmpty()){
                        yield MenuState.MAIN;
                    }
                    else
                        yield MenuState.ACT_SELECT_ENEMY;
                }
                case 2 -> {
                    if(player.getItemNumber() == 0){
                        yield MenuState.MAIN;
                    }
                    else
                        yield MenuState.ITEM_SELECT_ITEM;
                }
                    case 3 -> MenuState.MERCY_SELECT_ENEMY;
                default -> MenuState.MAIN;
            };
        }
        else {
            menuState = switch(menuState) {
                case FIGHT_SELECT_ENEMY -> MenuState.FIGHT;
                case ACT_SELECT_ENEMY -> MenuState.ACT_SELECT_ACT;
                case ACT_SELECT_ACT -> MenuState.ACT;
                case ITEM_SELECT_ITEM -> MenuState.ITEM;
                case MERCY_SELECT_ENEMY -> MenuState.MERCY_SELECT_SPARE;
                case MERCY_SELECT_SPARE -> MenuState.MERCY;
                case ACT, ITEM, MERCY -> {
                    // 若文本已经全部显示切换到FightScene
                    if(typewriterAllShown) {
                        SceneManager.getInstance().shouldSwitch = true;
                    }
                    yield menuState;
                }
                case FIGHT -> {
                    // Attack bar停止 — 开始攻击时重置相关动画以保证slice动画每次都能播放
                    if (!attackBarStopped) {
                        attackBarStopped = true;
                        // reset slice动画
                        if (attack_animation != null) {
                            attack_animation.reset();
                        }
                    }
                    yield menuState;
                }
                default -> menuState;
            };
        }
    }

    // 菜单“撤销/返回”操作
    public void handleMenuCancel() {
        // X返回上一级
        switch(menuState) {
            case MAIN -> {}
            case FIGHT_SELECT_ENEMY,  ACT_SELECT_ENEMY, MERCY_SELECT_ENEMY, ITEM_SELECT_ITEM ->
                menuState = MenuState.MAIN;
            case ACT_SELECT_ACT ->
                menuState = MenuState.ACT_SELECT_ENEMY;
            case MERCY_SELECT_SPARE ->
                menuState = MenuState.MERCY_SELECT_ENEMY;
            case FIGHT, ACT, ITEM, MERCY -> {
                // 打字机全部显示
                showAll();
            }
        }
    }

    public void menuSelectDown() {
        // 向下选择，item支持分页滚动
        switch(menuState) {
            case MAIN -> {}
            case FIGHT_SELECT_ENEMY, MERCY_SELECT_ENEMY, ACT_SELECT_ENEMY -> {
                if (selectedEnemy < EnemyManager.getInstance().getEnemyCount() - 1) {
                    selectedEnemy++;
                }
            }
            case ACT_SELECT_ACT -> {
                Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
                if (selectedAct < enemy.getActs().size() - 1) {
                    selectedAct++;
                }
            }
            case ITEM_SELECT_ITEM -> {
                int itemCnt = player.getItemNumber();
                int itemsPerPage = 4;
                if (selectedItem < itemCnt - 1) {
                    selectedItem++;
                    if (selectedItem >= itemListFirstIndex + itemsPerPage) {
                        itemListFirstIndex++;
                    }
                }
            }
            case MERCY_SELECT_SPARE -> {
                if (selectedEnemy < EnemyManager.getInstance().getEnemyCount() - 1) {
                    selectedEnemy++;
                }
            }
            case FIGHT, ACT, ITEM, MERCY -> {}
        }
    }

    public void moveBattleFrame(float deltaTime, float duration, float targetWidth, float targetHeight, float targetLeft, float targetBottom) {
        // 按sin函数平滑移动
        float t = Math.min(1.0f, (deltaTime * 1000) / duration);
        float smoothT = (float)(0.5f - 0.5f * Math.cos(Math.PI * t)); 
        battle_frame_width += (targetWidth - battle_frame_width) * smoothT;
        battle_frame_height += (targetHeight - battle_frame_height) * smoothT;
        battle_frame_left += (targetLeft - battle_frame_left) * smoothT;
        battle_frame_bottom += (targetBottom - battle_frame_bottom) * smoothT;
    }

    public void menuSelectUp() {
        // 向上选择，item支持分页滚动
        switch(menuState) {
            case MAIN -> {}
            case FIGHT_SELECT_ENEMY, MERCY_SELECT_ENEMY, ACT_SELECT_ENEMY -> {
                if (selectedEnemy > 0) {
                    selectedEnemy--;
                }
            }
            case ACT_SELECT_ACT -> {
                if (selectedAct > 0) {
                    selectedAct--;
                }
            }
            case ITEM_SELECT_ITEM -> {
                if (selectedItem > 0) {
                    selectedItem--;
                    if (selectedItem < itemListFirstIndex) {
                        itemListFirstIndex--;
                    }
                }
            }
            case MERCY_SELECT_SPARE -> {
                if (selectedEnemy > 0) {
                    selectedEnemy--;
                }
            }
            case FIGHT, ACT, ITEM, MERCY -> {}
        }
    }

    void update(float deltaTime) {
        if(menuState == MenuState.FIGHT) {
            if (!attackBarStopped) {
                updateAttackBarPosition(deltaTime);
            } else {
                updateSliceHpDisplay(deltaTime);
                updateAttackBarIndex(deltaTime);
                attack_animation.updateAnimation(deltaTime);
            }
        }
        if (showMiss) {
            missDisplayElapsed += deltaTime;
        }
        if (!typewriterAllShown) {
            typewriterElapsed += deltaTime;
        }
    }
    
    public void showAll() {
        if (!typewriterAllShown) {
            int total = 0;
            for (String line : displayLines) total += line.length();
            totalCharsToShow = total;
            typewriterAllShown = true;
        }
    }

    public boolean isTypewriterAllShown() {
        return typewriterAllShown;
    }



    public void selectMoveRight() {
        if(menuState != MenuState.MAIN) return;
        selectedAction = (selectedAction + 1) % 4;
    }

    public void selectMoveLeft() {
        if(menuState != MenuState.MAIN) return;
        selectedAction = (selectedAction + 3) % 4;
    }

    public void setSelected(int index){
        selectedAction = index;
    }

    public void setMenuState(MenuState state) {
        this.menuState = state;
    }

    public MenuState getMenuState() {
        return this.menuState;
    }

    public boolean isRenderPlayer() {
        return switch (menuState) {
            case ACT, FIGHT, ITEM, MERCY -> false;
            default -> true;
        };
    }
}
