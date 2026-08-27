package billy.parser;

import billy.BillyException;

/**
 * The words Billy accepts as commands, one for each thing it can be asked to do.
 *
 * <p>Using an enum rather than loose {@code String} constants means the set of
 * commands is fixed and known to the compiler: a command can only be one of these
 * values, and a misspelt name is a compile error instead of a command that quietly
 * never matches. The list of keywords shown to the user is built from these values,
 * so it cannot fall out of step with what Billy actually accepts.
 *
 * <p>The order below is the order the keywords are listed to the user.
 */
public enum CommandWord {
    /** Adds a task with no date attached, e.g. {@code todo borrow book}. */
    TODO("todo"),

    /** Adds a task with a due date, e.g. {@code deadline return book /by Sunday}. */
    DEADLINE("deadline"),

    /** Adds a task spanning a period, e.g. {@code event meeting /from 2pm /to 4pm}. */
    EVENT("event"),

    /** Prints every stored task. */
    LIST("list"),

    /** Prints the tasks falling on one day, e.g. {@code on 2019-12-02}. */
    ON("on"),

    /** Prints the tasks whose description mentions a word, e.g. {@code find book}. */
    FIND("find"),

    /** Marks a task as done, e.g. {@code mark 2}. */
    MARK("mark"),

    /** Marks a task as not done again, e.g. {@code unmark 2}. */
    UNMARK("unmark"),

    /** Removes a task from the list, e.g. {@code delete 3}. */
    DELETE("delete"),

    /** Ends the conversation. */
    BYE("bye");

    /** The word the user types to invoke this command. */
    private final String keyword;

    CommandWord(String keyword) {
        this.keyword = keyword;
    }

    /** Returns the word the user types to invoke this command. */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Finds the command a typed word refers to, ignoring capitalisation.
     *
     * @param word the first word of what the user typed
     * @return the matching command
     * @throws BillyException if no command uses that word
     */
    public static CommandWord fromKeyword(String word) throws BillyException {
        for (CommandWord command : values()) {
            if (command.keyword.equalsIgnoreCase(word)) {
                return command;
            }
        }
        throw new BillyException("I don't know what '" + word + "' means. " + describeAll());
    }

    /**
     * Lists every keyword Billy accepts, for use when a command is not recognised.
     *
     * @return a sentence such as {@code I understand: todo, deadline, ..., bye.}
     */
    public static String describeAll() {
        StringBuilder keywords = new StringBuilder();
        for (CommandWord command : values()) {
            if (keywords.length() > 0) {
                keywords.append(", ");
            }
            keywords.append(command.keyword);
        }
        return "I understand: " + keywords + ".";
    }
}
