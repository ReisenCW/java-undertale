package undertale.Interfaces;

public interface InputObserver {
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates);
}
