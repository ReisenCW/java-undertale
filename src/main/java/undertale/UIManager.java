package undertale;

public class UIManager {
    private static UIManager instance;

    private UIManager() {
        // 初始化UI管理器
    }

    public static UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager();
        }
        return instance;
    }

    public void renderUI() {
        // 渲染UI元素
    }
}
