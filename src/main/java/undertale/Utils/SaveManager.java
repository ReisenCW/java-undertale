package undertale.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveManager {
    private static SaveManager instance;
    private int successCount = 0;
    private final String SAVE_PATH = "save/save.data";
    private Gson gson = new Gson();

    static {
        instance = new SaveManager();
    }

    private SaveManager() {
        load();
    }

    public static SaveManager getInstance() {
        if(instance == null) {
            synchronized(SaveManager.class) {
                if(instance == null) {
                    instance = new SaveManager();
                }
            }
        }
        return instance;
    }

    public void incrementSuccess() {
        successCount++;
        save();
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void reset() {
        successCount = 0;
        save();
    }

    private void load() {
        try {
            if (Files.exists(Paths.get(SAVE_PATH))) {
                FileReader reader = new FileReader(SAVE_PATH);
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                reader.close();
                if (json.has("success")) {
                    successCount = json.get("success").getAsInt();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            // 确保 save 文件夹存在
            Files.createDirectories(Paths.get("save"));
            JsonObject json = new JsonObject();
            json.addProperty("success", successCount);
            FileWriter writer = new FileWriter(SAVE_PATH);
            gson.toJson(json, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}