package undertale;

import java.util.HashMap;

public class TextureManager {
    private static TextureManager instance;
    private HashMap<String, Texture> textures;

    private TextureManager() {
        textures = new HashMap<>();
        initTextures();
    }

    private void initTextures() {
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

    
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
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
