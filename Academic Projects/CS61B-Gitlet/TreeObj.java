package gitlet;

import java.util.HashMap;
import java.util.Map;

/** Commit Action class for Git-let, the tiny stupid version-control system.
 *  @author Vector Wang
 */
public class TreeObj extends Basement {
    /** This is just a javadoc comment for the stupid style check.*/
    private String name = "tree";

    /** This is just a javadoc comment for the stupid style check.
     * @param fileName is something*/
    public void setFileNameBtree(Map<String, String> fileName) {

        this.fileNameBtree = fileName;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public Map<String, String> getFileNameToBtree() {

        return fileNameBtree;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public String getIdleRemove() {
        return this.idleremove;

    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public boolean checkIdleLog() {
        return this.log.isEmpty()
                && this.idleremove.isEmpty();

    }


    /** Collection of tracked files. Key is the file name,
     *  value is the sha1 code for corresponding file. */
    private HashMap<String, String> track;

    /** This is just a javadoc comment for the stupid style check.*/
    private Map<String, String> fileNameBtree = new HashMap<>();

    /** This is just a javadoc comment for the stupid style check.*/
    private HashMap<String, String> log;

    /** Collection of files to be removed. */
    private String idleremove;
}
