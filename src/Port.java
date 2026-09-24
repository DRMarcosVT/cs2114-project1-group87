/**
 * Subclass of Location, specifically for ports, specific fields 
 */
public class Port extends Location {
    public static final int REPAIR_PRICE = 2;
    public static final int HIRE_PRICE = 15;
    public static final int RUM_PRICE = 4;
    public static final int UPGRADE_PRICE = 100;

    public Port(String key, String name, String description) {
        super(key, name, PORT, 0, description);
    }

    //get the rum price of the port
    public int rumPrice() { 
        //if the port is sark, the rum is half price
        return getKey().equals("sark") ? RUM_PRICE / 2 : RUM_PRICE; }
    //upgrade price is a linear equation with intercept 0 and slope: upgrade price
    public int upgradePrice(int nextLevel) { 
        return UPGRADE_PRICE * nextLevel; }

    // string of the more interesting description plus the more boring stats about the location
    public String describe() {
        return super.describe() + String.format(" Repair %d gold a point, hire %d a man, rum %d a bottle, upgrades %d times the level.",
            REPAIR_PRICE, HIRE_PRICE, rumPrice(), UPGRADE_PRICE);
    }
}
