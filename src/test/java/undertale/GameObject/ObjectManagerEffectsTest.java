package undertale.GameObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import undertale.GameObject.Effects.RippleEffect;
import undertale.GameObject.Effects.TitanSpawnParticle;

public class ObjectManagerEffectsTest {

    @Test
    public void addAndClearTitanSpawnParticles_updatesLayerAndList() {
        ObjectManager manager = new ObjectManager();

        TitanSpawnParticle p = new TitanSpawnParticle(10f, 20f, 45f);
        manager.addTitanSpawnParticle(p);

        assertEquals(1, manager.getTitanSpawnParticlesCount());
        assertEquals(1, manager.getEffectsLayer().getChildren().size());

        manager.clearTitanSpawnParticles();

        assertEquals(0, manager.getTitanSpawnParticlesCount());
        assertEquals(0, manager.getEffectsLayer().getChildren().size());
    }

    @Test
    public void addAndClearRipples_updatesLayerAndList() {
        ObjectManager manager = new ObjectManager();

        RippleEffect r = new RippleEffect(100f, 200f);
        manager.addRippleEffect(r);

        assertEquals(1, manager.getRippleEffectsCount());
        assertEquals(1, manager.getEffectsLayer().getChildren().size());

        manager.clearRipples();

        assertEquals(0, manager.getRippleEffectsCount());
        assertEquals(0, manager.getEffectsLayer().getChildren().size());
    }
}
