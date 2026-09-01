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
     * Collects what Billy says instead of printing it, or null while Billy is
     * talking to the console.
     *
     * <p>The window needs Billy's answer as a piece of text it can put in a
     * dialog box, not as something already written to a console that is not
     * there. Collecting it here keeps that difference in the one class whose
     * job is how Billy's words reach the user.
     */
    private StringBuilder transcript = null;

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

    /**
     * Returns the words Billy opens with, without the banner around them.
     *
     * <p>Asked for by name so that the window can open with the same greeting
     * the console does. The banner is left out of it because ASCII art of the
     * name only lines up in a font whose letters are all the same width.
     *
     * @return the greeting, spanning two lines.
     */
    public static String getGreeting() {
        return "Hey there! Billy here, at your service.\n"
                + "I track todos, deadlines and events. Type 'list' to see them all.";
    }

    /**
     * Returns Billy's sign-off.
     *
     * @return the farewell, on one line.
     */
    public static String getFarewell() {
        return "Catch you later! Don't be a stranger.";
    }

    /** Prints the startup banner and welcomes the user. */
    public void showWelcome() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println(getGreeting());
        System.out.println(DIVIDER);
    }

    /** Prints Billy's farewell before the program exits. */
    public void showGoodbye() {
        System.out.println(getFarewell());
        System.out.println(DIVIDER);
    }

    /**
     * Starts collecting what Billy says rather than printing it.
     *
     * <p>Called before each command the window carries out, so that the answer
     * can be handed back as text instead of disappearing into a console.
     */
    public void startCollecting() {
        transcript = new StringBuilder();
    }

    /**
     * Returns everything said since collecting started, and goes back to
     * printing.
     *
     * @return what Billy said, with no divider lines and no trailing blank line.
     */
    public String stopCollecting() {
        String collected = transcript.toString().strip();
        transcript = null;
        return collected;
    }

    /**
     * Prints one message as a block between divider lines.
     *
     * @param message what to say; may span several lines
     */
    public void show(String message) {
        if (transcript != null) {
            // No dividers: a dialog box already shows where the message ends.
            transcript.append(message).append("\n");
            return;
        }
        System.out.println(DIVIDER);
        System.out.println(message);
        System.out.println(DIVIDER);
    }

    /**
     * Prints something that went wrong.
     *
     * <p>Errors look the same as any other message today, but they are asked for
     * by a name of their own so that the callers read honestly and so that
     * setting them apart later — color, a prefix — is a change here and nowhere
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
