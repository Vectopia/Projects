package gitlet;
import java.io.Serializable;


/** Base Object class for Git-let, the tiny stupid version-control system.
 *  @author Vector Wang
 */
public class Basement implements Serializable {

    /** This is just a javadoc comment
     * for the stupid style check.
     * @param idp take in something */
    public void setId(String idp) {

        this.ids = idp;
    }

    /** This is just a javadoc comment
     * for the stupid style check.
     * @return return something*/
    public String getId() {

        return ids;
    }

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String ids;

}
