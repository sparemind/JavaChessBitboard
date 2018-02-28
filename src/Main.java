import board.Bitboard;
import board.Move;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Bitboard b = new Bitboard("8/8/8/8/8/8/3P4/8 w KQkq - 0 0");
        b.initStartingBoard();

        List<Move> moves = b.generatePseudoMoves();
        System.out.println(moves.size());
        System.out.println(moves);

        System.out.println(b);
        System.out.println();
        // System.out.println(Bitboard.bitboardToString(Piece.getMoveBitmap(KNIGHT, (1L << 39 | 1L << 45), 1L << 45, 0L, (byte) 0)));

        // long n = 1L;
        // for (int i = 0; i < 7; i++) {
        //     System.out.println(Bitboard.bitboardToString(n));
        //     n |= Piece.getMoveBitmap(KNIGHT, n, 0, 0);
        // }
    }
}
