package undertale.GameObject;

import java.util.ArrayList;
import java.util.Comparator;

import static org.lwjgl.opengl.GL11.*;

// 子弹渲染器, 用于批量渲染子弹以提升性能
public class BulletRenderer {
    public class BulletRenderData {
        public int id;
        public int textureId;
        public float x;
        public float y;
        public float angle;
        public float hScale;
        public float vScale;
        public float width;
        public float height;
        public float[] rgba;
        public boolean isAnimation;

        public BulletRenderData(int id, int textureId, float x, float y, float angle, float hScale, float vScale, float width, float height, float[] rgba, boolean isAnimation) {
            this.id = id;
            this.textureId = textureId;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.hScale = hScale;
            this.vScale = vScale;
            this.width = width;
            this.height = height;
            this.rgba = rgba;
            this.isAnimation = isAnimation;
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
        // 分离Animation和非Animation的bullet
        ArrayList<BulletRenderData> staticBullets = new ArrayList<>();
        ArrayList<BulletRenderData> animatedBullets = new ArrayList<>();
        
        for (BulletRenderData data : renderDataList) {
            if (data.isAnimation) {
                animatedBullets.add(data);
            } else {
                staticBullets.add(data);
            }
        }

        // 批量渲染静态texture的bullet
        renderStaticBullets(staticBullets);
        
        // 单独渲染Animation的bullet
        renderAnimatedBullets(animatedBullets);
    }

    private void renderStaticBullets(ArrayList<BulletRenderData> staticBullets) {
        // 按纹理ID分组渲染数据
        ArrayList<ArrayList<BulletRenderData>> groupedData = new ArrayList<>();
        for (BulletRenderData data : staticBullets) {
            int textureId = data.textureId;
            while (groupedData.size() <= textureId) {
                groupedData.add(new ArrayList<>());
            }
            groupedData.get(textureId).add(data);
        }

        // 对每个纹理组进行渲染
        for (int textureId = 0; textureId < groupedData.size(); textureId++) {
            ArrayList<BulletRenderData> dataList = groupedData.get(textureId);
            if (dataList.isEmpty()) continue;

            // 按ID排序以确保渲染顺序稳定
            // dataList.sort(Comparator.comparingInt(data -> data.id));
            dataList.sort((data1, data2) -> Float.compare(data2.y, data1.y));

            // 绑定纹理
            glBindTexture(GL_TEXTURE_2D, textureId);

            for (int i = 0; i < dataList.size(); i++) {
                BulletRenderData data = dataList.get(i);
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

    private void renderAnimatedBullets(ArrayList<BulletRenderData> animatedBullets) {
        // 按id排序以确保渲染顺序稳定
        animatedBullets.sort(Comparator.comparingInt(data -> data.id));

        for (BulletRenderData data : animatedBullets) {
            // 绑定纹理
            glBindTexture(GL_TEXTURE_2D, data.textureId);

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

            // 解绑纹理
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        // 重置颜色
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
