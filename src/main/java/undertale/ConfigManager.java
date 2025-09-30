package undertale;

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
    public final float BUTTON_WIDTH;
    public final float BUTTON_HEIGHT;
    public final float MENU_FRAME_WIDTH;
    public final float MENU_FRAME_HEIGHT;
    public final float MENU_FRAME_LEFT;
    public final float MENU_FRAME_BOTTOM;
    public final float BUTTON_SCALER;
    public final float BUTTON_MARGIN;
    public final float BOTTOM_OFFSET;
    public final float BATTLE_FRAME_LINE_WIDTH;
    public final HashMap<String, String> textures;
    public final HashMap<String, String> playerMap;

    private final String CONFIG_PATH = "config.json";

    public ConfigManager() {
        boolean debugVal = false;
        int widthVal = 1280;
        int heightVal = 720;
        float button_scaler = 1.6f;
        float bottom_offset = 100.0f; 
        float button_frame_line_width = 3.0f;
        float button_width = 110.0f;
        float button_height = 42.0f;
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
                    // ui
                    if (obj.has("ui")) {
                        JsonObject ui = obj.getAsJsonObject("ui");
                        if (ui.has("button_scaler")) button_scaler = ui.get("button_scaler").getAsFloat();
                        if (ui.has("bottom_offset")) bottom_offset = ui.get("bottom_offset").getAsFloat();
                        if (ui.has("button_frame_line_width")) button_frame_line_width = ui.get("button_frame_line_width").getAsFloat();
                        if (ui.has("button_width")) button_width = ui.get("button_width").getAsFloat();
                        if (ui.has("button_height")) button_height = ui.get("button_height").getAsFloat();
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
        this.BOTTOM_OFFSET = bottom_offset;
        this.BUTTON_SCALER = button_scaler;
        this.BATTLE_FRAME_LINE_WIDTH = button_frame_line_width;
        this.BUTTON_WIDTH = button_width * this.BUTTON_SCALER;
        this.BUTTON_HEIGHT = button_height * this.BUTTON_SCALER;
        this.BUTTON_MARGIN = (WINDOW_WIDTH - 4 * BUTTON_WIDTH) / 5;
        this.MENU_FRAME_WIDTH = WINDOW_WIDTH - BUTTON_MARGIN * 2;
        this.MENU_FRAME_HEIGHT = WINDOW_HEIGHT / 3;
        this.MENU_FRAME_LEFT = BUTTON_MARGIN;
        this.MENU_FRAME_BOTTOM = WINDOW_HEIGHT - BOTTOM_OFFSET - BUTTON_HEIGHT;
    }
}
