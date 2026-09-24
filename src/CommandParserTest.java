import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommandParserTest {
    private final CommandParser parser = new CommandParser();

    @Test
    public void parseSplitsAtFirstSpace() {
        Command c = parser.parse("sail dover boulogne");
        assertEquals("sail", c.getVerb());
        assertEquals("dover boulogne", c.getArgument());
    }

    @Test
    public void parseKeepsTextAsTyped() {
        Command c = parser.parse("SAIL \"Dover\".");
        assertEquals("SAIL", c.getVerb());
        assertEquals("\"Dover\".", c.getArgument());
        assertEquals("", parser.parse("").getVerb());
        assertEquals("", parser.parse("  sail dover").getVerb());
    }

    @Test
    public void verbTable() {
        assertTrue(parser.isVerb("sail"));
        assertFalse(parser.isVerb("sial"));
        assertEquals("sail <stop>", parser.templateOf("sail"));
        assertNull(parser.templateOf("sial"));
        assertEquals(12, parser.allTemplates().split("\n").length);
        assertTrue(parser.allTemplates().contains("bonus <gold>"));
    }

    @Test
    public void parseCountAcceptsWholeNumbers() {
        assertEquals(25, CommandParser.parseCount("25"));
        assertEquals(0, CommandParser.parseCount("0"));
        assertEquals(Integer.MAX_VALUE, CommandParser.parseCount("2147483647"));
    }

    @Test
    public void parseCountRejectsEverythingElse() {
        for (String bad : new String[] {"-50", "ten", "10.1", "99999999999", "5 men", ""}) {
            assertThrows(NumberFormatException.class, () -> CommandParser.parseCount(bad));
        }
    }

    @Test
    public void commandWordsAndText() {
        Command c = new Command("buy", "rum 5");
        assertEquals("rum", c.getWord(0));
        assertEquals("5", c.getWord(1));
        assertEquals("", c.getWord(2));
    }

    @Test
    public void commandNullVerbThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Command(null, ""));
    }
}
