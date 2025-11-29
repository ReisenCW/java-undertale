package undertale.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UIContainer - concrete composite for UI components. Extends UIBase to reuse constants.
 *
 * 已重构说明（Refactor note）: UI 子系统支持组合模式（UIComponent / UIContainer），
 * 可以把各个 UI 管理模块作为子组件加入容器，整体遍历/渲染。
 */
public class UIContainer implements UIComponent {
    private final List<UIComponent> children = new ArrayList<>();

    public UIContainer() { }

    @Override
    public void update(float deltaTime) {
        for (UIComponent c : new ArrayList<>(children)) c.update(deltaTime);
    }

    @Override
    public void render() {
        for (UIComponent c : new ArrayList<>(children)) c.render();
    }

    @Override
    public void addChild(UIComponent child) {
        if (child == null) return;
        children.add(child);
    }

    @Override
    public void removeChild(UIComponent child) {
        children.remove(child);
    }

    public List<UIComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
