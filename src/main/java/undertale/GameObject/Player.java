package undertale.GameObject;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.util.HashMap;

import undertale.GameMain.Game;
import undertale.Item.Item;
import undertale.Sound.SoundManager;
import undertale.Texture.Texture;
import undertale.Utils.ConfigManager;
import undertale.Utils.SaveManager;

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
    private int tensionPoints;
    private int baseTakenDamage;

    public enum LightLevel {
        NORMAL,
        ENHANCED
    }
    //光圈半径
    private float currentLightRadius;
    private float enhancedLightRadius;
    private float normalLightRadius;
    // 光圈动画相关
    private float startLightRadius = 0f;
    private float targetLightRadius = 0f;
    private boolean lightExpanding = false;
    private float lightExpandingDuration = 1.0f; // seconds
    private float lightElapsed = 0f; // seconds
    private float lightOscTime = 0f; // seconds
    private float lightOscSpeed = 6.0f; // 震动速度
    private float lightOscAmplitude = 1.2f; // 震动幅度

    private boolean isHighSpeed = true;
    private boolean isHurt = false;
    public boolean isMovable = true;

    private long hurtStartTime = 0;

    private Texture heartTexture;

    private float[] rgba;
    private Item[] items;

    private SoundManager soundManager;
    
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
        this.tensionPoints = 80;

        this.enhancedLightRadius = Float.parseFloat(playerMap.getOrDefault("enhancedLightRadius", "150.0"));
        this.normalLightRadius = Float.parseFloat(playerMap.getOrDefault("normalLightRadius", "100.0"));
        this.currentLightRadius = normalLightRadius;
        this.targetLightRadius = currentLightRadius;
        
        this.invisibleTime = Integer.parseInt(playerMap.getOrDefault("invisibleTime", "1500")); // ms
        this.flashTime = 100; // ms 闪烁周期
        
        this.rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        
        this.x = Game.getWindowWidth() / 2 - heartTexture.getWidth() / 2;
        this.y = Game.getWindowHeight() / 2 - heartTexture.getHeight() / 2;

        items = new Item[8];
        items[0] = new Item("Pie", "* Tasty.", 99);
        items[1] = new Item("Banana", "* Potassium.", 20);
        items[2] = new Item("Frisk Tea", "* Tastes like hell.", 12);
        items[3] = new Item("Silk Bind", "* Hornet's favorite.", 30);
        items[4] = new Item("Choco", "* Too sweet.", 14);
        items[5] = new Item("Snowman Piece", "* A piece of a snowman.", 30);
        items[6] = new Item("Dog Salad", "* Wolf.", 12);
        items[7] = new Item("Legendary Hero", "* Your ATK increased by 4.", 45);

        soundManager = SoundManager.getInstance();
        SaveManager saveManager = SaveManager.getInstance();
        baseTakenDamage = saveManager.getSuccessCount() * 5;
    }

    int m = 1;

    @Override
    public void update(float deltaTime) {
        handleSpeedMode();
        updatePosition(deltaTime); // 按键处理在inputManager中
        updateLight(deltaTime);
        handlePlayerOutBound(0, Game.getWindowWidth(), 0, Game.getWindowHeight());
    }

    // 回合开始时调用, 开始光圈扩展动画
    public void startLightExpansion() {
        this.lightElapsed = 0f;
        this.startLightRadius = 0.0f;
        this.currentLightRadius = this.startLightRadius;
        this.lightExpanding = true;
        this.lightOscTime = 0f;
    }

    private void updateLight(float deltaTime) {
        if (lightExpanding) {
            // 光圈扩展动画
            lightElapsed += deltaTime;
            float t = Math.min(1.0f, lightElapsed / lightExpandingDuration);
            // sin,越来越慢
            float ease = (float)Math.sin(t * Math.PI / 2.0);
            currentLightRadius = startLightRadius + (targetLightRadius - startLightRadius) * ease;
            if (t >= 1.0f) {
                lightExpanding = false;
            }
        }
        // 光圈震动
        lightOscTime += deltaTime;
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
        // 多偏移绘制黑色轮廓（outline），再绘制主纹理
        float w = hScale * heartTexture.getWidth();
        float h = vScale * heartTexture.getHeight();
        // outline 偏移量（以屏幕像素为单位），可调整为 1 或 2
        float thickness = 1.0f;
        float outlineAlpha = 1.0f * rgba[3];

        // 扩大到 5x5 偏移（排除中心点）以得到更粗的轮廓
        for (int ox = -2; ox <= 2; ox++) {
            for (int oy = -2; oy <= 2; oy++) {
                if (ox == 0 && oy == 0) continue;
                Texture.drawTexture(heartTexture.getId(),
                    this.x + ox * thickness, this.y + oy * thickness,
                    w, h,
                    0, 0f, 0f, 0f, outlineAlpha);
            }
        }

        // 主贴图（保留受伤闪烁色调）
        if(isHurt && ((System.currentTimeMillis() - hurtStartTime) / flashTime) % 2 == 0) {
            Texture.drawTexture(heartTexture.getId(),
                    this.x, this.y,
                    w, h,
                    0, rgba[0]/3, rgba[1]/3, rgba[2]/3, rgba[3]);
        } else{
            Texture.drawTexture(heartTexture.getId(),
                    this.x, this.y,
                    w, h,
                    0, rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }

    public void renderLight() {
        float cx = this.x + this.getWidth() / 2.0f;
        float cy = this.y + this.getHeight() / 2.0f;

        int rings = 6;

        float maxAlpha = 0.5f;
        // 每个圈的基础alpha, 内圈由所有圈的alpha叠加而成
        float baseAlpha = maxAlpha / rings;

        for (int i = 0; i < rings; i++) {
            float frac = (i + 1) / (float) rings; // 0..1
            // 利用sqrt使小i(内圈)变化快, 大i(外圈)变化慢, 即内圈稀疏, 外圈密集
            float radius = currentLightRadius * (float)Math.sqrt(frac);

            // 光圈振荡偏移 (所有环振幅相同, 但相位不同)
            float phase = i * 0.35f;
            float osc = (float)Math.sin(lightOscTime * lightOscSpeed + phase) * lightOscAmplitude;

            float drawRadius = radius + osc;

            Texture.drawCircle(cx, cy, drawRadius, 1.0f, 1.0f, 1.0f, baseAlpha);
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
        soundManager.playSE("heal");
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
        currentHealth -= (damage + baseTakenDamage);
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
        // 调整y以保持中心位置不变
        this.y -= (vScale - this.vScale) * getHeight() / 2.0f;
        this.vScale = vScale;
    }

    public float getHScale() {
        return hScale;
    }

    public void setHScale(float hScale) {
        // 调整x以保持中心位置不变
        this.x -= (hScale - this.hScale) * getWidth() / 2.0f;
        this.hScale = hScale;
    }

    @Override
    public float getWidth() {
        return hScale * heartTexture.getWidth();
    }

    @Override
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
        int count = 0;
        for (Item item : items) {
            if (item != null) {
                count++;
            }
        }
        return count;
    }

    public Item getItemByIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Invalid item index: " + index);
        }
        int count = 0;
        for (Item item : items) {
            if (item != null) {
                if (count == index) {
                    return item;
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException("Invalid item index: " + index);
    }

    public void removeItemByIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Invalid item index: " + index);
        }
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                if (count == index) {
                    items[i] = null;
                    return;
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException("Invalid item index: " + index);
    }

    public float getCurrentLightRadius() {
        return currentLightRadius;
    }

    public float getLightOscAmplitude() {
        return lightOscAmplitude;
    }

    public void setCurrentLightRadius(LightLevel level) {
        switch (level) {
            case NORMAL:
                this.currentLightRadius = normalLightRadius;
                break;
            case ENHANCED:
                this.currentLightRadius = enhancedLightRadius;
                break;
            default:
                this.currentLightRadius = normalLightRadius;
                break;
        }
    }

    public void setTargetLightRadius(LightLevel level) {
        switch (level) {
            case NORMAL:
                this.targetLightRadius = normalLightRadius;
                break;
            case ENHANCED:
                this.targetLightRadius = enhancedLightRadius;
                break;
            default:
                this.targetLightRadius = normalLightRadius;
                break;
        }
    }

    public void reset() {
        this.currentHealth = this.maxHealth;
        this.isHurt = false;
        this.isMovable = true;
        this.tensionPoints = 80;
        this.x = Game.getWindowWidth() / 2 - heartTexture.getWidth() / 2;
        this.y = Game.getWindowHeight() / 2 - heartTexture.getHeight() / 2;
        this.setCurrentLightRadius(LightLevel.NORMAL);
        this.setTargetLightRadius(LightLevel.NORMAL);
    }

    public void setTensionPoints(int points) {
        this.tensionPoints = points;
        if (this.tensionPoints < 0) {
            this.tensionPoints = 0;
        } else if(tensionPoints > 100) {
            this.tensionPoints = 100;
        }
    }

    public int getTensionPoints() {
        return tensionPoints;
    }

    public void updateTensionPoints(int delta) {
        setTensionPoints(this.tensionPoints + delta);
    }
}
