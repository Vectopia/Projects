package enigma;

import static enigma.EnigmaException.error;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *
 *  @author Vector Wang
 *  */
class Alphabet {
    /** The alphabet. */
    private String _chars;
    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if preprocess(CH) is in this alphabet. */
    boolean contains(char ch) {
        return _chars.contains(String.valueOf(ch));
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index >= 0 && index < size()) {
            return _chars.charAt(index);

        } else {
            throw error("Index out of bounds.");
        }
    }

    /** Returns the index of character preprocess(CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        int result = _chars.indexOf(ch);
        if (result == -1) {
            throw error("Character not found.");
        } else {
            return result;
        }
    }

    /** Returns the alphabet as string. */
    String getChars() {
        return _chars;
    }
}
