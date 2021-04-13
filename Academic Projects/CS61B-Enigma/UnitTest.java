package enigma;

import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author Vector Wang
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(PermutationTest.class,
                                      AlphabetTest.class,
                                      MovingRotorTest.class));
    }

}


