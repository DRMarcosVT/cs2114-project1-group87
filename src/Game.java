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
    private boolean confirmingQuit = false;

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
     * 
     * starts a game. 
     * run with java -cp bin game 
     *
     * @param args an optional seed
     */
    public static void main(String[] args) {
        //seeds the rng if an argument was passed
        //seed Random with a Long if there is an argument, otherwise use the default argument
        Random random = args.length > 0 ? new Random(Long.parseLong(args[0])) : new Random();
        //new game object 
        new Game(new Scanner(System.in), new ChannelMap(), random).run();
    } 

    /**
     * prints opening scene, answers one input at a time, simulating a game
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
     * Print output prettier. Prints a reply between two lines of equals signs, so each reply stands apart from
     * what the player typed. 
     *
     * @param reply the text to print
     */
    private static void show(String reply) {
        String rule = "=".repeat(60);
        System.out.println(rule + "\n" + reply + "\n" + rule);
    }

    /**
     * Middle-man between player input and the game. Carries out one command and returns the reply. It first handles a pending quit, a
     * blank line and an unknown verb; then refuses fight and flee with no enemy, sail during
     * a fight, and trading away from a port; then returns the command's template when a
     * required argument is missing; and only then runs the command.
     * 
     * 
     *
     * @param command the verb and argument the player typed
     * @return the text to print, ending with how the game ended if this command ended it
     */
    public String dispatch(Command command) {
        String verb = command.getVerb();
        String arg = command.getArgument();
        //set confirmingquit to false again to prevent the quit prompt from persisting
        if (confirmingQuit) {
            confirmingQuit = false;
            if (verb.equals("yes")) {
                return end("You quit");
            }
            return "Carrying on.";
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
        //if the verb needs an argument and none was given, return the acceptable form of the command
        if (template.contains("<") && arg.isEmpty()) return template;
        try {
            if (verb.equals("look")) {
                if (fighting) {
                    return encounter.getEnemy().describe();
                }
                return player.getLocation().describe();
            }
            if (verb.equals("status")) {
                return player.describe();
            }
            if (verb.equals("help")) {
                return parser.allTemplates();
            }
            if (verb.equals("quit")) {
                confirmingQuit = true;
                return "Quit without saving? Type yes.";
            }
            if (verb.equals("sail")) {
                return sail(arg);
            }
            if (verb.equals("repair")) {
                return "Repaired " + player.repair(CommandParser.parseCount(arg), Port.REPAIR_PRICE) + " points.";
            }
            if (verb.equals("hire")) {
                return "Hired " + player.hire(CommandParser.parseCount(arg), Port.HIRE_PRICE) + " men.";
            }
            if (verb.equals("buy")) {
                return buy(command);
            }
            if (verb.equals("bonus")) {
                if (player.payBonus(CommandParser.parseCount(arg))) {
                    return "The crew cheers.";
                }
                return "You don't hold that much gold.";
            }
            if (verb.equals("fight")) {
                String log = encounter.fight();
                encounter = null;
                return log + outcome();
            }
            if (verb.equals("flee")) {
                String text = encounter.flee();
                encounter = null;
                return text + outcome();
            }
            // the only verb left is retire
            return retire();
        } catch (NumberFormatException e) {
            return "Type a whole number of 0 or more.";
        }
    }

    /**
     * sails to a neighbouring node, if its not a port, the crew drinks and the encounter rolls
     * 
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
     * execute the buy command
     *
     * @param command the buy command, whose argument names the item
     * @return what was bought, or why nothing was bought
     */
    private String buy(Command command) {
        Port here = (Port) player.getLocation();
        //get the first word of the object, command parser has already split the input into verb + argument
        String item = command.getWord(0);
        /**
         * for cannons and armour, the command upgrades them +1
         */
        if (item.equals("cannons")) {
            if (player.upgradeCannons(here.upgradePrice(player.getCannons() + 1))) {
                return "Cannons now level " + player.getCannons() + ".";
            }
            return "No: already level 5, or not enough gold.";
        }
        if (item.equals("armour")) {
            if (player.upgradeArmour(here.upgradePrice(player.getArmour() + 1))) {
                return "Armour now level " + player.getArmour() + ".";
            }
            return "No: already level 5, or not enough gold.";
        }
        if (item.equals("rum")) {
            int bottles = CommandParser.parseCount(command.getWord(1));
            return "Bought " + player.buyRum(bottles, here.rumPrice()) + " bottles.";
        }
        return "You can buy cannons, armour or rum.";
    }

    /**
     * game ends if the player retires with the right amount of gold
     *
     * @return the winning ending, or how much more gold is needed, or that retiring only
     *     works at sark
     */
    private String retire() {
        //player must be at sark
        if (player.getLocation() != map.getHome()) return "You can only retire at Sark.";
        //player must be wealthy enough
        int needed = GOLD_TARGET - player.getGold();
        return needed > 0 ? "You need " + needed + " more gold to retire." : end("You retire to the abbey, rich");
    }

    /**
     * checks if the last even ended the game
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
     * game over, describes ending stats
     *
     * @param how the first sentence of the ending
     * @return the how sentence with the final gold and the number of ports visited
     */
    private String end(String how) {
        //boolean ends the game
        running = false;
        //game over message
        return String.format("%s. Final gold %d, ports visited %d.", how, player.getGold(), player.getPortsVisited());
    }

    /**
     * checks whether the game is still going
     *
     * @return false once any ending has happened
     */
    public boolean isRunning() { return running; }

    /**
     * gets the player's ship
     *
     * @return the ship, so tests can check its gold, hull and crew
     */
    public PlayerShip getPlayer() { return player; }

    /**
     * get the fight in progress
     *
     * @return the encounter, or null when no enemy is alongside
     */
    public Encounter getEncounter() { return encounter; }
}
