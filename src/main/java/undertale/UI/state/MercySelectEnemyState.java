package undertale.UI.state;

/**
 * Mercy select enemy state - select which enemy to show mercy to
 */
public class MercySelectEnemyState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.MERCY_SELECT_ENEMY;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        context.getSoundManager().playSE("confirm");
        context.setState(StateFactory.createState(MenuStateType.MERCY_SELECT_SPARE));
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        context.getSoundManager().playSE("menu_move");
        context.setState(StateFactory.createState(MenuStateType.MAIN));
    }
    
    @Override
    public void handleSelectUp(MenuStateContext context) {
        if (context.selectedEnemy > 0) {
            context.getSoundManager().playSE("menu_move");
            context.selectedEnemy--;
            context.getEnemyManager().setCurrentEnemy(context.selectedEnemy);
        }
    }
    
    @Override
    public void handleSelectDown(MenuStateContext context) {
        if (context.selectedEnemy < context.getEnemyManager().getEnemyCount() - 1) {
            context.getSoundManager().playSE("menu_move");
            context.selectedEnemy++;
            context.getEnemyManager().setCurrentEnemy(context.selectedEnemy);
        }
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        renderEnemyList(context);
    }
    
    @Override
    public void updatePlayerPosition(MenuStateContext context) {
        updatePlayerPositionInList(context, context.selectedEnemy);
    }
}
