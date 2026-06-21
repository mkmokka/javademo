package com.chess.model;

import com.chess.patterns.factory.PieceFactory;

public class Board {
    private final Piece[][] grid;

    public Board() {
        this.grid = new Piece[8][8];
        resetBoard();
    }

    public void resetBoard() {
        // Pawns Initialization
        for (int i = 0; i < 8; i++) {
            grid[1][i] = PieceFactory.createPiece(PieceType.PAWN, PieceColor.BLACK);
            grid[6][i] = PieceFactory.createPiece(PieceType.PAWN, PieceColor.WHITE);
        }
        setupRow(0, PieceColor.BLACK);
        setupRow(7, PieceColor.WHITE);
    }

    private void setupRow(int row, PieceColor color) {
        grid[row][0] = PieceFactory.createPiece(PieceType.ROOK, color);
        grid[row][7] = PieceFactory.createPiece(PieceType.ROOK, color);
        grid[row][1] = PieceFactory.createPiece(PieceType.KNIGHT, color);
        grid[row][6] = PieceFactory.createPiece(PieceType.KNIGHT, color);
        grid[row][2] = PieceFactory.createPiece(PieceType.BISHOP, color);
        grid[row][5] = PieceFactory.createPiece(PieceType.BISHOP, color);
        grid[row][3] = PieceFactory.createPiece(PieceType.QUEEN, color);
        grid[row][4] = PieceFactory.createPiece(PieceType.KING, color);
    }

    public Piece getPiece(Position pos) {
        return (pos != null && pos.isValid()) ? grid[pos.row()][pos.col()] : null;
    }

    public void setPiece(Position pos, Piece piece) {
        if (pos != null && pos.isValid()) {
            grid[pos.row()][pos.col()] = piece;
        }
    }

    public Piece[][] getGrid() { return grid; }
}
