/**
 * Represents a task that runs from one date or time until another.
 *
 * <p>Both ends are kept as free text, so anything the user types is accepted,
 * e.g. {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}.
 */
public class Event extends Task {

    /** When the event starts, exactly as the user typed it. */
    protected String from;

    /** When the event ends, exactly as the user typed it. */
    protected String to;

    /**
     * Creates an event that is not done yet.
     *
     * @param description what the user wants to do
     * @param from when the event starts
     * @param to when the event ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns this event as one line of the save file, with the start and end as
     * fields of their own, e.g. {@code E | 0 | project meeting | Mon 2pm | 4pm}.
     */
    @Override
    public String toSaveFormat() {
        return "E" + FIELD_SEPARATOR + super.toSaveFormat()
                + FIELD_SEPARATOR + from + FIELD_SEPARATOR + to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
