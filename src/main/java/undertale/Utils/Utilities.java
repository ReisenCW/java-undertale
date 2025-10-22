package undertale.Utils;

public class Utilities {
    public static Number getChangeStep(Number from, Number to, float deltaTime, float duration) {
        if (duration <= 0) {
            return to;
        }
        float change = to.floatValue() - from.floatValue();
        float step = change * (deltaTime / duration);

        return step;
    }
}
