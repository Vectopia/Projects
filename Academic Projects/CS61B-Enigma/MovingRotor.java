package enigma;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Vector Wang
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        if (notches != null) {
            _notch = new int[notches.length()]; int index = 0;
            while (index < notches.length()) {
                _notch[index] = alphabet().toInt(notches
                        .charAt(index));
                index = index + 1;
            }
        }
    }

    @Override
    boolean rotates() {

        return true;
    }

    @Override
    boolean atNotch() {
        boolean result = false;
        if (_notch == null) {
            return result;
        } else {
            for (int index : _notch) {
                if (index == setting()) {
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    void advance() {
        set(permutation().
                wrap(setting() + 1));
    }

    /** The index of location of notch. */
    private int[] _notch;
}
