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

    @Test
    public void getGreeting_always_matchesWhatTheConsoleSays() {
        // The window and the console open with the same words; only the banner
        // around them differs. Spelling the greeting out here is what would
        // catch the two drifting apart.
        assertEquals("Hey there! Billy here, at your service.\n"
                + "I track todos, deadlines and events. Type 'list' to see them all.",
                Ui.getGreeting());
    }

    @Test
    public void getFarewell_always_matchesWhatTheConsoleSays() {
        assertEquals("Catch you later! Don't be a stranger.", Ui.getFarewell());
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
        ui.show("Got it. I've added this task:", "  [T][ ] read book", "Now you have 1 task.");
        assertEquals("Got it. I've added this task:\n  [T][ ] read book\nNow you have 1 task.",
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
}
