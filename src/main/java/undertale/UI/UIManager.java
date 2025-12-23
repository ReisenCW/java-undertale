package undertale.UI;

import undertale.Enemy.EnemyManager;
import undertale.GameObject.Player;
import undertale.Scene.SceneManager;
import undertale.Sound.SoundManager;
import undertale.Scene.BattleFightScene;
import undertale.Texture.FontManager;
import undertale.UI.state.*;

/**
 * UI Manager - Refactored with State Pattern
 * 
 * Refactor note:
 * - Replaced enum-based state management with State Pattern
 * - Each menu state now has its own class implementing MenuState interface
 * - State transitions and behaviors are encapsulated in state classes
 * - UIManager delegates menu operations to the current state object
 * - This eliminates large switch statements and improves maintainability
 */
public class UIManager extends UIBase {
    private EnemyManager enemyManager;
    private TypeWriter menuTypeWriter;
    private BgUIManager bgUIManager;
    private AttackAnimManager attackAnimManager;
    private BattleFrameManager battleFrameManager;
    private GameOverUIManager gameOverUIManager;
    private BeginMenuManager beginMenuManager;
    private SoundManager soundManager;

    // State Pattern: Context holding current state
    private MenuStateContext stateContext;

    public UIManager(Player player, EnemyManager enemyManager, SoundManager soundManager, FontManager fontManager) {
        super();
        this.enemyManager = enemyManager;
        this.soundManager = soundManager;
        
        menuTypeWriter = new TypeWriter(fontManager);
        bgUIManager = new BgUIManager(fontManager, player);
        attackAnimManager = new AttackAnimManager(fontManager, player, enemyManager);
        battleFrameManager = new BattleFrameManager(player);
        gameOverUIManager = new GameOverUIManager(menuTypeWriter, player);
        beginMenuManager = new BeginMenuManager(fontManager);

        // Initialize state context
        stateContext = new MenuStateContext(player, enemyManager, soundManager, fontManager,
                                           menuTypeWriter, attackAnimManager);
        stateContext.setState(StateFactory.createState(MenuStateType.BEGIN));
    }

    public void resetVars(MenuStateType stateType) {
        stateContext.resetSelections();
        stateContext.resetTimeVars();
        stateContext.setState(StateFactory.createState(stateType));
        setSelected(0);
        attackAnimManager.resetStates();
    }

    public void renderBattleUI() {
        boolean allowFocus = stateContext.getCurrentStateType() != MenuStateType.SUCCESS;
        bgUIManager.renderButtons(stateContext.selectedAction, allowFocus);
        bgUIManager.renderPlayerInfo();
        bgUIManager.renderTensionBar();
        battleFrameManager.renderBattleFrame();
    }

    public void renderFrameContents(String roundText) {
        if(roundText == null) return;
        stateContext.getCurrentState().renderFrameContents(stateContext, roundText);
    }

    public void updatePlayerMenuPosition() {
        MenuStateType currentType = stateContext.getCurrentStateType();
        if(currentType == MenuStateType.FIGHT || currentType == MenuStateType.ACT || 
           currentType == MenuStateType.ITEM || currentType == MenuStateType.MERCY) {
            return;
        }
        stateContext.getCurrentState().updatePlayerPosition(stateContext);
    }

    // Delegate to current state
    public void handleMenuSelect() {
        stateContext.getCurrentState().handleSelect(stateContext);
    }

    public void handleMenuCancel() {
        stateContext.getCurrentState().handleCancel(stateContext);
    }

    public void menuSelectDown() {
        stateContext.getCurrentState().handleSelectDown(stateContext);
    }

    public void menuSelectUp() {
        stateContext.getCurrentState().handleSelectUp(stateContext);
    }

    public void makePlayerInFrame() {
        battleFrameManager.makePlayerInFrame();
    }

    public void moveBattleFrame(float deltaTime, float duration, float targetWidth, float targetHeight, float targetLeft, float targetBottom) {
        battleFrameManager.moveBattleFrame(deltaTime, duration, targetWidth, targetHeight, targetLeft, targetBottom);
    }

    public void update(float deltaTime) {
        if(stateContext.getCurrentStateType() == MenuStateType.BEGIN) {
            beginMenuManager.update(deltaTime);
            return;
        }
        if(stateContext.getCurrentStateType() == MenuStateType.FIGHT) {
            attackAnimManager.updateAttackAnim(deltaTime, enemyManager.getCurrentEnemy());
        }
        attackAnimManager.updateMissTime(deltaTime);
        if (!battleFrameManager.isFrameMoving() && !(SceneManager.getInstance().getCurrentScene() instanceof BattleFightScene)) {
            menuTypeWriter.update(deltaTime);
        }
        if(attackAnimManager.isAttackAnimFinished()) {
            if(attackAnimManager.isDamageDisplayFinished()) {
                stateContext.setState(StateFactory.createState(MenuStateType.SUCCESS));
            }
        }
    }

    public void selectMoveRight() {
        if(stateContext.getCurrentStateType() != MenuStateType.MAIN) return;
        soundManager.playSE("menu_move");
        stateContext.selectedAction = (stateContext.selectedAction + 1) % 4;
    }

    public void selectMoveLeft() {
        if(stateContext.getCurrentStateType() != MenuStateType.MAIN) return;
        soundManager.playSE("menu_move");
        stateContext.selectedAction = (stateContext.selectedAction + 3) % 4;
    }

    public void setSelected(int index){
        stateContext.selectedAction = index;
    }

    public void setMenuState(MenuStateType stateType) {
        stateContext.setState(StateFactory.createState(stateType));
    }

    public MenuStateType getMenuStateType() {
        return stateContext.getCurrentStateType();
    }

    public boolean isRenderPlayer() {
        MenuStateType type = stateContext.getCurrentStateType();
        return type != MenuStateType.ACT && type != MenuStateType.FIGHT && 
               type != MenuStateType.ITEM && type != MenuStateType.MERCY && 
               type != MenuStateType.SUCCESS;
    }

    // Game Over UI methods
    public void updateGameOver(float deltaTime) {
        gameOverUIManager.update(deltaTime);
    }

    public boolean isGameOverHeartAnimFinished() {
        return gameOverUIManager.isHeartAnimFinished();
    }

    public void renderGameOver() {
        gameOverUIManager.render();
    }

    public void resetGameOver() {
        gameOverUIManager.reset();
    }

    // Begin menu methods
    public void renderBeginMenu() {
        beginMenuManager.render();
    }

    public void beginMenuSelectUp() {
        soundManager.playSE("menu_move");
        beginMenuManager.selectUp();
    }

    public void beginMenuSelectDown() {
        soundManager.playSE("menu_move");
        beginMenuManager.selectDown();
    }

    public void handleBeginMenuSelect() {
        soundManager.playSE("confirm");
        if(beginMenuManager.confirmSelection()) {
            stateContext.setState(StateFactory.createState(MenuStateType.MAIN));
        }
    }

    public void handleGameOverConfirm() {
        if(gameOverUIManager.isMessageAllPrinted()) {
            ScreenFadeManager.getInstance().startFadeOutIn(1.5f,
                () -> SceneManager.getInstance().shouldSwitch = true,
                null
            );
        }
    }

    public void handleGameOverSkip() {
        if(!gameOverUIManager.isMessageAllPrinted()) {
            gameOverUIManager.showAllMessages();
        }
    }

    public void resetBeginMenu() {
        beginMenuManager.reset();
    }

    // Battle frame getters
    public float getFrameLeft() {
        return battleFrameManager.getFrameLeft();
    }

    public float getFrameBottom() {
        return battleFrameManager.getFrameBottom();
    }
    
    public float getFrameWidth() {
        return battleFrameManager.getFrameWidth();
    }

    public float getFrameHeight() {
        return battleFrameManager.getFrameHeight();
    }
}
