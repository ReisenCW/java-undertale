package undertale;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.util.HashMap;

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

    private Item[] items;
    
    public Player(String name) {
        ConfigManager configManager = Game.getConfigManager();
        HashMap<String, String> playerMap = configManager.playerMap;
        heartTexture = Game.getTexture("heart");
        this.name = playerMap.getOrDefault("name", name);
        this.level = Integer.parseInt(playerMap.getOrDefault("level", "4"));
        this.maxHealth =  16 + 4 * this.level;
        this.currentHealth = this.maxHealth;
        this.attackPower = Integer.parseInt(playerMap.getOrDefault("attackPower", "1000"));

        this.vScale = Float.parseFloat(playerMap.getOrDefault("vScale", "3.0"));
        this.hScale = Float.parseFloat(playerMap.getOrDefault("hScale", "3.0"));

        this.highSpeed = Float.parseFloat(playerMap.getOrDefault("highSpeed", "180.0"));
        this.lowSpeed = Float.parseFloat(playerMap.getOrDefault("lowSpeed", "80.0"));
        this.speed = highSpeed;
        
        this.invisibleTime = Integer.parseInt(playerMap.getOrDefault("invisibleTime", "1500")); // ms
        this.flashTime = 100; // ms 闪烁周期
        
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        
        this.x = Game.getWindowWidth() / 2 - heartTexture.getWidth() / 2;
        this.y = Game.getWindowHeight() / 2 - heartTexture.getHeight() / 2;

        items = new Item[8];
        items[0] = new Item("Pie", "Tasty.", 99);
        items[1] = new Item("Banana", "Potassium.", 20);
        items[2] = new Item("Frisk Tea", "Tastes like hell.", 12);
        items[3] = new Item("Silk Bind", "Hornet's favorite.", 30);
        items[4] = new Item("Choco", "Too sweet.", 14);
        items[5] = new Item("Snowman Piece", "A piece of a snowman.", 30);
        items[6] = new Item("Dog Salad", "Wolf.", 12);
        items[7] = new Item("Legendary Hero", "Your ATK increased by 4.", 45);
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
                this.speed = lowSpeed;
            }
        } else {
            if (!isHighSpeed) {
                isHighSpeed = true;
                this.speed = highSpeed;
            }
        }
    }


    public void handlePlayerOutBound(float left, float right, float top, float bottom) {
        if (this.x < left) {
            this.x = left;
        } else if (this.x + hScale * heartTexture.getWidth() > right) {
            this.x = right - hScale * heartTexture.getWidth();
        }
        if (this.y < top) {
            this.y = top;
        } else if (this.y + vScale * heartTexture.getHeight() > bottom) {
            this.y = bottom - vScale * heartTexture.getHeight();
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
                    this.x, this.y,
                    hScale * heartTexture.getWidth(), vScale * heartTexture.getHeight(),
                    0, rgba[0]/3, rgba[1]/3, rgba[2]/3, rgba[3]);
        }
        else{
            Texture.drawTexture(heartTexture.getId(),
                    this.x, this.y,
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

    public void heal(int healAmount) {
        currentHealth += healAmount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
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

    public int getItemNumber() {
        return items.length;
    }

    public Item getItemByIndex(int index) {
        if (index < 0 || index >= items.length) {
            throw new IndexOutOfBoundsException("Invalid item index: " + index);
        }
        return items[index];
    }
}
