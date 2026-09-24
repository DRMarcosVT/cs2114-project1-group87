import java.util.HashMap;
import java.util.LinkedHashMap;

public class CommandParser {
    private final HashMap<String, String> templates = new LinkedHashMap<>();

    public CommandParser() {
        String[][] table = {
            {"look", "look"}, {"status", "status"}, {"sail", "sail <stop>"},
            {"repair", "repair <points>"}, {"hire", "hire <men>"},
            {"buy", "buy cannons | buy armour | buy rum <bottles>"}, {"bonus", "bonus <gold>"},
            {"fight", "fight"}, {"flee", "flee"}, {"retire", "retire"}, {"help", "help"}, {"quit", "quit"}};
        for (String[] row : table) templates.put(row[0], row[1]);
    }

    public Command parse(String line) {
        int space = line.indexOf(' ');
        return space < 0 ? new Command(line, "") : new Command(line.substring(0, space), line.substring(space + 1));
    }

    public boolean isVerb(String word) { return templates.containsKey(word); }
    public String templateOf(String verb) { return templates.get(verb); }
    public String allTemplates() { return String.join("\n", templates.values()); }

    public static int parseCount(String text) {
        int n = Integer.parseInt(text);
        if (n < 0) throw new NumberFormatException();
        return n;
    }
}
