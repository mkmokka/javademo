package com.chess.model;

public class Piece {
    private final PieceType type;
    private final PieceColor color;

    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }

    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    public String getSymbol() {
        if (color == PieceColor.WHITE) {
            return switch (type) {
                case PAWN -> "♙";
                case ROOK -> "♖";
                case KNIGHT -> "♘";
                case BISHOP -> "♗";
                case QUEEN -> "♕";
                case KING -> "♔";
            };
        } else {
            return switch (type) {
                case PAWN -> "♟";
                case ROOK -> "♜";
                case KNIGHT -> "♞";
                case BISHOP -> "♝";
                case QUEEN -> "♛";
                case KING -> "♚";
            };
        }
    }
}
