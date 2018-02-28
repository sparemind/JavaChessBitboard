package board;

public class UndoMove {
    public final Move move;
    public final byte enpassantPosition;
    public final byte halfmoveClock;
    public final byte possibleCastling;

    public UndoMove(byte enpassantPosition, byte halfmoveClock, byte possibleCastling, Move move) {
        this.move = move;
        this.enpassantPosition = enpassantPosition;
        this.halfmoveClock = halfmoveClock;
        this.possibleCastling = possibleCastling;
    }
}
