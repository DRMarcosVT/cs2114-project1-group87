import java.util.Random;
import java.util.Scanner;

/**
 * Runs one voyage: reads each line the player types, checks whether the command is allowed
 * where the player is, carries it out, and prints the reply. 
 */
public class Game {
    public static final int GOLD_TARGET = 1000;
    private final Scanner in;
    private final CommandParser parser = new CommandParser();
    private final ChannelMap map;
    private final PlayerShip player;
    private Encounter encounter;
    private final Random random;
    private boolean running = true;
    private boolean confirmingQuit;

    /**
     * game constructor
     *  Puts a new player ship at their home port
     *
     * @param in where typed lines are read from
     * @param map the map to sail around in
     * @param random rng for every encounter
     * @throws IllegalArgumentException if map is null
     */
    public Game(Scanner in, ChannelMap map, Random random) {
        if (map == null) throw new IllegalArgumentException();
        this.in = in;
        this.map = map;
        this.random = random;
        player = new PlayerShip("Eustace", map.getHome());
    }

    /**
     * Starts a game on the keyboard. A number given as the first argument seeds the dice,
     * so java -cp bin Game 3 replays the voyage in docs/demo.txt; with no argument the
     * dice differ every run.
     * 
     * starts a game. 
     * run with java -cp bin game 3
     *
     * @param args an optional seed
     */
    public static void main(String[] args) {
        Random random = args.length > 0 ? new Random(Long.parseLong(args[0])) : new Random();
        new Game(new Scanner(System.in), new ChannelMap(), random).run();
    }

    /**
     * Prints the opening scene, then reads and answers one line at a time until the game
     * ends or the input runs out.
     */
    public void run() {
        show("You are Eustace the Monk, moored at Sark with 200 gold. Retire here with "
            + GOLD_TARGET + " gold to win. Type help for the commands.\n" + map.getHome().describe());
        while (running) {
            System.out.print("> ");
            if (!in.hasNextLine()) break;
            show(dispatch(parser.parse(in.nextLine())));
        }
        if (running) show(end("Input ended"));
    }

    /**
     * Prints a reply between two lines of equals signs, so each reply stands apart from
     * what the player typed.
     *
     * @param reply the text to print
     */
    private static void show(String reply) {
        String rule = "=".repeat(60);
        System.out.println(rule + "\n" + reply + "\n" + rule);
    }

    /**
     * Carries out one command and returns the reply. It first handles a pending quit, a
     * blank line and an unknown verb; then refuses fight and flee with no enemy, sail during
     * a fight, and trading away from a port; then returns the command's template when a
     * required argument is missing; and only then runs the command.
     *
     * @param command the verb and argument the player typed
     * @return the text to print, ending with how the game ended if this command ended it
     */
    public String dispatch(Command command) {
        String verb = command.getVerb();
        String arg = command.getArgument();
        if (confirmingQuit) {
            confirmingQuit = false;
            return verb.equals("yes") ? end("You quit") : "Carrying on.";
        }
        if (verb.isEmpty()) return "";
        if (!parser.isVerb(verb)) return "I don't understand '" + verb + "'. Type help.";
        boolean fighting = encounter != null;
        boolean inPort = player.getLocation().getKind().equals(Location.PORT);
        if ((verb.equals("fight") || verb.equals("flee")) && !fighting) return "Nothing to fight.";
        if (verb.equals("sail") && fighting) return "Not with an enemy alongside. Fight or flee.";
        if (!inPort && (verb.equals("repair") || verb.equals("hire") || verb.equals("buy") || verb.equals("bonus"))) {
            return "You must be in port.";
        }
        String template = parser.templateOf(verb);
        if (template.contains("<") && arg.isEmpty()) return template;
        try {
            return switch (verb) {
                case "look" -> fighting ? encounter.getEnemy().describe() : player.getLocation().describe();
                case "status" -> player.describe();
                case "help" -> parser.allTemplates();
                case "quit" -> { confirmingQuit = true; yield "Quit without saving? Type yes."; }
                case "sail" -> sail(arg);
                case "repair" -> "Repaired " + player.repair(CommandParser.parseCount(arg), Port.REPAIR_PRICE) + " points.";
                case "hire" -> "Hired " + player.hire(CommandParser.parseCount(arg), Port.HIRE_PRICE) + " men.";
                case "buy" -> buy(command);
                case "bonus" -> player.payBonus(CommandParser.parseCount(arg)) ? "The crew cheers." : "You don't hold that much gold.";
                case "fight" -> { String log = encounter.fight(); encounter = null; yield log + outcome(); }
                case "flee" -> { String text = encounter.flee(); encounter = null; yield text + outcome(); }
                default -> retire();
            };
        } catch (NumberFormatException e) {
            return "Type a whole number of 0 or more.";
        }
    }

    /**
     * Sails to a neighbouring stop. Arriving anywhere other than a port makes the crew drink
     * and rolls for an encounter.
     *
     * @param name the stop's key as the player typed it
     * @return the new stop's description, a sighting if an enemy appears, and the ending if
     *     the crew mutinies from lack of rum; or the reason the ship could not sail
     */
    private String sail(String name) {
        Location stop = map.find(name);
        Location here = player.getLocation();
        if (stop == null) return "No such place.";
        if (stop == here) return "You are already there.";
        if (!here.isNextTo(stop)) return "Not from here. You can sail to: " + here.neighbourList() + ".";
        player.moveTo(stop);
        String text = stop.describe();
        if (!stop.getKind().equals(Location.PORT)) {
            player.takeRum(player.getCrew().drink(player.getRum()));
            encounter = Encounter.roll(stop, player, random);
            if (encounter != null) text += "\nSail ho! " + encounter.getEnemy().describe() + " Fight or flee?";
        }
        return text + outcome();
    }

    /**
     * Buys cannons, armour or rum at the current port. The first word after buy picks the
     * item; for rum the second word is the number of bottles.
     *
     * @param command the buy command, whose argument names the item
     * @return what was bought, or why nothing was
     * @throws NumberFormatException if the rum count is not a whole number of 0 or more
     */
    private String buy(Command command) {
        Port here = (Port) player.getLocation();
        return switch (command.getWord(0)) {
            case "cannons" -> player.upgradeCannons(here.upgradePrice(player.getCannons() + 1))
                ? "Cannons now level " + player.getCannons() + "." : "No: already level 5, or not enough gold.";
            case "armour" -> player.upgradeArmour(here.upgradePrice(player.getArmour() + 1))
                ? "Armour now level " + player.getArmour() + "." : "No: already level 5, or not enough gold.";
            case "rum" -> "Bought " + player.buyRum(CommandParser.parseCount(command.getWord(1)), here.rumPrice()) + " bottles.";
            default -> "You can buy cannons, armour or rum.";
        };
    }

    /**
     * Ends the game with a win if the player is at Sark holding at least GOLD_TARGET gold.
     *
     * @return the winning ending, or how much more gold is needed, or that retiring only
     *     works at Sark
     */
    private String retire() {
        if (player.getLocation() != map.getHome()) return "You can only retire at Sark.";
        int needed = GOLD_TARGET - player.getGold();
        return needed > 0 ? "You need " + needed + " more gold to retire." : end("You retire to the abbey, rich");
    }

    /**
     * Checks whether the last sail, fight or flee lost the game: the ship sinks at hull 0 or
     * crew 0, and the crew mutinies at morale 0 or greed 100.
     *
     * @return the ending on a new line if the game is lost, otherwise an empty string
     */
    private String outcome() {
        Crew crew = player.getCrew();
        if (player.getHull() == 0 || crew.getCount() == 0) return "\n" + end("Your ship is lost");
        if (crew.mutinied()) return "\n" + end(crew.getMorale() == 0 ? "Mutiny: the crew's morale broke" : "Mutiny: the crew's greed won");
        return "";
    }

    /**
     * Stops the game and describes how it ended.
     *
     * @param how the first sentence of the ending, such as You quit
     * @return that sentence with the final gold and the number of ports visited
     */
    private String end(String how) {
        running = false;
        return String.format("%s. Final gold %d, ports visited %d.", how, player.getGold(), player.getPortsVisited());
    }

    /**
     * Tells whether the game is still going.
     *
     * @return false once any ending has happened
     */
    public boolean isRunning() { return running; }

    /**
     * Gets the player's ship.
     *
     * @return the ship, so tests can check its gold, hull and crew
     */
    public PlayerShip getPlayer() { return player; }

    /**
     * Gets the fight in progress.
     *
     * @return the encounter, or null when no enemy is alongside
     */
    public Encounter getEncounter() { return encounter; }
}
