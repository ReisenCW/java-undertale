package undertale.UI.state;

/**
 * MenuState Interface for State Pattern
 * Defines common behaviors for all menu states
 * 
 * This replaces the old enum-based MenuState system with a proper State Pattern implementation
 */
public interface MenuState {
    /**
     * Handle the select/confirm action
     * @param context The context holding shared state data
     */
    void handleSelect(MenuStateContext context);
    
    /**
     * Handle the cancel/back action
     * @param context The context holding shared state data
     */
    void handleCancel(MenuStateContext context);
    
    /**
     * Handle moving selection up
     * @param context The context holding shared state data
     */
    void handleSelectUp(MenuStateContext context);
    
    /**
     * Handle moving selection down
     * @param context The context holding shared state data
     */
    void handleSelectDown(MenuStateContext context);
    
    /**
     * Render frame contents specific to this state
     * @param context The context holding shared state data
     * @param roundText Text to display for MAIN state
     */
    void renderFrameContents(MenuStateContext context, String roundText);
    
    /**
     * Update player position for this state
     * @param context The context holding shared state data
     */
    void updatePlayerPosition(MenuStateContext context);
    
    /**
     * Get the state type identifier
     * @return MenuStateType enum value
     */
    MenuStateType getStateType();
}
