package undertale.UI.state;

/**
 * Mercy select spare state - confirm spare action
 */
public class MercySelectSpareState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.MERCY_SELECT_SPARE;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        context.getSoundManager().playSE("confirm");
        context.setState(StateFactory.createState(MenuStateType.MERCY));
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        context.getSoundManager().playSE("menu_move");
        context.setState(StateFactory.createState(MenuStateType.MERCY_SELECT_ENEMY));
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
        context.getFontManager().drawText("spare", 
            MENU_FRAME_LEFT + 100, 
            MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50, 
            1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    @Override
    public void updatePlayerPosition(MenuStateContext context) {
        updatePlayerPositionInList(context, 0);
    }
}
