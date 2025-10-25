package undertale.UI;

import undertale.GameMain.Game;
import undertale.Scene.SceneManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class BeginMenuManager extends UIBase {
    class Option {
        String text;
        float positionX;
        float positionY;

        Option(String text, float positionX, float positionY) {
            this.text = text;
            this.positionX = positionX;
            this.positionY = positionY;
        }
    }

    private FontManager fontManager;
    private Texture heartTexture;
    private float heartScaler = 3.0f;
    private Texture menuBackgroundTexture;
    private int choiceIndex = 0;
    private float optionPositionX = RIGHT_MARGIN / 2 - 50;

    private Option[] options = {
        new Option("Start", optionPositionX, 200),
        new Option("Reset", optionPositionX, 300),
        new Option("Exit", optionPositionX, 400)
    };

    public BeginMenuManager(ConfigManager configManager, FontManager fontManager) {
        super(configManager);
        this.fontManager = fontManager;
        loadResources();
    }

    private void loadResources() {
        heartTexture = Game.getTexture("heart");
        menuBackgroundTexture = Game.getTexture("main_menu_bg");
    }

    public void render() {
        // 绘制背景
        renderBackground();
        // 绘制选项
        renderOptions();
        // 绘制heart
        renderHeart();
    }

    private void renderBackground() {
        // 背景上下拉伸, 左右留白居中
        float bgScaler = (float)BOTTOM_MARGIN / menuBackgroundTexture.getHeight();
        float BG_WIDTH = menuBackgroundTexture.getWidth() * bgScaler;
        float BG_LEFT = (RIGHT_MARGIN - BG_WIDTH) / 2;
        Texture.drawTexture(menuBackgroundTexture.getId(),
            BG_LEFT, 0, BG_WIDTH, BOTTOM_MARGIN);
    }

    private void renderOptions() {
        for (int i = 0; i < options.length; i++) {
            Option option = options[i];
            fontManager.drawText(option.text, option.positionX, option.positionY, 1.2f,
                                0.0f, 0.4f, 1.0f, 1.0f);
        }
    }

    private void renderHeart() {
        float heartX = options[choiceIndex].positionX - 50;
        float heartY = options[choiceIndex].positionY - heartTexture.getHeight() * heartScaler / 2 - 5;
        float heartWidth = heartTexture.getWidth() * heartScaler;
        float heartHeight = heartTexture.getHeight() * heartScaler;
        Texture.drawTexture(heartTexture.getId(),
            heartX, heartY, heartWidth, heartHeight);
    }

    public void selectUp() {
        choiceIndex = (choiceIndex - 1 + options.length) % options.length;
    }

    public void selectDown() {
        choiceIndex = (choiceIndex + 1) % options.length;
    }

    public void confirmSelection() {
        switch (choiceIndex) {
            case 0: // Start
                SceneManager.getInstance().shouldSwitch = true;
                break;
            case 1: // Reset
                // TODO: 删除游戏存档
                break;
            case 2: // Exit
                System.exit(0);
                break;
            default:
                break;
        }
    }

    public int getChoiceIndex() {
        return choiceIndex;
    }

    public void reset() {
        choiceIndex = 0;
    }
}
