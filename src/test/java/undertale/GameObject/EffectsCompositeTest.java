package undertale.GameObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import undertale.GameObject.Effects.RippleEffect;
import undertale.GameObject.Effects.TitanSpawnParticle;

public class EffectsCompositeTest {

    @Test
    public void rippleAndParticle_areGameObjects_and_layered() {
        GameObjectComposite layer = new GameObjectComposite();
        RippleEffect r = new RippleEffect(100f, 200f);
        TitanSpawnParticle p = new TitanSpawnParticle(10f, 20f, 45f);

        assertTrue(r instanceof GameObject, "RippleEffect should extend GameObject");
        assertTrue(p instanceof GameObject, "TitanSpawnParticle should extend GameObject");

        layer.addChild(r);
        layer.addChild(p);

        assertEquals(2, layer.getChildren().size());

        // update should not throw (render touches Texture static state and requires full Game init)
        layer.update(0.016f);

        // clear
        layer.clearChildren();
        assertEquals(0, layer.getChildren().size());
    }
}
