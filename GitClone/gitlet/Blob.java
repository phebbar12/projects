package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/** Stores metadata regarding files and persists
 * this data to a blob subdirectory.
 *  @author Paree Hebbar
 */
public class Blob implements Serializable {

    /** The file NAME in CWD has its metadata stored in BLOB. */
    public Blob(String name, File cwd, File blob) throws IOException {
        _name = name;
        _contents = Utils.readContentsAsString(Utils.join(cwd, name));
        shaID = Utils.sha1(Utils.serialize(_contents));
        File s = Utils.join(blob, shaID);
        s.createNewFile();
        Utils.writeObject(s, this);
    }

    /** Returns whether this blob is equal to blob OTHER. */
    public boolean isEqual(Blob other) {
        if (other == null) {
            return false;
        }
        return _contents.equals(other.getContents());
    }

    /** Returns contents of file. */
    public String getContents() {
        return _contents;
    }

    /** Returns name of file. */
    public String getName() {
        return _name;
    }

    /** Returns hash ID of file. */
    public String getSha() {
        return shaID;
    }

    /** Unique hash ID of file. */
    private final String shaID;

    /** Contents of file. */
    private String _contents;

    /** Name of file. */
    private String _name;
}
