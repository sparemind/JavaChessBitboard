package tests;

import board.Bitboard;
import board.Move;

import java.util.List;

/**
 * PERFormance Test.
 */
public class PerftTimingTest {
    private static final int WARMUP = 2;
    private static final int TRIALS = 5;
    private static final int DEPTH = 5;

    public static void main(String[] args) {
        Bitboard board = new Bitboard();
        board.initStartingBoard();

        for (int i = 0; i < WARMUP; i++) {
            perft(board, DEPTH);
            System.out.println("WARMUP: " + i);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < TRIALS; i++) {
            long trialStart = System.currentTimeMillis();
            int nodes = perft(board, DEPTH);
            long trialTime = System.currentTimeMillis() - trialStart;

            System.out.printf("Nodes: %d, Time: %.3f\n", nodes, trialTime / 1000.0);
        }
        long time = System.currentTimeMillis() - start;

        System.out.printf("Average: %.3fs\n", time / 1000.0 / TRIALS);
    }

    private static int perft(Bitboard board, int depth) {
        if (depth <= 0) {
            return 1;
        }

        List<Move> moves = board.generateMoves();
        if (depth == 1) {
            return moves.size();
        }

        int nodes = 0;
        for (Move m : moves) {
            board.applyMove(m);
            nodes += perft(board, depth - 1);
            board.undoMove();
        }
        return nodes;
    }
}
