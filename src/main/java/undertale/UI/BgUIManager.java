package undertale.UI;

import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class BgUIManager extends UIBase {
    private FontManager fontManager;
    private Player player;

    private Texture hpText;
    private Texture attackNormal;
    private Texture attackChosen;
    private Texture actNormal;
    private Texture actChosen;
    private Texture itemNormal;
    private Texture itemChosen;
    private Texture mercyNormal;
    private Texture mercyChosen;
    private Texture tensionBar;
    private Texture tensionBarFill;
    private Texture[] buttons;

    public BgUIManager(ConfigManager configManager, FontManager fontManager, Player player) {
        super(configManager);

        this.fontManager = fontManager;
        this.player = player;

        loadResources();
    }

    public void loadResources() {
        attackNormal = Game.getTexture("attack_normal");
        attackChosen = Game.getTexture("attack_chosen");
        actNormal = Game.getTexture("act_normal");
        actChosen = Game.getTexture("act_chosen");
        itemNormal = Game.getTexture("item_normal");
        itemChosen = Game.getTexture("item_chosen");
        mercyNormal = Game.getTexture("mercy_normal");
        mercyChosen = Game.getTexture("mercy_chosen");
        hpText = Game.getTexture("hp_text");

        tensionBar = Game.getTexture("tension_bar");
        tensionBarFill = Game.getTexture("tension_bar_fill");

        buttons = new Texture[]{
            attackNormal, actNormal, itemNormal, mercyNormal,
            attackChosen, actChosen, itemChosen, mercyChosen
        };
    }

    public void renderButtons(int selectedAction, boolean allowFocus){
        for (int i = 0; i < 4; i++) {
            Texture.drawTexture(buttons[i + (i == selectedAction ? 4 : 0)].getId(),
                LEFT_MARGIN + BTN_MARGIN + i * (BTN_WIDTH + BTN_MARGIN), BOTTOM_MARGIN - BTN_HEIGHT - BOTTOM_OFFSET,
                BTN_WIDTH, BTN_HEIGHT);
        }
    }

    public void renderTensionBar() {
        int tp = player.getTensionPoints();
        float scale = 2.0f;
        // 先绘制tensionBar背景
        float barx = LEFT_MARGIN + 35;
        float bary = TOP_MARGIN + 100;
        Texture.drawTexture(tensionBar.getId(), barx, bary, tensionBar.getWidth() * scale, tensionBar.getHeight() * scale);

        // 再根据tp绘制填充部分(从底部往上竖向填充)
        float tpPercent = tp / 100.0f;
        float fillHeight = tensionBarFill.getHeight() * scale * tpPercent;
        float fillY = bary + tensionBar.getHeight() * scale - fillHeight;
        Texture.drawTexture(tensionBarFill.getId(), 
        barx + 2 * scale, 
        fillY, 
        tensionBarFill.getWidth() * scale, 
        fillHeight,
        0, 0, 64, 192, 255);
    }

    public void renderPlayerInfo() {
        // 绘制 name, LV, HP/MaxHP信息
        float HEIGHT = BOTTOM_MARGIN - BOTTOM_OFFSET - BTN_HEIGHT - 20;
        float OFFSET = LEFT_MARGIN + BTN_MARGIN;
        // 绘制name
        fontManager.drawText(player.getName(), OFFSET , HEIGHT, 1.0f, 1.0f, 1.0f, 1.0f);
        // 绘制LV
        fontManager.drawText("LV " + player.getLevel(), OFFSET + BTN_WIDTH / 4 * 3, HEIGHT, 1.0f, 1.0f, 1.0f, 1.0f);
        // 绘制HP
        float hpLeft = OFFSET + BTN_WIDTH * 3 / 2 + BTN_MARGIN - hpText.getWidth();
        float hpTop = HEIGHT - hpText.getHeight() * 2;
        Texture.drawTexture(hpText.getId(), hpLeft, hpTop, hpText.getWidth() * 2, hpText.getHeight() * 2);
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
}
