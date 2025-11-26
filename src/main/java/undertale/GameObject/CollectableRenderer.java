package undertale.GameObject;

import java.util.ArrayList;

import undertale.GameObject.Collectables.Collectable;

/**
 * Legacy collectable renderer kept for reference/performance experiments.
 *
 * NOTE: Collectables now render themselves via GameObject.render() and are
 * managed by composite layers. This class is deprecated and not used by
 * ObjectManager anymore.
 */
@Deprecated
public class CollectableRenderer {
    private ArrayList<Collectable> collectables;

    public CollectableRenderer() {
        collectables = new ArrayList<>();
    }

    public void addCollectable(Collectable collectable) {
        collectables.add(collectable);
    }

    public void clearCollectables() {
        collectables.clear();
    }

    public void renderCollectables() {
        for (Collectable collectable : collectables) {
            collectable.render();
        }
    }

    public ArrayList<Collectable> getCollectables() {
        return collectables;
    }
}
