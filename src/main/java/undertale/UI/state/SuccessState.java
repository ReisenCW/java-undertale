package undertale.UI.state;

import undertale.Scene.SceneManager;
import undertale.UI.ScreenFadeManager;

/**
 * Success state - display victory message
 */
public class SuccessState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.SUCCESS;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        if(context.getMenuTypeWriter().isTypewriterAllShown() && !context.isBackToMain) {
            context.isBackToMain = true;
            // Use fade out/in effect to return to battle menu
            ScreenFadeManager.getInstance().startFadeOutIn(
                1.5f,
                () -> {
                    SceneManager.getInstance().shouldSwitch = true;
                },
                () -> {
                    context.resetSelections();
                    context.resetTimeVars();
                    context.setState(StateFactory.createState(MenuStateType.BEGIN));
                }
            );
        }
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        context.getMenuTypeWriter().showAll();
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        String msg = "* You won!\n* You earned " + 
            context.getEnemyManager().getTotalExp() + " EXP and " + 
            context.getEnemyManager().getTotalGold(false) + " gold.";
        context.getMenuTypeWriter().renderTextsInMenu(msg);
    }
}
