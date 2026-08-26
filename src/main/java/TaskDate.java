import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * A point in time that a task refers to: a date, and optionally a time of day.
 *
 * <p>The time is optional because both ways of speaking are natural. "Return the
 * book by the 2nd of December" fixes only a day, while "the meeting starts at 6pm
 * on the 2nd" fixes a moment. Storing a bare {@link LocalDateTime} would force a
 * time onto the first case, and Billy would then claim a deadline was due at
 * midnight when the user never said so.
 *
 * <p>The same text is read and written in three different shapes, and keeping all
 * three in this one class means they can never drift apart:
 *
 * <ul>
 *   <li><b>typed</b> by the user as {@code 2019-12-02} or {@code 2019-12-02 1800}</li>
 *   <li><b>shown</b> back as {@code Dec 2 2019} or {@code Dec 2 2019, 6:00pm}</li>
 *   <li><b>saved</b> in the same shape it was typed, so a file written today
 *       reads back correctly tomorrow</li>
 * </ul>
 *
 * <p>Instances never change once built, so a task can hand its date out without
 * any risk of a caller altering it.
 */
public class TaskDate {

    /** Shown alongside an error to remind the user how a date is typed. */
    public static final String USAGE =
            "Use yyyy-MM-dd or d/M/yyyy, and a 24-hour time if you want one,"
            + " e.g. 2019-12-02, 2/12/2019 or 2/12/2019 1800.";

    /**
     * Reads a date written the way it is spoken here, e.g. {@code 2/12/2019}
     * for the 2nd of December.
     *
     * <p>The day comes first, as it does in most of the world outside the United
     * States. A single digit is allowed for either part, so both {@code 2/12/2019}
     * and {@code 02/12/2019} are understood.
     */
    private static final DateTimeFormatter DAY_FIRST_TYPED =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Reads and writes the time of day as four digits, e.g. {@code 1800}. */
    private static final DateTimeFormatter TIME_STORED = DateTimeFormatter.ofPattern("HHmm");

    /**
     * Shows a date the way a person writes it, e.g. {@code Dec 2 2019}.
     *
     * <p>The locale is fixed to English so that the month is spelt the same way
     * on every computer, rather than following whatever language the user's
     * machine happens to be set to.
     */
    private static final DateTimeFormatter DATE_SHOWN =
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);

    /** Shows a time the way a person says it, e.g. {@code 6:00PM} before lowercasing. */
    private static final DateTimeFormatter TIME_SHOWN =
            DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);

    /** The day this refers to. Never null. */
    private final LocalDate date;

    /** The time of day, or null if the user gave only a date. */
    private final LocalTime time;

    private TaskDate(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Reads what the user typed as a date, with a time of day if one is given.
     *
     * @param text a date such as {@code 2019-12-02} or {@code 2/12/2019},
     *             optionally followed by a 24-hour time such as {@code 1800}
     * @return the date it describes
     * @throws BillyException if the text is not a date in either expected shape
     */
    public static TaskDate parse(String text) throws BillyException {
        String trimmed = text.trim();
        // At most two pieces, so trailing rubbish fails as a bad time rather than
        // being quietly ignored.
        String[] parts = trimmed.split("\\s+", 2);
        try {
            LocalTime time = parts.length > 1 ? LocalTime.parse(parts[1], TIME_STORED) : null;
            return new TaskDate(parseDate(parts[0]), time);
        } catch (DateTimeParseException e) {
            throw new BillyException("I couldn't read '" + trimmed + "' as a date. " + USAGE);
        }
    }

    /**
     * Reads the date part on its own, in either accepted shape.
     *
     * <p>The two shapes cannot be confused for one another, since one is
     * separated by dashes and the other by slashes, so trying them in turn is
     * safe. The dashed form is tried first because that is the one Billy writes
     * to the save file, and so the one seen most often.
     *
     * @param text just the date, with no time after it
     * @return the day it names
     * @throws DateTimeParseException if it is neither shape, nor a real date
     */
    private static LocalDate parseDate(String text) {
        if (text.contains("/")) {
            return LocalDate.parse(text, DAY_FIRST_TYPED);
        }
        return LocalDate.parse(text);
    }

    /**
     * Returns whether this comes before another point in time.
     *
     * <p>On two different days the days decide it. On the same day, a time can
     * only settle the order if both sides name one: a date given without a time
     * says nothing about the hour, so it is treated as expressing no preference
     * rather than as midnight, which Billy was never told.
     *
     * @param other the point in time to compare against
     * @return whether this is known to come first
     */
    public boolean isBefore(TaskDate other) {
        if (!date.equals(other.date)) {
            return date.isBefore(other.date);
        }
        if (time == null || other.time == null) {
            return false;
        }
        return time.isBefore(other.time);
    }

    /** Returns the day this falls on, ignoring any time of day. */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Writes a date the way Billy shows it to the user, e.g. {@code Dec 2 2019}.
     *
     * <p>Offered separately so that a message about a day can be worded the same
     * way as the dates on the tasks it lists.
     *
     * @param date the day to write out
     * @return the day as the user would read it
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_SHOWN);
    }

    /**
     * Returns this as one field of the save file, in the same shape the user
     * typed it, e.g. {@code 2019-12-02 1800}.
     *
     * <p>Writing it back exactly as it was read means {@link #parse(String)} can
     * be reused to load it, so there is only ever one date format to get right.
     */
    public String toSaveFormat() {
        return time == null ? date.toString() : date + " " + time.format(TIME_STORED);
    }

    /**
     * Returns this as the user reads it, e.g. {@code Dec 2 2019} or
     * {@code Dec 2 2019, 6:00pm}.
     */
    @Override
    public String toString() {
        if (time == null) {
            return formatDate(date);
        }
        // Java writes the marker as "PM"; lowercase reads more naturally in a sentence.
        return formatDate(date) + ", " + time.format(TIME_SHOWN).toLowerCase(Locale.ENGLISH);
    }
}
