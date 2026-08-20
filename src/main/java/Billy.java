import java.util.Scanner;

/**
 * Billy is a friendly chatbot that keeps a list of tasks for the user.
 *
 * <p>This is the Level-4 increment: tasks come in three types (todo, deadline and
 * event), and can be listed, marked as done, and marked as not done again.
 * Typing {@value #EXIT_COMMAND} ends the conversation.
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

    /** Separates a deadline's description from its due date. */
    private static final String BY_SEPARATOR = "/by";

    /** Separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = "/from";

    /** Separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = "/to";

    /** The most tasks Billy can hold, as the list is a fixed-size array. */
    private static final int MAX_TASKS = 100;

    /** Returned by {@link #parseTaskNumber} when the user did not give a usable task number. */
    private static final int INVALID_TASK_NUMBER = -1;

    /** Stored tasks. Only the first {@code taskCount} slots hold real values. */
    private static final Task[] tasks = new Task[MAX_TASKS];

    /** How many slots of {@link #tasks} are currently in use. */
    private static int taskCount = 0;

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
                handleCommand(command);
            }
        }
    }

    /**
     * Works out what the user asked for and carries it out.
     *
     * <p>The first word decides the action; anything that is not a known keyword
     * is stored as a new task.
     */
    private static void handleCommand(String command) {
        if (command.isEmpty()) {
            reply("You'll have to give me something to work with!");
            return;
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
        // Bare text with no keyword is taken as a todo, as it has no date attached.
        default -> addTodo(command);
        }
    }

    /**
     * Builds a todo from the user's input and stores it.
     *
     * @param description what the user wants to do
     */
    private static void addTodo(String description) {
        if (description.isBlank()) {
            reply("A todo needs a description, like:\n  todo borrow book");
            return;
        }
        addTask(new Todo(description.trim()));
    }

    /**
     * Builds a deadline from the user's input and stores it.
     *
     * @param argument the text after the keyword, expected as {@code <description> /by <date>}
     */
    private static void addDeadline(String argument) {
        String[] parts = splitOn(argument, BY_SEPARATOR);
        if (parts == null) {
            reply("A deadline needs a description and a due date, like:\n"
                    + "  deadline return book /by Sunday");
            return;
        }
        addTask(new Deadline(parts[0], parts[1]));
    }

    /**
     * Builds an event from the user's input and stores it.
     *
     * @param argument the text after the keyword, expected as
     *                 {@code <description> /from <start> /to <end>}
     */
    private static void addEvent(String argument) {
        String[] descriptionAndRest = splitOn(argument, FROM_SEPARATOR);
        if (descriptionAndRest == null) {
            replyEventUsage();
            return;
        }
        // The start and end times are still joined together, so split them apart too.
        String[] startAndEnd = splitOn(descriptionAndRest[1], TO_SEPARATOR);
        if (startAndEnd == null) {
            replyEventUsage();
            return;
        }
        addTask(new Event(descriptionAndRest[0], startAndEnd[0], startAndEnd[1]));
    }

    /** Explains the expected form of an event command. */
    private static void replyEventUsage() {
        reply("An event needs a description, a start and an end, like:\n"
                + "  event project meeting /from Mon 2pm /to 4pm");
    }

    /**
     * Splits input around a separator such as {@value #BY_SEPARATOR}.
     *
     * @param input the text to split
     * @param separator the marker to split around
     * @return the two trimmed halves, or {@code null} if the separator is missing
     *         or either half is empty
     */
    private static String[] splitOn(String input, String separator) {
        int separatorPosition = input.indexOf(separator);
        if (separatorPosition == -1) {
            return null;
        }
        String before = input.substring(0, separatorPosition).trim();
        String after = input.substring(separatorPosition + separator.length()).trim();
        if (before.isEmpty() || after.isEmpty()) {
            return null;
        }
        return new String[] {before, after};
    }

    /** Stores an already-built task, unless the list is already full. */
    private static void addTask(Task task) {
        if (taskCount == MAX_TASKS) {
            reply("My memory is full at " + MAX_TASKS + " tasks. Time to get some done!");
            return;
        }
        tasks[taskCount] = task;
        taskCount++;
        reply("Got it. I've added this task:\n  " + task
                + "\nNow you have " + taskCount + (taskCount == 1 ? " task" : " tasks")
                + " in the list.");
    }

    /** Prints every stored task, numbered from 1. */
    private static void listTasks() {
        if (taskCount == 0) {
            reply("Your list is empty. Nothing to do... suspicious.");
            return;
        }
        System.out.println(DIVIDER);
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            // Array indices start at 0, but people count from 1.
            System.out.println((i + 1) + "." + tasks[i]);
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
     */
    private static void setTaskDone(String argument, boolean done) {
        int index = parseTaskNumber(argument);
        if (index == INVALID_TASK_NUMBER) {
            return; // parseTaskNumber has already explained the problem.
        }
        Task task = tasks[index];
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
     * @return the matching 0-based index, or {@link #INVALID_TASK_NUMBER} if the
     *         text was not a number or not a task that exists
     */
    private static int parseTaskNumber(String argument) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            // Covers both a missing number ("mark") and a non-number ("mark two").
            reply("I need a task number, like 'mark 2'.");
            return INVALID_TASK_NUMBER;
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            reply("There's no task " + taskNumber + " on your list. You have " + taskCount + ".");
            return INVALID_TASK_NUMBER;
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
