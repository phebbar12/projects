package enigma;

import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Paree Hebbar
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _perms = new HashMap<Character, Character>(size());
        String currcycle = "";
        for (int i = 0; i < cycles.length(); i++) {
            if (cycles.charAt(i) == '(') {
                currcycle = "";
            } else if (cycles.charAt(i) == ')') {
                addCycle(currcycle);
            } else {
                currcycle += cycles.charAt(i);
            }
        }



    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        if (!cycle.equals("")) {
            int i = 0;
            while (i < cycle.length() - 1) {
                _perms.put(cycle.charAt(i), cycle.charAt(i + 1));
                i++;
            }
            _perms.put(cycle.charAt(i), cycle.charAt(0));
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char atP = _alphabet.toChar(wrap(p));
        if (_perms.containsKey(atP)) {
            char mapP = _perms.get(atP);
            return _alphabet.toInt(mapP);
        } else {
            return p;
        }
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char atC = _alphabet.toChar(wrap(c));

        if (_perms.containsValue(atC)) {
            for (char key: _perms.keySet()) {
                if (_perms.get(key).equals(atC)) {
                    return _alphabet.toInt(key);
                }
            }
        }
        return c;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int index = _alphabet.toInt(p);
        return _alphabet.toChar(permute(index));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int index = _alphabet.toInt(c);
        return _alphabet.toChar(invert(index));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < _alphabet.size(); i++) {
            if (_perms.get(_alphabet.toChar(i)) == null) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** HashMap to store the permutation of each character. */
    private HashMap<Character, Character> _perms;

}
