package com.chess.patterns.strategy;

import com.chess.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBotStrategy implements MoveStrategy {
    public Move calculateMove(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.getPiece(from);
                if (piece != null && piece.getColor() == PieceColor.BLACK) {
                    // ডেমো বট চাল জেনারেটর (র্যান্ডম সামনের ১ ঘর খালি থাকলে চালবে)
                    Position to = new Position(r + 1, c);
                    if (to.isValid() && board.getPiece(to) == null) {
                        legalMoves.add(new Move(from, to));
                    }
                }
            }
        }
        return legalMoves.isEmpty() ? null : legalMoves.get(new Random().nextInt(legalMoves.size()));
    }
}
