package billy.command;

import java.util.ArrayList;
import java.util.List;

import billy.storage.Storage;
import billy.task.Task;
import billy.task.TaskList;
import billy.ui.Ui;

/**
 * Shows the tasks whose description mentions a keyword.
 *
 * <p>This is how a task is found again once the list has grown past what fits
 * on a screen, when the user remembers a word of what they wrote but not where
 * it sits.
 *
 * <p>Each match keeps the number it has in the full list, so a task found this
 * way can be marked or deleted straight away without running {@code list} first
 * to look its number up. That is the same promise {@link OnCommand} makes, and
 * the two would be confusing if only one of them kept it.
 */
public class FindCommand extends Command {

    /** The word being looked for, as the user typed it. */
    private final String keyword;

    /**
     * Creates a command that will look for one word.
     *
     * @param keyword the word to look for
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        List<Task> all = tasks.asList();
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            // Each task decides for itself whether its description matches.
            if (all.get(i).descriptionContains(keyword)) {
                found.add((i + 1) + "." + all.get(i));
            }
        }

        if (found.isEmpty()) {
            // The word is quoted back, so a typo in the search is easy to spot.
            ui.show("Nothing in your list mentions '" + keyword + "'.");
            return;
        }
        ui.show("Here are the matching tasks in your list:\n" + String.join("\n", found));
    }
}
