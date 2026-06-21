package com.chess.patterns.observer;

import com.chess.model.Move;

public interface GameObserver {
    void onMoveExecuted(String gameId, Move move);
}
