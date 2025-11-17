package undertale.GameObject;

import undertale.Utils.GameUtilities;

public class CollisionDetector {
    // 必须为player / bullet
    public static boolean checkRectCollision(GameObject obj1, GameObject obj2, float padding) {
        return obj1.getX() < obj2.getX() + obj2.getWidth() + padding && // 1左和2右
               obj1.getX() + obj1.getWidth() + padding > obj2.getX() && // 1右和2左
               obj1.getY() < obj2.getY() + obj2.getHeight() + padding && // 1上和2下
               obj1.getY() + obj1.getHeight() + padding > obj2.getY();    // 1下和2上
    }

    public static boolean checkRectCollision(GameObject obj1, GameObject obj2) {
        return checkRectCollision(obj1, obj2, 0f);
    }

    /**
     * 检测两个圆形物体的碰撞
     * @param obj1
     * @param obj2
     * @param padding : 额外的碰撞距离，正值表示增加碰撞范围，负值表示减少碰撞范围
     * @return
     */
    public static boolean checkCircleCollision(GameObject obj1, GameObject obj2, float padding) {
        float distance = (float) Math.sqrt(GameUtilities.getDistSquared(obj1, obj2));
        return distance < (obj1.getWidth() / 2 + obj2.getWidth() / 2 - padding);
    }

    public static boolean checkCircleCollision(GameObject obj1, GameObject obj2) {
        return checkCircleCollision(obj1, obj2, 0f);
    }

    public static boolean checkRectCircleCollision(GameObject rectObj, GameObject circleObj, float padding) {
        float circleX = circleObj.getX() + circleObj.getWidth() / 2;
        float circleY = circleObj.getY() + circleObj.getHeight() / 2;

        // 最近点要么是矩形的某个顶点，要么是某条边上的垂足
        float closestX = clamp(circleX, rectObj.getX(), rectObj.getX() + rectObj.getWidth());
        float closestY = clamp(circleY, rectObj.getY(), rectObj.getY() + rectObj.getHeight());

        float dx = circleX - closestX;
        float dy = circleY - closestY;

        return (dx * dx + dy * dy) < (circleObj.getWidth() / 2 * circleObj.getWidth() / 2 + padding);
    }

    public static boolean checkRectCircleCollision(GameObject rectObj, GameObject circleObj) {
        return checkRectCircleCollision(rectObj, circleObj, 0f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
