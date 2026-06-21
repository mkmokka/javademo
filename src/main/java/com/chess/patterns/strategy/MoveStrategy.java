package com.chess.patterns.strategy;

import com.chess.model.Board;
import com.chess.model.Move;

public interface MoveStrategy {
    Move calculateMove(Board board);
}
