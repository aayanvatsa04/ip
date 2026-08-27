package billy.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import billy.BillyException;

/**
 * Tests {@link CommandWord}, the set of words Billy accepts as commands.
 *
 * <p>Two promises are worth holding this enum to. The first is that the word a
 * user types finds its command whatever case they type it in, since
 * capitalisation is their business and not Billy's.
 *
 * <p>The second is that the list shown when a command is not recognised is built
 * from the same values used to match it. That is the whole reason the keywords
 * live in an enum rather than as loose strings: the help can then never advertise
 * a command Billy does not accept, nor omit one it does.
 */
public class CommandWordTest {

    @Test
    public void fromKeyword_exactWord_commandFound() throws BillyException {
        assertEquals(CommandWord.TODO, CommandWord.fromKeyword("todo"));
        assertEquals(CommandWord.BYE, CommandWord.fromKeyword("bye"));
    }

    @Test
    public void fromKeyword_capitalised_commandStillFound() throws BillyException {
        assertEquals(CommandWord.LIST, CommandWord.fromKeyword("LIST"));
        assertEquals(CommandWord.DEADLINE, CommandWord.fromKeyword("DeAdLiNe"));
    }

    @Test
    public void fromKeyword_everyKeywordBillyAdvertises_findsItsOwnCommand()
            throws BillyException {
        // Walking the values proves the match and the keyword agree for all of
        // them, rather than for the handful someone remembered to test.
        for (CommandWord command : CommandWord.values()) {
            assertEquals(command, CommandWord.fromKeyword(command.getKeyword()));
        }
    }

    @Test
    public void fromKeyword_unknownWord_exceptionThrown() {
        assertThrows(BillyException.class, () -> CommandWord.fromKeyword("blah"));
    }

    @Test
    public void fromKeyword_emptyWord_exceptionThrown() {
        assertThrows(BillyException.class, () -> CommandWord.fromKeyword(""));
    }

    @Test
    public void fromKeyword_wordWithATypo_exceptionThrown() {
        // Near misses are refused rather than guessed at, so Billy never acts on
        // a command the user did not give.
        assertThrows(BillyException.class, () -> CommandWord.fromKeyword("todos"));
        assertThrows(BillyException.class, () -> CommandWord.fromKeyword("mrak"));
    }

    @Test
    public void fromKeyword_unknownWord_messageQuotesItAndListsTheAlternatives() {
        BillyException thrown =
                assertThrows(BillyException.class, () -> CommandWord.fromKeyword("blah"));
        assertTrue(thrown.getMessage().contains("blah"));
        assertTrue(thrown.getMessage().contains(CommandWord.describeAll()));
    }

    @Test
    public void describeAll_always_namesEveryCommandThatExists() {
        String described = CommandWord.describeAll();
        // Built from the same values used to match, so this cannot fall out of
        // step with what Billy actually accepts.
        for (CommandWord command : CommandWord.values()) {
            assertTrue(described.contains(command.getKeyword()),
                    "the keyword " + command.getKeyword() + " should be advertised");
        }
    }

    @Test
    public void describeAll_always_readsAsASentenceInTheDeclaredOrder() {
        assertEquals("I understand: todo, deadline, event, list, on, mark, unmark, delete, bye.",
                CommandWord.describeAll());
    }

    @Test
    public void getKeyword_everyCommand_lowerCaseWordWithNoSpaces() {
        for (CommandWord command : CommandWord.values()) {
            String keyword = command.getKeyword();
            assertEquals(keyword.toLowerCase(), keyword);
            assertTrue(!keyword.isBlank() && !keyword.contains(" "));
        }
    }
}
