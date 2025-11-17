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
        float dx = obj1.getX() + obj1.getWidth() / 2.0f - (obj2.getX() + obj2.getWidth() / 2.0f);
        float dy = obj1.getY() + obj1.getHeight() / 2.0f - (obj2.getY() + obj2.getHeight() / 2.0f);
        return dx * dx + dy * dy;
    }
}
