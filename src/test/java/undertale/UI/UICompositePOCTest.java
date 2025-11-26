package undertale.UI;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UICompositePOCTest {

    static class TestComponent implements UIComponent {
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
    public void uiContainer_traversesChildren() {
        UIContainer root = new UIContainer();
        TestComponent a = new TestComponent();
        TestComponent b = new TestComponent();
        root.addChild(a);
        root.addChild(b);

        root.update(0.01f);
        root.render();

        assertTrue(a.updated);
        assertTrue(b.updated);
        assertTrue(a.rendered);
        assertTrue(b.rendered);
    }
}
