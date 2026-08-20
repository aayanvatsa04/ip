import java.util.ArrayList;
import java.util.Scanner;

/**
 * Billy is a friendly chatbot that keeps a list of tasks for the user.
 *
 * <p>This is the Level-6 increment: tasks come in three types (todo, deadline and
 * event), and can be listed, marked as done, and marked as not done again.
 * Anything Billy cannot make sense of is reported as a {@link BillyException}
 * rather than crashing. Typing {@value #EXIT_COMMAND} ends the conversation.
 */
public class Billy {

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

    /** The command that makes Billy stop reading input and exit. */
    private static final String EXIT_COMMAND = "bye";

    /** The command that makes Billy print everything stored so far. */
    private static final String LIST_COMMAND = "list";

    /** The command that marks a task as done, e.g. {@code mark 2}. */
    private static final String MARK_COMMAND = "mark";

    /** The command that marks a task as not done again, e.g. {@code unmark 2}. */
    private static final String UNMARK_COMMAND = "unmark";

    /** The command that adds a task with no date attached, e.g. {@code todo borrow book}. */
    private static final String TODO_COMMAND = "todo";

    /** The command that adds a task with a due date, e.g. {@code deadline return book /by Sunday}. */
    private static final String DEADLINE_COMMAND = "deadline";

    /** The command that adds a task spanning a period, e.g. {@code event meeting /from 2pm /to 4pm}. */
    private static final String EVENT_COMMAND = "event";

    /** The command that removes a task from the list, e.g. {@code delete 3}. */
    private static final String DELETE_COMMAND = "delete";

    /** Separates a deadline's description from its due date. */
    private static final String BY_SEPARATOR = "/by";

    /** Separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = "/from";

    /** Separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = "/to";

    /** Shown alongside an error to remind the user how a todo is typed. */
    private static final String TODO_USAGE = "Try: todo borrow book";

    /** Shown alongside an error to remind the user how a deadline is typed. */
    private static final String DEADLINE_USAGE = "Try: deadline return book /by Sunday";

    /** Shown alongside an error to remind the user how an event is typed. */
    private static final String EVENT_USAGE = "Try: event project meeting /from Mon 2pm /to 4pm";

    /** Listed when the user types a keyword Billy does not recognise. */
    private static final String KNOWN_COMMANDS =
            "I understand: todo, deadline, event, list, mark, unmark, delete, bye.";

    /**
     * Stored tasks, in the order the user added them.
     *
     * <p>An {@link ArrayList} grows as needed, so there is no limit on how many
     * tasks Billy can hold, and it tracks its own size.
     */
    private static final ArrayList<Task> tasks = new ArrayList<>();

    public static void main(String[] args) {
        greet();
        runCommandLoop();
        sayGoodbye();
    }

    /** Prints the startup banner and welcomes the user. */
    private static void greet() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hey there! Billy here, at your service.");
        System.out.println("I track todos, deadlines and events. Type 'list' to see them all.");
        System.out.println(DIVIDER);
    }

    /**
     * Reads commands from the user and acts on them one at a time.
     *
     * <p>Every problem with a command surfaces here as a {@link BillyException},
     * so this is the single place that turns a failure into a message on screen.
     * Because the loop continues afterwards, a mistake never ends the conversation.
     *
     * <p>Stops when the user types {@value #EXIT_COMMAND}, or when there is no
     * more input to read (for example, if the user presses Ctrl+D).
     */
    private static void runCommandLoop() {
        // try-with-resources closes the Scanner automatically, even if we break out early.
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase(EXIT_COMMAND)) {
                    break;
                }
                try {
                    handleCommand(command);
                } catch (BillyException e) {
                    reply(e.getMessage());
                }
            }
        }
    }

    /**
     * Works out what the user asked for and carries it out.
     *
     * <p>The first word decides the action. A word Billy does not recognise is
     * reported as an error, so a mistyped command is never stored as a task.
     *
     * @throws BillyException if the command cannot be carried out as typed
     */
    private static void handleCommand(String command) throws BillyException {
        if (command.isEmpty()) {
            throw new BillyException("You'll have to give me something to work with!");
        }

        // Split into the keyword and everything after it, e.g. "mark 2" -> "mark", "2".
        String[] parts = command.split("\\s+", 2);
        String keyword = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : "";

        switch (keyword) {
        case LIST_COMMAND -> listTasks();
        case MARK_COMMAND -> setTaskDone(argument, true);
        case UNMARK_COMMAND -> setTaskDone(argument, false);
        case TODO_COMMAND -> addTodo(argument);
        case DEADLINE_COMMAND -> addDeadline(argument);
        case EVENT_COMMAND -> addEvent(argument);
        case DELETE_COMMAND -> deleteTask(argument);
        default -> throw new BillyException(
                "I don't know what '" + keyword + "' means. " + KNOWN_COMMANDS);
        }
    }

    /**
     * Builds a todo from the user's input and stores it.
     *
     * @param description what the user wants to do
     * @throws BillyException if the description is missing
     */
    private static void addTodo(String description) throws BillyException {
        if (description.isBlank()) {
            throw new BillyException("The description of a todo can't be empty. " + TODO_USAGE);
        }
        addTask(new Todo(description.trim()));
    }

    /**
     * Builds a deadline from the user's input and stores it.
     *
     * @param argument the text after the keyword, expected as {@code <description> /by <date>}
     * @throws BillyException if the due date or the description is missing
     */
    private static void addDeadline(String argument) throws BillyException {
        String[] parts = splitOn(argument, BY_SEPARATOR, "description", "due date", DEADLINE_USAGE);
        addTask(new Deadline(parts[0], parts[1]));
    }

    /**
     * Builds an event from the user's input and stores it.
     *
     * @param argument the text after the keyword, expected as
     *                 {@code <description> /from <start> /to <end>}
     * @throws BillyException if the description, the start or the end is missing
     */
    private static void addEvent(String argument) throws BillyException {
        String[] descriptionAndRest =
                splitOn(argument, FROM_SEPARATOR, "description", "start time", EVENT_USAGE);
        // The start and end times are still joined together, so split them apart too.
        String[] startAndEnd =
                splitOn(descriptionAndRest[1], TO_SEPARATOR, "start time", "end time", EVENT_USAGE);
        addTask(new Event(descriptionAndRest[0], startAndEnd[0], startAndEnd[1]));
    }

    /**
     * Splits input around a separator such as {@value #BY_SEPARATOR}.
     *
     * <p>The two halves are named by the caller so that a failure can say exactly
     * which part of the command is missing.
     *
     * @param input the text to split
     * @param separator the marker to split around
     * @param beforeName what the text before the separator means, e.g. "description"
     * @param afterName what the text after the separator means, e.g. "due date"
     * @param usage an example of the command, shown to help the user correct it
     * @return the two trimmed halves
     * @throws BillyException if the separator is missing or either half is empty
     */
    private static String[] splitOn(String input, String separator, String beforeName,
            String afterName, String usage) throws BillyException {
        int separatorPosition = input.indexOf(separator);
        if (separatorPosition == -1) {
            throw new BillyException("I need '" + separator + "' in that command. " + usage);
        }

        String before = input.substring(0, separatorPosition).trim();
        String after = input.substring(separatorPosition + separator.length()).trim();
        if (before.isEmpty()) {
            throw new BillyException("The " + beforeName + " can't be empty. " + usage);
        }
        if (after.isEmpty()) {
            throw new BillyException("The " + afterName + " can't be empty. " + usage);
        }
        return new String[] {before, after};
    }

    /** Stores an already-built task and confirms it. */
    private static void addTask(Task task) {
        tasks.add(task);
        reply("Got it. I've added this task:\n  " + task + "\n" + taskCountSummary());
    }

    /** Describes how many tasks are now stored, e.g. {@code Now you have 3 tasks in the list.} */
    private static String taskCountSummary() {
        int count = tasks.size();
        return "Now you have " + count + (count == 1 ? " task" : " tasks") + " in the list.";
    }

    /**
     * Removes the task the user named from the list.
     *
     * @param argument the text after the keyword, expected to be a task number
     * @throws BillyException if the argument does not name a task that exists
     */
    private static void deleteTask(String argument) throws BillyException {
        int index = parseTaskNumber(argument, DELETE_COMMAND);
        // remove returns the task it took out, so it can be shown in the confirmation.
        Task removed = tasks.remove(index);
        reply("Noted. I've removed this task:\n  " + removed + "\n" + taskCountSummary());
    }

    /** Prints every stored task, numbered from 1. */
    private static void listTasks() {
        if (tasks.isEmpty()) {
            reply("Your list is empty. Nothing to do... suspicious.");
            return;
        }
        System.out.println(DIVIDER);
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            // List indices start at 0, but people count from 1.
            System.out.println((i + 1) + "." + tasks.get(i));
        }
        System.out.println(DIVIDER);
    }

    /**
     * Sets the done status of the task the user named.
     *
     * <p>Both {@value #MARK_COMMAND} and {@value #UNMARK_COMMAND} share this method,
     * as they differ only in the status they set and the wording they report.
     *
     * @param argument the text the user typed after the keyword, expected to be a task number
     * @param done the status to set: {@code true} for done, {@code false} for not done
     * @throws BillyException if the argument does not name a task that exists
     */
    private static void setTaskDone(String argument, boolean done) throws BillyException {
        int index = parseTaskNumber(argument, done ? MARK_COMMAND : UNMARK_COMMAND);
        Task task = tasks.get(index);
        String confirmation;
        if (done) {
            task.markAsDone();
            confirmation = "Nice! I've marked this task as done:";
        } else {
            task.markAsNotDone();
            confirmation = "OK, I've marked this task as not done yet:";
        }
        reply(confirmation + "\n  " + task);
    }

    /**
     * Converts what the user typed into an index into {@link #tasks}.
     *
     * @param argument the text the user typed after the keyword
     * @param keyword the command the number belongs to, used to give a fitting example
     * @return the matching 0-based index
     * @throws BillyException if the text is not a number, or names a task that does not exist
     */
    private static int parseTaskNumber(String argument, String keyword) throws BillyException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            // Covers both a missing number ("mark") and a non-number ("mark two").
            throw new BillyException("I need a task number, like '" + keyword + " 2'.");
        }

        if (tasks.isEmpty()) {
            throw new BillyException("Your list is empty, so there's nothing to change.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new BillyException("There's no task " + taskNumber
                    + " on your list. You have " + tasks.size() + ".");
        }
        return taskNumber - 1;
    }

    /** Prints a single message wrapped in divider lines. */
    private static void reply(String message) {
        System.out.println(DIVIDER);
        System.out.println(message);
        System.out.println(DIVIDER);
    }

    /** Prints Billy's farewell message before the program exits. */
    private static void sayGoodbye() {
        System.out.println("Catch you later! Don't be a stranger.");
        System.out.println(DIVIDER);
    }
}
