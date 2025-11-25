package undertale.Shaders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import undertale.Utils.ConfigManager;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * 负责从 resources 中加载以单文件形式存放的 shader(使用 @shader vertex / @shader fragment 标记)，
 * 将其编译并缓存为 GL Program，按 config.json 中的 "shaders" 键进行初始化。
 */
public class ShaderManager {
    private static volatile ShaderManager instance;

    private Map<String, Integer> shaders = new HashMap<>();
    private Map<String, Integer> programs = new HashMap<>();

    private ShaderManager() {
        ConfigManager configManager = ConfigManager.getInstance();
       
        if (configManager != null) {
            Map<String, String> vertexShaderMap = configManager.vertexShaders;
            Map<String, String> fragmentShaderMap = configManager.fragmentShaders;
            // 加载, 编译shader
            loadShaders(vertexShaderMap, GL_VERTEX_SHADER);
            loadShaders(fragmentShaderMap, GL_FRAGMENT_SHADER);
            // link shader programs
            linkShaderPrograms();
            // delete shader
            deleteShaders();
        }
    }

    private void loadShaders(Map<String, String> shaderMap, int shaderType) {
        for (Map.Entry<String, String> entry : shaderMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            try {
                int shaderId = loadAndCompileShader(v, shaderType);
                shaders.put(k, shaderId);
            } catch (Exception ex) {
                System.err.println("Failed to load shader '" + k + "' from '" + v + "': " + ex.getMessage());
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

    public void deleteShaders() {
        for (int shaderId : shaders.values()) {
            if (shaderId != 0) glDeleteShader(shaderId);
        }
        shaders.clear();
    }

    /**
     * 从资源（classpath）加载并编译一个 shader 文件
     * 返回 GL program id（或抛出异常）。
     */

    private int loadAndCompileShader(String shaderPath, int shaderType) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(shaderPath);
        if (in == null) {
            throw new RuntimeException("Shader resource not found: " + shaderPath);
        }

        StringBuilder shader = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                shader.append(line).append('\n');
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read shader file: " + shaderPath + " - " + e.getMessage(), e);
        }
        String shaderSrc = shader.toString();
        return compileShader(shaderSrc, shaderType);
    }

    private int compileShader(String shaderSrc, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, shaderSrc);
        glCompileShader(shader);
        // compile 错误信息
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new RuntimeException("Shader compile error: " + log);
        }
        // 如果compile成功, 将shader ID 返回
        return shader;
    }

    private void linkShaderPrograms() {
        programs.put("texture_shader", linkShader(
            shaders.get("texture_vertex_shader"),
            shaders.get("texture_fragment_shader")
        ));
        programs.put("titan_spawn_shader", linkShader(
            shaders.get("texture_vertex_shader"),
            shaders.get("titan_spawn_fragment_shader")
        ));
        programs.put("tp_shader", linkShader(
            shaders.get("texture_vertex_shader"),
            shaders.get("tp_fragment_shader")
        ));
    }

    // 链接 shader program
    private int linkShader(int vShader, int fShader) {
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
        return prog;
    }

    public static ShaderManager getInstance() {
        if (instance == null) {
            synchronized (ShaderManager.class) {
                if (instance == null) 
                    instance = new ShaderManager();
            }
        }
        return instance;
    }
}
