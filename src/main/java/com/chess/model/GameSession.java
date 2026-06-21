package com.chess.model;

import java.util.UUID;

public class GameSession {
    private String gameId;
    private Board board;
    private String statusDescription;
    private PieceColor currentTurn;
    private String mode;

    public GameSession() {}

    public GameSession(String mode) {
        this.gameId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.board = new Board();
        this.mode = mode;
        this.currentTurn = PieceColor.WHITE;
        this.statusDescription = "COMPUTER".equalsIgnoreCase(mode) ? "IN_PROGRESS" : "WAITING_FOR_PLAYER";
    }

    public synchronized boolean makeMove(Move move) {
        if ("WAITING_FOR_PLAYER".equals(this.statusDescription) || this.statusDescription.startsWith("GAME_OVER")) return false;
        
        Piece piece = board.getPiece(move.from());
        if (piece == null || piece.getColor() != currentTurn) return false;

        board.setPiece(move.to(), piece);
        board.setPiece(move.from(), null);

        checkWinner();

        if ("IN_PROGRESS".equals(this.statusDescription)) {
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
        if (!whiteKingExists) this.statusDescription = "GAME_OVER_WINNER_BLACK";
        else if (!blackKingExists) this.statusDescription = "GAME_OVER_WINNER_WHITE";
    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }

    public PieceColor getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(PieceColor currentTurn) { this.currentTurn = currentTurn; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
