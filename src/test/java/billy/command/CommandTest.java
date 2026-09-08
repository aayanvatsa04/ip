package billy.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import billy.task.TaskList;
import billy.task.Todo;

/**
 * Tests the numbering {@link Command} shares with the commands that show part of
 * the list.
 *
 * <p>The number beside a task is its place in the whole list, not its place among
 * the matches. That is what lets the user mark or delete a task straight after
 * finding it, and it is the one thing about this method that is easy to get wrong
 * and expensive when wrong: numbering the results instead would send {@code mark}
 * to a different task than the one the user just read.
 */
public class CommandTest {

    /** Builds a list holding the given descriptions as todos, in order. */
    private static TaskList listOf(String... descriptions) {
        TaskList tasks = new TaskList();
        for (String description : descriptions) {
            tasks.add(new Todo(description));
        }
        return tasks;
    }

    @Test
    public void numberMatching_someTasksMatch_numbersFollowTheWholeList() {
        TaskList tasks = listOf("read book", "return book", "read paper");

        List<String> found = Command.numberMatching(tasks,
                task -> task.descriptionContains("read"));

        // The second match is task 3 of the list, not task 2 of the results.
        assertEquals(List.of("1.[T][ ] read book", "3.[T][ ] read paper"), found);
    }

    @Test
    public void numberMatching_lastTaskOnlyMatch_keepsItsOwnNumber() {
        TaskList tasks = listOf("first", "second", "third");

        List<String> found = Command.numberMatching(tasks,
                task -> task.descriptionContains("third"));

        assertEquals(List.of("3.[T][ ] third"), found);
    }

    @Test
    public void numberMatching_everyTaskMatches_numberedFromOne() {
        TaskList tasks = listOf("first", "second");

        // This is how ListCommand asks for the whole list.
        List<String> lines = Command.numberMatching(tasks, task -> true);

        assertEquals(List.of("1.[T][ ] first", "2.[T][ ] second"), lines);
    }

    @Test
    public void numberMatching_nothingMatches_noLines() {
        TaskList tasks = listOf("read book");

        assertTrue(Command.numberMatching(tasks, task -> false).isEmpty());
    }

    @Test
    public void numberMatching_emptyList_noLines() {
        assertTrue(Command.numberMatching(new TaskList(), task -> true).isEmpty());
    }
}
