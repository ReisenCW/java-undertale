package undertale.GameObject;

/**
 * A concrete composite node: delegates update/render to children.
 * Used as "group" / "layer" container.
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
