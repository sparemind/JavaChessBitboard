package board;

import static board.Piece.FILE_MASK;
import static board.Piece.RANK_MASK;

/**
 * TODO
 */
public class Move implements Comparable<Move> {
    public static final byte KINGSIDE_CASTLE = 1;
    public static final byte QUEENSIDE_CASTLE = 2;

    public static final byte PROMOTION_MASK = 0b1000;
    public static final byte CAPTURE_MASK = 0b0100;
    public static final byte SPECIAL_MASK = 0b0011;

    public final Piece src;
    public final Piece dest;
    public final int score;
    // Bit 0: Special 0 -- Used to store additional data
    // Bit 1: Special 1 -- Used to store additional data
    // Bit 2: Capture   -- 1 iff this move is a capture
    // Bit 3: Promotion -- 1 iff this move is a promotion
    public final byte code;

    public Move(Piece src, Piece dest, int score, byte castle) {
        this(src, dest, (byte) 0, score, false, castle);
    }

    public Move(Piece src, Piece dest, byte promotion, int score, boolean isCapture) {
        this(src, dest, promotion, score, isCapture, (byte) 0);
    }

    private Move(Piece src, Piece dest, byte promotion, int score, boolean isCapture, byte castle) {
        this.src = src;
        this.dest = dest;
        this.score = score;

        byte b = 0;
        if (isCapture) {
            b |= CAPTURE_MASK;
        }
        if (castle == KINGSIDE_CASTLE) {
            // King-side castle
            b |= 0b0010;
        }
        if (castle == QUEENSIDE_CASTLE) {
            // Queen-side castle
            b |= 0b0011;
        }
        b |= (promotion - 1) & SPECIAL_MASK;
        this.code = b;
    }

    /**
     * Returns whether this move is a capture.
     *
     * @return True iff this move is a capture.
     */
    public boolean isCapture() {
        return (this.code & CAPTURE_MASK) != 0;
    }

    /**
     * Returns whether this move is a castling move.
     *
     * @return True iff this move is a castling move.
     */
    public boolean isCastle() {
        return !isCapture() && !isPromotion() && (this.code & SPECIAL_MASK) != 0;
    }

    /**
     * Returns what kind of castling move this is.
     *
     * @return 0 if this isn't a castling move, 1 if it is a king side castle, 2
     * if it is a queen side castle.
     */
    public byte castleType() {
        if (!isCastle()) {
            return 0;
        }
        byte c = (byte) (this.code & SPECIAL_MASK);
        return (byte) (c & (c << 1));
    }

    /**
     * Returns whether this move is a promotion.
     *
     * @return True iff this move is a promotion.
     */
    public boolean isPromotion() {
        return (this.code & PROMOTION_MASK) != 0;
    }

    /**
     * Returns the piece type of this move's promotion.
     *
     * @return The piece type of this move's promotion, or 0 if this isn't a
     * promotion move.
     */
    public int promotionPiece() {
        if (!isPromotion()) {
            return 0;
        }
        return (this.code & SPECIAL_MASK) + 1;
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
