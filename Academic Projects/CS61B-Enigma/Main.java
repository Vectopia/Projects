package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Vector Wang
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
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
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
     *  results to _output.
     *  Used some conceptual help from piazza posts and TA answers
     *  but worked on my own.*/

    private void process() {
        Machine process = readConfig();
        if (!_input.hasNext("\\*")) {
            throw error("Wrong setting!");
        }
        String temp = _input.nextLine();
        setUp(process, temp);
        try {
            for (; _input.hasNextLine(); ) {
                if (_input.hasNext("\\*")) {
                    temp = getNextInput();
                    for (; temp.equals(""); ) {
                        _output.println();
                        temp = getNextInput();
                    }
                    setUp(process, temp);
                } else {
                    temp = getNextInput();
                    if (temp.equals("")) {
                        _output.println();
                    } else {
                        temp = temp.replaceAll("\\s|[^"
                                + _alphabet.getChars()
                                + "]", "");
                        printMessageLine(process.convert(temp));
                    }
                }
            }
        } catch (OutOfMemoryError | IndexOutOfBoundsException flux) {
            throw error("Configuration "
                    + "ran out of memory "
                    + "or might be out of bound!");
        }
    }

    /** A Private Method.
     * @ return a next line.*/
    private String getNextInput() {
        return _input.nextLine();
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            final String config = _config.next();
            if (config.contains(")")) {
                throw error("Contains illegal characters ')' in config!");
            } else if (config.contains("(")) {
                throw error("Contains illegal characters '(' in config!");
            } else if (config.contains("*")) {
                throw error("Contains illegal characters '*' in config!");
            } else if (config.contains(";")) {
                throw error("Contains illegal characters ';' in config!");
            } else {
                _alphabet = new Alphabet(config);
                if (!_config.hasNextInt()) {
                    throw error("Wrong rotor setting!");
                }

                final int rotorNumbers = _config.nextInt();
                if (!_config.hasNextInt()) {
                    throw error("Wrong rotor setting!");
                }

                final int movingRotors = _config.nextInt();
                if (movingRotors < 0 || movingRotors >= rotorNumbers) {
                    throw error("Rotor pawls are set wrong!");
                }

                ArrayList<Rotor> rotorsTotal = new ArrayList<>();
                _next = _config.next();
                for (; _config.hasNext(); ) {
                    rotorsTotal.add(readRotor());
                }
                return new Machine(_alphabet, rotorNumbers,
                        movingRotors, rotorsTotal);
            }
        } catch (NoSuchElementException error) {
            throw error("Configuration "
                    + "file does not exist!");
        } catch (OutOfMemoryError | IndexOutOfBoundsException flux) {
            throw error("Configuration "
                    + "ran out of memory "
                    + "or might be out of bound!");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            if (_next.contains("(")) {
                throw error("Wrong rotor name!");
            } else if (_next.contains(")")) {
                throw error("Wrong rotor name!");
            }
            String rotorName; String atNotches; char rotorType;
            rotorName = _next; _next = _config.next();
            rotorType = _next.charAt(0); atNotches = _next.substring(1);

            if (rotorType == 'M') {
                if (atNotches.equals("")) {
                    throw error("Wrong moving rotors setting!");
                }
            }

            String per = ""; _next = _config.next();

            while (_next.contains("(") && _config.hasNext()) {
                if (!_next.contains(")")) {
                    throw error("Not a completed cycle");
                }
                per = per.concat(_next + " ");
                _next = _config.next();
            }

            if (!_config.hasNext()) {
                per = per.concat(_next + " ");
            }

            if (rotorType == 'R') {
                return new Reflector(rotorName,
                        new Permutation(per, _alphabet));
            } else if (rotorType == 'N') {
                return new FixedRotor(rotorName,
                        new Permutation(per, _alphabet));
            } else if (rotorType == 'M') {
                return new MovingRotor(rotorName,
                        new Permutation(per, _alphabet), atNotches);
            } else {
                throw error("Wrong rotor type!");
            }
        } catch (NoSuchElementException error) {
            throw error("No rotor settings!");
        } catch (OutOfMemoryError | IndexOutOfBoundsException flux) {
            throw error("Configuration "
                    + "ran out of memory "
                    + "or might be out of bound!");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] setString = settings.split(" ");
        int index = 0;
        if (setString.length < M.numRotors() + 1) {
            throw new EnigmaException("Setting not enough");
        }

        String[] newRotors = new String[M.numRotors()];
        index = 1;
        while (index <= M.numRotors()) {
            newRotors[index - 1] = setString[index];
            index = index + 1;
        }

        index = 0;
        while (index < newRotors.length - 1) {
            for (int iter = index + 1; iter < newRotors.length; iter =
                    iter + 1) {
                if (newRotors[index].equals(newRotors[iter])) {
                    throw error("Repeated rotors found!");
                }
            }
            index = index + 1;
        }

        String permuteOut = "";
        index = 7;
        while (index < setString.length) {
            permuteOut = permuteOut
                    .concat(setString[index] + " ");
            index = index + 1;
        }
        M.insertRotors(newRotors);

        if (!M._rotorsNew.get(0).reflecting()) {
            throw error("Wrong reflector setting!");
        }

        M.setRotors(setString[1 + M.numRotors()]);
        M.setPlugboard(new Permutation(permuteOut, _alphabet));
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int index = 0; int unprinted = 0; int length = msg.length();
        while (index < length) {

            unprinted = length - index;
            if (unprinted > 5) {
                _output.print(msg
                        .substring(index, index + 5) + " ");
            } else {
                _output.println(msg
                        .substring(index, index + unprinted));
            }
            index = index + 5;
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** The latest input from config. */
    private String _next;

}
