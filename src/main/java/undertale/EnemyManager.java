package undertale;

import java.util.ArrayList;
import java.util.Iterator;

public class EnemyManager {
	private ArrayList<Enemy> enemies;
    private static EnemyManager enemyManager;

    private TextureManager textureManager = TextureManager.getInstance();
    private UIManager uiManager = UIManager.getInstance();

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
        Enemy titan = new Enemy("Titan", 999999, 999999, 50, 20);
        float bodyBottom = uiManager.MENU_FRAME_BOTTOM - uiManager.MENU_FRAME_HEIGHT;
        float starBottom = bodyBottom - 45;
        Animation bodyAnimation = new Animation(0, false, 
            textureManager.getTexture("titan_body"));
        Animation starAnimation = new Animation(0, false,
            textureManager.getTexture("titan_star"));
        // 给所有backwing和frontwing添加动画
        Animation[] backwingAnimations = new Animation[4];
        for (int i = 0; i < backwingAnimations.length - 1; i++) {
            backwingAnimations[i] = new Animation(0.4f, true,
                textureManager.getTexture("titan_backwing_" + i + "_0"),
                textureManager.getTexture("titan_backwing_" + i + "_1"),
                textureManager.getTexture("titan_backwing_" + i + "_2"),
                textureManager.getTexture("titan_backwing_" + i + "_3"),
                textureManager.getTexture("titan_backwing_" + i + "_4"),
                textureManager.getTexture("titan_backwing_" + i + "_5"),
                textureManager.getTexture("titan_backwing_" + i + "_6"));
        }
        backwingAnimations[backwingAnimations.length - 1] = new Animation(0.4f, true, true, false,
            textureManager.getTexture("titan_backwing_0_0"),
            textureManager.getTexture("titan_backwing_0_1"),
            textureManager.getTexture("titan_backwing_0_2"),
            textureManager.getTexture("titan_backwing_0_3"),
            textureManager.getTexture("titan_backwing_0_4"),
            textureManager.getTexture("titan_backwing_0_5"),
            textureManager.getTexture("titan_backwing_0_6"));
        
        Animation[] frontwingAnimations = new Animation[3];
        for (int i = 0; i < frontwingAnimations.length; i++) {
            frontwingAnimations[i] = new Animation(0.4f, true,
                textureManager.getTexture("titan_frontwing_" + i + "_0"),
                textureManager.getTexture("titan_frontwing_" + i + "_1"),
                textureManager.getTexture("titan_frontwing_" + i + "_2"),
                textureManager.getTexture("titan_frontwing_" + i + "_3"),
                textureManager.getTexture("titan_frontwing_" + i + "_4"),
                textureManager.getTexture("titan_frontwing_" + i + "_5"),
                textureManager.getTexture("titan_frontwing_" + i + "_6"));
        }
        titan.addAnimation("frontwing_0", frontwingAnimations[0], windowCenterX - frontwingAnimations[0].getFrameWidth() / 2 - 40, bodyBottom, 4, 1.5f);

        titan.addAnimation("frontwing_1", frontwingAnimations[1], windowCenterX - frontwingAnimations[1].getFrameWidth() / 2 - 120, bodyBottom + 20, 4, 1.7f);

        titan.addAnimation("frontwing_2", frontwingAnimations[2], windowCenterX - frontwingAnimations[2].getFrameWidth() / 2 - 80, bodyBottom - 20, 4, 1.5f);

        titan.addAnimation("backwing_0", backwingAnimations[0], windowCenterX - backwingAnimations[0].getFrameWidth() / 2 - 100, bodyBottom, 1, 1.6f);

        titan.addAnimation("backwing_1", backwingAnimations[1], windowCenterX - backwingAnimations[1].getFrameWidth() / 2 - 120, bodyBottom + 20, 1, 1.7f);

        titan.addAnimation("backwing_2", backwingAnimations[2], windowCenterX - backwingAnimations[2].getFrameWidth() / 2 - 80, bodyBottom - 20, 0, 1.5f);

        titan.addAnimation("backwing_3", backwingAnimations[3], windowCenterX - backwingAnimations[3].getFrameWidth() / 2 - 40, bodyBottom - 20, 0, 1.6f);

        titan.addAnimation("body", bodyAnimation, windowCenterX - bodyAnimation.getFrameWidth() / 2, bodyBottom, 2, 1.5f);
        titan.addAnimation("star", starAnimation, windowCenterX - starAnimation.getFrameWidth() / 2 - 10 , starBottom, 3, 1.5f);
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
