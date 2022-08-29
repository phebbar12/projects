package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Paree Hebbar
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {

        Repo repo = new Repo();
        File git = new File(".gitlet");

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (args[0].equals("init") && validateArgs(args, 1)) {
            repo.init();
        } else if (git.exists()) {
            if (args[0].equals("add") && validateArgs(args, 2)) {
                repo.add(args[1]);
            } else if (args[0].equals("commit") && validateArgs(args, 2)) {
                repo.commit(args[1]);
            } else if (args[0].equals("rm") && validateArgs(args, 2)) {
                repo.remove(args[1]);
            } else if (args[0].equals("checkout")) {
                if (args.length == 2) {
                    repo.checkoutBranch(args[1]);
                } else if (args[1].equals("--") && validateArgs(args, 3)) {
                    repo.checkout(args[2]);
                } else if (args[2].equals("--") && validateArgs(args, 4)) {
                    repo.checkout(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands");
                }
            } else if (args[0].equals("log") && validateArgs(args, 1)) {
                repo.log();
            } else if (args[0].equals("global-log") && validateArgs(args, 1)) {
                repo.globalLog();
            } else if (args[0].equals("find") && validateArgs(args, 2)) {
                repo.find(args[1]);
            } else if (args[0].equals("status") && validateArgs(args, 1)) {
                repo.status();
            } else if (args[0].equals("branch") && validateArgs(args, 2)) {
                repo.makebranch(args[1]);
            } else if (args[0].equals("rm-branch") && validateArgs(args, 2)) {
                repo.removeBranch(args[1]);
            } else if (args[0].equals("status") && validateArgs(args, 1)) {
                repo.status();
            } else if (args[0].equals("reset") && validateArgs(args, 2)) {
                repo.reset(args[1]);
            } else if (args[0].equals("merge") && validateArgs(args, 2)) {
                repo.merge(args[1]);
            } else {
                System.out.println("No command with that name exists.");
            }
        } else {
            System.out.println("Not in an initialized Gitlet directory.");
        }
    }

    /** Checks if the arguments ARGUMENTS have right lenght LENGTH.
     * @return boolean
     * */
    public static boolean validateArgs(String[] arguments, int length) {
        if (arguments.length == length) {
            return true;
        } else {
            System.out.println("Incorrect operands");
            return false;
        }
    }


}
