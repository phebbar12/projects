package gitlet;

import java.io.Serializable;

import java.util.Date;
import java.util.HashMap;

/** Stores metadata for commits.
 *  @author Paree Hebbar
 */
public class Commits implements Serializable {

    /** Creates commit object with metadata about its
     * MESSAGE, PARENT1, PARENT2, TIMESTAMP, and FILES.
     */
    public Commits(String message, Commits parent1, Commits parent2,
                   Date timestamp, HashMap<String, Blob> files) {
        _message = message;
        _parent1 = parent1;
        _parent2 = parent2;
        _timestamp = timestamp;
        _files = files;
        shaID = Utils.sha1(Utils.serialize(this));
    }

    /** Returns hashID for this commit. */
    public String getSha() {
        return shaID;
    }

    /** Returns message of this commit. */
    public String getMessage() {
        return _message;
    }

    /** Returns timestamp of this commit. */
    public Date getTimestamp() {
        return _timestamp;
    }

    /** Returns first parent of this commit. */
    public Commits getParent1() {
        return _parent1;
    }

    /** Returns second parent of this commit. */
    public Commits getParent2() {
        return _parent2;
    }

    /** Checks to see if OTHER commit is the same as this commit OTHER.
     * @return boolean
     * */
    public boolean isEqual(Commits other) {
        if (other != null) {
            return getSha().equals(other.getSha());
        } else {
            return false;
        }
    }

    /** Returns files stored by this commit. */
    public HashMap<String, Blob> getFiles() {
        return _files;
    }

    /** Files stored by this commit. */
    private final HashMap<String, Blob> _files;

    /** Hash ID of this commit. */
    private final String shaID;

    /** Message of this commit. */
    private final String _message;

    /** Timestamp of this commit. */
    private final Date _timestamp;

    /** First parent of this commit. */
    private final Commits _parent1;

    /** Second parent of this commit. */
    private final Commits _parent2;
}
