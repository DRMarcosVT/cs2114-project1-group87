import java.util.Random;

public class Encounter {
    public static final int FLEE_DAMAGE = 10;
    public static final int ROLL_RANGE = 6;
    private final PlayerShip player;
    private final EnemyShip enemy;
    private final Random random;

    public Encounter(PlayerShip player, EnemyShip enemy, Random random) {
        if (player == null || enemy == null || random == null) throw new IllegalArgumentException();
        this.player = player;
        this.enemy = enemy;
        this.random = random;
    }

    public static Encounter roll(Location where, PlayerShip player, Random random) {
        if (random.nextInt(100) >= where.getEncounterPercent()) return null;
        int type = random.nextInt(100);
        int n = player.getNotoriety();
        EnemyShip enemy = where.getKind().equals(Location.SEA_LANE)
            ? (type < 70 ? EnemyShip.merchant(n) : EnemyShip.coastGuard(n))
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
        player.addNotoriety(enemy.getGain());
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
