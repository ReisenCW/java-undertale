package undertale.GameObject;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.BufferUtils;
import undertale.Texture.Texture;

 

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

    /**
     * 渲染子弹
     */
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

    /**
     * 渲染静态子弹
     */
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
            dataList.sort((data1, data2) -> Float.compare(data2.y, data1.y));

            // 计算四边形数量
            int quadCount = dataList.size();
            // 每个四边形 -> 6个顶点(2个三角形)
            // 每个顶点 -> 4个浮点数(x,y,u,v)
            FloatBuffer buf = BufferUtils.createFloatBuffer(quadCount * 6 * 4);

            for (int i = 0; i < dataList.size(); i++) {
                BulletRenderData data = dataList.get(i);
                float width = data.hScale * data.width;
                float height = data.vScale * data.height;
                Texture.appendQuad(buf, data.x, data.y, width, height, data.angle, 0.0f, 0.0f, 1.0f, 1.0f);
            }

            buf.flip();

            Texture.drawQuads(buf, quadCount, textureId, 1f, 1f, 1f, 1f);
        }
    }

    /**
     * 渲染动画子弹
     */
    private void renderAnimatedBullets(ArrayList<BulletRenderData> animatedBullets) {
        // 按id排序以确保渲染顺序稳定
        animatedBullets.sort(Comparator.comparingInt(data -> data.id));

        // 对于动画纹理, 逐个渲染
        for (BulletRenderData data : animatedBullets) {
            float width = data.hScale * data.width;
            float height = data.vScale * data.height;
            FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 4);
            Texture.appendQuad(buf, data.x, data.y, width, height, data.angle, 0f, 0f, 1f, 1f);
            buf.flip();
            Texture.drawQuads(buf, 1, data.textureId, data.rgba[0], data.rgba[1], data.rgba[2], data.rgba[3]);
        }
    }
}
