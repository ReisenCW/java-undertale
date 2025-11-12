package undertale.Enemy;

import java.util.ArrayList;
import java.util.Iterator;

import undertale.Animation.Animation;
import undertale.Animation.AnimationManager;
import undertale.GameMain.Game;
import undertale.GameObject.Player;
import undertale.Utils.ConfigManager;

public class EnemyManager {
	private ArrayList<Enemy> enemies;
    private static EnemyManager enemyManager;

    private float windowWidth = Game.getWindowWidth();
    private float windowCenterX = windowWidth / 2;

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

    // TODO: 给act加一个参数function，用于判断是否能够执行该act
    private void init() {
        // enemy_titan
        ConfigManager configManager = Game.getConfigManager();
        AnimationManager animationManager = AnimationManager.getInstance();
        Player player = Game.getPlayer();

        Enemy titan = new Enemy("Titan", 500, 500, 50, 20);
        titan.addAct(
            "check",
            "* Dark element boss.\n* Emit light, gather courage and use unleash to weaken it.",
            "check enemy info",
            () -> true,
            () -> {}
        );
        titan.addAct(
            "light",
            "* Your soul emits a greater light.",
            "costs 4 tp, player emits greater light",
            () -> (player.getTensionPoints() >= 4),
            () -> {
                player.setTensionPoints(player.getTensionPoints() - 4);
                player.setTargetLightRadius(Player.LightLevel.ENHANCED);
            }
        );
        titan.addAct(
            "unleash",
            "* Your soul emits a gentle light.\n* The titan's defense dropped to zero.",
            "costs 80 tp, titan gets weakened for 1 turn",
            () -> (player.getTensionPoints() >= 80),
            () -> {
                player.setTensionPoints(player.getTensionPoints() - 80);
                // TODO: titan 两回合防御下降
            }
        );
        titan.addAct(
            "single heal",
            "* You healed a small amount of HP.",
            "costs 4 tp, heals a small amount of hp",
            () -> (player.getTensionPoints() >= 4),
            () -> {
                int healAmount = 8 + (int)(Math.random() * 16);
                player.heal(healAmount);
                player.setTensionPoints(player.getTensionPoints() - 4);
            }
        );

        float bodyBottom = configManager.MENU_FRAME_BOTTOM - configManager.MENU_FRAME_HEIGHT;
        float starBottom = bodyBottom - 45;
        Animation bodyAnimation = animationManager.getAnimation("titan_body");
        Animation starAnimation = animationManager.getAnimation("titan_star");
        // 给所有backwing和frontwing添加动画
        Animation[] backwingAnimations = new Animation[4];
        for (int i = 0; i < backwingAnimations.length; i++) {
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
