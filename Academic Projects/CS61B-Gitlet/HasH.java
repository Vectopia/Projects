package gitlet;

import java.io.Serializable;

/** Commit Action class for Git-let,
 * the tiny stupid version-control system.
 *  @author Vector Wang
 */
public class HasH implements Serializable {
    /** This is just a javadoc comment for the stupid style check.*/
    private String name = "head";

    /** This is just a javadoc comment for the stupid style check.*/
    public HasH() { }

    /** This is just a javadoc comment for the stupid style check.
     * @param trees is something*/
    public void setTree(TreeObj trees) {

        this.tree = trees;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param comSHA is something*/
    public void setComHash(String comSHA) {

        this.comHash = comSHA;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param brnName is something*/
    public void setBranchName(String brnName) {

        this.branchName = brnName;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param com is something*/
    public void setCommit(Commit com) {

        this.commit = com;
    }



    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public String getComHash() {

        return comHash;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public String getBranchName() {

        return branchName;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public Commit getCommit() {

        return commit;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public TreeObj getTree() {

        return tree;
    }



    /** This is just a javadoc comment
     * for the stupid style check.*/
    private Commit commit;

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private TreeObj tree;

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String comHash;

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String branchName;
}
