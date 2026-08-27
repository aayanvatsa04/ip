package billy.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import billy.BillyException;

/**
 * Tests {@link TaskList}, which owns the user's tasks and the rules about them.
 *
 * <p>Two things here are easy to get wrong and expensive when wrong. The first is
 * counting: the numbers the user sees start at 1, while the list underneath
 * starts at 0, and this class is the one place that conversion happens. An
 * off-by-one would quietly mark or delete the task next to the one the user
 * meant, which is worse than an error because nothing looks broken.
 *
 * <p>The second is refusing a number that names nothing, which the commands rely
 * on so that they never have to check for themselves.
 */
public class TaskListTest {

    /** Builds a list holding the given descriptions as todos, in order. */
    private static TaskList listOf(String... descriptions) {
        TaskList tasks = new TaskList();
        for (String description : descriptions) {
            tasks.add(new Todo(description));
        }
        return tasks;
    }

    // ---------------------------------------------------------------
    // Building a list
    // ---------------------------------------------------------------

    @Test
    public void constructor_noArguments_listStartsEmpty() {
        TaskList tasks = new TaskList();
        assertTrue(tasks.isEmpty());
        assertEquals(0, tasks.size());
    }

    @Test
    public void constructor_existingTasks_listHoldsThemInOrder() {
        ArrayList<Task> loaded = new ArrayList<>();
        loaded.add(new Todo("read book"));
        loaded.add(new Todo("return book"));

        // This is the constructor Billy uses for a list read back from the file.
        TaskList tasks = new TaskList(loaded);
        assertEquals(2, tasks.size());
        assertFalse(tasks.isEmpty());
        assertEquals("[T][ ] read book", tasks.asList().get(0).toString());
    }

    @Test
    public void add_severalTasks_appendedInOrder() throws BillyException {
        TaskList tasks = listOf("first", "second", "third");
        assertEquals(3, tasks.size());
        // Added at the end each time, so the numbering follows the order typed.
        assertEquals("[T][ ] first", tasks.get(1).toString());
        assertEquals("[T][ ] third", tasks.get(3).toString());
    }

    // ---------------------------------------------------------------
    // Counting from 1, the way the user does
    // ---------------------------------------------------------------

    @Test
    public void get_firstTaskNumber_returnsFirstTask() throws BillyException {
        TaskList tasks = listOf("first", "second");
        // Task 1 is the first task, not the second: the classic off-by-one.
        assertEquals("[T][ ] first", tasks.get(1).toString());
    }

    @Test
    public void get_lastTaskNumber_returnsLastTask() throws BillyException {
        TaskList tasks = listOf("first", "second");
        // The other end of the same conversion: size() is a valid number here,
        // even though it is one past the end of the underlying list.
        assertEquals("[T][ ] second", tasks.get(2).toString());
    }

    @Test
    public void get_taskNumberJustPastTheEnd_exceptionThrown() {
        TaskList tasks = listOf("first", "second");
        assertThrows(BillyException.class, () -> tasks.get(3));
    }

    @Test
    public void get_zero_exceptionThrown() {
        // Nobody's list has a task 0, however natural the index feels in code.
        TaskList tasks = listOf("first");
        assertThrows(BillyException.class, () -> tasks.get(0));
    }

    @Test
    public void get_negativeNumber_exceptionThrown() {
        TaskList tasks = listOf("first");
        assertThrows(BillyException.class, () -> tasks.get(-1));
    }

    @Test
    public void get_taskInEmptyList_exceptionThrown() {
        assertThrows(BillyException.class, () -> new TaskList().get(1));
    }

    @Test
    public void get_emptyListAndOutOfRange_differentAdviceGiven() {
        BillyException onEmpty =
                assertThrows(BillyException.class, () -> new TaskList().get(1));
        BillyException outOfRange =
                assertThrows(BillyException.class, () -> listOf("first", "second").get(5));

        // "You have nothing" and "you have two" call for different advice, so the
        // two failures must not collapse into one message.
        assertNotEquals(onEmpty.getMessage(), outOfRange.getMessage());
        assertTrue(onEmpty.getMessage().contains("empty"));
        // The user is told which number was refused and how many they actually have.
        assertTrue(outOfRange.getMessage().contains("5"));
        assertTrue(outOfRange.getMessage().contains("2"));
    }

    @Test
    public void get_validNumber_returnsTheStoredTaskItself() throws BillyException {
        // The same object, not a copy: marking what comes back has to change what
        // is in the list, which is how MarkCommand works.
        Todo stored = new Todo("read book");
        TaskList tasks = new TaskList();
        tasks.add(stored);
        assertSame(stored, tasks.get(1));
    }

    // ---------------------------------------------------------------
    // Removing
    // ---------------------------------------------------------------

    @Test
    public void remove_middleTask_returnedAndRestClosesUp() throws BillyException {
        TaskList tasks = listOf("first", "second", "third");

        Task removed = tasks.remove(2);
        // The removed task is handed back so it can be named in the confirmation.
        assertEquals("[T][ ] second", removed.toString());
        assertEquals(2, tasks.size());
        // What followed it moves up, so the numbering stays unbroken.
        assertEquals("[T][ ] first", tasks.get(1).toString());
        assertEquals("[T][ ] third", tasks.get(2).toString());
    }

    @Test
    public void remove_onlyTask_listBecomesEmpty() throws BillyException {
        TaskList tasks = listOf("first");
        tasks.remove(1);
        assertTrue(tasks.isEmpty());
        assertEquals(0, tasks.size());
    }

    @Test
    public void remove_taskInEmptyList_exceptionThrown() {
        assertThrows(BillyException.class, () -> new TaskList().remove(1));
    }

    @Test
    public void remove_taskNumberPastTheEnd_exceptionThrown() {
        TaskList tasks = listOf("first");
        assertThrows(BillyException.class, () -> tasks.remove(2));
    }

    @Test
    public void remove_taskNumberPastTheEnd_listLeftUntouched() {
        TaskList tasks = listOf("first", "second");
        assertThrows(BillyException.class, () -> tasks.remove(9));
        // A refused removal must not have taken anything out on its way to failing.
        assertEquals(2, tasks.size());
    }

    // ---------------------------------------------------------------
    // Handing the tasks out
    // ---------------------------------------------------------------

    @Test
    public void asList_addingThroughTheView_exceptionThrown() {
        TaskList tasks = listOf("first");
        // Callers get a view they cannot change, so no task can be slipped in
        // behind the checks this class makes.
        assertThrows(UnsupportedOperationException.class,
            () -> tasks.asList().add(new Todo("sneaked in")));
    }

    @Test
    public void asList_removingThroughTheView_exceptionThrown() {
        TaskList tasks = listOf("first");
        assertThrows(UnsupportedOperationException.class, () -> tasks.asList().remove(0));
    }

    @Test
    public void asList_emptyList_noTasks() {
        assertTrue(new TaskList().asList().isEmpty());
    }
}
