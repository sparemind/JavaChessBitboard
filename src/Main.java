import board.Bitboard;
import board.Piece;

import static board.Piece.Type.KNIGHT;

public class Main {
    public static void main(String[] args) {
        Bitboard b = new Bitboard();
        b.initStartingBoard();

        // System.out.println(b.toString());
        // System.out.println();
        // System.out.println(Bitboard.bitboardToString(0xff00L));
        System.out.println(Bitboard.bitboardToString(Piece.getAttack(KNIGHT, (1L << 39 | 1L << 45), 1L << 45, 0L, (short) 0)));

        // long n = 1L;
        // for (int i = 0; i < 7; i++) {
        //     System.out.println(Bitboard.bitboardToString(n));
        //     n |= Piece.getAttack(KNIGHT, n, 0, 0);
        // }
    }
}
