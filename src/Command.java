public class Command {
    private final String verb;
    private final String argument;

    public Command(String verb, String argument) {
        //prevent the exception of reaching for the verb field in null
        if (verb == null) throw new IllegalArgumentException();
        this.verb = verb;
        //if the argument was null the argument is "", otherwise set the field to it
        this.argument = argument == null ? "" : argument;
    }

    //verb and argument getters
    public String getVerb() { return verb; }
    public String getArgument() { return argument; }

    //split the argument, useful for commands with quantities
    public String getWord(int i) {
        String[] words = argument.split(" ");
        //if the index is out of bounds return "" otherwise the string
        //ternary is needed to discriminate against incomplete quantitative imperatives
        //ex: buy rum needs buy rum ##
        return i < words.length ? words[i] : "";
    }
    
}
