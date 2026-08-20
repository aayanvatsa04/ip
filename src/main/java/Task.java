/**
 * Represents a single task in the user's list.
 *
 * <p>A task bundles its description together with whether it has been completed,
 * so the two can never fall out of step with each other.
 */
public class Task {

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
     * Returns this task as a status box followed by its description,
     * e.g. {@code [X] read book}.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
