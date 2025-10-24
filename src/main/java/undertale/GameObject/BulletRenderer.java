package undertale.GameObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

// 子弹渲染器, 用于批量渲染子弹以提升性能
public class BulletRenderer {
    public class BulletRenderData {
        public int textureId;
        public float x;
        public float y;
        public float angle;
        public float hScale;
        public float vScale;
        public float width;
        public float height;
        public float[] rgba;

        public BulletRenderData(int textureId, float x, float y, float angle, float hScale, float vScale, float width, float height, float[] rgba) {
            this.textureId = textureId;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.hScale = hScale;
            this.vScale = vScale;
            this.width = width;
            this.height = height;
            this.rgba = rgba;
        }
    }

    private ArrayList<BulletRenderData> renderDataList;
    public BulletRenderer() {
        renderDataList = new ArrayList<>();
    }

    public void addBulletRenderData(BulletRenderData data) {
        renderDataList.add(data);
    }

    public void clearBulletRenderData() {
        renderDataList.clear();
    }

    public void renderBullets() {
        // 按纹理ID分组渲染数据
        Map<Integer, List<BulletRenderData>> groupedData = new HashMap<>();
        for (BulletRenderData data : renderDataList) {
            groupedData.computeIfAbsent(data.textureId, k -> new ArrayList<>()).add(data);
        }

        // 对每个纹理组进行渲染
        for (Map.Entry<Integer, List<BulletRenderData>> entry : groupedData.entrySet()) {
            int textureId = entry.getKey();
            List<BulletRenderData> dataList = entry.getValue();

            // 绑定纹理
            glBindTexture(GL_TEXTURE_2D, textureId);

            for (BulletRenderData data : dataList) {
                // 设置颜色
                glColor4f(data.rgba[0], data.rgba[1], data.rgba[2], data.rgba[3]);

                // 计算尺寸
                float width = data.hScale * data.width;
                float height = data.vScale * data.height;

                // 以纹理中心为旋转中心
                float cx = data.x + width / 2.0f;
                float cy = data.y + height / 2.0f;

                glPushMatrix();
                glTranslatef(cx, cy, 0);
                glRotatef(data.angle, 0, 0, 1);
                glTranslatef(-cx, -cy, 0);

                glBegin(GL_QUADS);
                // 左上角
                glTexCoord2f(0.0f, 0.0f);
                glVertex2f(data.x, data.y);
                // 右上角
                glTexCoord2f(1.0f, 0.0f);
                glVertex2f(data.x + width, data.y);
                // 右下角
                glTexCoord2f(1.0f, 1.0f);
                glVertex2f(data.x + width, data.y + height);
                // 左下角
                glTexCoord2f(0.0f, 1.0f);
                glVertex2f(data.x, data.y + height);
                glEnd();

                glPopMatrix();
            }

            // 解绑纹理
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        // 重置颜色
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
