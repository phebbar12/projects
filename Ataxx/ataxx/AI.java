/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;

/** A Player that computes its own moves.
 *  @author Paree Hebbar
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null || alpha >= beta) {
            return staticScore(board, WINNING_VALUE + depth);
        }

        ArrayList<Move> moves = getMoves(board);


        int bestScore = staticScore(board, WINNING_VALUE + depth);
        Move bestMove = null;

        for (Move m: moves) {
            board.makeMove(m);
            if (sense == 1) {
                bestScore = minMax(board, depth - 1,
                        false, -1, alpha, beta);
                if (bestScore > alpha) {
                    alpha = bestScore;
                    bestMove = m;
                }
            } else {
                bestScore = minMax(board, depth - 1,
                        false, 1, alpha, beta);
                if (bestScore < beta) {
                    beta = bestScore;
                    bestMove = m;
                }
            }
            board.undo();
        }

        if (saveMove) {
            _lastFoundMove = bestMove;
        }
        return bestScore;

    }

    /** Return list of all possible moves on the current BOARD. */
    private ArrayList<Move> getMoves(Board board) {
        ArrayList<Move> moves = new ArrayList<Move>();

        for (int i = 0; i < board.EXTENDED_SIDE * board.EXTENDED_SIDE; i++) {
            if (board.get(i) == board.whoseMove()) {
                for (int row = 0; row < 5; row++) {
                    for (int j = board.neighbor(i, -2, -2 + row);
                         j <= board.neighbor(i, 2, -2 + row); j++) {
                        if (board.get(j) == EMPTY) {
                            char c0 = board.unindexC(i);
                            char r0 = board.unindexR(i);
                            char c1 = board.unindexC(j);
                            char r1 = board.unindexR(j);
                            Move m = Move.move(c0, r0, c1, r1);
                            moves.add(m);
                        }

                    }
                }
            }
        }
        if (board.legalMove(Move.pass())) {
            moves.add(Move.pass());
        }
        return moves;
    }


    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }

        return board.redPieces() - board.bluePieces();
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
}
