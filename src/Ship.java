public abstract class Ship {
    private final String name;
    private int hull;
    private final int maxHull;
    private int cannons;
    private int armour;
    private int gold;
    private int rum;
    private final Crew crew;

    public Ship(String name, int maxHull, int cannons, int armour, Crew crew, int gold, int rum) {
        if (maxHull <= 0 || crew == null) throw new IllegalArgumentException();
        this.name = name;
        this.maxHull = maxHull;
        this.hull = maxHull;
        this.crew = crew;
        this.gold = Math.max(0, gold);
        this.rum = Math.max(0, rum);
        setCannons(cannons);
        setArmour(armour);
    }

    public int attackStrength() {
        return Math.max(1, (int) ((5 * cannons + crew.getCount() / 2) * crew.moraleFactor()));
    }

    public int takeDamage(int raw) {
        if (raw < 0) throw new IllegalArgumentException();
        int through = raw == 0 ? 0 : Math.max(1, raw - 2 * armour);
        int lost = Math.min(hull, through);
        hull -= lost;
        crew.lose(through / 5);
        return lost;
    }

    public boolean isDefeated() { return hull == 0 || crew.getCount() == 0 || crew.mutinied(); }

    public void addGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        gold += amount;
    }

    public void spendGold(int amount) {
        if (amount < 0 || amount > gold) throw new IllegalArgumentException();
        gold -= amount;
    }

    public void addRum(int bottles) {
        if (bottles < 0) throw new IllegalArgumentException();
        rum += bottles;
    }

    public int takeRum(int bottles) {
        if (bottles < 0) throw new IllegalArgumentException();
        int taken = Math.min(bottles, rum);
        rum -= taken;
        return taken;
    }

    void setHull(int value) { hull = Math.max(0, Math.min(maxHull, value)); }
    void setCannons(int value) { cannons = Math.max(1, Math.min(5, value)); }
    void setArmour(int value) { armour = Math.max(0, Math.min(5, value)); }

    public String getName() { return name; }
    public int getHull() { return hull; }
    public int getMaxHull() { return maxHull; }
    public int getCannons() { return cannons; }
    public int getArmour() { return armour; }
    public int getGold() { return gold; }
    public int getRum() { return rum; }
    public Crew getCrew() { return crew; }

    public abstract String describe();
}
