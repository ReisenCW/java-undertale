package undertale;

import java.util.ArrayList;

public class BattleMenuScene extends Scene {
    // 菜单选项等UI元素
    private ArrayList<String> options = new ArrayList<>() {{
        add("attack");
        add("act");
        add("item");
        add("spare");
    }};
    private int selectedIndex = 0;

    public BattleMenuScene(ObjectManager objectManager, InputManager inputManager) {
        super(objectManager, inputManager);
    }

    @Override
    public void onEnter() {
    }

    @Override
    public void onExit() {
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void render() {

    }

    @Override
    public SceneEnum getCurrentScene() {
        return SceneEnum.BATTLE_MENU;
    }
}