import java.util.HashMap;

/**
 * The map of the English Channel that the player sails around: 18 stops and the links
 * between them.
 *
 * <p>The map has three kinds of stop. There are 7 ports, 8 sea
 * lanes connecting the ports, and 3 high-seas locations. 
 * 
 * 
 * {@code sail <location>} moves the ship from a stop to a linked stop.
 * 
 */
public class ChannelMap {
    
    /**
     * probability of an encounter in a sea lane
     */
    private static final int SEA_LANE_CHANCE = 35;

    /**
     * probability of an encounter at the high seas
     */
    private static final int HIGH_SEAS_CHANCE = 60;

    /**
     * Every location on the map, key: what the player types, the value is the location object
     */
    private final HashMap<String, Location> stops = new HashMap<>();

    /**
     * Builds the game's map. It creates the 18 stops, links them as drawn in
     * {@code docs/channel-map.png}, with Sark as home. Every call returns a new map
     * with its own {@link Location} objects, so two games never share a map.
     *
     * @return a new map with all 18 stops linked
     */
    public static ChannelMap standard() {
        // One row per stop: {key the player types, name the game prints, third value}.
        // For a port the third value is its nation; for any other stop it is the stop's
        // kind, which sets its encounter chance.
        String[][] locations = {
            {"southampton", "Southampton", Port.ENGLISH},
            {"winchelsea", "Winchelsea", Port.ENGLISH},
            {"dover", "Dover", Port.ENGLISH},
            {"barfleur", "Barfleur", Port.FRENCH},
            {"dieppe", "Dieppe", Port.FRENCH},
            {"boulogne", "Boulogne", Port.FRENCH},
            {"sark", "Sark", Port.PIRATE},
            {"southampton winchelsea", "Southampton-Winchelsea lane", Location.SEA_LANE},
            {"winchelsea dover", "Winchelsea-Dover lane", Location.SEA_LANE},
            {"barfleur dieppe", "Barfleur-Dieppe lane", Location.SEA_LANE},
            {"dieppe boulogne", "Dieppe-Boulogne lane", Location.SEA_LANE},
            {"barfleur sark", "Barfleur-Sark run", Location.SEA_LANE},
            {"southampton barfleur", "Southampton-Barfleur crossing", Location.SEA_LANE},
            {"winchelsea dieppe", "Winchelsea-Dieppe crossing", Location.SEA_LANE},
            {"dover boulogne", "Dover-Boulogne crossing", Location.SEA_LANE},
            {"west channel", "West Channel", Location.HIGH_SEAS},
            {"mid channel", "Mid Channel", Location.HIGH_SEAS},
            {"east channel", "East Channel", Location.HIGH_SEAS}};

        // One row per link: the keys of two stops a single sail moves between. A lane
        // needs two rows, one to each of its ports. Each stop lists its neighbours in the
        // order its links appear here, which is the order "You can sail to:" prints them.
        String[][] links = {
            // English coast lanes
            {"southampton winchelsea", "southampton"}, {"southampton winchelsea", "winchelsea"},
            {"winchelsea dover", "winchelsea"}, {"winchelsea dover", "dover"},
            // French coast lanes
            {"barfleur dieppe", "barfleur"}, {"barfleur dieppe", "dieppe"},
            {"dieppe boulogne", "dieppe"}, {"dieppe boulogne", "boulogne"},
            // The run between Barfleur and Sark
            {"barfleur sark", "barfleur"}, {"barfleur sark", "sark"},
            // Lanes across the Channel
            {"southampton barfleur", "southampton"}, {"southampton barfleur", "barfleur"},
            {"winchelsea dieppe", "winchelsea"}, {"winchelsea dieppe", "dieppe"},
            {"dover boulogne", "dover"}, {"dover boulogne", "boulogne"},
            // High seas: each stretch touches its nearest ports and the stretch next to it
            {"west channel", "southampton"}, {"west channel", "sark"},
            {"west channel", "barfleur"}, {"west channel", "mid channel"},
            {"mid channel", "winchelsea"}, {"mid channel", "dieppe"},
            {"mid channel", "east channel"},
            {"east channel", "dover"}, {"east channel", "boulogne"}};

        ChannelMap map = new ChannelMap();

        // Turn each row into a stop object and store it under its key.
        for (String[] row : locations) {
            String key = row[0];
            String name = row[1];
            String kindOrNation = row[2];
            if (kindOrNation.equals(Location.SEA_LANE)) {
                map.add(new Location(key, name, Location.SEA_LANE, SEA_LANE_CHANCE));
            }
            else if (kindOrNation.equals(Location.HIGH_SEAS)) {
                map.add(new Location(key, name, Location.HIGH_SEAS, HIGH_SEAS_CHANCE));
            }
            else {
                map.add(new Port(key, name, kindOrNation));
            }
        }

        // Record each link on both of its stops.
        for (String[] link : links) {
            map.connect(link[0], link[1]);
        }

        return map;
    }

    /**
     * Stores a stop under its key. Only {@link #standard()} calls this.
     *
     * @param stop the stop to store
     */
    private void add(Location stop) {
        stops.put(stop.getKey(), stop);
    }

    /**
     * Links two stored stops, so each lists the other as a neighbour and a single sail
     * moves between them. Only {@link #standard()} calls this.
     *
     * @param keyA the key of one stop
     * @param keyB the key of the other stop
     */
    private void connect(String keyA, String keyB) {
        stops.get(keyA).connect(stops.get(keyB));
    }

    /**
     * Looks up a stop by the text the player typed. The text must match a key exactly,
     * so {@code "Dover"}, {@code "dover."} and {@code "dover-boulogne"} find nothing.
     *
     * @param name the text typed after {@code sail}
     * @return the stop with that key, or {@code null} if there is none
     */
    public Location find(String name) {
        return stops.get(name);
    }

    /**
     * Gets the player's home port, where the player starts and where {@code retire} is
     * allowed. {@link #standard()} always stores it under the key {@code "sark"}.
     *
     * @return Sark
     */
    public Port getHome() {
        return (Port) stops.get("sark");
    }
}
