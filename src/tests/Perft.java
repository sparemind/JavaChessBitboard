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
        Bitboard board = new Bitboard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        // board.initStartingBoard();

        // System.out.println(perft(board, 5));

        long start = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            int nodes = perft(board, i);
            System.out.printf("Depth %d - Nodes: %d\n", i, nodes);
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
            // out.println(combs);
            c++;
            // if (c % 10000 == 0) System.out.println(c);
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
