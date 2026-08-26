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
 * <p>Billy itself now only coordinates: {@link Parser} makes sense of what was
 * typed, {@link TaskList} holds the tasks, {@link Storage} keeps them on disk
 * and {@link Ui} does the talking. What is left here is deciding which of them
 * to ask, and in what order.
 */
public class Billy {

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
        Parser.ParsedCommand parsed = Parser.parse(line);
        Command command = parsed.command();
        String argument = parsed.argument();

        // Each branch reads as what it does, because anything that had to be made
        // sense of was made sense of before it got here.
        switch (command) {
        case LIST -> listTasks();
        case ON -> listTasksOn(Parser.parseDay(argument));
        case MARK -> setTaskDone(Parser.parseTaskNumber(argument, command), true);
        case UNMARK -> setTaskDone(Parser.parseTaskNumber(argument, command), false);
        case TODO -> addTask(Parser.parseTodo(argument));
        case DEADLINE -> addTask(Parser.parseDeadline(argument));
        case EVENT -> addTask(Parser.parseEvent(argument));
        case DELETE -> deleteTask(Parser.parseTaskNumber(argument, command));
        case BYE -> {
            return false;
        }
        }
        return true;
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
     * @param taskNumber the task's number as the user sees it, counting from 1
     * @throws BillyException if no task has that number
     */
    private static void deleteTask(int taskNumber) throws BillyException {
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
     * @param day the day to look at
     */
    private static void listTasksOn(LocalDate day) {
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
     * @param taskNumber the task's number as the user sees it, counting from 1
     * @param done the status to set: {@code true} for done, {@code false} for not done
     * @throws BillyException if no task has that number
     */
    private static void setTaskDone(int taskNumber, boolean done) throws BillyException {
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

}
