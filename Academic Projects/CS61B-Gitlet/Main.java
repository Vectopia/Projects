package gitlet;

import java.io.File;
import java.util.Map;

import java.util.Set;
import java.util.Date;

import java.util.List;
import java.util.Queue;

import java.util.Locale;
import java.util.HashMap;

import java.util.Objects;
import java.util.HashSet;

import java.util.ArrayList;
import java.util.LinkedList;

import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Vector Wang
 */

public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    public static void main(String... args) {
        checkEmpty(args);
        String oper = args[0];
        checkDirectory(oper);

        switch (oper) {
        case "init": initMethod(); break;
        case "add": addCommit(args[1]); break;
        case "rm": removeCommit(args[1]); break;
        case "log": logSingle(); break;
        case "global-log": logTotal(); break;
        case "find": findCommit(args[1]); break;
        case "status": stageCur(); break;
        case "merge":  mergeCommit(args[1]); break;
        case "branch": addBranch(args[1]); break;
        case "rm-branch": removeBranch(args[1]); break;
        case "commit": case "commitHash":
            checkMessage(args);
            commit(args[1], System.currentTimeMillis());
            break;
        case "checkout": {
            if (args.length == 3) {
                checkOutFile(getHeaderObj()
                        .getComHash(), args[2]);
            } else if (args.length == 4
                    && "--".equals(args[2])) {
                checkOutFile(args[1], args[3]);
            } else if (args.length == 2) {
                checkOutBranch(args[1]);
            } else {

                errorExit("Incorrect operands.");
            }
            break;
        }
        case "reset": {
            Commit com = getCurCommit(args[1]);
            if (com == null) {
                errorExit("No commit with that id exists.");
            }
            resetStage(com);
            updateVexFile(getHeaderObj().getBranchName(), args[1]);
            break;
        }
        default:
            errorExit("No command with that name exists.");
            break;
        }
    }

    /** This is just a javadoc comment for the stupid style check.*/
    private static void initMethod() {
        File dest = new File(getRoot());
        checkGit(dest);

        boolean check = dest.mkdir();
        if (check) {

            clearCurIndex();

            File obj
                    = new File(getObjPath());
            obj.mkdir();

            File res
                    = new File(getVexPath());
            res.mkdir();

            String ios = "master"; updateHead(ios);

            commit("initial commit", 0);
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param fNa is something*/
    private static void addCommit(String fNa) {

        File file
                = getCurrentFile(fNa);
        checkExist(file);

        BlobObj b
                = new BlobObj(Utils.readContentsAsString(file));

        String fHash
                = intObj(b);
        Index i
                = getCurIndex();
        i.getNameToIndex().remove(fNa);

        TreeObj tree
                = getHeaderObj().getTree();
        String bHash
                = tree.getFileNameToBtree().get(fNa);

        if (!tree.getFileNameToBtree().containsKey(fNa)
                || !fHash.equals(bHash)) {
            i.getNameToIndex().put(fNa, fHash);
        }
        updateIndex(i);

    }

    /** This is just a javadoc comment for the stupid style check.
     * @param fileName is something*/
    private static void removeCommit(String fileName) {

        File fls = getCurrentFile(fileName);
        Index i = getCurIndex();
        TreeObj t = getHeaderObj().getTree();

        checkRM(i, fileName, t);
        i.getNameToIndex().remove(fileName);
        executeRM(i, fileName, t, fls); updateIndex(i);
    }

    /** This is just a javadoc comment for the stupid style check.*/
    private static void logTotal() {
        Map<String, Commit> curT = getCommitsAll();
        curT.forEach((a, b) -> printCurrent(b));
    }

    /** This is just a javadoc comment for the stupid style check.*/
    private static void logSingle() {
        Commit cur = getHeaderObj().getCommit();

        while (cur != null) {
            printCurrent(cur);

            if (cur.getParent1() != null) {

                cur = getCurCommit(cur.getParent1());

            } else {
                break;
            }
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param log is something*/
    private static void findCommit(String log) {

        Map<String, Commit> store = getCommitsAll();
        boolean valid = false;

        for (Map.Entry<String, Commit> x : store.entrySet()) {
            if (x.getValue().getLog().equals(log)) {

                System.out
                        .println(x.getKey());

                valid = true;
            }
        }

        noFind(valid);
    }

    /** This method checks status of the local repo. */
    private static void stageCur() {
        HasH hdT = getHeaderObj();
        List<String> brn = getBranchAll();

        Collections.sort(brn);
        System.out.println("=== Branches ===");
        System.out.println("*" + hdT.getBranchName());
        printBranch(brn, hdT);
        System.out.println();

        Set<String> sT
                = new HashSet<>();
        Set<String> rM
                = new HashSet<>();
        Index i
                = getCurIndex();

        updateStatus(i, sT, rM);

        System.out.println("=== Staged Files ===");
        sT.forEach(System.out::println);
        System.out.println();
        System.out.println("=== Removed Files ===");
        rM.forEach(System.out::println);
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param brn is something*/
    private static void mergeCommit(String brn) {
        mergeCheck(brn); HasH hd = getHeaderObj();

        Commit cur
                = hd.getCommit();
        mergeCheckItself(brn, hd); veriUndone();

        Commit mid =
                middlePt(hd.getBranchName(), brn);
        Commit tar
                = getBrnHead(brn);

        checkCommit(mid, tar, cur, hd);

        List<String> fight = new ArrayList<>();

        TreeObj curT
                = hd.getTree();
        TreeObj midT
                = getTreeObj(mid.getTree());
        TreeObj tarT
                = getTreeObj(tar.getTree());

        Map<String, String> currentFileNameToBlobs =
                curT.getFileNameToBtree();
        Map<String, String> targetFileNameToBlobs =
                tarT.getFileNameToBtree();
        Map<String, String> spFileNameToBlobs =
                midT.getFileNameToBtree();

        divExecution(spFileNameToBlobs, currentFileNameToBlobs,
                targetFileNameToBlobs, fight);

        targetFileNameToBlobs.forEach((x, y) -> {

            if (!spFileNameToBlobs.containsKey(x)) {
                if (!currentFileNameToBlobs.containsKey(x)) {
                    mergeCheckout(x, y);
                } else {
                    mergeGoToConflict(y,
                            currentFileNameToBlobs, x, fight);
                }
            }
        });

        mergeCommit(brn, hd, tar); mergePrintConflict(fight);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param brn is something*/
    private static void addBranch(String brn) {

        File flst = getVexFile(brn); checkBranchExist(flst);

        Utils.writeContents(flst, getHeaderObj().getComHash());
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param brn is something*/
    private static void removeBranch(String brn) {
        HasH hdT = getHeaderObj();
        checkBranchRemove(brn, hdT);


        File rest = getVexFile(brn);
        checkBranchNoExist(rest); rest.delete();
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param log is something
     * @param timelog is something
     * @return is something*/
    private static String commit(String log, long timelog) {

        return commit(log, timelog, null);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param log is something
     * @param timelog is something
     * @param parent is something
     * @return is something*/
    private static String commit(String log,
                                 long timelog, Commit parent) {

        HasH hd = getHeaderObj(); Index i = getCurIndex();
        commitChanges(i, timelog); TreeObj tree = new TreeObj();


        if (hd.getTree() != null) {
            tree.setFileNameBtree(hd.getTree().getFileNameToBtree());
        }
        i.getNameToIndex().forEach((tx, ty) -> {
            tree.getFileNameToBtree().put(tx, ty);
        });
        String tHash = intObj(tree); Commit com = new Commit();

        commitSet(com, parent, log, timelog, tHash, hd);

        String comHa = intObj(com);
        updateVexFile(hd.getBranchName(), comHa); clearCurIndex();
        updateCommitAll(comHa); return comHa;
    }

    /** This method checkout branches.
     * @param brn name of the branch
     *                   that going to be checked out. */
    private static void checkOutBranch(String brn) {
        HasH temp = getHeaderObj();
        checkOutBranchRemove(brn, temp);

        File check = getVexFile(brn);
        checkOutBranchExist(check);

        String fHash = Utils.readContentsAsString(check);
        resetStage(getCurCommit(fHash));

        updateHead(brn);

    }

    /** This method remove the selected branch.
     * @param comHash commitHash.
     * @param flse the name of the file that going to be checked out. */
    private static void checkOutFile(String comHash, String flse) {

        Commit com = getCurCommit(comHash);
        checkCommitExist(com);

        TreeObj tree = getTreeObj(com.getTree());
        checkFileExist(flse, tree);

        String bHash = tree.getFileNameToBtree().get(flse);
        File resF = getCurrentFile(flse);

        if (bHash == null) {

            Utils.restrictedDelete(resF);

        } else {

            String content = getCurBlob(bHash).getBlobContent();
            Utils.writeContents(resF, content);
        }

        Index i = getCurIndex(); i.getNameToIndex().remove(flse);
        updateIndex(i);

    }

    /** This is just a javadoc comment for the stupid style check.
     * @param tar is something*/
    private static void resetStage(Commit tar) {
        resetExecution();

        Map<String, String> rest =
                getTreeObj(tar.getTree()).getFileNameToBtree();
        rest.forEach(Main::resetAdd);

        clearCurIndex();
    }




    /** This method displays all commits ever uploaded.
     * @return all commit index. */
    protected static Map<String, Commit> getCommitsAll() {


        File fles = new File(getRoot()
                + File.separatorChar
                + "allCommits");

        String flsC = "";

        if (fles.isFile()) {
            flsC
                    = Utils.readContentsAsString(fles);

        }

        String[] split
                = flsC.split("\n");

        Map<String, Commit> result =
                new HashMap<String, Commit>();

        for (String s : split) {
            if (s.length() > 0) {

                result.put(s, getCurCommit(s));
            }
        }
        return result;
    }

    /** This method reports all the branch names.
     * @return branch names. */
    protected static List<String> getBranchAll() {

        return Utils.plainFilenamesIn(getVexPath());
    }

    /** This method prints commit in asked forms.
     * @param commit commit. */
    protected static void printCurrent(Commit commit) {
        String form = "EEE MMM d HH:mm:ss yyyy Z";

        SimpleDateFormat dateForm =
                new SimpleDateFormat(form, Locale.US);

        String iop = dateForm
                .format(new Date(commit.getTimeMark()));

        System.out.println("===");
        System.out.println("commit " + commit.getId());

        if (commit.getParent2() != null) {

            System.out.println("Merge:  "
                    + commit.getParent1()
                    .substring(0, 7)
                    + " "
                    + commit.getParent2()
                    .substring(0, 7));
        }

        System.out.println("Date: " +  iop);
        System.out.println(commit.getLog());

        System.out.println();
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param comHash is something*/
    protected static void updateCommitAll(String comHash) {

        String flC = "";

        File file = new File(getRoot()
                + File.separatorChar
                + "allCommits");

        if (file.isFile()) {
            flC
                    = Utils.readContentsAsString(file);
        }

        flC
                = comHash
                + "\n"
                + flC;

        Utils.writeContents(file, flC);
    }

    /** This method get object.
     * @param hash hash.
     * @param <T> in type T.
     * @param expectedClass class.
     * @return object. */
    protected static <T extends Basement>
        T getBaseObj(String hash, Class<T> expectedClass) {

        File fls = getObjFile(hash);
        if (fls.exists()) {

            return Utils.readObject(fls, expectedClass);

        } else if (hash != null) {

            List<String> flsN
                    = Utils.plainFilenamesIn
                    (getObjPath());

            List<String> resN =
                    flsN.stream().
                            filter(x -> x.startsWith(hash)).
                            collect(Collectors.toList());

            if (resN.size() == 1) {

                return getBaseObj
                        (resN.get(0),
                                expectedClass);
            }
        }

        return null;
    }

    /** This method displays the final history of all branches.
     * @param treHash hash.
     * @return tree. */
    protected static TreeObj getTreeObj(String treHash) {

        return getBaseObj(treHash,
                TreeObj.class);
    }

    /** This method displays commit.
     * @param comHashes hash.
     * @return commit. */
    protected static Commit getCurCommit(String comHashes) {

        return getBaseObj(comHashes,
                Commit.class);
    }

    /** This method displays the blob.
     * @param comHash hash.
     * @return blob. */
    protected static BlobObj getCurBlob(String comHash) {

        return getBaseObj(comHash,
                BlobObj.class);
    }

    /** This method displays the working path of one selected file.
     * @param flsName the name of the file that needs to be progressed.
     * @return file retrieved from inputted filename. */
    protected static File getCurrentFile(String flsName) {

        return new File(getCurPath()
                + File.separatorChar
                + flsName);
    }

    /** This method returns the index.
     * @return file index. */
    protected static Index getCurIndex() {

        File idx
                = getIndexFile();

        return Utils.readObject(idx, Index.class);
    }

    /** This method gets the object file.
     * @param comHash hash.
     * @return file name retrieved from certain pathway. */
    protected static File getObjFile(String comHash) {

        return new File(getObjPath()
                + File.separatorChar
                + comHash);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    protected static HasH getHeaderObj() {

        File hFls
                = getHeaderFile(getRoot());
        String brn
                = Utils.readContentsAsString(hFls);

        HasH hd = new HasH();
        hd.setBranchName(brn);

        File tesFls
                = getVexFile(brn);


        if (tesFls.exists()) {
            hd.setComHash(Utils.readContentsAsString(tesFls));

            Commit com
                    = getCurCommit(hd.getComHash());
            TreeObj trex
                    = getTreeObj(com.getTree());

            hd.setCommit(com);
            hd.setTree(trex);
        }
        return hd;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    protected static String getObjPath() {

        return getRoot()
                + File.separatorChar
                + "objects";
    }

    /** This method updates all files in the head branch.
     * @param rootPath targeted root path.
     * @return file names in the head branch.  */
    protected static File getHeaderFile(String rootPath) {

        return new File(rootPath
                + File.separatorChar
                + "HEAD");
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    protected static String getRoot() {

        return getCurPath()
                + File.separatorChar
                + ".gitlet";
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param branchName is something
     * @param comHash is something*/
    protected static void updateVexFile(String branchName,
                                        String comHash) {

        Utils.writeContents(getVexFile(branchName), comHash);
    }

    /** This method gets the referred file from the targeted branch.
     * @param branch targeted branch.
     * @return file retrieved through certain root path. */
    protected static File getVexFile(String branch) {

        return new File(getVexPath()
                + File.separatorChar
                + branch);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    protected static String getVexPath() {

        return getRoot()
                + File.separatorChar
                + "refs";
    }

    /** This is just a javadoc comment for the stupid style check.*/
    protected static void clearCurIndex() {

        updateIndex(new Index());
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param obj is something
     * @return is something*/
    protected static String intObj(Basement obj) {

        String sha256
                = Utils.sha1(Utils.serialize(obj));

        obj.setId(sha256);

        Utils.writeObject(getObjFile(sha256), obj);

        return sha256;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    protected static String getCurPath() {

        return System
                .getProperty("user.dir");
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param arguments is something*/
    protected static void checkEmpty(String... arguments) {

        if (arguments.length == 0) {

            errorExit("Please enter a command.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param order is something*/
    protected static void checkDirectory(String order) {

        if (!"init".equals(order)) {

            File fls
                    = new File(getRoot());

            if (!fls.isDirectory()) {

                errorExit("Not in an "
                        + "initialized Gitlet directory.");
            }
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param hash is something
     * @param toBeDeleted is something*/
    protected static void checkOutAndDelete(String name,
                                            String hash,
                                            File toBeDeleted) {

        if (hash != null) {

            checkoutFileStage(name, hash);

        } else {

            Utils.restrictedDelete(toBeDeleted);
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param target is something
     * @param current is something
     * @param storage is something*/
    protected static void conflictExecution(String name,
                                            String target,
                                            String current,
                                            List<String> storage) {

        mergeConflictFile(name,
                target, current);

        storage.add(name);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param target is something*/
    protected static void checkOutExecution(String name,
                                            String target) {

        if (target != null) {

            checkoutFileStage(name, target);
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param arguments is the list of conflict merge.*/
    protected static void checkMessage(String... arguments) {

        if (arguments.length < 2) {

            errorExit("Please enter a commit message.");
        }

        if (arguments[1].isEmpty()) {

            errorExit("Please enter a commit message.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param current is current file name.
     * @param target is target file name.
     * @param reference is the list of conflict merge.
     * @return is something*/
    protected static boolean allEqual(String current,
                                      String target,
                                      String reference) {

        return !Objects
                .equals(current, reference)
                && !Objects
                .equals(target, reference)
                && !Objects
                .equals(current, target);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param mid is sp file name.
     * @param cur is current file name.
     * @param tar is target file name.
     * @param conflict is the list of conflict merge.*/
    protected static void divExecution(Map<String, String> mid,
                                       Map<String, String> cur,
                                       Map<String, String> tar,
                                       List<String> conflict) {

        mid.forEach((fileName, blobHash) -> {
            if (cur.containsKey(fileName)
                    && tar.containsKey(fileName)) {

                File fls = getCurrentFile(fileName);
                String tarHash = tar.get(fileName);
                String curHash = cur.get(fileName);

                if (Objects.equals(curHash, blobHash)
                        && !Objects.equals(tarHash, blobHash)) {

                    checkOutAndDelete(fileName, tarHash, fls);

                } else if (allEqual(curHash,
                        tarHash, blobHash)) {

                    conflictExecution(fileName, tarHash,
                            curHash, conflict);

                }
            } else if (!cur.containsKey(fileName)
                    && tar.containsKey(fileName)) {

                checkOutExecution(fileName, tar.get(fileName));
            }
        });
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something*/
    protected static void mergeCheck(String name) {

        if (!getCurIndex()
                .getNameToIndex().isEmpty()) {

            errorExit("You have uncommitted changes.");
        }

        if (!getVexFile(name)
                .exists()) {

            errorExit("A branch with that name does not exist.");
        }

    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param heads is something*/
    protected static void mergeCheckItself(String name, HasH heads) {

        if (heads.getBranchName()
                .equals(name)) {

            errorExit("Cannot merge a branch with itself.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param commits1 is something
     * @param commits2 is something
     * @param commits3 is something
     * @param heads is something*/
    protected static void checkCommit(Commit commits1, Commit commits2,
                                      Commit commits3, HasH heads) {

        if (commits2.getId()
                .equals(commits1.getId())) {

            errorExit("Given branch is "
                    + "an ancestor of the current branch.");

        } else if (commits3.getId()
                .equals(commits1.getId())) {

            updateVexFile(heads.getBranchName(), commits2.getId());
            resetStage(commits2);
            errorExit("Current branch fast-forwarded.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param heads is something
     * @param commits is something*/
    protected static void mergeCommit(String name,
                                      HasH heads,
                                      Commit commits) {

        commit("Merged "
                + name
                + " into "
                + heads.getBranchName()
                + ".", System.currentTimeMillis(), commits);
    }

    /** This method update files into the head branch.
     * @param brnName name of the head branch. */
    protected static void updateHead(String brnName) {

        Utils.writeContents(getHeaderFile(getRoot()), brnName);
    }

    /** This method updates files by their index.
     * @param i index of the files progressed. */
    protected static void updateIndex(Index i) {

        Utils.writeObject(getIndexFile(), i);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    protected static File getIndexFile() {

        return new File(getRoot()
                + File.separatorChar
                + "index");
    }


    /** This is just a javadoc comment for the stupid style check.
     * @param conflicts is something*/
    protected static void mergePrintConflict(List<String> conflicts) {

        if (conflicts.size() > 0) {

            errorExit("Encountered a merge conflict.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param target is something*/
    protected static void mergeCheckout(String name, String target) {

        if (target != null) {

            checkoutFileStage(name, target);
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param current is something
     * @param target is something
     * @param conflicts is something*/
    protected static void mergeGoToConflict(String target,
                                            Map<String, String> current,
                                            String name,
                                            List<String> conflicts) {
        if (!Objects.equals(target,
                current.get(name))) {

            mergeConflictFile(name, target,
                    current.get(name));
            conflicts.add(name);
        }
    }

    /** check out file and stage.
     * @param fileName the name of uploaded files.
     * @param target target BlobHash. */
    protected static void checkoutFileStage(String fileName,
                                            String target) {

        Utils.writeContents(getCurrentFile(fileName),
                getCurBlob(target).getBlobContent());
        addCommit(fileName);
    }

    /** This method checks whether there is conflicted files.
     * @param flN the name of uploaded files.
     * @param tarHa target BlobHash.
     * @param curHa current BlobHash. */
    protected static void mergeConflictFile(String flN,
                                            String tarHa,
                                            String curHa) {

        File fls = getCurrentFile(flN);
        String flc = "<<<<<<< HEAD\n";

        if (curHa != null) {

            flc += getCurBlob(curHa)
                    .getBlobContent();
        }
        flc += "=======\n";

        if (tarHa != null) {

            flc += getCurBlob(tarHa)
                    .getBlobContent();
        }
        flc += ">>>>>>>\n";

        Utils.writeContents(fls, flc);
        addCommit(flN);
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param branchName is something
     * @return is something*/
    protected static Commit getBrnHead(String branchName) {

        return getCurCommit(Utils
                .readContentsAsString(getVexFile(branchName)));
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param sets is something
     * @param queue is something*/
    protected static void addParentsToSet(Set<String> sets,
                                          Queue<Commit> queue) {
        while (!queue.isEmpty()) {
            Commit collect
                    = queue.poll();
            String par1
                    = collect.getParent1();
            String par2
                    = collect.getParent2();

            if (par1 != null
                    && !sets.contains(par1)) {

                queue.add(getCurCommit(par1));
                sets.add(par1);
            }

            if (par2 != null
                    && !sets.contains(par2)) {

                queue.add(getCurCommit(par2));
                sets.add(par2);
            }

            sets.add(collect.getId());
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param brn1 is something
     * @param brn2 is something
     * @return is something*/
    protected static Commit middlePt(String brn1,
                                     String brn2) {

        File curF = getVexFile(brn2);
        String cHash = Utils.readContentsAsString(curF);
        Commit com = getCurCommit(cHash);

        Queue<Commit> qu = new LinkedList<>();
        Set<String> cole = new HashSet<>();

        qu.add(com);
        cole.add(com.getId());
        addParentsToSet(cole, qu);

        curF = getVexFile(brn1);
        cHash = Utils.readContentsAsString(curF);
        com = getCurCommit(cHash);


        if (cole.contains(cHash)) {

            return com;
        }

        qu = new LinkedList<>();
        qu.add(com);

        while (!qu.isEmpty()) {

            Commit colec
                    = qu.poll();
            String parent1
                    = colec.getParent1();
            String parent2
                    = colec.getParent2();

            if (cole.contains(parent1)) {

                return getCurCommit(parent1);
            }
            if (parent1 != null) {

                qu.add(getCurCommit(parent1));
            }

            if (cole.contains(parent2)) {

                return getCurCommit(parent2);
            }

            if (parent2 != null) {

                qu.add(getCurCommit(parent2));
            }
        }

        return null;
    }

    /** This is just a javadoc comment for the stupid style check.*/
    protected static void resetExecution() {

        List<String> fls = veriUndone();
        fls.forEach(fileName -> {

            Utils.restrictedDelete(getCurrentFile(fileName));
        });
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param hash is something*/
    protected static void resetAdd(String name, String hash) {

        if (hash != null) {

            BlobObj bl = getCurBlob(hash);

            Utils.writeContents(getCurrentFile(name),
                    bl.getBlobContent());
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @return return something*/
    protected static List<String> veriUndone() {

        HasH h = getHeaderObj();
        Index i = getCurIndex();

        List<String> fln =
                Utils.plainFilenamesIn(getCurPath());

        fln.forEach(fileName -> {

            if (!h.getTree().getFileNameToBtree()
                    .containsKey(fileName)
                    && !i.getNameToIndex()
                    .containsKey(fileName)) {

                errorExit("There is an "
                        + "untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        });

        return fln;
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param location is something*/
    protected static void checkGit(File location) {

        if (location.exists()) {

            errorExit("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
    }


    /** This is just a javadoc comment for the stupid style check.
     * @param files is something*/
    protected static void checkExist(File files) {

        if (!files.exists()) {

            errorExit("File does not exist.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param indx is something
     * @param name is something
     * @param trees is something*/
    protected static void checkRM(Index indx,
                                  String name,
                                  TreeObj trees) {

        if (!indx.getNameToIndex()
                .containsKey(name)
                && !trees.getFileNameToBtree()
                .containsKey(name)) {

            errorExit("No reason to remove the file.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param indx is something
     * @param name is something
     * @param files is something
     * @param trees is something*/
    protected static void executeRM(Index indx,
                                    String name,
                                    TreeObj trees,
                                    File files) {

        if (trees.getFileNameToBtree()
                .containsKey(name)) {

            Utils.restrictedDelete(files);

            indx.getNameToIndex()
                    .put(name, null);
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param result is something*/
    protected static void noFind(boolean result) {

        if (!result) {

            errorExit("Found no commit with that message.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param branchName is something
     * @param heads is something*/
    protected static void printBranch(List<String> branchName,
                                      HasH heads) {

        branchName.forEach(x -> {

            if (!x.equals(heads
                    .getBranchName())) {

                System.out.println(x);
            }
        });
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param idx is something
     * @param stage is something
     * @param remove is something*/
    protected static void updateStatus(Index idx,
                                       Set<String> stage,
                                       Set<String> remove) {
        idx.getNameToIndex().forEach((k, v) -> {

            if (v == null) {
                remove.add(k);

            } else {

                stage.add(k);
            }
        });
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param files is something*/
    protected static void checkBranchExist(File files) {

        if (files.exists()) {

            errorExit("A branch with that name already exists.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param files is something*/
    protected static void checkBranchNoExist(File files) {

        if (!files.exists()) {

            errorExit("A branch with that name does not exist.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param heads is something*/
    protected static void checkBranchRemove(String name, HasH heads) {

        if (name.equals(heads.getBranchName())) {

            errorExit("Cannot remove the current branch.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param heads is something*/
    protected static void checkOutBranchRemove(String name, HasH heads) {

        if (heads.getBranchName()
                .equals(name)) {

            errorExit("No need to checkout the current branch.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param files is something*/
    protected static void checkOutBranchExist(File files) {

        if (!files.exists()) {

            errorExit("No such branch exists.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param commits is something*/
    protected static void checkCommitExist(Commit commits) {

        if (commits == null) {

            errorExit("No commit with that id exists.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param name is something
     * @param trees is something*/
    protected static void checkFileExist(String name, TreeObj trees) {

        if (!trees.getFileNameToBtree()
                .containsKey(name)) {

            errorExit("File does not exist in that commit.");
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     * @param time is something
     * @param idx is something*/
    protected static void commitChanges(Index idx, long time) {

        if (time != 0) {
            if (idx.getNameToIndex().isEmpty()) {

                errorExit("No changes added to the commit.");
            }
        }
    }

    /** This is just a javadoc comment for the stupid style check.
     *  @param parent is something
     *  @param heads is something
     *  @param commits is something
     *  @param logs is something
     *  @param time is something
     *  @param tree is something*/
    protected static void commitSet(Commit commits, Commit parent,
                                    String logs, long time,
                                    String tree, HasH heads) {

        commits.setLog(logs); commits.setTimeMark(time);
        commits.setTree(tree); commits.setParent1(heads.getComHash());

        if (parent != null) {
            commits.setParent2(parent.getId());
        }
    }

    /** This method prints out error messages.
     * @param messg error messages. */
    protected static void errorExit(String messg) {
        Utils.message(messg);
        System.exit(0);
    }

}

