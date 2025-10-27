package undertale.Sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import undertale.GameMain.Game;
import undertale.Utils.ConfigManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoundManager {
    private static final SoundManager instance;
    private HashMap<String, String> soundEffects;
    private HashMap<String, String> musicTracks;

    // 音效缓存
    private final Map<String, Clip> seCache = new ConcurrentHashMap<>();

    // 音乐缓存
    private final Map<String, Clip> musicCache = new ConcurrentHashMap<>();

    // 当前正在播放的音乐路径（用于判断是否为缓存的 clip）
    private String currentMusicPath = null;
    
    // 是否已预加载过
    private boolean preloaded = false;

    // 当前music clip
    private Clip musicClip;
    private final Object musicLock = new Object();

    static {
        instance = new SoundManager();
    }

    private SoundManager() {
        ConfigManager configManager = Game.getConfigManager();
        this.soundEffects = configManager.se;
        this.musicTracks = configManager.music;
        // 预加载所有音效与音乐，避免首次播放时卡顿
        preloadAll();
    }

    public static SoundManager getInstance() {
        return instance;
    }

    // 加载 Clip
    private Clip loadClip(String path) throws Exception {
        AudioInputStream ais = null;
        try {
            InputStream ris = getClass().getClassLoader().getResourceAsStream(path);
            if (ris != null) {
                ais = AudioSystem.getAudioInputStream(new BufferedInputStream(ris));
            } else {
                File f = new File(path);
                if (!f.exists()) throw new FileNotFoundException("Sound file not found: " + path);
                ais = AudioSystem.getAudioInputStream(f);
            }

            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);

            AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, ais);

            Clip clip = AudioSystem.getClip();
            clip.open(dais);
            return clip;
        } finally {
            if (ais != null) {
                try { ais.close(); } catch (Exception ignored) {}
            }
        }
    }

    // 预加载所有配置中的 SE 与 music 到内存缓存
    private void preloadAll() {
        if (preloaded) return;
        preloaded = true;

        // 预加载音效
        if (soundEffects != null) {
            for (String path : soundEffects.values()) {
                if (path == null) continue;
                seCache.computeIfAbsent(path, key -> {
                    try {
                        return loadClip(key);
                    } catch (Exception e) {
                        System.err.println("Failed to preload SE: " + key);
                        e.printStackTrace();
                        return null;
                    }
                });
            }
        }

        // 预加载音乐
        if (musicTracks != null) {
            for (String path : musicTracks.values()) {
                if (path == null) continue;
                musicCache.computeIfAbsent(path, key -> {
                    try {
                        return loadClip(key);
                    } catch (Exception e) {
                        System.err.println("Failed to preload music: " + key);
                        e.printStackTrace();
                        return null;
                    }
                });
            }
        }
    }

    // 播放音效
    public void playSE(String soundFile) {
        try {
            // soundFile 为 真实路径或配置中的逻辑键
            String path = (soundEffects != null && soundEffects.containsKey(soundFile)
                    ? soundEffects.get(soundFile)
                    : soundFile);

            Clip clip = seCache.computeIfAbsent(path, key -> {
                try {
                    return loadClip(key);
                } catch (Exception e) {
                    System.err.println("Failed to load SE: " + key + " (requested: " + soundFile + ")");
                    e.printStackTrace();
                    return null;
                }
            });

            if (clip == null) return;

            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing SE: " + soundFile);
            e.printStackTrace();
        }
    }

    // 播放背景音乐, 暂停当前音乐
    public void playMusic(String musicFile) {
        synchronized (musicLock) {
            try {
                stopMusic();
                // musicFile 为 真实路径或配置中的逻辑键
                String path = (musicTracks != null && musicTracks.containsKey(musicFile)
                        ? musicTracks.get(musicFile)
                        : musicFile);

                // 优先使用缓存中的 clip
                Clip cached = (path != null) ? musicCache.get(path) : null;
                if (cached != null) {
                    musicClip = cached;
                    currentMusicPath = path;
                    if (musicClip.isRunning()) musicClip.stop();
                    musicClip.setFramePosition(0);
                    musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    musicClip.start();
                    return;
                }

                // 否则动态加载
                musicClip = loadClip(path);
                currentMusicPath = path;
                if (musicClip == null) return;
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();
            } catch (Exception e) {
                System.err.println("Error playing music: " + musicFile);
                e.printStackTrace();
            }
        }
    }

    // 停止音乐
    public void stopMusic() {
        synchronized (musicLock) {
            if (musicClip != null) {
                try {
                    musicClip.stop();
                    // 如果当前播放的 music 来自缓存，则不要 close（保留缓存）；否则关闭释放资源
                    if (currentMusicPath == null || !musicCache.containsKey(currentMusicPath)) {
                        musicClip.close();
                    } else {
                        // 重置到起始帧，方便下次直接播放
                        try { musicClip.setFramePosition(0); } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
                musicClip = null;
                currentMusicPath = null;
            }
        }
    }

    // 关闭 SoundManager, 释放资源
    public void shutdown() {
        for (Clip clip : seCache.values()) {
            try {
                if (clip != null) {
                    clip.stop();
                    clip.close();
                }
            } catch (Exception ignored) {}
        }
        seCache.clear();

        // 停止当前播放的音乐（若当前是非缓存 clip，将被关闭）
        stopMusic();

        // 关闭并清理音乐缓存
        for (Clip clip : musicCache.values()) {
            try {
                if (clip != null) {
                    clip.stop();
                    clip.close();
                }
            } catch (Exception ignored) {}
        }
        musicCache.clear();
    }
}
