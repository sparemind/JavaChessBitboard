package tests;

import board.Bitboard;
import board.Move;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

/**
 * PERFormance Test.
 */
public class Perft {
    public static void main(String[] args) {
        // Bitboard board = new Bitboard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        // Bitboard board = new Bitboard("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 0");
        Bitboard board = new Bitboard("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        // board.initStartingBoard();

        long start = System.currentTimeMillis();

        // System.out.println(board);
        System.out.println(perft(board, 4));
        for (int i = 0; i < 6; i++) {
            // int nodes = perft(board, i);
            // System.out.printf("Depth %d - Nodes: %d\n", i, nodes);
        }
        long time = System.currentTimeMillis() - start;
        System.out.printf("Finished in: %.3fs\n", time / 1000.0);
    }

    static PrintStream out;

    static {
        try {
            out = new PrintStream(new File("me.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static Stack<String> combs = new Stack<>();
    static int c = 0;

    public static int perft(Bitboard board, int depth) {
        if (depth <= 0) {
            out.println(combs);
            c++;
            if (c % 10000 == 0) System.out.println(c);
            return 1;
        }

        List<Move> moves = board.generateMoves();
        int nodes = 0;
        for (Move m : moves) {
            board.applyMove(m);
            combs.push(m.toString());
            nodes += perft(board, depth - 1);
            combs.pop();
            board.undoMove();
        }
        return nodes;
    }
}
