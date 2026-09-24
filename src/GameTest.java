import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {
    private Game game;

    @BeforeEach
    public void setUp() { game = new Game(new Scanner(""), new ChannelMap(), new FixedRandom(0)); }

    private String run(String input) {
        Game g = new Game(new Scanner(input), new ChannelMap(), new FixedRandom(0));
        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try { g.run(); } finally { System.setOut(original); }
        assertFalse(g.isRunning());
        return out.toString();
    }

    private String say(String line) { return game.dispatch(new CommandParser().parse(line)); }

    @Test
    public void newGameStartsAtSark() {
        assertEquals("sark", game.getPlayer().getLocation().getKey());
        assertNull(game.getEncounter());
        assertTrue(game.isRunning());
        assertThrows(IllegalArgumentException.class, () -> new Game(new Scanner(""), null, new FixedRandom(0)));
    }

    @Test
    public void runReadsUntilQuitOrInputEnds() {
        String out = run("look\nstatus\nquit\nyes\n");
        assertTrue(out.contains("Sark"));
        assertTrue(out.contains("Hull 100/100"));
        assertTrue(out.contains("You quit"));
        String ended = run("\nsial\n");
        assertTrue(ended.contains("I don't understand 'sial'"));
        assertTrue(ended.contains("Input ended"));
    }

    @Test
    public void lookStatusHelpQuit() {
        assertTrue(say("look").contains("Sark"));
        assertTrue(say("status").contains("morale 70"));
        assertEquals(12, say("help").split("\n").length);
        assertTrue(say("quit").contains("yes"));
        assertEquals("Carrying on.", say("no"));
        assertTrue(game.isRunning());
        say("quit");
        assertTrue(say("yes").contains("Final gold 200"));
        assertFalse(game.isRunning());
    }

    @Test
    public void sailDrinksRollsAndFights() {
        String text = say("sail barfleur sark");
        assertEquals("barfleur sark", game.getPlayer().getLocation().getKey());
        assertEquals(28, game.getPlayer().getRum());
        assertNotNull(game.getEncounter());
        assertTrue(text.contains("Merchant"));
        assertTrue(say("look").contains("Merchant"));
        assertEquals("Not with an enemy alongside. Fight or flee.", say("sail sark"));
        assertEquals("You must be in port.", say("repair 5"));
        assertTrue(say("fight").contains("surrenders"));
        assertEquals(350, game.getPlayer().getGold());
        assertNull(game.getEncounter());
        assertTrue(game.isRunning());
        assertEquals("Nothing to fight.", say("fight"));
        assertEquals("Nothing to fight.", say("flee"));
    }

    @Test
    public void fleeAndSinking() {
        say("sail barfleur sark");
        assertTrue(say("flee").contains("You run"));
        assertEquals(92, game.getPlayer().getHull());
        assertNull(game.getEncounter());
        say("sail sark");
        say("sail barfleur sark");
        game.getPlayer().setHull(1);
        assertTrue(say("fight").contains("Your ship is lost"));
        assertFalse(game.isRunning());
        Game greedy = new Game(new Scanner(""), new ChannelMap(), new FixedRandom(0));
        greedy.getPlayer().getCrew().onWin(2000);
        greedy.getPlayer().addGold(100);
        assertTrue(greedy.dispatch(new Command("sail", "barfleur sark")).contains("greed"));
        assertFalse(greedy.isRunning());
        Game dry = new Game(new Scanner(""), new ChannelMap(), new FixedRandom(99));
        dry.getPlayer().takeRum(30);
        for (int i = 0; i < 6; i++) dry.getPlayer().getCrew().onFlee();
        assertTrue(dry.dispatch(new Command("sail", "barfleur sark")).contains("morale broke"));
        assertFalse(dry.isRunning());
    }

    @Test
    public void portPurchases() {
        assertEquals("Hired 5 men.", say("hire 5"));
        assertEquals(25, game.getPlayer().getCrew().getCount());
        assertEquals(125, game.getPlayer().getGold());
        assertEquals("Hired 0 men.", say("hire 0"));
        assertEquals("Hired 8 men.", say("hire 2147483647"));
        assertEquals("Bought 2 bottles.", say("buy rum 2"));
        assertEquals("You can buy cannons, armour or rum.", say("buy swords"));
        assertEquals("No: already level 5, or not enough gold.", say("buy cannons"));
        game.getPlayer().addGold(200);
        assertEquals("Cannons now level 2.", say("buy cannons"));
        game.getPlayer().setArmour(PlayerShip.MAX_LEVEL);
        game.getPlayer().addGold(600);
        assertEquals("No: already level 5, or not enough gold.", say("buy armour"));
        assertEquals(601, game.getPlayer().getGold());
        assertEquals("Repaired 0 points.", say("repair 3"));
        assertEquals("The crew cheers.", say("bonus 100"));
        assertEquals("You don't hold that much gold.", say("bonus 5000"));
    }

    @Test
    public void retire() {
        assertEquals("You need 800 more gold to retire.", say("retire"));
        say("sail barfleur sark");
        say("fight");
        say("sail barfleur");
        assertEquals("You can only retire at Sark.", say("retire"));
        game.getPlayer().addGold(650);
        say("sail barfleur sark");
        say("fight");
        say("sail sark");
        assertTrue(say("retire").contains("rich"));
        assertFalse(game.isRunning());
    }

    @Test
    public void badInput() {
        assertEquals("", say(""));
        assertEquals("", say("  sail dover"));
        assertEquals("I don't understand 'sial'. Type help.", say("sial"));
        assertEquals("I don't understand 'SAIL'. Type help.", say("SAIL dover"));
        assertEquals("sail <stop>", say("sail"));
        assertEquals("No such place.", say("sail dover now please"));
        assertEquals("No such place.", say("sail help"));
        assertEquals("No such place.", say("sail Dover."));
        assertEquals("Not from here. You can sail to: barfleur sark, west channel.", say("sail dover"));
        assertEquals("You are already there.", say("sail sark"));
        assertEquals("Type a whole number of 0 or more.", say("hire 5 men"));
        assertEquals("Type a whole number of 0 or more.", say("hire -5"));
        assertEquals("Type a whole number of 0 or more.", say("hire ten"));
        assertEquals("Type a whole number of 0 or more.", say("buy rum"));
        assertEquals(200, game.getPlayer().getGold());
        assertEquals("I don't understand 'émoji'. Type help.", say("émoji"));
        String longLine = "sail " + "x".repeat(100000);
        assertEquals("No such place.", say(longLine));
    }
}
