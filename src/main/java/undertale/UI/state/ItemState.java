package undertale.UI.state;

import undertale.Scene.SceneManager;

/**
 * Item state - display result of item use and wait for confirmation
 */
public class ItemState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.ITEM;
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
        if (context.pendingItemDescription != null) {
            context.getMenuTypeWriter().renderTextsInMenu(context.pendingItemDescription);
        } else {
            context.getMenuTypeWriter().renderTextsInMenu("");
        }
    }
}
