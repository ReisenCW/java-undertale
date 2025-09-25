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

    private final int TOP_MARGIN = 0;
    private final int BOTTOM_MARGIN = Game.getWindowHeight();
    private final int LEFT_MARGIN = 0;
    private final int RIGHT_MARGIN = Game.getWindowWidth();

    private final int BOTTOM_OFFSET;
    private final float SCALER;
    private final float BTN_WIDTH;
    private final float BTN_HEIGHT;
    private final float BTN_MARGIN;

    private final float BATTLE_FRAME_LINE_WIDTH = 3.0f;
    private final float MENU_FRAME_WIDTH;
    private final float MENU_FRAME_HEIGHT;
    private final float MENU_FRAME_LEFT;
    private final float MENU_FRAME_BOTTOM; 
    private float battle_frame_width;
    private float battle_frame_height;
    private float battle_frame_left;
    private float battle_frame_bottom;

    private Player player;

    private FontManager fontManager = FontManager.getInstance();

    int selectedAction = -1;

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
        int LEFT_OFFSET = 25;
        player.setPosition(LEFT_MARGIN + BTN_MARGIN + selectedAction * (BTN_WIDTH + BTN_MARGIN) + LEFT_OFFSET - player.getWidth() / 2, BOTTOM_MARGIN - BOTTOM_OFFSET  - BTN_HEIGHT/2 - player.getHeight()/2);
    }

    public void updatePlayerInBound() {
        player.handlePlayerOutBound(battle_frame_left + BATTLE_FRAME_LINE_WIDTH, 
                                    battle_frame_left + battle_frame_width - BATTLE_FRAME_LINE_WIDTH, 
                                    battle_frame_bottom - battle_frame_height + BATTLE_FRAME_LINE_WIDTH, 
                                    battle_frame_bottom - BATTLE_FRAME_LINE_WIDTH);
    }

    private void renderBattleFrame() {
        Texture.drawRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 0.0f, 0.0f, 0.0f, 1.0f);        
        Texture.drawHollowRect(battle_frame_left, battle_frame_bottom - battle_frame_height, battle_frame_width, battle_frame_height, 1.0f, 1.0f, 1.0f, 1.0f, BATTLE_FRAME_LINE_WIDTH);
    }

    public void selectMoveRight() {
        selectedAction = (selectedAction + 1) % 4;
    }

    public void selectMoveLeft() {
        selectedAction = (selectedAction + 3) % 4;
    }

    public void setSelected(int index){
        selectedAction = index;
    }
}
