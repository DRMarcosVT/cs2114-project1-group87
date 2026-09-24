public class Location {
    /**
     * Making the port type fields static and final constants ensures all instances of
     * Location have these values and they cannot be pointed to another object
     */
    public static final String PORT = "port";
    public static final String SEA_LANE = "sea lane";
    public static final String HIGH_SEAS = "high seas";

    //annot be pointed away
    //enforce max neighbours
    public static final int MAX_NEIGHBOURS = 4;
    private final String key;
    private final String name;
    private final String kind;
    private final int encounterPercent;
    //what the player sees on arriving, printed by describe()
    private final String description;
    //this is where .connect() adds neighbours
    private final Location[] neighbours = new Location[MAX_NEIGHBOURS];

    private int neighbourCount;
    /**
     * 
     * @param key what the player types
     * @param name what the game prints
     * @param kind port, high seas, etc.
     * @param encounterPercent percent chance that sailing in starts an encounter
     * @param description what the player sees on arriving, a sentence or two
     */
    public Location(String key, String name, String kind, int encounterPercent, String description) {
        this.key = key;
        this.name = name;
        this.kind = kind;
        this.encounterPercent = encounterPercent;
        this.description = description;
    }
    /**
     * symmetrically connect two stops
     * 
     * @param other the object of the method, the location being connected with
     */
    public void connect(Location other) {
        //if its already next to, return nothing, nothing to do
        if (isNextTo(other)) return;
        //cannot connect to itself, or this location's neighbour array it is at max capacity, or if the latter location's array is full
        if (other == this || neighbourCount == MAX_NEIGHBOURS || other.neighbourCount == MAX_NEIGHBOURS) {
            throw new IllegalArgumentException();
        }
        //place the location at the end of the 
        neighbours[neighbourCount++] = other;
        other.neighbours[other.neighbourCount++] = this;
    }
    //check if a location is already in the location's neighbour array
    public boolean isNextTo(Location other) {
        for (int i = 0; i < neighbourCount; i++) if (neighbours[i] == other) return true;
        return false;
    }
    /**
     * create a string that contains a node's neighbours, to be printed by the game
     * @return list the list of neighbours of a node
     */
    public String neighbourList() {
        String list = "";
        for (int i = 0; i < neighbourCount; i++) {
            if (i > 0) {
                list += ", ";
            }
            list += neighbours[i].key;
        }
        return list;
    }
    /**
     * describes the location the player is in, printed in the game
     * @return description of the location of the player
     */
    public String describe() { 
        return String.format("%s, %s. %s You can sail to: %s.", name, kind, description, neighbourList()); }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getKind() { return kind; }
    public int getEncounterPercent() { return encounterPercent; }
    public String getDescription() { return description; }
}
