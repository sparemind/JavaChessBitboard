package board;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static board.Piece.*;
import static board.Piece.Player.BLACK;
import static board.Piece.Player.WHITE;
import static board.Piece.Type.PAWN;

/**
 * A bitboard representation of a chess position.
 * <p>
 * Uses Little-Endian Rank-File (LERF) mapping.
 */
public class Bitboard {
    // @formatter:off
    public static final int SIZE = 8;
    public static final int SQUARES = 64;

    public static final int NUM_PIECES = 6;
    public static final String PIECES = "PNBRQKpnbrqk";

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

    private static final int[] LS1B_LOOKUP = {
        64, 0,  1,  39,  2, 15, 40, 23,
        3,  12, 16, 59, 41, 19, 24, 54,
        4,  -1, 13, 10, 17, 62, 60, 28,
        42, 30, 20, 51, 25, 44, 55, 47,
        5,  32, -1, 38, 14, 22, 11, 58,
        18, 53, 63,  9, 61, 27, 29, 50,
        43, 46, 31, 37, 21, 57, 52,  8,
        26, 49, 45, 36, 56,  7, 48, 35,
        6,  34, 33, -1
    };

    // Masks for the space between the king and rook when determining if
    // castling is possible. From white's perspective.
    private static final long KINGSIDE_CASTLE_MASK  = 0b01100000L;
    private static final long QUEENSIDE_CASTLE_MASK = 0b00001110L;

    // Random bitstrings used for zobrist hashing
    private static final long[] ZOBRIST = new long[781];
    // Offsets determining where in ZOBRIST the zobrist bitstrings for each of
    // the hash components starts
    public static final int CASTLE_OFFSET = 768;
    public static final int ENPASSANT_OFFSET = 772;
    public static final int TURN_OFFSET = 780;
    static {
        Random rand = new Random(0);
        for(int i = 0; i < ZOBRIST.length; i++) {
            ZOBRIST[i] = rand.nextLong();
        }
    }
    // @formatter:on

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    // Bitmaps for each player and their pieces: [player][piece]
    private final long[][] boards;
    // Number of half-moves since the last pawn capture or piece advance
    private byte halfmoveClock;
    // What move the game is one (starts at 1)
    private short fullmoves;
    // Whether a player can castle.
    // Bit 0: White can castle kingside
    // Bit 1: White can castle queenside
    // Bit 2: Black can castle kingside
    // Bit 3: Black can castle queenside
    private byte possibleCastling;
    // Square that can be moved to in an en passant capture
    private byte enpassantPosition;
    // Whether it is white's turn or not
    private boolean whitesTurn;
    // Moves that have been applied to this board, with the most recent on top
    private LinkedList<UndoMove> undoStack;
    // The zobrist hash of this board
    private long signature;

    public Bitboard(String fen) {
        this();
        init(fen);
    }

    /**
     * Creates a new empty bitboard.
     */
    public Bitboard() {
        this.boards = new long[PLAYERS][STARTING_WHITE_PIECES.length];
        this.halfmoveClock = 0;
        this.fullmoves = 1;
        this.possibleCastling = 0b1111;
        this.enpassantPosition = 0;
        this.whitesTurn = true;
        this.undoStack = new LinkedList<>();
        this.signature = 0;
    }

    public Bitboard copy() {
        Bitboard copy = new Bitboard();

        System.arraycopy(this.boards[WHITE], 0, copy.boards[WHITE], 0, this.boards[WHITE].length);
        System.arraycopy(this.boards[BLACK], 0, copy.boards[BLACK], 0, this.boards[BLACK].length);
        copy.halfmoveClock = this.halfmoveClock;
        copy.fullmoves = this.fullmoves;
        copy.possibleCastling = this.possibleCastling;
        copy.enpassantPosition = this.enpassantPosition;
        copy.whitesTurn = this.whitesTurn;
        copy.undoStack = new LinkedList<>(this.undoStack);
        copy.signature = this.signature;

        return copy;
    }

    /**
     * Counts the number of 1 bits in a given bitmap.
     *
     * @param bitmap The bitmap to count the 1 bits of.
     * @return The number of 1 bits in the given bitmap.
     */
    public static int count(long bitmap) {
        int count = 0;
        while (bitmap != 0) {
            count++;
            // Remove LS1B
            bitmap &= bitmap - 1;
        }
        return count;
    }

    public static int ls1bSquare(long x) {
        return LS1B_LOOKUP[Math.abs((int) (ls1b(x) % 67))];
    }

    public static long ls1b(long x) {
        return x & -x;
    }

    public static byte position(long square) {
        int rank = (int) (square >>> 3); // square / 8
        int file = (int) (square & 0b111); // square % 8
        return position(file, rank);
    }

    public static byte position(int file, int rank) {
        return (byte) ((rank << 4) | file);
    }

    public static int square(byte position) {
        int file = position & FILE_MASK;
        int rank = (position & RANK_MASK) >>> 4;
        return square(file, rank);
    }

    public static int square(int file, int rank) {
        return (rank << 3) + file;
    }

    public static String bitmapToString(long bitmap) {
        StringBuilder s = new StringBuilder();

        for (int rank = SIZE - 1; rank >= 0; rank--) {
            // The rank number
            s.append(rank + 1);
            // The pieces in this rank
            for (int file = 0; file < SIZE; file++) {
                int square = rank * SIZE + file;
                boolean occupied = ((1L << square) & bitmap) != 0;

                char c;
                if (occupied) {
                    c = 'X';
                } else {
                    c = '.';
                }
                s.append(' ').append(c);
            }
            s.append('\n');
        }
        // The file letters
        s.append("  ");
        for (int i = 0; i < SIZE; i++) {
            s.append((char) ('a' + i)).append(' ');
        }

        return s.toString();
    }

    public static long getFile(int file) {
        return FILES[file];
    }

    public static long getRank(int rank) {
        return RANKS[rank];
    }

    /**
     * @param fen
     */
    private void init(String fen) {
        for (int player = 0; player < this.boards.length; player++) {
            for (int piece = 0; piece < this.boards.length; piece++) {
                this.boards[player][piece] = 0L;
            }
        }
        this.possibleCastling = 0;

        String[] fenParts = fen.split(" ");
        String pieces = fenParts[0];
        String turn = fenParts[1];
        String castling = fenParts[2];
        String enpassant = fenParts[3];
        String halfmoves = fenParts[4];
        String fullmoves = fenParts[5];

        // Board layout
        int square = SQUARES - SIZE; // A8
        for (char c : pieces.toCharArray()) {
            switch (c) {
                case '8':
                case '7':
                case '6':
                case '5':
                case '4':
                case '3':
                case '2':
                case '1':
                    square += (c - '0');
                    break;
                case '/':
                    square -= SIZE * 2;
                    break;
                default:
                    int pieceType = PIECES.indexOf(c);
                    this.boards[pieceType < NUM_PIECES ? 0 : 1][pieceType % NUM_PIECES] |= 1L << square;
                    square++;
                    break;
            }
        }

        // Current player to move
        this.whitesTurn = turn.equals("w");

        // Available castling options
        if (castling.contains("K")) {
            this.possibleCastling |= 0b0001;
        }
        if (castling.contains("Q")) {
            this.possibleCastling |= 0b0010;
        }
        if (castling.contains("k")) {
            this.possibleCastling |= 0b0100;
        }
        if (castling.contains("q")) {
            this.possibleCastling |= 0b1000;
        }

        // En passant square
        if (enpassant.equals("-")) {
            this.enpassantPosition = 0;
        } else {
            int file = enpassant.charAt(0) - 'a';
            int rank = enpassant.charAt(1) - '1';
            this.enpassantPosition = position(file, rank);
        }

        // Number of half-moves made since the last capture or pawn move
        this.halfmoveClock = (byte) Integer.parseInt(halfmoves);

        // The number of half-moves made in the game so far
        this.fullmoves = (short) (Integer.parseInt(fullmoves));
    }

    /**
     * Initializes this bitboard to be the starting position of a game.
     */
    public void initStartingBoard() {
        System.arraycopy(STARTING_WHITE_PIECES, 0, this.boards[WHITE], 0, STARTING_WHITE_PIECES.length);
        System.arraycopy(STARTING_BLACK_PIECES, 0, this.boards[BLACK], 0, STARTING_BLACK_PIECES.length);
        this.halfmoveClock = 0;
        this.fullmoves = 1;
        this.possibleCastling = 0b1111;
        this.enpassantPosition = 0;
        this.whitesTurn = true;
    }

    public void applyMove(Move move) {
        this.undoStack.addFirst(new UndoMove(this.enpassantPosition, this.halfmoveClock, this.possibleCastling, move));

        final byte color = (byte) (this.whitesTurn ? 0 : 1);
        final long srcSquare = square(move.src.position);
        final long destSquare = square(move.dest.position);
        final byte srcPiece = move.src.type();
        final byte destPiece = move.dest.type();

        // Remove the piece from the source square
        this.boards[color][srcPiece] &= ~(1L << srcSquare);

        if (move.isPromotion()) {
            // If the piece is a pawn being promoted, put the new piece
            // on the destination square
            final byte promotionPiece = move.promotionPiece();
            this.boards[color][promotionPiece] |= (1L << destSquare);
        } else {
            // Otherwise, put the same piece back on the destination square
            this.boards[color][srcPiece] |= (1L << destSquare);
        }
        // If this move is a capture, remove the enemy piece
        if (move.isCapture() && !move.isEnpassant()) {
            this.boards[1 - color][destPiece] &= ~(1L << destSquare);

            if (destPiece == Type.ROOK) {
                byte rookFile = move.dest.file();
                byte mask = 0;
                if (rookFile == File.H) {
                    mask = 0b01;
                } else if (rookFile == File.A) {
                    mask = 0b10;
                }
                // Change the castling options of the OTHER player
                mask <<= this.whitesTurn ? 2 : 0;
                this.possibleCastling &= ~mask;
            }
        }
        // If the king moves, no castling is possible
        if (srcPiece == Type.KING) {
            if (this.whitesTurn) {
                this.possibleCastling &= 0b1100;
            } else {
                this.possibleCastling &= 0b0011;
            }
        }
        // If a rook moves, castling on that side is no longer possible
        if (srcPiece == Type.ROOK) {
            byte rookFile = move.src.file();
            byte mask = 0;
            if (rookFile == File.H) {
                mask = 0b01;
            } else if (rookFile == File.A) {
                mask = 0b10;
            }
            mask <<= this.whitesTurn ? 0 : 2;
            this.possibleCastling &= ~mask;
        }
        if (move.toString().equals("b4a3")) {
            // System.out.println(move.code);
            // System.out.println(Bitboard.bitmapToString(this.boards[1 - color][0]));
        }
        // If an en passant move, TODO
        if (move.isEnpassant()) {
            // TODO: Different files doesn't automatically mean en passant
            // TODO: also need dest to be empty -- or: check if dest==enpassantPosition ???

            if (this.whitesTurn) {
                this.boards[1 - color][Type.PAWN] &= ~((1L << destSquare) >>> SIZE);
            } else {
                this.boards[1 - color][Type.PAWN] &= ~((1L << destSquare) << SIZE);
            }
        }

        // If this is a castling move, everything can happen as normal. The only
        // thing left to do now is move the rook into place.
        if (move.isCastle()) {
            int rank = this.whitesTurn ? 0 : 7;
            int rookSrcFile;
            int rookDestFile;
            if (move.castleType() == 1) {
                // King-side castle
                rookSrcFile = File.H;
                rookDestFile = File.F;
                this.possibleCastling &= ~(0b01 << (this.whitesTurn ? 0 : 2));
            } else {
                // Queen-side castle
                rookSrcFile = File.A;
                rookDestFile = File.D;
                this.possibleCastling &= ~(0b10 << (this.whitesTurn ? 0 : 2));
            }
            this.boards[color][Type.ROOK] &= ~(1L << square(rookSrcFile, rank));
            this.boards[color][Type.ROOK] |= 1L << square(rookDestFile, rank);
        }

        // Update the half-move clock
        this.halfmoveClock++;
        if (srcPiece == PAWN || move.isCapture()) {
            this.halfmoveClock = 0;
        }

        // Update en passant square
        if (move.isDoublePush()) {
            int epRank = move.src.rank() + (this.whitesTurn ? 1 : -1);
            this.enpassantPosition = position(move.src.file(), epRank);
        } else {
            this.enpassantPosition = 0;
        }

        // Switch turns
        this.whitesTurn = !this.whitesTurn;

        // If black's turn just ended, increment the full move clock
        if (this.whitesTurn) {
            this.fullmoves++;
        }
    }

    public void undoMove() {
        if (this.undoStack.isEmpty()) {
            return;
        }

        UndoMove undoMove = this.undoStack.removeFirst();

        // Switch turns
        this.whitesTurn = !this.whitesTurn;

        final byte color = (byte) (this.whitesTurn ? 0 : 1);
        final long srcSquare = square(undoMove.move.src.position);
        final long destSquare = square(undoMove.move.dest.position);
        final byte srcPiece = undoMove.move.src.type();
        final byte destPiece = undoMove.move.dest.type();

        // Add the piece back to the source square
        this.boards[color][srcPiece] |= 1L << srcSquare;

        if (undoMove.move.isPromotion()) {
            // If the piece was a pawn that was promoted, remove the promoted
            // piece from the destination square
            final byte promotionPiece = undoMove.move.promotionPiece();
            this.boards[color][promotionPiece] &= ~(1L << destSquare);
        } else {
            // Otherwise, remove the moved piece from the destination square
            this.boards[color][srcPiece] &= ~(1L << destSquare);
        }
        // If this move was a capture, put the enemy piece back
        if (undoMove.move.isCapture() && !undoMove.move.isEnpassant()) {
            this.boards[1 - color][destPiece] |= 1L << destSquare;

            if (destPiece == Type.ROOK) {
                byte rookFile = undoMove.move.dest.file();
                byte mask = 0;
                if (rookFile == File.H) {
                    mask = 0b01;
                } else if (rookFile == File.A) {
                    mask = 0b10;
                }
                // Change the castling options of the OTHER player
                mask <<= this.whitesTurn ? 2 : 0;
                this.possibleCastling |= mask;
            }
        }

        // If it was a castling move, everything can happen as normal. The only
        // thing left to do now is move the rook back into place.
        if (undoMove.move.isCastle()) {
            int rank = this.whitesTurn ? 0 : 7;
            int rookSrcFile;
            int rookDestFile;

            if (undoMove.move.castleType() == 1) {
                // King-side castle
                rookSrcFile = File.H;
                rookDestFile = File.F;
                this.possibleCastling |= 0b01 << (this.whitesTurn ? 0 : 2);
            } else {
                // Queen-side castle
                rookSrcFile = File.A;
                rookDestFile = File.D;
                this.possibleCastling |= 0b10 << (this.whitesTurn ? 0 : 2);
            }
            this.boards[color][Type.ROOK] &= ~(1L << square(rookDestFile, rank));
            this.boards[color][Type.ROOK] |= 1L << square(rookSrcFile, rank);
        }
        // If an en passant move, TODO
        if (undoMove.move.isEnpassant()) {
            if (this.whitesTurn) {
                this.boards[1 - color][Type.PAWN] |= ((1L << destSquare) >>> SIZE);
                // this.boards[1 - color][Type.PAWN] |= ((1L << (destSquare - SIZE)));
            } else {
                this.boards[1 - color][Type.PAWN] |= ((1L << destSquare) << SIZE);
                // this.boards[1 - color][Type.PAWN] |= ((1L << (destSquare + SIZE)));
            }
        }

        // Update castling options
        this.possibleCastling = undoMove.possibleCastling;

        // Update the half-move clock
        this.halfmoveClock = undoMove.halfmoveClock;

        // Update en passant square
        this.enpassantPosition = undoMove.enpassantPosition;

        // If it was white's turn, decrement the full move clock
        if (!this.whitesTurn) {
            this.fullmoves--;
        }
    }

    public List<Move> generateMoves() {
        final List<Move> pseudoMoves = generatePseudoMoves();
        final List<Move> moves = new ArrayList<>();

        final byte color = (byte) (this.whitesTurn ? 0 : 1);
        for (Move move : pseudoMoves) {
            // Check if a castling move can legally be carried out.
            if (move.isCastle()) {
                byte rank = (byte) (this.whitesTurn ? 0 : 7);
                long attackBitmap = generateAttackBitmap(1 - color);

                // If the king is in check, it can't castle
                if ((attackBitmap & this.boards[color][Type.KING]) != 0) {
                    continue;
                }

                // The file inbetween the king and its castling destination
                byte inbetweenFile;
                if (move.castleType() == 1) {
                    // King-side castle
                    inbetweenFile = File.F;
                } else {
                    // Queen-side castle
                    inbetweenFile = File.D;
                }

                // If the king would pass through check, it can't castle
                if ((attackBitmap & (1L << square(inbetweenFile, rank))) != 0) {
                    continue;
                }
            }

            applyMove(move);
            if (!inCheck(color)) {
                moves.add(move);
            }
            undoMove();
        }

        return moves;
    }

    public boolean inCheck(int player) {
        long attackBitmap = generateAttackBitmap(1 - player);

        return (this.boards[player][Type.KING] & attackBitmap) != 0;
    }

    public List<Move> generatePseudoMoves() {
        final byte color = (byte) (this.whitesTurn ? 0 : 1);
        final long[] playerBoards = this.boards[color];
        final long[] enemyBoards = this.boards[1 - color];

        long playerBitmap = 0;
        long enemyBitmap = 0;
        for (int i = 0; i < NUM_PIECES; i++) {
            playerBitmap |= playerBoards[i];
            enemyBitmap |= enemyBoards[i];
        }
        long blockers = playerBitmap | enemyBitmap;
        final long enpassantBoard = (this.enpassantPosition == 0) ? 0 : 1L << (square(this.enpassantPosition));

        List<Move> moves = new ArrayList<>();
        // Process each piece type for this player
        for (int piece = 0; piece < playerBoards.length; piece++) {
            // The bitmap of this piece
            long pieceBoard = playerBoards[piece];
            // Process all pieces in this bitmap
            while (pieceBoard != 0) {
                // Get a bitmap containing just the next piece to generate moves
                // for. Also get the coordinates of this piece.
                long nextPieceSquare = ls1b(pieceBoard);
                byte position = position(ls1bSquare(nextPieceSquare));

                // Get a bitmap of all locations this piece can move to
                long pieceMovesBoard = Piece.getMoveBitmap(this.whitesTurn, piece, nextPieceSquare, playerBitmap, enemyBitmap, enpassantBoard);

                // Create a move for each destination
                Piece src = new Piece(color, (byte) piece, position);
                while (pieceMovesBoard != 0) {
                    // Get a bitmap containing just the move destination square.
                    // Also get the coordinates of this destination square.
                    long nextDestSquare = ls1b(pieceMovesBoard);
                    byte destPosition = position(ls1bSquare(nextDestSquare));

                    // Determine the piece type of the destination square
                    int destPiece = Type.EMPTY;
                    for (int enemyPiece = 0; enemyPiece < enemyBoards.length; enemyPiece++) {
                        if ((nextDestSquare & enemyBoards[enemyPiece]) != 0) {
                            destPiece = enemyPiece;
                            break;
                        }
                    }

                    Piece dest = new Piece((byte) (1 - color), (byte) destPiece, destPosition);
                    byte promotionPiece = 0;
                    boolean isCapture = destPiece != Type.EMPTY;
                    if (piece == Type.PAWN && (dest.rank() == 0 || dest.rank() == 7)) {
                        // promotionPiece = Type.QUEEN;

                        moves.add(new Move(src, dest, (byte) Type.QUEEN, 0, isCapture));
                        moves.add(new Move(src, dest, (byte) Type.ROOK, 0, isCapture));
                        moves.add(new Move(src, dest, (byte) Type.KNIGHT, 0, isCapture));
                        moves.add(new Move(src, dest, (byte) Type.BISHOP, 0, isCapture));
                    } else {
                        moves.add(new Move(src, dest, promotionPiece, 0, isCapture));
                    }

                    // Move asdf = moves.get(moves.size() - 1);
                    // if (asdf.isEnpassant()) {
                    //     System.out.println(asdf);
                    //     System.out.println(this);
                    // }

                    // // If this is an enpassant move, set the captured piece to
                    // // be a pawn
                    // // if (piece == Type.PAWN && (position(nextDestSquare) & FILE_MASK) != src.file()) {
                    // //     destPiece = Type.PAWN;
                    // // }
                    // Piece dest = new Piece((byte) (1 - color), (byte) destPiece, destPosition);
                    // boolean isCapture = destPiece != Type.EMPTY;
                    // // if (piece == Type.PAWN && (dest.rank() == 0 || dest.rank() == 7)) {
                    // //     moves.add(new Move(src, dest, (byte) Type.QUEEN, 0, isCapture));
                    // //     moves.add(new Move(src, dest, (byte) Type.ROOK, 0, isCapture));
                    // //     moves.add(new Move(src, dest, (byte) Type.KNIGHT, 0, isCapture));
                    // //     moves.add(new Move(src, dest, (byte) Type.BISHOP, 0, isCapture));
                    // // } else {
                    // moves.add(new Move(src, dest, (byte) 0, 0, isCapture));
                    // // }

                    // Remove LS1B
                    pieceMovesBoard &= pieceMovesBoard - 1;
                }

                // Remove LS1B
                pieceBoard &= ~nextPieceSquare;
            }
        }

        // Determine castling moves
        boolean canKingsideCastle = this.whitesTurn ? (this.possibleCastling & 0b0001) != 0 : (this.possibleCastling & 0b0100) != 0;
        boolean canQueensideCastle = this.whitesTurn ? (this.possibleCastling & 0b0010) != 0 : (this.possibleCastling & 0b1000) != 0;
        byte rank = (byte) (this.whitesTurn ? 0 : 7);
        long kingsideMask = KINGSIDE_CASTLE_MASK << (this.whitesTurn ? 0 : 7 * SIZE);
        long queensideMask = QUEENSIDE_CASTLE_MASK << (this.whitesTurn ? 0 : 7 * SIZE);
        Piece src = new Piece(color, (byte) Type.KING, position(File.E, rank));
        if (canKingsideCastle && (kingsideMask & blockers) == 0) {
            Piece dest = new Piece(color, (byte) Type.EMPTY, position(File.G, rank));
            moves.add(new Move(src, dest, 0, Move.KINGSIDE_CASTLE));
        }
        if (canQueensideCastle && (queensideMask & blockers) == 0) {
            Piece dest = new Piece(color, (byte) Type.EMPTY, position(File.C, rank));
            moves.add(new Move(src, dest, 0, Move.QUEENSIDE_CASTLE));
        }

        return moves;
    }

    private long generateAttackBitmap(int player) {
        final long[] playerBoards = this.boards[player];
        final long[] enemyBoards = this.boards[1 - player];

        long playerBitmap = 0;
        long enemyBitmap = 0;
        for (int i = 0; i < NUM_PIECES; i++) {
            playerBitmap |= playerBoards[i];
            enemyBitmap |= enemyBoards[i];
        }
        final long enpassantBoard = (this.enpassantPosition == 0) ? 0 : 1L << (square(this.enpassantPosition));

        long result = 0;
        for (int piece = 0; piece < playerBoards.length; piece++) {
            // The bitmap of this piece
            long pieceBoard = playerBoards[piece];

            result |= Piece.getMoveBitmap(player == 0, piece, pieceBoard, playerBitmap, enemyBitmap, enpassantBoard, true);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        char[][] combinedBoard = new char[SIZE][SIZE];

        // Fill combinedBoard with characters representing each square/piece
        for (int player = 0; player < this.boards.length; player++) {
            for (int piece = 0; piece < this.boards[player].length; piece++) {
                long pieceBoard = this.boards[player][piece];
                for (int i = 0; i < SQUARES; i++) {
                    boolean occupied = ((1L << i) & pieceBoard) != 0;
                    int rank = i / SIZE;
                    int file = i % SIZE;

                    if (occupied) {
                        char c = PIECES.charAt(player * NUM_PIECES + piece);
                        combinedBoard[rank][file] = c;
                    } else if (combinedBoard[rank][file] == 0) {
                        char c = '.';
                        combinedBoard[rank][file] = c;
                    }
                }
            }
        }

        // Process each rank
        for (int i = combinedBoard.length - 1; i >= 0; i--) {
            // The rank number
            s.append(i + 1);
            // The pieces in this rank
            for (char square : combinedBoard[i]) {
                s.append(' ').append(square);
            }
            s.append('\n');
        }
        // The file letters
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
