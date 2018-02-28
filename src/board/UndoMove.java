package board;

public class UndoMove {
    public final Move move;
    public final byte enpassantPosition;

    public UndoMove(Bitboard board, Move move) {
        this.move = move;
        this.enpassantPosition = board.getEnpassantPosition();
    }
}
