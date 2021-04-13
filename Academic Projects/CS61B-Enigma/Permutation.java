package enigma;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Vector Wang
 */
class Permutation {
    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycle = cycles.replaceAll("(\\s)+", "");

    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {

        _cycle = _cycle + cycle;
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

        return _alphabet.toInt(permute(_alphabet.toChar(wrap(p))));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {

        return _alphabet.toInt(invert(_alphabet.toChar(wrap(c))));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        char permute = p;
        if (!_alphabet.contains(p) || _cycle == null) {
            return permute;
        }
        int index = _cycle.indexOf(String.valueOf(p));
        if (index != -1) {
            int nextIndex = index + 1;
            if (_cycle.charAt(nextIndex) != ')') {
                permute = _cycle.charAt(nextIndex);
            } else {
                int prevIndex = index;
                while (_cycle.charAt(prevIndex) != '(') {
                    prevIndex = prevIndex - 1;
                }
                permute = _cycle.charAt(prevIndex + 1);
            }
        }
        return permute;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        char invert = c;
        if (!_alphabet.contains(c) || _cycle.equals("")) {
            return invert;
        }

        int index = _cycle.indexOf(String.valueOf(c));
        if (index != -1) {
            int prevIndex = index - 1;
            if (_cycle.charAt(prevIndex) != '(') {
                invert = _cycle.charAt(prevIndex);
            } else {
                int nextIndex = index;
                while (_cycle.charAt(nextIndex) != ')') {
                    nextIndex = nextIndex + 1;
                }
                invert = _cycle.charAt(nextIndex - 1);
            }
        }
        return invert;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {

        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int index = 0;

        while (index <= _alphabet.size() - 1) {
            char indexChar = _alphabet.toChar(index);
            if (indexChar == permute(indexChar)) {
                return false;
            }
            index = index + 1;
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Cycle of the characters. */
    private String _cycle;
}
