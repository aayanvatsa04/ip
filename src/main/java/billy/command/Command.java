package billy.command;

import java.io.IOException;

import billy.BillyException;
import billy.storage.Storage;
import billy.task.TaskList;
import billy.ui.Ui;

/**
 * Something the user has asked Billy to do, ready to be carried out.
 *
 * <p>A command is built by {@link billy.parser.Parser Parser} from what was typed, and holds
 * whatever it needs: the task to add, the number to delete, the day to look at.
 * By the time it exists it is known to be valid, so carrying it out is simply a
 * matter of asking it to.
 *
 * <p>Each kind of command is its own subclass, which is what replaced a switch
 * over every command Billy knows. Adding a command is now a new class rather
 * than another branch in a method that grows forever, and no existing command
 * has to be touched for it.
 *
 * <p>The three helpers a command may need are handed to {@link #execute} rather
 * than held as fields, so a command carries only what makes it that command, and
 * can be built without knowing which Billy will run it.
 */
public abstract class Command {

    /**
     * Carries out this command.
     *
     * @param tasks the list to work on
     * @param ui how to tell the user what happened
     * @param storage where the list is kept between runs
     * @throws BillyException if the command cannot be carried out after all,
     *                        such as a task number that names nothing
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws BillyException;

    /**
     * Returns whether the conversation should end after this command.
     *
     * <p>Only one command says yes, so that is the exception rather than
     * something every subclass has to answer.
     *
     * @return whether the conversation should end after this command
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Writes the task list out, so the next run starts where this one left off.
     *
     * <p>Shared by the commands that change the list. A failure is reported
     * rather than thrown, because the change itself did work: the user should
     * still see the confirmation, alongside a warning that it will not outlive
     * this session.
     *
     * @param tasks the list to write
     * @param ui how to warn the user if writing fails
     * @param storage where to write it
     */
    protected void save(TaskList tasks, Ui ui, Storage storage) {
        try {
            storage.save(tasks.asList());
        } catch (IOException e) {
            ui.showError("I couldn't save your list to " + storage.getPath() + " ("
                    + Storage.describeFailure(e) + ").",
                    "The change is still here, but it will be lost when Billy closes.");
        }
    }
}
