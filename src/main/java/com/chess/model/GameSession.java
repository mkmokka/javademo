package com.chess.model;

import com.chess.patterns.state.*;
import com.chess.patterns.strategy.*;
import java.util.UUID;

public class GameSession {
    private final String gameId;
    private final Board board;
    private GameState state;
    private MoveStrategy blackStrategy;
    private PieceColor currentTurn;
    private final String mode; // "COMPUTER" or "FRIEND"

    public GameSession(String mode) {
        this.gameId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.board = new Board();
        this.mode = mode;
        this.currentTurn = PieceColor.WHITE;
        
        if ("COMPUTER".equalsIgnoreCase(mode)) {
            this.state = new InProgressState();
            this.blackStrategy = new RandomBotStrategy();
        } else {
            this.state = new WaitingState();
            this.blackStrategy = new UserMoveStrategy();
        }
    }

    public synchronized boolean makeMove(Move move) {
        if (!state.canMove()) return false;
        
        Piece piece = board.getPiece(move.from());
        if (piece == null || piece.getColor() != currentTurn) return false;

        // চাল এবং রুলস ইমপ্লিমেন্টেশন (সিম্পল ভ্যালিডেশন)
        board.setPiece(move.to(), piece);
        board.setPiece(move.from(), null);

        // কিং ক্যাপচার চেক (উইনার ডিটেকশন রুলস)
        checkWinner();

        if (state instanceof InProgressState) {
            currentTurn = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        }
        return true;
    }

    private void checkWinner() {
        boolean whiteKingExists = false;
        boolean blackKingExists = false;
        for (Piece[] row : board.getGrid()) {
            for (Piece p : row) {
                if (p != null && p.getType() == PieceType.KING) {
                    if (p.getColor() == PieceColor.WHITE) whiteKingExists = true;
                    if (p.getColor() == PieceColor.BLACK) blackKingExists = true;
                }
            }
        }
        if (!whiteKingExists) state = new GameOverState("BLACK");
        else if (!blackKingExists) state = new GameOverState("WHITE");
    }

    // Getters & Setters
    public String getGameId() { return gameId; }
    public Board getBoard() { return board; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public MoveStrategy getBlackStrategy() { return blackStrategy; }
    public PieceColor getCurrentTurn() { return currentTurn; }
    public String getMode() { return mode; }
}
