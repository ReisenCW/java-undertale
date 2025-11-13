package undertale.Enemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;

import undertale.Animation.Animation;
import undertale.Utils.SaveManager;

public class Enemy {
    private String name;
    public int maxHealth;
    private int initialHealth;
    public int currentHealth;
    private int dropGold;
    private int dropExp;
    private boolean allowRender;
    private float defenseRate;
    private boolean isDying = false;
    private float deathAlpha = 1.0f;
    private float deathAlphaSpeed = -2.0f; // 0.5秒内消失

    public boolean isYellow;

    private ArrayList<Act> acts;
    private static class AnimationEntry {
        String name;
        Animation animation;
        float left;
        float bottom;
        int priority;
        float scaler;
        float alpha;
        float targetAlpha;
        float alphaSpeed;
        boolean alphaAnimating;
        
        AnimationEntry(String name, Animation animation, float left, float bottom, int priority, float scaler) {
            this.name = name;
            this.animation = animation;
            this.left = left;
            this.bottom = bottom;
            this.priority = priority;
            this.scaler = scaler;
            this.alpha = 1.0f;
            this.targetAlpha = 1.0f;
            this.alphaSpeed = 0.0f;
            this.alphaAnimating = false;
        }
    }

    private ArrayList<AnimationEntry> animationEntries;

    public static class Act {
        private String name;
        private String description;
        private String requirement;
        private Supplier<Boolean> requirementChecker;
        private Runnable function;

        public Act(String name, String description, String requirement, Supplier<Boolean> requirementChecker, Runnable function) {
            this.name = name;
            this.description = description;
            this.requirement = requirement;
            this.requirementChecker = requirementChecker;
            this.function = function;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
        
        public String getRequirement() {
            return requirement;
        }

        public Supplier<Boolean> getRequirementChecker() {
            return requirementChecker;
        }

        public Runnable getFunction() {
            return function;
        }
    }

    Enemy(String name, int maxHealth, int currentHealth, int dropGold, int dropExp, ArrayList<String> acts, ArrayList<String> descriptions, ArrayList<String> requirements, ArrayList<Supplier<Boolean>> requirementCheckers, Runnable... actFunctions) {
        this.animationEntries = new ArrayList<>();
        this.acts = new ArrayList<>();
        if (acts != null && descriptions != null && requirements != null && actFunctions != null && acts.size() == descriptions.size() && acts.size() == requirements.size() && acts.size() == actFunctions.length) {
            for (int i = 0; i < acts.size(); i++) {
                Supplier<Boolean> checker = (requirementCheckers != null && i < requirementCheckers.size()) ? requirementCheckers.get(i) : () -> true;
                this.acts.add(new Act(acts.get(i), descriptions.get(i), requirements.get(i), checker, actFunctions[i]));
            }
        }
        this.name = name;
        this.maxHealth = maxHealth;
        this.initialHealth = currentHealth;
        this.currentHealth = currentHealth;
        this.dropGold = dropGold;
        this.dropExp = dropExp;
        this.allowRender = true;
        this.isYellow = false;
        this.defenseRate = 0.0f;
    }

    Enemy(String name, int maxHealth) {
        this(name, maxHealth, maxHealth, 0, 0, null, null, null, null);
    }

    Enemy(String name, int maxHealth, int currentHealth, int dropGold, int dropExp) {
        this(name, maxHealth, currentHealth, dropGold, dropExp, null, null, null, null);
    }

    public void update(float deltaTime) {
        for (AnimationEntry entry : animationEntries) {
            entry.animation.updateAnimation(deltaTime);
            // 处理alpha动画
            if (entry.alphaAnimating) {
                if (Math.abs(entry.alpha - entry.targetAlpha) < 0.01f) {
                    entry.alpha = entry.targetAlpha;
                    entry.alphaAnimating = false;
                } else {
                    entry.alpha += entry.alphaSpeed * deltaTime;
                    if ((entry.alphaSpeed > 0 && entry.alpha > entry.targetAlpha) ||
                        (entry.alphaSpeed < 0 && entry.alpha < entry.targetAlpha)) {
                        entry.alpha = entry.targetAlpha;
                        entry.alphaAnimating = false;
                    }
                }
            }
        }
        
        // 处理死亡动画
        if (isDying) {
            deathAlpha += deathAlphaSpeed * deltaTime;
            if (deathAlpha <= 0) {
                deathAlpha = 0;
                allowRender = false; // 完全消失后停止渲染
            }
        }
    }

    public void render() {
        if (!isAllowRender()) return;
        // 按priority升序排序，priority高的后渲染
        Collections.sort(animationEntries, Comparator.comparingInt(e -> e.priority));
        for (AnimationEntry entry : animationEntries) {
            entry.animation.renderCurrentFrame(
                entry.left,
                entry.bottom - entry.animation.getFrameHeight() * entry.scaler,
                entry.scaler, entry.scaler, 0.0f, 1.0f, 1.0f, 1.0f, entry.alpha * deathAlpha);
        }
    }

    public void heal(int healAmount) {
        currentHealth += healAmount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public void takeDamage(float damage) {
        currentHealth -= damage * (1 - defenseRate);
        if (currentHealth <= 0) {
            currentHealth = 0;
            if (!isDying) {
                isDying = true;
                // 增加success计数
                SaveManager.getInstance().incrementSuccess();
            }
        }
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

    public void setAnimationAlpha(String name, float targetAlpha, float duration) {
        for (AnimationEntry entry : animationEntries) {
            if (entry.name.equals(name)) {
                entry.targetAlpha = targetAlpha;
                entry.alphaSpeed = (targetAlpha - entry.alpha) / duration;
                entry.alphaAnimating = true;
                break;
            }
        }
    }

    public ArrayList<Act> getActs() {
        return acts;
    }

    public void addAct(String act, String description) {
        addAct(act, description, "", () -> {});
    }

    public void addAct(String act, String description, Runnable actFunction) {
        addAct(act, description, "", actFunction);
    }

    public void addAct(String act, String description, String requirement, Runnable actFunction) {
        acts.add(new Act(act, description, requirement, () -> true, actFunction));
    }

    public void addAct(String act, String description, String requirement, Supplier<Boolean> requirementChecker, Runnable actFunction) {
        acts.add(new Act(act, description, requirement, requirementChecker, actFunction));
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

    public void reset() {
        this.currentHealth = this.initialHealth;
        this.isYellow = false;
        this.allowRender = true;
        this.isDying = false;
        this.deathAlpha = 1.0f;
    }

    public void setAllowRender(boolean allow) {
        this.allowRender = allow;
    }

    public boolean isAllowRender() {
        return this.allowRender;
    }

    public void setDefenseRate(float rate) {
        if(rate > 1.0f) rate = 1.0f;
        else if(rate < 0.0f) rate = 0.0f;
        this.defenseRate = rate;
    }

    public float getDefenseRate() {
        return this.defenseRate;
    }
}