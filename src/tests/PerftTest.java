package tests;

import board.Bitboard;
import board.Move;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Check perft results for various positions.
 */
public class PerftTest {
    private static final int NODES = 0;
    private static final int CAPTURES = 1;
    private static final int EN_PASSANTS = 2;
    private static final int CASTLES = 3;
    private static final int PROMOTIONS = 4;

    // @formatter:off
    private static final int NUM_COUNTS = 5;

    private static final int INITIAL_POSITION = 0;
    private static final int POSITION_2 = 1;
    private static final int POSITION_3 = 2;
    private static final int POSITION_4 = 3;

    private static final int[][][] POSITIONS = {
        {   // Initial position
            {20,      0,     0,   0, 0}, // Depth 1
            {400,     0,     0,   0, 0},
            {8902,    34,    0,   0, 0},
            {197281,  1576,  0,   0, 0},
            {4865609, 82719, 258, 0, 0}
        },
        {   // Position 2 ("Kiwipete")
            {48,      8,      0,    2,      0}, // Depth 1
            {2039,    351,    1,    91,     0},
            {97862,   17102,  45,   3162,   0},
            {4085603, 757163, 1929, 128013, 15172},
            // {193690690, 35043416, 73365, 4993637, 8392}
        },
        {   // Position 3
            {14,     1,     0,    0, 0}, // Depth 1
            {191,    14,    0,    0, 0},
            {2812,   209,   2,    0, 0},
            {43238,  3348,  123,  0, 0},
            {674624, 52051, 1165, 0, 0}
        },
        {   // Position 4
            {6,      0,      0, 0,    0}, // Depth 1
            {264,    87,     0, 6,    48},
            {9467,   1021,   4, 0,    120},
            {422333, 131393, 0, 7795, 60032},
            //{15833292, 2046173, 6512, 0, 329464}
        }
    };
    // @formatter:on

    @Test
    public void initialPosition() {
        Bitboard board = new Bitboard();
        board.initStartingBoard();

        System.out.println("Initial Position");
        test(board, INITIAL_POSITION);
    }

    @Test
    public void position2() {
        Bitboard board = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");

        System.out.println("Position 2");
        test(board, POSITION_2);
    }

    @Test
    public void position3() {
        Bitboard board = new Bitboard("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");

        System.out.println("Position 3");
        test(board, POSITION_3);
    }

    @Test
    public void position4() {
        Bitboard board = new Bitboard("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");

        System.out.println("Position 4");
        test(board, POSITION_4);
    }

    private void test(Bitboard board, int testNumber) {
        int[][] expected = POSITIONS[testNumber];

        for (int depth = 1; depth <= expected.length; depth++) {
            int[] counts = new int[NUM_COUNTS];

            long start = System.currentTimeMillis();
            perft(board, depth, counts, null);
            long time = System.currentTimeMillis() - start;
            System.out.printf("Depth %d -- %.3fs\n", depth, time / 1000.0);

            for (int i = 0; i < NUM_COUNTS; i++) {
                assertEquals(expected[depth - 1][i], counts[i]);
            }
        }
        System.out.println();
    }

    private void perft(Bitboard board, int depth, int[] counts, Move move) {
        if (depth <= 0) {
            counts[NODES]++;
            if (move.isCapture()) {
                counts[CAPTURES]++;
            }
            if (move.isEnpassant()) {
                counts[EN_PASSANTS]++;
            }
            if (move.isCastle()) {
                counts[CASTLES]++;
            }
            if (move.isPromotion()) {
                counts[PROMOTIONS]++;
            }
            return;
        }

        List<Move> moves = board.generateMoves();
        for (Move m : moves) {
            board.applyMove(m);
            perft(board, depth - 1, counts, m);
            board.undoMove();
        }
    }
}
