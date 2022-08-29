package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayDeque;
import java.util.HashSet;

/** Object representing current repository.
 *  @author Paree Hebbar
 */
public class Repo {

    /**
     * Initializes repository, making necessary directories and initial commit.
     */
    public void init() throws IOException {

        if (_GITLET.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        }

        _GITLET.mkdir();
        _STAGEADD.mkdir();
        _STAGERM.mkdir();
        _BLOB.mkdir();
        _COMMITS.mkdir();
        _HEAD.createNewFile();
        _BRANCHES.createNewFile();
        _ACTIVE.createNewFile();


        Date d = new Date();
        d.setTime(0);


        Commits initial = new Commits("initial commit", null,
                null, d, new HashMap<String, Blob>());
        persist(_COMMITS, initial, initial.getSha());

        _branches = new TreeMap<>();
        _branches.put("master", initial);

        Utils.writeObject(_HEAD, initial);
        Utils.writeObject(_BRANCHES, _branches);

        Utils.writeContents(_ACTIVE, "master");

    }

    /**
     * Clears the stage directory of all files.
     */
    public void clearStage() {
        for (File f : _STAGEADD.listFiles()) {
            f.delete();
        }
        for (File f : _STAGERM.listFiles()) {
            f.delete();
        }
    }

    /**
     * Saves file NAME storing metadata OBJ in directory DIR.
     */
    public void persist(File dir, Object obj, String name) throws IOException {
        File save = Utils.join(dir, name);
        save.createNewFile();
        Utils.writeObject(save, (Serializable) obj);
    }

    /**
     * Saves file NAME to directory DIR.
     */
    public void persist(File dir, String name) throws IOException {
        File save = Utils.join(dir, name);
        save.createNewFile();
    }

    /**
     * Adds given file FILE to stage directory.
     */
    public void add(String file) throws IOException {
        File f = Utils.join(_CWD, file);

        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        Blob b = new Blob(file, _CWD, _BLOB);
        Commits curr = Utils.readObject(_HEAD, Commits.class);

        if (Utils.join(_STAGERM, file).exists()) {
            Utils.join(_STAGERM, file).delete();
        }
        if (b.isEqual(curr.getFiles().get(file))) {
            if (Utils.join(_STAGEADD, file).exists()) {
                Utils.join(_STAGEADD, file).delete();
            }
        } else {
            persist(_STAGEADD, b, file);
        }
    }

    /**
     * Creates new commit object with given message MESSAGE.
     */
    @SuppressWarnings("unchecked")
    public void commit(String message) throws IOException {
        Commits parent = Utils.readObject(_HEAD, Commits.class);
        HashMap<String, Blob> parentfiles = new HashMap<>();

        for (Map.Entry entry: parent.getFiles().entrySet()) {
            parentfiles.put((String) entry.getKey(), (Blob) entry.getValue());
        }

        if (_STAGEADD.listFiles().length == 0
                && _STAGERM.listFiles().length == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }

        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        for (File f : _STAGEADD.listFiles()) {
            Blob b = Utils.readObject(f, Blob.class);
            parentfiles.put(f.getName(), b);
        }

        for (File f : _STAGERM.listFiles()) {
            parentfiles.remove(f.getName());
        }

        Commits c = new Commits(message, parent, null, new Date(), parentfiles);
        persist(_COMMITS, c, c.getSha());

        _branches = Utils.readObject(_BRANCHES, TreeMap.class);
        _branches.put(Utils.readContentsAsString(_ACTIVE), c);

        clearStage();

        Utils.writeObject(_HEAD, c);
        Utils.writeObject(_BRANCHES, _branches);
    }

    /**
     * Removes file FILE from cwd and stages it for removal.
     */
    public void remove(String file) throws IOException {
        File fcwd = Utils.join(_CWD, file);
        File fstg = Utils.join(_STAGEADD, file);

        Commits curr = Utils.readObject(_HEAD, Commits.class);

        if (!fstg.exists() && !curr.getFiles().containsKey(file)) {
            System.out.println("No reason to remove the file.");
        }
        if (fstg.exists()) {
            fstg.delete();
        }
        if (curr.getFiles().containsKey(file)) {
            persist(_STAGERM, curr.getFiles().get(file), file);
            fcwd.delete();
        }


    }

    /**
     * Prints the metadata of all commits from the
     * current head commit to the initial commit.
     */
    public void log() {
        Commits curr = Utils.readObject(_HEAD, Commits.class);

        while (curr != null) {
            System.out.println("===");
            System.out.println("commit " + curr.getSha());
            System.out.println("Date: "
                    + simpleDateFormat.format(curr.getTimestamp()));
            System.out.println(curr.getMessage());
            System.out.println();

            curr = curr.getParent1();
        }
    }

    /**
     * Prints the metadata of all commits.
     */
    public void globalLog() {
        List<String> files = Utils.plainFilenamesIn(_COMMITS);

        for (String s : files) {
            if (s.startsWith(".")) {
                continue;
            }
            File c = Utils.join(_COMMITS, s);
            Commits curr = Utils.readObject(c, Commits.class);

            System.out.println("===");
            System.out.println("commit " + curr.getSha());
            System.out.println("Date: "
                    + simpleDateFormat.format(curr.getTimestamp()));
            System.out.println(curr.getMessage());
            System.out.println();
        }
    }

    /**
     * Finds the commmit in repository with message MESSAGE.
     */
    public void find(String message) {
        List<String> files = Utils.plainFilenamesIn(_COMMITS);
        boolean exists = false;

        for (String s : files) {
            if (s.startsWith(".")) {
                continue;
            }
            File c = Utils.join(_COMMITS, s);
            Commits curr = Utils.readObject(c, Commits.class);

            if (curr.getMessage().equals(message)) {
                System.out.println(curr.getSha());
                exists = true;
            }
        }

        if (!exists) {
            System.out.println("Found no commit with that message");
        }
    }


    /**
     * Moves head commit to commit with ID C
     * and checkouts out all the files from that commit.
     */
    @SuppressWarnings("unchecked")
    public void reset(String c) throws IOException {
        String commit = shortened(c);

        if (Utils.join(_COMMITS, commit).exists()) {
            _branches = Utils.readObject(_BRANCHES, TreeMap.class);
            Commits curr = Utils.readObject(
                    Utils.join(_COMMITS, commit), Commits.class);
            Commits head = Utils.readObject(_HEAD, Commits.class);
            Utils.writeObject(_HEAD, curr);
            _branches.put(Utils.readContentsAsString(_ACTIVE), curr);
            Utils.writeObject(_BRANCHES, _branches);

            for (Map.Entry entry : curr.getFiles().entrySet()) {
                String file = (String) entry.getKey();

                if (Utils.join(_CWD, file).exists()
                        && !head.getFiles().containsKey(file)) {
                    System.out.println("There is an "
                            + "untracked file in the way; delete it"
                            + ", or add and commit it first.");
                    return;
                } else {
                    checkout(file);
                }
            }

            for (Map.Entry entry : head.getFiles().entrySet()) {
                String file = (String) entry.getKey();
                if (!curr.getFiles().containsKey(file)) {
                    Utils.join(_CWD, file).delete();
                }
            }

            clearStage();
        } else {
            System.out.println("No commit with that id exists.");
        }

    }

    /**
     * Prints out information regarding all the branches,
     * tracked files, and removed files.
     */
    @SuppressWarnings("unchecked")
    public void status() {
        System.out.println("=== Branches ===");
        System.out.println("*" + Utils.readContentsAsString(_ACTIVE));
        Iterator<String> keys =
                Utils.readObject(_BRANCHES, TreeMap.class).keySet().iterator();

        while (keys.hasNext()) {
            String b = keys.next();
            if (!Utils.readContentsAsString(_ACTIVE).equals(b)) {
                System.out.println(b);
            }
        }

        while (keys.hasNext()) {
            System.out.println(keys.next());
        }

        System.out.println();
        System.out.println("=== Staged Files ===");

        for (String s : Utils.plainFilenamesIn(_STAGEADD)) {
            System.out.println(s);
        }

        System.out.println();
        System.out.println("=== Removed Files ===");

        for (String s : Utils.plainFilenamesIn(_STAGERM)) {
            System.out.println(s);
        }

        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    /**
     * Restores version of file FILE from head commit.
     */
    public void checkout(String file) throws IOException {
        Commits curr = Utils.readObject(_HEAD, Commits.class);

        if (!curr.getFiles().containsKey(file)) {
            System.out.println("File does not exist in that commit");
            return;
        } else {
            Blob b = curr.getFiles().get(file);
            File f = Utils.join(_CWD, file);

            if (!f.exists()) {
                f.createNewFile();
            }

            Utils.writeContents(f, b.getContents());
        }
    }

    /**
     * Restores version of file FILE from commit COMMIT.
     */
    public void checkout(String commit, String file) throws IOException {
        File c = Utils.join(_COMMITS, shortened(commit));

        if (!c.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commits head = Utils.readObject(_HEAD, Commits.class);
        Commits curr = Utils.readObject(c, Commits.class);
        Utils.writeObject(_HEAD, curr);
        checkout(file);
        Utils.writeObject(_HEAD, head);
    }

    /**
     * Returns the full commit ID associated with abbreviated ID COMMIT.
     */
    public String shortened(String commit) {
        if (commit.length() < _size) {
            for (String s : Utils.plainFilenamesIn(_COMMITS)) {
                if (s.startsWith(commit)) {
                    return s;
                }
            }
        }
        return commit;
    }

    /**
     * Chnages head and active pointer to head commit in branch BRANCH
     * and checksout all files in that commit.
     */
    @SuppressWarnings("unchecked")
    public void checkoutBranch(String branch) throws IOException {
        _branches = Utils.readObject(_BRANCHES, TreeMap.class);

        if (!_branches.containsKey(branch)) {
            System.out.println("No such branch exists");
        } else if (Utils.readContentsAsString(_ACTIVE).equals(branch)) {
            System.out.println("No need to checkout the current branch");
        } else {
            Commits curr = _branches.get(Utils.readContentsAsString(_ACTIVE));
            Commits head = _branches.get(branch);

            HashMap<String, Blob> currFiles = curr.getFiles();
            HashMap<String, Blob> headFiles = head.getFiles();


            for (Map.Entry entry : headFiles.entrySet()) {
                if (!currFiles.containsKey(entry.getKey())
                        && Utils.join(_CWD, (String) entry.getKey()).exists()) {
                    System.out.println("There is an untracked file in the way"
                            + "; delete it, or add and commit it first.");
                    return;
                } else {
                    checkout(head.getSha(), (String) entry.getKey());
                }
            }
            for (Map.Entry entry : currFiles.entrySet()) {
                if (!headFiles.containsKey(entry.getKey())) {
                    Utils.join(_CWD, (String) entry.getKey()).delete();
                }
            }

            clearStage();
            Utils.writeObject(_HEAD, head);
            Utils.writeContents(_ACTIVE, branch);
        }

    }

    /**
     * Creates new branch NAME in repo.
     */
    @SuppressWarnings("unchecked")
    public void makebranch(String name) {
        _branches = Utils.readObject(_BRANCHES, TreeMap.class);

        if (_branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
        } else {
            _branches.put(name, Utils.readObject(_HEAD, Commits.class));
        }

        Utils.writeObject(_BRANCHES, _branches);
    }

    /**
     * Removes branch NAME from repo.
     */
    @SuppressWarnings("unchecked")
    public void removeBranch(String name) {
        _branches = Utils.readObject(_BRANCHES, TreeMap.class);

        if (!_branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
        } else if (Utils.readContentsAsString(_ACTIVE).equals(name)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            _branches.remove(name);
            Utils.writeObject(_BRANCHES, _branches);
        }
    }

    /** Merges active branch with BRANCH. */
    @SuppressWarnings("unchecked")
    public void merge(String branch) throws IOException {
        _branches = Utils.readObject(_BRANCHES, TreeMap.class);
        Commits head = Utils.readObject(_HEAD, Commits.class);
        if (Utils.plainFilenamesIn(_STAGEADD).size() != 0
                || Utils.plainFilenamesIn(_STAGERM).size() != 0) {
            System.out.println("You have uncommitted changes.");
        } else if (Utils.readContentsAsString(_ACTIVE).equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
        } else if (!_branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
        } else if (getSplit(branch).isEqual(_branches.get(branch))) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
        } else if (getSplit(branch).isEqual(head)) {
            checkoutBranch(branch);
            System.out.println("Current branch fast-forwarded.");
        } else {
            String conflict = "";

            Commits split = getSplit(branch);
            Commits curr = Utils.readObject(_HEAD, Commits.class);
            Commits merger = _branches.get(branch);

            HashSet<String> files = mergeFiles(curr, merger, split);

            conflict = mergeHelper(curr, merger, split, files, branch);

            if (conflict.equals("untracked")) {
                return;
            } else if (conflict.equals("conflict")) {
                mergeCommit(branch, curr, merger, true);
            } else {
                mergeCommit(branch, curr, merger, false);
            }
        }
    }



    /** Helper for merging takes in C, M, S, FILES, BRANCH.
     * @return string to notify if conflicts occured.
     * */
    public String mergeHelper(Commits c, Commits m,
                              Commits s, HashSet<String> files,
                              String branch) throws IOException {

        HashMap<String, Blob> cfiles = c.getFiles();
        HashMap<String, Blob> mfiles = m.getFiles();
        HashMap<String, Blob> sfiles = s.getFiles();

        String conflict = "";


        for (String file: files) {

            Blob cfile = cfiles.get(file);
            Blob mfile = mfiles.get(file);
            Blob sfile = sfiles.get(file);

            File f = Utils.join(_CWD, file);

            if (untracked(file)) {
                return "untracked";
            }

            if (sfile == null) {
                if (cfile == null && mfile != null) {
                    Utils.writeContents(f, mfile.getContents());
                    persist(_STAGEADD, mfile, file);
                    f.createNewFile();
                } else if (cfile != null && mfile != null
                        && !cfile.isEqual(mfile)) {
                    handleConflict(mfile, cfile, f);
                    conflict = "conflict";
                }
            } else if (sfile.isEqual(cfile) && !sfile.isEqual(mfile)) {
                if (mfile == null) {
                    f.delete();
                    persist(_STAGERM, file);
                } else {
                    Utils.writeContents(f, mfile.getContents());
                    persist(_STAGEADD, file);
                }
            } else if (!sfile.isEqual(cfile) && !sfile.isEqual(mfile)) {
                handleConflict(mfile, cfile, f);
                conflict = "conflict";
            }
        }
        return conflict;
    }
    /** Handles case of conflicts. Takes in MFILE, CFILE, FILE
     * */
    public void handleConflict(Blob mfile,
                               Blob cfile, File file) throws IOException {
        String contents = "";
        String contents2 = "";
        if (mfile != null) {
            contents2 = mfile.getContents();
        }
        if (cfile != null) {
            contents = cfile.getContents();
        }


        String s = "<<<<<<< HEAD\n" + contents
                + "=======\n" + contents2 + ">>>>>>>\n";
        Utils.writeContents(file, s);
        Blob b = new Blob(file.getName(), _CWD, _BLOB);
        persist(_STAGEADD, b, file.getName());
    }

    /** Creates new commit after merging BRANCH.
     * Takes in CURR, GIVEN, CONFLICT. */
    public void mergeCommit(String branch, Commits curr, Commits given,
                            boolean conflict) throws IOException {
        String log = "Merged " + branch + " into "
                + Utils.readContentsAsString(_ACTIVE) + ".";

        HashMap<String, Blob> files = new HashMap<>();

        for (Map.Entry entry: curr.getFiles().entrySet()) {
            files.put((String) entry.getKey(), (Blob) entry.getValue());
        }

        for (File f : _STAGEADD.listFiles()) {
            Blob b = Utils.readObject(f, Blob.class);
            files.put(f.getName(), b);
        }

        for (File f : _STAGERM.listFiles()) {
            files.remove(f.getName());
        }

        Commits c = new Commits(log, curr, given, new Date(), files);
        persist(_COMMITS, c, c.getSha());

        Utils.writeObject(_HEAD, c);
        _branches.put(Utils.readContentsAsString(_ACTIVE), c);

        clearStage();

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Return all the files between C, M, and S.
     * @return Set of all files.
     * */
    public HashSet<String> mergeFiles(Commits c, Commits m, Commits s) {
        HashSet<String> allfiles = new HashSet<>();

        for (Map.Entry entry : c.getFiles().entrySet()) {
            String f = (String) entry.getKey();
            allfiles.add(f);
        }
        for (Map.Entry entry : m.getFiles().entrySet()) {
            String f = (String) entry.getKey();
            allfiles.add(f);
        }
        for (Map.Entry entry : s.getFiles().entrySet()) {
            String f = (String) entry.getKey();
            allfiles.add(f);
        }

        return allfiles;
    }

    /** Checks if file FILE is untracked in CWD.
     * @return whether file is untracked or not.
     * */
    public boolean untracked(String file) {
        Commits head = Utils.readObject(_HEAD, Commits.class);

        if (head.getFiles().get(file) == null
                && Utils.join(_CWD, file).exists()) {
            String mes = "There is an untracked file in the way"
                    + "; delete it, or add and commit it first.";
            System.out.println(mes);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns latest common ancestor between
     * the active branch and given BRANCH.
     */
    @SuppressWarnings("unchecked")
    public Commits getSplit(String branch) {
        _branches = Utils.readObject(_BRANCHES, TreeMap.class);

        ArrayDeque<Commits> stack = new ArrayDeque<>();
        HashSet<String> set = new HashSet<>();

        Commits currhead = Utils.readObject(_HEAD, Commits.class);
        Commits newhead = _branches.get(branch);

        stack.addLast(newhead);

        while (!stack.isEmpty()) {
            newhead = stack.removeFirst();
            set.add(newhead.getSha());

            if (newhead.getParent1() != null) {
                stack.addLast(newhead.getParent1());
            }

            if (newhead.getParent2() != null) {
                stack.addLast(newhead.getParent2());
            }
        }

        stack.addFirst(currhead);

        while (!stack.isEmpty()) {
            currhead = stack.removeFirst();
            if (set.contains(currhead.getSha())) {
                return currhead;
            }
            if (currhead.getParent1() != null) {
                stack.addLast(currhead.getParent1());
            }
            if (currhead.getParent2() != null) {
                stack.addLast(currhead.getParent2());
            }
        }

        return null;
    }

    /** Date formatting tool.*/
    private SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /** Pointer to current working directory.*/
    private File _CWD = new File(System.getProperty("user.dir"));

    /** Gitlet directory. */
    private File _GITLET = Utils.join(_CWD, ".gitlet");

    /** Pointer to stage directory for addition.*/
    private File _STAGEADD = Utils.join(_GITLET, ".stageadd");

    /** Pointer to stage directory for removal. */
    private File _STAGERM = Utils.join(_GITLET, ".stagerm");

    /** Pointer to blob directory. */
    private File _BLOB = Utils.join(_GITLET, ".blob");

    /** Pointer to commits directory.*/
    private File _COMMITS = Utils.join(_GITLET, ".commit");

    /** Pointer to head commit. */
    private File _HEAD = Utils.join(_COMMITS, ".head");

    /** Pointer to directory with branches information. */
    private File _BRANCHES = Utils.join(_COMMITS, ".master");

    /** Pointer to ACTIVE branch. */
    private File _ACTIVE = Utils.join(_COMMITS, ".active");

    /** Treemap with all branches. */
    private TreeMap<String, Commits> _branches;

    /** Size of commmits IDS. */
    private final int _size = 40;
}
