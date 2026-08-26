import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the task list on the hard disk, so it survives Billy being closed.
 *
 * <p>The file is a plain text one, holding one task per line with its fields
 * separated by {@value Task#FIELD_SEPARATOR}:
 *
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | Sunday
 * E | 0 | project meeting | Mon 2pm | 4pm
 * </pre>
 *
 * <p>The first field is the type letter, the second is 1 for a finished task and
 * 0 for an unfinished one, and the rest are the task's own details. A readable
 * text format was chosen over Java's object serialisation because a person can
 * open it, understand it and fix it by hand — useful while the program is still
 * being built.
 *
 * <p>Nothing here assumes the file, or the folder holding it, already exists:
 * both are created when first needed, and a missing file simply means there is
 * nothing to load yet.
 */
public class Storage {

    /**
     * Matches {@value Task#FIELD_SEPARATOR} when reading a line back.
     *
     * <p>Spaces around the bar are optional here so that a file tidied up by hand
     * still loads.
     */
    private static final String FIELD_PATTERN = "\\s*\\|\\s*";

    /** The file the tasks are read from and written to. */
    private final Path file;

    /**
     * How many lines the last {@link #load()} could not make sense of.
     *
     * <p>Recorded rather than printed, so that the wording of the warning stays
     * with the rest of Billy's messages instead of being spread across classes.
     */
    private int skippedLineCount = 0;

    /**
     * Creates storage backed by a particular file.
     *
     * @param file where the tasks are kept; a relative path is resolved against
     *             the folder Billy is run from
     */
    public Storage(Path file) {
        this.file = file;
    }

    /** Returns the file the tasks are kept in, for use in messages to the user. */
    public Path getPath() {
        return file;
    }

    /** Returns how many lines the last {@link #load()} skipped as unreadable. */
    public int getSkippedLineCount() {
        return skippedLineCount;
    }

    /**
     * Reads the saved tasks back from the file.
     *
     * <p>A missing file is not a failure: it means nothing has been saved yet,
     * which is exactly what happens the first time Billy is run on a computer.
     *
     * <p>A line that cannot be understood is skipped rather than abandoning the
     * whole file, so one damaged line never costs the user the rest of their
     * list. {@link #getSkippedLineCount()} reports whether that happened.
     *
     * @return the tasks that were read, in the order they were saved
     * @throws BillyException if the file is there but cannot be read at all
     */
    public ArrayList<Task> load() throws BillyException {
        skippedLineCount = 0;
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.isRegularFile(file)) {
            return tasks;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new BillyException("I couldn't read " + file + " (" + e.getMessage()
                    + "), so I'm starting with an empty list.");
        }

        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            try {
                tasks.add(parseTask(line));
            } catch (BillyException e) {
                // The line is damaged. Count it and carry on with the rest.
                skippedLineCount++;
            }
        }
        return tasks;
    }

    /**
     * Writes the whole list out, replacing whatever the file held before.
     *
     * <p>Rewriting the file each time is simpler than working out which line
     * changed, and with the handful of tasks a person keeps it costs nothing
     * noticeable.
     *
     * @param tasks the tasks to store, in the order they should be read back
     * @throws IOException if the folder or the file cannot be written
     */
    public void save(List<Task> tasks) throws IOException {
        Path folder = file.getParent();
        if (folder != null) {
            // Does nothing if the folder is already there, so no need to check first.
            Files.createDirectories(folder);
        }

        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toSaveFormat());
        }
        // Creates the file if it is missing, and empties it if it is not.
        Files.write(file, lines);
    }

    /**
     * Turns one saved line back into the task it describes.
     *
     * @param line one line of the save file
     * @return the task that line stands for
     * @throws BillyException if the line is not in the expected format
     */
    private static Task parseTask(String line) throws BillyException {
        String type = line.split(FIELD_PATTERN, 2)[0].trim();

        // The type letter says how many fields the line should have. Splitting with
        // that limit means a '|' the user typed in the last field stays part of it
        // rather than being mistaken for a separator.
        String[] fields;
        Task task;
        switch (type) {
        case "T" -> {
            fields = splitFields(line, 3);
            task = new Todo(fields[2]);
        }
        case "D" -> {
            fields = splitFields(line, 4);
            task = new Deadline(fields[2], fields[3]);
        }
        case "E" -> {
            fields = splitFields(line, 5);
            task = new Event(fields[2], fields[3], fields[4]);
        }
        default -> throw new BillyException("unknown task type: " + type);
        }

        if (fields[1].equals("1")) {
            task.markAsDone();
        } else if (!fields[1].equals("0")) {
            throw new BillyException("the done flag must be 0 or 1, but was: " + fields[1]);
        }
        return task;
    }

    /**
     * Splits one saved line into its fields, checking that none are missing.
     *
     * @param line one line of the save file
     * @param fieldCount how many fields a line of this type should have
     * @return the trimmed fields
     * @throws BillyException if there are too few fields, or one of them is empty
     */
    private static String[] splitFields(String line, int fieldCount) throws BillyException {
        String[] fields = line.split(FIELD_PATTERN, fieldCount);
        if (fields.length < fieldCount) {
            throw new BillyException("expected " + fieldCount + " fields but found "
                    + fields.length);
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
            if (fields[i].isEmpty()) {
                throw new BillyException("field " + (i + 1) + " is empty");
            }
        }
        return fields;
    }
}
