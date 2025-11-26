package undertale.UI;

/**
 * UIComponent: basic interface for UI nodes (leaf or composite)
 */
public interface UIComponent {
    void update(float deltaTime);
    void render();

    /**
     * Default no-op add/remove for leaf components. UIContainer will override.
     */
    default void addChild(UIComponent child) { throw new UnsupportedOperationException("Not a container"); }
    default void removeChild(UIComponent child) { throw new UnsupportedOperationException("Not a container"); }
}
