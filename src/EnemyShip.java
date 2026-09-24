public class EnemyShip extends Ship {
    private final int gain;

    private EnemyShip(String name, int hull, int crew, int cannons, int armour, int gold, int morale, int gain, int n) {
        super(name, hull + 2 * n, cannons + n / 10, armour + n / 10,
            new Crew(crew + n / 2, morale), gold + 5 * n, Math.min(Crew.MAX_COUNT, crew + n / 2));
        this.gain = gain;
    }

    private static int check(int notoriety) {
        if (notoriety < 0) throw new IllegalArgumentException();
        return notoriety;
    }

    public static EnemyShip merchant(int n) { return new EnemyShip("Merchant", 40, 8, 1, 0, 150, 30, 1, check(n)); }
    public static EnemyShip pirate(int n) { return new EnemyShip("Pirate", 60, 15, 2, 1, 100, 60, 2, check(n)); }
    public static EnemyShip coastGuard(int n) { return new EnemyShip("Coast guard", 80, 20, 3, 2, 50, 80, 3, check(n)); }

    public int getGain() { return gain; }

    public String describe() {
        return String.format("A %s: hull %d, %d men, %d cannons, armour %d.", getName(), getHull(),
            getCrew().getCount(), getCannons(), getArmour());
    }
}
