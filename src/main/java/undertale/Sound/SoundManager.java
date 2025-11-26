package undertale.Sound;

import javax.sound.sampled.*;
import undertale.GameMain.Game;
import undertale.Utils.ConfigManager;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 声音管理器，采用享元模式，负责音效和音乐的加载与复用
public class SoundManager
{
    private static final SoundManager instance = new SoundManager(); // 单例实例
    private HashMap<String, String> soundEffects; // 音效配置（逻辑名->路径）
    private HashMap<String, String> musicTracks; // 音乐配置（逻辑名->路径）
    
    private final Map<String, Clip> seCache = new ConcurrentHashMap<>(); // 音效享元池
    private final Map<String, Clip> musicCache = new ConcurrentHashMap<>(); // 音乐享元池
    
    private String currentMusicPath = null; // 当前正在播放的音乐路径
    private boolean preloaded = false; // 是否已预加载过
    private Clip musicClip; // 当前音乐Clip实例
    private final Object musicLock = new Object();  // 音乐播放锁（保证线程安全）
    private float currentMusicVolume = 1.0f; // 当前音乐音量

    // 构造函数，初始化配置，不自动预加载
    private SoundManager()
    {
        // 默认采用懒加载，如需预加载可显式调用 preloadAll()
        ConfigManager configManager = ConfigManager.getInstance();
        this.soundEffects = configManager.se;
        this.musicTracks = configManager.music;
    }
    // 获取单例实例
    public static SoundManager getInstance()
    {
        return instance;
    }
    // 预加载所有音效与音乐
    public void preloadAll()
    {
        if (preloaded) return; // 已预加载则跳过
        preloaded = true;
        // 预加载音效
        if (soundEffects != null) {
            for (String path : soundEffects.values())
            {
                if (path == null)
                {
                    continue;
                }
                seCache.computeIfAbsent(path, key -> {
                    try { return loadClip(key); } catch (Exception e) { return null; }
                });
            }
        }
        // 预加载音乐
        if (musicTracks != null)
        {
            for (String path : musicTracks.values())
            {
                if (path == null)
                {
                    continue;
                }
                musicCache.computeIfAbsent(path, key -> {
                    try { return loadClip(key); } catch (Exception e) { return null; }
                });
            }
        }
    }
    // 设置Clip音量（0.0f-1.0f）
    private void setClipVolume(Clip clip, float volume)
    {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
        {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log10(Math.max(volume, 0.0001f)) * 20.0f); // 音量转分贝
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
        }
    }
    // 加载音频文件为Clip
    private Clip loadClip(String path) throws Exception
    {
        AudioInputStream ais = null;
        try {
            // 优先从资源目录加载
            InputStream ris = getClass().getClassLoader().getResourceAsStream(path);
            if (ris != null)
            {
                ais = AudioSystem.getAudioInputStream(new BufferedInputStream(ris));
            }
            else
            {
                // 否则从文件系统加载
                File f = new File(path);
                if (!f.exists())
                {
                    throw new FileNotFoundException("Sound file not found: " + path);
                }
                ais = AudioSystem.getAudioInputStream(f);
            }
            // 格式转换为PCM_SIGNED
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
            // 创建Clip并打开
            Clip clip = AudioSystem.getClip();
            clip.open(dais);
            return clip;
        }
        finally
        {
            if (ais != null)
            {
                try
                {
                    ais.close();
                }
                catch (Exception ignored){}
            }
        }
    }
    // 播放音效（默认音量1.0）
    public void playSE(String soundFile)
    {
        playSE(soundFile, 1.0f);
    }
    // 播放音效（可指定音量）
    public void playSE(String soundFile, float volume)
    {
        try {
            // 获取音效路径（支持逻辑名和直接路径）
            String path = (soundEffects != null && soundEffects.containsKey(soundFile)
                    ? soundEffects.get(soundFile)
                    : soundFile);
            // 懒加载Clip，享元池复用
            Clip clip = seCache.computeIfAbsent(path, key -> {
                try { return loadClip(key); } catch (Exception e) { return null; }
            });
            if (clip == null)
            {
                return;
            }
            setClipVolume(clip, volume);
            if (clip.isRunning())
            {
                // 若正在播放，临时新建Clip实现多实例并发
                Clip newClip = loadClip(path);
                if (newClip != null)
                {
                    setClipVolume(newClip, volume);
                    newClip.start();
                }
            }
            else
            {
                // 否则复用享元Clip
                clip.setFramePosition(0);
                clip.start();
            }
        }
        catch (Exception e){}
    }
    // 播放背景音乐（默认音量）
    public void playMusic(String musicFile)
    {
        playMusic(musicFile, currentMusicVolume);
    }
    // 播放背景音乐（可指定音量）
    public void playMusic(String musicFile, float volume)
    {
        synchronized (musicLock) // 保证线程安全
        { 
            try
            {
                stopMusic(); // 停止当前音乐
                
                String path = (musicTracks != null && musicTracks.containsKey(musicFile)
                        ? musicTracks.get(musicFile)
                        : musicFile);// 获取音乐路径
                // 懒加载Clip，享元池复用
                Clip cached = musicCache.computeIfAbsent(path, key -> {
                    try { return loadClip(key); } catch (Exception e) { return null; }
                });
                if (cached != null)
                {
                    musicClip = cached;
                    currentMusicPath = path;
                    setClipVolume(musicClip, volume);
                    currentMusicVolume = volume;
                    if (musicClip.isRunning()) musicClip.stop();
                    musicClip.setFramePosition(0);
                    musicClip.loop(Clip.LOOP_CONTINUOUSLY); // 循环播放
                    musicClip.start();
                }
            }
            catch (Exception e) {}
        }
    }
    // 设置当前音乐音量
    public void setCurrentMusicVolume(float volume)
    {
        synchronized (musicLock)
        {
            currentMusicVolume = volume;
            if (musicClip != null)
            {
                setClipVolume(musicClip, volume);
            }
        }
    }
    // 停止音乐播放
    public void stopMusic()
    {
        synchronized (musicLock)
        {
            if (musicClip != null)
            {
                try
                {
                    musicClip.stop();
                    // 若不是享元池Clip则关闭释放
                    if (currentMusicPath == null || !musicCache.containsKey(currentMusicPath))
                    {
                        musicClip.close();
                    }
                    else
                    {
                        try { musicClip.setFramePosition(0); } catch (Exception ignored) {}
                    }
                }
                catch (Exception ignored) {}
                musicClip = null;
                currentMusicPath = null;
            }
        }
    }
    // 停止所有音效
    public void stopAllSe()
    {
        for (Clip clip : seCache.values())
            {
            try
            {
                if (clip != null && clip.isRunning())
                {
                    clip.stop();
                    clip.setFramePosition(0);
                }
            }
            catch (Exception ignored) {}
        }
    }
    // 停止指定音效
    public void stopSe(String soundFile)
    {
        String path = (soundEffects != null && soundEffects.containsKey(soundFile)
                ? soundEffects.get(soundFile)
                : soundFile);
        Clip clip = seCache.get(path);
        if (clip != null && clip.isRunning())
        {
            try
            {
                clip.stop();
                clip.setFramePosition(0);
            }
            catch (Exception ignored) {}
        }
    }
    // 检查音效是否正在播放
    public boolean isSePlaying(String soundFile)
    {
        String path = (soundEffects != null && soundEffects.containsKey(soundFile)
                ? soundEffects.get(soundFile)
                : soundFile);
        Clip clip = seCache.get(path);
        return clip != null && clip.isRunning();
    }
    // 关闭SoundManager，释放所有资源
    public void shutdown() {
        for (Clip clip : seCache.values())
        {
            try
            {
                if (clip != null)
                {
                    clip.stop();
                    clip.close();
                }
            }
            catch (Exception ignored) {}
        }
        seCache.clear();
        stopMusic();
        for (Clip clip : musicCache.values())
        {
            try
            {
                if (clip != null)
                {
                    clip.stop();
                    clip.close();
                }
            }
            catch (Exception ignored) {}
        }
        musicCache.clear();
    }

    // 检查指定音乐是否正在播放
    public boolean isMusicPlaying(String musicName)
    {
        if(currentMusicPath == null || musicTracks == null)
        {
            return false;
        }
        String path = musicTracks.get(musicName);
        return currentMusicPath.equals(path);
    }
}