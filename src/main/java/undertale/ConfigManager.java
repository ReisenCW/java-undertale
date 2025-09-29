package undertale;

import java.io.File;
import java.io.FileReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.HashMap;

public class ConfigManager {
    // 通过读取JSON文件来初始化配置
    public final boolean debug;
    public final int WINDOW_WIDTH;
    public final int WINDOW_HEIGHT;
    public final HashMap<String, String> textures;
    public final HashMap<String, String> playerMap;

    private final String CONFIG_PATH = "config.json";

    public ConfigManager() {
        boolean debugVal = false;
        int widthVal = 1280;
        int heightVal = 720;
        HashMap<String, String> texMap = new HashMap<>();
        HashMap<String, String> playerMapTmp = new HashMap<>();
        try {
            // 使用类加载器读取 resources 下的 config.json
            java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH);
            if (in != null) {
                try (java.io.InputStreamReader reader = new java.io.InputStreamReader(in)) {
                    Gson gson = new Gson();
                    JsonObject obj = gson.fromJson(reader, JsonObject.class);
                    // window
                    if (obj.has("window")) {
                        JsonObject win = obj.getAsJsonObject("window");
                        if (win.has("width")) widthVal = win.get("width").getAsInt();
                        if (win.has("height")) heightVal = win.get("height").getAsInt();
                    }
                    // player
                    if(obj.has("player")){
                        JsonObject player = obj.getAsJsonObject("player");
                        for (Map.Entry<String, JsonElement> entry : player.entrySet()) {
                            playerMapTmp.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                    // debug
                    if (obj.has("debug")) {
                        debugVal = obj.get("debug").getAsBoolean();
                    }
                    // textures
                    if (obj.has("textures")) {
                        for (JsonElement el : obj.getAsJsonArray("textures")) {
                            JsonObject tex = el.getAsJsonObject();
                            if (tex.has("key") && tex.has("path")) {
                                texMap.put(tex.get("key").getAsString(), tex.get("path").getAsString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to read config file: " + e.getMessage() + ". Use default settings.");
        }
        this.debug = debugVal;
        this.WINDOW_WIDTH = widthVal;
        this.WINDOW_HEIGHT = heightVal;
        this.textures = texMap;
        this.playerMap = playerMapTmp;
    }
}
