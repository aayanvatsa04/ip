import java.util.Scanner;

/**
 * Billy is a friendly chatbot that keeps a list of tasks for the user.
 *
 * <p>This is the Level-2 increment: any text the user types is stored as a task,
 * {@value #LIST_COMMAND} shows everything stored so far, and
 * {@value #EXIT_COMMAND} ends the conversation.
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

    /** The most tasks Billy can hold, as the list is a fixed-size array. */
    private static final int MAX_TASKS = 100;

    /** Stored tasks. Only the first {@code taskCount} slots hold real values. */
    private static final String[] tasks = new String[MAX_TASKS];

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
        System.out.println("Tell me anything and I'll remember it. Type 'list' to see it all.");
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

    /** Works out what the user asked for and carries it out. */
    private static void handleCommand(String command) {
        if (command.isEmpty()) {
            reply("You'll have to give me something to work with!");
        } else if (command.equalsIgnoreCase(LIST_COMMAND)) {
            listTasks();
        } else {
            addTask(command);
        }
    }

    /** Stores a task, unless the list is already full. */
    private static void addTask(String task) {
        if (taskCount == MAX_TASKS) {
            reply("My memory is full at " + MAX_TASKS + " tasks. Time to get some done!");
            return;
        }
        tasks[taskCount] = task;
        taskCount++;
        reply("added: " + task);
    }

    /** Prints every stored task, numbered from 1. */
    private static void listTasks() {
        if (taskCount == 0) {
            reply("Your list is empty. Nothing to do... suspicious.");
            return;
        }
        System.out.println(DIVIDER);
        for (int i = 0; i < taskCount; i++) {
            // Array indices start at 0, but people count from 1.
            System.out.println((i + 1) + ". " + tasks[i]);
        }
        System.out.println(DIVIDER);
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
