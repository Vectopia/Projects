package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Vector Wang
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void checkSize() {
        Permutation test01 = new Permutation("", new Alphabet(UPPER_STRING));
        assertEquals(26, test01.size());

        Permutation test02 = new Permutation("", new Alphabet("A"));
        assertEquals(1, test02.size());
    }

    @Test
    public void checkAlphabet() {
        Permutation test = new Permutation("(BCAD)", new Alphabet("ABCD"));
        assertEquals(4, test.alphabet().size());
        assertTrue(test.alphabet().contains('A'));
        assertTrue(test.alphabet().contains('C'));
        assertFalse(test.alphabet().contains('Z'));
    }

    @Test
    public void checkPermute() {
        Permutation test0 = new Permutation("(ABCD) (EF)",
                new Alphabet(UPPER_STRING));
        assertEquals(1, test0.permute(0));
        assertEquals(4, test0.permute(5));
        assertEquals(5, test0.permute(4));
        assertEquals(0, test0.permute(3));
        assertEquals(25, test0.permute(25));

        Permutation test1 = new Permutation("",
                new Alphabet("A"));
        assertEquals(0, test1.permute(0));

        Permutation test2 = new Permutation("(T)",
                new Alphabet("VECTOR"));
        assertEquals(5, test2.permute(11));
        assertEquals(1, test2.permute(13));

        Permutation test3 = new Permutation("(C) (TOR)",
                new Alphabet("VECTOR"));
        assertEquals(0, test3.permute(0));
        assertEquals(3, test3.permute(11));
        assertEquals(1, test3.permute(7));

        Permutation test4 = new Permutation("(BACD)",
                new Alphabet("ABCD"));
        assertEquals(1, test4.permute(7));
        assertEquals(3, test4.permute(2));
    }

    @Test
    public void checkCPermute() {
        Permutation test0 = new Permutation("(ABCD) (EF)",
                new Alphabet());
        assertEquals('B', test0.permute('A'));
        assertEquals('A', test0.permute('D'));
        assertEquals('E', test0.permute('F'));
        assertEquals('F', test0.permute('E'));
        assertEquals('Z', test0.permute('Z'));

        Permutation test1 = new Permutation("",
                new Alphabet("A"));
        assertEquals('A', test1.permute('A'));

        Permutation test2 = new Permutation("(C) (TOR)",
                new Alphabet("VECTOR"));
        assertEquals('C', test2.permute('C'));
        assertEquals('V', test2.permute('V'));
        assertEquals('O', test2.permute('T'));
    }

    @Test
    public void checkInvert() {
        Permutation test0 = new Permutation("(ABCD) (EF)",
                new Alphabet());
        assertEquals(0, test0.invert(1));
        assertEquals(3, test0.invert(0));
        assertEquals(2, test0.invert(3));
        assertEquals(4, test0.invert(5));
        assertEquals(5, test0.invert(4));

        Permutation test1 = new Permutation("",
                new Alphabet("A"));
        assertEquals(0, test1.invert(0));

        Permutation test2 = new Permutation("(T)",
                new Alphabet("VECTOR"));
        assertEquals(1, test2.invert(13));
        assertEquals(5, test2.invert(11));

        Permutation test3 = new Permutation("(C) (TOR)",
                new Alphabet("VECTOR"));
        assertEquals(0, test3.invert(0));
        assertEquals(4, test3.invert(11));
        assertEquals(1, test3.invert(7));
    }

    @Test
    public void checkCInvert() {
        Permutation test0 = new Permutation("(ABCD) (EF)",
                new Alphabet());
        assertEquals('A', test0.invert('B'));
        assertEquals('D', test0.invert('A'));
        assertEquals('E', test0.invert('F'));
        assertEquals('F', test0.invert('E'));

        Permutation test1 = new Permutation("",
                new Alphabet("A"));
        assertEquals('A', test1.invert('A'));

        Permutation test2 = new Permutation("(BACD)",
                new Alphabet("ABCD"));
        assertEquals('B', test2.invert('A'));
        assertEquals('D', test2.invert('B'));

        Permutation test3 = new Permutation("(E) (TOR)",
                new Alphabet("VECTOR"));
        assertEquals('E', test3.invert('E'));
        assertEquals('O', test3.invert('R'));
        assertEquals('V', test3.invert('V'));
    }

    @Test
    public void checkDerangement() {
        Permutation test0 = new Permutation("(BCAD)",
                new Alphabet("ABCD"));
        assertTrue(test0.derangement());

        Permutation test1 = new Permutation("(B)",
                new Alphabet("ABCD"));
        assertFalse(test1.derangement());
    }
}
