/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Vector Wang
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 30;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        int index = 0;
        _moves.clear();
        setMoveLimit(DEFAULT_MOVE_LIMIT);

        while (index < Math.pow(BOARD_SIZE, 2)) {


            int rowNum = index / BOARD_SIZE;
            int colNum = index % BOARD_SIZE;
            Piece currentLoca = contents[colNum][rowNum];

            if (currentLoca == WP) {
                _bkCount =  _bkCount + 1;
            } else if (currentLoca == BP) {
                _wtCount = _wtCount + 1;
            }

            _board[colNum * BOARD_SIZE + rowNum] =
                    currentLoca;
            _squareBoard[rowNum][colNum] =
                    sq(rowNum, colNum);
            set(_squareBoard[rowNum][colNum],
                    currentLoca);
            index = index + 1;
        }

        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        int index = 0;
        if (board == this) {
            return;
        }
        while (index < Math.pow(BOARD_SIZE, 2)) {
            int rowNum = index / BOARD_SIZE;
            int colNum = index % BOARD_SIZE;
            Piece currentLoca = board.get(sq(rowNum, colNum));
            String abbrev = currentLoca.abbrev();

            if (abbrev.equals("b")) {
                _bkCount = _bkCount + 1;
            } else if (abbrev.equals("w")) {
                _wtCount = _wtCount + 1;
            }

            _squareBoard[rowNum][colNum] =
                    sq(rowNum, colNum);
            set(_squareBoard[rowNum][colNum],
                    currentLoca);
            index = index + 1;
        }

        _moves.clear();
        _moves.addAll(board._moves);
        _moveLimit = DEFAULT_MOVE_LIMIT;
        _turn = board.turn();
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        if (next != null) {
            _turn = next;
        }
        _board[sq.index()] = v;
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to producej
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);

        movesCount = movesCount + 1;

        Square toMove = move.getFrom(),
                cache = move.getTo();

        move = Move.mv(toMove, cache,
                get(cache)
                        == get(toMove).opposite());

        _subsetsInitialized = false;
        _turn = turn().opposite();
        _moves.add(move);
        set(cache, get(toMove)); set(toMove, EMP);
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;

        Move temp = _moves.remove(_moves.size() - 1);
        Square toMove = temp.getFrom(),
                cache = temp.getTo();

        _turn = turn().opposite();
        _subsetsInitialized = false;

        set(toMove, get(cache));
        set(cache, temp.isCapture()
                ? get(cache).opposite() : EMP);
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        if (_turn != get(from)
                || !from.isValidMove(to)
                || blocked(from, to)) {

            return false;
        }

        int     length = from.distance(to),
                navigation = from.direction(to),
                dire = to.direction(from),
                count = 0, index = 1;

        ArrayList<Square> cacheList = new ArrayList<>();
        cacheList.add(from);

        while (from.moveDest(navigation, index) != null) {

            cacheList.add(
                    from.moveDest(
                            navigation, index));
            index = index + 1;
        }

        index = 1;
        while (from.moveDest(dire, index) != null) {

            cacheList.add(
                    from.moveDest(
                            dire, index));
            index = index + 1;
        }

        for (Square cacheSquare: cacheList) {
            if (get(cacheSquare) != EMP) {

                count = count + 1;
            }
        }

        return length == count;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return move != null && isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    ArrayList<Move> legalMoves() {
        int index1 = 0;
        ArrayList<Move> legalMove = new ArrayList<>();

        while (index1 < Math.pow(BOARD_SIZE, 2)) {
            int col = index1 / BOARD_SIZE,
                    row = index1 % BOARD_SIZE, index2 = 0;
            Square toMove = sq(col, row);

            while (index2 < Math.pow(BOARD_SIZE, 2)) {
                int tcol = index2 / BOARD_SIZE, trow = index2 % BOARD_SIZE;
                Move temp = Move.mv(toMove,
                        sq(tcol, trow));

                if (isLegal(temp)) {
                    legalMove.add(temp);
                }
                index2 = index2 + 1;
            }
            index1 = index1 + 1;
        }

        return legalMove;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        boolean wCount = piecesContiguous(WP);
        boolean bCount = piecesContiguous(BP);

        if (bCount || wCount || movesMade() >= _moveLimit) {
            _winnerKnown = true;
        }

        if (!_winnerKnown) {
            return null;
        } else {

            if (bCount && wCount) {
                if (turn() == WP) {

                    _winner = BP;
                } else {

                    _winner = WP;
                }
            } else if (bCount) {

                _winner = BP;
            } else if (wCount) {

                _winner = WP;
            }

            if ((_winner == null) && (_moves.size()
                    == DEFAULT_MOVE_LIMIT + DEFAULT_MOVE_LIMIT)) {

                _winner = EMP;
            }
            return _winner;
        }
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        int distance = from.distance(to), direction = from.direction(to);
        int stepCount = 1;
        while (stepCount <= distance) {
            Square currSquare = from.moveDest(direction, stepCount);
            if (currSquare == null
                    || (stepCount < distance
                    && get(currSquare) == get(from).opposite())
                    || (stepCount == distance
                    && get(currSquare) == get(from))) {
                return true;
            }
            stepCount++;
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        int size = 0;
        if (!visited[sq.col()][sq.row()]) {
            visited[sq.col()][sq.row()] = true;
            if (get(sq) == p) {
                size++;
                for (Square el: sq.adjacent()) {
                    if (get(el) == p) {
                        size += numContig(el, visited, p);
                    }
                }
            }
        }
        return size;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();

        visitReset();
        for (int index = 0;
             index < Math.pow(BOARD_SIZE, 2);
             index = index + 1) {
            Square temp = sq(index / BOARD_SIZE,
                    index % BOARD_SIZE);

            if (get(temp) == BP) {
                int toMoveSize = numContig(temp, _visitState, BP);
                if (toMoveSize != 0) {
                    _blackRegionSizes.add(toMoveSize);
                }
            }
            if (get(temp) == WP) {
                int toMoveSize = numContig(temp, _visitState, WP);
                if (toMoveSize != 0) {
                    _whiteRegionSizes.add(toMoveSize);
                }
            }

        }
        _whiteRegionSizes.sort(Collections.reverseOrder());
        _blackRegionSizes.sort(Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Initialize visited to all false. */
    protected void visitReset() {

        _visitState = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (boolean[] temp : _visitState) {
            Arrays.fill(temp, false);
        }
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {

            return _whiteRegionSizes;
        } else {

            return _blackRegionSizes;
        }
    }

    /** Current total white count. */
    private int _wtCount;

    /** Current total black count. */
    private int _bkCount;

    /** Visited field for numContig. */
    private boolean[][] _visitState = null;

    /** Return the current count.
     * @param id to identify black or white*/
    protected int count(char id) {
        if (id == 'b') {

            return _bkCount;
        } else if (id == 'w') {

            return _wtCount;
        } else {

            return -1;
        }
    }

    /** Current total move count. */
    private int movesCount;
    /** A new array of squares in their respective positions. */
    private Square[][] _squareBoard = new Square[BOARD_SIZE][BOARD_SIZE];

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
            { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
            _whiteRegionSizes = new ArrayList<>(),
            _blackRegionSizes = new ArrayList<>();
}
