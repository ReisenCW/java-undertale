package undertale.GameObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import undertale.GameObject.Bullets.Bullet;

public class ObjectManagerBulletsTest {

    @Test
    public void pendingAdd_then_updateFightScene_movesToLayerAndList() {
        ObjectManager manager = new ObjectManager();

        // create a bullet without textures (null) so tests do not trigger Texture static init
        Bullet b = new Bullet(1f, 2f, 0f, 0f, 0f, 1, (undertale.Texture.Texture) null);
        manager.addBullet(b);

        // no bullets until update processes pending
        assertEquals(0, manager.getBulletsCount());
        assertEquals(0, manager.getBulletsLayer().getChildren().size());

        // process pending bullets (player is null in this ObjectManager so update will skip player checks)
        manager.updateFightScene(0.016f);

        assertEquals(1, manager.getBulletsCount());
        // bullets layer should contain the bullet
        assertEquals(1, manager.getBulletsLayer().getChildren().size());
        // check bullets list size
        assertEquals(1, manager.getBulletsCount());

        // clear bullets should remove from both list and layer
        manager.clearBullets();
        assertEquals(0, manager.getBulletsCount());
        assertEquals(0, manager.getBulletsLayer().getChildren().size());
    }
}
