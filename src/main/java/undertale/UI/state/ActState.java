package undertale.UI.state;

import undertale.Enemy.Enemy;
import undertale.Scene.SceneManager;

/**
 * Act state - display result of act and wait for confirmation
 */
public class ActState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.ACT;
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
        Enemy enemy = context.getEnemyManager().getCurrentEnemy();
        context.getMenuTypeWriter().renderTextsInMenu(
            enemy.getActs().get(context.selectedAct).getDescription()
        );
    }
}
