package gitlet;

import ucb.junit.textui;
import org.junit.Test;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Vector Wang
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {

        System.exit(textui.runClasses(UnitTest.class));
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

}


