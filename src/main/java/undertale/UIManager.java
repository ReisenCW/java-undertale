package undertale;

import java.util.ArrayList;

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
    private Texture hp_text;

    public final int TOP_MARGIN = 0;
    public final int BOTTOM_MARGIN = Game.getWindowHeight();
    public final int LEFT_MARGIN = 0;
    public final int RIGHT_MARGIN = Game.getWindowWidth();

    public final int BOTTOM_OFFSET;
    public final float SCALER;
    public final float BTN_WIDTH;
    public final float BTN_HEIGHT;
    public final float BTN_MARGIN;

    public static final float BATTLE_FRAME_LINE_WIDTH = 3.0f;
    public float MENU_FRAME_WIDTH;
    public float MENU_FRAME_HEIGHT;
    public float MENU_FRAME_LEFT;
    public float MENU_FRAME_BOTTOM;
    public float battle_frame_width;
    public float battle_frame_height;
    public float battle_frame_left;
    public float battle_frame_bottom;

    private Player player;

    private FontManager fontManager;
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
    private int totalCharsToShow = 0;
    private long typewriterStartTime = 0;
    private boolean typewriterAllShown = false;
    private final int TYPEWRITER_SPEED = 30; // 每秒显示字符数

    private UIManager() {
        // 初始化
        attack_panel = Game.getTexture("attack_panel");
        hp_text = Game.getTexture("hp_text");

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
        player = Game.getPlayer();

        BOTTOM_OFFSET = 20;
        SCALER = 1.6f;
        BTN_WIDTH = buttons[0].getWidth() * SCALER;
        BTN_HEIGHT = buttons[0].getHeight() * SCALER;
        BTN_MARGIN = (RIGHT_MARGIN - LEFT_MARGIN - 4 * BTN_WIDTH) / 5;

        MENU_FRAME_WIDTH = RIGHT_MARGIN - LEFT_MARGIN - BTN_MARGIN * 2;
        MENU_FRAME_HEIGHT = (BOTTOM_MARGIN - TOP_MARGIN) / 3;
        MENU_FRAME_LEFT = LEFT_MARGIN + BTN_MARGIN;
        MENU_FRAME_BOTTOM = BOTTOM_MARGIN - BOTTOM_OFFSET - BTN_HEIGHT - 80;
        battle_frame_width = MENU_FRAME_WIDTH;
        battle_frame_height = MENU_FRAME_HEIGHT;
        battle_frame_left = MENU_FRAME_LEFT;
        battle_frame_bottom = MENU_FRAME_BOTTOM;

        fontManager = FontManager.getInstance();
    }

    public static UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager();
        }
        return instance;
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
                String description = "You ate the " + item.getName() + ", healed " + healAmount + " HP.\n" + item.getAdditionalDescription();
                renderTextsInMenu(description);
            }
            case MAIN ->
                renderTextsInMenu(roundText);
        }
    }

    private void renderEnemyList() {
        int enemyCnt = EnemyManager.getInstance().getEnemyCount();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        for (int i = 0; i < enemyCnt; i++) {
            Enemy enemy = EnemyManager.getInstance().getEnemy(i);
            fontManager.drawText(enemy.getName(), left, top + i * (fontManager.getFontHeight() + 20), 1.0f, 1.0f, 1.0f, 1.0f);
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
                case FIGHT, ACT, ITEM, MERCY -> {
                    // 若文本已经全部显示切换到FightScene
                    if(typewriterAllShown) {
                        SceneManager.getInstance().shouldSwitch = true;
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
            // 先按\n分割，再对每行做自动换行
            String[] lines = text.split("\\n");
            for (String rawLine : lines) {
                int start = 0;
                int len = rawLine.length();
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
                    start = end;
                }
            }
            totalCharsToShow = 0;
            typewriterStartTime = System.currentTimeMillis();
            typewriterAllShown = false;
        }

        // 计算当前应显示的字符数
        if (!typewriterAllShown) {
            long elapsed = System.currentTimeMillis() - typewriterStartTime;
            int chars = (int)(elapsed * TYPEWRITER_SPEED / 1000.0);
            int total = 0;
            for (String line : displayLines) total += line.length();
            totalCharsToShow = Math.min(chars, total);
            if (totalCharsToShow >= total) {
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

    private void renderBattleFrame() {
        Texture.drawRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 0.0f, 0.0f, 0.0f, 1.0f);        
        Texture.drawHollowRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 1.0f, 1.0f, 1.0f, 1.0f, BATTLE_FRAME_LINE_WIDTH);
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
