package undertale.UI.state;

import undertale.Enemy.Enemy;
import undertale.Item.Item;
import undertale.Texture.Texture;
import undertale.UI.UIBase;

/**
 * Abstract base class for menu states
 * Provides default implementations and helper methods
 */
public abstract class AbstractMenuState extends UIBase implements MenuState {
    
    @Override
    public void handleSelect(MenuStateContext context) {
        // Default: do nothing
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        // Default: do nothing
    }
    
    @Override
    public void handleSelectUp(MenuStateContext context) {
        // Default: do nothing
    }
    
    @Override
    public void handleSelectDown(MenuStateContext context) {
        // Default: do nothing
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        // Default: do nothing
    }
    
    @Override
    public void updatePlayerPosition(MenuStateContext context) {
        // Default: do nothing
    }
    
    // Helper method to render enemy list
    protected void renderEnemyList(MenuStateContext context) {
        int enemyCnt = context.getEnemyManager().getEnemyCount();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        for (int i = 0; i < enemyCnt; i++) {
            Enemy enemy = context.getEnemyManager().getEnemy(i);
            float blue = enemy.isYellow ? 0.0f : 1.0f;
            context.getFontManager().drawText(enemy.getName(), left, 
                top + i * (context.getFontManager().getFontHeight() + 20), 
                1.0f, 1.0f, blue, 1.0f);
        }
    }
    
    // Helper method to render act list
    protected void renderActList(MenuStateContext context, Enemy enemy) {
        int actCnt = enemy.getActs().size();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        for (int i = 0; i < actCnt; i++) {
            Enemy.Act act = enemy.getActs().get(i);
            float greyRGB = 180.0f / 255.0f;
            float actColor = act.getRequirementChecker().get() ? 1.0f : greyRGB;
            context.getFontManager().drawText(act.getName(), left, 
                top + i * (context.getFontManager().getFontHeight() + 20), 
                actColor, actColor, actColor, 1.0f);
            
            context.getFontManager().drawText(act.getRequirement(),
                left + 300, top + i * (context.getFontManager().getFontHeight() + 20),
                0.8f, greyRGB, greyRGB, greyRGB, 1.0f);
        }
    }
    
    // Helper method to render item list
    protected void renderItemList(MenuStateContext context) {
        int itemCnt = context.getPlayer().getItemNumber();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        int itemsPerPage = 4;
        float infoLeft = MENU_FRAME_LEFT + MENU_FRAME_WIDTH - 200;
        
        for (int i = 0; i < itemsPerPage; i++) {
            int idx = context.itemListFirstIndex + i;
            if (idx >= itemCnt) break;
            Item item = context.getPlayer().getItemByIndex(idx);
            float y = top + i * (context.getFontManager().getFontHeight() + 20);
            
            if (idx == context.selectedItem) {
                context.getFontManager().drawText("> " + item.getName(), left, y, 1.0f, 1.0f, 0.5f, 1.0f);
                String healInfo = "+" + item.getHealingAmount() + " HP";
                context.getFontManager().drawText(healInfo, infoLeft, y, 1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                context.getFontManager().drawText(item.getName(), left, y, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        
        // Page indicators
        if (itemCnt > 4) {
            int totalPages = Math.max(itemCnt - 3, 1);
            int currentPage = context.itemListFirstIndex;
            float indicatorX = MENU_FRAME_LEFT + MENU_FRAME_WIDTH - 30;
            float frameCenterY = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT / 2;
            float totalHeight = (totalPages - 1) * 40;
            float indicatorTop = frameCenterY - totalHeight / 2;
            
            for (int p = 0; p < totalPages; p++) {
                float cx = indicatorX;
                float cy = indicatorTop + p * 40;
                if (p == currentPage) {
                    Texture.drawRect(cx, cy, 14, 14, 1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    Texture.drawHollowRect(cx + 2, cy + 2, 10, 10, 1.0f, 1.0f, 1.0f, 1.0f, 2.0f);
                }
            }
        }
        
        if (context.itemListFirstIndex > 0) {
            context.getFontManager().drawText("↑", left - 40, top, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        if (context.itemListFirstIndex + itemsPerPage < itemCnt) {
            context.getFontManager().drawText("↓", left - 40, 
                top + (itemsPerPage - 1) * (context.getFontManager().getFontHeight() + 20), 
                1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
    
    // Helper method to update player position in list
    protected void updatePlayerPositionInList(MenuStateContext context, int row) {
        context.getPlayer().setPosition(
            MENU_FRAME_LEFT + 60 - context.getPlayer().getWidth() / 2, 
            MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 40 + 
            row * (context.getFontManager().getFontHeight() + 20) - 
            context.getPlayer().getHeight() / 2
        );
    }
}
