package undertale.GameObject;

/**
 * 游戏中的对象基类，包含位置、方向、速度等基本属性和方法
 * @field angle 为顺时针方向, 单位为度, 0度向右, 90度向下
 */
public abstract class GameObject {
    protected float x;
    protected float y;

    protected float selfAngle; // 单位为度
    protected boolean isNavi = false; // selfAngle是否跟随speedAngle
    protected boolean isColli = false;

    protected float speedX = 0.0f;
    protected float speedY = 0.0f;
    protected float accelerateX = 0.0f;
    protected float accelerateY = 0.0f;
    protected float maxSpeed = -1.0f;
    protected float minSpeed = -1.0f;

    public abstract void update(float deltaTime);

    public void updatePosition(float deltaTime) {
        speedX += accelerateX * deltaTime;
        speedY += accelerateY * deltaTime;
        x += speedX * deltaTime;
        y += speedY * deltaTime;
        limitSpeed();
        if(isNavi) {
            selfAngle = getSpeedAngle();
        }
    }

    private void limitSpeed() {
        if(maxSpeed == -1.0f && minSpeed == -1.0f) return;
        float currentSpeed = (float) Math.sqrt(speedX * speedX + speedY * speedY);
        if (maxSpeed != -1.0f && currentSpeed > maxSpeed) {
            speedX = speedX / currentSpeed * maxSpeed;
            speedY = speedY / currentSpeed * maxSpeed;
        }
        if (minSpeed != -1.0f && currentSpeed < minSpeed && currentSpeed > 0) {
            speedX = speedX / currentSpeed * minSpeed;
            speedY = speedY / currentSpeed * minSpeed;
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

    public void setX(float newX) {
        x = newX;
    }

    public float getY() {
        return y;
    }

    public void setY(float newY) {
        y = newY;
    }

    public float getHeight() {
        return 0;
    }

    public float getWidth() {
        return 0;
    }

    public float getSpeed() {
        return (float) Math.sqrt(speedX * speedX + speedY * speedY);
    }

    public void setSpeed(float newSpeed) {
        float current = getSpeed();
        if (current != 0) {
            speedX = speedX / current * newSpeed;
            speedY = speedY / current * newSpeed;
        } else {
            // 如果当前速度为0, 使用selfAngle作为方向
            speedX = (float) Math.cos(Math.toRadians(selfAngle)) * newSpeed;
            speedY = (float) Math.sin(Math.toRadians(selfAngle)) * newSpeed;
        }
        limitSpeed();
    }

    public float getSpeedAngle() {
        return (float) Math.toDegrees(Math.atan2(speedY, speedX));
    }

    public void setSpeedAngle(float newAngle) {
        float current = getSpeed();
        speedX = (float) Math.cos(Math.toRadians(newAngle)) * current;
        speedY = (float) Math.sin(Math.toRadians(newAngle)) * current;
        if (isNavi) {
            selfAngle = newAngle;
        }
    }

    public void setSelfAngle(float newAngle) {
        if(isNavi) {
            setSpeedAngle(newAngle);
        } else {
            selfAngle = newAngle;
        }
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
            selfAngle = getSpeedAngle();
        }
    }

    public boolean isInBound(float left, float right, float top, float bottom) {
        return x - getWidth() >= left &&
        x + getWidth() <= right &&
        y + getHeight() >= top && 
        y - getHeight() <= bottom;
    }

    public boolean checkCollisionWithPlayer(Player player) {
        return CollisionDetector.checkCircleCollision(this, player);
    }

    public void setColli(boolean colli) {
        isColli = colli;
    }

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
        if(isNavi) {
            selfAngle = getSpeedAngle();
        }
        limitSpeed();
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
        this.speedY = speedY;
        if(isNavi) {
            selfAngle = getSpeedAngle();
        }
        limitSpeed();
    }

    public float getAccelerateX() {
        return accelerateX;
    }

    public void setAccelerateX(float accelerateX) {
        this.accelerateX = accelerateX;
    }

    public float getAccelerateY() {
        return accelerateY;
    }

    public void setAccelerateY(float accelerateY) {
        this.accelerateY = accelerateY;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
        limitSpeed();
    }

    public void setMinSpeed(float minSpeed) {
        this.minSpeed = minSpeed;
        limitSpeed();
    }
}