package undertale;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Player extends GameObject {
    private String name;
    private int currentHealth;
    private int maxHealth;
    private int level;
    private int attackPower;
    private int invisibleTime;
    private int flashTime;
    private float vScale;
    private float hScale;
    private float highSpeed;
    private float lowSpeed;

    private boolean isHighSpeed = true;
    private boolean isHurt = false;
    public boolean isMovable = true;

    private long hurtStartTime = 0;

    private Texture heartTexture;

    private float[] rgba;

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
        this.invisibleTime = 1500; // ms
        this.flashTime = 100; // ms 闪烁周期
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

        heartTexture = new Texture("img_heart.png");

        setPosition(Game.getWindowWidth() / 2 - heartTexture.getWidth() / 2, 
            Game.getWindowHeight() / 2 - heartTexture.getHeight() / 2);

        setSpeed(highSpeed);
    }

    @Override
    public void update(float deltaTime) {
        handleSpeedMode();
        updatePosition(deltaTime); // 按键处理在inputManager中
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

    public void setColor (float r, float g, float b, float a) {
        this.rgba[0] = r;
        this.rgba[1] = g;
        this.rgba[2] = b;
        this.rgba[3] = a;
    }

    public void render() {
        if(isHurt && ((System.currentTimeMillis() - hurtStartTime) / flashTime) % 2 == 0) {
            Texture.drawTexture(heartTexture.getId(), 
                    getX(), getY(),
                    hScale * heartTexture.getWidth(), vScale * heartTexture.getHeight(),
                    0, rgba[0]/3, rgba[1]/3, rgba[2]/3, rgba[3]);
        }
        else{
            Texture.drawTexture(heartTexture.getId(), 
                    getX(), getY(),
                    hScale * heartTexture.getWidth(), vScale * heartTexture.getHeight(),
                    0, rgba[0], rgba[1], rgba[2], rgba[3]);
        }
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

    public void setInvisibleTime(int invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    public int getFlashTime() {
        return flashTime;
    }

    public void setFlashTime(int flashTime) {
        this.flashTime = flashTime;
    }

    public void destroyTexture() {
        glDeleteTextures(heartTexture.getId());
    }
}
