/**
 * Removes a task from the list.
 */
public class DeleteCommand extends Command {

    /** The task's number as the user sees it, counting from 1. */
    private final int taskNumber;

    /**
     * Creates a command that will remove one task.
     *
     * @param taskNumber the task's number as the user sees it, counting from 1
     */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws BillyException {
        // remove returns the task it took out, so it can be shown in the confirmation.
        Task removed = tasks.remove(taskNumber);
        ui.show("Noted. I've removed this task:\n  " + removed + "\n"
                + Ui.describeNewListSize(tasks.size()));
        save(tasks, ui, storage);
    }
}
