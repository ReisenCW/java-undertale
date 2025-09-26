package undertale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Enemy {
    private String name;
    public int maxHealth;
    public int currentHealth;
    private int dropGold;
    private int dropExp;

    private ArrayList<String> acts;

    private static class AnimationEntry {
        String name;
        Animation animation;
        float left;
        float bottom;
        int priority;
        float scaler;
        AnimationEntry(String name, Animation animation, float left, float bottom, int priority, float scaler) {
            this.name = name;
            this.animation = animation;
            this.left = left;
            this.bottom = bottom;
            this.priority = priority;
            this.scaler = scaler;
        }
    }

    private ArrayList<AnimationEntry> animationEntries;

    Enemy(String name, int maxHealth, int currentHealth, int dropGold, int dropExp, String... acts) {
        this.animationEntries = new ArrayList<>();
        this.acts = new ArrayList<>();
        this.name = name;
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
        this.dropGold = dropGold;
        this.dropExp = dropExp;
        for(String act : acts) {
            this.acts.add(act);
        }
        if(this.acts.size() == 0) {
            this.acts.add("check");
        }
    }

    Enemy(String name, int maxHealth) {
        this(name, maxHealth, maxHealth, 0, 0);
    }

    public void update(float deltaTime) {
        for (AnimationEntry entry : animationEntries) {
            entry.animation.updateAnimation(deltaTime);
        }
    }

    public void render() {
        // 按priority升序排序，priority高的后渲染
        Collections.sort(animationEntries, Comparator.comparingInt(e -> e.priority));
        for (AnimationEntry entry : animationEntries) {
            entry.animation.renderCurrentFrame(
                entry.left,
                entry.bottom - entry.animation.getFrameHeight() * entry.scaler,
                entry.scaler, entry.scaler, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public void heal(int healAmount) {
        currentHealth += healAmount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public void takeDamage(int damage) {
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public String getName() {
        return name;
    }

    public int getDropGold() {
        return dropGold;
    }

    public int getDropExp() {
        return dropExp;
    }

    public float getWidth(String key) {
        for (AnimationEntry entry : animationEntries) {
            if (entry.name.equals(key)) {
                return entry.scaler * entry.animation.getFrameWidth();
            }
        }
        return 0;
    }

    public float getHeight(String key) {
        for (AnimationEntry entry : animationEntries) {
            if (entry.name.equals(key)) {
                return entry.scaler * entry.animation.getFrameHeight();
            }
        }
        return 0;
    }

    public void addAnimation(String name, Animation animation, float left, float bottom, int priority, float scaler) {
        animationEntries.add(new AnimationEntry(name, animation, left, bottom, priority, scaler));
    }

    public ArrayList<String> getActs() {
        return acts;
    }
}