import board.Bitboard;
import board.Move;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Bitboard b = new Bitboard("r3k2r/Pppp1ppp/1b3nbN/nP6/BBPNP3/q7/P2P2PP/r2Q1RK1 w kq -");
        // b.initStartingBoard();

        List<Move> moves = b.generateMoves();
        System.out.println(moves.size());
        System.out.println(moves);
        System.out.println(b);
        // b.applyMove(moves.get(16));
        // System.out.println(b);
        // moves = b.generateMoves();
        // System.out.println(moves);
        // b.applyMove(moves.get(34));
        // System.out.println(b);
        // moves = b.generateMoves();
        // System.out.println(moves);
        // b.applyMove(moves.get(17));
        // System.out.println(b);
        // System.out.println(Long.toBinaryString(b.possibleCastling));
        // b.applyMove(moves.get(1));
        // b.undoMove();
        // System.out.println(b);

        //
        // int move = 3;
        // b.applyMove(moves.get(move));
        // b.undoMove();
        // // b.applyMove(moves.get(move));
        // System.out.println(b);
        //
        // int i = 0;
        // for (Move m : moves) {
        //     System.out.println(i++);
        //     b.applyMove(m);
        //     System.out.println(b);
        //     b.undoMove();
        // }

        // System.out.println();
        // System.out.println(Bitboard.bitmapToString(Piece.getMoveBitmap(KNIGHT, (1L << 39 | 1L << 45), 1L << 45, 0L, (byte) 0)));
        // System.out.println(Bitboard.bitmapToString(Piece.getMoveBitmap(BISHOP, 1L << 32, 0, 1L << 41, (byte) 0)));
        // System.out.println(Bitboard.bitmapToString(Piece.getMoveBitmap(KING, 1L << 36, 1L << 36, 0, (byte) 0)));


        // long n = 1L;
        // for (int i = 0; i < 7; i++) {
        //     System.out.println(Bitboard.bitmapToString(n));
        //     n |= Piece.getMoveBitmap(KNIGHT, n, 0, 0);
        // }
    }
}
