package undertale.Texture;

import java.util.HashMap;

import undertale.Utils.ConfigManager;

public class TextureManager {
    private static TextureManager instance;
    private HashMap<String, Texture> textures;
    private HashMap<String, String> textureFileMap;

    static {
        instance = new TextureManager();
    }

    private TextureManager() {
        textures = new HashMap<>();
        textureFileMap = ConfigManager.getInstance().textures;
        initTextures();
    }

    private void initTextures() {
        for (String name : textureFileMap.keySet()) {
            String filePath = textureFileMap.get(name);
            loadTexture(name, filePath);
        }
    } 
    
    public static TextureManager getInstance() {
        if(instance == null) {
            synchronized (TextureManager.class) {
                if (instance == null) {
                    instance = new TextureManager();
                }
            }
        }
        return instance;
    }

    public void loadTexture(String name, String filePath, int filterType) {
        Texture texture = new Texture(filePath, filterType);
        textures.put(name, texture);
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
