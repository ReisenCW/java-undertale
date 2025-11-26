package undertale.GameObject;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.BufferUtils;

import undertale.GameObject.Bullets.Bullet;
import undertale.Texture.Texture;

 

// 子弹渲染器, 用于批量渲染子弹以提升性能
/**
 * Legacy bulk bullet renderer kept for reference/performance experiments.
 *
 * NOTE: Rendering now happens via GameObject.render() and composite layers in
 * ObjectManager. This class is deprecated and no longer used by the engine.
 */
@Deprecated
public class BulletRenderer {
/**
 * Legacy bullet renderer kept for reference/performance experiments.
 *
 * NOTE: Bullets now render themselves via GameObject.render() and are
 * managed by composite layers. This class is deprecated and not used by
 * ObjectManager anymore.
 */
@Deprecated
// 子弹渲染器, 用于批量渲染子弹以提升性能
    private ArrayList<Bullet> bulletsToRender;

    public BulletRenderer() {
        bulletsToRender = new ArrayList<>();
    }
    public void addBullet(Bullet bullet) {
        bulletsToRender.add(bullet);
    }

    public void clearBullets() {
        bulletsToRender.clear();
    }

    /**
     * 渲染子弹
     */
    public void renderBullets() {
        // 分离Animation和非Animation的bullet
        ArrayList<Bullet> staticBullets = new ArrayList<>();
        ArrayList<Bullet> animatedBullets = new ArrayList<>();
        
        for (Bullet bullet : bulletsToRender) {
            if (bullet.hasAnimation()) {
                animatedBullets.add(bullet);
            } else {
                staticBullets.add(bullet);
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
    private void renderStaticBullets(ArrayList<Bullet> staticBullets) {
        // 按纹理ID分组渲染数据
        ArrayList<ArrayList<Bullet>> groupedData = new ArrayList<>();
        for (Bullet data : staticBullets) {
            Texture currentTexture = data.getCurrentTexture();
            if (currentTexture == null) continue;
            int textureId = currentTexture.getId();
            while (groupedData.size() <= textureId) {
                groupedData.add(new ArrayList<>());
            }
            groupedData.get(textureId).add(data);
        }

        // 对每个纹理组进行渲染
        for (int textureId = 0; textureId < groupedData.size(); textureId++) {
            ArrayList<Bullet> dataList = groupedData.get(textureId);
            if (dataList.isEmpty()) continue;

            // 按id排序以确保渲染顺序稳定
            dataList.sort((data1, data2) -> Integer.compare(data1.getId(), data2.getId()));

            // 计算四边形数量
            int quadCount = dataList.size();
            // 每个四边形 -> 6个顶点(2个三角形)
            // 每个顶点 -> 4个浮点数(x,y,u,v)
            FloatBuffer buf = BufferUtils.createFloatBuffer(quadCount * 6 * 4);

            for (Bullet data : dataList) {
                Texture currentTexture = data.getCurrentTexture();
                float width = data.getHScale() * currentTexture.getWidth();
                float height = data.getVScale() * currentTexture.getHeight();
                Texture.appendQuad(buf, data.x, data.y, width, height, data.getSelfAngle(), 0.0f, 0.0f, 1.0f, 1.0f);
            }

            buf.flip();

            Texture.drawQuads(buf, quadCount, textureId, 1f, 1f, 1f, 1f);
        }
    }

    /**
     * 渲染动画子弹
     */
    private void renderAnimatedBullets(ArrayList<Bullet> animatedBullets) {
        // 按id排序以确保渲染顺序稳定
        animatedBullets.sort(Comparator.comparingInt(bullet -> bullet.getId()));

        // 对于动画纹理, 逐个渲染
        for (Bullet bullet : animatedBullets) {
            bullet.render();
        }
    }
}
