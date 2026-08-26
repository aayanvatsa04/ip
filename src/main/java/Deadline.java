import java.time.LocalDate;

/**
 * Represents a task that must be done before a particular date or time.
 *
 * <p>The due date is a real date rather than free text, so Billy can show it in
 * a tidy form and answer questions about which day it falls on,
 * e.g. {@code [D][ ] return book (by: Dec 2 2019, 6:00pm)}.
 */
public class Deadline extends Task {

    /** When the task is due. */
    protected TaskDate by;

    /**
     * Creates a deadline that is not done yet.
     *
     * @param description what the user wants to do
     * @param by when it needs to be done by
     */
    public Deadline(String description, TaskDate by) {
        super(description);
        this.by = by;
    }

    /** A deadline falls on the day it is due. */
    @Override
    public boolean occursOn(LocalDate day) {
        return by.getDate().equals(day);
    }

    /**
     * Returns this deadline as one line of the save file, with the due date as a
     * field of its own, e.g. {@code D | 0 | return book | 2019-12-02 1800}.
     */
    @Override
    public String toSaveFormat() {
        return "D" + FIELD_SEPARATOR + super.toSaveFormat()
                + FIELD_SEPARATOR + by.toSaveFormat();
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
