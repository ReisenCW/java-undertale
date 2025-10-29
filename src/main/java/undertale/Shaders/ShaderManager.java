package undertale.Shaders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import undertale.GameMain.Game;
import undertale.Utils.ConfigManager;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * 负责从 resources 中加载以单文件形式存放的 shader(使用 @shader vertex / @shader fragment 标记)，
 * 将其编译并缓存为 GL Program，按 config.json 中的 "shaders" 键进行初始化。
 */
public class ShaderManager {
    private static ShaderManager instance;
    static {
        instance = new ShaderManager();
    }

    private final Map<String, Integer> programs = new HashMap<>();

    private ShaderManager() {
        ConfigManager configManager = Game.getConfigManager();
        if (configManager != null && configManager.shaders != null) {
            for (Map.Entry<String, String> e : configManager.shaders.entrySet()) {
                String key = e.getKey();
                String path = e.getValue();
                try {
                    int prog = loadAndCompileShader(path);
                    if (prog != 0) {
                        programs.put(key, prog);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to load shader '" + key + "' from '" + path + "': " + ex.getMessage());
                }
            }
        }
    }

    /**
     * 获取已编译好的 program id；找不到返回 0。
     */
    public int getProgram(String key) {
        return programs.getOrDefault(key, 0);
    }

    /**
     * 释放所有程序
     */
    public void dispose() {
        for (int prog : programs.values()) {
            if (prog != 0) glDeleteProgram(prog);
        }
        programs.clear();
    }

    /**
     * 从资源文件加载并编译 shader 程序
     * @param shaderPath shader 文件路径
     * @return shader program id
     */
    private int loadAndCompileShader(String shaderPath) {
        // shader 文件包含两部分，以 @shader vertex / @shader fragment 标记
        InputStream in = getClass().getClassLoader().getResourceAsStream(shaderPath);
        if (in == null) {
            throw new RuntimeException("Shader resource not found: " + shaderPath);
        }

        StringBuilder vertex = new StringBuilder();
        StringBuilder fragment = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            String current = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("@shader")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        if (parts[1].equalsIgnoreCase("vertex")) current = "vertex";
                        else if (parts[1].equalsIgnoreCase("fragment")) current = "fragment";
                        else current = null;
                    } else {
                        current = null;
                    }
                    continue;
                }

                if ("vertex".equals(current)) vertex.append(line).append('\n');
                else if ("fragment".equals(current)) fragment.append(line).append('\n');
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read shader file: " + e.getMessage(), e);
        }

        String vSrc = vertex.toString();
        String fSrc = fragment.toString();

        if (vSrc.isEmpty() || fSrc.isEmpty()) {
            throw new RuntimeException("Shader file must contain both vertex and fragment sections: " + shaderPath);
        }

        // compile
        // vertex shader
        int vShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vShader, vSrc);
        glCompileShader(vShader);
        // compile 错误信息
        if (glGetShaderi(vShader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(vShader);
            glDeleteShader(vShader);
            throw new RuntimeException("Vertex shader compile error: " + log);
        }

        // fragment shader
        int fShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fShader, fSrc);
        glCompileShader(fShader);
        if (glGetShaderi(fShader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(fShader);
            glDeleteShader(vShader);
            glDeleteShader(fShader);
            throw new RuntimeException("Fragment shader compile error: " + log);
        }

        // link shader program
        int prog = glCreateProgram();
        glAttachShader(prog, vShader);
        glAttachShader(prog, fShader);
        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(prog);
            glDeleteShader(vShader);
            glDeleteShader(fShader);
            glDeleteProgram(prog);
            throw new RuntimeException("Shader program link error: " + log);
        }

        // 成功获得program后可清除shader
        glDeleteShader(vShader);
        glDeleteShader(fShader);

        return prog;
    }

    public static ShaderManager getInstance() {
        return instance;
    }
}
