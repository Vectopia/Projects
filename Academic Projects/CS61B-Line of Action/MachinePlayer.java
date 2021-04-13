/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import static loa.Piece.*;

import java.util.List;


/** An automated Player.
 *  @author Vector Wang
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {

        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        int value; Board temp = new Board(getBoard());
        assert side() == temp.turn(); _foundMove = null;

        if (side() == WP) {
            value = findMove(temp, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(temp, chooseDepth(), true, -1, -INFTY, INFTY);
        }

        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {

        if (depth == 0
                || board.legalMoves().size() == 0
                || board.gameOver()) {
            return findScore(board);
        }

        int value; Move storage = null;
        List<Move> result = board.legalMoves();

        if (sense != 1) {

            value = INFTY;
        } else {

            value = -INFTY;
        }

        for (Move temp : result) {
            Board cache = new Board(board);
            cache.makeMove(temp);
            int score = findMove(cache,
                    depth - 1,
                    saveMove, -sense,
                    alpha, beta);

            if (score * sense > value * sense) {
                storage = temp;
                value = score;
            }

            if (sense < 0) {
                beta = Math.min(beta, score);
            } else if (sense > 0) {
                alpha = Math.max(alpha, score);
            }

            if (alpha >= beta) {
                break;
            }
        }

        if (saveMove) {
            _foundMove = storage;
        }

        return value;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 3;
    }

    /** A helper function to get the score.
     * @return an int value.
     * @param tempBoard a board. */

    int findScore(Board tempBoard) {

        if (tempBoard.winner() == WP) {

            return WINNING_VALUE;
        }

        if (tempBoard.winner() == EMP) {

            return 0;
        }

        if (tempBoard.winner() == BP) {

            return -WINNING_VALUE;
        } else {

            return tempBoard.getRegionSizes(BP).size()
                    - tempBoard.getRegionSizes(WP).size();
        }
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}
