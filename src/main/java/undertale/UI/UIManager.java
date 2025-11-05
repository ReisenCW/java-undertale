package undertale.UI;

import undertale.Enemy.Enemy;
import undertale.Enemy.EnemyManager;
import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Item.Item;
import undertale.Scene.SceneManager;
import undertale.Sound.SoundManager;
import undertale.Texture.FontManager;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;

public class UIManager extends UIBase {
    public enum MenuState {
        BEGIN,
        MAIN,
        FIGHT_SELECT_ENEMY,
        FIGHT,
        ACT_SELECT_ENEMY,
        ACT_SELECT_ACT,
        ACT,
        ITEM_SELECT_ITEM,
        ITEM,
        MERCY_SELECT_ENEMY,
        MERCY_SELECT_SPARE,
        MERCY,
        SUCCESS
    }

    private static UIManager instance;

    private Player player;

    private EnemyManager enemyManager;
    private FontManager fontManager;
    private TypeWriter menuTypeWriter;
    private BgUIManager bgUIManager;
    private AttackAnimManager attackAnimManager;
    private BattleFrameManager battleFrameManager;
    private GameOverUIManager gameOverUIManager;
    private BeginMenuManager beginMenuManager;
    private SoundManager soundManager;
    private String pendingItemDescription = null;

    public MenuState menuState = MenuState.BEGIN;

    public int selectedEnemy = 0;
    public int selectedAct = 0;
    public int selectedItem = 0;
    public int itemListFirstIndex = 0;
    public int selectedAction = -1;

    public boolean sliceSEPlayed = false;

    static {
        instance = new UIManager();
    }

    private UIManager() {
        super(Game.getConfigManager());
        ConfigManager configManager = Game.getConfigManager();
        fontManager = FontManager.getInstance();
        player = Game.getPlayer();
        menuTypeWriter = new TypeWriter(configManager, fontManager);
        bgUIManager = new BgUIManager(configManager, fontManager, player);
        attackAnimManager = new AttackAnimManager(configManager, fontManager, player);
        battleFrameManager = new BattleFrameManager(configManager, player);
        gameOverUIManager = new GameOverUIManager(configManager, menuTypeWriter, player);
        beginMenuManager = new BeginMenuManager(configManager, fontManager);
        enemyManager = EnemyManager.getInstance();
        soundManager = SoundManager.getInstance();
    }

    public static UIManager getInstance() {
        return instance;
    }

    public void resetVars(MenuState state) {
        resetStates(state);
        resetTimeVars();
    }

    private void resetStates(MenuState state) {
        menuState = state;
        selectedEnemy = 0;
        selectedAct = 0;
        selectedItem = 0;
        selectedAction = 0;
        itemListFirstIndex = 0;
        sliceSEPlayed = false;
        setSelected(0);

        attackAnimManager.resetStates();
    }

    private void resetTimeVars() {
        menuTypeWriter.reset();
        attackAnimManager.resetTimeVars();
    }

    public void renderBattleUI() {
        // 渲染按钮, 玩家信息, 战斗框架
        boolean allowFocus = menuState != MenuState.SUCCESS;
        bgUIManager.renderButtons(selectedAction, allowFocus);
        bgUIManager.renderPlayerInfo();
        battleFrameManager.renderBattleFrame();
    }

    public void renderFrameContents(String roundText) {
        if(roundText == null) return;
        switch(menuState) {
            case BEGIN -> {}
            case FIGHT_SELECT_ENEMY, ACT_SELECT_ENEMY, MERCY_SELECT_ENEMY -> 
                renderEnemyList();
            case ACT_SELECT_ACT ->
                renderActList(enemyManager.getCurrentEnemy());
            case ITEM_SELECT_ITEM ->
                renderItemList();
            case MERCY_SELECT_SPARE ->
                renderMercyList();
            case ACT -> {
                Enemy enemy = enemyManager.getCurrentEnemy();
                menuTypeWriter.renderTextsInMenu(enemy.getDescriptionByIndex(selectedAct));
            }
            case ITEM -> {
                // 当进入 ITEM 状态时，消费物品的逻辑已在选择确认时执行一次，并把要显示的文本存入 pendingItemDescription
                if (pendingItemDescription != null) {
                    menuTypeWriter.renderTextsInMenu(pendingItemDescription);
                } else {
                    menuTypeWriter.renderTextsInMenu("");
                }
            }
            case FIGHT -> {
                attackAnimManager.renderFightPanel(enemyManager.getCurrentEnemy());
            }
            case MAIN ->
                menuTypeWriter.renderTextsInMenu(roundText);
            case MERCY -> {
                String text = "* You spared the enemy.";
                Enemy enemy = enemyManager.getCurrentEnemy();
                if(enemy != null){
                    if(!enemy.isYellow) {
                        text += "\n* But the enemy's name isn't yellow.";
                    }
                    else{
                        text += "\n* You won!\n* You earned 0 " + " EXP and " + enemyManager.getTotalGold(true) + " gold.";
                    }
                }
                menuTypeWriter.renderTextsInMenu(text);
            }
            case SUCCESS -> {
                String msg = "* You won!\n* You earned " + enemyManager.getTotalExp() + " EXP and " + enemyManager.getTotalGold(true) + " gold.";
                menuTypeWriter.renderTextsInMenu(msg);
            }
        }
    }

    private void renderEnemyList() {
        int enemyCnt = enemyManager.getEnemyCount();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        for (int i = 0; i < enemyCnt; i++) {
            Enemy enemy = enemyManager.getEnemy(i);
            float blue = enemy.isYellow ? 0.0f : 1.0f;
            fontManager.drawText(enemy.getName(), left, top + i * (fontManager.getFontHeight() + 20), 1.0f, 1.0f, blue, 1.0f);
        }
    }

    private void renderActList(Enemy enemy) {
        int actCnt = enemy.getActs().size();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        for (int i = 0; i < actCnt; i++) {
            String act = enemy.getActs().get(i);
            fontManager.drawText(act, left, top + i * (fontManager.getFontHeight() + 20), 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderItemList() {
        // 一页显示4个，支持滚动
        int itemCnt = player.getItemNumber();
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float left = MENU_FRAME_LEFT + 100;
        int itemsPerPage = 4;
        float infoLeft = MENU_FRAME_LEFT + MENU_FRAME_WIDTH - 200; // 右侧回血信息位置
        for (int i = 0; i < itemsPerPage; i++) {
            int idx = itemListFirstIndex + i;
            if (idx >= itemCnt) break;
            Item item = player.getItemByIndex(idx);
            float y = top + i * (fontManager.getFontHeight() + 20);
            // 高亮当前选中项
            if (idx == selectedItem) {
                fontManager.drawText("> " + item.getName(), left, y, 1.0f, 1.0f, 0.5f, 1.0f);
                // 显示回血信息
                String healInfo = "+" + item.getHealingAmount() + " HP";
                fontManager.drawText(healInfo, infoLeft, y, 1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                fontManager.drawText(item.getName(), left, y, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        // 分页指示器：竖直小方块，当前页为大实心，其余为小空心
        int totalPages = Math.max(itemCnt - 3, 1);
        int currentPage = itemListFirstIndex;
        float indicatorX = MENU_FRAME_LEFT + MENU_FRAME_WIDTH - 30; // 靠近对话框右边框左侧
        float indicatorTop = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 30;
        float gap = 40;
        for (int p = 0; p < totalPages; p++) {
            float cx = indicatorX;
            float cy = indicatorTop + p * gap;
            if (p == currentPage) {
                // 大实心方块
                Texture.drawRect(cx, cy, 14, 14, 1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                // 小空心方块
                Texture.drawHollowRect(cx + 2, cy + 2, 10, 10, 1.0f, 1.0f, 1.0f, 1.0f, 2.0f);
            }
        }
        if (itemListFirstIndex > 0) {
            fontManager.drawText("↑", left - 40, top, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        if (itemListFirstIndex + itemsPerPage < itemCnt) {
            fontManager.drawText("↓", left - 40, top + (itemsPerPage - 1) * (fontManager.getFontHeight() + 20), 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderMercyList() {
        // spare单项
        fontManager.drawText("spare", MENU_FRAME_LEFT + 100, MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void updatePlayerMenuPosition() {
        if(menuState == MenuState.FIGHT || menuState == MenuState.ACT || menuState == MenuState.ITEM || menuState == MenuState.MERCY) {
            return;
        }
        else if(menuState == MenuState.MAIN) {
            int LEFT_OFFSET = 25;
            player.setPosition(LEFT_MARGIN + BTN_MARGIN + selectedAction * (BTN_WIDTH + BTN_MARGIN) + LEFT_OFFSET - player.getWidth() / 2, BOTTOM_MARGIN - BOTTOM_OFFSET  - BTN_HEIGHT/2 - player.getHeight()/2);
        }
        else {
            int row = switch(menuState) {
                case FIGHT_SELECT_ENEMY, MERCY_SELECT_ENEMY -> selectedEnemy;
                case ACT_SELECT_ACT -> selectedAct;
                case ITEM_SELECT_ITEM -> selectedItem - itemListFirstIndex;
                case MERCY_SELECT_SPARE -> 0;
                default -> 0;
            };
            // 渲染在list
            player.setPosition(MENU_FRAME_LEFT + 60 - player.getWidth() / 2, MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 40 + row * (fontManager.getFontHeight() + 20) - player.getHeight() / 2);
        }
    }

    // 菜单“确定”操作
    public void handleMenuSelect() {
        if(menuState == MenuState.SUCCESS){
            // 使用渐暗再变亮的特效切回战斗菜单
            ScreenFadeManager.getInstance().startFadeOutIn(
                1.5f,
                () -> {
                    SceneManager.getInstance().shouldSwitch = true;
                },
                () -> {
                    resetVars(MenuState.BEGIN);
                }
            );
        }
        else if (menuState == MenuState.MAIN) {
            soundManager.playSE("confirm");
            menuState = switch(selectedAction) {
                case 0 -> {
                    menuTypeWriter.showAll();
                    yield MenuState.FIGHT_SELECT_ENEMY;
                }
                case 1 -> {
                    // 若没有act，则不进入act菜单
                    if(enemyManager.getCurrentEnemy().getActs().isEmpty()){
                        yield MenuState.MAIN;
                    }
                    else{
                        menuTypeWriter.showAll();
                        yield MenuState.ACT_SELECT_ENEMY;
                    }
                }
                case 2 -> {
                    if(player.getItemNumber() == 0){
                        yield MenuState.MAIN;
                    }
                    else{
                        menuTypeWriter.showAll();
                        yield MenuState.ITEM_SELECT_ITEM;
                    }
                }
                    case 3 -> {
                        menuTypeWriter.showAll();
                        yield MenuState.MERCY_SELECT_ENEMY;
                    }
                default -> MenuState.MAIN;
            };
        }
        else {
            menuState = switch(menuState) {
                case FIGHT_SELECT_ENEMY -> {
                    soundManager.playSE("confirm");
                    yield MenuState.FIGHT;
                }
                case ACT_SELECT_ENEMY -> {
                    soundManager.playSE("confirm");
                    yield MenuState.ACT_SELECT_ACT;
                }
                case ACT_SELECT_ACT -> {
                    // 执行act对应函数
                    soundManager.playSE("confirm");
                    Enemy enemy = enemyManager.getCurrentEnemy();
                    enemy.getActFunctions().get(selectedAct).run();
                    yield MenuState.ACT;
                }
                case ITEM_SELECT_ITEM -> {
                    soundManager.playSE("confirm");
                    // 在确认选择物品时，执行一次性消费逻辑（回血、播放音效、准备要显示的文本）
                    Item item = player.getItemByIndex(selectedItem);
                    int healAmount = item.getHealingAmount();
                    player.heal(healAmount);
                    pendingItemDescription = "* You ate the " + item.getName() + ", healed " + healAmount + " HP.\n" + item.getAdditionalDescription();
                    // 重置打字机以便从头开始显示文字
                    menuTypeWriter.reset();
                    yield MenuState.ITEM;
                }
                case MERCY_SELECT_ENEMY -> {
                    soundManager.playSE("confirm");
                    yield MenuState.MERCY_SELECT_SPARE;
                }
                case MERCY_SELECT_SPARE -> {
                    soundManager.playSE("confirm");
                    yield MenuState.MERCY;
                }
                case ACT, ITEM, MERCY -> {
                    // 若文本已经全部显示切换到FightScene
                    if (menuTypeWriter.isTypewriterAllShown()) {
                        SceneManager.getInstance().shouldSwitch = true;
                    }
                    yield menuState;
                }
                case FIGHT -> {
                    // Attack bar停止 — 开始攻击时重置相关动画以保证slice动画每次都能播放
                    if(!sliceSEPlayed){
                        soundManager.playSE("slice");
                        sliceSEPlayed = true;
                    }
                    attackAnimManager.resetSliceAnimation();
                    yield menuState;
                }
                default -> menuState;
            };
        }
    }

    // 菜单“撤销/返回”操作
    public void handleMenuCancel() {
        // X返回上一级
        switch(menuState) {
            case BEGIN, MAIN -> {}
            case FIGHT_SELECT_ENEMY,  ACT_SELECT_ENEMY, MERCY_SELECT_ENEMY, ITEM_SELECT_ITEM -> {
                soundManager.playSE("menu_move");
                menuState = MenuState.MAIN;
            }
            case ACT_SELECT_ACT -> {
                soundManager.playSE("menu_move");
                menuState = MenuState.ACT_SELECT_ENEMY;
            }
            case MERCY_SELECT_SPARE -> {
                soundManager.playSE("menu_move");
                menuState = MenuState.MERCY_SELECT_ENEMY;
            }
            case FIGHT, ACT, ITEM, MERCY, SUCCESS -> {
                // 打字机全部显示
                menuTypeWriter.showAll();
            }
        }
    }

    public void menuSelectDown() {
        // 向下选择，item支持分页滚动
        switch(menuState) {
            case BEGIN, MAIN, SUCCESS -> {}
            case FIGHT_SELECT_ENEMY, MERCY_SELECT_ENEMY, ACT_SELECT_ENEMY -> {
                if (selectedEnemy < enemyManager.getEnemyCount() - 1) {
                    soundManager.playSE("menu_move");
                    selectedEnemy++;
                    enemyManager.setCurrentEnemy(selectedEnemy);
                }
            }
            case ACT_SELECT_ACT -> {
                Enemy enemy = enemyManager.getCurrentEnemy();
                if (selectedAct < enemy.getActs().size() - 1) {
                    soundManager.playSE("menu_move");
                    selectedAct++;
                }
            }
            case ITEM_SELECT_ITEM -> {
                int itemCnt = player.getItemNumber();
                int itemsPerPage = 4;
                if (selectedItem < itemCnt - 1) {
                    soundManager.playSE("menu_move");
                    selectedItem++;
                    if (selectedItem >= itemListFirstIndex + itemsPerPage) {
                        itemListFirstIndex++;
                    }
                }
            }
            case MERCY_SELECT_SPARE -> {
                if (selectedEnemy < enemyManager.getEnemyCount() - 1) {
                    soundManager.playSE("menu_move");
                    selectedEnemy++;
                    enemyManager.setCurrentEnemy(selectedEnemy);
                }
            }
            case FIGHT, ACT, ITEM, MERCY -> {}
        }
    }

    public void menuSelectUp() {
        // 向上选择, item支持分页滚动
        switch(menuState) {
            case BEGIN, MAIN, SUCCESS -> {}
            case FIGHT_SELECT_ENEMY, MERCY_SELECT_ENEMY, ACT_SELECT_ENEMY -> {
                if (selectedEnemy > 0) {
                    soundManager.playSE("menu_move");
                    selectedEnemy--;
                    enemyManager.setCurrentEnemy(selectedEnemy);
                }
            }
            case ACT_SELECT_ACT -> {
                if (selectedAct > 0) {
                    soundManager.playSE("menu_move");
                    selectedAct--;
                    enemyManager.setCurrentEnemy(selectedEnemy);
                }
            }
            case ITEM_SELECT_ITEM -> {
                if (selectedItem > 0) {
                    soundManager.playSE("menu_move");
                    selectedItem--;
                    if (selectedItem < itemListFirstIndex) {
                        itemListFirstIndex--;
                    }
                }
            }
            case MERCY_SELECT_SPARE -> {
                if (selectedEnemy > 0) {
                    soundManager.playSE("menu_move");
                    selectedEnemy--;
                    enemyManager.setCurrentEnemy(selectedEnemy);
                }
            }
            case FIGHT, ACT, ITEM, MERCY -> {}
        }
    }

    public void makePlayerInFrame() {
        battleFrameManager.makePlayerInFrame();
    }

    public void moveBattleFrame(float deltaTime, float duration, float targetWidth, float targetHeight, float targetLeft, float targetBottom) {
        battleFrameManager.moveBattleFrame(deltaTime, duration, targetWidth, targetHeight, targetLeft, targetBottom);
    }

    public void update(float deltaTime) {
        if(menuState == MenuState.BEGIN) {
            beginMenuManager.update(deltaTime);
            return;
        }
        if(menuState == MenuState.FIGHT) {
            attackAnimManager.updateAttackAnim(deltaTime, enemyManager.getCurrentEnemy());
        }
        attackAnimManager.updateMissTime(deltaTime);
        if (!battleFrameManager.isFrameMoving()) {
            menuTypeWriter.update(deltaTime);
        }
        if(attackAnimManager.isAttackAnimFinished()) {
            if(attackAnimManager.isDamageDisplayFinished()) {
                menuState = MenuState.SUCCESS;
            }
        }
    }
    
    public void selectMoveRight() {
        if(menuState != MenuState.MAIN) return;
        soundManager.playSE("menu_move");
        selectedAction = (selectedAction + 1) % 4;
    }

    public void selectMoveLeft() {
        if(menuState != MenuState.MAIN) return;
        soundManager.playSE("menu_move");
        selectedAction = (selectedAction + 3) % 4;
    }

    public void setSelected(int index){
        selectedAction = index;
    }

    public void setMenuState(MenuState state) {
        this.menuState = state;
        // 离开 ITEM 状态时清除一次性物品显示文本，避免残留
        if (state != MenuState.ITEM) {
            pendingItemDescription = null;
        }
    }

    public MenuState getMenuState() {
        return this.menuState;
    }

    public boolean isRenderPlayer() {
        return switch (menuState) {
            case ACT, FIGHT, ITEM, MERCY, SUCCESS -> false;
            default -> true;
        };
    }

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
        menuState = MenuState.MAIN;
        beginMenuManager.confirmSelection();
    }

    public void handleGameOverConfirm() {
        if(gameOverUIManager.isMessageAllPrinted()) {
            // 使用渐暗再变亮的特效切回战斗菜单
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