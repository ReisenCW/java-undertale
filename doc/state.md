# Behavioral – State Pattern

* The **State Pattern** is a behavioral design pattern that allows an object to alter its behavior when its internal state changes. The object appears to change its class without modifying the main class logic.

* Refactored files:

  * `UIManager.java` (core refactor)
  * `BattleMenuScene.java`
  * `BeginMenuScene.java`
  * `GameOverScene.java`
  * `Game.java`

* New files:

  * `MenuState.java` (state interface)
  * `AbstractMenuState.java` (base class for shared logic)
  * `MenuStateContext.java` (context managing state transitions)
  * `StateFactory.java` (creates and caches states)
  * `MenuStateType.java` (state enum)
  * Various concrete menu state classes (e.g., `MainMenuState`, `FightState`, `ItemState`, `MercyState`, etc.)

* Before Refactoring, `UIManager` used an enum and large `switch` statements to handle menu behavior. This led to difficult extendability (adding a new menu state required code modifications in multiple places) and hard-to-test menu logic. An example previous structure:

```java
public void handleMenuSelect() {
    switch(menuState) {
        case MAIN -> {
            // select FIGHT, ACT, ITEM, MERCY...
        }
        case FIGHT_SELECT_ENEMY -> {
            // confirm target...
        }
        // ... many more cases
    }
}
```

* After Refactoring, Each menu mode is now encapsulated in its own class implementing a unified `MenuState` interface.

```java
public interface MenuState {
    void handleSelect(MenuStateContext context);
    void handleCancel(MenuStateContext context);
    void handleSelectUp(MenuStateContext context);
    void handleSelectDown(MenuStateContext context);
    void renderFrameContents(MenuStateContext context, String roundText);
    MenuStateType getStateType();
}
```

A simplified example of a concrete state:

```java
public class MainMenuState extends AbstractMenuState {
    @Override
    public void handleSelect(MenuStateContext context) {
        MenuStateType next = switch(context.selectedAction) {
            case 0 -> MenuStateType.FIGHT_SELECT_ENEMY;
            case 1 -> MenuStateType.ACT_SELECT_ENEMY;
            case 2 -> MenuStateType.ITEM_SELECT_ITEM;
            case 3 -> MenuStateType.MERCY_SELECT_ENEMY;
            default -> MenuStateType.MAIN;
        };
        context.setState(StateFactory.createState(next));
    }
}
```

`UIManager` becomes a simple delegator:

```java
public void handleMenuSelect() {
    context.getCurrentState().handleSelect(context);
}
```

Uses a cache to avoid creating duplicate state instances:

```java
static {
    stateCache.put(MenuStateType.MAIN, new MainMenuState());
    // preload other states...
}

public static MenuState createState(MenuStateType type) {
    return stateCache.get(type);
}
```

* **Benefits**:
  * Before the refactor, UIManager contained large switch statements, tightly handled all state logic itself, was difficult to extend with new states, and hard to test due to strong coupling.
  * After applying the State Pattern, all menu behaviors are moved into dedicated state classes, eliminating switch usage in UIManager, improving modularity and maintainability.
  * New states can now be added safely without modifying existing code, and each state can be independently tested thanks to the loosely-coupled design.

  * New code follows **Open/Closed Principle**, Supports behavior extension without modifying UIManager. Logic is modular and maintainable

- Class diagram after using state pattern

![state](./uml/state.puml)