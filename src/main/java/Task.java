import java.time.LocalDate;

/**
 * Represents a single task in the user's list.
 *
 * <p>A task bundles its description together with whether it has been completed,
 * so the two can never fall out of step with each other.
 *
 * <p>A task knows two ways to write itself out: {@link #toString()} for the
 * screen, and {@link #toSaveFormat()} for the file it is stored in between runs.
 */
public class Task {

    /**
     * Separates the fields of one task in the save file, e.g. {@code T | 1 | read book}.
     *
     * <p>Spaces around the bar make a saved file easy to read by eye; {@link Storage}
     * trims them away again when reading.
     */
    public static final String FIELD_SEPARATOR = " | ";

    /** What the user wants to do, exactly as they typed it. */
    protected String description;

    /** Whether the task has been completed. */
    protected boolean isDone;

    /**
     * Creates a task that is not done yet.
     *
     * @param description what the user wants to do
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the symbol shown inside the status box.
     *
     * @return {@code "X"} if the task is done, or a single space if it is not
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not completed yet. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns whether this task has anything to do with a particular day.
     *
     * <p>A plain task carries no date, so the answer is no. The kinds of task
     * that do carry dates answer for themselves, which lets Billy search the
     * list without having to ask what type each task is.
     *
     * @param day the day being asked about
     * @return whether this task falls on that day
     */
    public boolean occursOn(LocalDate day) {
        return false;
    }

    /**
     * Returns the parts of this task that every task has, written as they appear
     * in the save file: whether it is done, then its description.
     *
     * <p>Subclasses put their own type letter in front and add their own extra
     * fields behind, mirroring the way {@link #toString()} is built up. Keeping
     * each class responsible for its own fields means adding a new kind of task
     * never means editing the ones that already exist.
     *
     * @return e.g. {@code 1 | read book}
     */
    public String toSaveFormat() {
        return (isDone ? "1" : "0") + FIELD_SEPARATOR + description;
    }

    /**
     * Returns this task as a status box followed by its description,
     * e.g. {@code [X] read book}.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
