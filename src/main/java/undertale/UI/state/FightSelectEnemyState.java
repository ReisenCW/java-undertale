package undertale.UI.state;

/**
 * Fight select enemy state - select which enemy to attack
 */
public class FightSelectEnemyState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.FIGHT_SELECT_ENEMY;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        context.getSoundManager().playSE("confirm");
        context.setState(StateFactory.createState(MenuStateType.FIGHT));
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
