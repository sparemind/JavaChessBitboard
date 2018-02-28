package board;

public class UndoMove {
    public final Move move;
    public final byte enpassantPosition;
    public final byte halfmoveClock;

    public UndoMove(Bitboard board, Move move) {
        this.move = move;
        this.enpassantPosition = board.getEnpassantPosition();
        this.halfmoveClock = board.getHalfmoveClock();
    }
}
