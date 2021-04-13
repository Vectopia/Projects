package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the Alphabet class.
 *
 * @author Vector Wang
 */
public class AlphabetTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    @Test
    public void checkDefaultAlphabet() {
        Permutation test = new Permutation("", new Alphabet());
        assertEquals(26, test.alphabet().size());

        assertEquals('A', test.alphabet().toChar(0));
        assertEquals('Z', test.alphabet().toChar(25));
        assertNotEquals('A', test.alphabet().toChar(1));
        assertNotEquals('K', test.alphabet().toChar(22));

        assertEquals(0, test.alphabet().toInt('A'));
        assertEquals(25, test.alphabet().toInt('Z'));

        assertTrue(test.alphabet().contains('A'));
        assertTrue(test.alphabet().contains('B'));
        assertTrue(test.alphabet().contains('C'));
        assertTrue(test.alphabet().contains('D'));
        assertTrue(test.alphabet().contains('Z'));
    }

    @Test
    public void checkAlphabet01() {
        Permutation test = new Permutation("(BCAD)", new Alphabet("ABCD"));
        assertEquals(4, test.alphabet().size());

        assertEquals('A', test.alphabet().toChar(0));
        assertEquals('D', test.alphabet().toChar(3));
        assertNotEquals('A', test.alphabet().toChar(1));
        assertNotEquals('D', test.alphabet().toChar(2));

        assertTrue(test.alphabet().contains('A'));
        assertTrue(test.alphabet().contains('C'));
        assertFalse(test.alphabet().contains('Z'));
    }

    @Test
    public void checkAlphabet02() {
        Permutation test = new Permutation("(CCCCCCC)",
                new Alphabet("CCCCCCC"));
        assertEquals(7, test.alphabet().size());

        assertEquals('C', test.alphabet().toChar(0));
        assertEquals('C', test.alphabet().toChar(3));
        assertNotEquals('A', test.alphabet().toChar(1));
        assertNotEquals('D', test.alphabet().toChar(2));

        assertTrue(test.alphabet().contains('C'));
    }

    @Test
    public void checkAlphabet03() {
        Permutation test = new Permutation("(DR)", new Alphabet("DDDDDCDR"));
        assertEquals(8, test.alphabet().size());
        assertTrue(test.alphabet().contains('D'));
        assertTrue(test.alphabet().contains('C'));
        assertTrue(test.alphabet().contains('R'));
    }

}
