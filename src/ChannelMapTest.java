import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ChannelMapTest {

    private static Location lane(String key) { return new Location(key, key, Location.SEA_LANE, 35, "Open water."); }

    @Test
    public void locationHoldsItsFields() {
        Location lane = lane("dover boulogne");
        assertEquals("dover boulogne", lane.getKey());
        assertEquals(Location.SEA_LANE, lane.getKind());
        assertEquals(35, lane.getEncounterPercent());
    }

    @Test
    public void connectLinksBothWaysOnce() {
        Location a = lane("a"), b = lane("b");
        a.connect(b);
        b.connect(a);
        assertTrue(a.isNextTo(b));
        assertTrue(b.isNextTo(a));
        assertEquals("b", a.neighbourList());
        assertEquals("a", b.neighbourList());
        assertFalse(a.isNextTo(lane("c")));
    }

    @Test
    public void connectRejectsSelfAndFifthLink() {
        Location a = lane("a");
        assertThrows(IllegalArgumentException.class, () -> a.connect(a));
        for (int i = 0; i < Location.MAX_NEIGHBOURS; i++) a.connect(lane("n" + i));
        assertThrows(IllegalArgumentException.class, () -> a.connect(lane("extra")));
    }

    @Test
    public void neighbourListKeepsConnectionOrder() {
        Port sark = new Port("sark", "Sark", "A harbour.");
        sark.connect(lane("barfleur sark"));
        sark.connect(new Location("west channel", "West Channel", Location.HIGH_SEAS, 60, "Open sea."));
        assertEquals("barfleur sark, west channel", sark.neighbourList());
        assertTrue(sark.describe().contains("barfleur sark, west channel"));
    }

    @Test
    public void standardMapHasEighteenLinkedStops() {
        ChannelMap map = new ChannelMap();
        assertEquals("sark", map.getHome().getKey());
        assertEquals("barfleur sark, west channel", map.getHome().neighbourList());
        String[] keys = {"southampton", "winchelsea", "dover", "barfleur", "dieppe", "boulogne", "sark",
            "southampton winchelsea", "winchelsea dover", "barfleur dieppe", "dieppe boulogne", "barfleur sark",
            "southampton barfleur", "winchelsea dieppe", "dover boulogne", "west channel", "mid channel", "east channel"};
        for (String key : keys) assertEquals(key, map.find(key).getKey());
        Location lane = map.find("dover boulogne");
        assertEquals(35, lane.getEncounterPercent());
        assertTrue(lane.isNextTo(map.find("dover")) && map.find("dover").isNextTo(lane));
        assertTrue(lane.isNextTo(map.find("boulogne")));
        assertTrue(lane.describe().contains("shortest crossing"));
        assertTrue(map.getHome().describe().contains("Cliffs on every side"));
        for (String key : keys) assertFalse(map.find(key).getDescription().isEmpty(), key);
        assertEquals(60, map.find("mid channel").getEncounterPercent());
        assertEquals(Location.PORT, map.find("dieppe").getKind());
    }

    @Test
    public void findIsExact() {
        ChannelMap map = new ChannelMap();
        assertEquals(Location.SEA_LANE, map.find("dover boulogne").getKind());
        for (String bad : new String[] {"dover now please", "dover-boulogne", "atlantis", "help", "dövér", "Dover", ""}) {
            assertNull(map.find(bad));
        }
    }

    @Test
    public void portPricesAndDescription() {
        Port dover = new Port("dover", "Dover", "A harbour.");
        Port sark = new Port("sark", "Sark", "A harbour.");
        assertEquals(Location.PORT, dover.getKind());
        assertEquals(0, dover.getEncounterPercent());
        assertEquals(4, dover.rumPrice());
        assertEquals(2, sark.rumPrice());
        assertEquals(200, dover.upgradePrice(2));
        assertTrue(dover.describe().contains("rum 4"));
        assertTrue(dover.describe().contains("Dover"));
        assertTrue(dover.describe().contains("A harbour."));
    }
}
