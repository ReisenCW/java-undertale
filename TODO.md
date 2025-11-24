# 1. 设计模式重构建议
有建议/想法可以群里说
仅供参考，不要完全相信文档的建议

## 1.1. 设计模式类型速通
- [柏码知识库 | 设计模式（二）创建型](https://www.itbaima.cn/zh-CN/document/8ftkb38wfn6ox0ug)
- [柏码知识库 | 设计模式（四）行为型](https://www.itbaima.cn/zh-CN/document/5434a3cyyjvwhs8s)



## 1.2. 工厂模式（Factory）
**现状**：场景创建直接在`Game`类中硬编码（如`new BeginMenuScene(...)`），新增场景需修改`Game`类，违反**开闭原则**。

**重构建议**：
1. **创建场景工厂类**：统一管理场景的创建逻辑：
   ```java
   public class SceneFactory {
       private ObjectManager objectManager;
       private InputManager inputManager;
       
       public SceneFactory(ObjectManager objectManager, InputManager inputManager) {
           this.objectManager = objectManager;
           this.inputManager = inputManager;
       }
       
       public Scene createScene(SceneEnum type) {
           switch (type) {
               case START_MENU:
                   return new BeginMenuScene(objectManager, inputManager);
               case BATTLE_MENU:
                   return new BattleMenuScene(objectManager, inputManager);
               // ... 其他场景
               default:
                   throw new IllegalArgumentException("Unknown scene type: " + type);
           }
       }
   }
   ```

2. **在`Game`中使用工厂**：
   ```java
   // 初始化时创建工厂
   SceneFactory sceneFactory = new SceneFactory(objectManager, inputManager);
   // 注册场景
   sceneManager.registerScene(SceneEnum.START_MENU, sceneFactory.createScene(SceneEnum.START_MENU));
   ```
   这样`Game`中场景的创建只与`SceneEnum`有关，对已有场景做修改（如重命名）时无需对`Game`进行修改

## 1.3. 依赖注入（Dependency Injection）的优化
**现状**：`Scene`的构造函数依赖`ObjectManager`和`InputManager`，但通过`Game`类直接传递，高层模块（`Game`）依赖低层模块（具体`Scene`），耦合紧密。

**重构建议**：
1. **使用构造函数注入**：通过工厂模式（如前文的`SceneFactory`）统一注入依赖，而非在`Game`中硬编码：
   ```java
   // SceneFactory 负责注入依赖，Scene 只依赖抽象
   public class SceneFactory {
       private final ObjectManager objectManager;
       private final InputManager inputManager;
       
       // 工厂的依赖通过构造函数注入
       public SceneFactory(ObjectManager objectManager, InputManager inputManager) {
           this.objectManager = objectManager;
           this.inputManager = inputManager;
       }
       
       public Scene createScene(SceneEnum type) {
           // 所有场景的依赖由工厂统一传递
           return switch (type) {
               case START_MENU -> new BeginMenuScene(objectManager, inputManager);
               case BATTLE_MENU -> new BattleMenuScene(objectManager, inputManager);
               // ...
           };
       }
   }
   ```

## 1.4. 单例模式规范
configManager应改为单例
为避免现有单例实现的不一致，建议遵循以下规范：

### 1.4.1. 枚举单例（推荐，适合无复杂初始化逻辑的类）
如SceneManager：
```java
public enum SceneManager {
    INSTANCE;
    
    private HashMap<SceneEnum, Scene> scenes = new HashMap<>();
    private Scene currentScene;
    
    // 原有方法实现...
    public void registerScene(SceneEnum type, Scene scene) {
        scenes.put(type, scene);
    }
    
    public static SceneManager getInstance() {
        return INSTANCE;
    }
}
```
枚举单例天然线程安全，且防止反射和序列化破坏单例。

### 1.4.2. 双重校验锁单例（适合需要延迟初始化或复杂构造的类）
如ShaderManager（需要加载配置后初始化）：
```java
public class ShaderManager {
    private static volatile ShaderManager instance;
    
    private ShaderManager() {
        // 私有构造，初始化逻辑
        ConfigManager config = ConfigManager.getInstance();
        loadShaders(config);
    }
    
    public static ShaderManager getInstance() {
        if (instance == null) {
            synchronized (ShaderManager.class) {
                if (instance == null) {
                    instance = new ShaderManager();
                }
            }
        }
        return instance;
    }
}
```

## 1.5. 观察者模式（Observer）
**现状**：输入事件（如按键）处理与场景逻辑耦合（如`InputManager`直接操作`Player`），场景切换时输入响应逻辑难以维护。

**重构建议**：
1. **定义输入观察者接口**：
   ```java
   public interface InputObserver {
       void onKeyPressed(int key);
       void onKeyReleased(int key);
   }
   ```

2. **`InputManager`作为被观察者**：
   ```java
   public class InputManager {
       private List<InputObserver> observers = new ArrayList<>();
       
       public void addObserver(InputObserver observer) {
           observers.add(observer);
       }
       
       public void removeObserver(InputObserver observer) {
           observers.remove(observer);
       }
       
       // 输入处理时通知观察者
       private void processInput() {
           // ... 原有逻辑
           for (InputObserver observer : observers) {
               if (glfwGetKey(window, key) == GLFW_PRESS) {
                   observer.onKeyPressed(key);
               }
           }
       }
   }
   ```

3. **场景实现观察者接口**：场景需要处理输入时，注册为观察者，切换场景时自动解绑：
   ```java
   public class BattleMenuScene extends Scene implements InputObserver {
       @Override
       public void onEnter() {
           inputManager.addObserver(this); // 进入场景时注册
       }
       
       @Override
       public void onExit() {
           inputManager.removeObserver(this); // 退出场景时解绑
       }
       
       @Override
       public void onKeyPressed(int key) {
           // 处理本场景的按键逻辑
       }
   }
   ```

## 1.6. 状态模式（State）的应用
**现状**：`UIManager`中通过`MenuState`枚举和大量`switch-case`处理菜单状态（如`renderFrameContents`方法），新增状态需修改多个`case`分支，维护成本高。

**重构建议**：
1. **定义状态接口**：
   ```java
   public interface MenuState {
       void render(UIManager uiManager, String roundText);
       void handleInput(UIManager uiManager, int key);
   }
   ```

2. **实现具体状态类**：
   ```java
   public class MainMenuState implements MenuState {
       @Override
       public void render(UIManager uiManager, String roundText) {
           uiManager.getMenuTypeWriter().renderTextsInMenu(roundText);
       }
       
       @Override
       public void handleInput(UIManager uiManager, int key) {
           // 主菜单输入逻辑
       }
   }
   
   public class FightState implements MenuState {
       @Override
       public void render(UIManager uiManager, String roundText) {
           uiManager.getAttackAnimManager().renderFightPanel(...);
       }
       
       // ...
   }
   ```

3. **`UIManager`中使用状态模式**：
   ```java
   public class UIManager extends UIBase {
       private MenuState currentState;
       
       public UIManager() {
           currentState = new MainMenuState(); // 初始状态
       }
       
       public void setCurrentState(MenuState state) {
           this.currentState = state;
       }
       
       public void renderFrameContents(String roundText) {
           currentState.render(this, roundText);
       }
       
       public void handleInput(int key) {
           currentState.handleInput(this, key);
       }
   }
   ```
   新增状态只需添加新的`MenuState`实现类，无需修改现有逻辑。

## 1.7. Builder Pattern
对于那些包含大量参数或存在众多重载方法的类与方法（例如Bullet类、drawTexture方法等），适合采用 Builder Pattern 进行优化。类似StringBuilder的设计思想，通过链式调用的方式分步设置所需参数，未显式设置的参数则自动沿用预设的默认值，既能避免冗长的构造函数或重载方法带来的维护成本，又能让参数配置过程更清晰直观，大幅提升代码的可读性与扩展性。

## 1.8. 模板方法模式（Template Method）的应用
**现状**：不同`Scene`（如`BattleFightScene`、`GameOverScene`）的生命周期（初始化、更新、渲染）流程相似，但具体实现不同，存在重复代码。

**重构建议**：
1. **在`Scene`基类中定义模板方法**：
   ```java
   public abstract class Scene {
       // 模板方法：固定流程
       public final void process(float deltaTime) {
           if (!initialized) {
               init(); // 初始化（只执行一次）
               initialized = true;
           }
           update(deltaTime); // 子类实现具体更新逻辑
           render(); // 子类实现具体渲染逻辑
       }
       
       protected abstract void init(); // 初始化（子类实现）
       protected abstract void update(float deltaTime); // 更新（子类实现）
       protected abstract void render(); // 渲染（子类实现）
       
       // 钩子方法：可选实现
       public void onEnter() {}
       public void onExit() {}
   }
   ```

2. **子类继承并实现抽象方法**：
   ```java
   public class BattleFightScene extends Scene {
       @Override
       protected void init() {
           // 战斗场景初始化逻辑
       }
       
       @Override
       protected void update(float deltaTime) {
           // 战斗场景更新逻辑
       }
       
       @Override
       protected void render() {
           // 战斗场景渲染逻辑
       }
   }
   ```
   统一场景生命周期流程，减少重复代码。



2. **减少静态依赖**：`Game`类中大量使用`static`方法（如`Game.getPlayer()`），导致类之间强耦合。可改为通过依赖注入传递实例，而非直接调用静态方法。

## 1.9. 命令模式（Command）的应用
**现状**：输入事件与具体操作（如菜单选择、战斗动作）直接绑定（如`InputManager`中处理按键后直接调用`player`方法），难以扩展（如添加撤销功能）。

**重构建议**：
1. **定义命令接口**：
   ```java
   public interface Command {
       void execute();
   }
   ```

2. **实现具体命令**：
   ```java
   public class SelectUpCommand implements Command {
       private BeginMenuManager menuManager;
       
       public SelectUpCommand(BeginMenuManager menuManager) {
           this.menuManager = menuManager;
       }
       
       @Override
       public void execute() {
           menuManager.selectUp();
       }
   }
   
   public class ConfirmCommand implements Command {
       private BeginMenuManager menuManager;
       
       @Override
       public void execute() {
           menuManager.confirmSelection();
       }
   }
   ```

3. **输入管理器映射命令**：
   ```java
   public class InputManager {
       private Map<Integer, Command> keyCommands = new HashMap<>();
       
       public void mapKey(int key, Command command) {
           keyCommands.put(key, command);
       }
       
       public void processInput() {
           for (Map.Entry<Integer, Command> entry : keyCommands.entrySet()) {
               if (glfwGetKey(window, entry.getKey()) == GLFW_PRESS) {
                   entry.getValue().execute();
               }
           }
       }
   }
   ```
   新增操作只需添加`Command`实现，无需修改输入处理逻辑。



## 1.10. 总结
重构优先级建议：
1. 先用**工厂模式**解耦场景创建，解决新增场景需修改多处代码的问题。
2. 再用**状态模式**重构`UIManager`的菜单状态，消除冗长的`switch-case`。
3. 最后通过**观察者模式**和**命令模式**优化输入处理，降低模块间耦合。

这些重构将提高代码的可扩展性、可读性和可维护性，符合设计模式的**开闭原则**和**单一职责原则**。