package undertale.GameMain;

import undertale.GameObject.Player;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class BgUIManager extends UIBase {
    private FontManager fontManager;
    private Player player;

    private Texture hp_text;
    private Texture attack_normal;
    private Texture attack_chosen;
    private Texture act_normal;
    private Texture act_chosen;
    private Texture item_normal;
    private Texture item_chosen;
    private Texture mercy_normal;
    private Texture mercy_chosen;
    private Texture[] buttons;

    public BgUIManager(ConfigManager configManager, FontManager fontManager, Player player) {
        super(configManager);

        this.fontManager = fontManager;
        this.player = player;

        loadResources();
    }

    public void loadResources() {
        attack_normal = Game.getTexture("attack_normal");
        attack_chosen = Game.getTexture("attack_chosen");
        act_normal = Game.getTexture("act_normal");
        act_chosen = Game.getTexture("act_chosen");
        item_normal = Game.getTexture("item_normal");
        item_chosen = Game.getTexture("item_chosen");
        mercy_normal = Game.getTexture("mercy_normal");
        mercy_chosen = Game.getTexture("mercy_chosen");
        hp_text = Game.getTexture("hp_text");

        buttons = new Texture[]{
            attack_normal, act_normal, item_normal, mercy_normal,
            attack_chosen, act_chosen, item_chosen, mercy_chosen
        };
    }

    public void renderButtons(int selectedAction){
        for (int i = 0; i < 4; i++) {
            Texture.drawTexture(buttons[i + (i == selectedAction ? 4 : 0)].getId(),
                LEFT_MARGIN + BTN_MARGIN + i * (BTN_WIDTH + BTN_MARGIN), BOTTOM_MARGIN - BTN_HEIGHT - BOTTOM_OFFSET,
                BTN_WIDTH, BTN_HEIGHT);
        }
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
}
