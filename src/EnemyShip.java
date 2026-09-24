public class EnemyShip extends Ship {
    private final int notorietyGain;
    //what the lookout sees, printed by describe() when the ship is sighted and on look
    private final String description;

    /**
     *  enemy ship constructor
     * @param name name of ship
     * @param description description text of ship
     * @param baseHull hp of ship at notoriety 0, scaled by notoriety
     * @param baseCrew crew number of ship at notoriety 0
     * @param baseCannons cannon number in ship at notoriety 0
     * @param baseArmour armour in ship at notoriety 0
     * @param baseGold gold in ship at notoriety 0
     * @param morale morale of enemy crew
     * @param notorietyForWin notoriety the player gains for beating this ship
     * @param notoriety the player's notoriety, which makes the ship stronger and richer
     */
    private EnemyShip(String name, String description, int baseHull, int baseCrew, int baseCannons,
        int baseArmour, int baseGold, int morale, int notorietyForWin, int notoriety) {
        // Each argument below fills the Ship constructor parameter named in its comment.
        super(name,                                                          // name
            baseHull + 2 * notoriety,                                        // maxHull
            baseCannons + notoriety / 10,                                    // cannons
            baseArmour + notoriety / 10,                                     // armour
            new Crew(Math.min(Crew.MAX_COUNT, baseCrew + notoriety / 2), morale), // crew, at most 40 men
            baseGold + 5 * notoriety,                                        // gold
            Math.min(Crew.MAX_COUNT, baseCrew + notoriety / 2));             // rum: one bottle per man
        this.notorietyGain = notorietyForWin;
        this.description = description;
    }
    /**
     * non negative notority
     * @param notoriety notoriety of player
     * @return notority of player
     */
    private static int check(int notoriety) {
        if (notoriety < 0) {
            throw new IllegalArgumentException();
        }
        return notoriety;
    }

    /**
     * methods that return a merchant, coast guard, or pirate enemyship object
     */

    public static EnemyShip merchant(int notoriety) {
        return new EnemyShip("Merchant",
            "A round-bellied cog rides low in the water under one square sail, her hold stacked with wine casks"
            + " from Gascony. A handful of sailors run along her deck, and the master is already shouting at them"
            + " to get the boy up the mast.",
            // hull, crew, cannons, armour, gold, morale, notoriety for a win, player notoriety
            40, 8, 1, 0, 150, 30, 1, check(notoriety));
    }

    public static EnemyShip pirate(int notoriety) {
        return new EnemyShip("Pirate",
            "A long, low ship with her oars out and no banner comes on fast out of the haze. Men crowd her rail"
            + " with axes and grappling hooks, and nobody aboard her is calling for terms.",
            // hull, crew, cannons, armour, gold, morale, notoriety for a win, player notoriety
            60, 15, 2, 1, 100, 60, 2, check(notoriety));
    }

    public static EnemyShip coastGuard(int notoriety) {
        return new EnemyShip("Coast guard",
            "A royal galley flying the king's three lions, two banks of oars beating in time and a castle of"
            + " planks raised at her stern for the archers. Her captain has a writ with your name on it.",
            // hull, crew, cannons, armour, gold, morale, notoriety for a win, player notoriety
            80, 20, 3, 2, 50, 80, 3, check(notoriety));
    }

    public int getNotorietyGain() { return notorietyGain; }

    /**    (non-Javadoc)
     * 
     * description of the ship printed in the game
     * 
     * format() string method is like f strings in python
     * @return description of the ship with hp, crew, cannons, and armour
     */
    public String describe() {
        return String.format("A %s. %s Hull %d, %d men, %d cannons, armour %d.", getName(), description,
            getHull(), getCrew().getCount(), getCannons(), getArmour());
    }
}
