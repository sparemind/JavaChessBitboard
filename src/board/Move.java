package board;

import static board.Piece.FILE_MASK;
import static board.Piece.RANK_MASK;

/**
 * TODO
 */
public class Move implements Comparable<Move> {
    public final Piece src;
    public final Piece dest;
    public final byte promotion;
    public final int score;
    public final boolean isCapture;

    public Move(Piece src, Piece dest, byte promotion, int score, boolean isCapture) {
        this.src = src;
        this.dest = dest;
        this.promotion = promotion;
        this.score = score;
        this.isCapture = isCapture;
    }

    @Override
    public int compareTo(Move o) {
        return o.score - this.score;
    }

    @Override
    public String toString() {
        char srcFile = (char) ('a' + (this.src.position & FILE_MASK));
        int srcRank = 1 + ((this.src.position & RANK_MASK) >>> 4);
        char destFile = (char) ('a' + (this.dest.position & FILE_MASK));
        int destRank = 1 + ((this.dest.position & RANK_MASK) >>> 4);

        return "" + srcFile + srcRank + destFile + destRank;
    }
}
