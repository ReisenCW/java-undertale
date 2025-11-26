package undertale.GameObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CompositePOCTest {

    static class TestLeaf extends GameObject {
        boolean updated = false;
        boolean rendered = false;

        @Override
        public void update(float deltaTime) {
            updated = true;
        }

        @Override
        public void render() {
            rendered = true;
        }
    }

    @Test
    public void compositeUpdateRender_traversesChildren() {
        GameObjectComposite root = new GameObjectComposite();
        TestLeaf a = new TestLeaf();
        TestLeaf b = new TestLeaf();

        root.addChild(a);
        root.addChild(b);

        root.update(0.016f);
        root.render();

        assertTrue(a.updated, "child a should be updated");
        assertTrue(b.updated, "child b should be updated");
        assertTrue(a.rendered, "child a should be rendered");
        assertTrue(b.rendered, "child b should be rendered");
    }

    @Test
    public void addRemoveChild_worksCorrectly() {
        GameObjectComposite root = new GameObjectComposite();
        TestLeaf a = new TestLeaf();
        root.addChild(a);
        assertTrue(root.hasChildren());
        assertEquals(1, root.getChildren().size());

        root.removeChild(a);
        assertFalse(root.hasChildren());
        assertEquals(0, root.getChildren().size());
        assertNull(a.getParent());
    }

    @Test
    public void clearChildren_doesNotThrow_and_detachesChildren() {
        GameObjectComposite root = new GameObjectComposite();
        TestLeaf a = new TestLeaf();
        TestLeaf b = new TestLeaf();

        root.addChild(a);
        root.addChild(b);

        // should not throw ConcurrentModificationException
        root.clearChildren();

        assertFalse(root.hasChildren(), "root should have no children after clearChildren");
        assertNull(a.getParent(), "child a parent must be null after clearChildren");
        assertNull(b.getParent(), "child b parent must be null after clearChildren");
    }
}
