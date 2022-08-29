package enigma;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Paree Hebbar
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (pawls >= numRotors) {
            throw new EnigmaException("invalid configuration");
        }
        _alphabet = alpha;
        _numRoters = numRotors;
        _pawls = pawls;
        Iterator<Rotor> r = allRotors.iterator();
        while (r.hasNext()) {
            Rotor curr = r.next();
            _allRotors.put(curr.name(), curr);
        }
        _rotors = new Rotor[_numRoters];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRoters;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {

        for (int i = 0; i < rotors.length; i++) {
            _rotors[i] = _allRotors.get(rotors[i]);
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 1; i < _rotors.length; i++) {
            _rotors[i].set(setting.charAt(i - 1));
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        boolean[] rotates = new boolean[numRotors()];
        rotates[_numRoters - 1] = true;


        for (int i = numRotors() - numPawls() + 1; i < numRotors(); i++) {
            if (_rotors[i].atNotch()) {
                rotates[i] = true;
                rotates[i - 1] = true;
            }
        }

        for (int i = 0; i < _rotors.length; i++) {
            if (rotates[i]) {
                _rotors[i].advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = numRotors() - 1; i >= 0; i--) {
            c = _rotors[i].convertForward(c);
        }
        for (int i = 1; i < numRotors(); i++) {
            c = _rotors[i].convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            if (alphabet().contains(msg.charAt(i))) {
                int c = alphabet().toInt(msg.charAt(i));
                result += alphabet().toChar(convert(c));
            }
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** number of rotors this machine has. */
    private int _numRoters;

    /** number of moving pawls this machine has. */
    private int _pawls;

    /** Hashmap of all rotors available to this machine. */
    private HashMap<String, Rotor> _allRotors = new HashMap<>();

    /** List of the rotors this machine is using. */
    private Rotor[] _rotors;

    /** Letters swapped by the plugboard. */
    private Permutation _plugboard;

}
