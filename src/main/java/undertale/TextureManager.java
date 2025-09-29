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
        loadTexture("hp_text", "img_hp.png");
        loadTexture("test_bullet", "img_ball_bullet.png");
        
        String[] menuOptions = {"attack", "act", "item", "mercy"};
        String[] states = {"normal", "chosen"};
        for (String option : menuOptions) {
            for (String state : states) {
                String textureName = option + "_" + state;
                String fileName = "img_" + option + "_" + state + ".png";
                loadTexture(textureName, fileName);
            }
        }
        // titan
        loadTexture("titan_body", "enemy_titan/img_titan_body.png");
        loadTexture("titan_star", "enemy_titan/img_titan_star.png");
        for(int i = 0; i < 3; i++){
            String folderName = "enemy_titan/img_titan_backwing_" + i;
            for(int j = 0; j < 7; j++){
                String fileName = "spr_titan_backwing_" + j + ".png";
                loadTexture("titan_backwing_" + i + "_" + j, folderName + "/" + fileName);
            }
            folderName = "enemy_titan/img_titan_frontwing_" + i;
            for(int j = 0; j < 7; j++){
                String fileName = "spr_titan_backwing_" + j + ".png";
                loadTexture("titan_frontwing_" + i + "_" + j, folderName + "/" + fileName);
            }
        }

        // attack
        loadTexture("attack_panel", "img_attack_panel.png");
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
