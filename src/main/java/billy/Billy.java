package billy;

import java.nio.file.Path;
import java.util.ArrayList;

import billy.command.Command;
import billy.parser.Parser;
import billy.storage.Storage;
import billy.task.TaskList;
import billy.ui.Ui;

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
 * <p>Billy itself now only coordinates: {@link Parser} turns what was typed into
 * a {@link Command}, which carries itself out against the {@link TaskList}, the
 * {@link Storage} and the {@link Ui}. Billy neither knows nor cares which
 * commands exist, so adding one leaves this class untouched.
 *
 * <p>A Billy is an object rather than a collection of static methods, so its
 * helpers are settled once when it is built and cannot be swapped underneath it
 * afterwards. Two Billys over different files could run without either knowing
 * about the other.
 */
public class Billy {

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

    /** Reads the task list from its file and writes it back again. */
    private final Storage storage;

    /** Says everything the user sees, and reads everything the user types. */
    private final Ui ui;

    /** The user's tasks, as they stood when Billy started, and as they change. */
    private final TaskList tasks;

    /**
     * What stopped the saved list being read, or null if nothing did.
     *
     * <p>Remembered rather than reported on the spot, because the greeting has to
     * come first and a constructor has no business writing to the screen. Its one
     * job is to leave the object ready to work; saying so is {@link #run()}'s.
     */
    private final String loadError;

    /**
     * Builds a Billy that keeps its tasks in a particular file.
     *
     * <p>Whatever goes wrong while reading, Billy is left able to work: a missing,
     * unreadable or damaged file costs at most the tasks it held, never the
     * session. That is why the failure is recorded rather than thrown.
     *
     * @param filePath where the task list is kept between runs
     */
    public Billy(Path filePath) {
        ui = new Ui();
        storage = new Storage(filePath);

        TaskList loaded;
        String failure;
        try {
            loaded = new TaskList(storage.load());
            failure = null;
        } catch (BillyException e) {
            loaded = new TaskList();
            failure = e.getMessage();
        }
        tasks = loaded;
        loadError = failure;
    }

    public static void main(String[] args) {
        new Billy(DATA_FILE).run();
    }

    /**
     * Holds the conversation, from the greeting to the farewell.
     *
     * <p>Everything this needs was settled when the object was built, so a Billy
     * can be made and run separately, and more than one could exist over
     * different files.
     */
    public void run() {
        ui.showWelcome();
        reportStartup();

        // Each command says for itself whether it was the last one, so this loop
        // never has to know which commands exist.
        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            try {
                Command command = Parser.parse(ui.readCommand());
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (BillyException e) {
                // Every problem with a command surfaces here, so this is the one
                // place a failure becomes a message. The loop carries on, so a
                // mistake never ends the conversation.
                ui.showError(e.getMessage());
            }
        }
        ui.close();
        ui.showGoodbye();
    }

    /**
     * Says what happened while the saved list was being read.
     *
     * <p>Nothing is said when there is nothing to report, so a first run on a
     * fresh computer looks exactly as it did before saving existed.
     */
    private void reportStartup() {
        if (loadError != null) {
            ui.showError(loadError);
            return;
        }

        ArrayList<String> notes = new ArrayList<>();
        if (!tasks.isEmpty()) {
            notes.add("Welcome back! I've loaded " + Ui.describeListSize(tasks.size())
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
}
