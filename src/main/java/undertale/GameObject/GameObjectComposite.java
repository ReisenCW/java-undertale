package undertale.GameObject;

/**
 * 组合节点：把多个 GameObject 聚合为一组，并将 update()/render() 代理给子节点。
 *
 * 已重构说明（Refactor note）: 该类是组合模式 (Composite) 的核心实现之一，
 * 被用作 layer/group 容器（例如 bulletsLayer / effectsLayer / collectablesLayer）。
 */
public class GameObjectComposite extends GameObject {

    public GameObjectComposite() { }

    @Override
    public void update(float deltaTime) {
        // default composite behavior: update children
        updateChildren(deltaTime);
    }

    @Override
    public void render() {
        renderChildren();
    }

    public void clearChildren() {
        // iterate over a copy to avoid ConcurrentModificationException
        for (GameObject c : new java.util.ArrayList<>(getChildren())) {
            // detach
            removeChild(c);
        }
    }
}
