package undertale.UI.state;

import undertale.Item.Item;

/**
 * Item select item state - select which item to use
 */
public class ItemSelectItemState extends AbstractMenuState {
    
    @Override
    public MenuStateType getStateType() {
        return MenuStateType.ITEM_SELECT_ITEM;
    }
    
    @Override
    public void handleSelect(MenuStateContext context) {
        context.getSoundManager().playSE("confirm");
        
        // Execute item consumption logic
        Item item = context.getPlayer().getItemByIndex(context.selectedItem);
        int healAmount = item.getHealingAmount();
        context.getPlayer().heal(healAmount);
        context.pendingItemDescription = "* You ate the " + item.getName() + 
            ", healed " + healAmount + " HP.\n" + item.getAdditionalDescription();
        
        // Reset typewriter for new text
        context.getMenuTypeWriter().reset();
        
        // Remove item from inventory
        context.getPlayer().removeItemByIndex(context.selectedItem);
        
        context.setState(StateFactory.createState(MenuStateType.ITEM));
    }
    
    @Override
    public void handleCancel(MenuStateContext context) {
        context.getSoundManager().playSE("menu_move");
        context.setState(StateFactory.createState(MenuStateType.MAIN));
    }
    
    @Override
    public void handleSelectUp(MenuStateContext context) {
        if (context.selectedItem > 0) {
            context.getSoundManager().playSE("menu_move");
            context.selectedItem--;
            if (context.selectedItem < context.itemListFirstIndex) {
                context.itemListFirstIndex--;
            }
        }
    }
    
    @Override
    public void handleSelectDown(MenuStateContext context) {
        int itemCnt = context.getPlayer().getItemNumber();
        int itemsPerPage = 4;
        if (context.selectedItem < itemCnt - 1) {
            context.getSoundManager().playSE("menu_move");
            context.selectedItem++;
            if (context.selectedItem >= context.itemListFirstIndex + itemsPerPage) {
                context.itemListFirstIndex++;
            }
        }
    }
    
    @Override
    public void renderFrameContents(MenuStateContext context, String roundText) {
        renderItemList(context);
    }
    
    @Override
    public void updatePlayerPosition(MenuStateContext context) {
        int row = context.selectedItem - context.itemListFirstIndex;
        updatePlayerPositionInList(context, row);
    }
}
