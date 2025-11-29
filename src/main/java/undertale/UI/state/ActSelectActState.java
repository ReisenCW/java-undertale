package undertale.UI.state;

import undertale.Enemy.Enemy;

/**
 * Act select act state - select which action to perform on enemy
 */
public class ActSelectActState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.ACT_SELECT_ACT;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        context.getSoundManager().playSE("confirm");
        Enemy enemy = context.getEnemyManager().getCurrentEnemy();
        
        // Only proceed if the act requirement is met
        if (enemy.getActs().get(context.selectedAct).getRequirementChecker().get()) {
            enemy.getActs().get(context.selectedAct).getFunction().run();
            context.setState(StateFactory.createState(MenuStateType.ACT));
        }
        // Otherwise stay in current state
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        context.getSoundManager().playSE("menu_move");
        context.setState(StateFactory.createState(MenuStateType.ACT_SELECT_ENEMY));
    }
    
    @Override
    public void handleSelectUp(MenuStateContext context) {
        if (context.selectedAct > 0) {
            context.getSoundManager().playSE("menu_move");
            context.selectedAct--;
        }
    }
    
    @Override
    public void handleSelectDown(MenuStateContext context) {
        Enemy enemy = context.getEnemyManager().getCurrentEnemy();
        if (context.selectedAct < enemy.getActs().size() - 1) {
            context.getSoundManager().playSE("menu_move");
            context.selectedAct++;
        }
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        renderActList(context, context.getEnemyManager().getCurrentEnemy());
    }
    
    @Override
    public void updatePlayerPosition(MenuStateContext context) {
        updatePlayerPositionInList(context, context.selectedAct);
    }
}
