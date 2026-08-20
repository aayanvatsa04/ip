/**
 * Represents a task that must be done before a particular date or time.
 *
 * <p>The due date is kept as free text, so anything the user types is accepted,
 * e.g. {@code [D][ ] return book (by: Sunday)}.
 */
public class Deadline extends Task {

    /** When the task is due, exactly as the user typed it. */
    protected String by;

    /**
     * Creates a deadline that is not done yet.
     *
     * @param description what the user wants to do
     * @param by when it needs to be done by
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
