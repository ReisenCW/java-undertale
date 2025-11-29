package undertale.UI.state;

/**
 * Fight state - perform attack animation
 */
public class FightState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.FIGHT;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        if(!context.sliceSEPlayed) {
            context.getSoundManager().playSE("slice");
            context.sliceSEPlayed = true;
        }
        context.getAttackAnimManager().resetSliceAnimation();
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        context.getMenuTypeWriter().showAll();
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        context.getAttackAnimManager().renderFightPanel(context.getEnemyManager().getCurrentEnemy());
    }
}
