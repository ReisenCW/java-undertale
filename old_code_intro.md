### **1. 核心模块（GameMain）**
负责游戏初始化、主循环控制、全局状态管理，是整个游戏的入口和中枢。

- **Game**  
  游戏主类，包含`main`入口（`run()`方法），负责：  
  - 初始化所有核心管理器（配置、纹理、场景、输入等）；  
  - 注册场景并设置初始场景；  
  - 启动主循环（`loop()`），协调更新（`update()`）和渲染（`render()`）逻辑；  
  - 提供全局访问接口（如获取窗口尺寸、玩家对象、资源等）。

- **Renderer**  
  渲染管理器，封装OpenGL渲染逻辑：  
  - 初始化OpenGL上下文（启用混合、设置视口）；  
  - 调用当前场景的渲染方法，叠加屏幕淡入淡出效果（`ScreenFadeManager`）；  
  - 处理特殊渲染（如"ESCAPING..."文本）。

- **Window**  
  窗口管理类（代码未完全提供），通过GLFW创建和管理游戏窗口，提供窗口句柄访问。

- **InputManager**  
  输入处理类，监听键盘/鼠标输入，维护输入状态（如按键按下、逃跑计时），并将输入传递给玩家或UI系统。


### **2. 场景管理（Scene）**
负责游戏场景的切换、生命周期管理，不同场景对应不同游戏阶段（如菜单、战斗、游戏结束）。

- **Scene**  
  场景基类，定义场景生命周期方法：  
  - 构造函数注入依赖（`ObjectManager`、`InputManager`）；  
  - 包含场景管理器（`SceneManager`）、UI管理器（`UIManager`）等核心组件引用；  
  - 抽象方法`update()`（帧更新）、`render()`（渲染）、`onEnter()`（进入场景）、`onExit()`（退出场景）。

- **SceneManager**  
  单例模式，场景全局管理器：  
  - 注册场景（`registerScene()`）、切换场景（`switchScene()`），维护当前场景；  
  - 管理场景切换状态（`shouldSwitch`），触发场景生命周期方法（进入/退出）；  
  - 提供场景查询接口（`getCurrentScene()`、`getScene()`）。

- **具体场景类**  
  继承`Scene`，实现特定场景逻辑：  
  - `BeginMenuScene`：初始菜单场景；  
  - `BattleMenuScene`：战斗菜单场景（选择战斗/行动/物品/饶恕）；  
  - `BattleFightScene`：战斗执行场景(躲避弹幕)；  
  - `GameOverScene`：死亡场景；  

- **回合类**
- 基类：Round
- 子类：RoundSwarm, RoundSnake, RoundFingers, RoundSpecial
- 每个具体回合子类实现一个具体的回合, 通过intensity控制难度, 在BattleFightScene中被使用
- BattleFightScene中通过phase和round循环控制回合与intensity


### **3. 游戏对象（GameObject）模块**
管理游戏中所有实体（玩家、敌人、子弹、特效等）的创建、更新和销毁。

- **ObjectManager**  
  游戏对象管理器，维护各类实体列表（如子弹、敌人）：  
  - 提供对象添加（如`addBullet()`）、移除、重置（`resetGame()`）接口；  
  - 关联玩家对象（`Player`），作为实体交互的核心。

- **Player**  
- GameObject的子类，玩家类，存储玩家状态（生命值、TP、物品栏）。

- **Bullet**
- GameObject的子类，表示游戏中的子弹/攻击实体
- Bullet有多个子类, 每个子类实现不同的子弹行为和渲染方式
  - BallBlast, BallSmall, TitanFingers, TitanSnake, TitanSpawn, TitanSpawnRed

- **Collectables**
- GameObject的子类, 表示游戏中的可收集物品(当前游戏中只有一个TP点)

### **4. 资源管理**
负责纹理、字体、声音、着色器等资源的加载、缓存和释放，避免重复加载。

- **TextureManager**  
  纹理管理器（单例），加载并缓存图片资源：  
  - 从配置文件（`ConfigManager`）读取纹理路径，通过`getTexture()`提供纹理访问；  
  - 封装OpenGL纹理创建逻辑。

- **FontManager**  
  字体管理器（单例），加载并缓存字体：  
  - 从配置文件读取字体路径，使用`stb`库烘焙字体位图；  
  - 提供文本绘制（`drawText()`）、文本宽度计算（`getTextWidth()`）接口。

- **SoundManager**  
  声音管理器（单例），预加载并播放音效（SE）和音乐：  
  - 从配置文件读取音频路径，缓存音频剪辑（`seCache`、`musicCache`）；  
  - 提供播放接口（`playSE()`），处理音频加载错误。

- **ShaderManager**  
  着色器管理器（单例），负责着色器程序的编译、链接：  
  - 从配置文件读取着色器路径，封装`glLinkProgram()`等OpenGL接口；  
  - 处理着色器链接错误，抛出运行时异常。


### **5. Enemy模块 **
- **EnemyManager**  
  敌人管理器，负责敌人对象的创建和管理：  
  - 提供敌人添加（`addEnemy()`）、移除、重置接口；  
  - 维护敌人列表的update和render逻辑
  - 提供获取敌人的接口
- **Enemy**
  - 敌人基类，包含敌人动画，血量，掉落exp, gold等属性
  - 包含act类，在BattleMenuScene中选择Act按钮以及对应的敌人后调用


### **5. UI系统（UI）**
负责游戏界面渲染和交互逻辑，包括菜单、状态栏、游戏结束界面等。

- **UIBase**  
  UI基类，定义通用UI常量：  
  - 窗口边距（`TOP_MARGIN`、`BOTTOM_MARGIN`）、UI尺寸（按钮大小、菜单框架尺寸）；  
  - 依赖`ConfigManager`初始化UI参数（从配置文件读取）。

- **UIManager**  
  主UI管理器（单例），处理菜单逻辑：  
  - 维护菜单状态（`MenuState`：主菜单、战斗选择、物品选择等）；  
  - 处理菜单选择事件（`handleMenuSelect()`），触发对应逻辑（如战斗、使用物品、逃跑）；  
  - 管理打字机效果（`TypeWriter`），控制文本逐字显示。

- **专项UI管理器**  
  - `BgUIManager`：渲染背景UI（如张力条`TensionBar`）；  
  - `GameOverUIManager`：游戏结束界面，处理心跳破碎动画、文字渲染；  
  - `ScreenFadeManager`：屏幕淡入淡出效果，用于场景切换过渡。


### **6. 工具类（Utils）**
提供配置解析、计时等通用功能。

- **ConfigManager**  
  配置管理器，解析`config.json`：  
  - 读取窗口尺寸、UI参数（按钮大小、边距）、资源路径（纹理、字体、音频、着色器）；  
  - 提供默认配置，处理文件读取失败的降级逻辑。

