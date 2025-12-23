package undertale.UI.state;

/**
 * Main menu state - handles the primary action selection (FIGHT, ACT, ITEM, MERCY)
 */
public class MainMenuState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.MAIN;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        context.getSoundManager().playSE("confirm");
        
        MenuState nextState = switch(context.selectedAction) {
            case 0 -> {
                context.getMenuTypeWriter().showAll();
                yield StateFactory.createState(MenuStateType.FIGHT_SELECT_ENEMY);
            }
            case 1 -> {
                if(context.getEnemyManager().getCurrentEnemy().getActs().isEmpty()) {
                    yield this; // Stay in MAIN
                } else {
                    context.getMenuTypeWriter().showAll();
                    yield StateFactory.createState(MenuStateType.ACT_SELECT_ENEMY);
                }
            }
            case 2 -> {
                if(context.getPlayer().getItemNumber() == 0) {
                    yield this; // Stay in MAIN
                } else {
                    context.getMenuTypeWriter().showAll();
                    yield StateFactory.createState(MenuStateType.ITEM_SELECT_ITEM);
                }
            }
            case 3 -> {
                context.getMenuTypeWriter().showAll();
                yield StateFactory.createState(MenuStateType.MERCY_SELECT_ENEMY);
            }
            default -> this;
        };
        
        context.setState(nextState);
    }

    @Override
    public void handleCancel(MenuStateContext context) {
        context.getMenuTypeWriter().showAll();
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        context.getMenuTypeWriter().renderTextsInMenu(roundText);
    }
    
    @Override
    public void updatePlayerPosition(MenuStateContext context) {
        int LEFT_OFFSET = 25;
        context.getPlayer().setPosition(
            LEFT_MARGIN + BTN_MARGIN + context.selectedAction * (BTN_WIDTH + BTN_MARGIN) + 
            LEFT_OFFSET - context.getPlayer().getWidth() / 2, 
            BOTTOM_MARGIN - BOTTOM_OFFSET - BTN_HEIGHT/2 - context.getPlayer().getHeight()/2
        );
    }
}
