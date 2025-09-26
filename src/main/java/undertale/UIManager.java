package undertale;

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
    public int selectedAction = -1;

    private UIManager() {
        // 初始化
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

    public void renderBattleUI() {
        // 渲染按钮
        renderButtons();
        renderPlayerInfo();
        renderBattleFrame();
        switch(menuState) {
            case FIGHT_SELECT_ENEMY, ACT_SELECT_ENEMY, MERCY_SELECT_ENEMY -> 
                renderEnemyList();
            case ACT_SELECT_ACT -> 
                renderActList(EnemyManager.getInstance().getEnemy(selectedEnemy));
            case ITEM_SELECT_ITEM -> 
                renderItemList();
            case MERCY_SELECT_SPARE -> 
                renderMercyList();
            case MAIN ->
                renderTexts();
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


    }

    private void renderMercyList() {
        // spare单项
        fontManager.drawText("spare", MENU_FRAME_LEFT + 100, MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderTexts() {
        // 回合texts
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
        fontManager.drawText("HP ", OFFSET + BTN_WIDTH * 3 / 2 + BTN_MARGIN, HEIGHT, 1.0f, 1.0f, 1.0f, 1.0f);
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
        if(menuState == MenuState.MAIN) {
            int LEFT_OFFSET = 25;
            player.setPosition(LEFT_MARGIN + BTN_MARGIN + selectedAction * (BTN_WIDTH + BTN_MARGIN) + LEFT_OFFSET - player.getWidth() / 2, BOTTOM_MARGIN - BOTTOM_OFFSET  - BTN_HEIGHT/2 - player.getHeight()/2);
        }
        else {
            int row = switch(menuState) {
                case FIGHT_SELECT_ENEMY, MERCY_SELECT_ENEMY -> selectedEnemy;
                case ACT_SELECT_ACT -> selectedAct;
                case ITEM_SELECT_ITEM -> selectedItem;
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
        // Z确定
        if (menuState == MenuState.MAIN) {
            menuState = switch(selectedAction) {
                case 0 -> MenuState.FIGHT_SELECT_ENEMY;
                case 1 -> MenuState.ACT_SELECT_ENEMY;
                case 2 -> MenuState.ITEM_SELECT_ITEM;
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
        }
    }

    public void menuSelectDown() {
        // 向下选择，不需要循环
        switch(menuState) {
            case MAIN -> {
                return;
            }
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
                // item
                return;
            }
            case MERCY_SELECT_SPARE -> {
                if (selectedEnemy < EnemyManager.getInstance().getEnemyCount() - 1) {
                    selectedEnemy++;
                }
            }
        }
    }

    public void menuSelectUp() {
        // 向上选择，不需要循环
        switch(menuState) {
            case MAIN -> {
                return;
            }
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
                // item
                return;
            }
            case MERCY_SELECT_SPARE -> {
                if (selectedEnemy > 0) {
                    selectedEnemy--;
                }
            }
        }
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
}
