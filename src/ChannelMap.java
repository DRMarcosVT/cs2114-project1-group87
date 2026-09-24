import java.util.HashMap;

/**
 * The map of the English Channel that the player sails around: 18 stops and the links
 * between them.
 *
 * The map has three kinds of stop. There are 7 ports, 8 sea
 * lanes connecting the ports, and 3 high-seas locations. 
 * 
 * 
 * Typing sail and a stop's key moves the ship to that stop if it is linked to the current one.
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
     * Builds the game's map: stores the 18 stops and links each to the stops one sail away.
     */
    public ChannelMap() {
        /**
         * add every stop on the map, the key is what the playertypes in, the value is the location object
         */
        stops.put("southampton", new Port("southampton", "Southampton", Port.ENGLISH));
        stops.put("winchelsea", new Port("winchelsea", "Winchelsea", Port.ENGLISH));
        stops.put("dover", new Port("dover", "Dover", Port.ENGLISH));
        stops.put("barfleur", new Port("barfleur", "Barfleur", Port.FRENCH));
        stops.put("dieppe", new Port("dieppe", "Dieppe", Port.FRENCH));
        stops.put("boulogne", new Port("boulogne", "Boulogne", Port.FRENCH));
        stops.put("sark", new Port("sark", "Sark", Port.PIRATE));
        stops.put("southampton winchelsea", new Location("southampton winchelsea", "Southampton-Winchelsea lane", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("winchelsea dover", new Location("winchelsea dover", "Winchelsea-Dover lane", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("barfleur dieppe", new Location("barfleur dieppe", "Barfleur-Dieppe lane", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("dieppe boulogne", new Location("dieppe boulogne", "Dieppe-Boulogne lane", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("barfleur sark", new Location("barfleur sark", "Barfleur-Sark run", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("southampton barfleur", new Location("southampton barfleur", "Southampton-Barfleur crossing", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("winchelsea dieppe", new Location("winchelsea dieppe", "Winchelsea-Dieppe crossing", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("dover boulogne", new Location("dover boulogne", "Dover-Boulogne crossing", Location.SEA_LANE, SEA_LANE_CHANCE));
        stops.put("west channel", new Location("west channel", "West Channel", Location.HIGH_SEAS, HIGH_SEAS_CHANCE));
        stops.put("mid channel", new Location("mid channel", "Mid Channel", Location.HIGH_SEAS, HIGH_SEAS_CHANCE));
        stops.put("east channel", new Location("east channel", "East Channel", Location.HIGH_SEAS, HIGH_SEAS_CHANCE));
        
        /**
         * Every location has its neigbours connected by adding them to their respective neighbours array
         * 
         * connects the Port object to a Location object symmetrically: southampton is a neighbour of southampton winchelsea and vice versa
         */
        stops.get("southampton").connect(stops.get("southampton winchelsea"));
        stops.get("southampton").connect(stops.get("southampton barfleur"));
        stops.get("southampton").connect(stops.get("west channel"));

        stops.get("winchelsea").connect(stops.get("southampton winchelsea"));
        stops.get("winchelsea").connect(stops.get("winchelsea dover"));
        stops.get("winchelsea").connect(stops.get("winchelsea dieppe"));
        stops.get("winchelsea").connect(stops.get("mid channel"));

        stops.get("dover").connect(stops.get("winchelsea dover"));
        stops.get("dover").connect(stops.get("dover boulogne"));
        stops.get("dover").connect(stops.get("east channel"));

        stops.get("barfleur").connect(stops.get("barfleur dieppe"));
        stops.get("barfleur").connect(stops.get("barfleur sark"));
        stops.get("barfleur").connect(stops.get("southampton barfleur"));
        stops.get("barfleur").connect(stops.get("west channel"));

        stops.get("dieppe").connect(stops.get("barfleur dieppe"));
        stops.get("dieppe").connect(stops.get("dieppe boulogne"));
        stops.get("dieppe").connect(stops.get("winchelsea dieppe"));
        stops.get("dieppe").connect(stops.get("mid channel"));

        stops.get("boulogne").connect(stops.get("dieppe boulogne"));
        stops.get("boulogne").connect(stops.get("dover boulogne"));
        stops.get("boulogne").connect(stops.get("east channel"));

        stops.get("sark").connect(stops.get("barfleur sark"));
        stops.get("sark").connect(stops.get("west channel"));

        stops.get("west channel").connect(stops.get("mid channel"));

        stops.get("mid channel").connect(stops.get("east channel"));
    }

    /**
     * 
     * looks up a stop in the stops registry, returns the value of the key
     *
     * @param name the text typed after sail
     * @return the stop with that key, null if there is none
     */
    public Location find(String name) {
        return stops.get(name);
    }

    /**
     * Gets the player's home port, where the player starts and where retire is
     * allowed. The constructor always stores it under the key "sark".
     * 
     * 
     *
     * @return Sark
     */
    public Port getHome() {
        return (Port) stops.get("sark");
    }
}
