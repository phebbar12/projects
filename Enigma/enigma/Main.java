package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;


import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Paree Hebbar
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String setting = _input.nextLine();
        if (setting.charAt(0) != '*') {
            throw new EnigmaException("settings required");
        } else {
            setUp(enigma, setting);
        }
        while (_input.hasNextLine()) {
            String line = _input.nextLine();

            if (line.length() != 0 && line.charAt(0) == '*') {
                setUp(enigma, line);
            } else {
                String[] messages = line.split(" ");
                String encoded = "";
                for (String msg: messages) {
                    encoded += enigma.convert(msg);
                }
                printMessageLine(encoded);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphabet = _config.next();
            if (alphabet.contains("(")
                    || alphabet.contains("*")
                    || alphabet.contains(")")) {
                throw new EnigmaException("incorrect configuration format");
            }
            _alphabet = new Alphabet(alphabet);
            if (!_config.hasNextInt()) {
                throw new EnigmaException("incorrect configuration format");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("incorrect configuration format");
            }
            int pawls = _config.nextInt();
            ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String typenotches = _config.next();
            String perm = "";

            while (_config.hasNext("\\(.*\\)")) {
                perm += _config.next();
            }

            Permutation perms = new Permutation(perm, _alphabet);

            char type = typenotches.charAt(0);

            if (type == 'M') {
                String notches = typenotches.substring(1);
                return new MovingRotor(name, perms, notches);
            } else if (type == 'N') {
                return new FixedRotor(name, perms);
            } else if (type == 'R') {
                return new Reflector(name, perms);
            } else {
                throw new EnigmaException("invalid rotor type");
            }

        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner setting = new Scanner(settings.substring(1));
        String[] rotors = new String[M.numRotors()];

        for (int i = 0; i < M.numRotors(); i++) {
            rotors[i] = setting.next();
        }

        String pos = setting.next();
        String perm = "";

        for (int i = 0; i < rotors.length; i++) {
            for (int x = 0; x < rotors.length; x++) {
                if (i != x && rotors[i].equals(rotors[x])) {
                    throw new EnigmaException("invald");
                }
            }
        }

        while (setting.hasNext()) {
            String nxt  = setting.next();
            if (!nxt.contains("(")) {
                throw new EnigmaException("incorrect length");
            }
            perm += nxt;
        }



        M.setPlugboard(new Permutation(perm, _alphabet));
        M.insertRotors(rotors);
        M.setRotors(pos);

        if (!M.getRotor(0).reflecting()) {
            throw new EnigmaException("invalid positions");
        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        while (msg.length() > 5) {
            _output.print(msg.substring(0, 5));
            _output.print(" ");
            msg = msg.substring(5);
        }
        _output.println(msg);

    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
