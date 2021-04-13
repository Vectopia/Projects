package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Commit Action class for Git-let, the tiny stupid version-control system.
 *  @author Vector Wang
 */
public class Index implements Serializable {


    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public Map<String, String> getNameToIndex() {

        return nameToIndex;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param fileName is something*/
    public void setNameToIndex(Map<String, String> fileName) {

        this.nameToIndex = fileName;
    }

    /** This is just a javadoc comment for the stupid style check.*/
    private String name = "index";

    /** This is just a javadoc comment for the stupid style check.*/
    private Map<String, String> nameToIndex = new HashMap<>();
}
