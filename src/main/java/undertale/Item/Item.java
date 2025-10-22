package undertale.Item;

public class Item {
    private String name;
    private String additionalDescription;
    private int healingAmount;

    public Item(String name, String additionalDescription, int healingAmount) {
        this.name = name;
        this.additionalDescription = additionalDescription;
        this.healingAmount = healingAmount;
    }

    public String getName() {
        return name;
    }

    public String getAdditionalDescription() {
        return additionalDescription;
    }

    public int getHealingAmount() {
        return healingAmount;
    }
}
    