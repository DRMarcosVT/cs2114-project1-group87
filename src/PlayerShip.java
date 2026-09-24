public class PlayerShip extends Ship {
    public static final int MAX_LEVEL = 5;
    private Location location;
    private int notoriety;
    private int portsVisited;

    public PlayerShip(String name, Port home) {
        super(name, 100, 1, 1, new Crew(20, 70), 200, 30);
        if (home == null) throw new IllegalArgumentException();
        location = home;
    }

    public void moveTo(Location destination) {
        if (!location.isNextTo(destination)) throw new IllegalArgumentException();
        location = destination;
        if (destination.getKind().equals(Location.PORT)) portsVisited++;
    }

    private int buy(int asked, int price, int room) {
        int units = Math.min(asked, Math.min(getGold() / price, room));
        spendGold(units * price);
        return units;
    }

    public int repair(int points, int pricePerPoint) {
        int bought = buy(points, pricePerPoint, getMaxHull() - getHull());
        setHull(getHull() + bought);
        return bought;
    }

    public int hire(int men, int pricePerMan) {
        return getCrew().hire(buy(men, pricePerMan, Crew.MAX_COUNT - getCrew().getCount()));
    }

    public int buyRum(int bottles, int pricePerBottle) {
        int bought = buy(bottles, pricePerBottle, Integer.MAX_VALUE - getRum());
        addRum(bought);
        return bought;
    }

    public boolean upgradeCannons(int price) {
        if (getCannons() == MAX_LEVEL || getGold() < price) return false;
        spendGold(price);
        setCannons(getCannons() + 1);
        return true;
    }

    public boolean upgradeArmour(int price) {
        if (getArmour() == MAX_LEVEL || getGold() < price) return false;
        spendGold(price);
        setArmour(getArmour() + 1);
        return true;
    }

    public boolean payBonus(int gold) {
        if (gold > getGold()) return false;
        spendGold(gold);
        getCrew().receiveBonus(gold);
        return true;
    }

    public void addNotoriety(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        notoriety += amount;
    }

    public Location getLocation() { return location; }
    public int getNotoriety() { return notoriety; }
    public int getPortsVisited() { return portsVisited; }

    public String describe() {
        Crew crew = getCrew();
        return String.format("Hull %d/%d. Crew %d, morale %d, greed %d. Cannons %d, armour %d. Rum %d. Gold %d. Notoriety %d. At %s.",
            getHull(), getMaxHull(), crew.getCount(), crew.getMorale(), crew.getGreed(), getCannons(), getArmour(),
            getRum(), getGold(), notoriety, location.getName());
    }
}
