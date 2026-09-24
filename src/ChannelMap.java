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
         * add every stop on the map, the key is what the player types in, the value is the location object;
         * the last argument of each is the description printed when the player arrives or types look
         */
        stops.put("southampton", new Port("southampton", "Southampton",
            "Wine casks from Gascony stand stacked on the quay below the Norman arch of the Bargate. Southampton Water gives the harbour two high tides a day."));
        stops.put("winchelsea", new Port("winchelsea", "Winchelsea",
            "A low town on a shingle bank at the mouth of the Brede, smelling of smoked herring. The sea takes a little more of the bank every winter."));
        stops.put("dover", new Port("dover", "Dover",
            "King Henry's great keep stands on the cliff above a harbour where the Dour runs out through the shingle. The king's passage boats to France load here."));
        stops.put("barfleur", new Port("barfleur", "Barfleur",
            "The harbour English kings sailed from to reach Normandy until the French took the duchy in 1204. Off the point lies the rock where the White Ship sank with the king's heir in 1120."));
        stops.put("dieppe", new Port("dieppe", "Dieppe",
            "A fishing town between chalk cliffs at the mouth of the Arques, its boats heaped with herring. The archbishop of Rouen has held it since 1197."));
        stops.put("boulogne", new Port("boulogne", "Boulogne",
            "The Roman lighthouse still stands on the cliff above the harbour. This is the count's town, and the count of Boulogne made you his steward before he declared you an outlaw."));
        stops.put("sark", new Port("sark", "Sark",
            "Cliffs on every side, and a single cleft in them where a boat can land. Your crew keeps a fire burning on the cliff top."));
        stops.put("southampton winchelsea", new Location("southampton winchelsea", "Southampton-Winchelsea lane", Location.SEA_LANE, SEA_LANE_CHANCE,
            "The English shore runs east past the low point of Selsey and the white chalk of Beachy Head."));
        stops.put("winchelsea dover", new Location("winchelsea dover", "Winchelsea-Dover lane", Location.SEA_LANE, SEA_LANE_CHANCE,
            "The coast bends past the flat Romney Marsh and the shingle point of Dungeness, where ships that hug the shore run aground."));
        stops.put("barfleur dieppe", new Location("barfleur dieppe", "Barfleur-Dieppe lane", Location.SEA_LANE, SEA_LANE_CHANCE,
            "The Norman shore runs east across the wide mouth of the Seine, the river road up to Rouen."));
        stops.put("dieppe boulogne", new Location("dieppe boulogne", "Dieppe-Boulogne lane", Location.SEA_LANE, SEA_LANE_CHANCE,
            "The chalk cliffs give way to sand dunes past the mouth of the Somme."));
        stops.put("barfleur sark", new Location("barfleur sark", "Barfleur-Sark run", Location.SEA_LANE, SEA_LANE_CHANCE,
            "West past the cape of La Hague, where the tide race between the cape and Alderney runs faster than a ship can row."));
        stops.put("southampton barfleur", new Location("southampton barfleur", "Southampton-Barfleur crossing", Location.SEA_LANE, SEA_LANE_CHANCE,
            "The old crossing between England and Normandy, little used since Normandy fell to France. The Isle of Wight drops behind to the north."));
        stops.put("winchelsea dieppe", new Location("winchelsea dieppe", "Winchelsea-Dieppe crossing", Location.SEA_LANE, SEA_LANE_CHANCE,
            "The widest stretch of the eastern crossing, with the Sussex shingle behind and the Norman chalk ahead."));
        stops.put("dover boulogne", new Location("dover boulogne", "Dover-Boulogne crossing", Location.SEA_LANE, SEA_LANE_CHANCE,
            "The shortest crossing to France. On a clear day the cliffs of both coasts are in sight."));
        stops.put("west channel", new Location("west channel", "West Channel", Location.HIGH_SEAS, HIGH_SEAS_CHANCE,
            "Open water west of the Isle of Wight, rolling with the swell from the ocean beyond."));
        stops.put("mid channel", new Location("mid channel", "Mid Channel", Location.HIGH_SEAS, HIGH_SEAS_CHANCE,
            "The middle of the Channel, with no coast in sight. A ship out here is lost or hunting."));
        stops.put("east channel", new Location("east channel", "East Channel", Location.HIGH_SEAS, HIGH_SEAS_CHANCE,
            "The narrows between Kent and the French coast, where the tide runs hard and the Goodwin shoals lie off the English side."));
        
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
     * home port getter helper for the PLayership constructor
     *
     * @return Sark
     */
    public Port getHome() {
        return (Port) stops.get("sark");
    }
}
