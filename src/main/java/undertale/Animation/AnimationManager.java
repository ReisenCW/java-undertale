package undertale.Animation;

import java.util.HashMap;

import undertale.Texture.TextureManager;

public class AnimationManager {
    private HashMap<String, Animation> animations;
    private static AnimationManager instance;
    private TextureManager textureManager;

    static {
        instance = new AnimationManager();
    }

    private AnimationManager() {
        textureManager = TextureManager.getInstance();
        animations = new HashMap<>();
        // attack_animation
        {
            Animation attack_animation = new Animation(0.2f, false);
            for(int i = 0; i < 7; i++) {
                attack_animation.addFrame(textureManager.getTexture("slice_" + i));
            }
            animations.put("attack_animation", attack_animation);
        }

        //titan_animation
        {
            Animation bodyAnimation = new Animation(0, false, 
                textureManager.getTexture("titan_body"));
            Animation starAnimation = new Animation(0, false,
                textureManager.getTexture("titan_star"));
            // 给所有backwing和frontwing添加动画
            Animation[] backwingAnimations = new Animation[4];
            for (int i = 0; i < backwingAnimations.length - 1; i++) {
                backwingAnimations[i] = new Animation(0.2f, true,
                    textureManager.getTexture("titan_backwing_" + i + "_0"),
                    textureManager.getTexture("titan_backwing_" + i + "_1"),
                    textureManager.getTexture("titan_backwing_" + i + "_2"),
                    textureManager.getTexture("titan_backwing_" + i + "_3"),
                    textureManager.getTexture("titan_backwing_" + i + "_4"),
                    textureManager.getTexture("titan_backwing_" + i + "_5"),
                    textureManager.getTexture("titan_backwing_" + i + "_6"));
            }
            backwingAnimations[backwingAnimations.length - 1] = new Animation(0.2f, true, true, false,
                textureManager.getTexture("titan_backwing_0_0"),
                textureManager.getTexture("titan_backwing_0_1"),
                textureManager.getTexture("titan_backwing_0_2"),
                textureManager.getTexture("titan_backwing_0_3"),
                textureManager.getTexture("titan_backwing_0_4"),
                textureManager.getTexture("titan_backwing_0_5"),
                textureManager.getTexture("titan_backwing_0_6"));
            
            Animation[] frontwingAnimations = new Animation[3];
            for (int i = 0; i < frontwingAnimations.length; i++) {
                frontwingAnimations[i] = new Animation(0.2f, true,
                    textureManager.getTexture("titan_frontwing_" + i + "_0"),
                    textureManager.getTexture("titan_frontwing_" + i + "_1"),
                    textureManager.getTexture("titan_frontwing_" + i + "_2"),
                    textureManager.getTexture("titan_frontwing_" + i + "_3"),
                    textureManager.getTexture("titan_frontwing_" + i + "_4"),
                    textureManager.getTexture("titan_frontwing_" + i + "_5"),
                    textureManager.getTexture("titan_frontwing_" + i + "_6"));
            }

            // 加入HashMap
            animations.put("titan_body", bodyAnimation);
            animations.put("titan_star", starAnimation);
            for (int i = 0; i < backwingAnimations.length; i++) {
                animations.put("titan_backwing_" + i, backwingAnimations[i]);
            }
            for (int i = 0; i < frontwingAnimations.length; i++) {
                animations.put("titan_frontwing_" + i, frontwingAnimations[i]);
            }
        }

        // titan_spawn_animation
        {
            Animation titan_spawn_animation = new Animation(1.0f, true);
            for (int i = 0; i <= 7; i++) {
                titan_spawn_animation.addFrame(textureManager.getTexture("spawn_" + i));
            }
            animations.put("titan_spawn_animation", titan_spawn_animation);
        }
    }

    public static AnimationManager getInstance() {
        return instance;
    }

    public Animation getAnimation(String name) {
        return animations.get(name);
    }
}

