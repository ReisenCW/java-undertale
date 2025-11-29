package undertale.Interfaces;

// 使用observer模式重构输入处理
public interface InputObserver {
    public void processInput(boolean[] preKeyStates, boolean[] currKeyStates);
}
