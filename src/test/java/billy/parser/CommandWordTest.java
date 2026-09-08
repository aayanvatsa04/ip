package billy.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import billy.BillyException;

/**
 * Tests {@link CommandWord}, the set of words Billy accepts as commands.
 *
 * <p>Two promises are worth holding this enum to. The first is that the word a
 * user types finds its command whatever case they type it in, since
 * capitalization is their business and not Billy's.
 *
 * <p>The second is that the list shown when a command is not recognized is built
 * from the same values used to match it. That is the whole reason the keywords
 * live in an enum rather than as loose strings: the help can then never advertise
 * a command Billy does not accept, nor omit one it does.
 *
 * <p>The shorter words add a third: no word may invoke two different commands. A
 * clash there would be silent, since the first command declared would swallow the
 * word and the second would simply never be reached.
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
        assertEquals("I understand: todo, deadline, event, list, on, find, mark, unmark,"
                + " delete, bye.", CommandWord.describeAll());
    }

    // ---------------------------------------------------------------
    // Shorter words
    // ---------------------------------------------------------------

    @Test
    public void fromKeyword_alias_commandFound() throws BillyException {
        assertEquals(CommandWord.TODO, CommandWord.fromKeyword("t"));
        assertEquals(CommandWord.DELETE, CommandWord.fromKeyword("rm"));
        assertEquals(CommandWord.BYE, CommandWord.fromKeyword("q"));
    }

    @Test
    public void fromKeyword_aliasCapitalised_commandStillFound() throws BillyException {
        // Capitalization is the user's business for a short word as much as a long one.
        assertEquals(CommandWord.DEADLINE, CommandWord.fromKeyword("DL"));
        assertEquals(CommandWord.UNMARK, CommandWord.fromKeyword("Um"));
    }

    @Test
    public void fromKeyword_everyAlias_findsItsOwnCommand() throws BillyException {
        // Walking the values proves every declared alias reaches the command it
        // was declared on, rather than the handful someone remembered to test.
        for (CommandWord command : CommandWord.values()) {
            for (String alias : command.getAliases()) {
                assertEquals(command, CommandWord.fromKeyword(alias),
                        "the alias " + alias + " should reach " + command);
            }
        }
    }

    @Test
    public void keywordsAndAliases_acrossEveryCommand_allDistinct() {
        // A word declared on two commands would be swallowed by whichever is
        // declared first, and the second would quietly become unreachable by it.
        Set<String> seen = new HashSet<>();
        for (CommandWord command : CommandWord.values()) {
            assertTrue(seen.add(command.getKeyword()),
                    "the word " + command.getKeyword() + " invokes more than one command");
            for (String alias : command.getAliases()) {
                assertTrue(seen.add(alias),
                        "the word " + alias + " invokes more than one command");
            }
        }
    }

    @Test
    public void getAliases_commandWithNoShorterWord_empty() {
        // on is already as short as it is worth making it.
        assertTrue(CommandWord.ON.getAliases().isEmpty());
    }

    @Test
    public void getAliases_everyCommand_lowerCaseWordsDistinctFromTheKeyword() {
        // Not every alias is shorter: bye also answers to exit and quit, which are
        // longer but are what someone leaving a program reaches for first. What
        // must hold is that an alias is a single lower-case word, and a different
        // word from the keyword it stands beside.
        for (CommandWord command : CommandWord.values()) {
            for (String alias : command.getAliases()) {
                assertEquals(alias.toLowerCase(), alias);
                assertTrue(!alias.isBlank() && !alias.contains(" "));
                assertNotEquals(command.getKeyword(), alias);
            }
        }
    }

    @Test
    public void describeAll_always_namesOnlyTheFullKeywords() {
        // The shorter words are deliberately left out: this sentence is shown to
        // someone who has just got a command wrong.
        String described = CommandWord.describeAll();
        for (CommandWord command : CommandWord.values()) {
            for (String alias : command.getAliases()) {
                assertTrue(!described.contains(" " + alias + ",") && !described.contains(" " + alias + "."),
                        "the alias " + alias + " should not be advertised");
            }
        }
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
