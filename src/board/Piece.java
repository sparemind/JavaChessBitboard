package board;

import static board.Bitboard.File;

/**
 * TODO
 */
public class Piece {
    // The number of players
    public static final int PLAYERS = 2;

    // Bit mask to get the lowest 7 bits of a byte
    public static final byte TYPE_MASK = (byte) 0b01111111;
    // Bit mask to get the highest bit of a byte
    public static final byte COLOR_MASK = (byte) 0b10000000;
    // Bit mask to get the lowest 4 bits of a byte
    public static final byte FILE_MASK = 0b00001111;
    // Bit mask to get the highest 4 bits of a byte
    public static final byte RANK_MASK = (byte) 0b11110000;

    // Compass direction
    // @formatter:off
    private static final int N  = 8;
    private static final int NE = 9;
    private static final int E  = 1;
    private static final int SE = -7;
    private static final int S  = -8;
    private static final int SW = -9;
    private static final int W  = -1;
    private static final int NW = 7;
    // @formatter:on

    // This piece's color and type.
    // Highest bit is the color, all others are the type.
    public final byte piece;
    // This piece's position.
    // Lowest 4 bits are the file, highest 4 bits are the rank
    public final byte position;

    /**
     * Creates a new piece.
     *
     * @param color     The color of the piece.
     * @param pieceType The Type of this piece.
     * @param position  The position of this piece on the board. The lowest 4
     *                  bits are the file, the highest 4 bits are the rank.
     */
    public Piece(byte color, byte pieceType, byte position) {
        this((byte) ((color << 7) | pieceType), position);
    }

    /**
     * Creates a new piece.
     *
     * @param piece    The combined color and type of this piece. The color is
     *                 the highest bit and the other 7 are the Type.
     * @param position The position of this piece on the board. The lowest 4
     *                 bits are the file, the highest 4 bits are the rank.
     */
    public Piece(byte piece, byte position) {
        this.piece = piece;
        this.position = position;
    }

    /**
     * Returns what type of piece this is.
     *
     * @return The type of piece this is.
     */
    public byte type() {
        return (byte) (this.piece & TYPE_MASK);
    }

    /**
     * Returns the 0-indexed file number of this piece.
     *
     * @return The 0-indexed file number of this piece.
     */
    public byte file() {
        return (byte) (this.position & FILE_MASK);
    }

    /**
     * Returns the 0-indexed rank number of this piece.
     *
     * @return The 0-indexed rank number of this piece.
     */
    public byte rank() {
        return (byte) ((this.position & RANK_MASK) >>> 4);
    }

    /**
     * TODO
     *
     * @param piece
     * @param pieceBoard
     * @param myBoard
     * @param enemyBoard
     * @param enpassantBoard
     * @return
     */
    public static long getMoveBitmap(boolean isWhite, int piece, long pieceBoard, long myBoard, long enemyBoard, long enpassantBoard) {
        switch (piece) {
            case Type.PAWN:
                return getPawnBitmap(isWhite, pieceBoard, myBoard, enemyBoard, enpassantBoard);
            case Type.KNIGHT:
                return getKnightBitmap(pieceBoard, myBoard);
            case Type.BISHOP:
                return getBishopBitmap(pieceBoard, myBoard, enemyBoard);
            case Type.ROOK:
                return getRookBitmap(pieceBoard, myBoard, enemyBoard);
            case Type.QUEEN:
                return getQueenBitmap(pieceBoard, myBoard, enemyBoard);
            case Type.KING:
                return getKingBitmap(pieceBoard, myBoard);
            default:
                return 0L;
        }
    }

    /**
     * @param pieceBoard
     * @param myBoard
     * @param enemyBoard
     * @param enpassantBoard
     * @return
     */
    private static long getPawnBitmap(boolean isWhite, long pieceBoard, long myBoard, long enemyBoard, long enpassantBoard) {
        long thirdRank = isWhite ? Bitboard.getRank(2) : Bitboard.getRank(5);
        // The leftmost and rightmost files from the perspective of this player
        long leftFile = isWhite ? Bitboard.getFile(File.A) : Bitboard.getFile(File.H);
        long rightFile = isWhite ? Bitboard.getFile(File.H) : Bitboard.getFile(File.A);

        long onePush;
        long doublePush;
        long attack = 0;

        if (isWhite) {
            // All open squares that can be moved to by moving one square forward
            onePush = (pieceBoard << 8) & ~(myBoard | enemyBoard);
            // All open squares that can be moved to by moving two squares forward
            // from the starting rank
            doublePush = ((onePush & thirdRank) << 8) & ~(myBoard | enemyBoard);
            // All squares that are being threatened
            attack |= (pieceBoard << (8 - 1)) & ~rightFile;
            attack |= (pieceBoard << (8 + 1)) & ~leftFile;
        } else {
            // All open squares that can be moved to by moving one square forward
            onePush = (pieceBoard >>> 8) & ~(myBoard | enemyBoard);
            // All open squares that can be moved to by moving two squares forward
            // from the starting rank
            doublePush = ((onePush & thirdRank) >>> 8) & ~(myBoard | enemyBoard);
            // All squares that are being threatened
            attack |= (pieceBoard >>> (8 - 1)) & ~rightFile;
            attack |= (pieceBoard >>> (8 + 1)) & ~leftFile;
        }

        // All squares that can be moved to as part of a capture
        long capture = attack & (enemyBoard | enpassantBoard);

        return onePush | doublePush | capture;
    }

    /**
     * @param pieceBoard
     * @param myBoard
     * @return
     */
    private static long getKnightBitmap(long pieceBoard, long myBoard) {
        long result = 0;

        // North-East-East and South-East-East
        result |= (pieceBoard << 10) & ~(Bitboard.getFile(File.A) | Bitboard.getFile(File.B));
        result |= (pieceBoard >>> 6) & ~(Bitboard.getFile(File.A) | Bitboard.getFile(File.B));

        // North-North-East and South-South-East
        result |= (pieceBoard << 17) & ~(Bitboard.getFile(File.A));
        result |= (pieceBoard >>> 15) & ~(Bitboard.getFile(File.A));

        // North-North-West and South-South-West
        result |= (pieceBoard << 15) & ~(Bitboard.getFile(File.H));
        result |= (pieceBoard >>> 17) & ~(Bitboard.getFile(File.H));

        // North-West-West and South-West-West
        result |= (pieceBoard << 6) & ~(Bitboard.getFile(File.G) | Bitboard.getFile(File.H));
        result |= (pieceBoard >>> 10) & ~(Bitboard.getFile(File.G) | Bitboard.getFile(File.H));

        // Exclude own pieces, since they cannot be captured
        result &= ~myBoard;

        return result;
    }

    private static long getBishopBitmap(long pieceBoard, long myBoard, long enemyBoard) {
        long result = 0;

        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, NE, Bitboard.getFile(File.A));
        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, SE, Bitboard.getFile(File.A));
        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, SW, Bitboard.getFile(File.H));
        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, NW, Bitboard.getFile(File.H));

        return result;
    }

    private static long getRookBitmap(long pieceBoard, long myBoard, long enemyBoard) {
        long result = 0;

        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, N, 0);
        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, E, Bitboard.getFile(File.A));
        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, S, 0);
        result |= dumb7Fill(pieceBoard, myBoard, enemyBoard, W, Bitboard.getFile(File.H));

        return result;
    }

    private static long getQueenBitmap(long pieceBoard, long myBoard, long enemyBoard) {
        return getRookBitmap(pieceBoard, myBoard, enemyBoard) | getBishopBitmap(pieceBoard, myBoard, enemyBoard);
    }

    private static long getKingBitmap(long pieceBoard, long myBoard) {
        long result = 0;

        result |= (pieceBoard << NW) & ~(myBoard | Bitboard.getFile(File.H));
        result |= (pieceBoard << N) & ~myBoard;
        result |= (pieceBoard << NE) & ~(myBoard | Bitboard.getFile(File.A));
        result |= (pieceBoard << E) & ~(myBoard | Bitboard.getFile(File.A));

        result |= (pieceBoard >>> NW) & ~(myBoard | Bitboard.getFile(File.A));
        result |= (pieceBoard >>> N) & ~myBoard;
        result |= (pieceBoard >>> NE) & ~(myBoard | Bitboard.getFile(File.H));
        result |= (pieceBoard >>> E) & ~(myBoard | Bitboard.getFile(File.H));

        return result;
    }

    private static long dumb7Fill(long pieceBoard, long myBoard, long enemyBoard, int direction, long exclusionMask) {
        long result = pieceBoard;

        long empty = ~(myBoard | enemyBoard | exclusionMask);
        if (direction > 0) {
            result |= (result << direction) & empty;
            result |= (result << direction) & empty;
            result |= (result << direction) & empty;
            result |= (result << direction) & empty;
            result |= (result << direction) & empty;
            result |= (result << direction) & empty;
            result |= (result << direction) & empty;
            // Include the blocker
            result |= (result << direction) & ~exclusionMask & enemyBoard;
        } else {
            direction = -direction;
            result |= (result >>> direction) & empty;
            result |= (result >>> direction) & empty;
            result |= (result >>> direction) & empty;
            result |= (result >>> direction) & empty;
            result |= (result >>> direction) & empty;
            result |= (result >>> direction) & empty;
            result |= (result >>> direction) & empty;
            // Include the blocker
            result |= (result >>> direction) & ~exclusionMask & enemyBoard;
        }

        return result & ~pieceBoard;
    }

    @Override
    public String toString() {
        char file = (char) ('a' + file());
        int rank = 1 + rank();
        return "" + file + rank;
    }

    public static class Player {
        public static final int WHITE = 0;
        public static final int BLACK = 1;
    }

    public static class Type {
        public static final int PAWN = 0;
        public static final int KNIGHT = 1;
        public static final int BISHOP = 2;
        public static final int ROOK = 3;
        public static final int QUEEN = 4;
        public static final int KING = 5;
        public static final int EMPTY = -1;
    }
}
