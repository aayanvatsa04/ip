import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Billy is a friendly chatbot that keeps a list of tasks for the user.
 *
 * <p>This is the Level-8 increment: tasks come in three types (todo, deadline and
 * event), and can be listed, marked as done, and marked as not done again.
 * Anything Billy cannot make sense of is reported as a {@link BillyException}
 * rather than crashing. Typing {@code bye} ends the conversation.
 *
 * <p>The list is kept on the hard disk by {@link Storage}: it is read back when
 * Billy starts and written out again after every change, so closing Billy no
 * longer loses the user's tasks.
 *
 * <p>Deadlines and events carry real dates rather than free text, so Billy can
 * tidy up how they are shown and answer {@code on} with the tasks falling on a
 * given day.
 *
 * <p>Talking to the user is left to {@link Ui}, the file to {@link Storage} and
 * the tasks themselves to {@link TaskList}, so what is left here is the work of
 * deciding what each command means and asking the right helper to carry it out.
 */
public class Billy {

    /** Separates a deadline's description from its due date. */
    private static final String BY_SEPARATOR = "/by";

    /** Separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = "/from";

    /** Separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = "/to";

    /** Shown alongside an error to remind the user how a todo is typed. */
    private static final String TODO_USAGE = "Try: todo borrow book";

    /** Shown alongside an error to remind the user how a deadline is typed. */
    private static final String DEADLINE_USAGE =
            "Try: deadline return book /by 2019-12-02 1800";

    /** Shown alongside an error to remind the user how an event is typed. */
    private static final String EVENT_USAGE =
            "Try: event project meeting /from 2019-12-02 1400 /to 2019-12-02 1600";

    /** Shown alongside an error to remind the user how a day is asked about. */
    private static final String ON_USAGE = "Try: on 2019-12-02";

    /**
     * The user's tasks.
     *
     * <p>Replaced wholesale when the saved list is read at startup, which is why
     * it is not final.
     */
    private static TaskList tasks = new TaskList();

    /**
     * Where the task list is kept between runs.
     *
     * <p>{@link Path#of(String, String...)} joins the parts with whatever separator
     * the computer uses, so this works on macOS, Linux and Windows alike; writing
     * {@code "data/billy.txt"} by hand would not. The path is relative, so Billy
     * keeps its data beside wherever it is run from rather than in a fixed place
     * that may not exist on someone else's computer.
     */
    private static final Path DATA_FILE = Path.of("data", "billy.txt");

    /** Reads the task list from {@link #DATA_FILE} and writes it back again. */
    private static final Storage storage = new Storage(DATA_FILE);

    /** Says everything the user sees, and reads everything the user types. */
    private static final Ui ui = new Ui();

    public static void main(String[] args) {
        ui.showWelcome();
        loadSavedTasks();
        runCommandLoop();
        ui.showGoodbye();
    }

    /**
     * Fills the task list with whatever was saved during an earlier run.
     *
     * <p>Whatever goes wrong here, Billy carries on with as much of the list as it
     * managed to read: a missing, unreadable or damaged file is worth a word to the
     * user, but it should never stop them from working.
     *
     * <p>Nothing is said at all when there is nothing to report, so a first run on
     * a fresh computer looks exactly as it did before saving existed.
     */
    private static void loadSavedTasks() {
        try {
            tasks = new TaskList(storage.load());
        } catch (BillyException e) {
            ui.showError(e.getMessage());
            return;
        }

        ArrayList<String> notes = new ArrayList<>();
        if (!tasks.isEmpty()) {
            notes.add("Welcome back! I've loaded " + describeTaskCount(tasks.size())
                    + " from your last session.");
        }
        int skipped = storage.getSkippedLineCount();
        if (skipped > 0) {
            notes.add("Heads up: I skipped " + skipped + (skipped == 1 ? " line" : " lines")
                    + " in " + storage.getPath() + " that I couldn't understand.");
        }
        if (!notes.isEmpty()) {
            ui.show(String.join("\n", notes));
        }
    }

    /**
     * Writes the task list to disk, so the next run starts where this one left off.
     *
     * <p>Called after every change to the list. A failure is reported rather than
     * thrown, because the change itself did work: the user should still see the
     * confirmation, alongside a warning that it will not outlive this session.
     */
    private static void saveTasks() {
        try {
            storage.save(tasks.asList());
        } catch (IOException e) {
            ui.showError("I couldn't save your list to " + storage.getPath() + " ("
                    + Storage.describeFailure(e) + ").\nThe change is still here, but it will be"
                    + " lost when Billy closes.");
        }
    }

    /**
     * Reads commands from the user and acts on them one at a time.
     *
     * <p>Every problem with a command surfaces here as a {@link BillyException},
     * so this is the single place that turns a failure into a message on screen.
     * Because the loop continues afterwards, a mistake never ends the conversation.
     *
     * <p>Stops when the user types {@code bye}, or when there is no more input to
     * read (for example, if the user presses Ctrl+D).
     */
    private static void runCommandLoop() {
        while (ui.hasNextCommand()) {
            try {
                // handleCommand reports whether the conversation should carry on.
                if (!handleCommand(ui.readCommand())) {
                    break;
                }
            } catch (BillyException e) {
                ui.showError(e.getMessage());
            }
        }
        ui.close();
    }

    /**
     * Works out what the user asked for and carries it out.
     *
     * <p>The first word decides the action. A word Billy does not recognise is
     * reported as an error, so a mistyped command is never stored as a task.
     *
     * @param line one whole line as the user typed it, already trimmed
     * @return {@code true} to carry on reading commands, {@code false} once the
     *         user has said goodbye
     * @throws BillyException if the command cannot be carried out as typed
     */
    private static boolean handleCommand(String line) throws BillyException {
        if (line.isEmpty()) {
            throw new BillyException("You'll have to give me something to work with!");
        }

        // Split into the keyword and everything after it, e.g. "mark 2" -> "mark", "2".
        String[] parts = line.split("\\s+", 2);
        Command command = Command.fromKeyword(parts[0]);
        String argument = parts.length > 1 ? parts[1] : "";

        switch (command) {
        case LIST -> listTasks();
        case ON -> listTasksOn(argument);
        case MARK -> setTaskDone(argument, true);
        case UNMARK -> setTaskDone(argument, false);
        case TODO -> addTodo(argument);
        case DEADLINE -> addDeadline(argument);
        case EVENT -> addEvent(argument);
        case DELETE -> deleteTask(argument);
        case BYE -> {
            return false;
        }
        }
        return true;
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
        // Reading the date here means a task can never hold one that isn't real.
        addTask(new Deadline(parts[0], TaskDate.parse(parts[1])));
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
        addTask(new Event(descriptionAndRest[0], TaskDate.parse(startAndEnd[0]),
                TaskDate.parse(startAndEnd[1])));
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

    /** Stores an already-built task, confirms it, and saves the new list. */
    private static void addTask(Task task) {
        tasks.add(task);
        ui.show("Got it. I've added this task:\n  " + task + "\n" + taskCountSummary());
        saveTasks();
    }

    /** Describes how many tasks are now stored, e.g. {@code Now you have 3 tasks in the list.} */
    private static String taskCountSummary() {
        return "Now you have " + describeTaskCount(tasks.size()) + " in the list.";
    }

    /**
     * Names a number of tasks with the matching plural, e.g. {@code 1 task} or
     * {@code 3 tasks}.
     */
    private static String describeTaskCount(int count) {
        return count + (count == 1 ? " task" : " tasks");
    }

    /**
     * Removes the task the user named from the list.
     *
     * @param argument the text after the keyword, expected to be a task number
     * @throws BillyException if the argument does not name a task that exists
     */
    private static void deleteTask(String argument) throws BillyException {
        int taskNumber = parseTaskNumber(argument, Command.DELETE);
        // remove returns the task it took out, so it can be shown in the confirmation.
        Task removed = tasks.remove(taskNumber);
        ui.show("Noted. I've removed this task:\n  " + removed + "\n" + taskCountSummary());
        saveTasks();
    }

    /** Prints every stored task, numbered from 1. */
    private static void listTasks() {
        if (tasks.isEmpty()) {
            ui.show("Your list is empty. Nothing to do... suspicious.");
            return;
        }
        List<Task> all = tasks.asList();
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            // List positions start at 0, but people count from 1.
            lines.add((i + 1) + "." + all.get(i));
        }
        ui.show("Here are the tasks in your list:\n" + String.join("\n", lines));
    }

    /**
     * Prints the tasks that fall on one particular day.
     *
     * <p>Each task is shown with the number it has in the full list, so that a
     * task found this way can be marked or deleted straight away without having
     * to run {@code list} first to look its number up.
     *
     * <p>A time of day may be given but is ignored: a whole day is being asked
     * about, so {@code on 2019-12-02 1800} means the same as {@code on 2019-12-02}.
     *
     * @param argument the text after the keyword, expected to be a date
     * @throws BillyException if no date is given, or it cannot be read as one
     */
    private static void listTasksOn(String argument) throws BillyException {
        if (argument.isBlank()) {
            throw new BillyException("Which day should I look at? " + ON_USAGE);
        }
        LocalDate day = TaskDate.parse(argument).getDate();

        List<Task> all = tasks.asList();
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            // Each task decides for itself whether it falls on the day.
            if (all.get(i).occursOn(day)) {
                found.add((i + 1) + "." + all.get(i));
            }
        }

        String shownDay = TaskDate.formatDate(day);
        if (found.isEmpty()) {
            ui.show("Nothing on " + shownDay + ". Enjoy the day off!");
            return;
        }
        ui.show("Here's what you have on " + shownDay + ":\n" + String.join("\n", found));
    }

    /**
     * Sets the done status of the task the user named.
     *
     * <p>Both {@code mark} and {@code unmark} share this method, as they differ
     * only in the status they set and the wording they report.
     *
     * @param argument the text the user typed after the keyword, expected to be a task number
     * @param done the status to set: {@code true} for done, {@code false} for not done
     * @throws BillyException if the argument does not name a task that exists
     */
    private static void setTaskDone(String argument, boolean done) throws BillyException {
        int taskNumber = parseTaskNumber(argument, done ? Command.MARK : Command.UNMARK);
        Task task = tasks.get(taskNumber);
        String confirmation;
        if (done) {
            task.markAsDone();
            confirmation = "Nice! I've marked this task as done:";
        } else {
            task.markAsNotDone();
            confirmation = "OK, I've marked this task as not done yet:";
        }
        ui.show(confirmation + "\n  " + task);
        saveTasks();
    }

    /**
     * Reads what the user typed as a task number.
     *
     * <p>Only the reading happens here. Whether the number names a task that
     * exists is for {@link TaskList} to say, since it is the one that knows.
     *
     * @param argument the text the user typed after the keyword
     * @param command the command the number belongs to, used to give a fitting example
     * @return the number as the user wrote it, counting from 1
     * @throws BillyException if the text is not a number at all
     */
    private static int parseTaskNumber(String argument, Command command) throws BillyException {
        try {
            return Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            // Covers both a missing number ("mark") and a non-number ("mark two").
            throw new BillyException(
                    "I need a task number, like '" + command.getKeyword() + " 2'.");
        }
    }

}
