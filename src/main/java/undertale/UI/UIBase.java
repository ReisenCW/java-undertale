package undertale.UI;

import undertale.GameMain.Game;
import undertale.Utils.ConfigManager;

public abstract class UIBase {
    protected final int TOP_MARGIN = 0;
    protected final int BOTTOM_MARGIN = Game.getWindowHeight();
    protected final int LEFT_MARGIN = 0;
    protected final int RIGHT_MARGIN = Game.getWindowWidth();

    public final float BATTLE_FRAME_LINE_WIDTH;
    public final float MENU_FRAME_WIDTH;
    public final float MENU_FRAME_HEIGHT;
    public final float MENU_FRAME_LEFT;
    public final float MENU_FRAME_BOTTOM;

    public final float BOTTOM_OFFSET;
    public final float SCALER;
    public final float BTN_WIDTH;
    public final float BTN_HEIGHT;
    public final float BTN_MARGIN;

    public UIBase(ConfigManager configManager) {
        BATTLE_FRAME_LINE_WIDTH = configManager.BATTLE_FRAME_LINE_WIDTH;
        MENU_FRAME_WIDTH = configManager.MENU_FRAME_WIDTH;
        MENU_FRAME_HEIGHT = configManager.MENU_FRAME_HEIGHT;
        MENU_FRAME_LEFT = configManager.MENU_FRAME_LEFT;
        MENU_FRAME_BOTTOM = configManager.MENU_FRAME_BOTTOM;
        BOTTOM_OFFSET = configManager.BOTTOM_OFFSET;
        SCALER = configManager.BUTTON_SCALER;
        BTN_WIDTH = configManager.BUTTON_WIDTH;
        BTN_HEIGHT = configManager.BUTTON_HEIGHT;
        BTN_MARGIN = configManager.BUTTON_MARGIN;
    }
}
