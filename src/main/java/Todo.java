/**
 * Represents a task with no date or time attached to it.
 *
 * <p>A todo adds nothing to {@link Task} beyond its {@code [T]} label, e.g.
 * {@code [T][ ] borrow book}.
 */
public class Todo extends Task {

    /**
     * Creates a todo that is not done yet.
     *
     * @param description what the user wants to do
     */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
