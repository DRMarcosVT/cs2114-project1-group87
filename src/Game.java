import java.util.Random;
import java.util.Scanner;

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

    public Game(Scanner in, ChannelMap map, Random random) {
        if (map == null) throw new IllegalArgumentException();
        this.in = in;
        this.map = map;
        this.random = random;
        player = new PlayerShip("Eustace", map.getHome());
    }

    public static void main(String[] args) {
        Random random = args.length > 0 ? new Random(Long.parseLong(args[0])) : new Random();
        new Game(new Scanner(System.in), new ChannelMap(), random).run();
    }

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

    private static void show(String reply) {
        String rule = "=".repeat(60);
        System.out.println(rule + "\n" + reply + "\n" + rule);
    }

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

    private String retire() {
        if (player.getLocation() != map.getHome()) return "You can only retire at Sark.";
        int needed = GOLD_TARGET - player.getGold();
        return needed > 0 ? "You need " + needed + " more gold to retire." : end("You retire to the abbey, rich");
    }

    private String outcome() {
        Crew crew = player.getCrew();
        if (player.getHull() == 0 || crew.getCount() == 0) return "\n" + end("Your ship is lost");
        if (crew.mutinied()) return "\n" + end(crew.getMorale() == 0 ? "Mutiny: the crew's morale broke" : "Mutiny: the crew's greed won");
        return "";
    }

    private String end(String how) {
        running = false;
        return String.format("%s. Final gold %d, ports visited %d.", how, player.getGold(), player.getPortsVisited());
    }

    public boolean isRunning() { return running; }
    public PlayerShip getPlayer() { return player; }
    public Encounter getEncounter() { return encounter; }
}
