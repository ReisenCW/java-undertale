package undertale;

import java.util.ArrayList;
import java.util.Iterator;

public class EnemyManager {
	private ArrayList<Enemy> enemies;
    private static EnemyManager enemyManager;

    private TextureManager textureManager = TextureManager.getInstance();

    private float windowWidth = Game.getWindowWidth();
    private float windowCenterX = windowWidth / 2;

	private EnemyManager() {
		enemies = new ArrayList<>();
        init();
    }

    public static EnemyManager getInstance() {
        if (enemyManager == null) {
            enemyManager = new EnemyManager();
        }
        return enemyManager;
    }

    private void init() {
        // enemy_titan
        ConfigManager configManager = Game.getConfigManager();
        AnimationManager animationManager = AnimationManager.getInstance();

        Enemy titan = new Enemy("Titan", 5000, 5000, 50, 20);
        titan.addAct("check", "Dark element boss.\nEmit light, gather courage and use unleash to weaken it.");
        titan.addAct("light", "Your soul emits a gentle light.");
        titan.addAct("unleash", "Your soul emits a gentle light.\nThe titan's defense dropped to zero.");
        titan.addAct("single heal", "You healed a small amount of HP.");

        float bodyBottom = configManager.MENU_FRAME_BOTTOM - configManager.MENU_FRAME_HEIGHT;
        float starBottom = bodyBottom - 45;
        Animation bodyAnimation = animationManager.getAnimation("titan_body");
        Animation starAnimation = animationManager.getAnimation("titan_star");
        // 给所有backwing和frontwing添加动画
        Animation[] backwingAnimations = new Animation[4];
        for (int i = 0; i < backwingAnimations.length - 1; i++) {
            backwingAnimations[i] = animationManager.getAnimation("titan_backwing_" + i);
        }
        Animation[] frontwingAnimations = new Animation[3];
        for (int i = 0; i < frontwingAnimations.length; i++) {
            frontwingAnimations[i] = animationManager.getAnimation("titan_frontwing_" + i);
        }
        // name, animation, left, bottom, priority, scaler
        Object[][] titanAnimations = {
            {"frontwing_0", frontwingAnimations[0], windowCenterX - frontwingAnimations[0].getFrameWidth() / 2 - 40, bodyBottom, 4, 1.5f},
            {"frontwing_1", frontwingAnimations[1], windowCenterX - frontwingAnimations[1].getFrameWidth() / 2 - 120, bodyBottom + 20, 4, 1.7f},
            {"frontwing_2", frontwingAnimations[2], windowCenterX - frontwingAnimations[2].getFrameWidth() / 2 - 80, bodyBottom - 20, 4, 1.5f},
            {"backwing_0", backwingAnimations[0], windowCenterX - backwingAnimations[0].getFrameWidth() / 2 - 100, bodyBottom, 1, 1.6f},
            {"backwing_1", backwingAnimations[1], windowCenterX - backwingAnimations[1].getFrameWidth() / 2 - 120, bodyBottom + 20, 1, 1.7f},
            {"backwing_2", backwingAnimations[2], windowCenterX - backwingAnimations[2].getFrameWidth() / 2 - 80, bodyBottom - 20, 0, 1.5f},
            {"backwing_3", backwingAnimations[3], windowCenterX - backwingAnimations[3].getFrameWidth() / 2 - 40, bodyBottom - 20, 0, 1.6f},
            {"body", bodyAnimation, windowCenterX - bodyAnimation.getFrameWidth() / 2, bodyBottom, 2, 1.5f},
            {"star", starAnimation, windowCenterX - starAnimation.getFrameWidth() / 2 - 10 , starBottom, 3, 1.5f}
        };
        for (Object[] anim : titanAnimations) {
            titan.addAnimation(
                (String)anim[0],
                (Animation)anim[1],
                (Float)anim[2],
                (Float)anim[3],
                (Integer)anim[4],
                (Float)anim[5]
            );
        }
        addEnemy(titan);
    }

	public void addEnemy(Enemy enemy) {
		enemies.add(enemy);
	}

	public void removeEnemy(Enemy enemy) {
		enemies.remove(enemy);
	}

	public void clearEnemies() {
		enemies.clear();
	}

	public ArrayList<Enemy> getEnemies() {
		return enemies;
	}

	public Enemy getEnemy(int index) {
		if (index >= 0 && index < enemies.size()) {
			return enemies.get(index);
		}
		return null;
	}

	public int getEnemyCount() {
		return enemies.size();
	}

	public void update(float deltaTime) {
		Iterator<Enemy> it = enemies.iterator();
		while (it.hasNext()) {
			Enemy enemy = it.next();
			enemy.update(deltaTime);
			if (!enemy.isAlive()) {
				it.remove();
			}
		}
	}

	public void render() {
        for (Enemy enemy : enemies) {
            enemy.render();
        }
	}
}
