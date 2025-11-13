package undertale.Enemy;

import java.util.ArrayList;
import java.util.Iterator;

public class EnemyManager {
	private ArrayList<Enemy> enemies;
    private static EnemyManager enemyManager;

    private Enemy currentEnemy = null;

    static {
        enemyManager = new EnemyManager();
    }

	private EnemyManager() {
		enemies = new ArrayList<>();
        init();
    }

    public static EnemyManager getInstance() {
        return enemyManager;
    }

    private void init() {
        // 创建Titan敌人
        Titan titan = new Titan();
        addEnemy(titan);
        setCurrentEnemy(0);
    }

    public boolean isAllEnemiesDefeated() {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public int getTotalExp() {
        int totalExp = 0;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                totalExp += enemy.getDropExp();
            }
        }
        return totalExp;
    }

    public int getTotalGold(boolean includeAlive) {
        int totalGold = 0;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() || includeAlive) {
                totalGold += enemy.getDropGold();
            }
        }
        return totalGold;
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
        if (index < 0) return null;
        int aliveIdx = 0;
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                if (aliveIdx == index) return e;
                aliveIdx++;
            }
        }
        return null;
	}

    // 返回hp>0的enemy数量
	public int getEnemyCount() {
        int cnt = 0;
        for (Enemy e : enemies) if (e.isAlive()) cnt++;
        return cnt;
	}

	public void update(float deltaTime) {
		Iterator<Enemy> it = enemies.iterator();
		while (it.hasNext()) {
			Enemy enemy = it.next();
			enemy.update(deltaTime);
		}
	}

	public void render() {
        for (Enemy enemy : enemies) {
            enemy.render();
        }
	}

    public void allowRenderEnemy(Enemy enemy, boolean allow) {
        if (enemy != null) {
            enemy.setAllowRender(allow);
        }
    }

    public void resetEnemies() {
        for (Enemy enemy : enemies) {
            enemy.reset();
        }
        setCurrentEnemy(0);
    }

    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    public void setCurrentEnemy(int index) {
        this.currentEnemy = getEnemy(index);
    }
}
