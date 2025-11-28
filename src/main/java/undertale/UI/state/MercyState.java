package undertale.UI.state;

import undertale.Enemy.Enemy;
import undertale.Scene.SceneManager;

/**
 * Mercy state - display result of mercy action
 */
public class MercyState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.MERCY;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        if (context.getMenuTypeWriter().isTypewriterAllShown()) {
            SceneManager.getInstance().shouldSwitch = true;
        }
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        context.getMenuTypeWriter().showAll();
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        String text = "* You spared the enemy.";
        Enemy enemy = context.getEnemyManager().getCurrentEnemy();
        if(enemy != null) {
            if(!enemy.isYellow) {
                text += "\n* But the enemy's name isn't yellow.";
            } else {
                text += "\n* You won!\n* You earned 0 EXP and " + 
                    context.getEnemyManager().getTotalGold(true) + " gold.";
            }
        }
        context.getMenuTypeWriter().renderTextsInMenu(text);
    }
}
