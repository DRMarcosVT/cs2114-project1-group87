public class Port extends Location {
    public static final String ENGLISH = "English";
    public static final String FRENCH = "French";
    public static final String PIRATE = "pirate";
    public static final int REPAIR_PRICE = 2;
    public static final int HIRE_PRICE = 15;
    public static final int RUM_PRICE = 4;
    public static final int UPGRADE_PRICE = 100;
    private final String nation;

    public Port(String key, String name, String nation) {
        super(key, name, PORT, 0);
        this.nation = nation;
    }

    public String getNation() { return nation; }
    public int rumPrice() { return nation.equals(PIRATE) ? RUM_PRICE / 2 : RUM_PRICE; }
    public int upgradePrice(int nextLevel) { return UPGRADE_PRICE * nextLevel; }

    public String describe() {
        return String.format("%s, %s port. You can sail to: %s. Repair %d gold a point, hire %d a man, rum %d a bottle, upgrades %d times the level.",
            getName(), nation, neighbourList(), REPAIR_PRICE, HIRE_PRICE, rumPrice(), UPGRADE_PRICE);
    }
}
