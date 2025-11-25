package undertale.UI;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameMain.Game;
import undertale.Scene.SceneManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Texture.TextureBuilder;
import undertale.Utils.SaveManager;

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
    private Animation mainMenuAnimation; // 只包含底部的动画部分
    private int choiceIndex = 0;
    private float optionPositionX = RIGHT_MARGIN / 2 - 50;

    private Option[] options = {
        new Option("Start", optionPositionX, 200),
        new Option("Reset", optionPositionX, 300),
        new Option("Exit", optionPositionX, 400)
    };

    public BeginMenuManager(FontManager fontManager) {
        super();
        this.fontManager = fontManager;
        loadResources();
    }

    private void loadResources() {
        AnimationManager animationManager = AnimationManager.getInstance();
        heartTexture = Game.getTexture("heart");
        menuBackgroundTexture = Game.getTexture("main_menu_bg");
        mainMenuAnimation = animationManager.getAnimation("main_menu_animation");
    }

    public void update(float deltaTime) {
        mainMenuAnimation.updateAnimation(deltaTime);
    }

    public void render() {
        // 绘制背景
        renderBackground();
        // 绘制选项
        renderOptions();
        // 绘制success计数
        renderSuccessCount();
        // 绘制heart
        renderHeart();
    }

    private void renderBackground() {
        // 背景上下拉伸, 左右留白居中
        float bgScaler = (float)BOTTOM_MARGIN / menuBackgroundTexture.getHeight();
        float BG_WIDTH = menuBackgroundTexture.getWidth() * bgScaler;
        float BG_LEFT = (RIGHT_MARGIN - BG_WIDTH) / 2;
        new TextureBuilder().textureId(menuBackgroundTexture.getId())
            .position(BG_LEFT, 0)
            .size(BG_WIDTH, BOTTOM_MARGIN)
            .draw();
        // 绘制底部动画部分
        mainMenuAnimation.renderCurrentFrame(
            BG_LEFT, BOTTOM_MARGIN - mainMenuAnimation.getCurrentFrame().getHeight() * bgScaler, bgScaler, bgScaler,
            0.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderOptions() {
        for (int i = 0; i < options.length; i++) {
            Option option = options[i];
            fontManager.drawText(option.text, option.positionX, option.positionY, 1.2f,
                                0.0f, 0.4f, 1.0f, 1.0f);
        }
    }

    private void renderSuccessCount() {
        int successCount = SaveManager.getInstance().getSuccessCount();
        String text = "You have won: " + successCount + " times";
        float textWidth = fontManager.getTextWidth(text);
        float x = RIGHT_MARGIN - textWidth - 20; // 右边距20
        float y = 50; // 上边距50
        fontManager.drawText(text, x, y, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderHeart() {
        float heartX = options[choiceIndex].positionX - 50;
        float heartY = options[choiceIndex].positionY - heartTexture.getHeight() * heartScaler / 2 - 5;
        float heartWidth = heartTexture.getWidth() * heartScaler;
        float heartHeight = heartTexture.getHeight() * heartScaler;
        
        new TextureBuilder().textureId(heartTexture.getId())
            .position(heartX, heartY)
            .size(heartWidth, heartHeight)
            .draw();
    }

    public void selectUp() {
        choiceIndex = (choiceIndex - 1 + options.length) % options.length;
    }

    public void selectDown() {
        choiceIndex = (choiceIndex + 1) % options.length;
    }

    /**
     * 开始菜单的确认选择
     * @return 是否开始游戏
     */
    public boolean confirmSelection() {
        switch (choiceIndex) {
            case 0: // Start
                // 使用渐暗再变亮的特效切换场景
                ScreenFadeManager.getInstance().startFadeOutIn(1.2f,
                    () -> SceneManager.getInstance().shouldSwitch = true,
                    null
                );
                return true;
            case 1: // Reset
                SaveManager.getInstance().reset();
                break;
            case 2: // Exit
                // 渐暗后退出
                ScreenFadeManager.getInstance().startFadeToBlack(1.0f, () -> System.exit(0));
                break;
            default:
                break;
        }
        return false;
    }

    public int getChoiceIndex() {
        return choiceIndex;
    }

    public void reset() {
        choiceIndex = 0;
    }
}
