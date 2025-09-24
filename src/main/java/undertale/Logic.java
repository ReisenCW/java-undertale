package undertale;

public class Logic {
    private InputManager inputManager;
    private ObjectManager objectManager;

    Logic(InputManager inputManager, ObjectManager objectManager) {
        this.inputManager = inputManager;
        this.objectManager = objectManager;
    }

    public void update(float deltaTime) {
        objectManager.update(deltaTime);
    }
}
