package billy.ui;

import java.util.Scanner;

/**
 * Everything Billy says to the user, and everything the user types back.
 *
 * <p>Gathering this in one class means the rest of Billy can describe *what*
 * happened without deciding *how* it appears. Nothing else needs to know that
 * messages are fenced by divider lines or that input arrives from the keyboard,
 * so moving Billy to a window later would be a change to this class alone.
 *
 * <p>Each message is printed as a block between two dividers, so the user can
 * always see where Billy's answer begins and ends.
 */
public class Ui {

    /** Horizontal line used to visually separate Billy's messages from the rest of the output. */
    private static final String DIVIDER =
            "____________________________________________________________";

    /** ASCII art of the chatbot's name, shown once when Billy starts up. */
    private static final String BANNER =
            " ____  _ _ _       \n"
            + "| __ )(_) | |_   _ \n"
            + "|  _ \\| | | | | | |\n"
            + "| |_) | | | | |_| |\n"
            + "|____/|_|_|_|\\__, |\n"
            + "             |___/ ";

    /** Where the user's commands are read from. */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Names a number of tasks with the matching plural, e.g. {@code 1 task} or
     * {@code 3 tasks}.
     *
     * <p>Lives here because it is a matter of wording, and is shared by the
     * commands that change the list and by the message shown when a saved list
     * is read, which would otherwise each spell the plural out for themselves.
     *
     * @param count how many tasks there are
     * @return the count with the right noun after it
     */
    public static String describeListSize(int count) {
        return count + (count == 1 ? " task" : " tasks");
    }

    /**
     * Says how many tasks are now stored, e.g.
     * {@code Now you have 3 tasks in the list.}
     *
     * @param count how many tasks there are
     * @return the sentence to append to a confirmation
     */
    public static String describeNewListSize(int count) {
        return "Now you have " + describeListSize(count) + " in the list.";
    }

    /** Prints the startup banner and welcomes the user. */
    public void showWelcome() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hey there! Billy here, at your service.");
        System.out.println("I track todos, deadlines and events. Type 'list' to see them all.");
        System.out.println(DIVIDER);
    }

    /** Prints Billy's farewell before the program exits. */
    public void showGoodbye() {
        System.out.println("Catch you later! Don't be a stranger.");
        System.out.println(DIVIDER);
    }

    /**
     * Prints one message as a block between divider lines.
     *
     * @param message what to say; may span several lines
     */
    public void show(String message) {
        System.out.println(DIVIDER);
        System.out.println(message);
        System.out.println(DIVIDER);
    }

    /**
     * Prints something that went wrong.
     *
     * <p>Errors look the same as any other message today, but they are asked for
     * by a name of their own so that the callers read honestly and so that
     * setting them apart later — colour, a prefix — is a change here and nowhere
     * else.
     *
     * @param message what went wrong, phrased for the person typing the command
     */
    public void showError(String message) {
        show(message);
    }

    /**
     * Returns whether there is another command to read.
     *
     * <p>False once the input runs out, which is what happens when the user
     * presses Ctrl+D or a file of commands ends.
     *
     * @return whether the user has typed anything more
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command.
     *
     * <p>Surrounding spaces are removed here, so no caller has to remember to do
     * it before making sense of the line.
     *
     * @return the line the user typed, trimmed
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Stops reading input. Called once the conversation is over. */
    public void close() {
        scanner.close();
    }
}
