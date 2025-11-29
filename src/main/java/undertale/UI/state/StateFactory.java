package undertale.UI.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating menu state instances
 * Uses singleton pattern to cache state instances
 */
public class StateFactory {
    private static final Map<MenuStateType, MenuState> stateCache = new HashMap<>();
    
    static {
        // Pre-create all states
        stateCache.put(MenuStateType.BEGIN, new BeginMenuState());
        stateCache.put(MenuStateType.MAIN, new MainMenuState());
        stateCache.put(MenuStateType.FIGHT_SELECT_ENEMY, new FightSelectEnemyState());
        stateCache.put(MenuStateType.FIGHT, new FightState());
        stateCache.put(MenuStateType.ACT_SELECT_ENEMY, new ActSelectEnemyState());
        stateCache.put(MenuStateType.ACT_SELECT_ACT, new ActSelectActState());
        stateCache.put(MenuStateType.ACT, new ActState());
        stateCache.put(MenuStateType.ITEM_SELECT_ITEM, new ItemSelectItemState());
        stateCache.put(MenuStateType.ITEM, new ItemState());
        stateCache.put(MenuStateType.MERCY_SELECT_ENEMY, new MercySelectEnemyState());
        stateCache.put(MenuStateType.MERCY_SELECT_SPARE, new MercySelectSpareState());
        stateCache.put(MenuStateType.MERCY, new MercyState());
        stateCache.put(MenuStateType.SUCCESS, new SuccessState());
    }
    
    /**
     * Get a state instance by type
     * @param type The type of state to create
     * @return The state instance
     */
    public static MenuState createState(MenuStateType type) {
        MenuState state = stateCache.get(type);
        if (state == null) {
            throw new IllegalArgumentException("Unknown state type: " + type);
        }
        return state;
    }
}
