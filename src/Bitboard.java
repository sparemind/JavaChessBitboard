public class Bitboard {
    // @formatter:off
    public static final int SIZE = 8;
    public static final int SQUARES = 64;

    public static final int PLAYERS = 2;
    public static final int WHITE = 0;
    public static final int BLACK = 1;

    public static final int PAWN   = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK   = 3;
    public static final int QUEEN  = 4;
    public static final int KING   = 5;

    public static final int NUM_PIECES = 6;
    public static final char[] PIECES = {
        'P', 'N', 'B', 'R', 'Q', 'K', // White pieces
        'p', 'n', 'b', 'r', 'q', 'k', // Black pieces
    };

    private static final int N  = 8;
    private static final int NE = 9;
    private static final int E  = 1;
    private static final int SE = -7;
    private static final int S  = -8;
    private static final int SW = -9;
    private static final int W  = -1;
    private static final int NW = 7;

    private static final long[] STARTING_WHITE_PIECES = {
        0x000000000000FF00L, // Pawns
        0x0000000000000042L, // Knights
        0x0000000000000024L, // Bishops
        0x0000000000000081L, // Rooks
        0x0000000000000008L, // Queen
        0x0000000000000010L, // King
    };

    private static final long[] STARTING_BLACK_PIECES = {
        0x00FF000000000000L, // Pawns
        0x4200000000000000L, // Knights
        0x2400000000000000L, // Bishops
        0x8100000000000000L, // Rooks
        0x0800000000000000L, // Queen
        0x1000000000000000L, // King
    };

    private static final long[] RANKS = {
        0x00000000000000FFL, // 1
        0x000000000000FF00L, // 2
        0x0000000000FF0000L, // 3
        0x00000000FF000000L, // 4
        0x000000FF00000000L, // 5
        0x0000FF0000000000L, // 6
        0x00FF000000000000L, // 7
        0xFF00000000000000L, // 8
    };

    private static final long[] FILES = {
        0x0101010101010101L, // A
        0x0202020202020202L, // B
        0x0404040404040404L, // C
        0x0808080808080808L, // D
        0x1010101010101010L, // E
        0x2020202020202020L, // F
        0x4040404040404040L, // G
        0x8080808080808080L, // H
    };
    // @formatter:on

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private final long[][] boards;
    private short halfmoveClock;
    private short ply;
    private boolean whitesTurn;

    public Bitboard() {
        this.boards = new long[PLAYERS][STARTING_WHITE_PIECES.length];
        this.halfmoveClock = 0;
        this.ply = 0;
        this.whitesTurn = true;
    }

    public void initStartingBoard() {
        System.arraycopy(STARTING_WHITE_PIECES, 0, this.boards[WHITE], 0, STARTING_WHITE_PIECES.length);
        System.arraycopy(STARTING_BLACK_PIECES, 0, this.boards[BLACK], 0, STARTING_BLACK_PIECES.length);
        this.halfmoveClock = 0;
        this.ply = 0;
        this.whitesTurn = true;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        char[][] combinedBoard = new char[SIZE][SIZE];

        for (int player = 0; player < this.boards.length; player++) {
            for (int piece = 0; piece < this.boards[player].length; piece++) {
                long pieceBoard = this.boards[player][piece];
                for (int i = 0; i < SQUARES; i++) {
                    boolean occupied = ((1L << i) & pieceBoard) != 0;
                    int rank = i / SIZE;
                    int file = i % SIZE;

                    if (occupied) {
                        char c = PIECES[player * NUM_PIECES + piece];
                        combinedBoard[rank][file] = c;
                    } else if (combinedBoard[rank][file] == 0) {
                        char c = '.';
                        combinedBoard[rank][file] = c;
                    }
                }
            }
        }

        for (int i = combinedBoard.length - 1; i >= 0; i--) {
            s.append(i + 1).append(' ');
            for (char square : combinedBoard[i]) {
                s.append(square).append(' ');
            }
            s.append('\n');
        }
        s.append("  ");
        for (int i = 0; i < SIZE; i++) {
            s.append((char) ('a' + i)).append(' ');
        }

        return s.toString();
    }

    public static class File {
        public static final int A = 0;
        public static final int B = 1;
        public static final int C = 2;
        public static final int D = 3;
        public static final int E = 4;
        public static final int F = 5;
        public static final int G = 6;
        public static final int H = 7;
    }
}
