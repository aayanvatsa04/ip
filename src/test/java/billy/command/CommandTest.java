package billy.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import billy.BillyException;
import billy.parser.CommandWord;
import billy.storage.Storage;
import billy.task.TaskList;
import billy.task.Todo;
import billy.ui.Ui;

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

    // ---------------------------------------------------------------
    // help
    // ---------------------------------------------------------------

    @Test
    public void execute_help_showsEveryCommandAndShorterWord() {
        // Worth testing through the command rather than only through
        // CommandWord, since a HelpCommand that built the listing and forgot to
        // show it would pass every test over there.
        Ui ui = new Ui();
        ui.startCollecting();
        new HelpCommand().execute(new TaskList(), ui, new Storage(Path.of("unused.txt")));
        String shown = ui.stopCollecting();

        for (CommandWord command : CommandWord.values()) {
            assertTrue(shown.contains(command.getKeyword()), command.getKeyword() + " should be shown");
            for (String alias : command.getAliases()) {
                assertTrue(shown.contains(alias), alias + " should be shown");
            }
        }
    }

    @Test
    public void execute_help_touchesNeitherTheListNorTheSaveFile() {
        // The only command that does neither. Storage points at a file that does
        // not exist and must stay that way: writing to it would be a bug the
        // user would only find when it overwrote something.
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        Path unwritten = Path.of("build", "should-not-be-written.txt");

        Ui ui = new Ui();
        ui.startCollecting();
        new HelpCommand().execute(tasks, ui, new Storage(unwritten));
        ui.stopCollecting();

        assertEquals(1, tasks.size());
        assertFalse(Files.exists(unwritten), "help must not write a save file");
    }

    // ---------------------------------------------------------------
    // What Billy remarks on
    // ---------------------------------------------------------------

    /** Runs a command against a list and returns everything Billy said. */
    private static String say(Command command, TaskList tasks) throws BillyException {
        Ui ui = new Ui();
        ui.startCollecting();
        command.execute(tasks, ui, new Storage(Path.of("build", "command-test.txt")));
        return ui.stopCollecting();
    }

    @Test
    public void execute_markTheLastOutstandingTask_saysSoOutright() throws BillyException {
        TaskList tasks = listOf("read book");
        assertTrue(say(new MarkCommand(1, true), tasks).contains("whole list conquered"));
    }

    @Test
    public void execute_markOneOfSeveral_noSuchRemark() throws BillyException {
        // The remark must wait for the last one. Firing on every mark would make
        // it meaningless, which is the easy way to get this wrong.
        TaskList tasks = listOf("read book", "write essay");
        assertFalse(say(new MarkCommand(1, true), tasks).contains("whole list conquered"));
    }

    @Test
    public void execute_deleteTheLastTask_saysTheListIsEmpty() throws BillyException {
        TaskList tasks = listOf("read book");
        String said = say(new DeleteCommand(1), tasks);
        assertTrue(said.contains("last of them"), said);
        // A count of zero would be the wrong thing to report here.
        assertFalse(said.contains("0 tasks"), said);
    }

    @Test
    public void execute_deleteOneOfSeveral_reportsTheCountInstead() throws BillyException {
        TaskList tasks = listOf("read book", "write essay");
        String said = say(new DeleteCommand(1), tasks);
        assertTrue(said.contains("1 task on the books"), said);
        assertFalse(said.contains("last of them"), said);
    }

    // ---------------------------------------------------------------
    // Warning about a duplicate
    // ---------------------------------------------------------------

    @Test
    public void execute_addATaskAlreadyOnTheList_warnsAndNamesTheUndo() throws BillyException {
        TaskList tasks = listOf("read book");
        String said = say(new AddCommand(new Todo("read book")), tasks);

        assertTrue(said.contains("same as task 1"), said);
        // The number offered must be the new task, not the original: deleting
        // the original would leave the accidental copy sitting in its place.
        assertTrue(said.contains("delete 2"), said);
        // Still added. Refusing it would decide for the user that two errands
        // worded alike cannot both be real.
        assertEquals(2, tasks.size());
    }

    @Test
    public void execute_addATaskMatchingSeveral_namesThemAll() throws BillyException {
        TaskList tasks = listOf("read book", "write essay", "read book");
        String said = say(new AddCommand(new Todo("read book")), tasks);
        assertTrue(said.contains("same as tasks 1 and 3"), said);
        assertTrue(said.contains("delete 4"), said);
    }

    @Test
    public void execute_addAnUnrelatedTask_noWarning() throws BillyException {
        TaskList tasks = listOf("read book");
        assertFalse(say(new AddCommand(new Todo("write essay")), tasks).contains("Heads up"));
    }
}
