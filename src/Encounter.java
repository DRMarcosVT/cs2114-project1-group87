import java.util.Random;

public class Encounter {
    public static final int FLEE_DAMAGE = 10;
    public static final int ROLL_RANGE = 6;
    private final PlayerShip player;
    private final EnemyShip enemy;
    private final Random random;

    /**
     * constructor for the class
     * @param player the player's ship object
     * @param enemy the enemyship object
     * @param random rng
     */
    public Encounter(PlayerShip player, EnemyShip enemy, Random random) {
        if (player == null || enemy == null || random == null) throw new IllegalArgumentException();
        this.player = player;
        this.enemy = enemy;
        this.random = random;
    }
    /**
     * rng decides if an encounter happens and what kind it is
     * @param where location of the encounter
     * @param player player ship instance
     * @param random rng
     * @return encounter object the combat happens with
     */
    public static Encounter roll(Location where, PlayerShip player, Random random) {
        //if the roll is outside the probability range of encounter, nothing happens
        if (random.nextInt(100) >= where.getEncounterPercent()) return null;
        int type = random.nextInt(100);
        int n = player.getNotoriety();
        /**
         * if in a sea lane: ~60% chance of a merchant, ~40% chance of coast guard
         * if in high seas: ~50% change of a pirate, ~50% chance of a merchant
         */
        EnemyShip enemy = where.getKind().equals(Location.SEA_LANE)
            ? (type < 60 ? EnemyShip.merchant(n) : EnemyShip.coastGuard(n))
            : (type < 50 ? EnemyShip.pirate(n) : EnemyShip.merchant(n));
        return new Encounter(player, enemy, random);
    }

    public String fight() {
        StringBuilder log = new StringBuilder();
        for (int round = 1; !player.isDefeated() && !enemy.isDefeated(); round++) {
            log.append("Round ").append(round).append(": you deal ")
                .append(enemy.takeDamage(player.attackStrength() + random.nextInt(ROLL_RANGE)));
            if (!enemy.isDefeated()) {
                log.append(", they deal ").append(player.takeDamage(enemy.attackStrength() + random.nextInt(ROLL_RANGE)));
            }
            log.append(".\n");
        }
        if (!enemy.isDefeated()) return log.append("You are beaten.").toString();
        int plunder = enemy.getGold();
        player.addGold(plunder);
        player.addRum(enemy.getRum());
        player.addNotoriety(enemy.getNotorietyGain());
        player.getCrew().onWin(plunder);
        return log.append(enemy.getHull() > 0 ? "Their crew surrenders." : "They sink.")
            .append(" Plunder: ").append(plunder).append(" gold, ").append(enemy.getRum()).append(" rum.").toString();
    }

    public String flee() {
        int lost = player.takeDamage(FLEE_DAMAGE);
        player.getCrew().onFlee();
        return "You run. Parting shots cost " + lost + " hull and the crew grumbles.";
    }

    public EnemyShip getEnemy() { return enemy; }
}
