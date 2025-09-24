package undertale;

import java.util.HashMap;

public class TextureManager {
    private HashMap<String, Texture> textures;

    public TextureManager() {
        textures = new HashMap<>();
        init();
    }

    private void init() {
        loadTexture("heart", "img_heart.png");
        loadTexture("test_bullet", "img_ball_bullet.png");
        
        loadTexture("attack_normal", "img_fight_normal.png");
        loadTexture("attack_chosen", "img_fight_chosen.png");
        loadTexture("act_normal", "img_act_normal.png");
        loadTexture("act_chosen", "img_act_chosen.png");
        loadTexture("item_normal", "img_item_normal.png");
        loadTexture("item_chosen", "img_item_chosen.png");
        loadTexture("mercy_normal", "img_mercy_normal.png");
        loadTexture("mercy_chosen", "img_mercy_chosen.png");
    } 

    public void loadTexture(String name, String filePath) {
        Texture texture = new Texture(filePath);
        textures.put(name, texture);
    }

    public Texture getTexture(String name) {
        return textures.get(name);
    }

    public void unloadTexture(String name) {
        Texture texture = textures.remove(name);
        if (texture != null) {
            texture.destroy();
        }
    }

    public void destroyAll() {
        for (Texture texture : textures.values()) {
            texture.destroy();
        }
        textures.clear();
    }
}
