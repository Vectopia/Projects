package enigma;

import java.util.Collection;

import java.util.ArrayList;

import static enigma.EnigmaException.error;

/** Class that represents a complete enigma machine.
 *
 *  @author Vector Wang
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {

        if (numRotors <= 1 || pawls < 0 || pawls >= numRotors) {
            throw error("Wrong machine setting!");
        }

        _alphabet = alpha; _pawlsNumber = pawls;
        _rotorsNumber = numRotors; _rotorsNew = new ArrayList<>();
        _rotorsTotal = new ArrayList<>(allRotors);

    }

    /** Return the number of rotor slots. */
    int numRotors() {

        return _rotorsNumber;
    }

    /** Return the number pawls (and thus rotating rotors). */
    int numPawls() {

        return _pawlsNumber;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotorsNew = new ArrayList<>();

        if (rotors.length == _rotorsNumber) {
            for (String rotorInsertion : rotors) {
                for (Rotor rotorCompare : _rotorsTotal) {
                    if (rotorCompare.name()
                            .equals(rotorInsertion)) {
                        _rotorsNew.add(rotorCompare);
                    }
                }
            }

            detectIdenticalError(rotors);
            detectReflectError();
            detectOrderError(numRotors(), numPawls(), numRotors(), 0, false);
            detectOrderError(1, 0, numRotors(), numPawls(), true);

        } else {
            throw error("Wrong rotors number.");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {

        if (setting.length() == numRotors() - 1) {
            int index = 0;

            while (index <= setting.length() - 1) {
                if (_alphabet.contains(setting.charAt(index))) {
                    _rotorsNew.get(index + 1)
                            .set(setting.charAt(index));
                } else {
                    throw error("Wrong setting strings!");
                }
                index = index + 1;
            }
        } else {
            throw error("Wrong setting numbers!");
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {

        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        final int ending = _rotorsNumber - 1;
        int feedback = _plugboard.permute(c);
        int[] notchMove = new int[_rotorsNumber]; int index = 0;

        for (; index < ending + 1; index = index + 1) {
            if (_rotorsNew.get(index).atNotch()) {
                notchMove [index] = 1;
            } else {
                notchMove [index] = 0;
            }
        }
        if (notchMove[ending] == 0) {
            _rotorsNew.get(ending).advance();
        }

        index = ending;
        while (index > 0) {
            if (notchMove[index] == 1) {
                _rotorsNew.get(index).advance();
                if (notchMove[index - 1] == 0) {
                    _rotorsNew.get(index - 1).advance();
                }
            }
            index = index - 1;
        }

        index = ending;
        for (; index >= 0; index = index - 1) {
            feedback = _rotorsNew.get(index).convertForward(feedback);
        }

        index = 1;
        for (; index <= ending; index = index + 1) {
            feedback = _rotorsNew.get(index).convertBackward(feedback);
        }
        return _plugboard.permute(feedback);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        int index = 0; String encode = "";
        StringBuilder decode = new StringBuilder();

        while (index <= msg.length() - 1) {
            encode = String.valueOf
                    (_alphabet.toChar(convert(_alphabet
                    .toInt(msg.charAt(index)))));
            decode.append(encode);
            index = index + 1;
        }
        return decode.toString();
    }

    /** Common alphabet of rotors. */
    private final Alphabet _alphabet;

    /** The number of pawls. */
    private final int _pawlsNumber;

    /** The number of rotors. */
    private final int _rotorsNumber;

    /** The sorted list of rotors used in the machine. */
    protected ArrayList<Rotor> _rotorsNew;

    /** The array list of rotors that can be used. */
    private final ArrayList<Rotor> _rotorsTotal;

    /** The plugboard that is used. */
    private Permutation _plugboard;

    /** A private method that checks for errors while inserting rotors.
     * @param x **The  starting variable of starting point **
     * @param y **The ending variable of starting point **
     * @param j **The starting variable of ending point **
     * @param k **The ending variable of ending point **
     * @param cond  **The variable of conditions **
     */
    private void detectOrderError(int x, int y, int j, int k, boolean cond) {
        try {
            for (int i = x - y; i < j - k; i = i + 1) {
                if (_rotorsNew.get(i).rotates() == cond) {
                    throw error("Wrong rotor order!");
                }
            }
        } catch (IndexOutOfBoundsException exception) {
            throw error("Wrong Setting!");
        }

    }


    /** A private method that checks for reflector errors.*/
    protected void detectReflectError() {
        if (!_rotorsNew.get(0).reflecting()) {
            throw error("The first rotor is not a reflector.");
        }
    }

    /** A private method that checks for identical rotors settings errors.
     * @param rotors **String of rotors to check for identical settings **
     * */
    private void detectIdenticalError(String[] rotors) {
        int rotorA = 0;
        while (rotorA <= rotors.length) {
            int rotorB = rotorA + 1;
            while (rotorB < rotors.length) {
                if (rotors[rotorA]
                        .equals(rotors[rotorB])) {
                    throw error("Identical rotors settings found!");
                }
                rotorB = rotorB + 1;
            }
            rotorA = rotorA + 1;
        }
    }

}
