public class Command {
    private final String verb;
    private final String argument;

    public Command(String verb, String argument) {
        if (verb == null) throw new IllegalArgumentException();
        this.verb = verb;
        this.argument = argument == null ? "" : argument;
    }

    public String getVerb() { return verb; }
    public String getArgument() { return argument; }

    public String getWord(int i) {
        String[] words = argument.split(" ");
        return i < words.length ? words[i] : "";
    }

    public String toString() { return (verb + " " + argument).trim(); }
}
