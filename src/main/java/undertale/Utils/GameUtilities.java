package undertale.Utils;

import undertale.GameObject.GameObject;

public class GameUtilities {
    public static Number getChangeStep(Number from, Number to, float deltaTime, float duration) {
        if (duration <= 0) {
            return to;
        }
        float change = to.floatValue() - from.floatValue();
        float step = change * (deltaTime / duration);

        return step;
    }

    public static float getDistSquared(GameObject obj1, GameObject obj2) {
        float dx = obj1.getX() - obj2.getX();
        float dy = obj1.getY() - obj2.getY();
        return dx * dx + dy * dy;
    }
}
