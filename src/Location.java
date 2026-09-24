public class Location {
    public static final String PORT = "port";
    public static final String SEA_LANE = "sea lane";
    public static final String HIGH_SEAS = "high seas";
    public static final int MAX_NEIGHBOURS = 4;
    private final String key;
    private final String name;
    private final String kind;
    private final int encounterPercent;
    private final Location[] neighbours = new Location[MAX_NEIGHBOURS];
    private int neighbourCount;

    public Location(String key, String name, String kind, int encounterPercent) {
        this.key = key;
        this.name = name;
        this.kind = kind;
        this.encounterPercent = encounterPercent;
    }

    public void connect(Location other) {
        if (isNextTo(other)) return;
        if (other == this || neighbourCount == MAX_NEIGHBOURS || other.neighbourCount == MAX_NEIGHBOURS) {
            throw new IllegalArgumentException();
        }
        neighbours[neighbourCount++] = other;
        other.neighbours[other.neighbourCount++] = this;
    }

    public boolean isNextTo(Location other) {
        for (int i = 0; i < neighbourCount; i++) if (neighbours[i] == other) return true;
        return false;
    }

    public String neighbourList() {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < neighbourCount; i++) list.append(i > 0 ? ", " : "").append(neighbours[i].key);
        return list.toString();
    }

    public String describe() { return String.format("%s, %s. You can sail to: %s.", name, kind, neighbourList()); }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getKind() { return kind; }
    public int getEncounterPercent() { return encounterPercent; }
}
