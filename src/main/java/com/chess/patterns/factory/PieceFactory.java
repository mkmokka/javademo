package com.chess.patterns.factory;

import com.chess.model.Piece;
import com.chess.model.PieceColor;
import com.chess.model.PieceType;

public class PieceFactory {
    public static Piece createPiece(PieceType type, PieceColor color) {
        return new Piece(type, color);
    }
}
