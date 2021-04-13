package gitlet;


/** Blob Object class for Git-let, the tiny stupid version-control system.
 *  @author Vector Wang
 */
public class BlobObj extends Basement {

    /** This is just a javadoc comment
     * for the stupid style check.
     * @param contents take in something */
    public BlobObj(String contents) {

        this.string = contents;
    }

    /** This is just a javadoc comment
     * for the stupid style check.
     * @return return something*/
    public String getBlobContent() {

        return string;
    }

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String string;
}
