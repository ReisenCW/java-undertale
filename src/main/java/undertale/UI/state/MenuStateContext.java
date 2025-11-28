package undertale.UI.state;

import undertale.Enemy.EnemyManager;
import undertale.GameObject.Player;
import undertale.Item.Item;
import undertale.Scene.SceneManager;
import undertale.Sound.SoundManager;
import undertale.Texture.FontManager;
import undertale.UI.TypeWriter;
import undertale.UI.AttackAnimManager;
import undertale.UI.UIBase;

/**
 * Context class holding shared state data for all menu states
 * This allows states to access and modify shared resources
 */
public class MenuStateContext extends UIBase {
    // Dependencies
    private Player player;
    private EnemyManager enemyManager;
    private FontManager fontManager;
    private SoundManager soundManager;
    private TypeWriter menuTypeWriter;
    private AttackAnimManager attackAnimManager;
    
    // Current state
    private MenuState currentState;
    
    // Selection indices
    public int selectedEnemy = 0;
    public int selectedAct = 0;
    public int selectedItem = 0;
    public int itemListFirstIndex = 0;
    public int selectedAction = -1;
    
    // Flags
    public boolean sliceSEPlayed = false;
    public boolean isBackToMain = false;
    
    // Item description for ITEM state
    public String pendingItemDescription = null;
    
    public MenuStateContext(Player player, EnemyManager enemyManager, 
                           SoundManager soundManager, FontManager fontManager,
                           TypeWriter menuTypeWriter, AttackAnimManager attackAnimManager) {
        super();
        this.player = player;
        this.enemyManager = enemyManager;
        this.soundManager = soundManager;
        this.fontManager = fontManager;
        this.menuTypeWriter = menuTypeWriter;
        this.attackAnimManager = attackAnimManager;
    }
    
    // State management
    public void setState(MenuState state) {
        this.currentState = state;
        // Clear item description when leaving ITEM state
        if (state.getStateType() != MenuStateType.ITEM) {
            pendingItemDescription = null;
        }
    }
    
    public MenuState getCurrentState() {
        return currentState;
    }
    
    public MenuStateType getCurrentStateType() {
        return currentState != null ? currentState.getStateType() : null;
    }
    
    // Getters for dependencies
    public Player getPlayer() {
        return player;
    }
    
    public EnemyManager getEnemyManager() {
        return enemyManager;
    }
    
    public FontManager getFontManager() {
        return fontManager;
    }
    
    public SoundManager getSoundManager() {
        return soundManager;
    }
    
    public TypeWriter getMenuTypeWriter() {
        return menuTypeWriter;
    }
    
    public AttackAnimManager getAttackAnimManager() {
        return attackAnimManager;
    }
    
    // Reset methods
    public void resetSelections() {
        selectedEnemy = 0;
        selectedAct = 0;
        selectedItem = 0;
        selectedAction = 0;
        itemListFirstIndex = 0;
        sliceSEPlayed = false;
        isBackToMain = false;
    }
    
    public void resetTimeVars() {
        menuTypeWriter.reset();
        attackAnimManager.resetTimeVars();
    }
}
