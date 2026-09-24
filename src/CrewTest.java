import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CrewTest {
    private final Crew crew = new Crew(20, 70);

    @Test
    public void constructorRejectsOutOfRange() {
        assertEquals(40, new Crew(40, 100).getCount());
        assertEquals(0, new Crew(0, 0).getMorale());
        assertThrows(IllegalArgumentException.class, () -> new Crew(41, 50));
        assertThrows(IllegalArgumentException.class, () -> new Crew(-1, 50));
        assertThrows(IllegalArgumentException.class, () -> new Crew(20, 101));
        assertThrows(IllegalArgumentException.class, () -> new Crew(20, -1));
    }

    @Test
    public void loseAndHire() {
        assertEquals(2, crew.lose(2));
        assertEquals(18, crew.getCount());
        assertEquals(60, crew.getMorale());
        assertEquals(18, crew.lose(50));
        assertEquals(0, crew.getMorale());
        assertEquals(5, crew.hire(5));
        assertEquals(35, crew.hire(100));
        assertThrows(IllegalArgumentException.class, () -> crew.lose(-1));
        assertThrows(IllegalArgumentException.class, () -> crew.hire(-1));
    }

    @Test
    public void moraleFactor() {
        assertEquals(0.85, crew.moraleFactor(), 1e-9);
        assertEquals(0.5, new Crew(20, 0).moraleFactor(), 1e-9);
        assertEquals(1.0, new Crew(20, 100).moraleFactor(), 1e-9);
    }

    @Test
    public void drink() {
        assertEquals(2, crew.drink(30));
        assertEquals(70, crew.getMorale());
        assertEquals(1, crew.drink(1));
        assertEquals(65, crew.getMorale());
        assertEquals(0, crew.drink(0));
        assertEquals(55, crew.getMorale());
        assertEquals(3, new Crew(21, 70).drink(30));
        assertThrows(IllegalArgumentException.class, () -> crew.drink(-1));
    }

    @Test
    public void winFleeAndMutiny() {
        crew.onWin(150);
        assertEquals(80, crew.getMorale());
        assertEquals(7, crew.getGreed());
        assertFalse(crew.mutinied());
        crew.onWin(5000);
        assertEquals(100, crew.getGreed());
        assertTrue(crew.mutinied());
        Crew fled = new Crew(20, 70);
        fled.onFlee();
        assertEquals(55, fled.getMorale());
        assertTrue(new Crew(20, 0).mutinied());
    }

    @Test
    public void receiveBonus() {
        crew.onWin(600);
        crew.receiveBonus(100);
        assertEquals(25, crew.getGreed());
        assertEquals(82, crew.getMorale());
        Crew empty = new Crew(0, 50);
        empty.receiveBonus(100);
        assertEquals(50, empty.getMorale());
    }
}
