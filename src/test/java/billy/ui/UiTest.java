package billy.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("You've got 1 task now.", Ui.describeNewListSize(1));
    }

    @Test
    public void describeNewListSize_severalTasks_pluralSentence() {
        assertEquals("You've got 2 tasks now.", Ui.describeNewListSize(2));
    }

    @Test
    public void describeNewListSize_noTasks_pluralSentence() {
        // Said after deleting the last task, so it is a real case, not a spare one.
        assertEquals("You've got 0 tasks now.", Ui.describeNewListSize(0));
    }

    @Test
    public void describeNewListSize_anyCount_wordedThroughDescribeListSize() {
        // The sentence is built from the same phrase used elsewhere, so the two
        // cannot disagree about how a count is worded.
        assertEquals("You've got " + Ui.describeListSize(7) + " now.",
                Ui.describeNewListSize(7));
    }

    @Test
    public void getGreeting_always_matchesWhatTheConsoleSays() {
        // The window and the console open with the same words; only the banner
        // around them differs. Spelling the greeting out here is what would
        // catch the two drifting apart.
        assertEquals("Hey there! Billy here. I'll remember your tasks so you don't have to.\n"
                + "Todos, deadlines and events. Type 'list' whenever you want to see them.",
                Ui.getGreeting());
    }

    @Test
    public void getFarewell_always_matchesWhatTheConsoleSays() {
        assertEquals("Catch you later! Your list will be right here when you get back.",
                Ui.getFarewell());
    }

    @Test
    public void stopCollecting_messagesShown_returnedWithoutDividers() {
        Ui ui = new Ui();
        ui.startCollecting();
        ui.show("first");
        ui.show("second");
        assertEquals("first\nsecond", ui.stopCollecting());
    }

    @Test
    public void stopCollecting_errorShown_returnedLikeAnyOtherMessage() {
        Ui ui = new Ui();
        ui.startCollecting();
        ui.showError("something went wrong");
        assertEquals("something went wrong", ui.stopCollecting());
    }

    @Test
    public void startCollecting_afterAnEarlierRound_nothingCarriedOver() {
        Ui ui = new Ui();
        ui.startCollecting();
        ui.show("first");
        ui.stopCollecting();

        ui.startCollecting();
        ui.show("second");
        assertEquals("second", ui.stopCollecting());
    }

    @Test
    public void show_severalLines_joinedIntoOneMessage() {
        // The lines are the message, not several messages: what the user sees is
        // one block, exactly as if the newlines had been typed by the caller.
        Ui ui = new Ui();
        ui.startCollecting();
        ui.show("Alright, added:", "  [T][ ] read book", "You've got 1 task now.");
        assertEquals("Alright, added:\n  [T][ ] read book\nYou've got 1 task now.",
                ui.stopCollecting());
    }

    @Test
    public void show_oneLine_noNewlineAdded() {
        Ui ui = new Ui();
        ui.startCollecting();
        ui.show("Your list is empty.");
        assertEquals("Your list is empty.", ui.stopCollecting());
    }

    @Test
    public void show_lineThatItselfSpansLines_leftAsItIs() {
        // A caller that already has a joined block can still pass it whole, which
        // is what the commands listing tasks do.
        Ui ui = new Ui();
        ui.startCollecting();
        ui.show("Heading:", "1.first\n2.second");
        assertEquals("Heading:\n1.first\n2.second", ui.stopCollecting());
    }

    @Test
    public void showError_severalLines_joinedIntoOneMessage() {
        Ui ui = new Ui();
        ui.startCollecting();
        ui.showError("I couldn't save your list.", "The change will be lost when Billy closes.");
        assertEquals("I couldn't save your list.\nThe change will be lost when Billy closes.",
                ui.stopCollecting());
    }

    @Test
    public void isErrorCollected_onlyOrdinaryMessages_false() {
        Ui ui = new Ui();
        ui.startCollecting();
        ui.show("Here's what you're on the hook for:");
        assertFalse(ui.isErrorCollected());
    }

    @Test
    public void isErrorCollected_errorShown_true() {
        // This is what tells the window to show the reply as a failure, and the
        // collected text itself no longer says so: showError and show produce
        // exactly the same string.
        Ui ui = new Ui();
        ui.startCollecting();
        ui.showError("I don't know what 'blah' means.");
        assertTrue(ui.isErrorCollected());
    }

    @Test
    public void isErrorCollected_errorThenOrdinaryMessage_stillTrue() {
        // A command that half worked still went wrong, so a later ordinary
        // message must not quietly clear the mark.
        Ui ui = new Ui();
        ui.startCollecting();
        ui.showError("I couldn't save your list.");
        ui.show("You've got 1 task now.");
        assertTrue(ui.isErrorCollected());
    }

    @Test
    public void startCollecting_afterAnError_markCleared() {
        // The mark belongs to one round of collecting. Left standing, every
        // reply after the user's first mistake would be shown as a failure.
        Ui ui = new Ui();
        ui.startCollecting();
        ui.showError("I don't know what 'blah' means.");
        ui.stopCollecting();

        ui.startCollecting();
        ui.show("Here's what you're on the hook for:");
        assertFalse(ui.isErrorCollected());
    }

    @Test
    public void show_noLines_assertionFails() {
        // show takes its lines as varargs, so show() compiles. It would print an
        // empty block between two dividers, which reads as Billy having nothing
        // to say rather than as the mistake it is.
        Ui ui = new Ui();
        assertThrows(AssertionError.class, () -> ui.show());
    }

    @Test
    public void stopCollecting_withoutStartCollecting_assertionFails() {
        // The window calls these two in pairs. Unpaired, this would be a bare
        // NullPointerException from inside Ui with nothing to say which caller
        // forgot to start.
        assertThrows(AssertionError.class, () -> new Ui().stopCollecting());
    }
}
