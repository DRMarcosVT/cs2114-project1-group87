import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ShipTest {

    private static Ship fresh() {
        return new Ship("Eustace", 100, 1, 1, new Crew(20, 70), 200, 30) {
            public String describe() { return ""; }
        };
    }

    @Test
    public void constructorStartsAtFullHullAndClampsLevels() {
        Ship small = new Ship("Boat", 40, 0, 9, new Crew(5, 50), 0, 0) {
            public String describe() { return ""; }
        };
        assertEquals(40, small.getHull());
        assertEquals(40, small.getMaxHull());
        assertEquals(1, small.getCannons());
        assertEquals(5, small.getArmour());
        assertThrows(IllegalArgumentException.class, () -> new Ship("x", 0, 1, 1, new Crew(1, 1), 0, 0) {
            public String describe() { return ""; }
        });
        assertThrows(IllegalArgumentException.class, () -> new Ship("x", 10, 1, 1, null, 0, 0) {
            public String describe() { return ""; }
        });
    }

    @Test
    public void attackStrength() {
        assertEquals(12, fresh().attackStrength());
        Ship emptied = fresh();
        emptied.getCrew().lose(20);
        assertEquals(2, emptied.attackStrength());
        Ship lowMorale = new Ship("x", 100, 1, 1, new Crew(20, 0), 0, 0) {
            public String describe() { return ""; }
        };
        assertEquals(7, lowMorale.attackStrength());
    }

    @Test
    public void takeDamage() {
        Ship ship = fresh();
        assertEquals(10, ship.takeDamage(12));
        assertEquals(90, ship.getHull());
        assertEquals(18, ship.getCrew().getCount());
        assertEquals(60, ship.getCrew().getMorale());
        assertEquals(1, ship.takeDamage(1));
        assertEquals(0, ship.takeDamage(0));
        assertEquals(89, ship.getHull());
        assertEquals(89, ship.takeDamage(500));
        assertEquals(0, ship.getHull());
        assertThrows(IllegalArgumentException.class, () -> ship.takeDamage(-1));
    }

    @Test
    public void isDefeated() {
        Ship ship = fresh();
        assertFalse(ship.isDefeated());
        ship.setHull(0);
        assertTrue(ship.isDefeated());
        Ship mutiny = fresh();
        mutiny.getCrew().onWin(5000);
        assertTrue(mutiny.isDefeated());
    }

    @Test
    public void gold() {
        Ship ship = fresh();
        ship.addGold(50);
        assertEquals(250, ship.getGold());
        ship.spendGold(100);
        assertEquals(150, ship.getGold());
        assertThrows(IllegalArgumentException.class, () -> ship.spendGold(500));
        assertEquals(150, ship.getGold());
        assertThrows(IllegalArgumentException.class, () -> ship.addGold(-1));
        assertThrows(IllegalArgumentException.class, () -> ship.spendGold(-1));
    }

    @Test
    public void rum() {
        Ship ship = fresh();
        ship.addRum(10);
        assertEquals(40, ship.getRum());
        assertEquals(5, ship.takeRum(5));
        assertEquals(35, ship.takeRum(50));
        assertEquals(0, ship.getRum());
        assertThrows(IllegalArgumentException.class, () -> ship.addRum(-1));
        assertThrows(IllegalArgumentException.class, () -> ship.takeRum(-1));
    }

    private static PlayerShip player() { return new PlayerShip("Eustace", ChannelMap.standard().getHome()); }

    @Test
    public void playerStartsAtSark() {
        PlayerShip p = player();
        assertEquals("sark", p.getLocation().getKey());
        assertEquals(0, p.getNotoriety());
        assertEquals(0, p.getPortsVisited());
        assertEquals(100, p.getHull());
        assertEquals(200, p.getGold());
        assertThrows(IllegalArgumentException.class, () -> new PlayerShip("x", null));
    }

    @Test
    public void moveToCountsPortArrivals() {
        ChannelMap map = ChannelMap.standard();
        PlayerShip p = new PlayerShip("Eustace", map.getHome());
        p.moveTo(map.find("barfleur sark"));
        assertEquals(0, p.getPortsVisited());
        p.moveTo(map.find("sark"));
        assertEquals(1, p.getPortsVisited());
        assertThrows(IllegalArgumentException.class, () -> p.moveTo(map.find("dover")));
        assertEquals("sark", p.getLocation().getKey());
    }

    @Test
    public void purchasesStopAtGoldAndCaps() {
        PlayerShip p = player();
        assertEquals(5, p.hire(5, Port.HIRE_PRICE));
        assertEquals(25, p.getCrew().getCount());
        assertEquals(125, p.getGold());
        assertEquals(0, p.hire(0, Port.HIRE_PRICE));
        assertEquals(8, p.hire(Integer.MAX_VALUE, Port.HIRE_PRICE));
        assertEquals(5, p.getGold());
        PlayerShip q = player();
        assertEquals(13, q.hire(2147483647, Port.HIRE_PRICE));
        assertEquals(5, q.getGold());
        PlayerShip r = player();
        r.setHull(90);
        assertEquals(10, r.repair(50, Port.REPAIR_PRICE));
        assertEquals(100, r.getHull());
        assertEquals(180, r.getGold());
        assertEquals(10, r.buyRum(10, 4));
        assertEquals(40, r.getRum());
        assertEquals(140, r.getGold());
        assertEquals(35, r.buyRum(100, 4));
        assertEquals(0, r.getGold());
    }

    @Test
    public void upgrades() {
        PlayerShip p = player();
        assertTrue(p.upgradeCannons(200));
        assertEquals(2, p.getCannons());
        assertEquals(0, p.getGold());
        assertFalse(p.upgradeArmour(200));
        assertEquals(1, p.getArmour());
        PlayerShip maxed = player();
        maxed.setCannons(PlayerShip.MAX_LEVEL);
        maxed.setArmour(PlayerShip.MAX_LEVEL);
        assertFalse(maxed.upgradeCannons(50));
        assertFalse(maxed.upgradeArmour(50));
        assertEquals(200, maxed.getGold());
    }

    @Test
    public void payBonus() {
        PlayerShip p = player();
        p.getCrew().onWin(600);
        assertTrue(p.payBonus(100));
        assertEquals(25, p.getCrew().getGreed());
        assertEquals(82, p.getCrew().getMorale());
        assertEquals(100, p.getGold());
        assertFalse(p.payBonus(500));
        assertEquals(100, p.getGold());
    }

    @Test
    public void notorietyAndStatus() {
        PlayerShip p = player();
        p.addNotoriety(2);
        assertEquals(2, p.getNotoriety());
        assertThrows(IllegalArgumentException.class, () -> p.addNotoriety(-1));
        String status = p.describe();
        for (String field : new String[] {"100/100", "Crew 20", "morale 70", "greed 0", "Cannons 1", "armour 1",
            "Rum 30", "Gold 200", "Notoriety 2", "Sark"}) {
            assertTrue(status.contains(field), field);
        }
    }

    @Test
    public void enemyFactoriesScaleWithNotoriety() {
        EnemyShip m = EnemyShip.merchant(10);
        assertEquals("Merchant", m.getName());
        assertEquals(60, m.getHull());
        assertEquals(13, m.getCrew().getCount());
        assertEquals(30, m.getCrew().getMorale());
        assertEquals(2, m.getCannons());
        assertEquals(1, m.getArmour());
        assertEquals(200, m.getGold());
        assertEquals(13, m.getRum());
        assertEquals(1, m.getGain());
        assertEquals(60, EnemyShip.pirate(0).getHull());
        assertEquals(2, EnemyShip.pirate(0).getGain());
        assertEquals(80, EnemyShip.coastGuard(0).getHull());
        assertEquals(3, EnemyShip.coastGuard(0).getGain());
        assertEquals(5, EnemyShip.coastGuard(100).getCannons());
        assertEquals(40, EnemyShip.coastGuard(100).getCrew().getCount());
        assertTrue(EnemyShip.merchant(0).describe().contains("Merchant"));
        assertThrows(IllegalArgumentException.class, () -> EnemyShip.merchant(-1));
        assertThrows(IllegalArgumentException.class, () -> EnemyShip.pirate(-1));
        assertThrows(IllegalArgumentException.class, () -> EnemyShip.coastGuard(-1));
    }

    @Test
    public void settersClamp() {
        Ship ship = fresh();
        ship.setHull(500);
        assertEquals(100, ship.getHull());
        ship.setCannons(9);
        assertEquals(5, ship.getCannons());
        ship.setArmour(-1);
        assertEquals(0, ship.getArmour());
    }
}
