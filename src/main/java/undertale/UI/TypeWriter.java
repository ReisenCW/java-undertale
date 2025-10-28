package undertale.UI;

import java.util.ArrayList;

import undertale.Sound.SoundManager;
import undertale.Texture.FontManager;
import undertale.Utils.ConfigManager;

public class TypeWriter extends UIBase {
    private FontManager fontManager;
    private SoundManager soundManager;

    private float typewriterElapsed;

    // 打字机效果相关变量
    private String lastText;
    private ArrayList<String> displayLines;
    private ArrayList<Boolean> isRawNewline;
    private int totalCharsToShow;
    private boolean typewriterAllShown;
    private final int TYPEWRITER_SPEED = 24; // 每秒显示字符数
    private final float LINE_PAUSE_DURATION = 0.25f; // 每行换行停顿时间（秒） 


    public TypeWriter(ConfigManager configManager, FontManager fontManager) {
        super(configManager);
        this.fontManager = fontManager;
        this.soundManager = SoundManager.getInstance();
        displayLines = new ArrayList<>();
        isRawNewline = new ArrayList<>();
        reset();
    }

    public void reset() {
        typewriterElapsed = 0f;
        lastText = null;
        displayLines.clear();
        isRawNewline.clear();
        totalCharsToShow = 0;
        typewriterAllShown = false;
    }

    public void update(float deltaTime) {
        if (!typewriterAllShown) {
            soundManager.playSE("text_print");
            typewriterElapsed += deltaTime;
        }
    }

    public void renderTexts(String text, float left, float top, float maxWidth) {
        // 打字机效果，X跳过全部显示，全部显示后Z才可继续
        float fontHeight = fontManager.getFontHeight() + 5;

        // 若文本变化，重置打字机状态
        if (lastText == null || !lastText.equals(text)) {
            lastText = text;
            displayLines.clear();
            isRawNewline.clear();
            // 先按\n分割，再对每行做自动换行
            String[] lines = text.split("\\n");
            for (String rawLine : lines) {
                int start = 0;
                int len = rawLine.length();
                boolean first = true;
                while (start < len) {
                    int end = start;
                    while (end < len) {
                        int nextSpace = rawLine.indexOf(' ', end);
                        String sub = rawLine.substring(start, nextSpace == -1 ? len : nextSpace);
                        if (fontManager.getTextWidth(sub) > maxWidth) break;
                        end = nextSpace == -1 ? len : nextSpace + 1;
                    }
                    if (end == start) end++;
                    String line = rawLine.substring(start, end);
                    displayLines.add(line);
                    isRawNewline.add(first); // 只有原始\n的第一行才true
                    first = false;
                    start = end;
                }
            }
            totalCharsToShow = 0;
            typewriterElapsed = 0f;
            typewriterAllShown = false;
        }

        // 计算当前应显示的字符数（仅原始\n换行才停顿）
        if (!typewriterAllShown) {
            int total = 0;
            int charsToShow = 0;
            for (int i = 0; i < displayLines.size(); i++) {
                String line = displayLines.get(i);
                boolean pause = isRawNewline != null && isRawNewline.size() > i && isRawNewline.get(i);
                float lineStart = (float)total / TYPEWRITER_SPEED + (pause ? i * LINE_PAUSE_DURATION : 0);
                float lineElapsed = typewriterElapsed - lineStart;
                if (lineElapsed > 0) {
                    int lineChars = Math.min(line.length(), (int)(lineElapsed * TYPEWRITER_SPEED));
                    charsToShow += lineChars;
                }
                // 若本行未全部显示，后续行不显示
                if (lineElapsed < ((float)line.length() / TYPEWRITER_SPEED)) {
                    break;
                }
                total += line.length();
            }
            // 限制最大
            int allChars = 0;
            for (String l : displayLines) allChars += l.length();
            totalCharsToShow = Math.min(charsToShow, allChars);
            if (totalCharsToShow >= allChars) {
                typewriterAllShown = true;
            }
        }

        // 绘制文本
        int shown = 0;
        int rowIdx = 0;
        for (String line : displayLines) {
            int remain = totalCharsToShow - shown;
            if (remain <= 0) break;
            int toShow = Math.min(remain, line.length());
            fontManager.drawText(line.substring(0, toShow), left, top + rowIdx * fontHeight, 1.0f, 1.0f, 1.0f, 1.0f);
            shown += toShow;
            rowIdx++;
        }
    }

    public void renderTextsInMenu(String text) {
        float left = MENU_FRAME_LEFT + 50;
        float top = MENU_FRAME_BOTTOM - MENU_FRAME_HEIGHT + 50;
        float maxWidth = MENU_FRAME_WIDTH - 40;
        renderTexts(text, left, top, maxWidth);
    }

    public boolean isTypewriterAllShown() {
        return typewriterAllShown;
    }

    public void showAll() {
        if (!typewriterAllShown) {
            int total = 0;
            for (String line : displayLines) total += line.length();
            totalCharsToShow = total;
            typewriterAllShown = true;
        }
    }
}
