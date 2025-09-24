package undertale;

public class Logic {
    private ObjectManager objectManager;

    Logic(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void update(float deltaTime) {
        objectManager.update(deltaTime);
    }
}
