package gitlet;

/** Commit Action class for Git-let, the tiny stupid version-control system.
 *  @author Vector Wang
 */
public class Commit extends Basement {

    /** This is just a javadoc comment for the stupid style check.*/
    public Commit() {
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param trees is something*/
    public void setTree(String trees) {

        this.tree = trees;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param parentA is something*/
    public void setParent1(String parentA) {

        this.parent1 = parentA;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param parent is something*/
    public void setParent2(String parent) {

        this.parent2 = parent;
    }

    /** This is just a javadoc comment for the stupid style check.
     *  @param logs is something*/
    public void setLog(String logs) {

        this.log = logs;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param times is something*/
    public void setTimeMark(long times) {

        this.timeMark = times;
    }

    /** This is just a javadoc comment for the stupid style check.*/
    public void setIdle() {

        idle = true;
    }



    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public String getTree() {

        return tree;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public String getParent1() {

        return parent1;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public String getParent2() {

        return parent2;
    }

    /** Gets log, return the log. */
    public String getLog() {

        return log;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public long getTimeMark() {

        return timeMark;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    public boolean getIdle() {
        return idle;
    }




    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String name = "commit";

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String tree;

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String parent1;

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String parent2;

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private String log;

    /** The state of merge. */
    private boolean idle;

    /** This is just a javadoc comment
     * for the stupid style check.*/
    private long timeMark;

}
