package billy.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import billy.BillyException;
import billy.command.AddCommand;
import billy.command.DeleteCommand;
import billy.command.ExitCommand;
import billy.command.ListCommand;
import billy.command.MarkCommand;
import billy.command.OnCommand;

/**
 * Tests {@link Parser}, which turns a typed line into the command it asks for.
 *
 * <p>The parser is the only part of Billy that deals with raw text, and it makes
 * a promise to everything behind it: what comes out is either a command already
 * known to be sound, or an exception. Nothing half-built ever escapes. These
 * tests hold it to both halves of that promise.
 *
 * <p>The refusals matter as much as the successes here. A parser that accepted
 * {@code deadline return book} without a due date would push the failure into a
 * class that has no idea what the user typed, and so no way to explain it.
 */
public class ParserTest {

    // ---------------------------------------------------------------
    // Each keyword reaches the command that carries it out
    // ---------------------------------------------------------------

    @Test
    public void parse_todo_addCommandReturned() throws BillyException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo borrow book"));
    }

    @Test
    public void parse_deadline_addCommandReturned() throws BillyException {
        // All three kinds of task are added by the same command, since adding is
        // the same act whatever is being added.
        assertInstanceOf(AddCommand.class,
                Parser.parse("deadline return book /by 2019-12-02 1800"));
    }

    @Test
    public void parse_event_addCommandReturned() throws BillyException {
        assertInstanceOf(AddCommand.class,
                Parser.parse("event project meeting /from 2019-12-02 1400 /to 2019-12-02 1600"));
    }

    @Test
    public void parse_list_listCommandReturned() throws BillyException {
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
    }

    @Test
    public void parse_on_onCommandReturned() throws BillyException {
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-12-02"));
    }

    @Test
    public void parse_mark_markCommandReturned() throws BillyException {
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 2"));
    }

    @Test
    public void parse_unmark_markCommandReturned() throws BillyException {
        // Marking done and undone differ only in a flag, so they share a class.
        assertInstanceOf(MarkCommand.class, Parser.parse("unmark 2"));
    }

    @Test
    public void parse_delete_deleteCommandReturned() throws BillyException {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 3"));
    }

    @Test
    public void parse_bye_exitCommandThatEndsTheConversation() throws BillyException {
        assertTrue(Parser.parse("bye").isExit());
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
    }

    @Test
    public void parse_anyOtherCommand_conversationContinues() throws BillyException {
        // Only one command ends the conversation; the rest must all say so.
        assertFalse(Parser.parse("list").isExit());
        assertFalse(Parser.parse("todo borrow book").isExit());
        assertFalse(Parser.parse("delete 1").isExit());
    }

    // ---------------------------------------------------------------
    // How forgiving the parser is about spacing and capitals
    // ---------------------------------------------------------------

    @Test
    public void parse_keywordInCapitals_stillRecognised() throws BillyException {
        // Capitalisation is the user's business, not Billy's.
        assertInstanceOf(ListCommand.class, Parser.parse("LIST"));
        assertInstanceOf(AddCommand.class, Parser.parse("ToDo borrow book"));
    }

    @Test
    public void parse_extraSpacesAfterKeyword_ignored() throws BillyException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo    borrow book"));
    }

    @Test
    public void parse_onWithATimeOfDay_timeIgnoredRatherThanRefused() throws BillyException {
        // A whole day is being asked about, so naming an hour is harmless.
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-12-02 1800"));
    }

    // ---------------------------------------------------------------
    // Lines that name no command
    // ---------------------------------------------------------------

    @Test
    public void parse_emptyLine_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse(""));
    }

    @Test
    public void parse_unknownKeyword_messageListsWhatBillyUnderstands() {
        BillyException thrown =
                assertThrows(BillyException.class, () -> Parser.parse("blah"));
        assertTrue(thrown.getMessage().contains("blah"));
        // Being told what is accepted is the difference between a dead end and a
        // second try, so the keywords have to appear in the message.
        assertTrue(thrown.getMessage().contains("todo"));
        assertTrue(thrown.getMessage().contains("bye"));
    }

    // ---------------------------------------------------------------
    // Commands named correctly but typed wrongly
    // ---------------------------------------------------------------

    @Test
    public void parse_todoWithoutDescription_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("todo"));
        assertThrows(BillyException.class, () -> Parser.parse("todo    "));
    }

    @Test
    public void parse_deadlineWithoutSeparator_exceptionThrown() {
        // Without /by there is no due date, and a deadline without one is not a
        // deadline. Guessing where the date starts is not the parser's job.
        assertThrows(BillyException.class, () -> Parser.parse("deadline return book"));
    }

    @Test
    public void parse_deadlineWithoutDescription_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("deadline /by 2019-12-02"));
    }

    @Test
    public void parse_deadlineWithoutDate_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("deadline return book /by"));
    }

    @Test
    public void parse_deadlineWithUnreadableDate_exceptionThrown() {
        assertThrows(BillyException.class,
            () -> Parser.parse("deadline return book /by someday"));
    }

    @Test
    public void parse_eventWithoutFrom_exceptionThrown() {
        assertThrows(BillyException.class,
            () -> Parser.parse("event meeting /to 2019-12-02 1600"));
    }

    @Test
    public void parse_eventWithoutTo_exceptionThrown() {
        assertThrows(BillyException.class,
            () -> Parser.parse("event meeting /from 2019-12-02 1400"));
    }

    @Test
    public void parse_eventEndingBeforeItStarts_exceptionThrown() {
        // Refused here rather than stored, because such an event covers no days
        // at all and so could never be found again by the `on` command.
        assertThrows(BillyException.class,
            () -> Parser.parse("event meeting /from 2019-12-02 1600 /to 2019-12-02 1400"));
    }

    @Test
    public void parse_markWithoutNumber_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("mark"));
    }

    @Test
    public void parse_markWithWordInsteadOfNumber_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("mark two"));
    }

    @Test
    public void parse_markWithoutNumber_messageNamesTheCommandTyped() {
        // The example given back should name the command the user actually typed,
        // rather than always suggesting the same one.
        BillyException thrown =
                assertThrows(BillyException.class, () -> Parser.parse("unmark"));
        assertTrue(thrown.getMessage().contains("unmark"));
    }

    @Test
    public void parse_deleteWithWordInsteadOfNumber_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("delete last"));
    }

    @Test
    public void parse_onWithoutDay_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("on"));
    }

    @Test
    public void parse_onWithUnreadableDay_exceptionThrown() {
        assertThrows(BillyException.class, () -> Parser.parse("on someday"));
    }
}
