package billy.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Task}, the parts every kind of task has in common.
 *
 * <p>Two things are pinned here. The first is that a task's description and
 * whether it is done stay together and can be changed only through the class,
 * since a status that drifts from its task is a bug the user sees as Billy
 * lying to them.
 *
 * <p>The second is the shape of the two ways a task writes itself out. Every
 * subclass builds on the base version, so an accidental change here would
 * quietly reword every task in the list and rewrite every line of the save file.
 */
public class TaskTest {

    @Test
    public void constructor_newTask_startsNotDone() {
        Task task = new Task("read book");
        // Nothing is done the moment it is added, whatever else may be true.
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[ ] read book", task.toString());
    }

    @Test
    public void markAsDone_notDoneTask_statusChanges() {
        Task task = new Task("read book");
        task.markAsDone();
        assertEquals("X", task.getStatusIcon());
        assertEquals("[X] read book", task.toString());
    }

    @Test
    public void markAsDone_alreadyDoneTask_staysDone() {
        // Marking twice is the user's prerogative and must not toggle back.
        Task task = new Task("read book");
        task.markAsDone();
        task.markAsDone();
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    public void markAsNotDone_doneTask_statusChangesBack() {
        Task task = new Task("read book");
        task.markAsDone();
        task.markAsNotDone();
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[ ] read book", task.toString());
    }

    @Test
    public void markAsNotDone_taskThatWasNeverDone_staysNotDone() {
        Task task = new Task("read book");
        task.markAsNotDone();
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    public void toSaveFormat_notDoneTask_flagIsZero() {
        assertEquals("0 | read book", new Task("read book").toSaveFormat());
    }

    @Test
    public void toSaveFormat_doneTask_flagIsOne() {
        Task task = new Task("read book");
        task.markAsDone();
        // The subclasses put their type letter in front of exactly this.
        assertEquals("1 | read book", task.toSaveFormat());
    }

    @Test
    public void occursOn_plainTask_neverOnAnyDay() {
        // A plain task carries no date, so it belongs to no day. Answering here
        // rather than asking each task its type is what lets `on` search the
        // whole list without knowing which kinds of task exist.
        Task task = new Task("read book");
        assertFalse(task.occursOn(LocalDate.of(2019, 12, 2)));
        assertFalse(task.occursOn(LocalDate.of(1999, 1, 1)));
    }

    @Test
    public void toString_descriptionWithSpaces_keptAsTyped() {
        // The description is the user's words and is not tidied up.
        assertEquals("[ ] read the  green book", new Task("read the  green book").toString());
    }
}
