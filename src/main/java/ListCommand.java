import java.util.ArrayList;
import java.util.List;

/**
 * Shows every task in the list, numbered as the user refers to them.
 */
public class ListCommand extends Command {

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
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
}
