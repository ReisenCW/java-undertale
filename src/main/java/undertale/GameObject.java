package undertale;

/**
 * 游戏中的对象基类，包含位置、方向、速度等基本属性和方法
 * @ direction向下为正，向右为正
 * @ angle为顺时针方向，0度向右，90度向下
 */
public abstract class GameObject {
    private float x;
    private float y;
    private float[] direction = {0.0f, 0.0f};
    private float speed;
    private float speedAngle; // 单位为度
    private float selfAngle; // 单位为度
    private boolean isNavi = false; // selfAngle是否跟随speedAngle

    public abstract void update(float deltaTime);

    public void updatePosition(float deltaTime) {
        float len = (float) Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        if (len != 0) {
            x += direction[0] * speed / len * deltaTime;
            y += direction[1] * speed / len * deltaTime;
        }
    }

    public void setPosition(float newX, float newY) {
        x = newX;
        y = newY;
    }

    public void setPositionX(float newX) {
        x = newX;
    }

    public void setPositionY(float newY) {
        y = newY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setDirection(float dirX, float dirY) {
        direction[0] = dirX;
        direction[1] = dirY;
        float len = (float) Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        if (len != 0) {
            direction[0] /= len;
            direction[1] /= len;
        }
        speedAngle = (float) Math.toDegrees(Math.atan2(direction[1], direction[0]));
        if (isNavi) {
            selfAngle = speedAngle;
        }
    }

    public void setDirectionX(float dirX) {
        direction[0] = dirX;
        float len = (float) Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        if (len != 0) {
            direction[0] /= len;
            direction[1] /= len;
        }
        speedAngle = (float) Math.toDegrees(Math.atan2(direction[1], direction[0]));
        if (isNavi) {
            selfAngle = speedAngle;
        }
    }

    public void setDirectionY(float dirY) {
        direction[1] = dirY;
        float len = (float) Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        if (len != 0) {
            direction[0] /= len;
            direction[1] /= len;
        }
        speedAngle = (float) Math.toDegrees(Math.atan2(direction[1], direction[0]));
        if (isNavi) {
            selfAngle = speedAngle;
        }
    }

    public float[] getDirection() {
        return direction;
    }

    public float getSpeed() {
        return speed;
    }

    public float getSpeedX() {
        float len = (float) Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        if (len == 0) return 0;
        return direction[0] * speed / len;
    }

    public float getSpeedY() {
        float len = (float) Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        if (len == 0) return 0;
        return direction[1] * speed / len;
    }

    public void setSpeed(float newSpeed) {
        speed = newSpeed;
    }

    public float getAngle() {
        return speedAngle;
    }

    public void setAngle(float newAngle) {
        speedAngle = newAngle;
        if (isNavi) {
            selfAngle = speedAngle;
        }
        direction[0] = (float) Math.cos(Math.toRadians(speedAngle));
        direction[1] = (float) Math.sin(Math.toRadians(speedAngle));
    }

    public void setSelfAngle(float newAngle) {
        if(isNavi) return;
        selfAngle = newAngle;
    }

    public float getSelfAngle() {
        return selfAngle;
    }

    public boolean isNavi() {
        return isNavi;
    }

    public void setNavi(boolean navi) {
        isNavi = navi;
        if (isNavi) {
            selfAngle = speedAngle;
        }
    }
}
