package billy.command;

import billy.storage.Storage;
import billy.task.Task;
import billy.task.TaskList;
import billy.ui.Ui;

/**
 * Adds a task to the list.
 *
 * <p>One class covers todos, deadlines and events alike, because adding is the
 * same act whatever is being added. Which kind of task it is was settled by
 * {@link billy.parser.Parser Parser}, and the task itself knows how to describe
 * and store itself, so there is nothing here that differs between the three.
 */
public class AddCommand extends Command {

    /** The task to add, already built and known to be valid. */
    private final Task task;

    /**
     * Creates a command that will add one task.
     *
     * @param task the task to add
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * Adds the task to the list, says so, and writes the list out.
     *
     * <p>The confirmation names the task and how many there now are, so the user
     * can see both what was added and where that leaves them.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        tasks.add(task);
        ui.show("Got it. I've added this task:\n  " + task + "\n"
                + Ui.describeNewListSize(tasks.size()));
        save(tasks, ui, storage);
    }
}
