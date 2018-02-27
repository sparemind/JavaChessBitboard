package board;

import static board.Bitboard.File;

/**
 * TODO
 */
public class Piece {
    // The number of players
    public static final int PLAYERS = 2;

    // Bit mask to get the lowest 7 bits of a byte
    private static final byte TYPE_MASK = (byte) 0b01111111;
    // Bit mask to get the highest bit of a byte
    private static final byte COLOR_MASK = (byte) 0b10000000;
    // Bit mask to get the highest 4 bits of a byte
    private static final byte RANK_MASK = (byte) 0b11110000;
    // Bit mask to get the lowest 4 bits of a byte
    private static final byte FILE_MASK = 0b00001111;

    // This piece's color and type.
    // Highest bit is the color, all others are the type.
    protected byte piece;
    // This piece's position.
    // Lowest 4 bits are the file, highest 4 bits are the rank
    protected byte position;

    /**
     * TODO
     *
     * @param pieceBoard
     * @param myBoard
     * @param enemyBoard
     * @return
     */
    public static long getAttack(int piece, long pieceBoard, long myBoard, long enemyBoard, short enpassantSquare) {
        switch (piece) {
            case Type.PAWN:
                return getPawnMoves(pieceBoard, myBoard, enemyBoard, enpassantSquare);
            case Type.KNIGHT:
                return getKnightMoves(pieceBoard, myBoard);
            case Type.BISHOP:
                return getBishopMove(pieceBoard, myBoard, enemyBoard);
            case Type.ROOK:
                return getRookMoves(pieceBoard, myBoard, enemyBoard);
            case Type.QUEEN:
                return getQueenMoves(pieceBoard, myBoard, enemyBoard);
            case Type.KING:
                return getKingMoves(pieceBoard, myBoard, enemyBoard);
            default:
                return 0L;
        }
    }

    private static long getPawnMoves(long pieceBoard, long myBoard, long enemyBoard, short enpassantSquare) {
        boolean isWhite = (pieceBoard & myBoard) != 0;
        int direction = isWhite ? 8 : -8;
        long thirdRank = isWhite ? 2 : 5;
        long leftFile = isWhite ? Bitboard.getFile(File.A) : Bitboard.getFile(File.H);
        long rightFile = isWhite ? Bitboard.getFile(File.H) : Bitboard.getFile(File.A);

        long result = 0;

        // All open spaces that can be moved to by moving one square forward
        long onePush = (pieceBoard << direction) & ~(myBoard | enemyBoard);
        // All open spaces that can be moved to by moving two squares forward
        // from the starting rank
        long doublePush = ((onePush & thirdRank) << direction) & ~(myBoard | enemyBoard);
        // All squares that are being threatened
        long attack = 0;
        attack |= (pieceBoard << (direction - 1)) & ~leftFile;
        attack |= (pieceBoard << (direction + 1)) & ~rightFile;


        return result;
    }

    private static long getKnightMoves(long pieceBoard, long myBoard) {
        long result = 0;

        result |= (pieceBoard << 10) & ~(Bitboard.getFile(File.A) | Bitboard.getFile(File.B));
        result |= (pieceBoard >>> 6) & ~(Bitboard.getFile(File.A) | Bitboard.getFile(File.B));

        result |= (pieceBoard << 17) & ~(Bitboard.getFile(File.A));
        result |= (pieceBoard >>> 15) & ~(Bitboard.getFile(File.A));

        result |= (pieceBoard << 15) & ~(Bitboard.getFile(File.H));
        result |= (pieceBoard >>> 17) & ~(Bitboard.getFile(File.H));

        result |= (pieceBoard << 6) & ~(Bitboard.getFile(File.G) | Bitboard.getFile(File.H));
        result |= (pieceBoard >>> 10) & ~(Bitboard.getFile(File.G) | Bitboard.getFile(File.H));

        // Exclude own pieces, since they cannot be captured
        result &= ~myBoard;

        return result;
    }

    private static long getBishopMove(long pieceBoard, long myBoard, long enemyBoard) {
        return 0;
    }

    private static long getRookMoves(long pieceBoard, long myBoard, long enemyBoard) {
        return 0;
    }

    private static long getQueenMoves(long pieceBoard, long myBoard, long enemyBoard) {
        return 0;
    }

    private static long getKingMoves(long pieceBoard, long myBoard, long enemyBoard) {
        return 0;
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
    }
}
