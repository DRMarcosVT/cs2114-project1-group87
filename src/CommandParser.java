import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * builds commands in Game.run()
 * CommandParser
 */
public class CommandParser {
    //LinkedHashMap is an ordered hashmap, aids in learning where commands are when 
    //calling help
    private final HashMap<String, String> templates = new LinkedHashMap<>();

    public CommandParser() {
        //each verb the player can type and the template it is used in
        templates.put("look", "look");
        templates.put("status", "status");
        templates.put("sail", "sail <stop>");
        templates.put("repair", "repair <points>");
        templates.put("hire", "hire <men>");
        templates.put("buy", "buy cannons | buy armour | buy rum <bottles>");
        templates.put("bonus", "bonus <gold>");
        templates.put("fight", "fight");
        templates.put("flee", "flee");
        templates.put("retire", "retire");
        templates.put("help", "help");
        templates.put("quit", "quit");
    }
    //turn a typed in command into a Command object
    public Command parse(String line) {
        //find the space index
        int space = line.indexOf(' ');
        /**
         * if the space is not in the command, split into verb/argument at the space and return a Command 
         * with no argument (ex: look)
         * 
         * if it is, return the Command with verb and argument
         */
        return space < 0 ? new Command(line, "") : new Command(line.substring(0, space), line.substring(space + 1));
    }
    /**
     * check the templates to see if it is a recognised command
     * @param word the parsed command verb
     * @return boolean based on the existance of the key in the dictionary
     */
    public boolean isVerb(String word) { return templates.containsKey(word); }
    /**
     * for the help command, get the template of the verb
     * @param verb you want the template of
     * @return the tempalte of the command
     */
    public String templateOf(String verb) { return templates.get(verb); }

    /**
     * uses the String join method on the Collection of values of type String, the 
     * String. join() method returns a string
     * @return all the templates in String form
     */
    public String allTemplates() { return String.join("\n", templates.values()); }

    /**
     * parse as an int numeric arguments, throw on negative numbers to prevent cheating
     * @param text the numeric argument
     * @return the numeric argument parsed as an int
     */
    public static int parseCount(String text) {
        int n = Integer.parseInt(text);
        if (n < 0) throw new NumberFormatException();
        return n;
    }
}
