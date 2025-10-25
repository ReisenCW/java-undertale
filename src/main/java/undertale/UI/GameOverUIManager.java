package undertale.UI;


import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class GameOverUIManager extends UIBase{
    private TypeWriter typeWriter;
    private Texture gameOverBgTexture;
    private float gameOverBgAlpha;
    private float gameOverTimeElapsed;
    
    private final float TEXT_LEFT = 150.0f;
    private final float TEXT_TOP = BOTTOM_MARGIN / 2 + 100.0f;
    private final float LINE_WIDTH = RIGHT_MARGIN - TEXT_LEFT * 2;
    
    private String gameOverText;
    private boolean printMessage;
    private float gameOverAppearDuration = 1.5f; // seconds, 背景淡入时间
    private float printMessageStartTime = 2.0f; // seconds, 开始打印文字的时间

    private String[] gameOverMessages;

    GameOverUIManager(ConfigManager configManager, TypeWriter typeWriter, Player player) {
        super(configManager);
        this.typeWriter = typeWriter;
        gameOverMessages = new String[]{
            player.getName() + ", stay determined...",
            player.getName() + " don't give up...",
            "Interesting...\nShall we hasten?",
            "The fate of the world lies in your hands...",
        };
        reset();
        loadResources();
    }

    public void reset() {
        gameOverBgAlpha = 0.0f;
        gameOverTimeElapsed = 0.0f;
        printMessage = false;
        selectRandomMessage();
    }

    private void loadResources() {
        gameOverBgTexture = Game.getTexture("game_over_bg");
    }

    public void update(float deltaTime) {
        // 更新游戏结束背景的透明度，实现淡入效果
        if (gameOverTimeElapsed < printMessageStartTime) {
            gameOverTimeElapsed += deltaTime;
        } else {
            printMessage = true;
        }
        if (gameOverBgAlpha < 1.0f) {
            gameOverBgAlpha = Math.min(gameOverTimeElapsed / gameOverAppearDuration, 1.0f);
        }

    }

    public void render() {
        renderGameOverBackground();
        renderGameOverMessage();
    }

    private void renderGameOverBackground() {
        Texture.drawTexture(
            gameOverBgTexture.getId(),
            TEXT_LEFT,
            20,
            LINE_WIDTH,
            BOTTOM_MARGIN / 2 - 20,
            0.0f,
            1.0f,1.0f,1.0f, gameOverBgAlpha
        );
    }

    private void renderGameOverMessage() {
        if (printMessage && gameOverText != null) {
            typeWriter.renderTexts(gameOverText, TEXT_LEFT, TEXT_TOP, LINE_WIDTH);
        }
    }

    public void selectRandomMessage() {
        int index = (int)(Math.random() * gameOverMessages.length);
        gameOverText = gameOverMessages[index];
    }
}
