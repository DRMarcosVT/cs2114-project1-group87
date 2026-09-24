public class EnemyShip extends Ship {
    private final int gain;
    //what the lookout sees, printed by describe() when the ship is sighted and on look
    private final String description;

    private EnemyShip(String name, String description, int hull, int crew, int cannons, int armour, int gold, int morale, int gain, int n) {
        super(name, hull + 2 * n, cannons + n / 10, armour + n / 10,
            new Crew(Math.min(Crew.MAX_COUNT, crew + n / 2), morale), gold + 5 * n, Math.min(Crew.MAX_COUNT, crew + n / 2));
        this.gain = gain;
        this.description = description;
    }

    private static int check(int notoriety) {
        if (notoriety < 0) throw new IllegalArgumentException();
        return notoriety;
    }

    public static EnemyShip merchant(int n) {
        return new EnemyShip("Merchant",
            "A round-bellied cog rides low in the water under one square sail, her hold stacked with wine casks"
            + " from Gascony. A handful of sailors run along her deck, and the master is already shouting at them"
            + " to get the boy up the mast.",
            40, 8, 1, 0, 150, 30, 1, check(n));
    }

    public static EnemyShip pirate(int n) {
        return new EnemyShip("Pirate",
            "A long, low ship with her oars out and no banner comes on fast out of the haze. Men crowd her rail"
            + " with axes and grappling hooks, and nobody aboard her is calling for terms.",
            60, 15, 2, 1, 100, 60, 2, check(n));
    }

    public static EnemyShip coastGuard(int n) {
        return new EnemyShip("Coast guard",
            "A royal galley flying the king's three lions, two banks of oars beating in time and a castle of"
            + " planks raised at her stern for the archers. Her captain has a writ with your name on it.",
            80, 20, 3, 2, 50, 80, 3, check(n));
    }

    public int getGain() { return gain; }

    public String describe() {
        return String.format("A %s. %s Hull %d, %d men, %d cannons, armour %d.", getName(), description,
            getHull(), getCrew().getCount(), getCannons(), getArmour());
    }
}
