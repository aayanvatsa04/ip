import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the tasks falling on one particular day.
 *
 * <p>Each match keeps the number it has in the full list, so a task found this
 * way can be marked or deleted straight away without running {@code list} first
 * to look its number up.
 */
public class OnCommand extends Command {

    /** The day being asked about. */
    private final LocalDate day;

    /**
     * Creates a command that will look at one day.
     *
     * @param day the day to look at
     */
    public OnCommand(LocalDate day) {
        this.day = day;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
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
}
