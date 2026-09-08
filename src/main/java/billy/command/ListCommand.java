package billy.command;

import java.util.List;
import java.util.stream.IntStream;

import billy.storage.Storage;
import billy.task.Task;
import billy.task.TaskList;
import billy.ui.Ui;

/**
 * Shows every task in the list, numbered as the user refers to them.
 */
public class ListCommand extends Command {

    /**
     * Shows every task, numbered the way the user refers to them.
     *
     * <p>An empty list gets a remark of its own rather than a heading with
     * nothing under it, which would read as though something had gone wrong.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        if (tasks.isEmpty()) {
            ui.show("Your list is empty. Nothing to do... suspicious.");
            return;
        }

        List<Task> all = tasks.asList();
        // Streaming the positions rather than the tasks keeps each task's number
        // available, since a stream of tasks alone could not say where each came from.
        List<String> lines = IntStream.range(0, all.size())
                // List positions start at 0, but people count from 1.
                .mapToObj(i -> (i + 1) + "." + all.get(i))
                .toList();
        ui.show("Here are the tasks in your list:", String.join("\n", lines));
    }
}
