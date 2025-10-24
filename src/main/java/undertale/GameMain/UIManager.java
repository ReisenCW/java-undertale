package undertale.GameMain;

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

public class UIManager extends UIBase {
    private static UIManager instance;

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

    private Player player;

    private EnemyManager enemyManager;
    private FontManager fontManager;
    private MenuTypeWriter menuTypeWriter;
    private BgUIManager bgUIManager;
    private AttackAnimManager attackAnimManager;

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

    static {
        instance = new UIManager();
    }

    private UIManager() {
        super(Game.getConfigManager());
        ConfigManager configManager = Game.getConfigManager();
        fontManager = FontManager.getInstance();
        player = Game.getPlayer();
        menuTypeWriter = new MenuTypeWriter(configManager, fontManager);
        bgUIManager = new BgUIManager(configManager, fontManager, player);
        attackAnimManager = new AttackAnimManager(configManager, fontManager, player);
        enemyManager = EnemyManager.getInstance();

        battle_frame_width = configManager.MENU_FRAME_WIDTH;
        battle_frame_height = configManager.MENU_FRAME_HEIGHT;
        battle_frame_left = configManager.MENU_FRAME_LEFT;
        battle_frame_bottom = configManager.MENU_FRAME_BOTTOM;
    }



    public static UIManager getInstance() {
        return instance;
    }

    public void resetVars() {
        resetStates();
        resetTimeVars();
    }

    private void resetStates() {
        menuState = MenuState.MAIN;
        selectedEnemy = 0;
        selectedAct = 0;
        selectedItem = 0;
        selectedAction = 0;
        itemListFirstIndex = 0;
        setSelected(0);

        attackAnimManager.resetStates();
    }

    private void resetTimeVars() {
        menuTypeWriter.reset();
        attackAnimManager.resetTimeVars();
    }

    public void renderBattleUI() {
        // 渲染按钮, 玩家信息, 战斗框架
        bgUIManager.renderButtons(selectedAction);
        bgUIManager.renderPlayerInfo();
        renderBattleFrame();
    }

    public void renderFrameContents(String roundText) {
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
            case ACT -> {
                Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
                menuTypeWriter.renderTextsInMenu(enemy.getDescriptionByIndex(selectedAct));
            }
            case ITEM -> {
                Item item = player.getItemByIndex(selectedItem);
                int healAmount = item.getHealingAmount();
                player.heal(healAmount);
                String description = "* You ate the " + item.getName() + ", healed " + healAmount + " HP.\n" + item.getAdditionalDescription();
                menuTypeWriter.renderTextsInMenu(description);
            }
            case FIGHT -> {
                attackAnimManager.renderFightPanel(enemyManager.getEnemy(selectedEnemy));
            }
            case MAIN ->
                menuTypeWriter.renderTextsInMenu(roundText);
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
                menuTypeWriter.renderTextsInMenu(text);
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

    // private void renderFightPanel() {
    //     if (!showMiss){
    //         Texture.drawTexture(attack_panel.getId(),
    //                             MENU_FRAME_LEFT + BATTLE_FRAME_LINE_WIDTH, 
    //                             MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + BATTLE_FRAME_LINE_WIDTH,
    //                             MENU_FRAME_WIDTH, MENU_FRAME_HEIGHT);
    //         renderAttackBar();
    //         if(attackBarStopped) {
    //             renderSlice();
    //         }
    //     }
    //     else {
    //         renderMiss();
    //     }
    // }

    // private void renderAttackBar() {
    //     float scaler = 1.7f;
    //     float bar_x = MENU_FRAME_LEFT + BATTLE_FRAME_LINE_WIDTH + attack_bar_offset;
    //     float bar_y = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT / 2 - scaler * attack_bar[attack_bar_index].getHeight() / 2 + BATTLE_FRAME_LINE_WIDTH;
    //     Texture.drawTexture(attack_bar[attack_bar_index].getId(), bar_x, bar_y, scaler * attack_bar[attack_bar_index].getWidth(), scaler * attack_bar[attack_bar_index].getHeight());
    // }

    // private void renderSlice() {
    //     // 在选中的敌人上绘制slice动画
    //     Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
    //     if (enemy == null) return;
    //     float scaler = 2.0f;
    //     float x = enemy.getEntryLeft("body") + enemy.getWidth("body") / 2 - scaler * attack_animation.getFrameWidth() / 2 - 50;
    //     float y = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 - scaler * attack_animation.getFrameHeight() / 2;

    //     attack_animation.renderCurrentFrame(x, y, scaler, scaler, 0, 1, 1, 1, 1);
    //     renderDamage(enemy, player.getAttackPower() * attackRate);
    // }

    // private void renderDamage(Enemy enemy, float damage) {
    //     if (showDamage && enemy != null) {
    //         // 伤害数字
    //         String text = String.valueOf((int)damage);
    //         float dmgTextScaler = 2.5f;
    //         float textX = (RIGHT_MARGIN + LEFT_MARGIN) / 2 - fontManager.getTextWidth(text) / 2 * dmgTextScaler;
    //         float textBaseY = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 + 40;
    //         float moveTotalTime = damageDisplayDuration / 3; // 上升和下降共1/3时间
    //         float theta = (float)(Math.min(Math.PI,(Math.PI * damageDisplayElapsed) / moveTotalTime));
    //         float textY = textBaseY - 40 * (float)Math.sin(theta);
    //         fontManager.drawText(text, textX, textY, dmgTextScaler, 1.0f, 0.0f, 0.0f, 1.0f);

    //         // 血条
    //         float maxHealthLength = 900.0f;
    //         float healthHeight = 20.0f;
    //         float currentHealthLength = displayedHealth / enemy.maxHealth * maxHealthLength;
    //         float healthX = enemy.getEntryLeft("body") + enemy.getWidth("body") / 2 - maxHealthLength / 2 - 50;
    //         float healthY = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 + 80;

    //         Texture.drawRect(healthX, healthY, maxHealthLength, healthHeight, 1.0f, 0.0f, 0.0f, 1.0f);
    //         Texture.drawRect(healthX, healthY, currentHealthLength, healthHeight, 0.0f, 1.0f, 0.0f, 1.0f);
    //     }
    // }

    // private void renderMiss() {
    //     Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
    //     float missScaler = 0.6f;
    //     if (enemy == null) return;
    //     if (showMiss) {
    //         float missX = (RIGHT_MARGIN + LEFT_MARGIN) / 2 - miss_text.getWidth() / 2 * missScaler;
    //         float baseMissY = enemy.getEntryBottom("body") - enemy.getHeight("body") / 2 - miss_text.getHeight() / 2 * missScaler + 50;
    //         // sin从0到pi变化，振幅50
    //         float moveTotalTime = MISS_DISPLAY_DURATION / 2; // 上升和下降共1/2时间
    //         float theta = (float)(Math.PI * missDisplayElapsed / moveTotalTime);
    //         float missY = baseMissY - 50 * Math.max(0, (float)Math.sin(theta));
    //         Texture.drawTexture(miss_text.getId(), missX, missY, miss_text.getWidth() * missScaler, miss_text.getHeight() * missScaler);

    //         if (missDisplayElapsed >= MISS_DISPLAY_DURATION) {
    //             showMiss = false;
    //             missDisplayElapsed = 0f;
    //             SceneManager.getInstance().shouldSwitch = true;
    //         }
    //     }
    // }

    private void renderBattleFrame() {
        Texture.drawRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 0.0f, 0.0f, 0.0f, 1.0f);
        Texture.drawHollowRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 1.0f, 1.0f, 1.0f, 1.0f, BATTLE_FRAME_LINE_WIDTH);
    }

    // private void updateSliceHpDisplay(float deltaTime) {
    //     Enemy enemy = EnemyManager.getInstance().getEnemy(selectedEnemy);
    //     if (attack_animation.isFinished() && !showDamage) {
    //         int damage = (int)(player.getAttackPower() * attackRate);
    //         showDamage = true;
    //         displayedHealth = enemy.currentHealth;
    //         damageDisplayElapsed = 0f;
    //         enemy.takeDamage(damage);
    //         damagePerMilliSecond = (float)damage / damageDisplayDuration * 8;
    //     }
    //     if(showDamage) {
    //         displayedHealth -= damagePerMilliSecond * deltaTime * 1000;
    //         if(displayedHealth < enemy.currentHealth) {
    //             displayedHealth = enemy.currentHealth;
    //         }
            
    //         damageDisplayElapsed += deltaTime * 1000;
    //         if (damageDisplayElapsed >= damageDisplayDuration) {
    //             showDamage = false;
    //             damageDisplayElapsed = 0f;
    //             // 切换场景
    //             SceneManager.getInstance().shouldSwitch = true;
    //         }
    //     }
    // }

    // public void updateAttackBarPosition(float deltaTime) {
    //     // 在attack_bar_duration内attack_bar_offset从0线性变到MENU_FRAME_WIDTH
    //     // 需在FIGHT状态下每帧调用
    //     if (attack_bar_duration <= 0) return;
    //     attackBarElapsed += deltaTime * 1000f;
    //     float t = Math.min(1.0f, (float)attackBarElapsed / attack_bar_duration);
    //     attack_bar_offset = t * MENU_FRAME_WIDTH;
    //     // 计算attackRate为开口向上的二次函数，最中间为1，两边为0
    //     float norm = t; // 0~1
    //     float center = 0.5f;
    //     attackRate = 1.0f - 4.0f * (norm - center) * (norm - center); // 抛物线
    //     if (attackRate < 0.0f) attackRate = 0.0f;
    //     if (t >= 1.0f) {
    //         attack_bar_offset = MENU_FRAME_WIDTH;
    //     }

    //     // 检查attackBar是否到最右且未按Z
    //     if (attack_bar_offset >= MENU_FRAME_WIDTH && !attackBarStopped && !showMiss) {
    //         showMiss = true;
    //         missDisplayElapsed = 0f;
    //         // attackBar和attackPanel消失
    //         attackBarStopped = true;
    //     }
    // }

    // public void updateAttackBarIndex(float deltaTime) {
    //     // 攻击后,按下Z,Attack bar会停止, 并且在index = 0和1之间来回切换, 每次切换持续时间为300ms
    //     if (!attackBarStopped) return;
    //     attackBarBlinkElapsed += deltaTime * 1000f;
    //     int period = 300; // ms
    //     int phase = (int)((attackBarBlinkElapsed / period) % 2);
    //     attack_bar_index = phase;
    // }

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
                    if (menuTypeWriter.isTypewriterAllShown()) {
                        SceneManager.getInstance().shouldSwitch = true;
                    }
                    yield menuState;
                }
                case FIGHT -> {
                    // Attack bar停止 — 开始攻击时重置相关动画以保证slice动画每次都能播放
                    attackAnimManager.resetSliceAnimation();
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
                menuTypeWriter.showAll();
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
        if (duration <= 0) {
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

    public void menuSelectUp() {
        // 向上选择, item支持分页滚动
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
            attackAnimManager.updateAttackAnim(deltaTime, enemyManager.getEnemy(selectedEnemy));
        }
        attackAnimManager.updateMissTime(deltaTime);
        if (bfMoving != false) {
            menuTypeWriter.update(deltaTime);
        }
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
