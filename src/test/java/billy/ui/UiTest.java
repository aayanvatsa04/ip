package billy.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the wording {@link Ui} works out, as opposed to the printing it does.
 *
 * <p>Most of this class writes to the screen and reads the keyboard, which is
 * covered end to end by the text UI test plan rather than here. What is worth
 * testing in isolation are the two methods that decide wording, because they are
 * ordinary logic wearing a message's clothes: they pick between "task" and
 * "tasks", and every command that changes the list says its confirmation through
 * them.
 *
 * <p>Getting the plural wrong is the kind of fault that never breaks anything and
 * is noticed by everyone who reads it.
 */
public class UiTest {

    @Test
    public void describeListSize_oneTask_singular() {
        assertEquals("1 task", Ui.describeListSize(1));
    }

    @Test
    public void describeListSize_severalTasks_plural() {
        assertEquals("3 tasks", Ui.describeListSize(3));
    }

    @Test
    public void describeListSize_noTasks_plural() {
        // "0 task" is the mistake a count-of-one check written the wrong way round
        // would produce, so zero is worth pinning as well as one.
        assertEquals("0 tasks", Ui.describeListSize(0));
    }

    @Test
    public void describeNewListSize_oneTask_singularSentence() {
        assertEquals("Now you have 1 task in the list.", Ui.describeNewListSize(1));
    }

    @Test
    public void describeNewListSize_severalTasks_pluralSentence() {
        assertEquals("Now you have 2 tasks in the list.", Ui.describeNewListSize(2));
    }

    @Test
    public void describeNewListSize_noTasks_pluralSentence() {
        // Said after deleting the last task, so it is a real case, not a spare one.
        assertEquals("Now you have 0 tasks in the list.", Ui.describeNewListSize(0));
    }

    @Test
    public void describeNewListSize_anyCount_wordedThroughDescribeListSize() {
        // The sentence is built from the same phrase used elsewhere, so the two
        // cannot disagree about how a count is worded.
        assertEquals("Now you have " + Ui.describeListSize(7) + " in the list.",
                Ui.describeNewListSize(7));
    }
}
