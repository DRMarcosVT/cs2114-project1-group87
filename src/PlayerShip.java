public class PlayerShip extends Ship {
    public static final int MAX_LEVEL = 5;
    private Location location;
    private int notoriety;
    private int portsVisited;
    /**
     * constructor 
     * @param name name of the ship
     * @param home home port, initialised as sark
     */
    public PlayerShip(String name, Port home) {
        super(name, 100, 1, 1, new Crew(20, 70), 200, 30);
        location = home;
    }
    /**
     * move to a desired location
     * @param destination the place the user typed in
     */
    public void moveTo(Location destination) {
        if (!location.isNextTo(destination)) throw new IllegalArgumentException();
        location = destination;
        //if the location is a port, increment the ports visited value
        if (destination.getKind().equals(Location.PORT)) {
            portsVisited++;
        }
    }
    /**
     * generic purchase method
     * @param asked how much the player typed in to buy
     * @param price the cost of the purchase
     * @param room the room in inventroy
     * @return the amount purchased
     */
    private int buy(int asked, int price, int room) {
        //you can buy what you can afford, and no more than you can afford or have room for
        int units = Math.min(asked, Math.min(getGold() / price, room));
        spendGold(units * price);
        return units;
    }
    /**
     * repair the ship
     * @param points how much hp the player wants to repair
     * @param pricePerPoint the cost per hp point
     * @return the mount of hp bought
     */
    public int repair(int points, int pricePerPoint) {
        int bought = buy(points, pricePerPoint, getMaxHull() - getHull());
        setHull(getHull() + bought);
        return bought;
    }
    /**
     * hire more crew members
     * @param men the amount of men the player typed in
     * @param pricePerMan the cost per recruit
     * @return the number hired
     */
    public int hire(int men, int pricePerMan) {
        //by the buy() method
        //player can purchase the smallest of: the entered value, what they can afford, or
        //what they have room for
        //calls the hire method in Crew.java 
        return getCrew().hire(buy(men, pricePerMan, Crew.MAX_COUNT - getCrew().getCount()));
    }
    /**
     * player buys rum, there's unlimited room for room capped at the Integer max to
     * prevent overflow
     * @param bottles the player wants to buy
     * @param pricePerBottle price per bottle
     * @return the number of bottles bought
     */
    public int buyRum(int bottles, int pricePerBottle) {
        int bought = buy(bottles, pricePerBottle, Integer.MAX_VALUE - getRum());
        addRum(bought);
        return bought;
    }
    /**
     * upgrade cannons method
     * @param price cost per level up
     * @return if it upgraded or not
     */
    public boolean upgradeCannons(int price) {
        //cannot upgrade past max or past affordability
        if (getCannons() == MAX_LEVEL || getGold() < price) return false;

        spendGold(price);
        setCannons(getCannons() + 1);
        return true;
    }
    /**
     * upgrade armour method
     * @param price cost per level up
     * @return if it upgraded or not
     */
    public boolean upgradeArmour(int price) {
        //cannot upgrade past max level or affordability
        if (getArmour() == MAX_LEVEL || getGold() < price) return false;
        spendGold(price);
        setArmour(getArmour() + 1);
        return true;
    }
    /**
     * pay bonus to prevent mutinity
     * @param gold amount of gold paid
     * @return if it worked or not
     */
    public boolean payBonus(int gold) {
        //cannot pay more than you can afford
        if (gold > getGold()) return false;
        spendGold(gold);
        getCrew().receiveBonus(gold);
        return true;
    }
    /**
     * sinkinng a ship adds notoriety, affects rng
     * @param amount amount notority is increased by
     */
    public void addNotoriety(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        notoriety += amount;
    }
    /**
     * location, notority, ports visited getters
     */
    public Location getLocation() { return location; }
    public int getNotoriety() { return notoriety; }
    public int getPortsVisited() { return portsVisited; }

    /**    
     * message printed describing the status of the ship
     */
    public String describe() {
        Crew crew = getCrew();
        return String.format("Hull %d/%d. Crew %d, morale %d, greed %d. Cannons %d, armour %d. Rum %d. Gold %d. Notoriety %d. At %s.",
            getHull(), getMaxHull(), crew.getCount(), crew.getMorale(), crew.getGreed(), getCannons(), getArmour(),
            getRum(), getGold(), notoriety, location.getName());
    }
}
