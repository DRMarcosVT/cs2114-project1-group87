import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EncounterTest {
    private ChannelMap map;
    private PlayerShip player;

    @BeforeEach
    public void setUp() {
        map = new ChannelMap();
        player = new PlayerShip("Eustace", map.getHome());
    }

    @Test
    public void constructorRejectsNulls() {
        assertThrows(IllegalArgumentException.class, () -> new Encounter(player, null, new FixedRandom(0)));
        assertThrows(IllegalArgumentException.class, () -> new Encounter(null, EnemyShip.merchant(0), new FixedRandom(0)));
    }

    @Test
    public void rollPicksByStopAndDraw() {
        Encounter lane = Encounter.roll(map.find("barfleur sark"), player, new FixedRandom(0));
        assertEquals("Merchant", lane.getEnemy().getName());
        assertEquals("Coast guard", Encounter.roll(map.find("barfleur sark"), player, new FixedRandom(34, 70)).getEnemy().getName());
        assertEquals("Merchant", Encounter.roll(map.find("barfleur sark"), player, new FixedRandom(34, 69)).getEnemy().getName());
        assertEquals("Pirate", Encounter.roll(map.find("west channel"), player, new FixedRandom(0)).getEnemy().getName());
        assertEquals("Merchant", Encounter.roll(map.find("west channel"), player, new FixedRandom(59)).getEnemy().getName());
        assertNull(Encounter.roll(map.find("sark"), player, new FixedRandom(0)));
        assertNull(Encounter.roll(map.find("west channel"), player, new FixedRandom(99)));
        assertNull(Encounter.roll(map.find("barfleur sark"), player, new FixedRandom(35)));
    }

    @Test
    public void fightWinsAgainstMerchant() {
        String log = new Encounter(player, EnemyShip.merchant(0), new FixedRandom(0)).fight();
        assertTrue(log.contains("Round 3"));
        assertFalse(log.contains("Round 4"));
        assertTrue(log.contains("surrenders"));
        assertEquals(97, player.getHull());
        assertEquals(350, player.getGold());
        assertEquals(38, player.getRum());
        assertEquals(80, player.getCrew().getMorale());
        assertEquals(7, player.getCrew().getGreed());
        assertEquals(1, player.getNotoriety());
    }

    @Test
    public void fightSinksEnemyWhoseCrewStillStands() {
        player.setCannons(5);
        String log = new Encounter(player, EnemyShip.merchant(0), new FixedRandom(0)).fight();
        assertTrue(log.contains("They sink"));
        assertEquals(350, player.getGold());
    }

    @Test
    public void fightLostOnLowHull() {
        player.setHull(5);
        String log = new Encounter(player, EnemyShip.pirate(0), new FixedRandom(0)).fight();
        assertTrue(log.contains("Round 1"));
        assertFalse(log.contains("Round 2"));
        assertTrue(log.contains("beaten"));
        assertEquals(0, player.getHull());
        assertEquals(200, player.getGold());
        assertTrue(player.isDefeated());
    }

    @Test
    public void flee() {
        Encounter e = new Encounter(player, EnemyShip.merchant(0), new FixedRandom(0));
        e.flee();
        assertEquals(92, player.getHull());
        assertEquals(19, player.getCrew().getCount());
        assertEquals(50, player.getCrew().getMorale());
        assertEquals(200, player.getGold());
        player.setHull(5);
        e.flee();
        assertEquals(0, player.getHull());
    }
}
