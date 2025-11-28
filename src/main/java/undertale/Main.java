package undertale;
import undertale.GameMain.Game;

public class Main {
	public static void main(String[] args) {
		// 依赖注入 (Dependency Injection) 重构内容: Game 类负责创建和持有 EnemyManager 实例，并将其传递给 UIManager 和 BattleFightScene。
		// 作用: 集中管理游戏的主要组件，消除全局单例依赖。
		Game.getInstance().run();
	}
}