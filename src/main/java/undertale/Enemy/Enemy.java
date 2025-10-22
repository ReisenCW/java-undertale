package undertale.Enemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import undertale.Animation.Animation;

public class Enemy {
    private String name;
    public int maxHealth;
    public int currentHealth;
    private int dropGold;
    private int dropExp;

    public boolean isYellow;

    private ArrayList<String> acts;
    private ArrayList<String> descriptions;
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

    Enemy(String name, int maxHealth, int currentHealth, int dropGold, int dropExp, ArrayList<String> acts, ArrayList<String> descriptions) {
        this.animationEntries = new ArrayList<>();
        this.acts = acts;
        this.descriptions = descriptions;
        this.name = name;
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
        this.dropGold = dropGold;
        this.dropExp = dropExp;
        this.isYellow = false;
        if (acts != null && !acts.isEmpty() && descriptions != null && !descriptions.isEmpty()) {
            this.acts = acts;
        } else {
            this.acts = new ArrayList<>();
            this.descriptions = new ArrayList<>();
        }
    }

    Enemy(String name, int maxHealth) {
        this(name, maxHealth, maxHealth, 0, 0, null, null);
    }

    Enemy(String name, int maxHealth, int currentHealth, int dropGold, int dropExp) {
        this(name, maxHealth, currentHealth, dropGold, dropExp, null, null);
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

    public void takeDamage(float damage) {
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

    public ArrayList<String> getDescriptions() {
        return descriptions;
    }

    public String getActByIndex(int index) {
        if (index >= 0 && index < acts.size()) {
            return acts.get(index);
        }
        return null;
    }

    public String getDescriptionByIndex(int index) {
        if (index >= 0 && index < descriptions.size()) {
            return descriptions.get(index);
        }
        return null;
    }

    public void addAct(String act, String description) {
        acts.add(act);
        descriptions.add(description);
    }

    public String getActDescription(String act) {
        int index = acts.indexOf(act);
        if (index != -1 && index < descriptions.size()) {
            return descriptions.get(index);
        }
        return null;
    }

    public AnimationEntry getAnimationEntry(String name) {
        for (AnimationEntry entry : animationEntries) {
            if (entry.name.equals(name)) {
                return entry;
            }
        }
        return null;
    }

    public ArrayList<AnimationEntry> getAnimationEntries() {
        return animationEntries;
    }

    public float getEntryLeft(String name) {
        for (AnimationEntry entry : animationEntries) {
            if (entry.name.equals(name)) {
                return entry.left;
            }
        }
        return 0;
    }

    public float getEntryBottom(String name) {
        for (AnimationEntry entry : animationEntries) {
            if (entry.name.equals(name)) {
                return entry.bottom;
            }
        }
        return 0;
    }
}