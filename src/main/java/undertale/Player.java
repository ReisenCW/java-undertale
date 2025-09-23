package undertale;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Player extends GameObject {
    private String name;
    private int currentHealth;
    private int maxHealth;
    private int level;
    private int attackPower;
    private int invisibleTime = 1500; // ms
    private float vScale;
    private float hScale;
    private float highSpeed;
    private float lowSpeed;

    private boolean isHighSpeed = true;
    private boolean isHurt = false;

    private long hurtStartTime = 0;

    private Texture heartTexture;

    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.maxHealth = 16 + 4 * this.level;
        this.currentHealth = this.maxHealth;
        this.attackPower = 1000;
        this.vScale = 3.0f;
        this.hScale = 3.0f;
        this.highSpeed = 300.0f;
        this.lowSpeed = 150.0f;

        heartTexture = new Texture("img_heart.png");

        setPosition(Game.getWindowWidth() / 2 - heartTexture.getWidth() / 2, 
            Game.getWindowHeight() / 2 - heartTexture.getHeight() / 2);

        setSpeed(highSpeed);
    }

    @Override
    public void update(float deltaTime) {
        handleSpeedMode();
        handlePlayerMovement(deltaTime);
        handlePlayerOutBound(0, Game.getWindowWidth(), 0, Game.getWindowHeight());
    }

    private void handleSpeedMode() {
        // 按下shift时为低速模式
        if(Game.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || Game.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
            if (isHighSpeed) {
                isHighSpeed = false;
                setSpeed(lowSpeed);
            }
        } else {
            if (!isHighSpeed) {
                isHighSpeed = true;
                setSpeed(highSpeed);
            }
        }
    }

    private void handlePlayerMovement(float deltaTime) {
        // 通过上下左右箭头移动
        if (Game.isKeyPressed(GLFW_KEY_UP))
            setDirectionY(-1);
        else if (Game.isKeyPressed(GLFW_KEY_DOWN))
            setDirectionY(1);
        else
            setDirectionY(0);
        if (Game.isKeyPressed(GLFW_KEY_LEFT))
            setDirectionX(-1);
        else if (Game.isKeyPressed(GLFW_KEY_RIGHT))
            setDirectionX(1);
        else
            setDirectionX(0);
        updatePosition(deltaTime);
    }

    private void handlePlayerOutBound(int left, int right, int top, int bottom) {
        if (getX() < left) {
            setPositionX(left);
        } else if (getX() + hScale * heartTexture.getWidth() > right) {
            setPositionX(right - hScale * heartTexture.getWidth());
        }
        if (getY() < top) {
            setPositionY(top);
        } else if (getY() + vScale * heartTexture.getHeight() > bottom) {
            setPositionY(bottom - vScale * heartTexture.getHeight());
        }

    }

    public void render() {
        Texture.drawTexture(heartTexture.getId(), 
                            getX(), getY(), 
                            hScale * heartTexture.getWidth(), vScale * heartTexture.getHeight());
        renderInvisibility();
    }

    private void renderInvisibility(){
        // 闪烁效果
    }

    public String getName() {
        return name;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getLevel() {
        return level;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public void takeDamage(int damage) {
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public boolean isFullHealth() {
        return currentHealth == maxHealth;
    }

    public float getVScale() {
        return vScale;
    }

    public void setVScale(float vScale) {
        this.vScale = vScale;
    }

    public float getHScale() {
        return hScale;
    }

    public void setHScale(float hScale) {
        this.hScale = hScale;
    }

    public float getWidth() {
        return hScale * heartTexture.getWidth();
    }

    public float getHeight() {
        return vScale * heartTexture.getHeight();
    }

    public float getHighSpeed() {
        return highSpeed;
    }

    public void setHighSpeed(float highSpeed) {
        this.highSpeed = highSpeed;
    }

    public float getLowSpeed() {
        return lowSpeed;
    }

    public void setLowSpeed(float lowSpeed) {
        this.lowSpeed = lowSpeed;
    }

    public boolean isHighSpeed() {
        return isHighSpeed;
    }

    public boolean isHurt() {
        return isHurt;
    }

    public void setHurt(boolean isHurt) {
        this.isHurt = isHurt;
        if (isHurt) {
            hurtStartTime = System.currentTimeMillis();
        }
    }

    public int getInvisibleTime() {
        return invisibleTime;
    }

    public void destroyTexture() {
        glDeleteTextures(heartTexture.getId());
    }
}
